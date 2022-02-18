package de.dlyt.yanndroid.oneui.view;

import static androidx.core.util.Preconditions.checkArgument;
import static androidx.core.view.ViewCompat.TYPE_NON_TOUCH;
import static androidx.core.view.ViewCompat.TYPE_TOUCH;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.FocusFinder;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.SectionIndexer;

import androidx.annotation.CallSuper;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.animation.SeslAnimationUtils;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslSubheaderRoundedCorner;
import androidx.core.os.TraceCompat;
import androidx.core.util.Preconditions;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.view.AbsSavedState;
import androidx.reflect.provider.SeslSystemReflector;
import androidx.reflect.view.SeslInputDeviceReflector;
import androidx.reflect.view.SeslPointerIconReflector;
import androidx.reflect.widget.SeslOverScrollerReflector;
import androidx.reflect.widget.SeslTextViewReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.AdapterHelper;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.ChildHelper;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.DefaultItemAnimator;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.FastScroller;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.GapWorker;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.GridLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.RecyclerViewAccessibilityDelegate;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslRecyclerViewFastScroller;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.ViewBoundsCheck;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.ViewInfoStore;
import de.dlyt.yanndroid.oneui.sesl.support.EdgeEffectSupport;
import de.dlyt.yanndroid.oneui.sesl.view.NestedScrollingChildHelper;
import de.dlyt.yanndroid.oneui.view.RecyclerView.ItemAnimator.ItemHolderInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild2, NestedScrollingChild3 {
    private boolean mIsOneUI4;
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC = Build.VERSION.SDK_INT >= 23;
    static final boolean ALLOW_THREAD_GAP_WORK = Build.VERSION.SDK_INT >= 21;
    public static final boolean DEBUG = false;
    public static final int DEFAULT_ORIENTATION = 1;
    static final boolean DISPATCH_TEMP_DETACH = false;
    private static final int FOCUS_MOVE_DOWN = 1;
    private static final int FOCUS_MOVE_FULL_DOWN = 3;
    private static final int FOCUS_MOVE_FULL_UP = 2;
    private static final int FOCUS_MOVE_UP = 0;
    private static final boolean FORCE_ABS_FOCUS_SEARCH_DIRECTION = Build.VERSION.SDK_INT <= 15;
    static final boolean FORCE_INVALIDATE_DISPLAY_LIST = Build.VERSION.SDK_INT == 18 || Build.VERSION.SDK_INT == 19 || Build.VERSION.SDK_INT == 20;
    public static final long FOREVER_NS = Long.MAX_VALUE;
    private static final float FRAME_LATENCY_LIMIT = 16.66f;
    private static final int GO_TO_TOP_HIDE = 1500;
    private static final int GTP_STATE_NONE = 0;
    private static final int GTP_STATE_PRESSED = 2;
    private static final int GTP_STATE_SHOWN = 1;
    @IntDef({HORIZONTAL, VERTICAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 0;
    private static final int HOVERSCROLL_DELAY = 0;
    private static final int HOVERSCROLL_DOWN = 2;
    private static final int HOVERSCROLL_HEIGHT_BOTTOM_DP = 25;
    private static final int HOVERSCROLL_HEIGHT_TOP_DP = 25;
    private static float HOVERSCROLL_SPEED = 10.0f;
    private static final int HOVERSCROLL_UP = 1;
    private static final boolean IGNORE_DETACHED_FOCUSED_CHILD = Build.VERSION.SDK_INT <= 15;
    private static final int INVALID_POINTER = -1;
    public static final int INVALID_TYPE = -1;
    private static final int LASTITEM_ADD_REMOVE_DURATION = 330;
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class, Integer.TYPE, Integer.TYPE};
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static final int MAX_SCROLL_DURATION = 2000;
    private static final int MOTION_EVENT_ACTION_PEN_DOWN = 211;
    private static final int MOTION_EVENT_ACTION_PEN_MOVE = 213;
    private static final int MOTION_EVENT_ACTION_PEN_UP = 212;
    private static final int MSG_HOVERSCROLL_MOVE = 0;
    private static final int[] NESTED_SCROLLING_ATTRS = {16843830};
    public static final long NO_ID = -1;
    public static final int NO_POSITION = -1;
    static final boolean POST_UPDATES_ON_ANIMATION = Build.VERSION.SDK_INT >= 16;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final int STATISTICS_MAX_COUNT = 5;
    static final String TAG = "SeslRecyclerView";
    public static final int TOUCH_SLOP_DEFAULT = 0;
    public static final int TOUCH_SLOP_PAGING = 1;
    static final String TRACE_BIND_VIEW_TAG = "RV OnBindView";
    static final String TRACE_CREATE_VIEW_TAG = "RV CreateView";
    private static final String TRACE_HANDLE_ADAPTER_UPDATES_TAG = "RV PartialInvalidate";
    public static final String TRACE_NESTED_PREFETCH_TAG = "RV Nested Prefetch";
    private static final String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG = "RV FullInvalidate";
    private static final String TRACE_ON_LAYOUT_TAG = "RV OnLayout";
    public static final String TRACE_PREFETCH_TAG = "RV Prefetch";
    static final String TRACE_SCROLL_TAG = "RV Scroll";
    public static final int UNDEFINED_DURATION = Integer.MIN_VALUE;
    public static final boolean VERBOSE_TRACING = false;
    static final StretchEdgeEffectFactory sDefaultEdgeEffectFactory = new StretchEdgeEffectFactory();
    private final int ON_ABSORB_VELOCITY = 10000;
    RecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    public Adapter mAdapter;
    public AdapterHelper mAdapterHelper;
    boolean mAdapterUpdateDuringMeasure;
    private int mAnimatedBlackTop = -1;
    private float mApproxLatency = 0.0f;
    public int mBlackTop = -1;
    private EdgeEffect mBottomGlow;
    Rect mChildBound = new Rect();
    private ChildDrawingOrderCallback mChildDrawingOrderCallback;
    public ChildHelper mChildHelper;
    boolean mClipToPadding;
    private View mCloseChildByBottom = null;
    private View mCloseChildByTop = null;
    private int mCloseChildPositionByBottom = -1;
    private int mCloseChildPositionByTop = -1;
    private Context mContext;
    public boolean mDataSetHasChangedAfterLayout = false;
    boolean mDispatchItemsChangedEvent = false;
    private int mDispatchScrollCounter = 0;
    private int mDistanceFromCloseChildBottom = 0;
    private int mDistanceFromCloseChildTop = 0;
    private boolean mDrawLastRoundedCorner = true;
    private boolean mDrawRect = false;
    private boolean mDrawReverse = false;
    private int mEatenAccessibilityChangeFlags;
    private boolean mEdgeEffectByDragging = false;
    private EdgeEffectFactory mEdgeEffectFactory = sDefaultEdgeEffectFactory;
    boolean mEnableFastScroller;
    private boolean mEnableGoToTop = false;
    private int mExtraPaddingInBottomHoverArea = 0;
    private int mExtraPaddingInTopHoverArea = 0;
    private SeslRecyclerViewFastScroller mFastScroller;
    private boolean mFastScrollerEnabled = false;
    private SeslFastScrollerEventListener mFastScrollerEventListener;
    boolean mFirstLayoutComplete;
    GapWorker mGapWorker;
    private int mGoToTopBottomPadding;
    private int mGoToTopElevation;
    private ValueAnimator mGoToTopFadeInAnimator;
    private ValueAnimator mGoToTopFadeOutAnimator;
    private Drawable mGoToTopImage;
    private Drawable mGoToTopImageLight;
    private int mGoToTopImmersiveBottomPadding;
    private int mGoToTopLastState = 0;
    private boolean mGoToTopMoved = false;
    private Rect mGoToTopRect = new Rect();
    private int mGoToTopSize;
    private int mGoToTopState = 0;
    private ImageView mGoToTopView;
    private boolean mGoToToping = false;
    boolean mHasFixedSize;
    private boolean mHasNestedScrollRange = false;
    private boolean mHoverAreaEnter = false;
    private int mHoverBottomAreaHeight = 0;
    private long mHoverRecognitionCurrentTime = 0;
    private long mHoverRecognitionDurationTime = 0;
    private long mHoverRecognitionStartTime = 0;
    private int mHoverScrollDirection = -1;
    private boolean mHoverScrollEnable = true;
    private int mHoverScrollSpeed = 0;
    private long mHoverScrollStartTime = 0;
    private boolean mHoverScrollStateChanged = false;
    private int mHoverScrollStateForListener = 0;
    private long mHoverScrollTimeInterval = 300;
    private int mHoverTopAreaHeight = 0;
    private boolean mIgnoreMotionEventTillDown;
    private IndexTip mIndexTip;
    private boolean mIndexTipEnabled = false;
    private int mInitialTopOffsetOfScreen = 0;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mInterceptRequestLayoutDepth = 0;
    private OnItemTouchListener mInterceptingOnItemTouchListener;
    private boolean mIsArrowKeyPressed;
    boolean mIsAttached;
    private boolean mIsCloseChildSetted = false;
    private boolean mIsCtrlKeyPressed = false;
    private boolean mIsCtrlMultiSelection = false;
    private boolean mIsEnabledPaddingInHoverScroll = false;
    private boolean mIsFirstMultiSelectionMove = true;
    private boolean mIsFirstPenMoveEvent = true;
    private boolean mIsHoverOverscrolled = false;
    private boolean mIsLongPressMultiSelection = false;
    private boolean mIsNeedCheckLatency = false;
    private boolean mIsNeedPenSelectIconSet = false;
    private boolean mIsNeedPenSelection = false;
    private boolean mIsPenDragBlockEnabled = true;
    private boolean mIsPenHovered = false;
    private boolean mIsPenPressed = false;
    private boolean mIsPenSelectPointerSetted = false;
    private boolean mIsPenSelectionEnabled = true;
    private boolean mIsSendHoverScrollState = false;
    private boolean mIsSetOnlyAddAnim = false;
    private boolean mIsSetOnlyRemoveAnim = false;
    private boolean mIsSkipMoveEvent = false;
    ItemAnimator mItemAnimator = new DefaultItemAnimator();
    private ItemAnimator.ItemAnimatorListener mItemAnimatorListener = new ItemAnimatorRestoreListener();
    final ArrayList<ItemDecoration> mItemDecorations = new ArrayList<>();
    boolean mItemsAddedOrRemoved = false;
    boolean mItemsChanged = false;
    private int mLastAutoMeasureNonExactMeasuredHeight = 0;
    private int mLastAutoMeasureNonExactMeasuredWidth = 0;
    private boolean mLastAutoMeasureSkippedDueToExact;
    private int mLastBlackTop = -1;
    private ValueAnimator mLastItemAddRemoveAnim = null;
    private int mLastItemAnimTop = -1;
    private int mLastTouchX;
    private int mLastTouchY;
    public LayoutManager mLayout;
    private int mLayoutOrScrollCounter = 0;
    boolean mLayoutSuppressed;
    boolean mLayoutWasDefered;
    private EdgeEffect mLeftGlow;
    public Rect mListPadding = new Rect();
    private SeslLongPressMultiSelectionListener mLongPressMultiSelectionListener;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private final int[] mMinMaxLayoutPositions = new int[2];
    private final int mMotionEventUpPendingFlag = 33554432;
    private boolean mNeedsHoverScroll = false;
    private final int[] mNestedOffsets = new int[2];
    private int mNestedScrollRange = 0;
    private boolean mNewTextViewHoverState = false;
    private final RecyclerViewDataObserver mObserver = new RecyclerViewDataObserver();
    private int mOldHoverScrollDirection = -1;
    private boolean mOldTextViewHoverState = false;
    private List<OnChildAttachStateChangeListener> mOnChildAttachStateListeners;
    private OnFlingListener mOnFlingListener;
    private SeslOnGoToTopClickListener mOnGoToTopClickListener = null;
    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners = new ArrayList<>();
    private SeslOnMultiSelectedListener mOnMultiSelectedListener;
    private int mPagingTouchSlop = 0;
    private int mPenDistanceFromTrackedChildTop = 0;
    private int mPenDragBlockBottom = 0;
    private Drawable mPenDragBlockImage;
    private int mPenDragBlockLeft = 0;
    private Rect mPenDragBlockRect = new Rect();
    private int mPenDragBlockRight = 0;
    private int mPenDragBlockTop = 0;
    private int mPenDragEndX = 0;
    private int mPenDragEndY = 0;
    private long mPenDragScrollTimeInterval = 500;
    private ArrayList<Integer> mPenDragSelectedItemArray;
    private int mPenDragSelectedViewPosition = -1;
    private int mPenDragStartX = 0;
    private int mPenDragStartY = 0;
    private View mPenTrackedChild = null;
    private int mPenTrackedChildPosition = -1;
    final List<ViewHolder> mPendingAccessibilityImportanceChange = new ArrayList();
    SavedState mPendingSavedState;
    boolean mPostedAnimatorRunner = false;
    public GapWorker.LayoutPrefetchRegistryImpl mPrefetchRegistry = ALLOW_THREAD_GAP_WORK ? new GapWorker.LayoutPrefetchRegistryImpl() : null;
    private boolean mPreserveFocusAfterLayout = true;
    private long mPrevLatencyTime;
    private boolean mPreventFirstGlow = false;
    private int mRectColor;
    private Paint mRectPaint = new Paint();
    public final Recycler mRecycler = new Recycler();
    RecyclerListener mRecyclerListener;
    final List<RecyclerListener> mRecyclerListeners = new ArrayList();
    private final int[] mRecyclerViewOffsets = new int[2];
    private int mRemainNestedScrollRange = 0;
    final int[] mReusableIntPair = new int[2];
    private EdgeEffect mRightGlow;
    private View mRootViewCheckForDialog = null;
    private SeslSubheaderRoundedCorner mRoundedCorner;
    private float mScaledHorizontalScrollFactor = Float.MIN_VALUE;
    private float mScaledVerticalScrollFactor = Float.MIN_VALUE;
    private OnScrollListener mScrollListener;
    private List<OnScrollListener> mScrollListeners;
    private final int[] mScrollOffset = new int[2];
    private int mScrollPointerId = INVALID_POINTER;
    private int mScrollState = SCROLL_STATE_IDLE;
    private NestedScrollingChildHelper mScrollingChildHelper;
    Drawable mSelector;
    Rect mSelectorRect = new Rect();
    private int mShowFadeOutGTP = 0;
    private boolean mSizeChnage = false;
    public final State mState = new State();
    private int mStatisticalCount = 0;
    final Rect mTempRect = new Rect();
    private final Rect mTempRect2 = new Rect();
    final RectF mTempRectF = new RectF();
    private EdgeEffect mTopGlow;
    private int mTouchSlop;
    private int mTouchSlop2 = 0;
    private boolean mUsePagingTouchSlopForStylus = false;
    private VelocityTracker mVelocityTracker;
    final ViewFlinger mViewFlinger = new ViewFlinger();
    final ViewInfoStore mViewInfoStore = new ViewInfoStore();
    private final int[] mWindowOffsets = new int[2];

    static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private Animator.AnimatorListener mAnimListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }

        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mLastItemAddRemoveAnim = null;
            mIsSetOnlyAddAnim = false;
            mIsSetOnlyRemoveAnim = false;
            if (getItemAnimator() instanceof DefaultItemAnimator) {
                ((DefaultItemAnimator) getItemAnimator()).clearPendingAnimFlag();
            }
            RecyclerView.this.invalidate();
        }
    };

    private final Runnable mAutoHide = new Runnable() {
        @Override
        public void run() {
            setupGoToTop(GTP_STATE_NONE);
        }
    };

    private final Runnable mGoToTopEdgeEffectRunnable = new Runnable() {
        @Override
        public void run() {
            ensureTopGlow();
            mTopGlow.onAbsorb(ON_ABSORB_VELOCITY);
            invalidate();
        }
    };

    private final Runnable mGoToToFadeInRunnable = new Runnable() {
        @Override
        public void run() {
            playGotoToFadeIn();
        }
    };
    private final Runnable mGoToToFadeOutRunnable = new Runnable() {
        @Override
        public void run() {
            playGotoToFadeOut();
        }
    };

    // kang
    private Handler mHoverHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int i2;

            if (msg.what == MSG_HOVERSCROLL_MOVE) {
                if (mAdapter == null) {
                    Log.e(TAG, "No adapter attached; skipping MSG_HOVERSCROLL_MOVE");
                    return;
                }

                mHoverRecognitionCurrentTime = System.currentTimeMillis();
                mHoverRecognitionDurationTime = (mHoverRecognitionCurrentTime - mHoverRecognitionStartTime) / 1000;
                if (mIsPenHovered && mHoverRecognitionCurrentTime - mHoverScrollStartTime < mHoverScrollTimeInterval) {
                    return;
                }

                if (!mIsPenPressed || mHoverRecognitionCurrentTime - mHoverScrollStartTime >= mPenDragScrollTimeInterval) {
                    if (mIsPenHovered && !mIsSendHoverScrollState) {
                        if (mScrollListener != null) {
                            mHoverScrollStateForListener = HOVERSCROLL_UP;
                            mScrollListener.onScrollStateChanged(RecyclerView.this, HOVERSCROLL_UP);
                        }
                        mIsSendHoverScrollState = true;
                    }

                    boolean canScrollVertically = mLayout.canScrollVertically();
                    boolean canScrollHorizontally = mLayout.canScrollHorizontally();
                    boolean z2 = mLayout.getLayoutDirection() == 1;
                    int childCount = getChildCount();
                    boolean z3 = findFirstChildPosition() + childCount < mAdapter.getItemCount();
                    if (!z3 && childCount > 0) {
                        getDecoratedBoundsWithMargins(getChildAt(childCount - 1), mChildBound);
                        z3 = !canScrollHorizontally ? mChildBound.bottom > getBottom() - mListPadding.bottom || mChildBound.bottom > getHeight() - mListPadding.bottom : !z2 ? mChildBound.right > getRight() - mListPadding.right || mChildBound.right > getWidth() - mListPadding.right : mChildBound.left < mListPadding.left;
                    }
                    boolean z4 = findFirstChildPosition() > 0;
                    if (!z4 && childCount > 0) {
                        getDecoratedBoundsWithMargins(getChildAt(0), mChildBound);
                        z4 = !canScrollHorizontally ? mChildBound.top < mListPadding.top : !(!z2 ? mChildBound.left >= mListPadding.left : mChildBound.right <= getRight() - mListPadding.right && mChildBound.right <= getWidth() - mListPadding.right);
                    }

                    mHoverScrollSpeed = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HOVERSCROLL_SPEED, mContext.getResources().getDisplayMetrics()) + 0.5f);
                    if (mHoverRecognitionDurationTime > 2 && RecyclerView.this.mHoverRecognitionDurationTime < 4) {
                        mHoverScrollSpeed += (int) (((double) mHoverScrollSpeed) * 0.1d);
                    } else if (mHoverRecognitionDurationTime >= 4 && mHoverRecognitionDurationTime < 5) {
                        mHoverScrollSpeed += (int) (((double) mHoverScrollSpeed) * 0.2d);
                    } else if (mHoverRecognitionDurationTime >= 5) {
                        mHoverScrollSpeed += (int) (((double) mHoverScrollSpeed) * 0.3d);
                    }
                    int i3 = 2;
                    if (mHoverScrollDirection == HOVERSCROLL_DOWN) {
                        if (!canScrollHorizontally || !z2) {
                            i2 = mHoverScrollSpeed * -1;
                        } else {
                            i2 = mHoverScrollSpeed * 1;
                        }
                        if ((mPenTrackedChild == null && mCloseChildByBottom != null) || (mOldHoverScrollDirection != mHoverScrollDirection && mIsCloseChildSetted)) {
                            mPenTrackedChild = mCloseChildByBottom;
                            mPenDistanceFromTrackedChildTop = mDistanceFromCloseChildBottom;
                            mPenTrackedChildPosition = mCloseChildPositionByBottom;
                            mOldHoverScrollDirection = mHoverScrollDirection;
                            mIsCloseChildSetted = true;
                        }
                    } else {
                        if (!canScrollHorizontally || !z2) {
                            i2 = mHoverScrollSpeed * 1;
                        } else {
                            i2 = mHoverScrollSpeed * -1;
                        }
                        if ((mPenTrackedChild == null && mCloseChildByTop != null) || (mOldHoverScrollDirection != mHoverScrollDirection && mIsCloseChildSetted)) {
                            mPenTrackedChild = mCloseChildByTop;
                            mPenDistanceFromTrackedChildTop = mDistanceFromCloseChildTop;
                            mPenTrackedChildPosition = mCloseChildPositionByTop;
                            mOldHoverScrollDirection = mHoverScrollDirection;
                            mIsCloseChildSetted = true;
                        }
                    }

                    if (getChildAt(getChildCount() - 1) != null) {
                        if ((i2 >= 0 || !z4) && (i2 <= 0 || !z3)) {
                            int overScrollMode = getOverScrollMode();
                            boolean z5 = overScrollMode == 0 || (overScrollMode == 1 && !contentFits());
                            if (z5 && !mIsHoverOverscrolled) {
                                if (canScrollHorizontally) {
                                    ensureLeftGlow();
                                    ensureRightGlow();
                                } else {
                                    ensureTopGlow();
                                    ensureBottomGlow();
                                }
                                if (mHoverScrollDirection == HOVERSCROLL_DOWN) {
                                    if (canScrollHorizontally) {
                                        mLeftGlow.onAbsorb(ON_ABSORB_VELOCITY);
                                        if (!mRightGlow.isFinished()) {
                                            mRightGlow.onRelease();
                                        }
                                    } else {
                                        mTopGlow.onAbsorb(ON_ABSORB_VELOCITY);
                                        if (!mBottomGlow.isFinished()) {
                                            mBottomGlow.onRelease();
                                        }
                                    }
                                } else if (mHoverScrollDirection == HOVERSCROLL_UP) {
                                    if (canScrollHorizontally) {
                                        mRightGlow.onAbsorb(ON_ABSORB_VELOCITY);
                                        if (!mLeftGlow.isFinished()) {
                                            mLeftGlow.onRelease();
                                        }
                                    } else {
                                        mBottomGlow.onAbsorb(ON_ABSORB_VELOCITY);
                                        setupGoToTop(GTP_STATE_SHOWN);
                                        autoHide(GTP_STATE_SHOWN);
                                        if (!mTopGlow.isFinished()) {
                                            mTopGlow.onRelease();
                                        }
                                    }
                                }
                                invalidate();
                                mIsHoverOverscrolled = true;
                            }
                            if (mScrollState == SCROLL_STATE_DRAGGING) {
                                setScrollState(SCROLL_STATE_IDLE);
                            }
                            if (!z5 && !mIsHoverOverscrolled) {
                                mIsHoverOverscrolled = true;
                                return;
                            }
                            return;
                        }
                        if (canScrollHorizontally) {
                            i3 = 1;
                        }
                        startNestedScroll(i3, TYPE_NON_TOUCH);
                        if (!dispatchNestedPreScroll(canScrollHorizontally ? z2 ? -i2 : i2 : 0, canScrollVertically ? i2 : 0, null, null, TYPE_NON_TOUCH)) {
                            int i4 = canScrollHorizontally ? z2 ? -i2 : i2 : 0;
                            if (!canScrollVertically) {
                                i2 = 0;
                            }
                            scrollByInternal(i4, i2, null, 0);
                            setScrollState(SCROLL_STATE_DRAGGING);
                            if (mIsLongPressMultiSelection) {
                                updateLongPressMultiSelection(mPenDragEndX, mPenDragEndY, false);
                            }
                        } else {
                            adjustNestedScrollRangeBy(i2);
                        }
                        mHoverHandler.sendEmptyMessageDelayed(0, 0);
                    }
                }
            }
        }
    };
    // kang

    private Runnable mItemAnimatorRunner = new Runnable() {
        @Override
        public void run() {
            if (mItemAnimator != null) {
                mItemAnimator.runPendingAnimations();
            }
            mPostedAnimatorRunner = false;
        }
    };

    final Runnable mUpdateChildViewsRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mFirstLayoutComplete || isLayoutRequested()) {
                return;
            }
            if (!mIsAttached) {
                requestLayout();
                return;
            }
            if (mLayoutSuppressed) {
                mLayoutWasDefered = true;
                return;
            }
            consumePendingUpdateOperations();
        }
    };

    private final ViewInfoStore.ProcessCallback mViewInfoProcessCallback = new ViewInfoStore.ProcessCallback() {
        @Override
        public void processDisappeared(ViewHolder viewHolder, @NonNull ItemHolderInfo info, @Nullable ItemHolderInfo postInfo) {
            mRecycler.unscrapView(viewHolder);
            animateDisappearance(viewHolder, info, postInfo);
        }

        @Override
        public void processAppeared(ViewHolder viewHolder, ItemHolderInfo preInfo, ItemHolderInfo info) {
            animateAppearance(viewHolder, preInfo, info);
        }

        @Override
        public void processPersistent(ViewHolder viewHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo) {
            viewHolder.setIsRecyclable(false);
            if (mDataSetHasChangedAfterLayout) {
                if (mItemAnimator.animateChange(viewHolder, viewHolder, preInfo, postInfo)) {
                    postAnimationRunner();
                }
            } else if (mItemAnimator.animatePersistence(viewHolder, preInfo, postInfo)) {
                postAnimationRunner();
            }
        }

        @Override
        public void unused(ViewHolder viewHolder) {
            mLayout.removeAndRecycleView(viewHolder.itemView, mRecycler);
        }
    };


    public RecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.recyclerViewStyle);
    }

    public RecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScrollContainer(true);
        setFocusableInTouchMode(true);

        mContext = context;
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        seslInitConfigurations(context);
        setWillNotDraw(getOverScrollMode() == View.OVER_SCROLL_NEVER);

        mItemAnimator.setListener(mItemAnimatorListener);
        initAdapterManager();
        initChildrenHelper();
        initAutofill();
        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(this));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView, defStyleAttr, 0);

        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.RecyclerView, attrs, a, defStyleAttr, 0);
        String layoutManagerName = a.getString(R.styleable.RecyclerView_layoutManager);
        int descendantFocusability = a.getInt(R.styleable.RecyclerView_android_descendantFocusability, -1);
        if (descendantFocusability == -1) {
            setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }
        mClipToPadding = a.getBoolean(R.styleable.RecyclerView_android_clipToPadding, true);
        mEnableFastScroller = a.getBoolean(R.styleable.RecyclerView_fastScrollEnabled, false);
        if (mEnableFastScroller) {
            StateListDrawable verticalThumbDrawable = (StateListDrawable) a.getDrawable(R.styleable.RecyclerView_fastScrollVerticalThumbDrawable);
            Drawable verticalTrackDrawable = a.getDrawable(R.styleable.RecyclerView_fastScrollVerticalTrackDrawable);
            StateListDrawable horizontalThumbDrawable = (StateListDrawable) a.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable);
            Drawable horizontalTrackDrawable = a.getDrawable(R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable);
            initFastScroller(verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable);
        }
        a.recycle();

        createLayoutManager(context, layoutManagerName, attrs, defStyleAttr, 0);

        boolean nestedScrollingEnabled = true;
        if (Build.VERSION.SDK_INT >= 21) {
            a = context.obtainStyledAttributes(attrs, NESTED_SCROLLING_ATTRS, defStyleAttr, 0);
            ViewCompat.saveAttributeDataForStyleable(this, context, NESTED_SCROLLING_ATTRS, attrs, a, defStyleAttr, 0);
            nestedScrollingEnabled = a.getBoolean(0, true);
            a.recycle();
        }

        TypedValue typedValue = new TypedValue();
        mPenDragBlockImage = context.getResources().getDrawable(R.drawable.sesl_pen_block_selection);
        if (context.getTheme().resolveAttribute(R.attr.goToTopStyle, typedValue, true)) {
            mGoToTopImageLight = context.getResources().getDrawable(typedValue.resourceId);
        }
        context.getTheme().resolveAttribute(R.attr.roundedCornerColor, typedValue, true);
        if (typedValue.resourceId > 0) {
            mRectColor = context.getResources().getColor(typedValue.resourceId);
        }
        mRectPaint.setColor(this.mRectColor);
        mRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mItemAnimator.setHostView(this);
        mRoundedCorner = new SeslSubheaderRoundedCorner(getContext());
        mRoundedCorner.setRoundedCorners(12);

        setNestedScrollingEnabled(nestedScrollingEnabled);
    }

    public void seslInitConfigurations(Context context) {
        ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mTouchSlop2 = vc.getScaledTouchSlop();
        mPagingTouchSlop = vc.getScaledPagingTouchSlop();
        mScaledHorizontalScrollFactor = ViewConfigurationCompat.getScaledHorizontalScrollFactor(vc, context);
        mScaledVerticalScrollFactor = ViewConfigurationCompat.getScaledVerticalScrollFactor(vc, context);
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mHoverTopAreaHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HOVERSCROLL_HEIGHT_TOP_DP, context.getResources().getDisplayMetrics()) + 0.5f);
        mHoverBottomAreaHeight = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HOVERSCROLL_HEIGHT_BOTTOM_DP, context.getResources().getDisplayMetrics()) + 0.5f);
        mGoToTopBottomPadding = context.getResources().getDimensionPixelSize(R.dimen.sesl_go_to_top_scrollable_view_gap);
        mGoToTopImmersiveBottomPadding = 0;
        mGoToTopSize = context.getResources().getDimensionPixelSize(R.dimen.sesl_go_to_top_scrollable_view_size);
        mGoToTopElevation = context.getResources().getDimensionPixelSize(R.dimen.sesl_go_to_top_elevation);
    }


    String exceptionLabel() {
        return " " + super.toString() + ", adapter:" + mAdapter + ", layout:" + mLayout + ", context:" + getContext();
    }

    @SuppressLint("InlinedApi")
    private void initAutofill() {
        if (ViewCompat.getImportantForAutofill(this) == View.IMPORTANT_FOR_AUTOFILL_AUTO) {
            ViewCompat.setImportantForAutofill(this, View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
    }

    @Nullable
    public RecyclerViewAccessibilityDelegate getCompatAccessibilityDelegate() {
        return mAccessibilityDelegate;
    }

    public void setAccessibilityDelegateCompat(@Nullable RecyclerViewAccessibilityDelegate accessibilityDelegate) {
        mAccessibilityDelegate = accessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityDelegate);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return "de.dlyt.yanndroid.oneui.view.RecyclerView";
    }

    private void createLayoutManager(Context context, String className, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (className != null) {
            className = className.trim();
            if (!className.isEmpty()) {
                className = getFullClassName(context, className);
                try {
                    ClassLoader classLoader;
                    if (isInEditMode()) {
                        classLoader = this.getClass().getClassLoader();
                    } else {
                        classLoader = context.getClassLoader();
                    }
                    Class<? extends LayoutManager> layoutManagerClass = Class.forName(className, false, classLoader).asSubclass(LayoutManager.class);
                    Constructor<? extends LayoutManager> constructor;
                    Object[] constructorArgs = null;
                    try {
                        constructor = layoutManagerClass.getConstructor(LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE);
                        constructorArgs = new Object[]{context, attrs, defStyleAttr, defStyleRes};
                    } catch (NoSuchMethodException e) {
                        try {
                            constructor = layoutManagerClass.getConstructor();
                        } catch (NoSuchMethodException e1) {
                            e1.initCause(e);
                            throw new IllegalStateException(attrs.getPositionDescription() + ": Error creating LayoutManager " + className, e1);
                        }
                    }
                    constructor.setAccessible(true);
                    setLayoutManager(constructor.newInstance(constructorArgs));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(attrs.getPositionDescription() + ": Unable to find LayoutManager " + className, e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException(attrs.getPositionDescription() + ": Could not instantiate the LayoutManager: " + className, e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(attrs.getPositionDescription() + ": Could not instantiate the LayoutManager: " + className, e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(attrs.getPositionDescription() + ": Cannot access non-public constructor " + className, e);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(attrs.getPositionDescription() + ": Class is not a LayoutManager " + className, e);
                }
            }
        }
    }

    private String getFullClassName(Context context, String className) {
        if (className.charAt(0) == '.') {
            return context.getPackageName() + className;
        }
        if (className.contains(".")) {
            return className;
        }
        return RecyclerView.class.getPackage().getName() + '.' + className;
    }

    private void initChildrenHelper() {
        mChildHelper = new ChildHelper(new ChildHelper.Callback() {
            @Override
            public int getChildCount() {
                return RecyclerView.this.getChildCount();
            }

            @Override
            public void addView(View child, int index) {
                if (VERBOSE_TRACING) {
                    TraceCompat.beginSection("RV addView");
                }
                RecyclerView.this.addView(child, index);
                if (VERBOSE_TRACING) {
                    TraceCompat.endSection();
                }
                dispatchChildAttached(child);
            }

            @Override
            public int indexOfChild(View view) {
                return RecyclerView.this.indexOfChild(view);
            }

            @Override
            public void removeViewAt(int index) {
                final View child = RecyclerView.this.getChildAt(index);
                if (child != null) {
                    dispatchChildDetached(child);

                    child.clearAnimation();
                }
                if (VERBOSE_TRACING) {
                    TraceCompat.beginSection("RV removeViewAt");
                }
                RecyclerView.this.removeViewAt(index);
                if (VERBOSE_TRACING) {
                    TraceCompat.endSection();
                }
            }

            @Override
            public View getChildAt(int offset) {
                return RecyclerView.this.getChildAt(offset);
            }

            @Override
            public void removeAllViews() {
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    dispatchChildDetached(child);

                    child.clearAnimation();
                }
                RecyclerView.this.removeAllViews();
            }

            @Override
            public ViewHolder getChildViewHolder(View view) {
                return getChildViewHolderInt(view);
            }

            @Override
            public void attachViewToParent(View child, int index, ViewGroup.LayoutParams layoutParams) {
                final ViewHolder vh = getChildViewHolderInt(child);
                if (vh != null) {
                    if (!vh.isTmpDetached() && !vh.shouldIgnore()) {
                        throw new IllegalArgumentException("Called attach on a child which is not detached: " + vh + exceptionLabel());
                    }
                    if (DEBUG) {
                        Log.d(TAG, "reAttach " + vh);
                    }
                    vh.clearTmpDetachFlag();
                }
                RecyclerView.this.attachViewToParent(child, index, layoutParams);
            }

            @Override
            public void detachViewFromParent(int offset) {
                final View view = getChildAt(offset);
                if (view != null) {
                    final ViewHolder vh = getChildViewHolderInt(view);
                    if (vh != null) {
                        if (vh.isTmpDetached() && !vh.shouldIgnore()) {
                            throw new IllegalArgumentException("called detach on an already detached child " + vh + exceptionLabel());
                        }
                        if (DEBUG) {
                            Log.d(TAG, "tmpDetach " + vh);
                        }
                        vh.addFlags(ViewHolder.FLAG_TMP_DETACHED);
                    }
                }
                RecyclerView.this.detachViewFromParent(offset);
            }

            @Override
            public void onEnteredHiddenState(View child) {
                final ViewHolder vh = getChildViewHolderInt(child);
                if (vh != null) {
                    vh.onEnteredHiddenState(RecyclerView.this);
                }
            }

            @Override
            public void onLeftHiddenState(View child) {
                final ViewHolder vh = getChildViewHolderInt(child);
                if (vh != null) {
                    vh.onLeftHiddenState(RecyclerView.this);
                }
            }
        });
    }

    void initAdapterManager() {
        mAdapterHelper = new AdapterHelper(new AdapterHelper.Callback() {
            @Override
            public ViewHolder findViewHolder(int position) {
                final ViewHolder vh = findViewHolderForPosition(position, true);
                if (vh == null) {
                    return null;
                }
                if (mChildHelper.isHidden(vh.itemView)) {
                    if (DEBUG) {
                        Log.d(TAG, "assuming view holder cannot be find because it is hidden");
                    }
                    return null;
                }
                return vh;
            }

            @Override
            public void offsetPositionsForRemovingInvisible(int start, int count) {
                offsetPositionRecordsForRemove(start, count, true);
                mItemsAddedOrRemoved = true;
                mState.mDeletedInvisibleItemCountSincePreviousLayout += count;
            }

            @Override
            public void offsetPositionsForRemovingLaidOutOrNewView(int positionStart, int itemCount) {
                offsetPositionRecordsForRemove(positionStart, itemCount, false);
                mItemsAddedOrRemoved = true;
            }


            @Override
            public void markViewHoldersUpdated(int positionStart, int itemCount, Object payload) {
                viewRangeUpdate(positionStart, itemCount, payload);
                mItemsChanged = true;
            }

            @Override
            public void onDispatchFirstPass(AdapterHelper.UpdateOp op) {
                dispatchUpdate(op);
            }

            void dispatchUpdate(AdapterHelper.UpdateOp op) {
                switch (op.cmd) {
                    case AdapterHelper.UpdateOp.ADD:
                        mLayout.onItemsAdded(RecyclerView.this, op.positionStart, op.itemCount);
                        break;
                    case AdapterHelper.UpdateOp.REMOVE:
                        mLayout.onItemsRemoved(RecyclerView.this, op.positionStart, op.itemCount);
                        break;
                    case AdapterHelper.UpdateOp.UPDATE:
                        mLayout.onItemsUpdated(RecyclerView.this, op.positionStart, op.itemCount, op.payload);
                        break;
                    case AdapterHelper.UpdateOp.MOVE:
                        mLayout.onItemsMoved(RecyclerView.this, op.positionStart, op.itemCount, 1);
                        break;
                }
            }

            @Override
            public void onDispatchSecondPass(AdapterHelper.UpdateOp op) {
                dispatchUpdate(op);
            }

            @Override
            public void offsetPositionsForAdd(int positionStart, int itemCount) {
                offsetPositionRecordsForInsert(positionStart, itemCount);
                mItemsAddedOrRemoved = true;
            }

            @Override
            public void offsetPositionsForMove(int from, int to) {
                offsetPositionRecordsForMove(from, to);
                mItemsAddedOrRemoved = true;
            }
        });
    }

    public void setHasFixedSize(boolean hasFixedSize) {
        mHasFixedSize = hasFixedSize;
    }

    public boolean hasFixedSize() {
        return mHasFixedSize;
    }

    @Override
    public void setClipToPadding(boolean clipToPadding) {
        if (clipToPadding != mClipToPadding) {
            invalidateGlows();
        }
        mClipToPadding = clipToPadding;
        super.setClipToPadding(clipToPadding);
        if (mFirstLayoutComplete) {
            requestLayout();
        }
    }

    @Override
    public boolean getClipToPadding() {
        return mClipToPadding;
    }

    public void setScrollingTouchSlop(int slopConstant) {
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        Log.d(TAG, "setScrollingTouchSlop(): slopConstant[" + slopConstant + "]");
        seslSetPagingTouchSlopForStylus(false);
        switch (slopConstant) {
            default:
                Log.w(TAG, "setScrollingTouchSlop(): bad argument constant " + slopConstant + "; using default value");
            case TOUCH_SLOP_DEFAULT:
                mTouchSlop = vc.getScaledTouchSlop();
                break;

            case TOUCH_SLOP_PAGING:
                mTouchSlop = vc.getScaledPagingTouchSlop();
                break;
        }
    }

    public void swapAdapter(@Nullable Adapter adapter, boolean removeAndRecycleExistingViews) {
        setLayoutFrozen(false);
        setAdapterInternal(adapter, true, removeAndRecycleExistingViews);
        processDataSetCompletelyChanged(true);
        requestLayout();
    }

    public void setAdapter(@Nullable Adapter adapter) {
        setLayoutFrozen(false);
        setAdapterInternal(adapter, false, true);
        processDataSetCompletelyChanged(false);
        requestLayout();
    }

    public void removeAndRecycleViews() {
        if (mItemAnimator != null) {
            mItemAnimator.endAnimations();
        }
        if (mLayout != null) {
            mLayout.removeAndRecycleAllViews(mRecycler);
            mLayout.removeAndRecycleScrapInt(mRecycler);
        }
        mRecycler.clear();
    }

    private void setAdapterInternal(@Nullable Adapter adapter, boolean compatibleWithPrevious, boolean removeAndRecycleViews) {
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mObserver);
            mAdapter.onDetachedFromRecyclerView(this);
        }
        if (!compatibleWithPrevious || removeAndRecycleViews) {
            removeAndRecycleViews();
        }
        mAdapterHelper.reset();
        final Adapter oldAdapter = mAdapter;
        mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
            adapter.onAttachedToRecyclerView(this);
        }
        if (mLayout != null) {
            mLayout.onAdapterChanged(oldAdapter, mAdapter);
        }
        mRecycler.onAdapterChanged(oldAdapter, mAdapter, compatibleWithPrevious);
        mState.mStructureChanged = true;
    }

    @Nullable
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Deprecated
    public void setRecyclerListener(@Nullable RecyclerListener listener) {
        mRecyclerListener = listener;
    }

    @SuppressLint("RestrictedApi")
    public void addRecyclerListener(@NonNull RecyclerListener listener) {
        checkArgument(listener != null, "'listener' arg cannot " + "be null.");
        mRecyclerListeners.add(listener);
    }

    public void removeRecyclerListener(@NonNull RecyclerListener listener) {
        mRecyclerListeners.remove(listener);
    }

    @Override
    public int getBaseline() {
        if (mLayout != null) {
            return mLayout.getBaseline();
        } else {
            return super.getBaseline();
        }
    }

    public void addOnChildAttachStateChangeListener(@NonNull OnChildAttachStateChangeListener listener) {
        if (mOnChildAttachStateListeners == null) {
            mOnChildAttachStateListeners = new ArrayList<>();
        }
        mOnChildAttachStateListeners.add(listener);
    }

    public void removeOnChildAttachStateChangeListener(@NonNull OnChildAttachStateChangeListener listener) {
        if (mOnChildAttachStateListeners == null) {
            return;
        }
        mOnChildAttachStateListeners.remove(listener);
    }

    public void clearOnChildAttachStateChangeListeners() {
        if (mOnChildAttachStateListeners != null) {
            mOnChildAttachStateListeners.clear();
        }
    }

    public void setLayoutManager(@Nullable LayoutManager layout) {
        if (layout == mLayout) {
            return;
        }
        mDrawRect = mDrawRect && layout instanceof LinearLayoutManager;
        mDrawLastRoundedCorner = mDrawLastRoundedCorner && mDrawRect;
        stopScroll();
        if (mLayout != null) {
            if (mItemAnimator != null) {
                mItemAnimator.endAnimations();
            }
            mLayout.removeAndRecycleAllViews(mRecycler);
            mLayout.removeAndRecycleScrapInt(mRecycler);
            mRecycler.clear();

            if (mIsAttached) {
                mLayout.dispatchDetachedFromWindow(this, mRecycler);
            }
            mLayout.setRecyclerView(null);
            mLayout = null;
        } else {
            mRecycler.clear();
        }
        mChildHelper.removeAllViewsUnfiltered();
        mLayout = layout;
        if (layout != null) {
            if (layout.mRecyclerView != null) {
                throw new IllegalArgumentException("LayoutManager " + layout + " is already attached to a RecyclerView:" + layout.mRecyclerView.exceptionLabel());
            }
            mLayout.setRecyclerView(this);
            if (mIsAttached) {
                mLayout.dispatchAttachedToWindow(this);
            }
        }
        mRecycler.updateViewCacheSize();
        requestLayout();
    }

    public void setOnFlingListener(@Nullable OnFlingListener onFlingListener) {
        mOnFlingListener = onFlingListener;
    }

    @Nullable
    public OnFlingListener getOnFlingListener() {
        return mOnFlingListener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        mApproxLatency = 0.0f;
        mStatisticalCount = 0;
        mIsNeedCheckLatency = false;

        SavedState state = new SavedState(super.onSaveInstanceState());
        if (mPendingSavedState != null) {
            state.copyFrom(mPendingSavedState);
        } else if (mLayout != null) {
            state.mLayoutState = mLayout.onSaveInstanceState();
        } else {
            state.mLayoutState = null;
        }

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        mPendingSavedState = (SavedState) state;
        super.onRestoreInstanceState(mPendingSavedState.getSuperState());
        requestLayout();
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    private void addAnimatingView(ViewHolder viewHolder) {
        final View view = viewHolder.itemView;
        final boolean alreadyParented = view.getParent() == this;
        mRecycler.unscrapView(getChildViewHolder(view));
        if (viewHolder.isTmpDetached()) {
            mChildHelper.attachViewToParent(view, -1, view.getLayoutParams(), true);
        } else if (!alreadyParented) {
            mChildHelper.addView(view, true);
        } else {
            mChildHelper.hide(view);
        }
    }

    boolean removeAnimatingView(View view) {
        startInterceptRequestLayout();
        final boolean removed = mChildHelper.removeViewIfHidden(view);
        if (removed) {
            final ViewHolder viewHolder = getChildViewHolderInt(view);
            mRecycler.unscrapView(viewHolder);
            mRecycler.recycleViewHolderInternal(viewHolder);
            if (DEBUG) {
                Log.d(TAG, "after removing animated view: " + view + ", " + this);
            }
        }
        stopInterceptRequestLayout(!removed);
        return removed;
    }

    @Nullable
    public LayoutManager getLayoutManager() {
        return mLayout;
    }

    @NonNull
    public RecycledViewPool getRecycledViewPool() {
        return mRecycler.getRecycledViewPool();
    }

    public void setRecycledViewPool(@Nullable RecycledViewPool pool) {
        mRecycler.setRecycledViewPool(pool);
    }

    public void setViewCacheExtension(@Nullable ViewCacheExtension extension) {
        mRecycler.setViewCacheExtension(extension);
    }

    public void setItemViewCacheSize(int size) {
        mRecycler.setViewCacheSize(size);
    }

    public int getScrollState() {
        return mScrollState;
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "setting scroll state to " + state + " from " + mScrollState, new Exception());
        }
        Log.d(TAG, "setting scroll state to " + state + " from " + mScrollState);
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
            stopScrollersInternal();
        }
        dispatchOnScrollStateChanged(state);

        if (state == SCROLL_STATE_DRAGGING) {
            mEdgeEffectByDragging = false;
            mIsNeedCheckLatency = true;
        }
        if (state == SCROLL_STATE_IDLE) {
            if (mIndexTipEnabled && mIndexTip != null) {
                mIndexTip.hide();
            }
            if (mEnableGoToTop && mGoToToping) {
                ensureTopGlow();
                mTopGlow.onAbsorb(ON_ABSORB_VELOCITY);
                invalidate();
            }
        }
    }

    public void addItemDecoration(@NonNull ItemDecoration decor, int index) {
        if (mLayout != null) {
            mLayout.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or layout");
        }
        if (mItemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (index < 0) {
            mItemDecorations.add(decor);
        } else {
            mItemDecorations.add(index, decor);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void addItemDecoration(@NonNull ItemDecoration decor) {
        addItemDecoration(decor, -1);
    }

    @NonNull
    public ItemDecoration getItemDecorationAt(int index) {
        final int size = getItemDecorationCount();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index + " is an invalid index for size " + size);
        }

        return mItemDecorations.get(index);
    }

    public int getItemDecorationCount() {
        return mItemDecorations.size();
    }

    public void removeItemDecorationAt(int index) {
        final int size = getItemDecorationCount();
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(index + " is an invalid index for size " + size);
        }

        removeItemDecoration(getItemDecorationAt(index));
    }

    public void removeItemDecoration(@NonNull ItemDecoration decor) {
        if (mLayout != null) {
            mLayout.assertNotInLayoutOrScroll("Cannot remove item decoration during a scroll  or layout");
        }
        mItemDecorations.remove(decor);
        if (mItemDecorations.isEmpty()) {
            setWillNotDraw(getOverScrollMode() == View.OVER_SCROLL_NEVER);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void setChildDrawingOrderCallback(@Nullable ChildDrawingOrderCallback childDrawingOrderCallback) {
        if (childDrawingOrderCallback == mChildDrawingOrderCallback) {
            return;
        }
        mChildDrawingOrderCallback = childDrawingOrderCallback;
        setChildrenDrawingOrderEnabled(mChildDrawingOrderCallback != null);
    }

    @Deprecated
    public void setOnScrollListener(@Nullable OnScrollListener listener) {
        mScrollListener = listener;
    }

    public void addOnScrollListener(@NonNull OnScrollListener listener) {
        if (mScrollListeners == null) {
            mScrollListeners = new ArrayList<>();
        }
        mScrollListeners.add(listener);
    }

    public void removeOnScrollListener(@NonNull OnScrollListener listener) {
        if (mScrollListeners != null) {
            mScrollListeners.remove(listener);
        }
    }

    public void clearOnScrollListeners() {
        if (mScrollListeners != null) {
            mScrollListeners.clear();
        }
    }

    public void scrollToPosition(int position) {
        if (mLayoutSuppressed) {
            return;
        }
        stopScroll();
        if (mLayout == null) {
            Log.e(TAG, "Cannot scroll to position a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        mLayout.scrollToPosition(position);
        awakenScrollBars();
        if (mFastScroller != null && mAdapter != null) {
            mFastScroller.onScroll(findFirstVisibleItemPosition(), getChildCount(), mAdapter.getItemCount());
        }
    }

    void jumpToPositionForSmoothScroller(int position) {
        if (mLayout == null) {
            return;
        }

        setScrollState(SCROLL_STATE_SETTLING);
        mLayout.scrollToPosition(position);
        awakenScrollBars();
    }

    public void smoothScrollToPosition(int position) {
        if (mLayoutSuppressed) {
            return;
        }
        if (mLayout == null) {
            Log.e(TAG, "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        mLayout.smoothScrollToPosition(this, mState, position);
    }

    @Override
    public void scrollTo(int x, int y) {
        Log.w(TAG, "RecyclerView does not support scrolling to an absolute position. Use scrollToPosition instead");
    }

    @Override
    public void scrollBy(int x, int y) {
        if (mLayout == null) {
            Log.e(TAG, "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        if (mLayoutSuppressed) {
            return;
        }
        final boolean canScrollHorizontal = mLayout.canScrollHorizontally();
        final boolean canScrollVertical = mLayout.canScrollVertically();
        if (canScrollHorizontal || canScrollVertical) {
            scrollByInternal(canScrollHorizontal ? x : 0, canScrollVertical ? y : 0, null, TYPE_TOUCH);
        }
    }

    public void nestedScrollBy(int x, int y) {
        nestedScrollByInternal(x, y, null, TYPE_NON_TOUCH);
    }

    @SuppressWarnings("SameParameterValue")
    private void nestedScrollByInternal(int x, int y, @Nullable MotionEvent motionEvent, int type) {
        if (mLayout == null) {
            Log.e(TAG, "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        if (mLayoutSuppressed) {
            return;
        }
        mReusableIntPair[0] = 0;
        mReusableIntPair[1] = 0;
        final boolean canScrollHorizontal = mLayout.canScrollHorizontally();
        final boolean canScrollVertical = mLayout.canScrollVertically();

        int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
        if (canScrollHorizontal) {
            nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
        }
        if (canScrollVertical) {
            nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
        }

        float verticalDisplacement = motionEvent == null ? getHeight() / 2f : motionEvent.getY();
        float horizontalDisplacement = motionEvent == null ? getWidth() / 2f : motionEvent.getX();
        x -= releaseHorizontalGlow(x, verticalDisplacement);
        y -= releaseVerticalGlow(y, horizontalDisplacement);
        startNestedScroll(nestedScrollAxis, type);
        if (dispatchNestedPreScroll(canScrollHorizontal ? x : 0, canScrollVertical ? y : 0, mReusableIntPair, mScrollOffset, type)) {
            x -= mReusableIntPair[0];
            y -= mReusableIntPair[1];
        }

        scrollByInternal(canScrollHorizontal ? x : 0, canScrollVertical ? y : 0, motionEvent, type);
        if (mGapWorker != null && (x != 0 || y != 0)) {
            mGapWorker.postFromTraversal(this, x, y);
        }
        stopNestedScroll(type);
    }

    void scrollStep(int dx, int dy, @Nullable int[] consumed) {
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();

        TraceCompat.beginSection(TRACE_SCROLL_TAG);
        fillRemainingScrollValues(mState);

        int consumedX = 0;
        int consumedY = 0;
        if (dx != 0) {
            consumedX = mLayout.scrollHorizontallyBy(dx, mRecycler, mState);
        }
        if (dy != 0) {
            consumedY = mLayout.scrollVerticallyBy(dy, mRecycler, mState);
            if (mGoToTopState == GTP_STATE_NONE) {
                setupGoToTop(GTP_STATE_SHOWN);
                autoHide(GTP_STATE_SHOWN);
            }
        }

        TraceCompat.endSection();
        repositionShadowingViews();

        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);

        if (consumed != null) {
            consumed[0] = consumedX;
            consumed[1] = consumedY;
        }
    }

    void consumePendingUpdateOperations() {
        if (!mFirstLayoutComplete || mDataSetHasChangedAfterLayout) {
            TraceCompat.beginSection(TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG);
            dispatchLayout();
            TraceCompat.endSection();
            return;
        }
        if (!mAdapterHelper.hasPendingUpdates()) {
            return;
        }

        if (mAdapterHelper.hasAnyUpdateTypes(AdapterHelper.UpdateOp.UPDATE) && !mAdapterHelper.hasAnyUpdateTypes(AdapterHelper.UpdateOp.ADD | AdapterHelper.UpdateOp.REMOVE | AdapterHelper.UpdateOp.MOVE)) {
            TraceCompat.beginSection(TRACE_HANDLE_ADAPTER_UPDATES_TAG);
            startInterceptRequestLayout();
            onEnterLayoutOrScroll();
            mAdapterHelper.preProcess();
            if (!mLayoutWasDefered) {
                if (hasUpdatedView()) {
                    dispatchLayout();
                } else {
                    mAdapterHelper.consumePostponedUpdates();
                }
            }
            stopInterceptRequestLayout(true);
            onExitLayoutOrScroll();
            TraceCompat.endSection();
        } else if (mAdapterHelper.hasPendingUpdates()) {
            TraceCompat.beginSection(TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG);
            dispatchLayout();
            TraceCompat.endSection();
        }
    }

    private boolean hasUpdatedView() {
        final int childCount = mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if (holder == null || holder.shouldIgnore()) {
                continue;
            }
            if (holder.isUpdated()) {
                return true;
            }
        }
        return false;
    }

    boolean scrollByInternal(int x, int y, MotionEvent ev, int type) {
        int unconsumedX = 0;
        int unconsumedY = 0;
        int consumedX = 0;
        int consumedY = 0;

        consumePendingUpdateOperations();
        if (mAdapter != null) {
            mReusableIntPair[0] = 0;
            mReusableIntPair[1] = 0;
            scrollStep(x, y, mReusableIntPair);
            consumedX = mReusableIntPair[0];
            consumedY = mReusableIntPair[1];
            unconsumedX = x - consumedX;
            unconsumedY = y - consumedY;
        }
        if (!mItemDecorations.isEmpty()) {
            invalidate();
        }

        mReusableIntPair[0] = 0;
        mReusableIntPair[1] = 0;
        dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, mScrollOffset, type, mReusableIntPair);
        unconsumedX -= mReusableIntPair[0];
        unconsumedY -= mReusableIntPair[1];
        boolean consumedNestedScroll = mReusableIntPair[0] != 0 || mReusableIntPair[1] != 0;

        mLastTouchX -= mScrollOffset[0];
        mLastTouchY -= mScrollOffset[1];
        mNestedOffsets[0] += mScrollOffset[0];
        mNestedOffsets[1] += mScrollOffset[1];

        if (!mPreventFirstGlow && getOverScrollMode() != View.OVER_SCROLL_NEVER) {
            if (ev != null && !MotionEventCompat.isFromSource(ev, InputDevice.SOURCE_MOUSE)) {
                pullGlows(ev.getX(), unconsumedX, ev.getY(), unconsumedY);
            }
            considerReleasingGlowsOnScroll(x, y);
        }
        if (consumedX != 0 || consumedY != 0) {
            dispatchOnScrolled(consumedX, consumedY);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        if ((mLayout instanceof StaggeredGridLayoutManager) && (!canScrollVertically(-1) || !canScrollVertically(1))) {
            mLayout.onScrollStateChanged(SCROLL_STATE_IDLE);
        }
        mPreventFirstGlow = false;
        return consumedNestedScroll || consumedX != 0 || consumedY != 0;
    }

    private int releaseHorizontalGlow(int deltaX, float y) {
        float consumed = 0;
        float displacement = y / getHeight();
        float pullDistance = (float) deltaX / getWidth();
        if (mLeftGlow != null && EdgeEffectSupport.getDistance(mLeftGlow) != 0) {
            if (canScrollHorizontally(-1)) {
                mLeftGlow.onRelease();
            } else {
                consumed = -EdgeEffectSupport.onPullDistance(mLeftGlow, -pullDistance, 1 - displacement);
                if (EdgeEffectSupport.getDistance(mLeftGlow) == 0) {
                    mLeftGlow.onRelease();
                }
            }
            invalidate();
        } else if (mRightGlow != null && EdgeEffectSupport.getDistance(mRightGlow) != 0) {
            if (canScrollHorizontally(1)) {
                mRightGlow.onRelease();
            } else {
                consumed = EdgeEffectSupport.onPullDistance(mRightGlow, pullDistance, displacement);
                if (EdgeEffectSupport.getDistance(mRightGlow) == 0) {
                    mRightGlow.onRelease();
                }
            }
            invalidate();
        }
        return Math.round(consumed * getWidth());
    }

    private int releaseVerticalGlow(int deltaY, float x) {
        float consumed = 0;
        float displacement = x / getWidth();
        float pullDistance = (float) deltaY / getHeight();
        if (mTopGlow != null && EdgeEffectSupport.getDistance(mTopGlow) != 0) {
            if (canScrollVertically(-1)) {
                mTopGlow.onRelease();
            } else {
                consumed = -EdgeEffectSupport.onPullDistance(mTopGlow, -pullDistance, displacement);
                if (EdgeEffectSupport.getDistance(mTopGlow) == 0) {
                    mTopGlow.onRelease();
                }
            }
            invalidate();
        } else if (mBottomGlow != null && EdgeEffectSupport.getDistance(mBottomGlow) != 0) {
            if (canScrollVertically(1)) {
                mBottomGlow.onRelease();
            } else {
                consumed = EdgeEffectSupport.onPullDistance(mBottomGlow, pullDistance, 1 - displacement);
                if (EdgeEffectSupport.getDistance(mBottomGlow) == 0) {
                    mBottomGlow.onRelease();
                }
            }
            invalidate();
        }
        return Math.round(consumed * getHeight());
    }

    @Override
    public int computeHorizontalScrollOffset() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollHorizontally() ? mLayout.computeHorizontalScrollOffset(mState) : 0;
    }

    @Override
    public int computeHorizontalScrollExtent() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollHorizontally() ? mLayout.computeHorizontalScrollExtent(mState) : 0;
    }

    @Override
    public int computeHorizontalScrollRange() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollHorizontally() ? mLayout.computeHorizontalScrollRange(mState) : 0;
    }

    @Override
    public int computeVerticalScrollOffset() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollVertically() ? mLayout.computeVerticalScrollOffset(mState) : 0;
    }

    @Override
    public int computeVerticalScrollExtent() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollVertically() ? mLayout.computeVerticalScrollExtent(mState) : 0;
    }

    @Override
    public int computeVerticalScrollRange() {
        if (mLayout == null) {
            return 0;
        }
        return mLayout.canScrollVertically() ? mLayout.computeVerticalScrollRange(mState) : 0;
    }

    void startInterceptRequestLayout() {
        mInterceptRequestLayoutDepth++;
        if (mInterceptRequestLayoutDepth == 1 && !mLayoutSuppressed) {
            mLayoutWasDefered = false;
        }
    }

    void stopInterceptRequestLayout(boolean performLayoutChildren) {
        if (mInterceptRequestLayoutDepth < 1) {
            if (DEBUG) {
                throw new IllegalStateException("stopInterceptRequestLayout was called more times than startInterceptRequestLayout." + exceptionLabel());
            }
            mInterceptRequestLayoutDepth = 1;
        }
        if (!performLayoutChildren && !mLayoutSuppressed) {
            mLayoutWasDefered = false;
        }
        if (mInterceptRequestLayoutDepth == 1) {
            if (performLayoutChildren && mLayoutWasDefered && !mLayoutSuppressed && mLayout != null && mAdapter != null) {
                dispatchLayout();
            }
            if (!mLayoutSuppressed) {
                mLayoutWasDefered = false;
            }
        }
        mInterceptRequestLayoutDepth--;
    }

    @Override
    public final void suppressLayout(boolean suppress) {
        if (suppress != mLayoutSuppressed) {
            assertNotInLayoutOrScroll("Do not suppressLayout in layout or scroll");
            if (!suppress) {
                mLayoutSuppressed = false;
                if (mLayoutWasDefered && mLayout != null && mAdapter != null) {
                    requestLayout();
                }
                mLayoutWasDefered = false;
            } else {
                final long now = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                onTouchEvent(cancelEvent);
                mLayoutSuppressed = true;
                mIgnoreMotionEventTillDown = true;
                stopScroll();
            }
        }
    }

    @Override
    public final boolean isLayoutSuppressed() {
        return mLayoutSuppressed;
    }

    @Deprecated
    public void setLayoutFrozen(boolean frozen) {
        suppressLayout(frozen);
    }

    @Deprecated
    public boolean isLayoutFrozen() {
        return isLayoutSuppressed();
    }

    @Deprecated
    @Override
    public void setLayoutTransition(LayoutTransition transition) {
        if (Build.VERSION.SDK_INT < 18) {
            if (transition == null) {
                suppressLayout(false);
                return;
            } else {
                int layoutTransitionChanging = 4;
                if (transition.getAnimator(LayoutTransition.CHANGE_APPEARING) == null && transition.getAnimator(LayoutTransition.CHANGE_DISAPPEARING) == null && transition.getAnimator(LayoutTransition.APPEARING) == null && transition.getAnimator(LayoutTransition.DISAPPEARING) == null && transition.getAnimator(layoutTransitionChanging) == null) {
                    suppressLayout(true);
                    return;
                }
            }
        }

        if (transition == null) {
            super.setLayoutTransition(null);
        } else {
            throw new IllegalArgumentException("Providing a LayoutTransition into RecyclerView is not supported. Please use setItemAnimator() instead for animating changes to the items in this RecyclerView");
        }
    }

    public void smoothScrollBy(@Px int dx, @Px int dy) {
        smoothScrollBy(dx, dy, null);
    }

    public void smoothScrollBy(@Px int dx, @Px int dy, @Nullable Interpolator interpolator) {
        smoothScrollBy(dx, dy, interpolator, UNDEFINED_DURATION);
    }

    public void smoothScrollBy(@Px int dx, @Px int dy, @Nullable Interpolator interpolator, int duration) {
        smoothScrollBy(dx, dy, interpolator, duration, false);
    }

    void smoothScrollBy(@Px int dx, @Px int dy, @Nullable Interpolator interpolator, int duration, boolean withNestedScrolling) {
        if (mLayout == null) {
            Log.e(TAG, "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        if (mLayoutSuppressed) {
            return;
        }
        if (!mLayout.canScrollHorizontally()) {
            dx = 0;
        }
        if (!mLayout.canScrollVertically()) {
            dy = 0;
        }
        if (dx != 0 || dy != 0) {
            boolean durationSuggestsAnimation = duration == UNDEFINED_DURATION || duration > 0;
            if (durationSuggestsAnimation) {
                if (withNestedScrolling) {
                    int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                    if (dx != 0) {
                        nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                    }
                    if (dy != 0) {
                        nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                    }
                    startNestedScroll(nestedScrollAxis, TYPE_NON_TOUCH);
                }
                mViewFlinger.smoothScrollBy(dx, dy, duration, interpolator);
                showGoToTop();
            } else {
                scrollBy(dx, dy);
            }
        }
    }

    public boolean fling(int velocityX, int velocityY) {
        if (mLayout == null) {
            Log.e(TAG, "Cannot fling without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return false;
        }
        if (mLayoutSuppressed) {
            return false;
        }

        final boolean canScrollHorizontal = mLayout.canScrollHorizontally();
        final boolean canScrollVertical = mLayout.canScrollVertically();

        if (!canScrollHorizontal || Math.abs(velocityX) < mMinFlingVelocity) {
            velocityX = 0;
        }
        if (!canScrollVertical || Math.abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityX == 0 && velocityY == 0) {
            return false;
        }

        if (velocityX != 0) {
            if (mLeftGlow != null && EdgeEffectSupport.getDistance(mLeftGlow) != 0) {
                mLeftGlow.onAbsorb(-velocityX);
                velocityX = 0;
            } else if (mRightGlow != null && EdgeEffectSupport.getDistance(mRightGlow) != 0) {
                mRightGlow.onAbsorb(velocityX);
                velocityX = 0;
            }
        }
        if (velocityY != 0) {
            if (mTopGlow != null && EdgeEffectSupport.getDistance(mTopGlow) != 0) {
                mTopGlow.onAbsorb(-velocityY);
                velocityY = 0;
            } else if (mBottomGlow != null && EdgeEffectSupport.getDistance(mBottomGlow) != 0) {
                mBottomGlow.onAbsorb(velocityY);
                velocityY = 0;
            }
        }
        if (velocityX == 0 && velocityY == 0) {
            return false;
        }

        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            final boolean canScroll = canScrollHorizontal || canScrollVertical;
            dispatchNestedFling(velocityX, velocityY, canScroll);

            if (mOnFlingListener != null && mOnFlingListener.onFling(velocityX, velocityY)) {
                return true;
            }

            if (canScroll) {
                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontal) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertical) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis, TYPE_NON_TOUCH);

                velocityX = Math.max(-mMaxFlingVelocity, Math.min(velocityX, mMaxFlingVelocity));
                velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity));
                mViewFlinger.fling(velocityX, velocityY);
                return true;
            }
        }
        return false;
    }

    public void stopScroll() {
        setScrollState(SCROLL_STATE_IDLE);
        stopScrollersInternal();
    }

    private void stopScrollersInternal() {
        mViewFlinger.stop();
        if (mLayout != null) {
            mLayout.stopSmoothScroller();
        }
    }

    public int getMinFlingVelocity() {
        return mMinFlingVelocity;
    }

    public int getMaxFlingVelocity() {
        return mMaxFlingVelocity;
    }

    private void pullGlows(float x, float overscrollX, float y, float overscrollY) {
        boolean invalidate = false;
        if (overscrollX < 0) {
            ensureLeftGlow();
            EdgeEffectSupport.onPullDistance(mLeftGlow, -overscrollX / getWidth(), 1f - y / getHeight());
            invalidate = true;
        } else if (overscrollX > 0) {
            ensureRightGlow();
            EdgeEffectSupport.onPullDistance(mRightGlow, overscrollX / getWidth(), y / getHeight());
            invalidate = true;
        }

        if (overscrollY < 0) {
            ensureTopGlow();
            EdgeEffectSupport.onPullDistance(mTopGlow, -overscrollY / getHeight(), x / getWidth());
            invalidate = true;
        } else if (overscrollY > 0) {
            ensureBottomGlow();
            EdgeEffectSupport.onPullDistance(mBottomGlow, overscrollY / getHeight(), 1f - x / getWidth());
            invalidate = true;
        }

        mEdgeEffectByDragging = !invalidate;

        if (invalidate || overscrollX != 0 || overscrollY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void releaseGlows() {
        boolean needsInvalidate = false;
        if (mLeftGlow != null) {
            mLeftGlow.onRelease();
            needsInvalidate = mLeftGlow.isFinished();
        }
        if (mTopGlow != null) {
            mTopGlow.onRelease();
            needsInvalidate |= mTopGlow.isFinished();
        }
        if (mRightGlow != null) {
            mRightGlow.onRelease();
            needsInvalidate |= mRightGlow.isFinished();
        }
        if (mBottomGlow != null) {
            mBottomGlow.onRelease();
            needsInvalidate |= mBottomGlow.isFinished();
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void considerReleasingGlowsOnScroll(int dx, int dy) {
        boolean needsInvalidate = false;
        if (mLeftGlow != null && !mLeftGlow.isFinished() && dx > 0) {
            mLeftGlow.onRelease();
            needsInvalidate = mLeftGlow.isFinished();
        }
        if (mRightGlow != null && !mRightGlow.isFinished() && dx < 0) {
            mRightGlow.onRelease();
            needsInvalidate |= mRightGlow.isFinished();
        }
        if (mTopGlow != null && !mTopGlow.isFinished() && dy > 0) {
            mTopGlow.onRelease();
            needsInvalidate |= mTopGlow.isFinished();
        }
        if (mBottomGlow != null && !mBottomGlow.isFinished() && dy < 0) {
            mBottomGlow.onRelease();
            needsInvalidate |= mBottomGlow.isFinished();
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void absorbGlows(int velocityX, int velocityY) {
        if (velocityX < 0) {
            ensureLeftGlow();
            if (mLeftGlow.isFinished()) {
                mLeftGlow.onAbsorb(-velocityX);
            }
        } else if (velocityX > 0) {
            ensureRightGlow();
            if (mRightGlow.isFinished()) {
                mRightGlow.onAbsorb(velocityX);
            }
        }

        if (velocityY < 0) {
            ensureTopGlow();
            if (mTopGlow.isFinished()) {
                mTopGlow.onAbsorb(-velocityY);
            }
        } else if (velocityY > 0) {
            ensureBottomGlow();
            if (mBottomGlow.isFinished()) {
                mBottomGlow.onAbsorb(velocityY);
            }
        }

        if (velocityX != 0 || velocityY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void ensureLeftGlow() {
        if (mLeftGlow != null) {
            return;
        }
        mLeftGlow = mEdgeEffectFactory.createEdgeEffect(this, EdgeEffectFactory.DIRECTION_LEFT);
        if (mClipToPadding) {
            mLeftGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        } else {
            mLeftGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureRightGlow() {
        if (mRightGlow != null) {
            return;
        }
        mRightGlow = mEdgeEffectFactory.createEdgeEffect(this, EdgeEffectFactory.DIRECTION_RIGHT);
        if (mClipToPadding) {
            mRightGlow.setSize(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        } else {
            mRightGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureTopGlow() {
        if (mTopGlow != null) {
            return;
        }
        mTopGlow = mEdgeEffectFactory.createEdgeEffect(this, EdgeEffectFactory.DIRECTION_TOP);
        if (mClipToPadding) {
            mTopGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        } else {
            mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }

    }

    void ensureBottomGlow() {
        if (mBottomGlow != null) {
            return;
        }
        mBottomGlow = mEdgeEffectFactory.createEdgeEffect(this, EdgeEffectFactory.DIRECTION_BOTTOM);
        if (mClipToPadding) {
            mBottomGlow.setSize(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        } else {
            mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    void invalidateGlows() {
        mLeftGlow = mRightGlow = mTopGlow = mBottomGlow = null;
    }

    @SuppressLint("RestrictedApi")
    public void setEdgeEffectFactory(@NonNull EdgeEffectFactory edgeEffectFactory) {
        Preconditions.checkNotNull(edgeEffectFactory);
        mEdgeEffectFactory = edgeEffectFactory;
        invalidateGlows();
    }

    @NonNull
    public EdgeEffectFactory getEdgeEffectFactory() {
        return mEdgeEffectFactory;
    }

    @Override
    public View focusSearch(View focused, int direction) {
        View result = mLayout.onInterceptFocusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        final boolean canRunFocusFailure = mAdapter != null && mLayout != null && !isComputingLayout() && !mLayoutSuppressed;

        final FocusFinder ff = FocusFinder.getInstance();
        if (canRunFocusFailure && (direction == View.FOCUS_FORWARD || direction == View.FOCUS_BACKWARD)) {
            boolean needsFocusFailureLayout = false;
            if (mLayout.canScrollVertically()) {
                final int absDir = direction == View.FOCUS_FORWARD ? View.FOCUS_DOWN : View.FOCUS_UP;
                final View found = ff.findNextFocus(this, focused, absDir);
                needsFocusFailureLayout = found == null;
                if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                    direction = absDir;
                }
            }
            if (!needsFocusFailureLayout && mLayout.canScrollHorizontally()) {
                boolean rtl = mLayout.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
                final int absDir = (direction == View.FOCUS_FORWARD) ^ rtl ? View.FOCUS_RIGHT : View.FOCUS_LEFT;
                final View found = ff.findNextFocus(this, focused, absDir);
                needsFocusFailureLayout = found == null;
                if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                    direction = absDir;
                }
            }
            if (needsFocusFailureLayout) {
                consumePendingUpdateOperations();
                final View focusedItemView = findContainingItemView(focused);
                if (focusedItemView == null) {
                    return null;
                }
                startInterceptRequestLayout();
                mLayout.onFocusSearchFailed(focused, direction, mRecycler, mState);
                stopInterceptRequestLayout(false);
            }
            result = ff.findNextFocus(this, focused, direction);
        } else {
            result = ff.findNextFocus(this, focused, direction);
            if (result == null && canRunFocusFailure) {
                consumePendingUpdateOperations();
                final View focusedItemView = findContainingItemView(focused);
                if (focusedItemView == null) {
                    return null;
                }
                startInterceptRequestLayout();
                result = mLayout.onFocusSearchFailed(focused, direction, mRecycler, mState);
                stopInterceptRequestLayout(false);
            }
        }
        if (result != null && !result.hasFocusable()) {
            if (getFocusedChild() == null || (direction == View.FOCUS_UP && focused != null && focused.getBottom() < result.getBottom() && !canScrollVertically(-1))) {
                return super.focusSearch(focused, direction);
            }
            requestChildOnScreen(result, null);
            return focused;
        }
        if (mIsArrowKeyPressed && result == null && (mLayout instanceof StaggeredGridLayoutManager)) {
            int dt = 0;
            if (direction == View.FOCUS_DOWN) {
                dt = getFocusedChild().getBottom() - getBottom();
            } else if (direction == View.FOCUS_UP) {
                dt = getFocusedChild().getTop() - getTop();
            }
            ((StaggeredGridLayoutManager) mLayout).scrollBy(dt, mRecycler, mState);
            mIsArrowKeyPressed = false;
        }
        return isPreferredNextFocus(focused, result, direction) ? result : super.focusSearch(focused, direction);
    }

    private boolean isPreferredNextFocus(View focused, View next, int direction) {
        if (next == null || next == this || next == focused) {
            return false;
        }
        if (findContainingItemView(next) == null) {
            return false;
        }
        if (focused == null) {
            return true;
        }
        if (findContainingItemView(focused) == null) {
            return true;
        }

        mTempRect.set(0, 0, focused.getWidth(), focused.getHeight());
        mTempRect2.set(0, 0, next.getWidth(), next.getHeight());
        offsetDescendantRectToMyCoords(focused, mTempRect);
        offsetDescendantRectToMyCoords(next, mTempRect2);
        final int rtl = mLayout.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL ? -1 : 1;
        int rightness = 0;
        if ((mTempRect.left < mTempRect2.left || mTempRect.right <= mTempRect2.left) && mTempRect.right < mTempRect2.right) {
            rightness = 1;
        } else if ((mTempRect.right > mTempRect2.right || mTempRect.left >= mTempRect2.right) && mTempRect.left > mTempRect2.left) {
            rightness = -1;
        }
        int downness = 0;
        if ((mTempRect.top < mTempRect2.top || mTempRect.bottom <= mTempRect2.top) && mTempRect.bottom < mTempRect2.bottom) {
            downness = 1;
        } else if ((mTempRect.bottom > mTempRect2.bottom || mTempRect.top >= mTempRect2.bottom) && mTempRect.top > mTempRect2.top) {
            downness = -1;
        }
        switch (direction) {
            case View.FOCUS_LEFT:
                return rightness < 0;
            case View.FOCUS_RIGHT:
                return rightness > 0;
            case View.FOCUS_UP:
                return downness < 0;
            case View.FOCUS_DOWN:
                return downness > 0;
            case View.FOCUS_FORWARD:
                return downness > 0 || (downness == 0 && rightness * rtl > 0);
            case View.FOCUS_BACKWARD:
                return downness < 0 || (downness == 0 && rightness * rtl < 0);
        }
        throw new IllegalArgumentException("Invalid direction: " + direction + exceptionLabel());
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (!mLayout.onRequestChildFocus(this, mState, child, focused) && focused != null) {
            requestChildOnScreen(child, focused);
        }
        super.requestChildFocus(child, focused);
    }

    private void requestChildOnScreen(@NonNull View child, @Nullable View focused) {
        View rectView = (focused != null) ? focused : child;
        mTempRect.set(0, 0, rectView.getWidth(), rectView.getHeight());

        final ViewGroup.LayoutParams focusedLayoutParams = rectView.getLayoutParams();
        if (focusedLayoutParams instanceof LayoutParams) {
            final LayoutParams lp = (LayoutParams) focusedLayoutParams;
            if (!lp.mInsetsDirty) {
                final Rect insets = lp.mDecorInsets;
                mTempRect.left -= insets.left;
                mTempRect.right += insets.right;
                mTempRect.top -= insets.top;
                mTempRect.bottom += insets.bottom;
            }
        }

        if (focused != null) {
            offsetDescendantRectToMyCoords(focused, mTempRect);
            offsetRectIntoDescendantCoords(child, mTempRect);
        }
        mLayout.requestChildRectangleOnScreen(this, child, mTempRect, !mFirstLayoutComplete, (focused == null));
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        return mLayout.requestChildRectangleOnScreen(this, child, rect, immediate);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (mLayout == null || !mLayout.onAddFocusables(this, views, direction, focusableMode)) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (isComputingLayout()) {
            return false;
        }
        return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLayoutOrScrollCounter = 0;
        mIsAttached = true;
        mFirstLayoutComplete = mFirstLayoutComplete && !isLayoutRequested();
        if (mLayout != null) {
            mLayout.dispatchAttachedToWindow(this);
        }
        mPostedAnimatorRunner = false;

        if (ALLOW_THREAD_GAP_WORK) {
            mGapWorker = GapWorker.sGapWorker.get();
            if (mGapWorker == null) {
                mGapWorker = new GapWorker();

                Display display = ViewCompat.getDisplay(this);
                float refreshRate = 60.0f;
                if (!isInEditMode() && display != null) {
                    float displayRefreshRate = display.getRefreshRate();
                    if (displayRefreshRate >= 30.0f) {
                        refreshRate = displayRefreshRate;
                    }
                }
                mGapWorker.mFrameIntervalNs = (long) (1000000000 / refreshRate);
                GapWorker.sGapWorker.set(mGapWorker);
            }
            mGapWorker.add(this);

            if (mLayout != null && mLayout.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                if (mFastScroller != null) {
                    mFastScroller.setScrollbarPosition(getVerticalScrollbarPosition());
                }
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mItemAnimator != null) {
            mItemAnimator.endAnimations();
        }
        stopScroll();
        mIsAttached = false;
        if (mLayout != null) {
            mLayout.dispatchDetachedFromWindow(this, mRecycler);
        }
        mPendingAccessibilityImportanceChange.clear();
        removeCallbacks(mItemAnimatorRunner);
        mViewInfoStore.onDetach();

        if (ALLOW_THREAD_GAP_WORK && mGapWorker != null) {
            mGapWorker.remove(this);
            mGapWorker = null;
        }

        if (mIndexTipEnabled && mIndexTip != null) {
            mIndexTip.forcedHide();
        }
    }

    @Override
    public boolean isAttachedToWindow() {
        return mIsAttached;
    }

    void assertInLayoutOrScroll(String message) {
        if (!isComputingLayout()) {
            if (message == null) {
                throw new IllegalStateException("Cannot call this method unless RecyclerView is computing a layout or scrolling" + exceptionLabel());
            }
            throw new IllegalStateException(message + exceptionLabel());
        }
    }

    void assertNotInLayoutOrScroll(String message) {
        if (isComputingLayout()) {
            if (message == null) {
                throw new IllegalStateException("Cannot call this method while RecyclerView is computing a layout or scrolling" + exceptionLabel());
            }
            throw new IllegalStateException(message);
        }
        if (mDispatchScrollCounter > 0) {
            Log.w(TAG, "Cannot call this method in a scroll callback. Scroll callbacks might be run during a measure & layout pass where you cannot change the RecyclerView data. Any method call that might change the structure of the RecyclerView or the adapter contents should be postponed to the next frame.", new IllegalStateException("" + exceptionLabel()));
        }
    }

    public void addOnItemTouchListener(@NonNull OnItemTouchListener listener) {
        mOnItemTouchListeners.add(listener);
    }

    public void removeOnItemTouchListener(@NonNull OnItemTouchListener listener) {
        mOnItemTouchListeners.remove(listener);
        if (mInterceptingOnItemTouchListener == listener) {
            mInterceptingOnItemTouchListener = null;
        }
    }

    private boolean dispatchToOnItemTouchListeners(MotionEvent e) {
        if (mInterceptingOnItemTouchListener == null) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                return false;
            }
            return findInterceptingOnItemTouchListener(e);
        } else {
            mInterceptingOnItemTouchListener.onTouchEvent(this, e);
            final int action = e.getAction();
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                mInterceptingOnItemTouchListener = null;
            }
            return true;
        }
    }

    private boolean findInterceptingOnItemTouchListener(MotionEvent e) {
        int action = e.getAction();
        final int listenerCount = mOnItemTouchListeners.size();
        for (int i = 0; i < listenerCount; i++) {
            final OnItemTouchListener listener = mOnItemTouchListeners.get(i);
            if (listener.onInterceptTouchEvent(this, e) && action != MotionEvent.ACTION_CANCEL) {
                mInterceptingOnItemTouchListener = listener;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mLayoutSuppressed) {
            return false;
        }

        mInterceptingOnItemTouchListener = null;
        if (findInterceptingOnItemTouchListener(e)) {
            cancelScroll();
            return true;
        }

        if (mLayout == null) {
            return false;
        }

        final boolean canScrollHorizontally = mLayout.canScrollHorizontally();
        final boolean canScrollVertically = mLayout.canScrollVertically();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(e);

        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();
        final MotionEvent obtain = MotionEvent.obtain(e);;

        if (mFastScroller != null && mFastScroller.onInterceptTouchEvent(e)) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mIgnoreMotionEventTillDown) {
                    mIgnoreMotionEventTillDown = false;
                }
                mScrollPointerId = e.getPointerId(0);
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);

                if (mUsePagingTouchSlopForStylus) {
                    if (e.isFromSource(InputDeviceCompat.SOURCE_STYLUS)) {
                        mTouchSlop = mPagingTouchSlop;
                    } else {
                        mTouchSlop = mTouchSlop2;
                    }
                }

                if (stopGlowAnimations(e) || mScrollState == SCROLL_STATE_SETTLING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                    stopNestedScroll(TYPE_NON_TOUCH);
                }

                mNestedOffsets[0] = mNestedOffsets[1] = 0;

                if (mHasNestedScrollRange) {
                    adjustNestedScrollRange();
                }
                mPreventFirstGlow = false;

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontally) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertically) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
                mIsSkipMoveEvent = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mScrollPointerId = e.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (e.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
                break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                int lastTouchXDiff = mLastTouchX - x;
                int lastTouchYDiff = mLastTouchY - y;
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    final int dx = x - mInitialTouchX;
                    final int dy = y - mInitialTouchY;
                    boolean startScroll = false;
                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
                        lastTouchXDiff = lastTouchXDiff > 0 ? lastTouchXDiff - mTouchSlop : lastTouchXDiff + mTouchSlop;
                        mLastTouchX = x;
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                        lastTouchYDiff = lastTouchYDiff > 0 ? lastTouchYDiff - mTouchSlop : lastTouchYDiff + mTouchSlop;
                        mPreventFirstGlow = true;
                        mLastTouchY = y;
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];
                    if (!mGoToTopMoved) {
                        if (scrollByInternal(canScrollHorizontally ? lastTouchXDiff : 0, canScrollVertically ? lastTouchYDiff : 0, obtain, 0)) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (mGapWorker != null && lastTouchXDiff != 0 && lastTouchYDiff != 0) {
                        mGapWorker.postFromTraversal(this, lastTouchXDiff, lastTouchYDiff);
                    }
                }
                adjustNestedScrollRangeBy(lastTouchYDiff);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
                mVelocityTracker.clear();
                stopNestedScroll(TYPE_TOUCH);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                cancelScroll();
            }
            break;

            case MOTION_EVENT_ACTION_PEN_DOWN: {
                if (mIgnoreMotionEventTillDown) {
                    mIgnoreMotionEventTillDown = false;
                }
            }
        }
        return mScrollState == SCROLL_STATE_DRAGGING;
    }

    private boolean stopGlowAnimations(MotionEvent e) {
        boolean stopped = false;
        if (mLeftGlow != null && EdgeEffectSupport.getDistance(mLeftGlow) != 0 && !canScrollHorizontally(-1)) {
            EdgeEffectSupport.onPullDistance(mLeftGlow, 0, 1 - (e.getY() / getHeight()));
            stopped = true;
        }
        if (mRightGlow != null && EdgeEffectSupport.getDistance(mRightGlow) != 0 && !canScrollHorizontally(1)) {
            EdgeEffectSupport.onPullDistance(mRightGlow, 0, e.getY() / getHeight());
            stopped = true;
        }
        if (mTopGlow != null && EdgeEffectSupport.getDistance(mTopGlow) != 0 && !canScrollVertically(-1)) {
            EdgeEffectSupport.onPullDistance(mTopGlow, 0, e.getX() / getWidth());
            stopped = true;
        }
        if (mBottomGlow != null && EdgeEffectSupport.getDistance(mBottomGlow) != 0 && !canScrollVertically(1)) {
            EdgeEffectSupport.onPullDistance(mBottomGlow, 0, 1 - e.getX() / getWidth());
            stopped = true;
        }
        return stopped;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final int listenerCount = mOnItemTouchListeners.size();
        for (int i = 0; i < listenerCount; i++) {
            final OnItemTouchListener listener = mOnItemTouchListeners.get(i);
            listener.onRequestDisallowInterceptTouchEvent(disallowIntercept);
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mLayoutSuppressed || mIgnoreMotionEventTillDown) {
            return false;
        }
        if (dispatchToOnItemTouchListeners(e)) {
            cancelScroll();
            return true;
        }

        if (mLayout == null) {
            return false;
        }

        final boolean canScrollHorizontally = mLayout.canScrollHorizontally();
        final boolean canScrollVertically = mLayout.canScrollVertically();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;

        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
        }
        final MotionEvent vtev = MotionEvent.obtain(e);
        vtev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);

        if (mFastScroller != null && mFastScroller.onTouchEvent(e)) {
            if (mFastScrollerEventListener != null) {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE: {
                        if (mFastScroller.getEffectState() == SeslRecyclerViewFastScroller.EFFECT_STATE_OPEN) {
                            mFastScrollerEventListener.onPressed(mFastScroller.getScrollY());
                        }
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        if (mFastScroller.getEffectState() == SeslRecyclerViewFastScroller.EFFECT_STATE_CLOSE) {
                            mFastScrollerEventListener.onReleased(mFastScroller.getScrollY());
                        }
                    }
                }
            }
            vtev.recycle();
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = e.getPointerId(0);
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);

                if (mHasNestedScrollRange) {
                    adjustNestedScrollRange();
                }

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontally) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertically) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollPointerId = e.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (e.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (canScrollHorizontally) {
                        if (dx > 0) {
                            dx = Math.max(0, dx - mTouchSlop);
                        } else {
                            dx = Math.min(0, dx + mTouchSlop);
                        }
                        if (dx != 0) {
                            startScroll = true;
                        }
                    }
                    if (canScrollVertically) {
                        if (dy > 0) {
                            dy = Math.max(0, dy - mTouchSlop);
                        } else {
                            dy = Math.min(0, dy + mTouchSlop);
                        }
                        if (dy != 0) {
                            startScroll = true;
                        }
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mReusableIntPair[0] = 0;
                    mReusableIntPair[1] = 0;
                    dx -= releaseHorizontalGlow(dx, e.getY());
                    dy -= releaseVerticalGlow(dy, e.getX());

                    if (dispatchNestedPreScroll(canScrollHorizontally ? dx : 0, canScrollVertically ? dy : 0, mReusableIntPair, mScrollOffset, TYPE_TOUCH)) {
                        dx -= mReusableIntPair[0];
                        dy -= mReusableIntPair[1];
                        mNestedOffsets[0] += mScrollOffset[0];
                        mNestedOffsets[1] += mScrollOffset[1];
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];

                    if ((e.getFlags() & 33554432) != 0) {
                        mVelocityTracker.addMovement(vtev);
                        mIsSkipMoveEvent = true;
                        return false;
                    }
                    if (!mGoToTopMoved) {
                        if (scrollByInternal(canScrollHorizontally ? dx : 0, canScrollVertically ? dy : 0, e, TYPE_TOUCH)) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (mGapWorker != null && (dx != 0 || dy != 0)) {
                        mGapWorker.postFromTraversal(this, dx, dy);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                onPointerUp(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float xvel = canScrollHorizontally ? -mVelocityTracker.getXVelocity(mScrollPointerId) : 0;
                final float yvel = canScrollVertically ? -mVelocityTracker.getYVelocity(mScrollPointerId) : 0;
                if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                Log.i(TAG, "onTouchUp() velocity : " + yvel + ", last move skip : " + mIsSkipMoveEvent + ", use scroller : " + mViewFlinger.mOverScroller.getClass().getName());
                resetScroll();
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                cancelScroll();
            }
            break;
        }

        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();

        return true;
    }

    private void resetScroll() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        stopNestedScroll(TYPE_TOUCH);
        releaseGlows();
    }

    private void cancelScroll() {
        resetScroll();
        setScrollState(SCROLL_STATE_IDLE);
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = e.getActionIndex();
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = e.getPointerId(newIndex);
            mInitialTouchX = mLastTouchX = (int) (e.getX(newIndex) + 0.5f);
            mInitialTouchY = mLastTouchY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mLayout == null) {
            return false;
        }
        if (mLayoutSuppressed) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_SCROLL) {
            final float vScroll, hScroll;
            if ((event.getSource() & InputDeviceCompat.SOURCE_CLASS_POINTER) != 0) {
                if (mLayout.canScrollVertically()) {
                    vScroll = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                } else {
                    vScroll = 0f;
                }
                if (mLayout.canScrollHorizontally()) {
                    hScroll = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
                } else {
                    hScroll = 0f;
                }
            } else if ((event.getSource() & InputDeviceCompat.SOURCE_ROTARY_ENCODER) != 0) {
                final float axisScroll = event.getAxisValue(MotionEventCompat.AXIS_SCROLL);
                if (mLayout.canScrollVertically()) {
                    vScroll = -axisScroll;
                    hScroll = 0f;
                } else if (mLayout.canScrollHorizontally()) {
                    vScroll = 0f;
                    hScroll = axisScroll;
                } else {
                    vScroll = 0f;
                    hScroll = 0f;
                }
            } else {
                vScroll = 0f;
                hScroll = 0f;
            }

            if (vScroll != 0 || hScroll != 0) {
                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (vScroll > 0) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                } else {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }

                startNestedScroll(nestedScrollAxis, TYPE_NON_TOUCH);
                if (!dispatchNestedPreScroll((int) (hScroll * mScaledHorizontalScrollFactor), (int) (vScroll * mScaledVerticalScrollFactor), null, null, TYPE_NON_TOUCH)) {
                    nestedScrollByInternal((int) (hScroll * mScaledHorizontalScrollFactor), (int) (vScroll * mScaledVerticalScrollFactor), event, TYPE_NON_TOUCH);
                }
            }
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (mLayout == null) {
            defaultOnMeasure(widthSpec, heightSpec);
            return;
        }
        mListPadding.set(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        if (mLayout.isAutoMeasureEnabled()) {
            final int widthMode = MeasureSpec.getMode(widthSpec);
            final int heightMode = MeasureSpec.getMode(heightSpec);

            mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);

            mLastAutoMeasureSkippedDueToExact = widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY;
            if (mLastAutoMeasureSkippedDueToExact || mAdapter == null) {
                return;
            }

            if (mState.mLayoutStep == State.STEP_START) {
                dispatchLayoutStep1();
            }
            mLayout.setMeasureSpecs(widthSpec, heightSpec);
            mState.mIsMeasuring = true;
            dispatchLayoutStep2();

            mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);

            if (mLayout.shouldMeasureTwice()) {
                mLayout.setMeasureSpecs(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
                mState.mIsMeasuring = true;
                dispatchLayoutStep2();
                mLayout.setMeasuredDimensionFromChildren(widthSpec, heightSpec);
            }

            mLastAutoMeasureNonExactMeasuredWidth = getMeasuredWidth();
            mLastAutoMeasureNonExactMeasuredHeight = getMeasuredHeight();
        } else {
            if (mHasFixedSize) {
                mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
                return;
            }
            if (mAdapterUpdateDuringMeasure) {
                startInterceptRequestLayout();
                onEnterLayoutOrScroll();
                processAdapterUpdatesAndSetAnimationFlags();
                onExitLayoutOrScroll();

                if (mState.mRunPredictiveAnimations) {
                    mState.mInPreLayout = true;
                } else {
                    mAdapterHelper.consumeUpdatesInOnePass();
                    mState.mInPreLayout = false;
                }
                mAdapterUpdateDuringMeasure = false;
                stopInterceptRequestLayout(false);
            } else if (mState.mRunPredictiveAnimations) {
                setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
                return;
            }

            if (mAdapter != null) {
                mState.mItemCount = mAdapter.getItemCount();
            } else {
                mState.mItemCount = 0;
            }
            startInterceptRequestLayout();
            mLayout.onMeasure(mRecycler, mState, widthSpec, heightSpec);
            stopInterceptRequestLayout(false);
            mState.mInPreLayout = false;
        }
    }

    void defaultOnMeasure(int widthSpec, int heightSpec) {
        final int width = LayoutManager.chooseSize(widthSpec, getPaddingLeft() + getPaddingRight(), ViewCompat.getMinimumWidth(this));
        final int height = LayoutManager.chooseSize(heightSpec, getPaddingTop() + getPaddingBottom(), ViewCompat.getMinimumHeight(this));

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            if (mFastScroller != null) {
                mFastScroller.onSizeChanged(w, h, oldw, oldh);
            }
            invalidateGlows();
        }
    }

    public void setItemAnimator(@Nullable ItemAnimator animator) {
        if (mItemAnimator != null) {
            mItemAnimator.endAnimations();
            mItemAnimator.setListener(null);
        }
        mItemAnimator = animator;
        if (mItemAnimator != null) {
            mItemAnimator.setListener(mItemAnimatorListener);
            mItemAnimator.setHostView(this);
        }
    }

    public void onEnterLayoutOrScroll() {
        mLayoutOrScrollCounter++;
    }

    void onExitLayoutOrScroll() {
        onExitLayoutOrScroll(true);
    }

    public void onExitLayoutOrScroll(boolean enableChangeEvents) {
        mLayoutOrScrollCounter--;
        if (mLayoutOrScrollCounter < 1) {
            if (DEBUG && mLayoutOrScrollCounter < 0) {
                throw new IllegalStateException("layout or scroll counter cannot go below zero. Some calls are not matching" + exceptionLabel());
            }
            mLayoutOrScrollCounter = 0;
            if (enableChangeEvents) {
                dispatchContentChangedIfNecessary();
                dispatchPendingImportantForAccessibilityChanges();
            }
        }
    }

    boolean isAccessibilityEnabled() {
        return mAccessibilityManager != null && mAccessibilityManager.isEnabled();
    }

    private void dispatchContentChangedIfNecessary() {
        final int flags = mEatenAccessibilityChangeFlags;
        mEatenAccessibilityChangeFlags = 0;
        if (flags != 0 && isAccessibilityEnabled()) {
            final AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
            AccessibilityEventCompat.setContentChangeTypes(event, flags);
            sendAccessibilityEventUnchecked(event);
        }
    }

    public boolean isComputingLayout() {
        return mLayoutOrScrollCounter > 0;
    }

    boolean shouldDeferAccessibilityEvent(AccessibilityEvent event) {
        if (isComputingLayout()) {
            int type = 0;
            if (event != null) {
                type = AccessibilityEventCompat.getContentChangeTypes(event);
            }
            if (type == 0) {
                type = AccessibilityEventCompat.CONTENT_CHANGE_TYPE_UNDEFINED;
            }
            mEatenAccessibilityChangeFlags |= type;
            return true;
        }
        return false;
    }

    @Override
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (shouldDeferAccessibilityEvent(event)) {
            return;
        }
        super.sendAccessibilityEventUnchecked(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Nullable
    public ItemAnimator getItemAnimator() {
        return mItemAnimator;
    }

    void postAnimationRunner() {
        if (!mPostedAnimatorRunner && mIsAttached) {
            ViewCompat.postOnAnimation(this, mItemAnimatorRunner);
            mPostedAnimatorRunner = true;
        }
    }

    private boolean predictiveItemAnimationsEnabled() {
        return (mItemAnimator != null && mLayout.supportsPredictiveItemAnimations());
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        if (mDataSetHasChangedAfterLayout) {
            mAdapterHelper.reset();
            if (mDispatchItemsChangedEvent) {
                mLayout.onItemsChanged(this);
            }
        }
        if (predictiveItemAnimationsEnabled()) {
            mAdapterHelper.preProcess();
        } else {
            mAdapterHelper.consumeUpdatesInOnePass();
        }
        boolean animationTypeSupported = mItemsAddedOrRemoved || mItemsChanged;
        mState.mRunSimpleAnimations = mFirstLayoutComplete && mItemAnimator != null && (mDataSetHasChangedAfterLayout || animationTypeSupported || mLayout.mRequestedSimpleAnimations) && (!mDataSetHasChangedAfterLayout || mAdapter.hasStableIds());
        mState.mRunPredictiveAnimations = mState.mRunSimpleAnimations && animationTypeSupported && !mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled();
    }

    void dispatchLayout() {
        if (mAdapter == null) {
            Log.w(TAG, "No adapter attached; skipping layout");
            return;
        }
        if (mLayout == null) {
            Log.e(TAG, "No layout manager attached; skipping layout");
            return;
        }
        mState.mIsMeasuring = false;

        boolean needsRemeasureDueToExactSkip = mLastAutoMeasureSkippedDueToExact && (mLastAutoMeasureNonExactMeasuredWidth != getWidth() || mLastAutoMeasureNonExactMeasuredHeight != getHeight());
        mLastAutoMeasureNonExactMeasuredWidth = 0;
        mLastAutoMeasureNonExactMeasuredHeight = 0;
        mLastAutoMeasureSkippedDueToExact = false;

        if (mState.mLayoutStep == State.STEP_START) {
            dispatchLayoutStep1();
            mLayout.setExactMeasureSpecsFrom(this);
            dispatchLayoutStep2();
        } else if (mAdapterHelper.hasUpdates() || needsRemeasureDueToExactSkip || mLayout.getWidth() != getWidth() || mLayout.getHeight() != getHeight()) {
            mLayout.setExactMeasureSpecsFrom(this);
            dispatchLayoutStep2();
        } else {
            mLayout.setExactMeasureSpecsFrom(this);
        }
        dispatchLayoutStep3();
    }

    private void saveFocusInfo() {
        View child = null;
        if (mPreserveFocusAfterLayout && hasFocus() && mAdapter != null) {
            child = getFocusedChild();
        }

        final ViewHolder focusedVh = child == null ? null : findContainingViewHolder(child);
        if (focusedVh == null) {
            resetFocusInfo();
        } else {
            mState.mFocusedItemId = mAdapter.hasStableIds() ? focusedVh.getItemId() : NO_ID;
            mState.mFocusedItemPosition = mDataSetHasChangedAfterLayout ? NO_POSITION : (focusedVh.isRemoved() ? focusedVh.mOldPosition : focusedVh.getAbsoluteAdapterPosition());
            mState.mFocusedSubChildId = getDeepestFocusedViewWithId(focusedVh.itemView);
        }
    }

    private void resetFocusInfo() {
        mState.mFocusedItemId = NO_ID;
        mState.mFocusedItemPosition = NO_POSITION;
        mState.mFocusedSubChildId = View.NO_ID;
    }

    @Nullable
    private View findNextViewToFocus() {
        int startFocusSearchIndex = mState.mFocusedItemPosition != -1 ? mState.mFocusedItemPosition : 0;
        ViewHolder nextFocus;
        final int itemCount = mState.getItemCount();
        for (int i = startFocusSearchIndex; i < itemCount; i++) {
            nextFocus = findViewHolderForAdapterPosition(i);
            if (nextFocus == null) {
                break;
            }
            if (nextFocus.itemView.hasFocusable()) {
                return nextFocus.itemView;
            }
        }
        final int limit = Math.min(itemCount, startFocusSearchIndex);
        for (int i = limit - 1; i >= 0; i--) {
            nextFocus = findViewHolderForAdapterPosition(i);
            if (nextFocus == null) {
                return null;
            }
            if (nextFocus.itemView.hasFocusable()) {
                return nextFocus.itemView;
            }
        }
        return null;
    }

    private void recoverFocusFromState() {
        if (!mPreserveFocusAfterLayout || mAdapter == null || !hasFocus() || getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS || (getDescendantFocusability() == FOCUS_BEFORE_DESCENDANTS && isFocused())) {
            return;
        }
        if (!isFocused()) {
            final View focusedChild = getFocusedChild();
            if (IGNORE_DETACHED_FOCUSED_CHILD && (focusedChild.getParent() == null || !focusedChild.hasFocus())) {
                if (mChildHelper.getChildCount() == 0) {
                    requestFocus();
                    return;
                }
            } else if (!mChildHelper.isHidden(focusedChild)) {
                return;
            }
        }
        ViewHolder focusTarget = null;
        if (mState.mFocusedItemId != NO_ID && mAdapter.hasStableIds()) {
            focusTarget = findViewHolderForItemId(mState.mFocusedItemId);
        }
        View viewToFocus = null;
        if (focusTarget == null || mChildHelper.isHidden(focusTarget.itemView) || !focusTarget.itemView.hasFocusable()) {
            if (mChildHelper.getChildCount() > 0) {
                viewToFocus = findNextViewToFocus();
            }
        } else {
            viewToFocus = focusTarget.itemView;
        }

        if (viewToFocus != null) {
            if (mState.mFocusedSubChildId != NO_ID) {
                View child = viewToFocus.findViewById(mState.mFocusedSubChildId);
                if (child != null && child.isFocusable()) {
                    viewToFocus = child;
                }
            }
            viewToFocus.requestFocus();
        }
    }

    private int getDeepestFocusedViewWithId(View view) {
        int lastKnownId = view.getId();
        while (!view.isFocused() && view instanceof ViewGroup && view.hasFocus()) {
            view = ((ViewGroup) view).getFocusedChild();
            final int id = view.getId();
            if (id != View.NO_ID) {
                lastKnownId = view.getId();
            }
        }
        return lastKnownId;
    }

    final void fillRemainingScrollValues(State state) {
        if (getScrollState() == SCROLL_STATE_SETTLING) {
            final OverScroller scroller = mViewFlinger.mOverScroller;
            state.mRemainingScrollHorizontal = scroller.getFinalX() - scroller.getCurrX();
            state.mRemainingScrollVertical = scroller.getFinalY() - scroller.getCurrY();
        } else {
            state.mRemainingScrollHorizontal = 0;
            state.mRemainingScrollVertical = 0;
        }
    }

    private void dispatchLayoutStep1() {
        mState.assertLayoutStep(State.STEP_START);
        fillRemainingScrollValues(mState);
        mState.mIsMeasuring = false;
        startInterceptRequestLayout();
        mViewInfoStore.clear();
        onEnterLayoutOrScroll();
        processAdapterUpdatesAndSetAnimationFlags();
        saveFocusInfo();
        mState.mTrackOldChangeHolders = mState.mRunSimpleAnimations && mItemsChanged;
        mItemsAddedOrRemoved = mItemsChanged = false;
        mState.mInPreLayout = mState.mRunPredictiveAnimations;
        mState.mItemCount = mAdapter.getItemCount();
        findMinMaxChildLayoutPositions(mMinMaxLayoutPositions);

        if (mState.mRunSimpleAnimations) {
            int count = mChildHelper.getChildCount();
            for (int i = 0; i < count; ++i) {
                final ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
                if (holder.shouldIgnore() || (holder.isInvalid() && !mAdapter.hasStableIds())) {
                    continue;
                }
                final ItemHolderInfo animationInfo = mItemAnimator.recordPreLayoutInformation(mState, holder, ItemAnimator.buildAdapterChangeFlagsForAnimations(holder), holder.getUnmodifiedPayloads());
                mViewInfoStore.addToPreLayout(holder, animationInfo);
                if (mState.mTrackOldChangeHolders && holder.isUpdated() && !holder.isRemoved() && !holder.shouldIgnore() && !holder.isInvalid()) {
                    long key = getChangedHolderKey(holder);
                    mViewInfoStore.addToOldChangeHolders(key, holder);
                }
            }
        }
        if (mState.mRunPredictiveAnimations) {
            saveOldPositions();
            final boolean didStructureChange = mState.mStructureChanged;
            mState.mStructureChanged = false;
            mLayout.onLayoutChildren(mRecycler, mState);
            mState.mStructureChanged = didStructureChange;

            for (int i = 0; i < mChildHelper.getChildCount(); ++i) {
                final View child = mChildHelper.getChildAt(i);
                final ViewHolder viewHolder = getChildViewHolderInt(child);
                if (viewHolder.shouldIgnore()) {
                    continue;
                }
                if (!mViewInfoStore.isInPreLayout(viewHolder)) {
                    int flags = ItemAnimator.buildAdapterChangeFlagsForAnimations(viewHolder);
                    boolean wasHidden = viewHolder.hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                    if (!wasHidden) {
                        flags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                    }
                    final ItemHolderInfo animationInfo = mItemAnimator.recordPreLayoutInformation(mState, viewHolder, flags, viewHolder.getUnmodifiedPayloads());
                    if (wasHidden) {
                        recordAnimationInfoIfBouncedHiddenView(viewHolder, animationInfo);
                    } else {
                        mViewInfoStore.addToAppearedInPreLayoutHolders(viewHolder, animationInfo);
                    }
                }
            }
            clearOldPositions();
        } else {
            clearOldPositions();
        }
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
        mState.mLayoutStep = State.STEP_LAYOUT;
    }

    private void dispatchLayoutStep2() {
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();
        mState.assertLayoutStep(State.STEP_LAYOUT | State.STEP_ANIMATIONS);
        mAdapterHelper.consumeUpdatesInOnePass();
        mState.mItemCount = mAdapter.getItemCount();
        mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        if (mPendingSavedState != null && mAdapter.canRestoreState()) {
            if (mPendingSavedState.mLayoutState != null) {
                mLayout.onRestoreInstanceState(mPendingSavedState.mLayoutState);
            }
            mPendingSavedState = null;
        }
        mState.mInPreLayout = false;
        mLayout.onLayoutChildren(mRecycler, mState);

        mState.mStructureChanged = false;

        mState.mRunSimpleAnimations = mState.mRunSimpleAnimations && mItemAnimator != null;
        mState.mLayoutStep = State.STEP_ANIMATIONS;
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
    }

    private void dispatchLayoutStep3() {
        mState.assertLayoutStep(State.STEP_ANIMATIONS);
        startInterceptRequestLayout();
        onEnterLayoutOrScroll();
        mState.mLayoutStep = State.STEP_START;
        if (mState.mRunSimpleAnimations) {
            for (int i = mChildHelper.getChildCount() - 1; i >= 0; i--) {
                ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
                if (holder.shouldIgnore()) {
                    continue;
                }
                long key = getChangedHolderKey(holder);
                final ItemHolderInfo animationInfo = mItemAnimator.recordPostLayoutInformation(mState, holder);
                ViewHolder oldChangeViewHolder = mViewInfoStore.getFromOldChangeHolders(key);
                if (oldChangeViewHolder != null && !oldChangeViewHolder.shouldIgnore()) {
                    final boolean oldDisappearing = mViewInfoStore.isDisappearing(oldChangeViewHolder);
                    final boolean newDisappearing = mViewInfoStore.isDisappearing(holder);
                    if (oldDisappearing && oldChangeViewHolder == holder) {
                        mViewInfoStore.addToPostLayout(holder, animationInfo);
                    } else {
                        final ItemHolderInfo preInfo = mViewInfoStore.popFromPreLayout(oldChangeViewHolder);
                        mViewInfoStore.addToPostLayout(holder, animationInfo);
                        ItemHolderInfo postInfo = mViewInfoStore.popFromPostLayout(holder);
                        if (preInfo == null) {
                            handleMissingPreInfoForChangeError(key, holder, oldChangeViewHolder);
                        } else {
                            animateChange(oldChangeViewHolder, holder, preInfo, postInfo, oldDisappearing, newDisappearing);
                        }
                    }
                } else {
                    mViewInfoStore.addToPostLayout(holder, animationInfo);
                }
            }

            mViewInfoStore.process(mViewInfoProcessCallback);
        }

        mLastBlackTop = mBlackTop;
        mBlackTop = -1;
        if (mDrawRect && !canScrollVertically(-1) && !canScrollVertically(1)) {
            int i = -1;
            int itemCount = mAdapter.getItemCount() - 1;
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayout;
            if (linearLayoutManager.mReverseLayout && linearLayoutManager.mStackFromEnd) {
                mDrawReverse = true;
                i = 0;
            } else if (linearLayoutManager.mReverseLayout || linearLayoutManager.mStackFromEnd) {
                mDrawRect = false;
            } else {
                i = itemCount;
            }
            if (i >= 0 && i <= findLastVisibleItemPosition()) {
                View child = mChildHelper.getChildAt(i);
                if (child != null) {
                    mBlackTop = child.getBottom();
                }
            }
        }

        mLayout.removeAndRecycleScrapInt(mRecycler);
        mState.mPreviousLayoutItemCount = mState.mItemCount;
        mDataSetHasChangedAfterLayout = false;
        mDispatchItemsChangedEvent = false;
        mState.mRunSimpleAnimations = false;

        mState.mRunPredictiveAnimations = false;
        mLayout.mRequestedSimpleAnimations = false;
        if (mRecycler.mChangedScrap != null) {
            mRecycler.mChangedScrap.clear();
        }
        if (mLayout.mPrefetchMaxObservedInInitialPrefetch) {
            mLayout.mPrefetchMaxCountObserved = 0;
            mLayout.mPrefetchMaxObservedInInitialPrefetch = false;
            mRecycler.updateViewCacheSize();
        }

        mLayout.onLayoutCompleted(mState);
        onExitLayoutOrScroll();
        stopInterceptRequestLayout(false);
        mViewInfoStore.clear();
        if (didChildRangeChange(mMinMaxLayoutPositions[0], mMinMaxLayoutPositions[1])) {
            dispatchOnScrolled(0, 0);
        }
        recoverFocusFromState();
        resetFocusInfo();
    }

    private void handleMissingPreInfoForChangeError(long key, ViewHolder holder, ViewHolder oldChangeViewHolder) {
        final int childCount = mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mChildHelper.getChildAt(i);
            ViewHolder other = getChildViewHolderInt(view);
            if (other == holder) {
                continue;
            }
            final long otherKey = getChangedHolderKey(other);
            if (otherKey == key) {
                if (mAdapter != null && mAdapter.hasStableIds()) {
                    throw new IllegalStateException("Two different ViewHolders have the same stable ID. Stable IDs in your adapter MUST BE unique and SHOULD NOT change.\n ViewHolder 1:" + other + " \n View Holder 2:" + holder + exceptionLabel());
                } else {
                    throw new IllegalStateException("Two different ViewHolders have the same change ID. This might happen due to inconsistent Adapter update events or if the LayoutManager lays out the same View multiple times.\n ViewHolder 1:" + other + " \n View Holder 2:" + holder + exceptionLabel());
                }
            }
        }
        Log.e(TAG, "Problem while matching changed view holders with the new ones. The pre-layout information for the change holder " + oldChangeViewHolder + " cannot be found but it is necessary for " + holder + exceptionLabel());
    }

    void recordAnimationInfoIfBouncedHiddenView(ViewHolder viewHolder, ItemHolderInfo animationInfo) {
        viewHolder.setFlags(0, ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
        if (mState.mTrackOldChangeHolders && viewHolder.isUpdated() && !viewHolder.isRemoved() && !viewHolder.shouldIgnore()) {
            long key = getChangedHolderKey(viewHolder);
            mViewInfoStore.addToOldChangeHolders(key, viewHolder);
        }
        mViewInfoStore.addToPreLayout(viewHolder, animationInfo);
    }

    private void findMinMaxChildLayoutPositions(int[] into) {
        final int count = mChildHelper.getChildCount();
        if (count == 0) {
            into[0] = NO_POSITION;
            into[1] = NO_POSITION;
            return;
        }
        int minPositionPreLayout = Integer.MAX_VALUE;
        int maxPositionPreLayout = Integer.MIN_VALUE;
        for (int i = 0; i < count; ++i) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getChildAt(i));
            if (holder.shouldIgnore()) {
                continue;
            }
            final int pos = holder.getLayoutPosition();
            if (pos < minPositionPreLayout) {
                minPositionPreLayout = pos;
            }
            if (pos > maxPositionPreLayout) {
                maxPositionPreLayout = pos;
            }
        }
        into[0] = minPositionPreLayout;
        into[1] = maxPositionPreLayout;
    }

    private boolean didChildRangeChange(int minPositionPreLayout, int maxPositionPreLayout) {
        findMinMaxChildLayoutPositions(mMinMaxLayoutPositions);
        return mMinMaxLayoutPositions[0] != minPositionPreLayout || mMinMaxLayoutPositions[1] != maxPositionPreLayout;
    }

    @Override
    protected void removeDetachedView(View child, boolean animate) {
        ViewHolder vh = getChildViewHolderInt(child);
        if (vh != null) {
            if (vh.isTmpDetached()) {
                vh.clearTmpDetachFlag();
            } else if (!vh.shouldIgnore()) {
                throw new IllegalArgumentException("Called removeDetachedView with a view which is not flagged as tmp detached." + vh + exceptionLabel());
            }
        }

        child.clearAnimation();

        dispatchChildDetached(child);
        super.removeDetachedView(child, animate);
    }

    long getChangedHolderKey(ViewHolder holder) {
        return mAdapter.hasStableIds() ? holder.getItemId() : holder.mPosition;
    }

    void animateAppearance(@NonNull ViewHolder itemHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo) {
        itemHolder.setIsRecyclable(false);
        if (mItemAnimator.animateAppearance(itemHolder, preLayoutInfo, postLayoutInfo)) {
            postAnimationRunner();
        }
    }

    void animateDisappearance(@NonNull ViewHolder holder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo) {
        addAnimatingView(holder);
        holder.setIsRecyclable(false);
        if (mItemAnimator.animateDisappearance(holder, preLayoutInfo, postLayoutInfo)) {
            postAnimationRunner();
        }
    }

    private void animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preInfo, @NonNull ItemHolderInfo postInfo, boolean oldHolderDisappearing, boolean newHolderDisappearing) {
        oldHolder.setIsRecyclable(false);
        if (oldHolderDisappearing) {
            addAnimatingView(oldHolder);
        }
        if (oldHolder != newHolder) {
            if (newHolderDisappearing) {
                addAnimatingView(newHolder);
            }
            oldHolder.mShadowedHolder = newHolder;
            addAnimatingView(oldHolder);
            mRecycler.unscrapView(oldHolder);
            newHolder.setIsRecyclable(false);
            newHolder.mShadowingHolder = oldHolder;
        }
        if (mItemAnimator.animateChange(oldHolder, newHolder, preInfo, postInfo)) {
            postAnimationRunner();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        TraceCompat.beginSection(TRACE_ON_LAYOUT_TAG);
        dispatchLayout();
        TraceCompat.endSection();
        mFirstLayoutComplete = true;

        if (mFastScroller != null && mAdapter != null) {
            mFastScroller.onItemCountChanged(getChildCount(), mAdapter.getItemCount());
        }

        if (changed) {
            mSizeChnage = true;
            seslSetImmersiveScrollBottomPadding(0);
            setupGoToTop(-1);
            autoHide(GTP_STATE_SHOWN);
            if (mLayout != null && !mLayout.canScrollHorizontally()) {
                mHasNestedScrollRange = false;

                for (ViewParent parent = getParent(); parent != null && parent instanceof ViewGroup; parent = parent.getParent()) {
                    if (parent instanceof NestedScrollingParent2 && findSuperClass(parent, "CoordinatorLayout")) {
                        ViewGroup viewGroup = (ViewGroup) parent;
                        viewGroup.getLocationInWindow(mWindowOffsets);
                        int height = mWindowOffsets[1] + viewGroup.getHeight();
                        getLocationInWindow(mWindowOffsets);
                        mInitialTopOffsetOfScreen = mWindowOffsets[1];
                        mRemainNestedScrollRange = getHeight() - (height - mInitialTopOffsetOfScreen);
                        if (mRemainNestedScrollRange < 0) {
                            mRemainNestedScrollRange = 0;
                        }
                        mNestedScrollRange = mRemainNestedScrollRange;
                        mHasNestedScrollRange = true;
                    }
                }

                if (!mHasNestedScrollRange) {
                    mInitialTopOffsetOfScreen = 0;
                    mRemainNestedScrollRange = 0;
                    mNestedScrollRange = 0;
                }
            }

            if (mIndexTipEnabled && mIndexTip != null) {
                mIndexTip.setLayout(0, 0, r, b, getPaddingLeft(), getPaddingRight());
            }
        }
    }

    @Override
    public void requestLayout() {
        if (mInterceptRequestLayoutDepth == 0 && !mLayoutSuppressed) {
            super.requestLayout();
        } else {
            mLayoutWasDefered = true;
        }
    }

    void markItemDecorInsetsDirty() {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = mChildHelper.getUnfilteredChildAt(i);
            ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
        }
        mRecycler.markItemDecorInsetsDirty();
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).onDrawOver(c, this, mState);
        }
        boolean needsInvalidate = false;
        if (mLeftGlow != null && !mLeftGlow.isFinished()) {
            final int restore = c.save();
            final int padding = mClipToPadding ? getPaddingBottom() : 0;
            c.rotate(270);
            c.translate(-getHeight() + padding, 0);
            needsInvalidate = mLeftGlow != null && mLeftGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (mTopGlow != null && !mTopGlow.isFinished()) {
            final int restore = c.save();
            if (mClipToPadding) {
                c.translate(getPaddingLeft(), getPaddingTop());
            }
            needsInvalidate |= mTopGlow != null && mTopGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (mRightGlow != null && !mRightGlow.isFinished()) {
            final int restore = c.save();
            final int width = getWidth();
            final int padding = mClipToPadding ? getPaddingTop() : 0;
            c.rotate(90);
            c.translate(padding, -width);
            needsInvalidate |= mRightGlow != null && mRightGlow.draw(c);
            c.restoreToCount(restore);
        }
        if (mBottomGlow != null && !mBottomGlow.isFinished()) {
            final int restore = c.save();
            c.rotate(180);
            if (mClipToPadding) {
                c.translate(-getWidth() + getPaddingRight(), -getHeight() + getPaddingBottom());
            } else {
                c.translate(-getWidth(), -getHeight());
            }
            needsInvalidate |= mBottomGlow != null && mBottomGlow.draw(c);
            c.restoreToCount(restore);
        }

        if (!needsInvalidate && mItemAnimator != null && mItemDecorations.size() > 0 && mItemAnimator.isRunning()) {
            needsInvalidate = true;
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        if (mEnableGoToTop) {
            drawGoToTop();
        }

        if (mIsPenDragBlockEnabled && !mIsLongPressMultiSelection && mLayout != null) {
            if (mPenDragBlockLeft != 0 || mPenDragBlockTop != 0) {
                int childTop = 0;

                int trackedChildPos = mPenTrackedChildPosition;
                if (trackedChildPos >= findFirstVisibleItemPosition() && trackedChildPos <= findLastVisibleItemPosition()) {
                    mPenTrackedChild = mLayout.findViewByPosition(trackedChildPos);
                    if (mPenTrackedChild != null) {
                        childTop = mPenTrackedChild.getTop();
                    }
                    mPenDragStartY = childTop + mPenDistanceFromTrackedChildTop;
                }

                mPenDragBlockTop = mPenDragStartY < mPenDragEndY ? mPenDragStartY : mPenDragEndY;
                mPenDragBlockBottom = mPenDragEndY > mPenDragStartY ? mPenDragEndY : mPenDragStartY;
                mPenDragBlockRect.set(mPenDragBlockLeft, mPenDragBlockTop, mPenDragBlockRight, mPenDragBlockBottom);
                mPenDragBlockImage.setBounds(mPenDragBlockRect);
                mPenDragBlockImage.draw(c);
            }
        }
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).onDraw(c, this, mState);
        }

        if (mStatisticalCount <= 5 && mIsNeedCheckLatency) {
            if (mStatisticalCount != 0) {
                float currentTimeMillis = (float) (System.currentTimeMillis() - mPrevLatencyTime);
                if (currentTimeMillis > FRAME_LATENCY_LIMIT) {
                    currentTimeMillis = 16.66f;
                }
                mApproxLatency += currentTimeMillis;
            }
            mPrevLatencyTime = System.currentTimeMillis();
            if (mStatisticalCount == 5) {
                mApproxLatency /= 5.0f;
            }
            mStatisticalCount += 1;
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && mLayout.checkLayoutParams((LayoutParams) p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager" + exceptionLabel());
        }
        return mLayout.generateDefaultLayoutParams();
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        if (mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager" + exceptionLabel());
        }
        return mLayout.generateLayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (mLayout == null) {
            throw new IllegalStateException("RecyclerView has no LayoutManager" + exceptionLabel());
        }
        return mLayout.generateLayoutParams(p);
    }

    public boolean isAnimating() {
        return mItemAnimator != null && mItemAnimator.isRunning();
    }

    void saveOldPositions() {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (DEBUG && holder.mPosition == -1 && !holder.isRemoved()) {
                throw new IllegalStateException("view holder cannot have position -1 unless iy is removed" + exceptionLabel());
            }
            if (!holder.shouldIgnore()) {
                holder.saveOldPosition();
            }
        }
    }

    void clearOldPositions() {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (!holder.shouldIgnore()) {
                holder.clearOldPosition();
            }
        }
        mRecycler.clearOldPositions();
    }

    void offsetPositionRecordsForMove(int from, int to) {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        final int start, end, inBetweenOffset;
        if (from < to) {
            start = from;
            end = to;
            inBetweenOffset = -1;
        } else {
            start = to;
            end = from;
            inBetweenOffset = 1;
        }

        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder == null || holder.mPosition < start || holder.mPosition > end) {
                continue;
            }
            if (DEBUG) {
                Log.d(TAG, "offsetPositionRecordsForMove attached child " + i + " holder " + holder);
            }
            if (holder.mPosition == from) {
                holder.offsetPosition(to - from, false);
            } else {
                holder.offsetPosition(inBetweenOffset, false);
            }

            mState.mStructureChanged = true;
        }
        mRecycler.offsetPositionRecordsForMove(from, to);
        requestLayout();
    }

    void offsetPositionRecordsForInsert(int positionStart, int itemCount) {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.shouldIgnore() && holder.mPosition >= positionStart) {
                if (DEBUG) {
                    Log.d(TAG, "offsetPositionRecordsForInsert attached child " + i + " holder " + holder + " now at position " + (holder.mPosition + itemCount));
                }
                holder.offsetPosition(itemCount, false);
                mState.mStructureChanged = true;
            }
        }
        mRecycler.offsetPositionRecordsForInsert(positionStart, itemCount);
        requestLayout();
    }

    void offsetPositionRecordsForRemove(int positionStart, int itemCount, boolean applyToPreLayout) {
        final int positionEnd = positionStart + itemCount;
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.shouldIgnore()) {
                if (holder.mPosition >= positionEnd) {
                    if (DEBUG) {
                        Log.d(TAG, "offsetPositionRecordsForRemove attached child " + i + " holder " + holder + " now at position " + (holder.mPosition - itemCount));
                    }
                    holder.offsetPosition(-itemCount, applyToPreLayout);
                    mState.mStructureChanged = true;
                } else if (holder.mPosition >= positionStart) {
                    if (DEBUG) {
                        Log.d(TAG, "offsetPositionRecordsForRemove attached child " + i + " holder " + holder + " now REMOVED");
                    }
                    holder.flagRemovedAndOffsetPosition(positionStart - 1, -itemCount, applyToPreLayout);
                    mState.mStructureChanged = true;
                }
            }
        }
        mRecycler.offsetPositionRecordsForRemove(positionStart, itemCount, applyToPreLayout);
        requestLayout();
    }

    void viewRangeUpdate(int positionStart, int itemCount, Object payload) {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        final int positionEnd = positionStart + itemCount;

        for (int i = 0; i < childCount; i++) {
            final View child = mChildHelper.getUnfilteredChildAt(i);
            final ViewHolder holder = getChildViewHolderInt(child);
            if (holder == null || holder.shouldIgnore()) {
                continue;
            }
            if (holder.mPosition >= positionStart && holder.mPosition < positionEnd) {
                holder.addFlags(ViewHolder.FLAG_UPDATE);
                holder.addChangePayload(payload);
                ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
            }
        }
        mRecycler.viewRangeUpdate(positionStart, itemCount);
    }

    boolean canReuseUpdatedViewHolder(ViewHolder viewHolder) {
        return mItemAnimator == null || mItemAnimator.canReuseUpdatedViewHolder(viewHolder, viewHolder.getUnmodifiedPayloads());
    }

    void processDataSetCompletelyChanged(boolean dispatchItemsChanged) {
        mDispatchItemsChangedEvent |= dispatchItemsChanged;
        mDataSetHasChangedAfterLayout = true;
        markKnownViewsInvalid();
    }

    void markKnownViewsInvalid() {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.shouldIgnore()) {
                holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
            }
        }
        markItemDecorInsetsDirty();
        mRecycler.markKnownViewsInvalid();
    }

    public void invalidateItemDecorations() {
        if (mItemDecorations.size() == 0) {
            return;
        }
        if (mLayout != null) {
            mLayout.assertNotInLayoutOrScroll("Cannot invalidate item decorations during a scroll or layout");
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public boolean getPreserveFocusAfterLayout() {
        return mPreserveFocusAfterLayout;
    }

    public void setPreserveFocusAfterLayout(boolean preserveFocusAfterLayout) {
        mPreserveFocusAfterLayout = preserveFocusAfterLayout;
    }

    public ViewHolder getChildViewHolder(@NonNull View child) {
        final ViewParent parent = child.getParent();
        if (parent != null && parent != this) {
            throw new IllegalArgumentException("View " + child + " is not a direct child of " + this);
        }
        return getChildViewHolderInt(child);
    }

    @Nullable
    public View findContainingItemView(@NonNull View view) {
        ViewParent parent = view.getParent();
        while (parent != null && parent != this && parent instanceof View) {
            view = (View) parent;
            parent = view.getParent();
        }
        return parent == this ? view : null;
    }

    @Nullable
    public ViewHolder findContainingViewHolder(@NonNull View view) {
        View itemView = findContainingItemView(view);
        return itemView == null ? null : getChildViewHolder(itemView);
    }


    public static ViewHolder getChildViewHolderInt(View child) {
        if (child == null) {
            return null;
        }
        return ((LayoutParams) child.getLayoutParams()).mViewHolder;
    }

    @Deprecated
    public int getChildPosition(@NonNull View child) {
        return getChildAdapterPosition(child);
    }

    public int getChildAdapterPosition(@NonNull View child) {
        final ViewHolder holder = getChildViewHolderInt(child);
        return holder != null ? holder.getAbsoluteAdapterPosition() : NO_POSITION;
    }

    public int getChildLayoutPosition(@NonNull View child) {
        final ViewHolder holder = getChildViewHolderInt(child);
        return holder != null ? holder.getLayoutPosition() : NO_POSITION;
    }

    public long getChildItemId(@NonNull View child) {
        if (mAdapter == null || !mAdapter.hasStableIds()) {
            return NO_ID;
        }
        final ViewHolder holder = getChildViewHolderInt(child);
        return holder != null ? holder.getItemId() : NO_ID;
    }

    @Deprecated
    @Nullable
    public ViewHolder findViewHolderForPosition(int position) {
        return findViewHolderForPosition(position, false);
    }

    @Nullable
    public ViewHolder findViewHolderForLayoutPosition(int position) {
        return findViewHolderForPosition(position, false);
    }

    @Nullable
    public ViewHolder findViewHolderForAdapterPosition(int position) {
        if (mDataSetHasChangedAfterLayout) {
            return null;
        }
        final int childCount = mChildHelper.getUnfilteredChildCount();
        ViewHolder hidden = null;
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.isRemoved() && getAdapterPositionInRecyclerView(holder) == position) {
                if (mChildHelper.isHidden(holder.itemView)) {
                    hidden = holder;
                } else {
                    return holder;
                }
            }
        }
        return hidden;
    }

    @Nullable
    ViewHolder findViewHolderForPosition(int position, boolean checkNewPosition) {
        final int childCount = mChildHelper.getUnfilteredChildCount();
        ViewHolder hidden = null;
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.isRemoved()) {
                if (checkNewPosition) {
                    if (holder.mPosition != position) {
                        continue;
                    }
                } else if (holder.getLayoutPosition() != position) {
                    continue;
                }
                if (mChildHelper.isHidden(holder.itemView)) {
                    hidden = holder;
                } else {
                    return holder;
                }
            }
        }
        return hidden;
    }

    public ViewHolder findViewHolderForItemId(long id) {
        if (mAdapter == null || !mAdapter.hasStableIds()) {
            return null;
        }
        final int childCount = mChildHelper.getUnfilteredChildCount();
        ViewHolder hidden = null;
        for (int i = 0; i < childCount; i++) {
            final ViewHolder holder = getChildViewHolderInt(mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && !holder.isRemoved() && holder.getItemId() == id) {
                if (mChildHelper.isHidden(holder.itemView)) {
                    hidden = holder;
                } else {
                    return holder;
                }
            }
        }
        return hidden;
    }

    @Nullable
    public View findChildViewUnder(float x, float y) {
        final int count = mChildHelper.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = mChildHelper.getChildAt(i);
            final float translationX = child.getTranslationX();
            final float translationY = child.getTranslationY();
            if (x >= child.getLeft() + translationX && x <= child.getRight() + translationX && y >= child.getTop() + translationY && y <= child.getBottom() + translationY) {
                return child;
            }
        }
        return null;
    }

    @Override
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return super.drawChild(canvas, child, drawingTime);
    }

    public void offsetChildrenVertical(@Px int dy) {
        final int childCount = mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            mChildHelper.getChildAt(i).offsetTopAndBottom(dy);
        }
    }

    public void onChildAttachedToWindow(@NonNull View child) {
    }

    public void onChildDetachedFromWindow(@NonNull View child) {
    }

    public void offsetChildrenHorizontal(@Px int dx) {
        final int childCount = mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            mChildHelper.getChildAt(i).offsetLeftAndRight(dx);
        }
    }

    public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect outBounds) {
        getDecoratedBoundsWithMarginsInt(view, outBounds);
    }

    static void getDecoratedBoundsWithMarginsInt(View view, Rect outBounds) {
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final Rect insets = lp.mDecorInsets;
        outBounds.set(view.getLeft() - insets.left - lp.leftMargin, view.getTop() - insets.top - lp.topMargin, view.getRight() + insets.right + lp.rightMargin, view.getBottom() + insets.bottom + lp.bottomMargin);
    }

    Rect getItemDecorInsetsForChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.mInsetsDirty) {
            return lp.mDecorInsets;
        }

        if (mState.isPreLayout() && (lp.isItemChanged() || lp.isViewInvalid())) {
            return lp.mDecorInsets;
        }
        final Rect insets = lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        final int decorCount = mItemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            mTempRect.set(0, 0, 0, 0);
            mItemDecorations.get(i).getItemOffsets(mTempRect, child, this, mState);
            insets.left += mTempRect.left;
            insets.top += mTempRect.top;
            insets.right += mTempRect.right;
            insets.bottom += mTempRect.bottom;
        }
        lp.mInsetsDirty = false;
        return insets;
    }

    public void onScrolled(@Px int dx, @Px int dy) {
    }

    void dispatchOnScrolled(int hresult, int vresult) {
        mDispatchScrollCounter++;
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        onScrollChanged(scrollX, scrollY, scrollX - hresult, scrollY - vresult);

        onScrolled(hresult, vresult);

        if (mFastScroller != null && mAdapter != null) {
            if (hresult != 0 || vresult != 0) {
                mFastScroller.onScroll(findFirstVisibleItemPosition(), getChildCount(), mAdapter.getItemCount());
            }
        }

        if (mIndexTipEnabled && mIndexTip != null) {
            if (mScrollState != 0) {
                mIndexTip.show(mScrollState, vresult);
            }
            mIndexTip.invalidate();
        }

        if (mScrollListener != null) {
            mScrollListener.onScrolled(this, hresult, vresult);
        }
        if (mScrollListeners != null) {
            for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                mScrollListeners.get(i).onScrolled(this, hresult, vresult);
            }
        }
        mDispatchScrollCounter--;
    }

    public void onScrollStateChanged(int state) {
    }

    void dispatchOnScrollStateChanged(int state) {
        if (mLayout != null) {
            mLayout.onScrollStateChanged(state);
        }

        onScrollStateChanged(state);

        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(this, state);
        }
        if (mScrollListeners != null) {
            for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                mScrollListeners.get(i).onScrollStateChanged(this, state);
            }
        }
    }

    public boolean hasPendingAdapterUpdates() {
        return !mFirstLayoutComplete || mDataSetHasChangedAfterLayout || mAdapterHelper.hasPendingUpdates();
    }


    class ViewFlinger implements Runnable {
        private int mLastFlingX;
        private int mLastFlingY;
        OverScroller mOverScroller;
        Interpolator mInterpolator = sQuinticInterpolator;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;

        ViewFlinger() {
            mOverScroller = new OverScroller(getContext(), sQuinticInterpolator);
        }

        @Override
        public void run() {
            if (mLayout == null) {
                stop();
                return;
            }

            mReSchedulePostAnimationCallback = false;
            mEatRunOnAnimationRequest = true;

            consumePendingUpdateOperations();

            final OverScroller scroller = mOverScroller;
            if (scroller.computeScrollOffset()) {
                final int x = scroller.getCurrX();
                final int y = scroller.getCurrY();
                int unconsumedX = x - mLastFlingX;
                int unconsumedY = y - mLastFlingY;
                mLastFlingX = x;
                mLastFlingY = y;
                int consumedX = 0;
                int consumedY = 0;

                mReusableIntPair[0] = 0;
                mReusableIntPair[1] = 0;
                if (dispatchNestedPreScroll(unconsumedX, unconsumedY, mReusableIntPair, null, TYPE_NON_TOUCH)) {
                    unconsumedX -= mReusableIntPair[0];
                    unconsumedY -= mReusableIntPair[1];
                    adjustNestedScrollRangeBy(mReusableIntPair[1]);
                } else {
                    adjustNestedScrollRangeBy(unconsumedY);
                }

                if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
                    considerReleasingGlowsOnScroll(unconsumedX, unconsumedY);
                }

                if (mAdapter != null) {
                    mReusableIntPair[0] = 0;
                    mReusableIntPair[1] = 0;
                    scrollStep(unconsumedX, unconsumedY, mReusableIntPair);
                    consumedX = mReusableIntPair[0];
                    consumedY = mReusableIntPair[1];
                    unconsumedX -= consumedX;
                    unconsumedY -= consumedY;

                    SmoothScroller smoothScroller = mLayout.mSmoothScroller;
                    if (smoothScroller != null && !smoothScroller.isPendingInitialRun() && smoothScroller.isRunning()) {
                        final int adapterSize = mState.getItemCount();
                        if (adapterSize == 0) {
                            smoothScroller.stop();
                        } else if (smoothScroller.getTargetPosition() >= adapterSize) {
                            smoothScroller.setTargetPosition(adapterSize - 1);
                            smoothScroller.onAnimation(consumedX, consumedY);
                        } else {
                            smoothScroller.onAnimation(consumedX, consumedY);
                        }
                    }
                }

                if (!mItemDecorations.isEmpty()) {
                    invalidate();
                }

                mReusableIntPair[0] = 0;
                mReusableIntPair[1] = 0;

                if (seslDispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, null, TYPE_NON_TOUCH, mReusableIntPair)) {
                    mScrollOffset[0] = 0;
                    mScrollOffset[1] = 0;
                }
                if (mScrollOffset[0] < 0 || mScrollOffset[1] < 0) {
                    mScrollOffset[0] = 0;
                    mScrollOffset[1] = 0;
                }

                unconsumedX -= mReusableIntPair[0];
                unconsumedY -= mReusableIntPair[1];

                if (consumedX != 0 || consumedY != 0) {
                    dispatchOnScrolled(consumedX, consumedY);
                }

                if (!awakenScrollBars()) {
                    invalidate();
                }

                boolean scrollerFinishedX = scroller.getCurrX() == scroller.getFinalX();
                boolean scrollerFinishedY = scroller.getCurrY() == scroller.getFinalY();
                final boolean doneScrolling = scroller.isFinished() || ((scrollerFinishedX || unconsumedX != 0) && (scrollerFinishedY || unconsumedY != 0));

                SmoothScroller smoothScroller = mLayout.mSmoothScroller;
                boolean smoothScrollerPending = smoothScroller != null && smoothScroller.isPendingInitialRun();

                if (!smoothScrollerPending && doneScrolling) {
                    if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
                        final int vel = (int) scroller.getCurrVelocity();
                        int velX = unconsumedX < 0 ? -vel : unconsumedX > 0 ? vel : 0;
                        int velY = unconsumedY < 0 ? -vel : unconsumedY > 0 ? vel : 0;
                        absorbGlows(velX, velY);
                    }

                    if (ALLOW_THREAD_GAP_WORK) {
                        mPrefetchRegistry.clearPrefetchPositions();
                    }
                } else {
                    postOnAnimation();
                    if (mGapWorker != null) {
                        mGapWorker.postFromTraversal(RecyclerView.this, consumedX, consumedY);
                    }
                }
            }

            SmoothScroller smoothScroller = mLayout.mSmoothScroller;
            if (smoothScroller != null && smoothScroller.isPendingInitialRun()) {
                smoothScroller.onAnimation(0, 0);
            }

            mEatRunOnAnimationRequest = false;
            if (mReSchedulePostAnimationCallback) {
                internalPostOnAnimation();
            } else {
                setScrollState(SCROLL_STATE_IDLE);
                stopNestedScroll(TYPE_NON_TOUCH);
            }
        }

        void postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true;
            } else {
                internalPostOnAnimation();
            }
        }

        private void internalPostOnAnimation() {
            removeCallbacks(this);
            ViewCompat.postOnAnimation(RecyclerView.this, this);
        }

        public void fling(int velocityX, int velocityY) {
            setScrollState(SCROLL_STATE_SETTLING);
            mLastFlingX = mLastFlingY = 0;
            if (mInterpolator != sQuinticInterpolator) {
                mInterpolator = sQuinticInterpolator;
                mOverScroller = new OverScroller(getContext(), sQuinticInterpolator);
            }
            SeslOverScrollerReflector.fling(mOverScroller, 0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, mIsSkipMoveEvent, mApproxLatency);
            postOnAnimation();
        }

        public void smoothScrollBy(int dx, int dy, int duration, @Nullable Interpolator interpolator) {
            if (duration == UNDEFINED_DURATION) {
                duration = computeScrollDuration(dx, dy, 0, 0);
            }
            if (interpolator == null) {
                interpolator = sQuinticInterpolator;
            }

            startNestedScroll(dx != 0 ? ViewCompat.SCROLL_AXIS_VERTICAL : ViewCompat.SCROLL_AXIS_HORIZONTAL, TYPE_NON_TOUCH);

            if (!dispatchNestedPreScroll(dx, dy, null, null, TYPE_NON_TOUCH)) {
                if (mInterpolator != interpolator) {
                    mInterpolator = interpolator;
                    mOverScroller = new OverScroller(getContext(), interpolator);
                }

                mLastFlingX = mLastFlingY = 0;

                setScrollState(SCROLL_STATE_SETTLING);
                mOverScroller.startScroll(0, 0, dx, dy, duration);

                if (Build.VERSION.SDK_INT < 23) {
                    mOverScroller.computeScrollOffset();
                }

                postOnAnimation();
            }

            adjustNestedScrollRangeBy(dy);
        }

        // kang
        private float distanceInfluenceForSnapDuration(float f) {
            return (float) Math.sin((double) ((f - 0.5f) * 0.47123894f));
        }

        private int computeScrollDuration(int i, int i2, int i3, int i4) {
            int i5;
            int abs = Math.abs(i);
            int abs2 = Math.abs(i2);
            boolean z = abs > abs2;
            int sqrt = (int) Math.sqrt((double) ((i3 * i3) + (i4 * i4)));
            int sqrt2 = (int) Math.sqrt((double) ((i * i) + (i2 * i2)));
            int width = z ? getWidth() : getHeight();
            int i6 = width / 2;
            float f = (float) width;
            float f2 = (float) i6;
            float distanceInfluenceForSnapDuration = f2 + (distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) sqrt2)) / f)) * f2);
            if (sqrt > 0) {
                i5 = Math.round(Math.abs(distanceInfluenceForSnapDuration / ((float) sqrt)) * 1000.0f) * 4;
            } else {
                if (!z) {
                    abs = abs2;
                }
                i5 = (int) (((((float) abs) / f) + 1.0f) * 300.0f);
            }
            return Math.min(i5, MAX_SCROLL_DURATION);
        }
        // kang

        public void stop() {
            removeCallbacks(this);
            mOverScroller.abortAnimation();
        }

    }

    void repositionShadowingViews() {
        int count = mChildHelper.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mChildHelper.getChildAt(i);
            ViewHolder holder = getChildViewHolder(view);
            if (holder != null && holder.mShadowingHolder != null) {
                View shadowingView = holder.mShadowingHolder.itemView;
                int left = view.getLeft();
                int top = view.getTop();
                if (left != shadowingView.getLeft() || top != shadowingView.getTop()) {
                    shadowingView.layout(left, top, left + shadowingView.getWidth(), top + shadowingView.getHeight());
                }
            }
        }
    }

    private class RecyclerViewDataObserver extends AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        @Override
        public void onChanged() {
            assertNotInLayoutOrScroll(null);
            mState.mStructureChanged = true;

            processDataSetCompletelyChanged(true);
            if (!mAdapterHelper.hasPendingUpdates()) {
                requestLayout();
            }
            if (mFastScroller != null) {
                mFastScroller.onSectionsChanged();
            }
            if (mIndexTip != null) {
                mIndexTip.updateSections();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeChanged(positionStart, itemCount, payload)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeInserted(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeRemoved(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeMoved(fromPosition, toPosition, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        void triggerUpdateProcessor() {
            if (POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
            } else {
                mAdapterUpdateDuringMeasure = true;
                requestLayout();
            }
        }

        @Override
        public void onStateRestorationPolicyChanged() {
            if (mPendingSavedState == null) {
                return;
            }
            Adapter<?> adapter = mAdapter;
            if (adapter != null && adapter.canRestoreState()) {
                requestLayout();
            }
        }
    }

    public static class EdgeEffectFactory {
        @Retention(RetentionPolicy.SOURCE)
        @IntDef({DIRECTION_LEFT, DIRECTION_TOP, DIRECTION_RIGHT, DIRECTION_BOTTOM})
        public @interface EdgeDirection {
        }
        public static final int DIRECTION_LEFT = 0;
        public static final int DIRECTION_TOP = 1;
        public static final int DIRECTION_RIGHT = 2;
        public static final int DIRECTION_BOTTOM = 3;

        protected @NonNull EdgeEffect createEdgeEffect(@NonNull RecyclerView view, @EdgeDirection int direction) {
            return new EdgeEffect(view.getContext());
        }
    }

    static class StretchEdgeEffectFactory extends EdgeEffectFactory {
        @NonNull
        @Override
        protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
            return EdgeEffectSupport.create(view, null);
        }
    }

    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;
        SparseArray<ScrapData> mScrap = new SparseArray<>();
        private int mAttachCount = 0;

        public void clear() {
            for (int i = 0; i < mScrap.size(); i++) {
                ScrapData data = mScrap.valueAt(i);
                if (data != null) {
                    data.mScrapHeap.clear();
                } else {
                    Log.e(TAG, "clear() wasn't executed because RecycledViewPool.mScrap was invalid");
                }
            }
        }

        public void setMaxRecycledViews(int viewType, int max) {
            ScrapData scrapData = getScrapDataForType(viewType);
            scrapData.mMaxScrap = max;
            final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;
            while (scrapHeap.size() > max) {
                scrapHeap.remove(scrapHeap.size() - 1);
            }
        }

        public int getRecycledViewCount(int viewType) {
            return getScrapDataForType(viewType).mScrapHeap.size();
        }

        @Nullable
        public ViewHolder getRecycledView(int viewType) {
            final ScrapData scrapData = mScrap.get(viewType);
            if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
                final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;
                for (int i = scrapHeap.size() - 1; i >= 0; i--) {
                    if (scrapHeap.get(i) != null) {
                        if (!scrapHeap.get(i).isAttachedToTransitionOverlay()) {
                            return scrapHeap.remove(i);
                        }
                    } else {
                        Log.e(TAG, "ViewHolder object null when getRecycledView is in progress. pos= " + i + " size=" + scrapHeap.size() + " max= " + scrapData.mMaxScrap + " holder= " + size() + " scrapHeap= " + scrapHeap);
                    }
                }
            }
            return null;
        }

        int size() {
            int count = 0;
            for (int i = 0; i < mScrap.size(); i++) {
                ArrayList<ViewHolder> viewHolders = mScrap.valueAt(i).mScrapHeap;
                if (viewHolders != null) {
                    count += viewHolders.size();
                }
            }
            return count;
        }

        public void putRecycledView(ViewHolder scrap) {
            final int viewType = scrap.getItemViewType();
            final ArrayList<ViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
            if (mScrap.get(viewType).mMaxScrap <= scrapHeap.size()) {
                return;
            }
            if (DEBUG && scrapHeap.contains(scrap)) {
                throw new IllegalArgumentException("this scrap item already exists");
            }
            scrap.resetInternal();
            scrapHeap.add(scrap);
        }

        long runningAverage(long oldAverage, long newValue) {
            if (oldAverage == 0) {
                return newValue;
            }
            return (oldAverage / 4 * 3) + (newValue / 4);
        }

        void factorInCreateTime(int viewType, long createTimeNs) {
            ScrapData scrapData = getScrapDataForType(viewType);
            scrapData.mCreateRunningAverageNs = runningAverage(scrapData.mCreateRunningAverageNs, createTimeNs);
        }

        void factorInBindTime(int viewType, long bindTimeNs) {
            ScrapData scrapData = getScrapDataForType(viewType);
            scrapData.mBindRunningAverageNs = runningAverage(scrapData.mBindRunningAverageNs, bindTimeNs);
        }

        boolean willCreateInTime(int viewType, long approxCurrentNs, long deadlineNs) {
            long expectedDurationNs = getScrapDataForType(viewType).mCreateRunningAverageNs;
            return expectedDurationNs == 0 || (approxCurrentNs + expectedDurationNs < deadlineNs);
        }

        boolean willBindInTime(int viewType, long approxCurrentNs, long deadlineNs) {
            long expectedDurationNs = getScrapDataForType(viewType).mBindRunningAverageNs;
            return expectedDurationNs == 0 || (approxCurrentNs + expectedDurationNs < deadlineNs);
        }

        void attach() {
            mAttachCount++;
        }

        void detach() {
            mAttachCount--;
        }

        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter, boolean compatibleWithPrevious) {
            if (oldAdapter != null) {
                detach();
            }
            if (!compatibleWithPrevious && mAttachCount == 0) {
                clear();
            }
            if (newAdapter != null) {
                attach();
            }
        }

        private ScrapData getScrapDataForType(int viewType) {
            ScrapData scrapData = mScrap.get(viewType);
            if (scrapData == null) {
                scrapData = new ScrapData();
                mScrap.put(viewType, scrapData);
            }
            return scrapData;
        }


        static class ScrapData {
            final ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
            int mMaxScrap = DEFAULT_MAX_SCRAP;
            long mCreateRunningAverageNs = 0;
            long mBindRunningAverageNs = 0;
        }
    }

    @Nullable
    static RecyclerView findNestedRecyclerView(@NonNull View view) {
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        if (view instanceof RecyclerView) {
            return (RecyclerView) view;
        }
        final ViewGroup parent = (ViewGroup) view;
        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView descendant = findNestedRecyclerView(child);
            if (descendant != null) {
                return descendant;
            }
        }
        return null;
    }

    static void clearNestedRecyclerViewIfNotNested(@NonNull ViewHolder holder) {
        if (holder.mNestedRecyclerView != null) {
            View item = holder.mNestedRecyclerView.get();
            while (item != null) {
                if (item == holder.itemView) {
                    return;
                }

                ViewParent parent = item.getParent();
                if (parent instanceof View) {
                    item = (View) parent;
                } else {
                    item = null;
                }
            }
            holder.mNestedRecyclerView = null;
        }
    }

    public long getNanoTime() {
        if (ALLOW_THREAD_GAP_WORK) {
            return System.nanoTime();
        } else {
            return 0;
        }
    }

    public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;
        final ArrayList<ViewHolder> mCachedViews = new ArrayList<ViewHolder>();
        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);
        private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
        int mViewCacheMax = DEFAULT_CACHE_SIZE;
        RecycledViewPool mRecyclerPool;
        private ViewCacheExtension mViewCacheExtension;
        static final int DEFAULT_CACHE_SIZE = 2;

        public void clear() {
            mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        public void setViewCacheSize(int viewCount) {
            mRequestedCacheMax = viewCount;
            updateViewCacheSize();
        }

        public void updateViewCacheSize() {
            int extraCache = mLayout != null ? mLayout.mPrefetchMaxCountObserved : 0;
            mViewCacheMax = mRequestedCacheMax + extraCache;

            for (int i = mCachedViews.size() - 1; i >= 0 && mCachedViews.size() > mViewCacheMax; i--) {
                recycleCachedViewAt(i);
            }
        }

        @NonNull
        public List<ViewHolder> getScrapList() {
            return mUnmodifiableAttachedScrap;
        }

        boolean validateViewHolderForOffsetPosition(ViewHolder holder) {
            if (holder.isRemoved()) {
                if (DEBUG && !mState.isPreLayout()) {
                    throw new IllegalStateException("should not receive a removed view unless it is pre layout" + exceptionLabel());
                }
                return mState.isPreLayout();
            }
            if (holder.mPosition < 0 || holder.mPosition >= mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder adapter position" + holder + exceptionLabel());
            }
            if (!mState.isPreLayout()) {
                final int type = mAdapter.getItemViewType(holder.mPosition);
                if (type != holder.getItemViewType()) {
                    return false;
                }
            }
            if (mAdapter.hasStableIds()) {
                return holder.getItemId() == mAdapter.getItemId(holder.mPosition);
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        private boolean tryBindViewHolderByDeadline(@NonNull ViewHolder holder, int offsetPosition, int position, long deadlineNs) {
            holder.mBindingAdapter = null;
            holder.mOwnerRecyclerView = RecyclerView.this;
            final int viewType = holder.getItemViewType();
            long startBindNs = getNanoTime();
            if (deadlineNs != FOREVER_NS && !mRecyclerPool.willBindInTime(viewType, startBindNs, deadlineNs)) {
                return false;
            }
            mAdapter.bindViewHolder(holder, offsetPosition);
            long endBindNs = getNanoTime();
            mRecyclerPool.factorInBindTime(holder.getItemViewType(), endBindNs - startBindNs);
            attachAccessibilityDelegateOnBind(holder);
            if (mState.isPreLayout()) {
                holder.mPreLayoutPosition = position;
            }
            return true;
        }

        public void bindViewToPosition(@NonNull View view, int position) {
            ViewHolder holder = getChildViewHolderInt(view);
            if (holder == null) {
                throw new IllegalArgumentException("The view does not have a ViewHolder. You cannot pass arbitrary views to this method, they should be created by the Adapter" + exceptionLabel());
            }
            final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item " + "position " + position + "(offset:" + offsetPosition + ")." + "state:" + mState.getItemCount() + exceptionLabel());
            }
            tryBindViewHolderByDeadline(holder, offsetPosition, position, FOREVER_NS);

            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (!checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else {
                rvLayoutParams = (LayoutParams) lp;
            }

            rvLayoutParams.mInsetsDirty = true;
            rvLayoutParams.mViewHolder = holder;
            rvLayoutParams.mPendingInvalidate = holder.itemView.getParent() == null;
        }

        public int convertPreLayoutPositionToPostLayout(int position) {
            if (position < 0 || position >= mState.getItemCount()) {
                throw new IndexOutOfBoundsException("invalid position " + position + ". State item count is " + mState.getItemCount() + exceptionLabel());
            }
            if (!mState.isPreLayout()) {
                return position;
            }
            return mAdapterHelper.findPositionOffset(position);
        }

        @NonNull
        public View getViewForPosition(int position) {
            return getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            return tryGetViewHolderForPositionByDeadline(position, dryRun, FOREVER_NS).itemView;
        }

        @Nullable
        public ViewHolder tryGetViewHolderForPositionByDeadline(int position, boolean dryRun, long deadlineNs) {
            if (position < 0 || position >= mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + position + "(" + position + "). Item count:" + mState.getItemCount() + exceptionLabel());
            }
            boolean fromScrapOrHiddenOrCache = false;
            ViewHolder holder = null;
            if (mState.isPreLayout()) {
                holder = getChangedScrapViewForPosition(position);
                fromScrapOrHiddenOrCache = holder != null;
            }
            if (holder == null) {
                holder = getScrapOrHiddenOrCachedHolderForPosition(position, dryRun);
                if (holder != null) {
                    if (!validateViewHolderForOffsetPosition(holder)) {
                        if (!dryRun) {
                            holder.addFlags(ViewHolder.FLAG_INVALID);
                            if (holder.isScrap()) {
                                removeDetachedView(holder.itemView, false);
                                holder.unScrap();
                            } else if (holder.wasReturnedFromScrap()) {
                                holder.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(holder);
                        }
                        holder = null;
                    } else {
                        fromScrapOrHiddenOrCache = true;
                    }
                }
            }
            if (holder == null) {
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + position + "(offset:" + offsetPosition + ").state:" + mState.getItemCount() + exceptionLabel());
                }

                final int type = mAdapter.getItemViewType(offsetPosition);
                if (mAdapter.hasStableIds()) {
                    holder = getScrapOrCachedViewForId(mAdapter.getItemId(offsetPosition), type, dryRun);
                    if (holder != null) {
                        holder.mPosition = offsetPosition;
                        fromScrapOrHiddenOrCache = true;
                    }
                }
                if (holder == null && mViewCacheExtension != null) {
                    final View view = mViewCacheExtension.getViewForPositionAndType(this, position, type);
                    if (view != null) {
                        holder = getChildViewHolder(view);
                        if (holder == null) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned a view which does not have a ViewHolder" + exceptionLabel());
                        } else if (holder.shouldIgnore()) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view." + exceptionLabel());
                        }
                    }
                }
                if (holder == null) {
                    if (DEBUG) {
                        Log.d(TAG, "tryGetViewHolderForPositionByDeadline(" + position + ") fetching from shared pool");
                    }
                    holder = getRecycledViewPool().getRecycledView(type);
                    if (holder != null) {
                        holder.resetInternal();
                        if (FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(holder);
                        }
                    }
                }
                if (holder == null) {
                    long start = getNanoTime();
                    if (deadlineNs != FOREVER_NS && !mRecyclerPool.willCreateInTime(type, start, deadlineNs)) {
                        return null;
                    }
                    holder = mAdapter.createViewHolder(RecyclerView.this, type);
                    if (ALLOW_THREAD_GAP_WORK) {
                        RecyclerView innerView = findNestedRecyclerView(holder.itemView);
                        if (innerView != null) {
                            holder.mNestedRecyclerView = new WeakReference<>(innerView);
                        }
                    }

                    long end = getNanoTime();
                    mRecyclerPool.factorInCreateTime(type, end - start);
                    if (DEBUG) {
                        Log.d(TAG, "tryGetViewHolderForPositionByDeadline created new ViewHolder");
                    }
                }
            }

            if (fromScrapOrHiddenOrCache && !mState.isPreLayout() && holder.hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST)) {
                holder.setFlags(0, ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                if (mState.mRunSimpleAnimations) {
                    int changeFlags = ItemAnimator.buildAdapterChangeFlagsForAnimations(holder);
                    changeFlags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                    final ItemHolderInfo info = mItemAnimator.recordPreLayoutInformation(mState, holder, changeFlags, holder.getUnmodifiedPayloads());
                    recordAnimationInfoIfBouncedHiddenView(holder, info);
                }
            }

            boolean bound = false;
            if (mState.isPreLayout() && holder.isBound()) {
                holder.mPreLayoutPosition = position;
            } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
                if (DEBUG && holder.isRemoved()) {
                    throw new IllegalStateException("Removed holder should be bound and it should come here only in pre-layout. Holder: " + holder + exceptionLabel());
                }
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                bound = tryBindViewHolderByDeadline(holder, offsetPosition, position, deadlineNs);
            }

            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (!checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else {
                rvLayoutParams = (LayoutParams) lp;
            }
            rvLayoutParams.mViewHolder = holder;
            rvLayoutParams.mPendingInvalidate = fromScrapOrHiddenOrCache && bound;
            return holder;
        }

        private void attachAccessibilityDelegateOnBind(ViewHolder holder) {
            if (isAccessibilityEnabled()) {
                final View itemView = holder.itemView;
                if (ViewCompat.getImportantForAccessibility(itemView) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    ViewCompat.setImportantForAccessibility(itemView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }
                if (mAccessibilityDelegate == null) {
                    setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(RecyclerView.this));
                    Log.d(RecyclerView.TAG, "attachAccessibilityDelegate: mAccessibilityDelegate is null, so re create");
                }
                AccessibilityDelegateCompat itemDelegate = mAccessibilityDelegate.getItemDelegate();
                if (itemDelegate instanceof RecyclerViewAccessibilityDelegate.ItemDelegate) {
                    ((RecyclerViewAccessibilityDelegate.ItemDelegate) itemDelegate).saveOriginalDelegate(itemView);
                }
                ViewCompat.setAccessibilityDelegate(itemView, itemDelegate);
            }
        }

        private void invalidateDisplayListInt(ViewHolder holder) {
            if (holder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt((ViewGroup) holder.itemView, false);
            }
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean invalidateThis) {
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                final View view = viewGroup.getChildAt(i);
                if (view instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) view, true);
                }
            }
            if (!invalidateThis) {
                return;
            }
            if (viewGroup.getVisibility() == View.INVISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
                viewGroup.setVisibility(View.INVISIBLE);
            } else {
                final int visibility = viewGroup.getVisibility();
                viewGroup.setVisibility(View.INVISIBLE);
                viewGroup.setVisibility(visibility);
            }
        }

        public void recycleView(@NonNull View view) {
            ViewHolder holder = getChildViewHolderInt(view);
            if (holder.isTmpDetached()) {
                removeDetachedView(view, false);
            }
            if (holder.isScrap()) {
                holder.unScrap();
            } else if (holder.wasReturnedFromScrap()) {
                holder.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(holder);
            if (mItemAnimator != null && !holder.isRecyclable()) {
                mItemAnimator.endAnimation(holder);
            }
        }

        void recycleAndClearCachedViews() {
            final int count = mCachedViews.size();
            for (int i = count - 1; i >= 0; i--) {
                recycleCachedViewAt(i);
            }
            mCachedViews.clear();
            if (ALLOW_THREAD_GAP_WORK) {
                mPrefetchRegistry.clearPrefetchPositions();
            }
        }

        void recycleCachedViewAt(int cachedViewIndex) {
            if (DEBUG) {
                Log.d(TAG, "Recycling cached view at index " + cachedViewIndex);
            }
            ViewHolder viewHolder = mCachedViews.get(cachedViewIndex);
            if (DEBUG) {
                Log.d(TAG, "CachedViewHolder to be recycled: " + viewHolder);
            }
            addViewHolderToRecycledViewPool(viewHolder, true);
            mCachedViews.remove(cachedViewIndex);
        }

        void recycleViewHolderInternal(ViewHolder holder) {
            if (holder.isScrap() || holder.itemView.getParent() != null) {
                throw new IllegalArgumentException("Scrapped or attached views may not be recycled. isScrap:" + holder.isScrap() + " isAttached:" + (holder.itemView.getParent() != null) + exceptionLabel());
            }

            if (holder.isTmpDetached()) {
                throw new IllegalArgumentException("Tmp detached view should be removed from RecyclerView before it can be recycled: " + holder + exceptionLabel());
            }

            if (holder.shouldIgnore()) {
                throw new IllegalArgumentException("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle." + exceptionLabel());
            }
            final boolean transientStatePreventsRecycling = holder.doesTransientStatePreventRecycling();
            @SuppressWarnings("unchecked") final boolean forceRecycle = mAdapter != null && transientStatePreventsRecycling && mAdapter.onFailedToRecycleView(holder);
            boolean cached = false;
            boolean recycled = false;
            if (DEBUG && mCachedViews.contains(holder)) {
                throw new IllegalArgumentException("cached view received recycle internal? " + holder + exceptionLabel());
            }
            if (forceRecycle || holder.isRecyclable()) {
                if (mViewCacheMax > 0 && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)) {
                    int cachedViewSize = mCachedViews.size();
                    if (cachedViewSize >= mViewCacheMax && cachedViewSize > 0) {
                        recycleCachedViewAt(0);
                        cachedViewSize--;
                    }

                    int targetCacheIndex = cachedViewSize;
                    if (ALLOW_THREAD_GAP_WORK && cachedViewSize > 0 && !mPrefetchRegistry.lastPrefetchIncludedPosition(holder.mPosition)) {
                        int cacheIndex = cachedViewSize - 1;
                        while (cacheIndex >= 0) {
                            int cachedPos = mCachedViews.get(cacheIndex).mPosition;
                            if (!mPrefetchRegistry.lastPrefetchIncludedPosition(cachedPos)) {
                                break;
                            }
                            cacheIndex--;
                        }
                        targetCacheIndex = cacheIndex + 1;
                    }
                    mCachedViews.add(targetCacheIndex, holder);
                    cached = true;
                }
                if (!cached) {
                    addViewHolderToRecycledViewPool(holder, true);
                    recycled = true;
                }
            } else {
                if (DEBUG) {
                    Log.d(TAG, "trying to recycle a non-recycleable holder. Hopefully, it will re-visit here. We are still removing it from animation lists" + exceptionLabel());
                }
            }
            mViewInfoStore.removeViewHolder(holder);
            if (!cached && !recycled && transientStatePreventsRecycling) {
                holder.mBindingAdapter = null;
                holder.mOwnerRecyclerView = null;
            }
        }

        public void addViewHolderToRecycledViewPool(@NonNull ViewHolder holder, boolean dispatchRecycled) {
            clearNestedRecyclerViewIfNotNested(holder);
            View itemView = holder.itemView;
            if (mAccessibilityDelegate != null) {
                AccessibilityDelegateCompat itemDelegate = mAccessibilityDelegate.getItemDelegate();
                AccessibilityDelegateCompat originalDelegate = null;
                if (itemDelegate instanceof RecyclerViewAccessibilityDelegate.ItemDelegate) {
                    originalDelegate = ((RecyclerViewAccessibilityDelegate.ItemDelegate) itemDelegate).getAndRemoveOriginalDelegateForItem(itemView);
                }
                ViewCompat.setAccessibilityDelegate(itemView, originalDelegate);
            }
            if (dispatchRecycled) {
                dispatchViewRecycled(holder);
            }
            holder.mBindingAdapter = null;
            holder.mOwnerRecyclerView = null;
            getRecycledViewPool().putRecycledView(holder);
        }

        void quickRecycleScrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            holder.mScrapContainer = null;
            holder.mInChangeScrap = false;
            holder.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(holder);
        }

        void scrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            if (holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID) || !holder.isUpdated() || canReuseUpdatedViewHolder(holder)) {
                if (holder.isInvalid() && !holder.isRemoved() && !mAdapter.hasStableIds()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool." + exceptionLabel());
                }
                holder.setScrapContainer(this, false);
                mAttachedScrap.add(holder);
            } else {
                if (mChangedScrap == null) {
                    mChangedScrap = new ArrayList<ViewHolder>();
                }
                holder.setScrapContainer(this, true);
                mChangedScrap.add(holder);
            }
        }

        void unscrapView(ViewHolder holder) {
            if (holder.mInChangeScrap) {
                mChangedScrap.remove(holder);
            } else {
                mAttachedScrap.remove(holder);
            }
            holder.mScrapContainer = null;
            holder.mInChangeScrap = false;
            holder.clearReturnedFromScrapFlag();
        }

        int getScrapCount() {
            return mAttachedScrap.size();
        }

        View getScrapViewAt(int index) {
            return mAttachedScrap.get(index).itemView;
        }

        void clearScrap() {
            mAttachedScrap.clear();
            if (mChangedScrap != null) {
                mChangedScrap.clear();
            }
        }

        ViewHolder getChangedScrapViewForPosition(int position) {
            final int changedScrapSize;
            if (mChangedScrap == null || (changedScrapSize = mChangedScrap.size()) == 0) {
                return null;
            }
            for (int i = 0; i < changedScrapSize; i++) {
                final ViewHolder holder = mChangedScrap.get(i);
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }
            if (mAdapter.hasStableIds()) {
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                if (offsetPosition > 0 && offsetPosition < mAdapter.getItemCount()) {
                    final long id = mAdapter.getItemId(offsetPosition);
                    for (int i = 0; i < changedScrapSize; i++) {
                        final ViewHolder holder = mChangedScrap.get(i);
                        if (!holder.wasReturnedFromScrap() && holder.getItemId() == id) {
                            holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                            return holder;
                        }
                    }
                }
            }
            return null;
        }

        ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int position, boolean dryRun) {
            final int scrapCount = mAttachedScrap.size();

            for (int i = 0; i < scrapCount; i++) {
                final ViewHolder holder = mAttachedScrap.get(i);
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }

            if (!dryRun) {
                View view = mChildHelper.findHiddenNonRemovedView(position);
                if (view != null) {
                    final ViewHolder vh = getChildViewHolderInt(view);
                    mChildHelper.unhide(view);
                    int layoutIndex = mChildHelper.indexOfChild(view);
                    if (layoutIndex == RecyclerView.NO_POSITION) {
                        throw new IllegalStateException("layout index should not be -1 after unhiding a view:" + vh + exceptionLabel());
                    }
                    mChildHelper.detachViewFromParent(layoutIndex);
                    scrapView(view);
                    vh.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP | ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                    return vh;
                }
            }

            final int cacheSize = mCachedViews.size();
            for (int i = 0; i < cacheSize; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (!holder.isInvalid() && holder.getLayoutPosition() == position && !holder.isAttachedToTransitionOverlay()) {
                    if (!dryRun) {
                        mCachedViews.remove(i);
                    }
                    if (DEBUG) {
                        Log.d(TAG, "getScrapOrHiddenOrCachedHolderForPosition(" + position + ") found match in cache: " + holder);
                    }
                    return holder;
                }
            }
            return null;
        }

        ViewHolder getScrapOrCachedViewForId(long id, int type, boolean dryRun) {
            final int count = mAttachedScrap.size();
            for (int i = count - 1; i >= 0; i--) {
                final ViewHolder holder = mAttachedScrap.get(i);
                if (holder.getItemId() == id && !holder.wasReturnedFromScrap()) {
                    if (type == holder.getItemViewType()) {
                        holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                        if (holder.isRemoved()) {
                            if (!mState.isPreLayout()) {
                                holder.setFlags(ViewHolder.FLAG_UPDATE, ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED);
                            }
                        }
                        return holder;
                    } else if (!dryRun) {
                        mAttachedScrap.remove(i);
                        removeDetachedView(holder.itemView, false);
                        quickRecycleScrapView(holder.itemView);
                    }
                }
            }

            final int cacheSize = mCachedViews.size();
            for (int i = cacheSize - 1; i >= 0; i--) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder.getItemId() == id && !holder.isAttachedToTransitionOverlay()) {
                    if (type == holder.getItemViewType()) {
                        if (!dryRun) {
                            mCachedViews.remove(i);
                        }
                        return holder;
                    } else if (!dryRun) {
                        recycleCachedViewAt(i);
                        return null;
                    }
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        void dispatchViewRecycled(@NonNull ViewHolder holder) {
            if (mRecyclerListener != null) {
                mRecyclerListener.onViewRecycled(holder);
            }

            final int listenerCount = mRecyclerListeners.size();
            for (int i = 0; i < listenerCount; i++) {
                mRecyclerListeners.get(i).onViewRecycled(holder);
            }
            if (mAdapter != null) {
                mAdapter.onViewRecycled(holder);
            }
            if (mState != null) {
                mViewInfoStore.removeViewHolder(holder);
            }
            if (DEBUG) Log.d(TAG, "dispatchViewRecycled: " + holder);
        }

        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter, boolean compatibleWithPrevious) {
            clear();
            getRecycledViewPool().onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
        }

        void offsetPositionRecordsForMove(int from, int to) {
            final int start, end, inBetweenOffset;
            if (from < to) {
                start = from;
                end = to;
                inBetweenOffset = -1;
            } else {
                start = to;
                end = from;
                inBetweenOffset = 1;
            }
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder == null || holder.mPosition < start || holder.mPosition > end) {
                    continue;
                }
                if (holder.mPosition == from) {
                    holder.offsetPosition(to - from, false);
                } else {
                    holder.offsetPosition(inBetweenOffset, false);
                }
                if (DEBUG) {
                    Log.d(TAG, "offsetPositionRecordsForMove cached child " + i + " holder " + holder);
                }
            }
        }

        void offsetPositionRecordsForInsert(int insertedAt, int count) {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null && holder.mPosition >= insertedAt) {
                    if (DEBUG) {
                        Log.d(TAG, "offsetPositionRecordsForInsert cached " + i + " holder " + holder + " now at position " + (holder.mPosition + count));
                    }
                    holder.offsetPosition(count, false);
                }
            }
        }

        void offsetPositionRecordsForRemove(int removedFrom, int count, boolean applyToPreLayout) {
            final int removedEnd = removedFrom + count;
            final int cachedCount = mCachedViews.size();
            for (int i = cachedCount - 1; i >= 0; i--) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null) {
                    if (holder.mPosition >= removedEnd) {
                        if (DEBUG) {
                            Log.d(TAG, "offsetPositionRecordsForRemove cached " + i + " holder " + holder + " now at position " + (holder.mPosition - count));
                        }
                        holder.offsetPosition(-count, applyToPreLayout);
                    } else if (holder.mPosition >= removedFrom) {
                        holder.addFlags(ViewHolder.FLAG_REMOVED);
                        recycleCachedViewAt(i);
                    }
                }
            }
        }

        void setViewCacheExtension(ViewCacheExtension extension) {
            mViewCacheExtension = extension;
        }

        void setRecycledViewPool(RecycledViewPool pool) {
            if (mRecyclerPool != null) {
                mRecyclerPool.detach();
            }
            mRecyclerPool = pool;
            if (mRecyclerPool != null && getAdapter() != null) {
                mRecyclerPool.attach();
            }
        }

        RecycledViewPool getRecycledViewPool() {
            if (mRecyclerPool == null) {
                mRecyclerPool = new RecycledViewPool();
            }
            return mRecyclerPool;
        }

        void viewRangeUpdate(int positionStart, int itemCount) {
            final int positionEnd = positionStart + itemCount;
            final int cachedCount = mCachedViews.size();
            for (int i = cachedCount - 1; i >= 0; i--) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder == null) {
                    continue;
                }

                final int pos = holder.mPosition;
                if (pos >= positionStart && pos < positionEnd) {
                    holder.addFlags(ViewHolder.FLAG_UPDATE);
                    recycleCachedViewAt(i);
                }
            }
        }

        void markKnownViewsInvalid() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null) {
                    holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
                    holder.addChangePayload(null);
                }
            }

            if (mAdapter == null || !mAdapter.hasStableIds()) {
                recycleAndClearCachedViews();
            }
        }

        void clearOldPositions() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                holder.clearOldPosition();
            }
            final int scrapCount = mAttachedScrap.size();
            for (int i = 0; i < scrapCount; i++) {
                mAttachedScrap.get(i).clearOldPosition();
            }
            if (mChangedScrap != null) {
                final int changedScrapCount = mChangedScrap.size();
                for (int i = 0; i < changedScrapCount; i++) {
                    mChangedScrap.get(i).clearOldPosition();
                }
            }
        }

        void markItemDecorInsetsDirty() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                LayoutParams layoutParams = (LayoutParams) holder.itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }
    }

    public abstract static class ViewCacheExtension {
        @Nullable
        public abstract View getViewForPositionAndType(@NonNull Recycler recycler, int position, int type);
    }

    public abstract static class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();
        private boolean mHasStableIds = false;
        private StateRestorationPolicy mStateRestorationPolicy = StateRestorationPolicy.ALLOW;

        @NonNull
        public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

        public abstract void onBindViewHolder(@NonNull VH holder, int position);

        public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
            onBindViewHolder(holder, position);
        }

        public int findRelativeAdapterPositionIn(@NonNull Adapter<? extends ViewHolder> adapter, @NonNull ViewHolder viewHolder, int localPosition) {
            if (adapter == this) {
                return localPosition;
            }
            return NO_POSITION;
        }

        @NonNull
        public final VH createViewHolder(@NonNull ViewGroup parent, int viewType) {
            try {
                TraceCompat.beginSection(TRACE_CREATE_VIEW_TAG);
                final VH holder = onCreateViewHolder(parent, viewType);
                if (holder.itemView.getParent() != null) {
                    throw new IllegalStateException("ViewHolder views must not be attached when created. Ensure that you are not passing 'true' to the attachToRoot parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
                }
                holder.mItemViewType = viewType;
                return holder;
            } finally {
                TraceCompat.endSection();
            }
        }

        public final void bindViewHolder(@NonNull VH holder, int position) {
            boolean rootBind = holder.mBindingAdapter == null;
            if (rootBind) {
                holder.mPosition = position;
                if (hasStableIds()) {
                    holder.mItemId = getItemId(position);
                }
                holder.setFlags(ViewHolder.FLAG_BOUND, ViewHolder.FLAG_BOUND | ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);
                TraceCompat.beginSection(TRACE_BIND_VIEW_TAG);
            }
            holder.mBindingAdapter = this;
            onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());
            if (rootBind) {
                holder.clearPayload();
                final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                if (layoutParams instanceof RecyclerView.LayoutParams) {
                    ((LayoutParams) layoutParams).mInsetsDirty = true;
                }
                TraceCompat.endSection();
            }
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public void setHasStableIds(boolean hasStableIds) {
            if (hasObservers()) {
                throw new IllegalStateException("Cannot change whether this adapter has stable IDs while the adapter has registered observers.");
            }
            mHasStableIds = hasStableIds;
        }

        public long getItemId(int position) {
            return NO_ID;
        }

        public abstract int getItemCount();

        public final boolean hasStableIds() {
            return mHasStableIds;
        }

        public void onViewRecycled(@NonNull VH holder) {
        }

        public boolean onFailedToRecycleView(@NonNull VH holder) {
            return false;
        }

        public void onViewAttachedToWindow(@NonNull VH holder) {
        }

        public void onViewDetachedFromWindow(@NonNull VH holder) {
        }

        public final boolean hasObservers() {
            return mObservable.hasObservers();
        }

        public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        }

        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        }

        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        public final void notifyItemChanged(int position) {
            mObservable.notifyItemRangeChanged(position, 1);
        }

        public final void notifyItemChanged(int position, @Nullable Object payload) {
            mObservable.notifyItemRangeChanged(position, 1, payload);
        }

        public final void notifyItemRangeChanged(int positionStart, int itemCount) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount);
        }

        public final void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        public final void notifyItemInserted(int position) {
            mObservable.notifyItemRangeInserted(position, 1);
        }

        public final void notifyItemMoved(int fromPosition, int toPosition) {
            mObservable.notifyItemMoved(fromPosition, toPosition);
        }

        public final void notifyItemRangeInserted(int positionStart, int itemCount) {
            mObservable.notifyItemRangeInserted(positionStart, itemCount);
        }

        public final void notifyItemRemoved(int position) {
            mObservable.notifyItemRangeRemoved(position, 1);
        }

        public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mObservable.notifyItemRangeRemoved(positionStart, itemCount);
        }

        public void setStateRestorationPolicy(@NonNull StateRestorationPolicy strategy) {
            mStateRestorationPolicy = strategy;
            mObservable.notifyStateRestorationPolicyChanged();
        }

        @NonNull
        public final StateRestorationPolicy getStateRestorationPolicy() {
            return mStateRestorationPolicy;
        }

        public boolean canRestoreState() {
            switch (mStateRestorationPolicy) {
                case PREVENT:
                    return false;
                case PREVENT_WHEN_EMPTY:
                    return getItemCount() > 0;
                default:
                    return true;
            }
        }

        public enum StateRestorationPolicy {
            ALLOW,
            PREVENT_WHEN_EMPTY,
            PREVENT
        }
    }

    @SuppressWarnings("unchecked")
    void dispatchChildDetached(View child) {
        final ViewHolder viewHolder = getChildViewHolderInt(child);
        onChildDetachedFromWindow(child);
        if (mAdapter != null && viewHolder != null) {
            mAdapter.onViewDetachedFromWindow(viewHolder);
        }
        if (mOnChildAttachStateListeners != null) {
            final int cnt = mOnChildAttachStateListeners.size();
            for (int i = cnt - 1; i >= 0; i--) {
                mOnChildAttachStateListeners.get(i).onChildViewDetachedFromWindow(child);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void dispatchChildAttached(View child) {
        final ViewHolder viewHolder = getChildViewHolderInt(child);
        onChildAttachedToWindow(child);
        if (mAdapter != null && viewHolder != null) {
            mAdapter.onViewAttachedToWindow(viewHolder);
        }
        if (mOnChildAttachStateListeners != null) {
            final int cnt = mOnChildAttachStateListeners.size();
            for (int i = cnt - 1; i >= 0; i--) {
                mOnChildAttachStateListeners.get(i).onChildViewAttachedToWindow(child);
            }
        }
    }

    public abstract static class LayoutManager {
        ChildHelper mChildHelper;
        public RecyclerView mRecyclerView;
        @Nullable
        SmoothScroller mSmoothScroller;
        boolean mRequestedSimpleAnimations = false;
        boolean mIsAttachedToWindow = false;
        boolean mAutoMeasure = false;
        private boolean mMeasurementCacheEnabled = true;
        private boolean mItemPrefetchEnabled = true;
        public int mPrefetchMaxCountObserved;
        public boolean mPrefetchMaxObservedInInitialPrefetch;
        private int mWidthMode, mHeightMode;
        private int mWidth, mHeight;

        private final ViewBoundsCheck.Callback mHorizontalBoundCheckCallback = new ViewBoundsCheck.Callback() {
            @Override
            public View getChildAt(int index) {
                return LayoutManager.this.getChildAt(index);
            }

            @Override
            public int getParentStart() {
                return LayoutManager.this.getPaddingLeft();
            }

            @Override
            public int getParentEnd() {
                return LayoutManager.this.getWidth() - LayoutManager.this.getPaddingRight();
            }

            @Override
            public int getChildStart(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return LayoutManager.this.getDecoratedLeft(view) - params.leftMargin;
            }

            @Override
            public int getChildEnd(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
                return LayoutManager.this.getDecoratedRight(view) + params.rightMargin;
            }
        };

        private final ViewBoundsCheck.Callback mVerticalBoundCheckCallback = new ViewBoundsCheck.Callback() {
            @Override
            public View getChildAt(int index) {
                return LayoutManager.this.getChildAt(index);
            }

            @Override
            public int getParentStart() {
                return LayoutManager.this.getPaddingTop();
            }

            @Override
            public int getParentEnd() {
                return LayoutManager.this.getHeight()
                        - LayoutManager.this.getPaddingBottom();
            }

            @Override
            public int getChildStart(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                        view.getLayoutParams();
                return LayoutManager.this.getDecoratedTop(view) - params.topMargin;
            }

            @Override
            public int getChildEnd(View view) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                        view.getLayoutParams();
                return LayoutManager.this.getDecoratedBottom(view) + params.bottomMargin;
            }
        };

        public ViewBoundsCheck mHorizontalBoundCheck = new ViewBoundsCheck(mHorizontalBoundCheckCallback);
        public ViewBoundsCheck mVerticalBoundCheck = new ViewBoundsCheck(mVerticalBoundCheckCallback);


        public interface LayoutPrefetchRegistry {
            void addPosition(int layoutPosition, int pixelDistance);
        }

        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                mRecyclerView = null;
                mChildHelper = null;
                mWidth = 0;
                mHeight = 0;
            } else {
                mRecyclerView = recyclerView;
                mChildHelper = recyclerView.mChildHelper;
                mWidth = recyclerView.getWidth();
                mHeight = recyclerView.getHeight();
            }
            mWidthMode = MeasureSpec.EXACTLY;
            mHeightMode = MeasureSpec.EXACTLY;
        }

        void setMeasureSpecs(int wSpec, int hSpec) {
            mWidth = MeasureSpec.getSize(wSpec);
            mWidthMode = MeasureSpec.getMode(wSpec);
            if (mWidthMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mWidth = 0;
            }

            mHeight = MeasureSpec.getSize(hSpec);
            mHeightMode = MeasureSpec.getMode(hSpec);
            if (mHeightMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mHeight = 0;
            }
        }

        void setMeasuredDimensionFromChildren(int widthSpec, int heightSpec) {
            final int count = getChildCount();
            if (count == 0) {
                mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                final Rect bounds = mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(child, bounds);
                if (bounds.left < minX) {
                    minX = bounds.left;
                }
                if (bounds.right > maxX) {
                    maxX = bounds.right;
                }
                if (bounds.top < minY) {
                    minY = bounds.top;
                }
                if (bounds.bottom > maxY) {
                    maxY = bounds.bottom;
                }
            }
            mRecyclerView.mTempRect.set(minX, minY, maxX, maxY);
            setMeasuredDimension(mRecyclerView.mTempRect, widthSpec, heightSpec);
        }

        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            int usedWidth = childrenBounds.width() + getPaddingLeft() + getPaddingRight();
            int usedHeight = childrenBounds.height() + getPaddingTop() + getPaddingBottom();
            int width = chooseSize(wSpec, usedWidth, getMinimumWidth());
            int height = chooseSize(hSpec, usedHeight, getMinimumHeight());
            setMeasuredDimension(width, height);
        }

        public void requestLayout() {
            if (mRecyclerView != null) {
                mRecyclerView.requestLayout();
            }
        }

        public void assertInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertInLayoutOrScroll(message);
            }
        }

        public static int chooseSize(int spec, int desired, int min) {
            final int mode = View.MeasureSpec.getMode(spec);
            final int size = View.MeasureSpec.getSize(spec);
            switch (mode) {
                case View.MeasureSpec.EXACTLY:
                    return size;
                case View.MeasureSpec.AT_MOST:
                    return Math.min(size, Math.max(desired, min));
                case View.MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }

        public void assertNotInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertNotInLayoutOrScroll(message);
            }
        }

        @Deprecated
        public void setAutoMeasureEnabled(boolean enabled) {
            mAutoMeasure = enabled;
        }

        public boolean isAutoMeasureEnabled() {
            return mAutoMeasure;
        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public final void setItemPrefetchEnabled(boolean enabled) {
            if (enabled != mItemPrefetchEnabled) {
                mItemPrefetchEnabled = enabled;
                mPrefetchMaxCountObserved = 0;
                if (mRecyclerView != null) {
                    mRecyclerView.mRecycler.updateViewCacheSize();
                }
            }
        }

        public final boolean isItemPrefetchEnabled() {
            return mItemPrefetchEnabled;
        }

        public void collectAdjacentPrefetchPositions(int dx, int dy, State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        }

        public void collectInitialPrefetchPositions(int adapterItemCount, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        }

        void dispatchAttachedToWindow(RecyclerView view) {
            mIsAttachedToWindow = true;
            onAttachedToWindow(view);
        }

        void dispatchDetachedFromWindow(RecyclerView view, Recycler recycler) {
            mIsAttachedToWindow = false;
            onDetachedFromWindow(view, recycler);
        }

        public boolean isAttachedToWindow() {
            return mIsAttachedToWindow;
        }

        public void postOnAnimation(Runnable action) {
            if (mRecyclerView != null) {
                ViewCompat.postOnAnimation(mRecyclerView, action);
            }
        }

        public boolean removeCallbacks(Runnable action) {
            if (mRecyclerView != null) {
                return mRecyclerView.removeCallbacks(action);
            }
            return false;
        }

        @CallSuper
        public void onAttachedToWindow(RecyclerView view) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView view) {
        }

        @CallSuper
        public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
            onDetachedFromWindow(view);
        }

        public boolean getClipToPadding() {
            return mRecyclerView != null && mRecyclerView.mClipToPadding;
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public void onLayoutCompleted(State state) {
        }

        public abstract LayoutParams generateDefaultLayoutParams();

        public boolean checkLayoutParams(LayoutParams lp) {
            return lp != null;
        }

        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            } else if (lp instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) lp);
            } else {
                return new LayoutParams(lp);
            }
        }

        public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            return new LayoutParams(c, attrs);
        }

        public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
            return 0;
        }

        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            return 0;
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public void scrollToPosition(int position) {
            if (DEBUG) {
                Log.e(TAG, "You MUST implement scrollToPosition. It will soon become abstract");
            }
        }

        public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
            Log.e(TAG, "You must override smoothScrollToPosition to support smooth scrolling");
        }

        public void startSmoothScroll(SmoothScroller smoothScroller) {
            if (mSmoothScroller != null && smoothScroller != mSmoothScroller && mSmoothScroller.isRunning()) {
                mSmoothScroller.stop();
            }
            mSmoothScroller = smoothScroller;
            mSmoothScroller.start(mRecyclerView, this);
        }

        public boolean isSmoothScrolling() {
            return mSmoothScroller != null && mSmoothScroller.isRunning();
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(mRecyclerView);
        }

        public void endAnimation(View view) {
            if (mRecyclerView.mItemAnimator != null) {
                mRecyclerView.mItemAnimator.endAnimation(getChildViewHolderInt(view));
            }
        }

        public void addDisappearingView(View child) {
            addDisappearingView(child, -1);
        }

        public void addDisappearingView(View child, int index) {
            addViewInt(child, index, true);
        }

        public void addView(View child) {
            addView(child, -1);
        }

        public void addView(View child, int index) {
            addViewInt(child, index, false);
        }

        private void addViewInt(View child, int index, boolean disappearing) {
            final ViewHolder holder = getChildViewHolderInt(child);
            if (disappearing || holder.isRemoved()) {
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(holder);
            } else {
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(holder);
            }
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (holder.wasReturnedFromScrap() || holder.isScrap()) {
                if (holder.isScrap()) {
                    holder.unScrap();
                } else {
                    holder.clearReturnedFromScrapFlag();
                }
                mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);
                if (DISPATCH_TEMP_DETACH) {
                    ViewCompat.dispatchFinishTemporaryDetach(child);
                }
            } else if (child.getParent() == mRecyclerView) {
                int currentIndex = mChildHelper.indexOfChild(child);
                if (index == -1) {
                    index = mChildHelper.getChildCount();
                }
                if (currentIndex == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but view is not a real child. Unfiltered index:" + mRecyclerView.indexOfChild(child) + mRecyclerView.exceptionLabel());
                }
                if (currentIndex != index) {
                    mRecyclerView.mLayout.moveView(currentIndex, index);
                }
            } else {
                mChildHelper.addView(child, index, false);
                lp.mInsetsDirty = true;
                if (mSmoothScroller != null && mSmoothScroller.isRunning()) {
                    mSmoothScroller.onChildAttachedToWindow(child);
                }
            }
            if (lp.mPendingInvalidate) {
                if (DEBUG) {
                    Log.d(TAG, "consuming pending invalidate on child " + lp.mViewHolder);
                }
                holder.itemView.invalidate();
                lp.mPendingInvalidate = false;
            }
        }

        public void removeView(View child) {
            mChildHelper.removeView(child);
        }

        public void removeViewAt(int index) {
            final View child = getChildAt(index);
            if (child != null) {
                mChildHelper.removeViewAt(index);
            }
        }

        public void removeAllViews() {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                mChildHelper.removeViewAt(i);
            }
        }

        public int getBaseline() {
            return -1;
        }

        public int getPosition(@NonNull View view) {
            return ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }

        public int getItemViewType(@NonNull View view) {
            return getChildViewHolderInt(view).getItemViewType();
        }

        @Nullable
        public View findContainingItemView(@NonNull View view) {
            if (mRecyclerView == null) {
                return null;
            }
            View found = mRecyclerView.findContainingItemView(view);
            if (found == null) {
                return null;
            }
            if (mChildHelper.isHidden(found)) {
                return null;
            }
            return found;
        }

        @Nullable
        public View findViewByPosition(int position) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                ViewHolder vh = getChildViewHolderInt(child);
                if (vh == null) {
                    continue;
                }
                if (vh.getLayoutPosition() == position && !vh.shouldIgnore() && (mRecyclerView.mState.isPreLayout() || !vh.isRemoved())) {
                    return child;
                }
            }
            return null;
        }

        public void detachView(@NonNull View child) {
            final int ind = mChildHelper.indexOfChild(child);
            if (ind >= 0) {
                detachViewInternal(ind, child);
            }
        }

        public void detachViewAt(int index) {
            detachViewInternal(index, getChildAt(index));
        }

        private void detachViewInternal(int index, @NonNull View view) {
            if (DISPATCH_TEMP_DETACH) {
                ViewCompat.dispatchStartTemporaryDetach(view);
            }
            mChildHelper.detachViewFromParent(index);
        }

        public void attachView(@NonNull View child, int index, LayoutParams lp) {
            ViewHolder vh = getChildViewHolderInt(child);
            if (vh.isRemoved()) {
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(vh);
            } else {
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(vh);
            }
            mChildHelper.attachViewToParent(child, index, lp, vh.isRemoved());
            if (DISPATCH_TEMP_DETACH) {
                ViewCompat.dispatchFinishTemporaryDetach(child);
            }
        }

        public void attachView(@NonNull View child, int index) {
            attachView(child, index, (LayoutParams) child.getLayoutParams());
        }

        public void attachView(@NonNull View child) {
            attachView(child, -1);
        }

        public void removeDetachedView(@NonNull View child) {
            mRecyclerView.removeDetachedView(child, false);
        }

        public void moveView(int fromIndex, int toIndex) {
            View view = getChildAt(fromIndex);
            if (view == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:" + fromIndex + mRecyclerView.toString());
            }
            detachViewAt(fromIndex);
            attachView(view, toIndex);
        }

        public void detachAndScrapView(@NonNull View child, @NonNull Recycler recycler) {
            int index = mChildHelper.indexOfChild(child);
            scrapOrRecycleView(recycler, index, child);
        }

        public void detachAndScrapViewAt(int index, @NonNull Recycler recycler) {
            final View child = getChildAt(index);
            scrapOrRecycleView(recycler, index, child);
        }

        public void removeAndRecycleView(@NonNull View child, @NonNull Recycler recycler) {
            removeView(child);
            recycler.recycleView(child);
        }

        public void removeAndRecycleViewAt(int index, @NonNull Recycler recycler) {
            final View view = getChildAt(index);
            removeViewAt(index);
            recycler.recycleView(view);
        }

        public int getChildCount() {
            return mChildHelper != null ? mChildHelper.getChildCount() : 0;
        }

        @Nullable
        public View getChildAt(int index) {
            return mChildHelper != null ? mChildHelper.getChildAt(index) : null;
        }

        public int getWidthMode() {
            return mWidthMode;
        }

        public int getHeightMode() {
            return mHeightMode;
        }

        @Px
        public int getWidth() {
            return mWidth;
        }

        @Px
        public int getHeight() {
            return mHeight;
        }

        @Px
        public int getPaddingLeft() {
            return mRecyclerView != null ? mRecyclerView.getPaddingLeft() : 0;
        }

        @Px
        public int getPaddingTop() {
            return mRecyclerView != null ? mRecyclerView.getPaddingTop() : 0;
        }

        @Px
        public int getPaddingRight() {
            return mRecyclerView != null ? mRecyclerView.getPaddingRight() : 0;
        }

        @Px
        public int getPaddingBottom() {
            return mRecyclerView != null ? mRecyclerView.getPaddingBottom() : 0;
        }

        @Px
        public int getPaddingStart() {
            return mRecyclerView != null ? ViewCompat.getPaddingStart(mRecyclerView) : 0;
        }

        @Px
        public int getPaddingEnd() {
            return mRecyclerView != null ? ViewCompat.getPaddingEnd(mRecyclerView) : 0;
        }

        public boolean isFocused() {
            return mRecyclerView != null && mRecyclerView.isFocused();
        }

        public boolean hasFocus() {
            return mRecyclerView != null && mRecyclerView.hasFocus();
        }

        @Nullable
        public View getFocusedChild() {
            if (mRecyclerView == null) {
                return null;
            }
            final View focused = mRecyclerView.getFocusedChild();
            if (focused == null || mChildHelper.isHidden(focused)) {
                return null;
            }
            return focused;
        }

        public int getItemCount() {
            final Adapter a = mRecyclerView != null ? mRecyclerView.getAdapter() : null;
            return a != null ? a.getItemCount() : 0;
        }

        public void offsetChildrenHorizontal(@Px int dx) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenHorizontal(dx);
            }
        }

        public void offsetChildrenVertical(@Px int dy) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenVertical(dy);
            }
        }

        public void ignoreView(@NonNull View view) {
            if (view.getParent() != mRecyclerView || mRecyclerView.indexOfChild(view) == -1) {
                throw new IllegalArgumentException("View should be fully attached to be ignored" + mRecyclerView.exceptionLabel());
            }
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.addFlags(ViewHolder.FLAG_IGNORE);
            mRecyclerView.mViewInfoStore.removeViewHolder(vh);
        }

        public void stopIgnoringView(@NonNull View view) {
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.stopIgnoring();
            vh.resetInternal();
            vh.addFlags(ViewHolder.FLAG_INVALID);
        }

        public void detachAndScrapAttachedViews(@NonNull Recycler recycler) {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                scrapOrRecycleView(recycler, i, v);
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int index, View view) {
            final ViewHolder viewHolder = getChildViewHolderInt(view);
            if (viewHolder.shouldIgnore()) {
                if (DEBUG) {
                    Log.d(TAG, "ignoring view " + viewHolder);
                }
                return;
            }
            if (viewHolder.isInvalid() && !viewHolder.isRemoved() && !mRecyclerView.mAdapter.hasStableIds()) {
                removeViewAt(index);
                recycler.recycleViewHolderInternal(viewHolder);
            } else {
                detachViewAt(index);
                recycler.scrapView(view);
                mRecyclerView.mViewInfoStore.onViewDetached(viewHolder);
            }
        }

        void removeAndRecycleScrapInt(Recycler recycler) {
            final int scrapCount = recycler.getScrapCount();
            for (int i = scrapCount - 1; i >= 0; i--) {
                final View scrap = recycler.getScrapViewAt(i);
                final ViewHolder vh = getChildViewHolderInt(scrap);
                if (vh.shouldIgnore()) {
                    continue;
                }
                vh.setIsRecyclable(false);
                if (vh.isTmpDetached()) {
                    mRecyclerView.removeDetachedView(scrap, false);
                }
                if (mRecyclerView.mItemAnimator != null) {
                    mRecyclerView.mItemAnimator.endAnimation(vh);
                }
                vh.setIsRecyclable(true);
                recycler.quickRecycleScrapView(scrap);
            }
            recycler.clearScrap();
            if (scrapCount > 0) {
                mRecyclerView.invalidate();
            }
        }

        public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;
            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight() + widthUsed, lp.width, canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom() + heightUsed, lp.height, canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }

        public boolean shouldReMeasureChild(View child, int widthSpec, int heightSpec, LayoutParams lp) {
            return !mMeasurementCacheEnabled || !isMeasurementUpToDate(child.getMeasuredWidth(), widthSpec, lp.width) || !isMeasurementUpToDate(child.getMeasuredHeight(), heightSpec, lp.height);
        }

        public boolean shouldMeasureChild(View child, int widthSpec, int heightSpec, LayoutParams lp) {
            return child.isLayoutRequested() || !mMeasurementCacheEnabled || !isMeasurementUpToDate(child.getWidth(), widthSpec, lp.width) || !isMeasurementUpToDate(child.getHeight(), heightSpec, lp.height);
        }

        public boolean isMeasurementCacheEnabled() {
            return mMeasurementCacheEnabled;
        }

        public void setMeasurementCacheEnabled(boolean measurementCacheEnabled) {
            mMeasurementCacheEnabled = measurementCacheEnabled;
        }

        private static boolean isMeasurementUpToDate(int childSize, int spec, int dimension) {
            final int specMode = MeasureSpec.getMode(spec);
            final int specSize = MeasureSpec.getSize(spec);
            if (dimension > 0 && childSize != dimension) {
                return false;
            }
            switch (specMode) {
                case MeasureSpec.UNSPECIFIED:
                    return true;
                case MeasureSpec.AT_MOST:
                    return specSize >= childSize;
                case MeasureSpec.EXACTLY:
                    return specSize == childSize;
            }
            return false;
        }

        public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            final Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            widthUsed += insets.left + insets.right;
            heightUsed += insets.top + insets.bottom;

            final int widthSpec = getChildMeasureSpec(getWidth(), getWidthMode(), getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed, lp.width, canScrollHorizontally());
            final int heightSpec = getChildMeasureSpec(getHeight(), getHeightMode(), getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed, lp.height, canScrollVertically());
            if (shouldMeasureChild(child, widthSpec, heightSpec, lp)) {
                child.measure(widthSpec, heightSpec);
            }
        }

        @Deprecated
        public static int getChildMeasureSpec(int parentSize, int padding, int childDimension, boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
            } else {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    resultMode = MeasureSpec.AT_MOST;
                }
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }

        public static int getChildMeasureSpec(int parentSize, int parentMode, int padding, int childDimension, boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    switch (parentMode) {
                        case MeasureSpec.AT_MOST:
                        case MeasureSpec.EXACTLY:
                            resultSize = size;
                            resultMode = parentMode;
                            break;
                        case MeasureSpec.UNSPECIFIED:
                            resultSize = 0;
                            resultMode = MeasureSpec.UNSPECIFIED;
                            break;
                    }
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = 0;
                    resultMode = MeasureSpec.UNSPECIFIED;
                }
            } else {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = MeasureSpec.EXACTLY;
                } else if (childDimension == LayoutParams.MATCH_PARENT) {
                    resultSize = size;
                    resultMode = parentMode;
                } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                    resultSize = size;
                    if (parentMode == MeasureSpec.AT_MOST || parentMode == MeasureSpec.EXACTLY) {
                        resultMode = MeasureSpec.AT_MOST;
                    } else {
                        resultMode = MeasureSpec.UNSPECIFIED;
                    }

                }
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }

        public int getDecoratedMeasuredWidth(@NonNull View child) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredWidth() + insets.left + insets.right;
        }

        public int getDecoratedMeasuredHeight(@NonNull View child) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return child.getMeasuredHeight() + insets.top + insets.bottom;
        }

        public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
            final Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            child.layout(left + insets.left, top + insets.top, right - insets.right, bottom - insets.bottom);
        }

        public void layoutDecoratedWithMargins(@NonNull View child, int left, int top, int right, int bottom) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final Rect insets = lp.mDecorInsets;
            child.layout(left + insets.left + lp.leftMargin, top + insets.top + lp.topMargin, right - insets.right - lp.rightMargin, bottom - insets.bottom - lp.bottomMargin);
        }

        public void getTransformedBoundingBox(@NonNull View child, boolean includeDecorInsets, @NonNull Rect out) {
            if (includeDecorInsets) {
                Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
                out.set(-insets.left, -insets.top, child.getWidth() + insets.right, child.getHeight() + insets.bottom);
            } else {
                out.set(0, 0, child.getWidth(), child.getHeight());
            }

            if (mRecyclerView != null) {
                final Matrix childMatrix = child.getMatrix();
                if (childMatrix != null && !childMatrix.isIdentity()) {
                    final RectF tempRectF = mRecyclerView.mTempRectF;
                    tempRectF.set(out);
                    childMatrix.mapRect(tempRectF);
                    out.set((int) Math.floor(tempRectF.left), (int) Math.floor(tempRectF.top), (int) Math.ceil(tempRectF.right), (int) Math.ceil(tempRectF.bottom));
                }
            }
            out.offset(child.getLeft(), child.getTop());
        }

        public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect outBounds) {
            RecyclerView.getDecoratedBoundsWithMarginsInt(view, outBounds);
        }

        public int getDecoratedLeft(@NonNull View child) {
            return child.getLeft() - getLeftDecorationWidth(child);
        }

        public int getDecoratedTop(@NonNull View child) {
            return child.getTop() - getTopDecorationHeight(child);
        }

        public int getDecoratedRight(@NonNull View child) {
            return child.getRight() + getRightDecorationWidth(child);
        }

        public int getDecoratedBottom(@NonNull View child) {
            return child.getBottom() + getBottomDecorationHeight(child);
        }

        public void calculateItemDecorationsForChild(@NonNull View child, @NonNull Rect outRect) {
            if (mRecyclerView == null) {
                outRect.set(0, 0, 0, 0);
                return;
            }
            Rect insets = mRecyclerView.getItemDecorInsetsForChild(child);
            outRect.set(insets);
        }

        public int getTopDecorationHeight(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.top;
        }

        public int getBottomDecorationHeight(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.bottom;
        }

        public int getLeftDecorationWidth(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.left;
        }

        public int getRightDecorationWidth(@NonNull View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.right;
        }

        @Nullable
        public View onFocusSearchFailed(@NonNull View focused, int direction, @NonNull Recycler recycler, @NonNull State state) {
            return null;
        }

        @Nullable
        public View onInterceptFocusSearch(@NonNull View focused, int direction) {
            return null;
        }

        private int[] getChildRectangleOnScreenScrollAmount(View child, Rect rect) {
            int[] out = new int[2];
            final int parentLeft = getPaddingLeft();
            final int parentTop = getPaddingTop();
            final int parentRight = getWidth() - getPaddingRight();
            final int parentBottom = getHeight() - getPaddingBottom();
            final int childLeft = child.getLeft() + rect.left - child.getScrollX();
            final int childTop = child.getTop() + rect.top - child.getScrollY();
            final int childRight = childLeft + rect.width();
            final int childBottom = childTop + rect.height();

            final int offScreenLeft = Math.min(0, childLeft - parentLeft);
            final int offScreenTop = Math.min(0, childTop - parentTop);
            final int offScreenRight = Math.max(0, childRight - parentRight);
            final int offScreenBottom = Math.max(0, childBottom - parentBottom);

            final int dx;
            if (getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft : Math.min(childLeft - parentLeft, offScreenRight);
            }

            final int dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
            out[0] = dx;
            out[1] = dy;
            return out;
        }

        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate) {
            return requestChildRectangleOnScreen(parent, child, rect, immediate, false);
        }

        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
            int[] scrollAmount = getChildRectangleOnScreenScrollAmount(child, rect);
            int dx = scrollAmount[0];
            int dy = scrollAmount[1];
            if (!focusedChildVisible || isFocusedChildVisibleAfterScrolling(parent, dx, dy)) {
                if (dx != 0 || dy != 0) {
                    if (immediate) {
                        parent.scrollBy(dx, dy);
                    } else {
                        parent.smoothScrollBy(dx, dy);
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isViewPartiallyVisible(@NonNull View child, boolean completelyVisible, boolean acceptEndPointInclusion) {
            int boundsFlag = (ViewBoundsCheck.FLAG_CVS_GT_PVS | ViewBoundsCheck.FLAG_CVS_EQ_PVS | ViewBoundsCheck.FLAG_CVE_LT_PVE | ViewBoundsCheck.FLAG_CVE_EQ_PVE);
            boolean isViewFullyVisible = mHorizontalBoundCheck.isViewWithinBoundFlags(child, boundsFlag) && mVerticalBoundCheck.isViewWithinBoundFlags(child, boundsFlag);
            if (completelyVisible) {
                return isViewFullyVisible;
            } else {
                return !isViewFullyVisible;
            }
        }

        private boolean isFocusedChildVisibleAfterScrolling(RecyclerView parent, int dx, int dy) {
            final View focusedChild = parent.getFocusedChild();
            if (focusedChild == null) {
                return false;
            }
            final int parentLeft = getPaddingLeft();
            final int parentTop = getPaddingTop();
            final int parentRight = getWidth() - getPaddingRight();
            final int parentBottom = getHeight() - getPaddingBottom();
            final Rect bounds = mRecyclerView.mTempRect;
            getDecoratedBoundsWithMargins(focusedChild, bounds);

            if (bounds.left - dx >= parentRight || bounds.right - dx <= parentLeft || bounds.top - dy >= parentBottom || bounds.bottom - dy <= parentTop) {
                return false;
            }
            return true;
        }

        @Deprecated
        public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull View child, @Nullable View focused) {
            return isSmoothScrolling() || parent.isComputingLayout();
        }

        public boolean onRequestChildFocus(@NonNull RecyclerView parent, @NonNull State state, @NonNull View child, @Nullable View focused) {
            return onRequestChildFocus(parent, child, focused);
        }

        public void onAdapterChanged(@Nullable Adapter oldAdapter, @Nullable Adapter newAdapter) {
        }

        public boolean onAddFocusables(@NonNull RecyclerView recyclerView, @NonNull ArrayList<View> views, int direction, int focusableMode) {
            return false;
        }

        public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        }

        public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart, int itemCount, @Nullable Object payload) {
            onItemsUpdated(recyclerView, positionStart, itemCount);
        }

        public void onItemsMoved(@NonNull RecyclerView recyclerView, int from, int to, int itemCount) {
        }

        public int computeHorizontalScrollExtent(@NonNull State state) {
            return 0;
        }

        public int computeHorizontalScrollOffset(@NonNull State state) {
            return 0;
        }

        public int computeHorizontalScrollRange(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollExtent(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollOffset(@NonNull State state) {
            return 0;
        }

        public int computeVerticalScrollRange(@NonNull State state) {
            return 0;
        }

        public void onMeasure(@NonNull Recycler recycler, @NonNull State state, int widthSpec, int heightSpec) {
            mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
        }

        public void setMeasuredDimension(int widthSize, int heightSize) {
            mRecyclerView.setMeasuredDimension(widthSize, heightSize);
        }

        @Px
        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(mRecyclerView);
        }

        @Px
        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(mRecyclerView);
        }

        @Nullable
        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }

        void stopSmoothScroller() {
            if (mSmoothScroller != null) {
                mSmoothScroller.stop();
            }
        }

        void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (mSmoothScroller == smoothScroller) {
                mSmoothScroller = null;
            }
        }

        public void onScrollStateChanged(int state) {
        }

        public void removeAndRecycleAllViews(@NonNull Recycler recycler) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                final View view = getChildAt(i);
                if (!getChildViewHolderInt(view).shouldIgnore()) {
                    removeAndRecycleViewAt(i, recycler);
                }
            }
        }

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
            onInitializeAccessibilityNodeInfo(mRecyclerView.mRecycler, mRecyclerView.mState, info);
        }

        public void onInitializeAccessibilityNodeInfo(@NonNull Recycler recycler, @NonNull State state, @NonNull AccessibilityNodeInfoCompat info) {
            if (mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollHorizontally(-1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                info.setScrollable(true);
            }
            if (mRecyclerView.canScrollVertically(1) || mRecyclerView.canScrollHorizontally(1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
                info.setScrollable(true);
            }
            final AccessibilityNodeInfoCompat.CollectionInfoCompat collectionInfo = AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state));
            info.setCollectionInfo(collectionInfo);
        }

        public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            onInitializeAccessibilityEvent(mRecyclerView.mRecycler, mRecyclerView.mState, event);
        }

        public void onInitializeAccessibilityEvent(@NonNull Recycler recycler, @NonNull State state, @NonNull AccessibilityEvent event) {
            if (mRecyclerView == null || event == null) {
                return;
            }
            event.setScrollable(mRecyclerView.canScrollVertically(1) || mRecyclerView.canScrollVertically(-1) || mRecyclerView.canScrollHorizontally(-1) || mRecyclerView.canScrollHorizontally(1));

            if (mRecyclerView.mAdapter != null) {
                event.setItemCount(mRecyclerView.mAdapter.getItemCount());
            }
        }

        public void onInitializeAccessibilityNodeInfoForItem(View host, AccessibilityNodeInfoCompat info) {
            final ViewHolder vh = getChildViewHolderInt(host);
            if (vh != null && !vh.isRemoved() && !mChildHelper.isHidden(vh.itemView)) {
                onInitializeAccessibilityNodeInfoForItem(mRecyclerView.mRecycler, mRecyclerView.mState, host, info);
            }
        }

        public void onInitializeAccessibilityNodeInfoForItem(@NonNull Recycler recycler, @NonNull State state, @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
            int verticalPos = canScrollVertically() ? getPosition(host) : 0;
            int horizontalPos = canScrollHorizontally() ? getPosition(host) : 0;

            final AccessibilityNodeInfoCompat.CollectionItemInfoCompat collectionInfo = AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(verticalPos, 1, horizontalPos, 1, false, false);
            info.setCollectionItemInfo(collectionInfo);
        }

        public void requestSimpleAnimationsInNextLayout() {
            mRequestedSimpleAnimations = true;
        }

        public int getSelectionModeForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            return AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_NONE;
        }

        public int getRowCountForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            if (mRecyclerView != null && mRecyclerView.mAdapter != null && canScrollVertically()) {
                return mRecyclerView.mAdapter.getItemCount();
            } else {
                return 1;
            }
        }

        public int getColumnCountForAccessibility(@NonNull Recycler recycler, @NonNull State state) {
            if (mRecyclerView != null && mRecyclerView.mAdapter != null && canScrollHorizontally()) {
                return mRecyclerView.mAdapter.getItemCount();
            } else {
                return 1;
            }
        }

        public boolean isLayoutHierarchical(@NonNull Recycler recycler, @NonNull State state) {
            return false;
        }

        public boolean performAccessibilityAction(int action, @Nullable Bundle args) {
            return performAccessibilityAction(mRecyclerView.mRecycler, mRecyclerView.mState, action, args);
        }

        public boolean performAccessibilityAction(@NonNull Recycler recycler, @NonNull State state, int action, @Nullable Bundle args) {
            if (mRecyclerView == null) {
                return false;
            }
            int vScroll = 0, hScroll = 0;
            int height = getHeight();
            int width = getWidth();
            Rect rect = new Rect();
            if (mRecyclerView.getMatrix().isIdentity() && mRecyclerView.getGlobalVisibleRect(rect)) {
                height = rect.height();
                width = rect.width();
            }
            switch (action) {
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD:
                    if (mRecyclerView.canScrollVertically(-1)) {
                        vScroll = -(height - getPaddingTop() - getPaddingBottom());
                    }
                    if (mRecyclerView.canScrollHorizontally(-1)) {
                        hScroll = -(width - getPaddingLeft() - getPaddingRight());
                    }
                    break;
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD:
                    if (mRecyclerView.canScrollVertically(1)) {
                        vScroll = height - getPaddingTop() - getPaddingBottom();
                    }
                    if (mRecyclerView.canScrollHorizontally(1)) {
                        hScroll = width - getPaddingLeft() - getPaddingRight();
                    }
                    break;
            }
            if (vScroll == 0 && hScroll == 0) {
                return false;
            }
            mRecyclerView.smoothScrollBy(hScroll, vScroll, null, UNDEFINED_DURATION, true);
            return true;
        }

        public boolean performAccessibilityActionForItem(@NonNull View view, int action, @Nullable Bundle args) {
            return performAccessibilityActionForItem(mRecyclerView.mRecycler, mRecyclerView.mState, view, action, args);
        }

        public boolean performAccessibilityActionForItem(@NonNull Recycler recycler, @NonNull State state, @NonNull View view, int action, @Nullable Bundle args) {
            return false;
        }

        public static Properties getProperties(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            Properties properties = new Properties();
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView, defStyleAttr, defStyleRes);
            properties.orientation = a.getInt(R.styleable.RecyclerView_android_orientation, DEFAULT_ORIENTATION);
            properties.spanCount = a.getInt(R.styleable.RecyclerView_spanCount, 1);
            properties.reverseLayout = a.getBoolean(R.styleable.RecyclerView_reverseLayout, false);
            properties.stackFromEnd = a.getBoolean(R.styleable.RecyclerView_stackFromEnd, false);
            a.recycle();
            return properties;
        }

        void setExactMeasureSpecsFrom(RecyclerView recyclerView) {
            setMeasureSpecs(MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), MeasureSpec.EXACTLY));
        }

        public boolean shouldMeasureTwice() {
            return false;
        }

        public boolean hasFlexibleChildInBothOrientations() {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                final ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp.width < 0 && lp.height < 0) {
                    return true;
                }
            }
            return false;
        }

        public static class Properties {
            public int orientation;
            public int spanCount;
            public boolean reverseLayout;
            public boolean stackFromEnd;
        }
    }

    public abstract static class ItemDecoration {
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
            onDraw(c, parent);
        }

        @Deprecated
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent) {
        }

        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
            onDrawOver(c, parent);
        }

        @Deprecated
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent) {
        }

        public void seslOnDispatchDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
        }

        @Deprecated
        public void getItemOffsets(@NonNull Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
            outRect.set(0, 0, 0, 0);
        }

        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull State state) {
            getItemOffsets(outRect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(), parent);
        }
    }

    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e);

        void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e);

        void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept);
    }

    public static class SimpleOnItemTouchListener implements RecyclerView.OnItemTouchListener {
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    public abstract static class OnScrollListener {
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        }

        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        }
    }

    public interface RecyclerListener {
        void onViewRecycled(@NonNull ViewHolder holder);
    }

    public interface SeslFastScrollerEventListener {
        void onPressed(float y);

        void onReleased(float y);
    }

    public interface SeslLongPressMultiSelectionListener {
        void onItemSelected(RecyclerView view, View child, int position, long id);

        void onLongPressMultiSelectionEnded(int x, int y);

        void onLongPressMultiSelectionStarted(int x, int y);
    }

    public interface SeslOnGoToTopClickListener {
        boolean onGoToTopClick(RecyclerView view);
    }

    public interface SeslOnMultiSelectedListener {
        void onMultiSelectStart(int x, int y);

        void onMultiSelectStop(int x, int y);
    }

    public interface OnChildAttachStateChangeListener {
        void onChildViewAttachedToWindow(@NonNull View view);

        void onChildViewDetachedFromWindow(@NonNull View view);
    }

    public abstract static class ViewHolder {
        @NonNull
        public final View itemView;
        public WeakReference<RecyclerView> mNestedRecyclerView;
        public int mPosition = NO_POSITION;
        int mOldPosition = NO_POSITION;
        long mItemId = NO_ID;
        int mItemViewType = INVALID_TYPE;
        int mPreLayoutPosition = NO_POSITION;
        ViewHolder mShadowedHolder = null;
        ViewHolder mShadowingHolder = null;
        static final int FLAG_BOUND = 1 << 0;
        static final int FLAG_UPDATE = 1 << 1;
        static final int FLAG_INVALID = 1 << 2;
        static final int FLAG_REMOVED = 1 << 3;
        static final int FLAG_NOT_RECYCLABLE = 1 << 4;
        static final int FLAG_RETURNED_FROM_SCRAP = 1 << 5;
        static final int FLAG_IGNORE = 1 << 7;
        static final int FLAG_TMP_DETACHED = 1 << 8;
        static final int FLAG_ADAPTER_POSITION_UNKNOWN = 1 << 9;
        static final int FLAG_ADAPTER_FULLUPDATE = 1 << 10;
        static final int FLAG_MOVED = 1 << 11;
        static final int FLAG_APPEARED_IN_PRE_LAYOUT = 1 << 12;
        static final int PENDING_ACCESSIBILITY_STATE_NOT_SET = -1;
        static final int FLAG_BOUNCED_FROM_HIDDEN_LIST = 1 << 13;
        int mFlags;
        private static final List<Object> FULLUPDATE_PAYLOADS = Collections.emptyList();
        List<Object> mPayloads = null;
        List<Object> mUnmodifiedPayloads = null;
        private int mIsRecyclableCount = 0;
        Recycler mScrapContainer = null;
        boolean mInChangeScrap = false;
        private int mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        @VisibleForTesting
        int mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
        RecyclerView mOwnerRecyclerView;
        Adapter<? extends ViewHolder> mBindingAdapter;

        public ViewHolder(@NonNull View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }

        void flagRemovedAndOffsetPosition(int mNewPosition, int offset, boolean applyToPreLayout) {
            addFlags(ViewHolder.FLAG_REMOVED);
            offsetPosition(offset, applyToPreLayout);
            mPosition = mNewPosition;
        }

        void offsetPosition(int offset, boolean applyToPreLayout) {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }
            if (mPreLayoutPosition == NO_POSITION) {
                mPreLayoutPosition = mPosition;
            }
            if (applyToPreLayout) {
                mPreLayoutPosition += offset;
            }
            mPosition += offset;
            if (itemView.getLayoutParams() != null) {
                ((LayoutParams) itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        void clearOldPosition() {
            mOldPosition = NO_POSITION;
            mPreLayoutPosition = NO_POSITION;
        }

        void saveOldPosition() {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }
        }

        public boolean shouldIgnore() {
            return (mFlags & FLAG_IGNORE) != 0;
        }

        @Deprecated
        public final int getPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }

        public final int getLayoutPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }

        @Deprecated
        public final int getAdapterPosition() {
            return getBindingAdapterPosition();
        }

        public final int getBindingAdapterPosition() {
            if (mBindingAdapter == null) {
                return NO_POSITION;
            }
            if (mOwnerRecyclerView == null) {
                return NO_POSITION;
            }
            @SuppressWarnings("unchecked")
            Adapter<? extends ViewHolder> rvAdapter = mOwnerRecyclerView.getAdapter();
            if (rvAdapter == null) {
                return NO_POSITION;
            }
            int globalPosition = mOwnerRecyclerView.getAdapterPositionInRecyclerView(this);
            if (globalPosition == NO_POSITION) {
                return NO_POSITION;
            }
            return rvAdapter.findRelativeAdapterPositionIn(mBindingAdapter, this, globalPosition);
        }

        public final int getAbsoluteAdapterPosition() {
            if (mOwnerRecyclerView == null) {
                return NO_POSITION;
            }
            return mOwnerRecyclerView.getAdapterPositionInRecyclerView(this);
        }

        @Nullable
        public final Adapter<? extends ViewHolder> getBindingAdapter() {
            return mBindingAdapter;
        }

        public final int getOldPosition() {
            return mOldPosition;
        }

        public final long getItemId() {
            return mItemId;
        }

        public final int getItemViewType() {
            return mItemViewType;
        }

        boolean isScrap() {
            return mScrapContainer != null;
        }

        void unScrap() {
            mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            return (mFlags & FLAG_RETURNED_FROM_SCRAP) != 0;
        }

        void clearReturnedFromScrapFlag() {
            mFlags = mFlags & ~FLAG_RETURNED_FROM_SCRAP;
        }

        void clearTmpDetachFlag() {
            mFlags = mFlags & ~FLAG_TMP_DETACHED;
        }

        void stopIgnoring() {
            mFlags = mFlags & ~FLAG_IGNORE;
        }

        void setScrapContainer(Recycler recycler, boolean isChangeScrap) {
            mScrapContainer = recycler;
            mInChangeScrap = isChangeScrap;
        }

        public boolean isInvalid() {
            return (mFlags & FLAG_INVALID) != 0;
        }

        boolean needsUpdate() {
            return (mFlags & FLAG_UPDATE) != 0;
        }

        public boolean isBound() {
            return (mFlags & FLAG_BOUND) != 0;
        }

        public boolean isRemoved() {
            return (mFlags & FLAG_REMOVED) != 0;
        }

        boolean hasAnyOfTheFlags(int flags) {
            return (mFlags & flags) != 0;
        }

        boolean isTmpDetached() {
            return (mFlags & FLAG_TMP_DETACHED) != 0;
        }

        boolean isAttachedToTransitionOverlay() {
            return itemView.getParent() != null && itemView.getParent() != mOwnerRecyclerView;
        }

        boolean isAdapterPositionUnknown() {
            return (mFlags & FLAG_ADAPTER_POSITION_UNKNOWN) != 0 || isInvalid();
        }

        void setFlags(int flags, int mask) {
            mFlags = (mFlags & ~mask) | (flags & mask);
        }

        void addFlags(int flags) {
            mFlags |= flags;
        }

        void addChangePayload(Object payload) {
            if (payload == null) {
                addFlags(FLAG_ADAPTER_FULLUPDATE);
            } else if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                createPayloadsIfNeeded();
                mPayloads.add(payload);
            }
        }

        private void createPayloadsIfNeeded() {
            if (mPayloads == null) {
                mPayloads = new ArrayList<Object>();
                mUnmodifiedPayloads = Collections.unmodifiableList(mPayloads);
            }
        }

        void clearPayload() {
            if (mPayloads != null) {
                mPayloads.clear();
            }
            mFlags = mFlags & ~FLAG_ADAPTER_FULLUPDATE;
        }

        List<Object> getUnmodifiedPayloads() {
            if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                if (mPayloads == null || mPayloads.size() == 0) {
                    return FULLUPDATE_PAYLOADS;
                }
                return mUnmodifiedPayloads;
            } else {
                return FULLUPDATE_PAYLOADS;
            }
        }

        void resetInternal() {
            mFlags = 0;
            mPosition = NO_POSITION;
            mOldPosition = NO_POSITION;
            mItemId = NO_ID;
            mPreLayoutPosition = NO_POSITION;
            mIsRecyclableCount = 0;
            mShadowedHolder = null;
            mShadowingHolder = null;
            clearPayload();
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
            mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
            clearNestedRecyclerViewIfNotNested(this);
        }

        void onEnteredHiddenState(RecyclerView parent) {
            if (mPendingAccessibilityState != PENDING_ACCESSIBILITY_STATE_NOT_SET) {
                mWasImportantForAccessibilityBeforeHidden = mPendingAccessibilityState;
            } else {
                mWasImportantForAccessibilityBeforeHidden = ViewCompat.getImportantForAccessibility(itemView);
            }
            parent.setChildImportantForAccessibilityInternal(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }

        void onLeftHiddenState(RecyclerView parent) {
            parent.setChildImportantForAccessibilityInternal(this, mWasImportantForAccessibilityBeforeHidden);
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        }

        @Override
        public String toString() {
            String className = getClass().isAnonymousClass() ? "ViewHolder" : getClass().getSimpleName();
            final StringBuilder sb = new StringBuilder(className + "{" + Integer.toHexString(hashCode()) + " position=" + mPosition + " id=" + mItemId + ", oldPos=" + mOldPosition + ", pLpos:" + mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap ").append(mInChangeScrap ? "[changeScrap]" : "[attachedScrap]");
            }
            if (isInvalid()) sb.append(" invalid");
            if (!isBound()) sb.append(" unbound");
            if (needsUpdate()) sb.append(" update");
            if (isRemoved()) sb.append(" removed");
            if (shouldIgnore()) sb.append(" ignored");
            if (isTmpDetached()) sb.append(" tmpDetached");
            if (!isRecyclable()) sb.append(" not recyclable(" + mIsRecyclableCount + ")");
            if (isAdapterPositionUnknown()) sb.append(" undefined adapter position");

            if (itemView.getParent() == null) sb.append(" no parent");
            sb.append("}");
            return sb.toString();
        }

        public final void setIsRecyclable(boolean recyclable) {
            mIsRecyclableCount = recyclable ? mIsRecyclableCount - 1 : mIsRecyclableCount + 1;
            if (mIsRecyclableCount < 0) {
                mIsRecyclableCount = 0;
                if (DEBUG) {
                    throw new RuntimeException("isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
                }
                Log.e(VIEW_LOG_TAG, "isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
            } else if (!recyclable && mIsRecyclableCount == 1) {
                mFlags |= FLAG_NOT_RECYCLABLE;
            } else if (recyclable && mIsRecyclableCount == 0) {
                mFlags &= ~FLAG_NOT_RECYCLABLE;
            }
            if (DEBUG) {
                Log.d(TAG, "setIsRecyclable val:" + recyclable + ":" + this);
            }
        }

        public final boolean isRecyclable() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0 && !ViewCompat.hasTransientState(itemView);
        }

        boolean shouldBeKeptAsChild() {
            return (mFlags & FLAG_NOT_RECYCLABLE) != 0;
        }

        boolean doesTransientStatePreventRecycling() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0 && ViewCompat.hasTransientState(itemView);
        }

        boolean isUpdated() {
            return (mFlags & FLAG_UPDATE) != 0;
        }

        public int getFlags() {
            return mFlags;
        }
    }

    @VisibleForTesting
    boolean setChildImportantForAccessibilityInternal(ViewHolder viewHolder, int importantForAccessibility) {
        if (isComputingLayout()) {
            viewHolder.mPendingAccessibilityState = importantForAccessibility;
            mPendingAccessibilityImportanceChange.add(viewHolder);
            return false;
        }
        ViewCompat.setImportantForAccessibility(viewHolder.itemView, importantForAccessibility);
        return true;
    }

    @SuppressLint("WrongConstant")
    void dispatchPendingImportantForAccessibilityChanges() {
        for (int i = mPendingAccessibilityImportanceChange.size() - 1; i >= 0; i--) {
            ViewHolder viewHolder = mPendingAccessibilityImportanceChange.get(i);
            if (viewHolder.itemView.getParent() != this || viewHolder.shouldIgnore()) {
                continue;
            }
            int state = viewHolder.mPendingAccessibilityState;
            if (state != ViewHolder.PENDING_ACCESSIBILITY_STATE_NOT_SET) {
                ViewCompat.setImportantForAccessibility(viewHolder.itemView, state);
                viewHolder.mPendingAccessibilityState = ViewHolder.PENDING_ACCESSIBILITY_STATE_NOT_SET;
            }
        }
        mPendingAccessibilityImportanceChange.clear();
    }

    int getAdapterPositionInRecyclerView(ViewHolder viewHolder) {
        if (viewHolder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN) || !viewHolder.isBound()) {
            return RecyclerView.NO_POSITION;
        }
        return mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
    }

    @VisibleForTesting
    void initFastScroller(StateListDrawable verticalThumbDrawable, Drawable verticalTrackDrawable, StateListDrawable horizontalThumbDrawable, Drawable horizontalTrackDrawable) {
        if (verticalThumbDrawable == null || verticalTrackDrawable == null || horizontalThumbDrawable == null || horizontalTrackDrawable == null) {
            throw new IllegalArgumentException("Trying to set fast scroller without both required drawables."+ exceptionLabel());
        }

        Resources resources = getContext().getResources();
        new FastScroller(this, verticalThumbDrawable, verticalTrackDrawable, horizontalThumbDrawable, horizontalTrackDrawable, resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness), resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range), resources.getDimensionPixelOffset(R.dimen.fastscroll_margin));
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getScrollingChildHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public void stopNestedScroll(int type) {
        getScrollingChildHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getScrollingChildHelper().hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public final void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, @NonNull int[] consumed) {
        getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    private boolean seslDispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, @NonNull int[] consumed) {
        return getScrollingChildHelper().seslDispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    public static class LayoutParams extends android.view.ViewGroup.MarginLayoutParams {
        ViewHolder mViewHolder;
        public final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;
        boolean mPendingInvalidate = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.LayoutParams) source);
        }

        public boolean viewNeedsUpdate() {
            return mViewHolder.needsUpdate();
        }

        public boolean isViewInvalid() {
            return mViewHolder.isInvalid();
        }

        public boolean isItemRemoved() {
            return mViewHolder.isRemoved();
        }

        public boolean isItemChanged() {
            return mViewHolder.isUpdated();
        }

        @Deprecated
        public int getViewPosition() {
            return mViewHolder.getPosition();
        }

        public int getViewLayoutPosition() {
            return mViewHolder.getLayoutPosition();
        }

        @Deprecated
        public int getViewAdapterPosition() {
            return mViewHolder.getBindingAdapterPosition();
        }

        public int getAbsoluteAdapterPosition() {
            return mViewHolder.getAbsoluteAdapterPosition();
        }

        public int getBindingAdapterPosition() {
            return mViewHolder.getBindingAdapterPosition();
        }
    }

    public abstract static class AdapterDataObserver {
        public void onChanged() {
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
        }

        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            onItemRangeChanged(positionStart, itemCount);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        }

        public void onStateRestorationPolicyChanged() {
        }
    }

    public abstract static class SmoothScroller {
        private int mTargetPosition = RecyclerView.NO_POSITION;
        private RecyclerView mRecyclerView;
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private boolean mRunning;
        private View mTargetView;
        private final Action mRecyclingAction;
        private boolean mStarted;

        public SmoothScroller() {
            mRecyclingAction = new Action(0, 0);
        }

        void start(RecyclerView recyclerView, LayoutManager layoutManager) {
            recyclerView.mViewFlinger.stop();

            if (mStarted) {
                Log.w(TAG, "An instance of " + this.getClass().getSimpleName() + " was started more than once. Each instance of" + this.getClass().getSimpleName() + " is intended to only be used once. You should create a new instance for each use.");
            }

            mRecyclerView = recyclerView;
            mLayoutManager = layoutManager;
            if (mTargetPosition == RecyclerView.NO_POSITION) {
                throw new IllegalArgumentException("Invalid target position");
            }
            mRecyclerView.mState.mTargetPosition = mTargetPosition;
            mRunning = true;
            mPendingInitialRun = true;
            mTargetView = findViewByPosition(getTargetPosition());
            onStart();
            mRecyclerView.mViewFlinger.postOnAnimation();

            mStarted = true;
        }

        public void setTargetPosition(int targetPosition) {
            mTargetPosition = targetPosition;
        }

        @Nullable
        public PointF computeScrollVectorForPosition(int targetPosition) {
            LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof ScrollVectorProvider) {
                return ((ScrollVectorProvider) layoutManager).computeScrollVectorForPosition(targetPosition);
            }
            Log.w(TAG, "You should override computeScrollVectorForPosition when the LayoutManager does not implement " + ScrollVectorProvider.class.getCanonicalName());
            return null;
        }

        @Nullable
        public LayoutManager getLayoutManager() {
            return mLayoutManager;
        }

        protected final void stop() {
            if (!mRunning) {
                return;
            }
            mRunning = false;
            onStop();
            mRecyclerView.mState.mTargetPosition = RecyclerView.NO_POSITION;
            mTargetView = null;
            mTargetPosition = RecyclerView.NO_POSITION;
            mPendingInitialRun = false;
            mLayoutManager.onSmoothScrollerStopped(this);
            mLayoutManager = null;
            mRecyclerView = null;
        }

        public boolean isPendingInitialRun() {
            return mPendingInitialRun;
        }

        public boolean isRunning() {
            return mRunning;
        }

        public int getTargetPosition() {
            return mTargetPosition;
        }

        void onAnimation(int dx, int dy) {
            final RecyclerView recyclerView = mRecyclerView;
            if (mTargetPosition == RecyclerView.NO_POSITION || recyclerView == null) {
                stop();
            }

            if (mPendingInitialRun && mTargetView == null && mLayoutManager != null) {
                PointF pointF = computeScrollVectorForPosition(mTargetPosition);
                if (pointF != null && (pointF.x != 0 || pointF.y != 0)) {
                    recyclerView.scrollStep((int) Math.signum(pointF.x), (int) Math.signum(pointF.y), null);
                }
            }

            mPendingInitialRun = false;

            if (mTargetView != null) {
                if (getChildPosition(mTargetView) == mTargetPosition) {
                    onTargetFound(mTargetView, recyclerView.mState, mRecyclingAction);
                    mRecyclingAction.runIfNecessary(recyclerView);
                    stop();
                } else {
                    Log.e(TAG, "Passed over target position while smooth scrolling.");
                    mTargetView = null;
                }
            }
            if (mRunning) {
                onSeekTargetStep(dx, dy, recyclerView.mState, mRecyclingAction);
                boolean hadJumpTarget = mRecyclingAction.hasJumpTarget();
                mRecyclingAction.runIfNecessary(recyclerView);
                if (hadJumpTarget) {
                    if (mRunning) {
                        mPendingInitialRun = true;
                        recyclerView.mViewFlinger.postOnAnimation();
                    }
                }
            }
        }

        public int getChildPosition(View view) {
            return mRecyclerView.getChildLayoutPosition(view);
        }

        public int getChildCount() {
            return mRecyclerView.mLayout.getChildCount();
        }

        public View findViewByPosition(int position) {
            return mRecyclerView.mLayout.findViewByPosition(position);
        }

        @Deprecated
        public void instantScrollToPosition(int position) {
            mRecyclerView.scrollToPosition(position);
        }

        protected void onChildAttachedToWindow(View child) {
            if (getChildPosition(child) == getTargetPosition()) {
                mTargetView = child;
                if (DEBUG) {
                    Log.d(TAG, "smooth scroll target view has been attached");
                }
            }
        }

        protected void normalize(@NonNull PointF scrollVector) {
            final float magnitude = (float) Math.sqrt(scrollVector.x * scrollVector.x + scrollVector.y * scrollVector.y);
            scrollVector.x /= magnitude;
            scrollVector.y /= magnitude;
        }

        protected abstract void onStart();

        protected abstract void onStop();

        protected abstract void onSeekTargetStep(@Px int dx, @Px int dy, @NonNull State state, @NonNull Action action);

        protected abstract void onTargetFound(@NonNull View targetView, @NonNull State state, @NonNull Action action);


        public static class Action {
            public static final int UNDEFINED_DURATION = RecyclerView.UNDEFINED_DURATION;
            private int mDx;
            private int mDy;
            private int mDuration;
            private int mJumpToPosition = NO_POSITION;
            private Interpolator mInterpolator;
            private boolean mChanged = false;
            private int mConsecutiveUpdates = 0;

            public Action(@Px int dx, @Px int dy) {
                this(dx, dy, UNDEFINED_DURATION, null);
            }

            public Action(@Px int dx, @Px int dy, int duration) {
                this(dx, dy, duration, null);
            }

            public Action(@Px int dx, @Px int dy, int duration, @Nullable Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
            }

            public void jumpTo(int targetPosition) {
                mJumpToPosition = targetPosition;
            }

            boolean hasJumpTarget() {
                return mJumpToPosition >= 0;
            }

            void runIfNecessary(RecyclerView recyclerView) {
                if (mJumpToPosition >= 0) {
                    final int position = mJumpToPosition;
                    mJumpToPosition = NO_POSITION;
                    recyclerView.jumpToPositionForSmoothScroller(position);
                    mChanged = false;
                    return;
                }
                if (mChanged) {
                    validate();
                    recyclerView.mViewFlinger.smoothScrollBy(mDx, mDy, mDuration, mInterpolator);
                    mConsecutiveUpdates++;
                    if (mConsecutiveUpdates > 10) {
                        Log.e(TAG, "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                    }
                    mChanged = false;
                } else {
                    mConsecutiveUpdates = 0;
                }
            }

            private void validate() {
                if (mInterpolator != null && mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                } else if (mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            @Px
            public int getDx() {
                return mDx;
            }

            public void setDx(@Px int dx) {
                mChanged = true;
                mDx = dx;
            }

            @Px
            public int getDy() {
                return mDy;
            }

            public void setDy(@Px int dy) {
                mChanged = true;
                mDy = dy;
            }

            public int getDuration() {
                return mDuration;
            }

            public void setDuration(int duration) {
                mChanged = true;
                mDuration = duration;
            }

            @Nullable
            public Interpolator getInterpolator() {
                return mInterpolator;
            }

            public void setInterpolator(@Nullable Interpolator interpolator) {
                mChanged = true;
                mInterpolator = interpolator;
            }

            public void update(@Px int dx, @Px int dy, int duration, @Nullable Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
                mChanged = true;
            }
        }

        public interface ScrollVectorProvider {
            @Nullable
            PointF computeScrollVectorForPosition(int targetPosition);
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }

        public void notifyStateRestorationPolicyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onStateRestorationPolicyChanged();
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount, null);
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
            }
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, 1);
            }
        }
    }

    public static class SavedState extends AbsSavedState {
        Parcelable mLayoutState;

        @SuppressWarnings("deprecation")
        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            mLayoutState = in.readParcelable(loader != null ? loader : LayoutManager.class.getClassLoader());
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(mLayoutState, 0);
        }

        void copyFrom(SavedState other) {
            mLayoutState = other.mLayoutState;
        }

        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public static class State {
        static final int STEP_START = 1;
        public static final int STEP_LAYOUT = 1 << 1;
        static final int STEP_ANIMATIONS = 1 << 2;
        int mTargetPosition = RecyclerView.NO_POSITION;
        private SparseArray<Object> mData;
        int mPreviousLayoutItemCount = 0;
        int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        @IntDef(flag = true, value = {STEP_START, STEP_LAYOUT, STEP_ANIMATIONS})
        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutState {
        }
        @LayoutState
        public int mLayoutStep = STEP_START;
        int mItemCount = 0;
        boolean mStructureChanged = false;
        boolean mInPreLayout = false;
        boolean mTrackOldChangeHolders = false;
        boolean mIsMeasuring = false;
        boolean mRunSimpleAnimations = false;
        boolean mRunPredictiveAnimations = false;
        int mFocusedItemPosition;
        long mFocusedItemId;
        int mFocusedSubChildId;
        int mRemainingScrollHorizontal;
        int mRemainingScrollVertical;

        void assertLayoutStep(int accepted) {
            if ((accepted & mLayoutStep) == 0) {
                throw new IllegalStateException("Layout state should be one of " + Integer.toBinaryString(accepted) + " but it is " + Integer.toBinaryString(mLayoutStep));
            }
        }

        public void prepareForNestedPrefetch(Adapter adapter) {
            mLayoutStep = STEP_START;
            mItemCount = adapter.getItemCount();
            mInPreLayout = false;
            mTrackOldChangeHolders = false;
            mIsMeasuring = false;
        }

        public boolean isMeasuring() {
            return mIsMeasuring;
        }

        public boolean isPreLayout() {
            return mInPreLayout;
        }

        public boolean willRunPredictiveAnimations() {
            return mRunPredictiveAnimations;
        }

        public boolean willRunSimpleAnimations() {
            return mRunSimpleAnimations;
        }

        public void remove(int resourceId) {
            if (mData == null) {
                return;
            }
            mData.remove(resourceId);
        }

        @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
        public <T> T get(int resourceId) {
            if (mData == null) {
                return null;
            }
            return (T) mData.get(resourceId);
        }

        public void put(int resourceId, Object data) {
            if (mData == null) {
                mData = new SparseArray<Object>();
            }
            mData.put(resourceId, data);
        }

        public int getTargetScrollPosition() {
            return mTargetPosition;
        }

        public boolean hasTargetScrollPosition() {
            return mTargetPosition != RecyclerView.NO_POSITION;
        }

        public boolean didStructureChange() {
            return mStructureChanged;
        }

        public int getItemCount() {
            return mInPreLayout ? (mPreviousLayoutItemCount - mDeletedInvisibleItemCountSincePreviousLayout) : mItemCount;
        }

        public int getRemainingScrollHorizontal() {
            return mRemainingScrollHorizontal;
        }

        public int getRemainingScrollVertical() {
            return mRemainingScrollVertical;
        }

        @Override
        public String toString() {
            return "State{mTargetPosition=" + mTargetPosition + ", mData=" + mData + ", mItemCount=" + mItemCount + ", mIsMeasuring=" + mIsMeasuring + ", mPreviousLayoutItemCount=" + mPreviousLayoutItemCount + ", mDeletedInvisibleItemCountSincePreviousLayout=" + mDeletedInvisibleItemCountSincePreviousLayout + ", mStructureChanged=" + mStructureChanged + ", mInPreLayout=" + mInPreLayout + ", mRunSimpleAnimations=" + mRunSimpleAnimations + ", mRunPredictiveAnimations=" + mRunPredictiveAnimations + '}';
        }
    }

    public abstract static class OnFlingListener {
        public abstract boolean onFling(int velocityX, int velocityY);
    }

    private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {
        ItemAnimatorRestoreListener() {
        }

        @Override
        public void onAnimationFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.mShadowedHolder != null && item.mShadowingHolder == null) {
                item.mShadowedHolder = null;
            }
            item.mShadowingHolder = null;
            if (!item.shouldBeKeptAsChild()) {
                if (!removeAnimatingView(item.itemView) && item.isTmpDetached()) {
                    removeDetachedView(item.itemView, false);
                }
            }
        }
    }

    @SuppressWarnings("UnusedParameters")
    public abstract static class ItemAnimator {
        public static final int FLAG_CHANGED = ViewHolder.FLAG_UPDATE;
        public static final int FLAG_REMOVED = ViewHolder.FLAG_REMOVED;
        public static final int FLAG_INVALIDATED = ViewHolder.FLAG_INVALID;
        public static final int FLAG_MOVED = ViewHolder.FLAG_MOVED;
        public static final int FLAG_APPEARED_IN_PRE_LAYOUT = ViewHolder.FLAG_APPEARED_IN_PRE_LAYOUT;
        @IntDef(flag = true, value = {FLAG_CHANGED, FLAG_REMOVED, FLAG_MOVED, FLAG_INVALIDATED, FLAG_APPEARED_IN_PRE_LAYOUT})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AdapterChanges {
        }
        private ItemAnimatorListener mListener = null;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList<ItemAnimatorFinishedListener>();
        private View mHostView = null;
        private long mAddDuration = 120;
        private long mRemoveDuration = 120;
        private long mMoveDuration = 250;
        private long mChangeDuration = 250;

        public long getMoveDuration() {
            return mMoveDuration;
        }

        public void setMoveDuration(long moveDuration) {
            mMoveDuration = moveDuration;
        }

        public long getAddDuration() {
            return mAddDuration;
        }

        public void setAddDuration(long addDuration) {
            mAddDuration = addDuration;
        }

        public long getRemoveDuration() {
            return mRemoveDuration;
        }

        public void setRemoveDuration(long removeDuration) {
            mRemoveDuration = removeDuration;
        }

        public long getChangeDuration() {
            return mChangeDuration;
        }

        public void setChangeDuration(long changeDuration) {
            mChangeDuration = changeDuration;
        }

        void setListener(ItemAnimatorListener listener) {
            mListener = listener;
        }

        public @NonNull ItemHolderInfo recordPreLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder, @AdapterChanges int changeFlags, @NonNull List<Object> payloads) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        public @NonNull ItemHolderInfo recordPostLayoutInformation(@NonNull State state, @NonNull ViewHolder viewHolder) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        public abstract boolean animateDisappearance(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo);

        public abstract boolean animateAppearance(@NonNull ViewHolder viewHolder, @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        public abstract boolean animatePersistence(@NonNull ViewHolder viewHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        public abstract boolean animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder, @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        @AdapterChanges
        static int buildAdapterChangeFlagsForAnimations(ViewHolder viewHolder) {
            int flags = viewHolder.mFlags & (FLAG_INVALIDATED | FLAG_REMOVED | FLAG_CHANGED);
            if (viewHolder.isInvalid()) {
                return FLAG_INVALIDATED;
            }
            if ((flags & FLAG_INVALIDATED) == 0) {
                final int oldPos = viewHolder.getOldPosition();
                final int pos = viewHolder.getAbsoluteAdapterPosition();
                if (oldPos != NO_POSITION && pos != NO_POSITION && oldPos != pos) {
                    flags |= FLAG_MOVED;
                }
            }
            return flags;
        }

        public abstract void runPendingAnimations();

        public abstract void endAnimation(@NonNull ViewHolder item);

        public abstract void endAnimations();

        public abstract boolean isRunning();

        public final void dispatchAnimationFinished(@NonNull ViewHolder viewHolder) {
            onAnimationFinished(viewHolder);
            if (mListener != null) {
                mListener.onAnimationFinished(viewHolder);
            }
        }

        public void onAnimationFinished(@NonNull ViewHolder viewHolder) {
        }

        public final void dispatchAnimationStarted(@NonNull ViewHolder viewHolder) {
            onAnimationStarted(viewHolder);
        }

        public void onAnimationStarted(@NonNull ViewHolder viewHolder) {
        }

        public final boolean isRunning(@Nullable ItemAnimatorFinishedListener listener) {
            boolean running = isRunning();
            if (listener != null) {
                if (!running) {
                    listener.onAnimationsFinished();
                } else {
                    mFinishedListeners.add(listener);
                }
            }
            return running;
        }

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
            return true;
        }

        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder, @NonNull List<Object> payloads) {
            return canReuseUpdatedViewHolder(viewHolder);
        }

        public final void dispatchAnimationsFinished() {
            final int count = mFinishedListeners.size();
            for (int i = 0; i < count; ++i) {
                mFinishedListeners.get(i).onAnimationsFinished();
            }
            mFinishedListeners.clear();
        }

        @NonNull
        public ItemHolderInfo obtainHolderInfo() {
            return new ItemHolderInfo();
        }

        interface ItemAnimatorListener {
            void onAnimationFinished(@NonNull ViewHolder item);
        }

        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        public static class ItemHolderInfo {
            public int left;
            public int top;
            public int right;
            public int bottom;
            @AdapterChanges
            public int changeFlags;

            public ItemHolderInfo() {
            }

            @NonNull
            public ItemHolderInfo setFrom(@NonNull RecyclerView.ViewHolder holder) {
                return setFrom(holder, 0);
            }

            @NonNull
            public ItemHolderInfo setFrom(@NonNull RecyclerView.ViewHolder holder, @AdapterChanges int flags) {
                final View view = holder.itemView;
                this.left = view.getLeft();
                this.top = view.getTop();
                this.right = view.getRight();
                this.bottom = view.getBottom();
                return this;
            }
        }

        void setHostView(View view) {
            mHostView = view;
        }

        public View getHostView() {
            return mHostView;
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mChildDrawingOrderCallback == null) {
            return super.getChildDrawingOrder(childCount, i);
        } else {
            return mChildDrawingOrderCallback.onGetChildDrawingOrder(childCount, i);
        }
    }

    public interface ChildDrawingOrderCallback {
        int onGetChildDrawingOrder(int childCount, int i);
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }

    public void seslSetLastRoundedCorner(boolean draw) {
        mDrawLastRoundedCorner = draw;
    }

    public void seslSetFillBottomEnabled(boolean draw) {
        if (mLayout instanceof LinearLayoutManager) {
            mDrawRect = draw;
            requestLayout();
        }
    }

    public void seslSetFillBottomColor(int color) {
        mRectPaint.setColor(color);
    }

    private void runLastItemAddDeleteAnim(View view) {
        if (mLastItemAddRemoveAnim == null) {
            if ((getItemAnimator() instanceof DefaultItemAnimator) && mLastItemAnimTop == -1) {
                mLastItemAnimTop = ((DefaultItemAnimator) getItemAnimator()).getLastItemBottom();
            }
            if (mIsSetOnlyAddAnim) {
                mLastItemAddRemoveAnim = ValueAnimator.ofInt(mLastItemAnimTop, ((int) view.getY()) + view.getHeight());
            } else if (mIsSetOnlyRemoveAnim) {
                mLastItemAddRemoveAnim = ValueAnimator.ofInt(mLastItemAnimTop, view.getBottom());
            }
            mLastItemAddRemoveAnim.setDuration(LASTITEM_ADD_REMOVE_DURATION);
            mLastItemAddRemoveAnim.addListener(mAnimListener);
            mLastItemAddRemoveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mAnimatedBlackTop = (Integer) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mLastItemAddRemoveAnim.start();
        }
    }

    private int getPendingAnimFlag() {
        if (getItemAnimator() instanceof DefaultItemAnimator) {
            return ((DefaultItemAnimator) getItemAnimator()).getPendingAnimFlag();
        } else {
            return 0;
        }
    }

    private boolean findSuperClass(ViewParent parent, String klass) {
        for (Class<?> cls = parent.getClass(); cls != null; cls = cls.getSuperclass()) {
            if (cls.getSimpleName().equals(klass)) {
                return true;
            }
        }
        return false;
    }

    private void adjustNestedScrollRange() {
        getLocationInWindow(mWindowOffsets);
        mRemainNestedScrollRange = mNestedScrollRange - (mInitialTopOffsetOfScreen - mWindowOffsets[1]);
        if (mInitialTopOffsetOfScreen - mWindowOffsets[1] < 0) {
            mNestedScrollRange = mRemainNestedScrollRange;
            mInitialTopOffsetOfScreen = mWindowOffsets[1];
        }
    }

    private void adjustNestedScrollRangeBy(int y) {
        if (!mHasNestedScrollRange) {
            return;
        }
        if (!canScrollUp() || mRemainNestedScrollRange != 0) {
            mRemainNestedScrollRange = mRemainNestedScrollRange - y;
            if (mRemainNestedScrollRange < 0) {
                mRemainNestedScrollRange = 0;
                return;
            }
            if (mRemainNestedScrollRange > mNestedScrollRange) {
                mRemainNestedScrollRange = mNestedScrollRange;
            }
        }
    }

    @Override
    public boolean verifyDrawable(Drawable drawable) {
        return mGoToTopImage == drawable || super.verifyDrawable(drawable);
    }

    private boolean isTalkBackIsRunning() {
        AccessibilityManager accessibilty = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        String services = Settings.Secure.getString(getContext().getContentResolver(), "enabled_accessibility_services");

        if (accessibilty != null && accessibilty.isEnabled() && services != null) {
            return services.matches("(?i).*com.samsung.accessibility/com.samsung.android.app.talkback.TalkBackService.*")
                    ||services.matches("(?i).*com.samsung.android.accessibility.talkback/com.samsung.android.marvin.talkback.TalkBackService.*")
                    || services.matches("(?i).*com.google.android.marvin.talkback.TalkBackService.*")
                    || services.matches("(?i).*com.samsung.accessibility/com.samsung.accessibility.universalswitch.UniversalSwitchService.*");
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if ((keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && event.getAction() == KeyEvent.ACTION_DOWN) {
            mIsArrowKeyPressed = true;
        }
        return super.dispatchKeyEvent(event);
    }

    // kang
    @Override
    public boolean dispatchTouchEvent(MotionEvent var1) {
        if (this.mLayout == null) {
            Log.d("SeslRecyclerView", "No layout manager attached; skipping gototop & multiselection");
            return super.dispatchTouchEvent(var1);
        } else {
            int var2 = var1.getActionMasked();
            int var3 = (int)(var1.getX() + 0.5F);
            int var4 = (int)(var1.getY() + 0.5F);
            if (this.mPenDragSelectedItemArray == null) {
                this.mPenDragSelectedItemArray = new ArrayList();
            }

            int var5;
            int var6;
            int var7;
            if (this.mIsEnabledPaddingInHoverScroll) {
                var5 = this.mListPadding.top;
                var6 = this.getHeight();
                var7 = this.mListPadding.bottom;
                var6 -= var7;
                var7 = var5;
                var5 = var6;
            } else {
                var5 = this.getHeight();
                var7 = 0;
            }

            boolean var8;
            if (this.mIsPenSelectionEnabled && !SeslTextViewReflector.semIsTextSelectionProgressing()) {
                var8 = true;
            } else {
                var8 = false;
            }

            this.mIsNeedPenSelection = var8;
            if (var2 != 0) {
                label162: {
                    if (var2 != 1) {
                        if (var2 == 2) {
                            if (this.mIsCtrlMultiSelection) {
                                this.multiSelection(var3, var4, var7, var5, false);
                                return true;
                            }

                            if (this.mIsLongPressMultiSelection) {
                                this.updateLongPressMultiSelection(var3, var4, true);
                                return true;
                            }

                            if (this.isSupportGotoTop() && this.mGoToTopState == 2) {
                                if (!this.mGoToTopRect.contains(var3, var4)) {
                                    this.mGoToTopState = 1;
                                    this.mGoToTopView.setPressed(false);
                                    this.autoHide(1);
                                    this.mGoToTopMoved = true;
                                }

                                return true;
                            }

                            return super.dispatchTouchEvent(var1);
                        }

                        if (var2 != 3) {
                            switch(var2) {
                                case 211:
                                    if (this.mPenDragSelectedItemArray == null) {
                                        this.mPenDragSelectedItemArray = new ArrayList();
                                    }

                                    return super.dispatchTouchEvent(var1);
                                case MOTION_EVENT_ACTION_PEN_UP:
                                    break label162;
                                case MOTION_EVENT_ACTION_PEN_MOVE:
                                    this.multiSelection(var3, var4, var7, var5, false);
                                    return super.dispatchTouchEvent(var1);
                                default:
                                    return super.dispatchTouchEvent(var1);
                            }
                        }

                        if (this.isSupportGotoTop()) {
                            var5 = this.mGoToTopState;
                            if (var5 != 0) {
                                if (var5 == 2) {
                                    this.mGoToTopState = 1;
                                }

                                this.mGoToTopView.setPressed(false);
                            }
                        }
                    }

                    if (this.mIsCtrlMultiSelection) {
                        this.multiSelectionEnd(var3, var4);
                        this.mIsCtrlMultiSelection = false;
                        return true;
                    }

                    if (this.mIsLongPressMultiSelection) {
                        de.dlyt.yanndroid.oneui.view.RecyclerView.SeslLongPressMultiSelectionListener var9 = this.mLongPressMultiSelectionListener;
                        if (var9 != null) {
                            var9.onLongPressMultiSelectionEnded(var3, var4);
                        }

                        this.mIsFirstMultiSelectionMove = true;
                        this.mPenDragSelectedViewPosition = -1;
                        this.mPenDragStartX = 0;
                        this.mPenDragStartY = 0;
                        this.mPenDragEndX = 0;
                        this.mPenDragEndY = 0;
                        this.mPenDragBlockLeft = 0;
                        this.mPenDragBlockTop = 0;
                        this.mPenDragBlockRight = 0;
                        this.mPenDragBlockBottom = 0;
                        this.mPenDragSelectedItemArray.clear();
                        this.mPenTrackedChild = null;
                        this.mPenDistanceFromTrackedChildTop = 0;
                        if (this.mHoverHandler.hasMessages(0)) {
                            this.mHoverHandler.removeMessages(0);
                            if (this.mScrollState == 1) {
                                this.setScrollState(0);
                            }
                        }

                        this.mIsHoverOverscrolled = false;
                        this.invalidate();
                        this.mIsLongPressMultiSelection = false;
                    }
                }

                if (this.isSupportGotoTop() && this.mGoToTopState == 2) {
                    if (this.canScrollUp()) {
                        de.dlyt.yanndroid.oneui.view.RecyclerView.SeslOnGoToTopClickListener var10 = this.mOnGoToTopClickListener;
                        if (var10 != null && var10.onGoToTopClick(this)) {
                            return true;
                        }

                        Log.d("SeslRecyclerView", " can scroll top ");
                        var6 = this.getChildCount();
                        if (this.computeVerticalScrollOffset() != 0) {
                            this.stopScroll();
                            de.dlyt.yanndroid.oneui.view.RecyclerView.LayoutManager var11 = this.mLayout;
                            if (var11 instanceof de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager) {
                                ((de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager)var11).scrollToPositionWithOffset(0, 0);
                            } else {
                                this.mGoToToping = true;
                                if (var6 > 0 && var6 < this.findFirstVisibleItemPosition()) {
                                    var11 = this.mLayout;
                                    if (var11 instanceof de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager) {
                                        var5 = var6;
                                        if (var11 instanceof de.dlyt.yanndroid.oneui.sesl.recyclerview.GridLayoutManager) {
                                            var7 = ((GridLayoutManager)var11).getSpanCount();
                                            var5 = var6;
                                            if (var6 < var7) {
                                                var5 = var7;
                                            }
                                        }

                                        ((de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager)this.mLayout).scrollToPositionWithOffset(var5, 0);
                                    } else {
                                        this.scrollToPosition(var6);
                                    }
                                }

                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        smoothScrollToPosition(0);
                                    }
                                });
                            }
                        }
                    }

                    this.autoHide(0);
                    this.playSoundEffect(0);
                    return true;
                }

                if (this.mGoToTopMoved) {
                    this.mGoToTopMoved = false;
                    VelocityTracker var12 = this.mVelocityTracker;
                    if (var12 != null) {
                        var12.clear();
                    }
                }

                this.multiSelectionEnd(var3, var4);
            } else {
                if (this.isSupportGotoTop()) {
                    this.mGoToTopMoved = false;
                    this.mGoToToping = false;
                }

                if (this.isSupportGotoTop() && this.mGoToTopState != 2 && this.mGoToTopRect.contains(var3, var4)) {
                    this.setupGoToTop(2);
                    this.mGoToTopView.setPressed(true);
                    return true;
                }

                if (this.mIsCtrlKeyPressed && var1.getToolType(0) == 3) {
                    this.mIsCtrlMultiSelection = true;
                    this.mIsNeedPenSelection = true;
                    this.multiSelection(var3, var4, var7, var5, false);
                    return true;
                }

                if (this.mIsLongPressMultiSelection) {
                    this.mIsLongPressMultiSelection = false;
                }
            }

            return super.dispatchTouchEvent(var1);
        }
    }
    // kang

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        final int count = mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            mItemDecorations.get(i).seslOnDispatchDraw(canvas, this, mState);
        }

        if (mDrawRect && (mBlackTop != -1 || mLastBlackTop != -1) && !canScrollVertically(-1) && !canScrollVertically(1) || isAnimating()) {
            if (mLastItemAddRemoveAnim == null || !mLastItemAddRemoveAnim.isRunning()) {
                mAnimatedBlackTop = mBlackTop;
            }

            if (isAnimating()) {
                final int pendingAnimFlag = getPendingAnimFlag();
                if (pendingAnimFlag == ViewHolder.FLAG_REMOVED) {
                    mIsSetOnlyAddAnim = true;
                } else if (pendingAnimFlag == ViewHolder.FLAG_BOUND) {
                    mIsSetOnlyRemoveAnim = true;
                }

                final View child;
                if (mDrawReverse) {
                    child = mBlackTop != -1 ?
                            mChildHelper.getChildAt(0) : getChildAt(0);
                } else if (mBlackTop != -1) {
                    child = mChildHelper.getChildAt(mChildHelper.getChildCount() - 1);
                } else {
                    child = getChildAt(getChildCount() - 1);
                }

                if (child != null) {
                    if (mIsSetOnlyAddAnim || mIsSetOnlyRemoveAnim) {
                        runLastItemAddDeleteAnim(child);
                    } else {
                        mAnimatedBlackTop = Math.round(child.getY()) + child.getHeight();
                    }
                }
                invalidate();
            }

            if (!(mBlackTop == -1 && mAnimatedBlackTop == mBlackTop && !mIsSetOnlyAddAnim)) {
                canvas.drawRect(0.0f, (float) mAnimatedBlackTop, (float) getWidth(), (float) getBottom(), mRectPaint);
                if (mDrawLastRoundedCorner) {
                    mRoundedCorner.drawRoundedCorner(0, mAnimatedBlackTop, getWidth(), getBottom(), canvas);
                }
            }
        }

        mLastItemAnimTop = mBlackTop;
    }

    public void seslSetFastScrollerEnabled(boolean enabled) {
        if (mLayout instanceof StaggeredGridLayoutManager) {
            Log.e(TAG, "FastScroller cannot be used with StaggeredGridLayoutManager.");
            return;
        }

        if (mFastScroller != null) {
            mFastScroller.setEnabled(enabled);
        } else if (enabled) {
            mFastScroller = new SeslRecyclerViewFastScroller(this);
            mFastScroller.setEnabled(true);
            mFastScroller.setScrollbarPosition(getVerticalScrollbarPosition());
        }

        mFastScrollerEnabled = enabled;

        if (mFastScroller != null) {
            mFastScroller.updateLayout();
        }
    }

    public boolean seslIsFastScrollerEnabled() {
        return mFastScrollerEnabled;
    }

    public void seslSetFastScrollerThreshold(float threshold) {
        if (mFastScroller != null && threshold >= 0.0f) {
            mFastScroller.setThreshold(threshold);
        }
    }

    @Override
    public boolean isVerticalScrollBarEnabled() {
        return !mFastScrollerEnabled && super.isVerticalScrollBarEnabled();
    }

    public boolean isInScrollingContainer() {
        for (ViewParent parent = getParent(); parent != null && (parent instanceof ViewGroup); parent = parent.getParent()) {
            if (((ViewGroup) parent).shouldDelayChildPressedState()) {
                return true;
            }
        }
        return false;
    }

    public void seslSetFastScrollerEventListener(SeslFastScrollerEventListener listener) {
        mFastScrollerEventListener = listener;
    }

    public void seslSetGoToTopEnabled(boolean enabled) {
        mGoToTopImage = mGoToTopImageLight != null ?
                mGoToTopImageLight : mContext.getResources().getDrawable(mIsOneUI4 ? R.drawable.sesl4_list_go_to_top : R.drawable.sesl_list_go_to_top);

        if (mGoToTopImage != null) {
            if (enabled) {
                if (mGoToTopView == null) {
                    mGoToTopView = new ImageView(mContext);

                    if (Build.VERSION.SDK_INT >= 26) {
                        mGoToTopView.setBackground(mContext.getResources().getDrawable(R.drawable.sesl_go_to_top_background));
                        mGoToTopView.setElevation(mGoToTopElevation);
                    }

                    mGoToTopView.setImageDrawable(mGoToTopImage);
                }

                mGoToTopView.setAlpha(0.0f);
                if (!mEnableGoToTop) {
                    getOverlay().add(mGoToTopView);
                }
            } else if (mEnableGoToTop) {
                getOverlay().remove(mGoToTopView);
            }

            mEnableGoToTop = enabled;

            mGoToTopFadeInAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mGoToTopFadeInAnimator.setDuration(333L);
            mGoToTopFadeInAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);
            mGoToTopFadeInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    try {
                        mGoToTopView.setAlpha((Float) animator.getAnimatedValue());
                    } catch (Exception unused) { }
                }
            });
            mGoToTopFadeOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            mGoToTopFadeOutAnimator.setDuration(150L);
            mGoToTopFadeOutAnimator.setInterpolator(LINEAR_INTERPOLATOR);
            mGoToTopFadeOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    try {
                        mGoToTopView.setAlpha((Float) animator.getAnimatedValue());
                    } catch (Exception unused) { }
                }
            });
            mGoToTopFadeOutAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    try {
                        mShowFadeOutGTP = GTP_STATE_SHOWN;
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    try {
                        mShowFadeOutGTP = GTP_STATE_PRESSED;
                        setupGoToTop(GTP_STATE_NONE);
                    } catch (Exception ignored) { }
                }
            });
        }
    }

    public void showGoToTop() {
        if (mEnableGoToTop && canScrollUp() && mGoToTopState != GTP_STATE_PRESSED) {
            setupGoToTop(GTP_STATE_SHOWN);
            autoHide(GTP_STATE_SHOWN);
        }
    }

    private boolean isSupportGotoTop() {
        return !isTalkBackIsRunning() && mEnableGoToTop;
    }

    void playGotoToFadeOut() {
        if (!mGoToTopFadeOutAnimator.isRunning()) {
            if (mGoToTopFadeInAnimator.isRunning()) {
                mGoToTopFadeOutAnimator.cancel();
            }
            mGoToTopFadeOutAnimator.setFloatValues(mGoToTopView.getAlpha(), 0.0f);
            mGoToTopFadeOutAnimator.start();
        }
    }

    void playGotoToFadeIn() {
        if (!mGoToTopFadeInAnimator.isRunning()) {
            if (mGoToTopFadeOutAnimator.isRunning()) {
                mGoToTopFadeOutAnimator.cancel();
            }
            mGoToTopFadeInAnimator.setFloatValues(mGoToTopView.getAlpha(), 1.0f);
            mGoToTopFadeInAnimator.start();
        }
    }

    void autoHide(int state) {
        if (mEnableGoToTop) {
            if (state == GTP_STATE_NONE) {
                if (!seslIsFastScrollerEnabled()) {
                    removeCallbacks(mAutoHide);
                    postDelayed(mAutoHide, GO_TO_TOP_HIDE);
                }
            } else if (state == GTP_STATE_SHOWN) {
                removeCallbacks(mAutoHide);
                postDelayed(mAutoHide, GO_TO_TOP_HIDE);
            }
        }
    }

    void setupGoToTop(int state) {
        if (mEnableGoToTop) {
            removeCallbacks(mAutoHide);
            if (state == GTP_STATE_SHOWN && !canScrollUp()) {
                state = GTP_STATE_NONE;
            }

            if (state != -1 || !mSizeChnage) {
                if (state == -1 && (canScrollUp() || canScrollDown())) {
                    state = GTP_STATE_SHOWN;
                }
            } else if (canScrollUp() || canScrollDown()) {
                state = mGoToTopLastState;
            } else {
                state = GTP_STATE_NONE;
            }

            if (state != GTP_STATE_NONE) {
                removeCallbacks(mGoToToFadeOutRunnable);
            } else if (state != GTP_STATE_SHOWN) {
                removeCallbacks(mGoToToFadeInRunnable);
            }

            if (mShowFadeOutGTP == GTP_STATE_NONE && state == GTP_STATE_NONE && mGoToTopLastState != GTP_STATE_NONE) {
                post(mGoToToFadeOutRunnable);
            }

            if (state != GTP_STATE_PRESSED) {
                mGoToTopView.setPressed(false);
            }

            mGoToTopState = state;

            int padding = getPaddingLeft() + (((getWidth() - getPaddingLeft()) - getPaddingRight()) / 2);
            if (state != GTP_STATE_NONE) {
                if (state == GTP_STATE_SHOWN || state == GTP_STATE_PRESSED) {
                    removeCallbacks(mGoToToFadeOutRunnable);
                    mGoToTopRect.set(padding - (mGoToTopSize / 2), ((getHeight() - mGoToTopSize) - mGoToTopBottomPadding) - mGoToTopImmersiveBottomPadding, padding + (mGoToTopSize / 2), (getHeight() - mGoToTopBottomPadding) - mGoToTopImmersiveBottomPadding);
                }
            } else if (mShowFadeOutGTP == GTP_STATE_PRESSED) {
                mGoToTopRect.set(0, 0, 0, 0);
            }

            if (mShowFadeOutGTP == GTP_STATE_PRESSED) {
                mShowFadeOutGTP = GTP_STATE_NONE;
            }

            mGoToTopView.layout(mGoToTopRect.left, mGoToTopRect.top, mGoToTopRect.right, mGoToTopRect.bottom);

            if (state == GTP_STATE_SHOWN && (mGoToTopLastState == GTP_STATE_NONE || mGoToTopView.getAlpha() == 0.0f || mSizeChnage)) {
                post(mGoToToFadeInRunnable);
            }
            mSizeChnage = false;

            mGoToTopLastState = mGoToTopState;
        }
    }

    private void drawGoToTop() {
        mGoToTopView.setTranslationY((float) getScrollY());
        if (mGoToTopState != GTP_STATE_NONE && !canScrollUp()) {
            setupGoToTop(GTP_STATE_NONE);
        }
    }

    public int seslGetHoverBottomPadding() {
        return mHoverBottomAreaHeight;
    }

    public void seslSetHoverBottomPadding(int bottom) {
        mHoverBottomAreaHeight = bottom;
    }

    public int seslGetHoverTopPadding() {
        return mHoverTopAreaHeight;
    }

    public void seslSetHoverTopPadding(int top) {
        mHoverTopAreaHeight = top;
    }

    public int seslGetGoToTopBottomPadding() {
        return mGoToTopBottomPadding;
    }

    public void seslSetGoToTopBottomPadding(int bottom) {
        mGoToTopBottomPadding = bottom;
    }

    public void seslSetOnGoToTopClickListener(SeslOnGoToTopClickListener listener) {
        mOnGoToTopClickListener = listener;
    }

    public void seslShowGoToTopEdge(int delayMillis) {
        removeCallbacks(mGoToTopEdgeEffectRunnable);
        postDelayed(mGoToTopEdgeEffectRunnable, (long) delayMillis);
    }

    public void seslSetImmersiveScrollBottomPadding(int bottom) {
        if (bottom >= 0) {
            if (mEnableGoToTop) {
                int immersiveBottom = ((getHeight() - mGoToTopSize) - mGoToTopBottomPadding) - bottom;
                if (immersiveBottom < 0) {
                    mGoToTopImmersiveBottomPadding = 0;
                    Log.e(TAG, "The Immersive padding value (" + bottom + ") was too large to draw GoToTop.");
                    return;
                }
                mGoToTopImmersiveBottomPadding = bottom;

                if (mGoToTopState != GTP_STATE_NONE) {
                    int padding = getPaddingLeft() + (((getWidth() - getPaddingLeft()) - getPaddingRight()) / 2);
                    mGoToTopRect.set(padding - (mGoToTopSize / 2), immersiveBottom, padding + (mGoToTopSize / 2), mGoToTopSize + immersiveBottom);
                    mGoToTopView.layout(mGoToTopRect.left, mGoToTopRect.top, mGoToTopRect.right, mGoToTopRect.bottom);
                }
            }

            if (mFastScroller != null && mAdapter != null) {
                mFastScroller.setImmersiveBottomPadding(bottom);
            }
        }
    }

    public void seslSetIndexTipEnabled(boolean enabled) {
        if (mAdapter instanceof SectionIndexer) {
            if (enabled) {
                if (mIndexTip == null) {
                    mIndexTip = new IndexTip(mContext);
                } else {
                    mIndexTip.hide();
                }

                if (!mIndexTipEnabled) {
                    getOverlay().add(mIndexTip);
                }
                mIndexTip.setLayout(0, 0, getRight(), getBottom(), getPaddingLeft(), getPaddingRight());
            } else if (mIndexTipEnabled) {
                getOverlay().remove(mIndexTip);
            }
            mIndexTipEnabled = enabled;
        } else {
            throw new IllegalStateException("In order to use Index Tip, your Adapter has to implements SectionIndexer. or check if setAdapter is preceded.");
        }
    }

    public boolean seslIsIndexTipEnabled() {
        return mIndexTipEnabled;
    }

    public void seslUpdateIndexTipPosition() {
        if (mIndexTip == null) {
            return;
        }

        if (mIndexTip.mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mIndexTip.mIsNeedUpdate = true;
            mIndexTip.invalidate();
            return;
        }
        mIndexTip.mIsNeedUpdate = false;
    }

    private int getRecyclerViewScreenLocationY() {
        getLocationOnScreen(mRecyclerViewOffsets);
        return mRecyclerViewOffsets[1];
    }

    class IndexTip extends View {
        private static final int ALPHA_DURATION = 150;
        private static final int CHANGE_TEXT_DELAY = 90;
        private static final int FADE_DURATION = 300;
        private static final int SCALE_DURATION = 200;
        private static final float SHAPE_COLOR_ALPHA_RATIO = 0.9f;
        private final PathInterpolator ALPHA_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f);
        private final PathInterpolator SCALE_INTERPOLATOR = new PathInterpolator(0.22f, 0.25f, 0.0f, 1.0f);
        private float mAnimatingWidth;
        private int mCenterX;
        private int mCenterY;
        private Context mContext;
        private int mCurrentOrientation;
        private boolean mForcedHide = false;
        private int mHeight;
        private boolean mIsNeedUpdate = false;
        private boolean mIsShowing = false;
        private int mMaxWidth;
        private int mMinWidth;
        private int mParentPosY;
        private String mPrevText;
        private float mPrevWidth;
        private float mRadius;
        private SectionIndexer mSectionIndexer;
        private Object[] mSections;
        private Paint mShapePaint;
        private String mShowingText;
        private int mSidePadding;
        private int mStatusBarHeight;
        private String mTargetText;
        private String mText;
        private Rect mTextBounds;
        private Paint mTextPaint;
        private int mTopMargin;
        private ValueAnimator mValueAnimator;

        private final Runnable mShapeDelayRunnable = new Runnable() {
            @Override
            public void run() {
                if (RecyclerView.this.mIndexTip != null && mIsShowing) {
                    startAnimation();
                    mIsShowing = false;
                }
            }
        };

        private final Runnable mTextDelayRunnable = new Runnable() {
            @Override
            public void run() {
                if (RecyclerView.this.mIndexTip != null) {
                    mShowingText = mTargetText;
                    invalidate();
                }
            }
        };

        private IndexTip(Context context) {
            super(context);
            mContext = context;
            init();
        }

        private void init() {
            mSectionIndexer = (SectionIndexer) RecyclerView.this.mAdapter;
            updateSections();
            
            mShapePaint = new Paint();
            mShapePaint.setStyle(Paint.Style.FILL);
            mShapePaint.setAntiAlias(true);
            mShapePaint.setColor(getColorWithAlpha(mContext.getResources().getColor(R.color.sesl_scrollbar_index_tip_color), SHAPE_COLOR_ALPHA_RATIO));
            
            mTextPaint = new Paint();
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTypeface(Typeface.create(mContext.getString(R.string.sesl_font_family_regular), Typeface.NORMAL));
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setTextSize(mContext.getResources().getDimension(R.dimen.sesl_index_tip_text_size));
            mTextPaint.setColor(mContext.getResources().getColor(R.color.sesl_white));
            
            mTextBounds = new Rect();

            mText = "";
            mShowingText = "";
            mPrevText = "";

            mPrevWidth = 0.0f;
            mAnimatingWidth = 0.0f;

            mHeight = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_index_tip_height);
            mSidePadding = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_index_tip_padding);
            mMinWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_index_tip_min_width);
            mMaxWidth = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_index_tip_max_width);
            mTopMargin = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_index_tip_margin_top);
            mRadius = mContext.getResources().getDimension(R.dimen.sesl_index_tip_radius);
            mCenterY = mTopMargin + Math.round(((float) mHeight) / 2.0f);
            mParentPosY = 0;
            
            int resId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resId > 0) {
                mStatusBarHeight = mContext.getResources().getDimensionPixelSize(resId);
            } else {
                mStatusBarHeight = 0;
            }
            
            setAlpha(0.0f);
        }

        private void setLayout(int l, int t, int r, int b, int left, int right) {
            layout(l, t, r, b);

            mCenterX = left + Math.round(((float) (((r - l) - left) - right)) / 2.0f);
            mCurrentOrientation = mContext.getResources().getConfiguration().orientation;
            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mIsNeedUpdate = false;
            }
            hide();
        }

        private int getColorWithAlpha(int color, float ratio) {
            int newColor = 0;
            int alpha = Math.round(Color.alpha(color) * ratio);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            newColor = Color.argb(alpha, r, g, b);
            return newColor;
        }

        private void updateSections() {
            if (mSectionIndexer != null) {
                mSections = mSectionIndexer.getSections();
                if (mSections != null) {
                    hide();
                    return;
                }
                throw new IllegalStateException("Section is null. This array, or its contents should be non-null");
            }
        }

        private void updateText() {
            mText = "";

            final int firstVisibleItemPosition = RecyclerView.this.findFirstVisibleItemPosition();
            if (firstVisibleItemPosition == -1) {
                Log.e(RecyclerView.TAG, "First visible item was null.");
                return;
            }
            int sectionPos = mSectionIndexer.getSectionForPosition(firstVisibleItemPosition);
            if (sectionPos >= 0) {
                if (sectionPos < mSections.length && mSections[sectionPos] != null) {
                    mText = mSections[sectionPos].toString();
                }
            }
        }

        // kang
        private void subString(String str) {
            int length = str.length();
            do {
                length--;
                if (length > 0) {
                    str = str.substring(0, length) + "...";
                } else {
                    return;
                }
            } while ((this.mTextPaint.measureText(str) / 2.0f) + ((float) mSidePadding) >= ((float) mMaxWidth));
            mText = str;
        }
        // kang

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            updateText();

            if (mShowingText.equals("")) {
                mShowingText = mText;
                mTargetText = mText;
            }

            if (mText.equals("")) {
                if (mPrevText.equals("")) {
                    return;
                }

                if (!mForcedHide && mIsShowing) {
                    startAnimation();
                    mIsShowing = false;
                    mForcedHide = true;
                }

                mText = mPrevText;
            } else {
                mForcedHide = false;
            }

            float textWidth = (mTextPaint.measureText(mText) / 2.0f) + ((float) mSidePadding);
            if (textWidth < ((float) mMinWidth)) {
                textWidth = (float) mMinWidth;
            } else if (textWidth > ((float) mMaxWidth)) {
                subString(mText);
                textWidth = (float) mMaxWidth;
            }
            if (((float) mCenterX) < textWidth) {
                textWidth = (float) mCenterX;
            }

            if (mPrevWidth != 0.0f && mPrevWidth != textWidth) {
                animating(textWidth);
            }
            if (mAnimatingWidth == 0.0f) {
                mAnimatingWidth = textWidth;
            }

            int y = mStatusBarHeight;
            if (mIsNeedUpdate) {
                mParentPosY = RecyclerView.this.getRecyclerViewScreenLocationY();
                if (mParentPosY < mStatusBarHeight) {
                    y -= mParentPosY;
                }
            }

            canvas.drawRoundRect(((float) mCenterX) - mAnimatingWidth, (float) (mTopMargin + y), ((float) mCenterX) + mAnimatingWidth, (float) (mTopMargin + mHeight + y), mRadius, mRadius, mShapePaint);
            mTextPaint.getTextBounds(mShowingText, 0, mShowingText.length() - 1, mTextBounds);
            canvas.drawText(mShowingText, (float) mCenterX, (((float) mCenterY) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2.0f)) + ((float) y), mTextPaint);

            if (!mText.equals(mTargetText)) {
                if (mText.length() > mTargetText.length()) {
                    changeText();
                } else {
                    mTargetText = mText;
                    mShowingText = mText;
                }
            }

            if (mText.equals(mPrevText)) {
                mPrevText = mText;
                mPrevWidth = textWidth;
            }
        }

        private void changeText() {
            mTargetText = mText;
            removeCallbacks(mTextDelayRunnable);
            postDelayed(mTextDelayRunnable, CHANGE_TEXT_DELAY);
        }

        private void animating(float newWidth) {
            if (mValueAnimator != null) {
                mValueAnimator.cancel();
            }
            mValueAnimator = ValueAnimator.ofFloat(mAnimatingWidth, newWidth);
            mValueAnimator.setDuration(SCALE_DURATION);
            mValueAnimator.setInterpolator(SCALE_INTERPOLATOR);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mAnimatingWidth = (Float) animator.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.start();
        }

        private void show(int state, int vresult) {
            if (state == RecyclerView.SCROLL_STATE_DRAGGING && RecyclerView.this.mRemainNestedScrollRange != 0 && vresult >= 0) {
                RecyclerView.this.adjustNestedScrollRange();
            } else if (vresult != 0 && !mIsShowing && RecyclerView.this.canScrollUp() && !RecyclerView.this.mGoToToping && !mForcedHide) {
                startAnimation();
                mIsShowing = true;
            }
        }

        private void hide() {
            if (mIsShowing) {
                removeCallbacks(mShapeDelayRunnable);
                postDelayed(mShapeDelayRunnable, FADE_DURATION);
                return;
            }
            forcedHide();
        }

        private void forcedHide() {
            mIsShowing = false;
            removeCallbacks(mShapeDelayRunnable);
            setAlpha(0.0f);
            invalidate();
        }

        private void startAnimation() {
            ObjectAnimator animator;
            if (mIsShowing) {
                animator = ObjectAnimator.ofFloat(RecyclerView.this.mIndexTip, "alpha", RecyclerView.this.mIndexTip.getAlpha(), 0.0f);
            } else {
                animator = ObjectAnimator.ofFloat(RecyclerView.this.mIndexTip, "alpha", RecyclerView.this.mIndexTip.getAlpha(), 1.0f);
            }
            animator.setDuration(ALPHA_DURATION);
            animator.setInterpolator(ALPHA_INTERPOLATOR);

            AnimatorSet set = new AnimatorSet();
            set.play(animator);
            set.start();
        }
    }

    public void seslStartLongPressMultiSelection() {
        mIsLongPressMultiSelection = true;
    }

    public void seslSetCtrlkeyPressed(boolean pressed) {
        mIsCtrlKeyPressed = pressed;
    }

    // kang
    private void updateLongPressMultiSelection(int i, int i2, boolean z) {
        int i3;
        int i4;
        int i5;
        int i6;
        OnScrollListener onScrollListener;
        int childCount = this.mChildHelper.getChildCount();
        if (this.mIsFirstMultiSelectionMove) {
            this.mPenDragStartX = i;
            this.mPenDragStartY = i2;
            float f = (float) i;
            float f2 = (float) i2;
            this.mPenTrackedChild = findChildViewUnder(f, f2);
            if (this.mPenTrackedChild == null) {
                this.mPenTrackedChild = seslFindNearChildViewUnder(f, f2);
                if (this.mPenTrackedChild == null) {
                    Log.e(TAG, "updateLongPressMultiSelection, mPenTrackedChild is NULL");
                    this.mIsFirstMultiSelectionMove = false;
                    return;
                }
            }
            SeslLongPressMultiSelectionListener seslLongPressMultiSelectionListener = this.mLongPressMultiSelectionListener;
            if (seslLongPressMultiSelectionListener != null) {
                seslLongPressMultiSelectionListener.onLongPressMultiSelectionStarted(i, i2);
            }
            this.mPenTrackedChildPosition = getChildLayoutPosition(this.mPenTrackedChild);
            this.mPenDragSelectedViewPosition = this.mPenTrackedChildPosition;
            this.mPenDistanceFromTrackedChildTop = this.mPenDragStartY - this.mPenTrackedChild.getTop();
            this.mIsFirstMultiSelectionMove = false;
        }
        if (this.mIsEnabledPaddingInHoverScroll) {
            int i7 = this.mListPadding.top;
            i4 = getHeight() - this.mListPadding.bottom;
            i3 = i7;
        } else {
            i4 = getHeight();
            i3 = 0;
        }
        this.mPenDragEndX = i;
        this.mPenDragEndY = i2;
        int i8 = this.mPenDragEndY;
        if (i8 < 0) {
            this.mPenDragEndY = 0;
        } else if (i8 > i4) {
            this.mPenDragEndY = i4;
        }
        View findChildViewUnder = findChildViewUnder((float) this.mPenDragEndX, (float) this.mPenDragEndY);
        if (findChildViewUnder == null && (findChildViewUnder = seslFindNearChildViewUnder((float) this.mPenDragEndX, (float) this.mPenDragEndY)) == null) {
            Log.e(TAG, "updateLongPressMultiSelection, touchedView is NULL");
            return;
        }
        int childLayoutPosition = getChildLayoutPosition(findChildViewUnder);
        if (childLayoutPosition != -1) {
            this.mPenDragSelectedViewPosition = childLayoutPosition;
            int i9 = this.mPenTrackedChildPosition;
            int i10 = this.mPenDragSelectedViewPosition;
            if (i9 < i10) {
                i6 = i9;
                i5 = i10;
            } else {
                i5 = i9;
                i6 = i10;
            }
            int i11 = this.mPenDragStartX;
            int i12 = this.mPenDragEndX;
            if (i11 >= i12) {
                i11 = i12;
            }
            this.mPenDragBlockLeft = i11;
            int i13 = this.mPenDragStartY;
            int i14 = this.mPenDragEndY;
            if (i13 >= i14) {
                i13 = i14;
            }
            this.mPenDragBlockTop = i13;
            int i15 = this.mPenDragEndX;
            int i16 = this.mPenDragStartX;
            if (i15 <= i16) {
                i15 = i16;
            }
            this.mPenDragBlockRight = i15;
            int i17 = this.mPenDragEndY;
            int i18 = this.mPenDragStartY;
            if (i17 <= i18) {
                i17 = i18;
            }
            this.mPenDragBlockBottom = i17;
            int i19 = 0;
            while (true) {
                boolean z2 = true;
                if (i19 >= childCount) {
                    break;
                }
                View childAt = getChildAt(i19);
                if (childAt != null) {
                    this.mPenDragSelectedViewPosition = getChildLayoutPosition(childAt);
                    if (childAt.getVisibility() == 0) {
                        int i20 = this.mPenDragSelectedViewPosition;
                        if (i6 > i20 || i20 > i5 || i20 == this.mPenTrackedChildPosition) {
                            z2 = false;
                        }
                        if (z2) {
                            int i21 = this.mPenDragSelectedViewPosition;
                            if (i21 != -1 && !this.mPenDragSelectedItemArray.contains(Integer.valueOf(i21))) {
                                this.mPenDragSelectedItemArray.add(Integer.valueOf(this.mPenDragSelectedViewPosition));
                                SeslLongPressMultiSelectionListener seslLongPressMultiSelectionListener2 = this.mLongPressMultiSelectionListener;
                                if (seslLongPressMultiSelectionListener2 != null) {
                                    seslLongPressMultiSelectionListener2.onItemSelected(this, childAt, this.mPenDragSelectedViewPosition, getChildItemId(childAt));
                                }
                            }
                        } else {
                            int i22 = this.mPenDragSelectedViewPosition;
                            if (i22 != -1 && this.mPenDragSelectedItemArray.contains(Integer.valueOf(i22))) {
                                this.mPenDragSelectedItemArray.remove(Integer.valueOf(this.mPenDragSelectedViewPosition));
                                SeslLongPressMultiSelectionListener seslLongPressMultiSelectionListener3 = this.mLongPressMultiSelectionListener;
                                if (seslLongPressMultiSelectionListener3 != null) {
                                    seslLongPressMultiSelectionListener3.onItemSelected(this, childAt, this.mPenDragSelectedViewPosition, getChildItemId(childAt));
                                }
                            }
                        }
                    }
                }
                i19++;
            }
            int i23 = this.mLastTouchY - i2;
            if (z && Math.abs(i23) >= this.mTouchSlop) {
                if (i2 <= i3 + this.mHoverTopAreaHeight && i23 > 0) {
                    if (!this.mHoverAreaEnter) {
                        this.mHoverAreaEnter = true;
                        this.mHoverScrollStartTime = System.currentTimeMillis();
                        OnScrollListener onScrollListener2 = this.mScrollListener;
                        if (onScrollListener2 != null) {
                            onScrollListener2.onScrollStateChanged(this, 1);
                        }
                    }
                    if (!this.mHoverHandler.hasMessages(0)) {
                        this.mHoverRecognitionStartTime = System.currentTimeMillis();
                        this.mHoverScrollDirection = 2;
                        this.mHoverHandler.sendEmptyMessage(0);
                    }
                } else if (i2 < (i4 - this.mHoverBottomAreaHeight) - this.mRemainNestedScrollRange || i23 >= 0) {
                    if (this.mHoverAreaEnter && (onScrollListener = this.mScrollListener) != null) {
                        onScrollListener.onScrollStateChanged(this, 0);
                    }
                    this.mHoverScrollStartTime = 0;
                    this.mHoverRecognitionStartTime = 0;
                    this.mHoverAreaEnter = false;
                    if (this.mHoverHandler.hasMessages(0)) {
                        this.mHoverHandler.removeMessages(0);
                        if (this.mScrollState == 1) {
                            setScrollState(0);
                        }
                    }
                    this.mIsHoverOverscrolled = false;
                } else {
                    if (!this.mHoverAreaEnter) {
                        this.mHoverAreaEnter = true;
                        this.mHoverScrollStartTime = System.currentTimeMillis();
                        OnScrollListener onScrollListener3 = this.mScrollListener;
                        if (onScrollListener3 != null) {
                            onScrollListener3.onScrollStateChanged(this, 1);
                        }
                    }
                    if (!this.mHoverHandler.hasMessages(0)) {
                        this.mHoverRecognitionStartTime = System.currentTimeMillis();
                        this.mHoverScrollDirection = 1;
                        this.mHoverHandler.sendEmptyMessage(0);
                    }
                }
            }
            invalidate();
            return;
        }
        Log.e(TAG, "touchedPosition is NO_POSITION");
    }

    private void multiSelection(int var1, int var2, int var3, int var4, boolean var5) {
        if (this.mIsNeedPenSelection) {
            de.dlyt.yanndroid.oneui.view.RecyclerView.SeslOnMultiSelectedListener var13;
            if (this.mIsFirstPenMoveEvent) {
                this.mPenDragStartX = var1;
                this.mPenDragStartY = var2;
                this.mIsPenPressed = true;
                float var6 = (float)var1;
                float var7 = (float)var2;
                View var8 = this.findChildViewUnder(var6, var7);
                this.mPenTrackedChild = var8;
                if (var8 == null) {
                    var8 = this.seslFindNearChildViewUnder(var6, var7);
                    this.mPenTrackedChild = var8;
                    if (var8 == null) {
                        Log.e("SeslRecyclerView", "multiSelection, mPenTrackedChild is NULL");
                        this.mIsPenPressed = false;
                        this.mIsFirstPenMoveEvent = false;
                        return;
                    }
                }

                var13 = this.mOnMultiSelectedListener;
                if (var13 != null) {
                    var13.onMultiSelectStart(var1, var2);
                }

                this.mPenTrackedChildPosition = this.getChildLayoutPosition(this.mPenTrackedChild);
                this.mPenDistanceFromTrackedChildTop = this.mPenDragStartY - this.mPenTrackedChild.getTop();
                this.mIsFirstPenMoveEvent = false;
            }

            if (this.mPenDragStartX == 0 && this.mPenDragStartY == 0) {
                this.mPenDragStartX = var1;
                this.mPenDragStartY = var2;
                var13 = this.mOnMultiSelectedListener;
                if (var13 != null) {
                    var13.onMultiSelectStart(var1, var2);
                }

                this.mIsPenPressed = true;
            }

            this.mPenDragEndX = var1;
            this.mPenDragEndY = var2;
            if (var2 < 0) {
                this.mPenDragEndY = 0;
            } else if (var2 > var4) {
                this.mPenDragEndY = var4;
            }

            int var9 = this.mPenDragStartX;
            int var10;
            if (var9 < var1) {
                var10 = var9;
            } else {
                var10 = var1;
            }

            this.mPenDragBlockLeft = var10;
            int var11 = this.mPenDragStartY;
            var10 = this.mPenDragEndY;
            int var12;
            if (var11 < var10) {
                var12 = var11;
            } else {
                var12 = var10;
            }

            this.mPenDragBlockTop = var12;
            if (var1 <= var9) {
                var1 = var9;
            }

            this.mPenDragBlockRight = var1;
            var1 = var11;
            if (var10 > var11) {
                var1 = var10;
            }

            this.mPenDragBlockBottom = var1;
            var5 = true;
        }

        if (var5) {
            de.dlyt.yanndroid.oneui.view.RecyclerView.OnScrollListener var14;
            if (var2 <= var3 + this.mHoverTopAreaHeight) {
                if (!this.mHoverAreaEnter) {
                    this.mHoverAreaEnter = true;
                    this.mHoverScrollStartTime = System.currentTimeMillis();
                    var14 = this.mScrollListener;
                    if (var14 != null) {
                        var14.onScrollStateChanged(this, 1);
                    }
                }

                if (!this.mHoverHandler.hasMessages(0)) {
                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                    this.mHoverScrollDirection = 2;
                    this.mHoverHandler.sendEmptyMessage(0);
                }
            } else if (var2 >= var4 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange) {
                if (!this.mHoverAreaEnter) {
                    this.mHoverAreaEnter = true;
                    this.mHoverScrollStartTime = System.currentTimeMillis();
                    var14 = this.mScrollListener;
                    if (var14 != null) {
                        var14.onScrollStateChanged(this, 1);
                    }
                }

                if (!this.mHoverHandler.hasMessages(0)) {
                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                    this.mHoverScrollDirection = 1;
                    this.mHoverHandler.sendEmptyMessage(0);
                }
            } else {
                if (this.mHoverAreaEnter) {
                    var14 = this.mScrollListener;
                    if (var14 != null) {
                        var14.onScrollStateChanged(this, 0);
                    }
                }

                this.mHoverScrollStartTime = 0L;
                this.mHoverRecognitionStartTime = 0L;
                this.mHoverAreaEnter = false;
                if (this.mHoverHandler.hasMessages(0)) {
                    this.mHoverHandler.removeMessages(0);
                    if (this.mScrollState == 1) {
                        this.setScrollState(0);
                    }
                }

                this.mIsHoverOverscrolled = false;
            }

            if (this.mIsPenDragBlockEnabled) {
                this.invalidate();
            }
        }

    }
    // kang

    private void multiSelectionEnd(int i, int i2) {
        if (mIsPenPressed && mOnMultiSelectedListener != null) {
            mOnMultiSelectedListener.onMultiSelectStop(i, i2);
        }

        mIsPenPressed = false;
        mIsFirstPenMoveEvent = true;
        mPenDragSelectedViewPosition = -1;
        mPenDragSelectedItemArray.clear();
        mPenDragStartX = 0;
        mPenDragStartY = 0;
        mPenDragEndX = 0;
        mPenDragEndY = 0;
        mPenDragBlockLeft = 0;
        mPenDragBlockTop = 0;
        mPenDragBlockRight = 0;
        mPenDragBlockBottom = 0;
        mPenTrackedChild = null;
        mPenDistanceFromTrackedChildTop = 0;

        if (mIsPenDragBlockEnabled) {
            invalidate();
        }
        if (mHoverHandler.hasMessages(0)) {
            mHoverHandler.removeMessages(0);
        }
    }

    // kang
    public View seslFindNearChildViewUnder(float var1, float var2) {
        int var3 = this.mChildHelper.getChildCount();
        int var4 = (int)(var1 + 0.5F);
        int var5 = (int)(0.5F + var2);
        int var6 = var3 - 1;
        int var7 = 0;
        int var8 = var6;
        int var9 = var5;

        int var10;
        View var11;
        int var12;
        int var13;
        for(var10 = 2147483647; var8 >= 0; var9 = var13) {
            var11 = this.getChildAt(var8);
            var3 = var7;
            var12 = var10;
            var13 = var9;
            if (var11 != null) {
                var3 = (var11.getTop() + var11.getBottom()) / 2;
                if (var7 == var3) {
                    var3 = var7;
                    var12 = var10;
                    var13 = var9;
                } else {
                    var12 = Math.abs(var5 - var3);
                    if (var12 < var10) {
                        var13 = var3;
                    } else {
                        if (!(this.mLayout instanceof de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager)) {
                            break;
                        }

                        var13 = var9;
                        var12 = var10;
                    }
                }
            }

            --var8;
            var7 = var3;
            var10 = var12;
        }

        var10 = -1;
        var8 = 2147483647;
        var12 = var8;
        var7 = -1;
        var3 = var6;

        int var18;
        while(true) {
            if (var3 < 0) {
                Log.e("SeslRecyclerView", "findNearChildViewUnder didn't find valid child view! " + var1 + ", " + var2);
                return null;
            }

            var11 = this.getChildAt(var3);
            int var14 = var10;
            int var15 = var7;
            int var16 = var8;
            int var17 = var12;
            if (var11 != null) {
                var16 = var11.getTop();
                var15 = var11.getBottom();
                var17 = var11.getLeft();
                var14 = var11.getRight();
                var5 = var10;
                var18 = var7;
                var6 = var8;
                var13 = var12;
                if (var9 >= var16) {
                    var5 = var10;
                    var18 = var7;
                    var6 = var8;
                    var13 = var12;
                    if (var9 <= var15) {
                        var6 = Math.abs(var4 - var17);
                        var16 = Math.abs(var4 - var14);
                        var14 = var10;
                        var10 = var8;
                        if (var6 <= var8) {
                            var14 = var3;
                            var10 = var6;
                        }

                        var5 = var14;
                        var18 = var7;
                        var6 = var10;
                        var13 = var12;
                        if (var16 <= var12) {
                            var18 = var3;
                            var13 = var16;
                            var6 = var10;
                            var5 = var14;
                        }
                    }
                }

                if (var9 > var15) {
                    break;
                }

                var14 = var5;
                var15 = var18;
                var16 = var6;
                var17 = var13;
                if (var3 == 0) {
                    break;
                }
            }

            --var3;
            var10 = var14;
            var7 = var15;
            var8 = var16;
            var12 = var17;
        }

        return var6 < var13 ? this.mChildHelper.getChildAt(var5) : this.mChildHelper.getChildAt(var18);
    }
    // kang

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_PAGE_UP: {
                if (event.hasNoModifiers()) {
                    pageScroll(FOCUS_MOVE_UP);
                }
            }
            break;

            case KeyEvent.KEYCODE_PAGE_DOWN: {
                if (event.hasNoModifiers()) {
                    pageScroll(FOCUS_MOVE_DOWN);
                }
            }
            break;

            case KeyEvent.KEYCODE_CTRL_LEFT:
            case KeyEvent.KEYCODE_CTRL_RIGHT: {
                mIsCtrlKeyPressed = true;
            }
            break;

            case KeyEvent.KEYCODE_MOVE_HOME: {
                if (event.hasNoModifiers()) {
                    pageScroll(FOCUS_MOVE_FULL_UP);
                }
            }
            break;

            case KeyEvent.KEYCODE_MOVE_END: {
                if (event.hasNoModifiers()) {
                    pageScroll(FOCUS_MOVE_FULL_DOWN);
                }
            }
            break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
            mIsCtrlKeyPressed = false;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void seslSetPenSelectionEnabled(boolean enabled) {
        this.mIsPenSelectionEnabled = enabled;
    }

    public void seslSetOnMultiSelectedListener(SeslOnMultiSelectedListener listener) {
        mOnMultiSelectedListener = listener;
    }

    public final SeslOnMultiSelectedListener seslGetOnMultiSelectedListener() {
        return mOnMultiSelectedListener;
    }

    public void seslSetLongPressMultiSelectionListener(SeslLongPressMultiSelectionListener listener) {
        mLongPressMultiSelectionListener = listener;
    }

    public final SeslLongPressMultiSelectionListener getLongPressMultiSelectionListener() {
        return mLongPressMultiSelectionListener;
    }

    private boolean contentFits() {
        final int childCount = getChildCount();
        if (childCount == 0) {
            return true;
        }
        if (childCount != mAdapter.getItemCount()) {
            return false;
        }
        if (getChildAt(0).getTop() < mListPadding.top || getChildAt(childCount - 1).getBottom() > getHeight() - mListPadding.bottom) {
            return false;
        }
        return true;
    }

    private boolean showPointerIcon(MotionEvent event, int type) {
        SeslInputDeviceReflector.semSetPointerType(event.getDevice(), type);
        return true;
    }

    private boolean canScrollUp() {
        if (findFirstChildPosition() > 0 || getChildCount() <= 0) {
            return findFirstChildPosition() > 0;
        }
        return getChildAt(0).getTop() < getPaddingTop();
    }

    // kang
    private boolean canScrollDown() {
        final int childCount = getChildCount();

        if (mAdapter == null) {
            Log.e(TAG, "No adapter attached; skipping canScrollDown");
            return false;
        }
        boolean z = findFirstChildPosition() + childCount < mAdapter.getItemCount();
        if (z || childCount <= 0) {
            return z;
        }
        return getChildAt(childCount - 1).getBottom() > getBottom() - mListPadding.bottom;
    }

    private int findFirstChildPosition() {
        int i;

        if (mLayout instanceof LinearLayoutManager) {
            i = ((LinearLayoutManager) mLayout).findFirstVisibleItemPosition();
        } else if (mLayout instanceof StaggeredGridLayoutManager) {
            i = ((StaggeredGridLayoutManager) mLayout).findFirstVisibleItemPositions(null)[mLayout.getLayoutDirection() == 1 ? ((StaggeredGridLayoutManager) this.mLayout).getSpanCount() - 1 : 0];
        } else {
            i = 0;
        }

        if (i == -1) {
            return 0;
        }
        return i;
    }
    // kang

    private boolean isLockScreenMode() {
        return ((KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
    }

    public void seslSetHoverScrollEnabled(boolean enabled) {
        mHoverScrollEnable = enabled;
        mHoverScrollStateChanged = true;
    }

    public int findFirstVisibleItemPosition() {
        if (mLayout instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) mLayout).findFirstVisibleItemPosition();
        }
        if (mLayout instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) mLayout).findFirstVisibleItemPositions(null)[0];
        }
        return NO_POSITION;
    }

    int findLastVisibleItemPosition() {
        if (mLayout instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) mLayout).findLastVisibleItemPosition();
        }
        if (mLayout instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) mLayout).findLastVisibleItemPositions(null)[0];
        }
        return NO_POSITION;
    }

    // kang
    private boolean pageScroll(int focus) {
        int i;
        int i2 = 0;
        if (mAdapter == null) {
            Log.e(TAG, "No adapter attached; skipping pageScroll");
            return false;
        }
        int itemCount = mAdapter.getItemCount();
        if (itemCount <= 0) {
            return false;
        }
        if (focus == 0) {
            i = findFirstVisibleItemPosition() - getChildCount();
        } else if (focus == 1) {
            i = findLastVisibleItemPosition() + getChildCount();
        } else if (focus == 2) {
            i = 0;
        } else if (focus != 3) {
            return false;
        } else {
            i = itemCount - 1;
        }
        int i4 = itemCount - 1;
        if (i > i4) {
            i2 = i4;
        } else if (i >= 0) {
            i2 = i;
        }
        mLayout.mRecyclerView.scrollToPosition(i2);
        mLayout.mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (getChildAt(0) != null) {
                    getChildAt(0).requestFocus();
                }
            }
        });
        return true;
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent var1) {
        if (this.mAdapter == null) {
            Log.d("SeslRecyclerView", "No adapter attached; skipping hover scroll");
            return super.dispatchHoverEvent(var1);
        } else {
            int var2 = var1.getAction();
            int var3 = var1.getToolType(0);
            if ((var2 == 7 || var2 == 9) && var3 == 2) {
                this.mIsPenHovered = true;
            } else if (var2 == 10) {
                this.mIsPenHovered = false;
            }

            boolean var4 = SeslTextViewReflector.semIsTextViewHovered();
            this.mNewTextViewHoverState = var4;
            if (var4 || !this.mOldTextViewHoverState || !this.mIsPenDragBlockEnabled || var1.getButtonState() != 32 && var1.getButtonState() != 2) {
                this.mIsNeedPenSelectIconSet = false;
            } else {
                this.mIsNeedPenSelectIconSet = true;
            }

            this.mOldTextViewHoverState = this.mNewTextViewHoverState;
            boolean var6;
            int var7;
            boolean var17;
            if (var2 != 9 && !this.mHoverScrollStateChanged) {
                if (var2 == 7) {
                    if ((!this.mIsPenDragBlockEnabled || this.mIsPenSelectPointerSetted || var1.getToolType(0) != 2 || var1.getButtonState() != 32 && var1.getButtonState() != 2) && !this.mIsNeedPenSelectIconSet) {
                        if (this.mIsPenDragBlockEnabled && this.mIsPenSelectPointerSetted && var1.getButtonState() != 32 && var1.getButtonState() != 2) {
                            this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                            this.mIsPenSelectPointerSetted = false;
                        }
                    } else {
                        this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_PEN_SELECT());
                        this.mIsPenSelectPointerSetted = true;
                    }
                } else if (var2 == 10 && this.mIsPenSelectPointerSetted) {
                    this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                    this.mIsPenSelectPointerSetted = false;
                }
            } else {
                this.mNeedsHoverScroll = true;
                this.mHoverScrollStateChanged = false;
                if (this.mHasNestedScrollRange) {
                    this.adjustNestedScrollRange();
                }

                if (!this.mHoverScrollEnable) {
                    this.mNeedsHoverScroll = false;
                }

                if (this.mNeedsHoverScroll && var3 == 2) {
                    String var5 = SeslSystemReflector.getField_SEM_PEN_HOVERING();
                    if (android.provider.Settings.System.getInt(this.mContext.getContentResolver(), var5, 0) == 1) {
                        var6 = true;
                    } else {
                        var6 = false;
                    }

                    label413: {
                        label412: {
                            try {
                                var7 = android.provider.Settings.System.getInt(this.mContext.getContentResolver(), "car_mode_on");
                            } catch (Settings.SettingNotFoundException var14) {
                                Log.i("SeslRecyclerView", "dispatchHoverEvent car_mode_on SettingNotFoundException");
                                break label412;
                            }

                            if (var7 == 1) {
                                var17 = true;
                                break label413;
                            }
                        }

                        var17 = false;
                    }

                    if (!var6 || var17) {
                        this.mNeedsHoverScroll = false;
                    }

                    if (var6 && this.mIsPenDragBlockEnabled && !this.mIsPenSelectPointerSetted && (var1.getButtonState() == 32 || var1.getButtonState() == 2)) {
                        this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_PEN_SELECT());
                        this.mIsPenSelectPointerSetted = true;
                    }
                }

                if (this.mNeedsHoverScroll && var3 == 3) {
                    this.mNeedsHoverScroll = false;
                }
            }

            if (!this.mNeedsHoverScroll) {
                return super.dispatchHoverEvent(var1);
            } else {
                var4 = this.mLayout.canScrollHorizontally();
                float var8;
                if (var4) {
                    var8 = var1.getY();
                } else {
                    var8 = var1.getX();
                }

                int var9 = (int)var8;
                if (var4) {
                    var8 = var1.getX();
                } else {
                    var8 = var1.getY();
                }

                int var10 = (int)var8;
                int var11 = this.getChildCount();
                int var12;
                int var16;
                if (this.mIsEnabledPaddingInHoverScroll) {
                    var12 = this.mListPadding.top;
                    var3 = this.getHeight() - this.mListPadding.bottom;
                } else {
                    var12 = this.mExtraPaddingInTopHoverArea;
                    if (var4) {
                        var16 = this.getWidth();
                    } else {
                        var16 = this.getHeight();
                    }

                    var3 = var16;
                }

                if (this.findFirstChildPosition() + var11 < this.mAdapter.getItemCount()) {
                    var6 = true;
                } else {
                    var6 = false;
                }

                boolean var13 = var6;
                if (!var6) {
                    var13 = var6;
                    if (var11 > 0) {
                        label381: {
                            label380: {
                                this.getDecoratedBoundsWithMargins(this.getChildAt(var11 - 1), this.mChildBound);
                                if (var4) {
                                    if (this.mChildBound.right <= this.getRight() - this.mListPadding.right && this.mChildBound.right <= this.getWidth() - this.mListPadding.right) {
                                        break label380;
                                    }
                                } else if (this.mChildBound.bottom <= this.getBottom() - this.mListPadding.bottom && this.mChildBound.bottom <= this.getHeight() - this.mListPadding.bottom) {
                                    break label380;
                                }

                                var6 = true;
                                break label381;
                            }

                            var6 = false;
                        }

                        var13 = var6;
                    }
                }

                if (this.findFirstChildPosition() > 0) {
                    var17 = true;
                } else {
                    var17 = false;
                }

                var6 = var17;
                if (!var17) {
                    var6 = var17;
                    if (var11 > 0) {
                        label368: {
                            label367: {
                                this.getDecoratedBoundsWithMargins(this.getChildAt(0), this.mChildBound);
                                if (var4) {
                                    if (this.mChildBound.left < this.mListPadding.left) {
                                        break label367;
                                    }
                                } else if (this.mChildBound.top < this.mListPadding.top) {
                                    break label367;
                                }

                                var6 = false;
                                break label368;
                            }

                            var6 = true;
                        }
                    }
                }

                if (var1.getToolType(0) == 2) {
                    var17 = true;
                } else {
                    var17 = false;
                }

                de.dlyt.yanndroid.oneui.view.RecyclerView.OnScrollListener var15;
                if ((var10 <= this.mHoverTopAreaHeight + var12 || var10 >= var3 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange) && var9 > 0) {
                    if (var4) {
                        var11 = this.getBottom();
                    } else {
                        var11 = this.getRight();
                    }

                    if (var9 <= var11 && (var6 || var13) && (var10 < var12 || var10 > this.mHoverTopAreaHeight + var12 || var6 || !this.mIsHoverOverscrolled)) {
                        var11 = this.mHoverBottomAreaHeight;
                        var16 = this.mRemainNestedScrollRange;
                        if ((var10 < var3 - var11 - var16 || var10 > var3 - var16 || var13 || !this.mIsHoverOverscrolled) && (!var17 || var1.getButtonState() != 32 && var1.getButtonState() != 2) && var17 && !this.isLockScreenMode()) {
                            if (this.mHasNestedScrollRange) {
                                var16 = this.mRemainNestedScrollRange;
                                if (var16 > 0 && var16 != this.mNestedScrollRange) {
                                    this.adjustNestedScrollRange();
                                }
                            }

                            if (!this.mHoverAreaEnter) {
                                this.mHoverScrollStartTime = System.currentTimeMillis();
                            }

                            if (var2 != 7) {
                                if (var2 != 9) {
                                    if (var2 == 10) {
                                        if (this.mHoverHandler.hasMessages(0)) {
                                            this.mHoverHandler.removeMessages(0);
                                        }

                                        if (this.mScrollState == 1) {
                                            this.setScrollState(0);
                                        }

                                        this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                                        this.mHoverRecognitionStartTime = 0L;
                                        this.mHoverScrollStartTime = 0L;
                                        this.mIsHoverOverscrolled = false;
                                        this.mHoverAreaEnter = false;
                                        this.mIsSendHoverScrollState = false;
                                        if (this.mHoverScrollStateForListener != 0) {
                                            this.mHoverScrollStateForListener = 0;
                                            var15 = this.mScrollListener;
                                            if (var15 != null) {
                                                var15.onScrollStateChanged(this, 0);
                                            }
                                        }

                                        return super.dispatchHoverEvent(var1);
                                    }
                                } else {
                                    this.mHoverAreaEnter = true;
                                    if (var10 >= var12 && var10 <= var12 + this.mHoverTopAreaHeight) {
                                        if (!this.mHoverHandler.hasMessages(0)) {
                                            this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                            if (var4) {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_LEFT();
                                            } else {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_UP();
                                            }

                                            this.showPointerIcon(var1, var16);
                                            this.mHoverScrollDirection = 2;
                                            this.mHoverHandler.sendEmptyMessage(0);
                                        }
                                    } else {
                                        var16 = this.mHoverBottomAreaHeight;
                                        var7 = this.mRemainNestedScrollRange;
                                        if (var10 >= var3 - var16 - var7 && var10 <= var3 - var7 && !this.mHoverHandler.hasMessages(0)) {
                                            this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                            if (var4) {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_RIGHT();
                                            } else {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_DOWN();
                                            }

                                            this.showPointerIcon(var1, var16);
                                            this.mHoverScrollDirection = 1;
                                            this.mHoverHandler.sendEmptyMessage(0);
                                        }
                                    }
                                }
                            } else {
                                if (!this.mHoverAreaEnter) {
                                    this.mHoverAreaEnter = true;
                                    var1.setAction(10);
                                    return super.dispatchHoverEvent(var1);
                                }

                                if (var10 >= var12 && var10 <= var12 + this.mHoverTopAreaHeight) {
                                    if (!this.mHoverHandler.hasMessages(0)) {
                                        this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                        if (!this.mIsHoverOverscrolled || this.mHoverScrollDirection == 1) {
                                            if (var4) {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_LEFT();
                                            } else {
                                                var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_UP();
                                            }

                                            this.showPointerIcon(var1, var16);
                                        }

                                        this.mHoverScrollDirection = 2;
                                        this.mHoverHandler.sendEmptyMessage(0);
                                    }
                                } else {
                                    var16 = this.mHoverBottomAreaHeight;
                                    var7 = this.mRemainNestedScrollRange;
                                    if (var10 >= var3 - var16 - var7 && var10 <= var3 - var7) {
                                        if (!this.mHoverHandler.hasMessages(0)) {
                                            this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                            if (!this.mIsHoverOverscrolled || this.mHoverScrollDirection == 2) {
                                                if (var4) {
                                                    var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_RIGHT();
                                                } else {
                                                    var16 = SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_SCROLL_DOWN();
                                                }

                                                this.showPointerIcon(var1, var16);
                                            }

                                            this.mHoverScrollDirection = 1;
                                            this.mHoverHandler.sendEmptyMessage(0);
                                        }
                                    } else {
                                        if (this.mHoverHandler.hasMessages(0)) {
                                            this.mHoverHandler.removeMessages(0);
                                            if (this.mScrollState == 1) {
                                                this.setScrollState(0);
                                            }
                                        }

                                        this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                                        this.mHoverRecognitionStartTime = 0L;
                                        this.mHoverScrollStartTime = 0L;
                                        this.mIsHoverOverscrolled = false;
                                        this.mHoverAreaEnter = false;
                                        this.mIsSendHoverScrollState = false;
                                    }
                                }
                            }

                            return true;
                        }
                    }
                }

                if (this.mHasNestedScrollRange) {
                    var16 = this.mRemainNestedScrollRange;
                    if (var16 > 0 && var16 != this.mNestedScrollRange) {
                        this.adjustNestedScrollRange();
                    }
                }

                if (this.mHoverHandler.hasMessages(0)) {
                    this.mHoverHandler.removeMessages(0);
                    this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                    if (this.mScrollState == 1) {
                        this.setScrollState(0);
                    }
                }

                label490: {
                    if ((var10 <= var12 + this.mHoverTopAreaHeight || var10 >= var3 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange) && var9 > 0) {
                        if (var4) {
                            var16 = this.getBottom();
                        } else {
                            var16 = this.getRight();
                        }

                        if (var9 <= var16) {
                            break label490;
                        }
                    }

                    this.mIsHoverOverscrolled = false;
                }

                if (this.mHoverAreaEnter || this.mHoverScrollStartTime != 0L) {
                    this.showPointerIcon(var1, SeslPointerIconReflector.getField_SEM_TYPE_STYLUS_DEFAULT());
                }

                this.mHoverRecognitionStartTime = 0L;
                this.mHoverScrollStartTime = 0L;
                this.mHoverAreaEnter = false;
                this.mIsSendHoverScrollState = false;
                if (var2 == 10) {
                    if (this.mHoverScrollStateForListener != 0) {
                        this.mHoverScrollStateForListener = 0;
                        var15 = this.mScrollListener;
                        if (var15 != null) {
                            var15.onScrollStateChanged(this, 0);
                        }
                    } else {
                        this.mIsHoverOverscrolled = false;
                    }
                }

                return super.dispatchHoverEvent(var1);
            }
        }
    }
    // kang

    public void seslSetSmoothScrollEnabled(boolean enabled) {
        if (mViewFlinger != null) {
            SeslOverScrollerReflector.setSmoothScrollEnabled(mViewFlinger.mOverScroller, enabled);
        }
    }

    public void seslSetPagingTouchSlopForStylus(boolean use) {
        mUsePagingTouchSlopForStylus = use;
    }

    public boolean seslIsPagingTouchSlopForStylusEnabled() {
        return mUsePagingTouchSlopForStylus;
    }
}
