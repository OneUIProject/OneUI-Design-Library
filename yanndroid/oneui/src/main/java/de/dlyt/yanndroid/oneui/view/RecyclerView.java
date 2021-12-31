package de.dlyt.yanndroid.oneui.view;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.util.SeslSubheaderRoundedCorner;
import androidx.core.os.TraceCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.ScrollingView;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.view.AbsSavedState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.DefaultItemAnimator;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.FastScroller;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslAdapterHelper;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslChildHelper;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslGapWorker;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslLinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslOverScroller;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslRecyclerViewAccessibilityDelegate;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslRecyclerViewFastScroller;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslViewBoundsCheck;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslViewInfoStore;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.sesl.utils.SamsungEdgeEffect;

public class RecyclerView extends ViewGroup implements NestedScrollingChild2, ScrollingView {
    private boolean mIsOneUI4;
    public static final int HORIZONTAL = 0;
    public static final int INVALID_TYPE = -1;
    public static final long NO_ID = -1L;
    public static final int NO_POSITION = -1;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    public static final int TOUCH_SLOP_DEFAULT = 0;
    public static final int TOUCH_SLOP_PAGING = 1;
    public static final int VERTICAL = 1;
    public static final boolean DEBUG = false;
    public static final long FOREVER_NS = 9223372036854775807L;
    public static final String TRACE_NESTED_PREFETCH_TAG = "RV Nested Prefetch";
    public static final String TRACE_PREFETCH_TAG = "RV Prefetch";
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC;
    static final int DEFAULT_ORIENTATION = 1;
    static final boolean DISPATCH_TEMP_DETACH = false;
    static final boolean FORCE_INVALIDATE_DISPLAY_LIST;
    static final int MAX_SCROLL_DURATION = 2000;
    static final boolean POST_UPDATES_ON_ANIMATION;
    static final String TAG = "SeslRecyclerView";
    static final String TRACE_BIND_VIEW_TAG = "RV OnBindView";
    static final String TRACE_CREATE_VIEW_TAG = "RV CreateView";
    static final String TRACE_SCROLL_TAG = "RV Scroll";
    static final boolean VERBOSE_TRACING = false;
    static final Interpolator sQuinticInterpolator;
    private static final boolean ALLOW_THREAD_GAP_WORK;
    private static final int[] CLIP_TO_PADDING_ATTR = new int[]{16842987};
    private static final int FOCUS_MOVE_DOWN = 1;
    private static final int FOCUS_MOVE_FULL_DOWN = 3;
    private static final int FOCUS_MOVE_FULL_UP = 2;
    private static final int FOCUS_MOVE_UP = 0;
    private static final boolean FORCE_ABS_FOCUS_SEARCH_DIRECTION;
    private static final int GTP_STATE_NONE = 0;
    private static final int GTP_STATE_PRESSED = 2;
    private static final int GTP_STATE_SHOWN = 1;
    private static final int HOVERSCROLL_DOWN = 2;
    private static final int HOVERSCROLL_HEIGHT_BOTTOM_DP = 25;
    private static final int HOVERSCROLL_HEIGHT_TOP_DP = 25;
    private static final int HOVERSCROLL_UP = 1;
    private static final boolean IGNORE_DETACHED_FOCUSED_CHILD;
    private static final int INVALID_POINTER = -1;
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE;
    private static final int MOTION_EVENT_ACTION_PEN_DOWN = 211;
    private static final int MOTION_EVENT_ACTION_PEN_MOVE = 213;
    private static final int MOTION_EVENT_ACTION_PEN_UP = 212;
    private static final int MSG_HOVERSCROLL_MOVE = 0;
    private static final int[] NESTED_SCROLLING_ATTRS = new int[]{16843830};
    private static final String TRACE_HANDLE_ADAPTER_UPDATES_TAG = "RV PartialInvalidate";
    private static final String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG = "RV FullInvalidate";
    private static final String TRACE_ON_LAYOUT_TAG = "RV OnLayout";

    static {
        boolean var0;
        if (VERSION.SDK_INT != 18 && VERSION.SDK_INT != 19 && VERSION.SDK_INT != 20) {
            var0 = false;
        } else {
            var0 = true;
        }

        FORCE_INVALIDATE_DISPLAY_LIST = var0;
        if (VERSION.SDK_INT >= 23) {
            var0 = true;
        } else {
            var0 = false;
        }

        ALLOW_SIZE_IN_UNSPECIFIED_SPEC = var0;
        if (VERSION.SDK_INT >= 16) {
            var0 = true;
        } else {
            var0 = false;
        }

        POST_UPDATES_ON_ANIMATION = var0;
        if (VERSION.SDK_INT >= 21) {
            var0 = true;
        } else {
            var0 = false;
        }

        ALLOW_THREAD_GAP_WORK = var0;
        if (VERSION.SDK_INT <= 15) {
            var0 = true;
        } else {
            var0 = false;
        }

        FORCE_ABS_FOCUS_SEARCH_DIRECTION = var0;
        if (VERSION.SDK_INT <= 15) {
            var0 = true;
        } else {
            var0 = false;
        }

        IGNORE_DETACHED_FOCUSED_CHILD = var0;
        LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE = new Class[]{Context.class, AttributeSet.class, Integer.TYPE, Integer.TYPE};
        sQuinticInterpolator = new Interpolator() {
            public float getInterpolation(float var1) {
                --var1;
                return var1 * var1 * var1 * var1 * var1 + 1.0F;
            }
        };
    }

    public final RecyclerView.Recycler mRecycler;
    public final RecyclerView.State mState;
    final ArrayList<RecyclerView.ItemDecoration> mItemDecorations;
    final List<RecyclerView.ViewHolder> mPendingAccessibilityImportanceChange;
    final Rect mTempRect;
    final RectF mTempRectF;
    final Runnable mUpdateChildViewsRunnable;
    final RecyclerView.ViewFlinger mViewFlinger;
    final SeslViewInfoStore mViewInfoStore;
    private final AccessibilityManager mAccessibilityManager;
    private final Runnable mAutoHide;
    private final Runnable mGoToToFadeInRunnable;
    private final Runnable mGoToToFadeOutRunnable;
    private final int[] mMinMaxLayoutPositions;
    private final int[] mNestedOffsets;
    private final RecyclerView.RecyclerViewDataObserver mObserver;
    private final ArrayList<RecyclerView.OnItemTouchListener> mOnItemTouchListeners;
    private final int[] mScrollConsumed;
    private final int[] mScrollOffset;
    private final Rect mTempRect2;
    private final SeslViewInfoStore.ProcessCallback mViewInfoProcessCallback;
    private final int[] mWindowOffsets;
    public SeslChildHelper mChildHelper;
    public int mGoToTopImmersiveBottomPadding;
    public ImageView mGoToTopView;
    public boolean mHoverAreaEnter;
    public RecyclerView.Adapter mAdapter;
    public SeslAdapterHelper mAdapterHelper;
    public int mBlackTop;
    public boolean mDataSetHasChangedAfterLayout;
    public RecyclerView.LayoutManager mLayout;
    public Rect mListPadding;
    public SeslGapWorker.LayoutPrefetchRegistryImpl mPrefetchRegistry;
    SeslRecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    boolean mAdapterUpdateDuringMeasure;
    int mAnimatedBlackTop;
    boolean mClipToPadding;
    boolean mDispatchItemsChangedEvent;
    boolean mDrawRect;
    boolean mDrawReverse;
    boolean mDrawWhiteTheme;
    boolean mEnableFastScroller;
    boolean mFirstLayoutComplete;
    SeslGapWorker mGapWorker;
    boolean mHasFixedSize;
    boolean mIsAttached;
    RecyclerView.ItemAnimator mItemAnimator;
    boolean mItemsAddedOrRemoved;
    boolean mItemsChanged;
    int mLastBlackTop;
    boolean mLayoutFrozen;
    boolean mLayoutWasDefered;
    boolean mPostedAnimatorRunner;
    RecyclerView.RecyclerListener mRecyclerListener;
    Drawable mSelector;
    Rect mSelectorRect;
    boolean mShowGTPAtFirstTime;
    private int GO_TO_TOP_HIDE;
    private int HOVERSCROLL_DELAY;
    private float HOVERSCROLL_SPEED;
    private RecyclerView.OnItemTouchListener mActiveOnItemTouchListener;
    private boolean mAlwaysDisableHoverHighlight;
    private SamsungEdgeEffect mBottomGlow;
    private RecyclerView.ChildDrawingOrderCallback mChildDrawingOrderCallback;
    private View mCloseChildByBottom;
    private View mCloseChildByTop;
    private int mCloseChildPositionByBottom;
    private int mCloseChildPositionByTop;
    private Context mContext;
    private int mDispatchScrollCounter;
    private int mDistanceFromCloseChildBottom;
    private int mDistanceFromCloseChildTop;
    private boolean mDrawLastRoundedCorner;
    private int mEatenAccessibilityChangeFlags;
    private boolean mEnableGoToTop;
    private int mExtraPaddingInBottomHoverArea;
    private int mExtraPaddingInTopHoverArea;
    private SeslRecyclerViewFastScroller mFastScroller;
    private boolean mFastScrollerEnabled;
    private RecyclerView.SeslFastScrollerEventListener mFastScrollerEventListener;
    private int mGoToTopBottomPadding;
    private float mGoToTopElevation;
    private ValueAnimator mGoToTopFadeInAnimator;
    private ValueAnimator mGoToTopFadeOutAnimator;
    private Drawable mGoToTopImage;
    private Drawable mGoToTopImageLight;
    private int mGoToTopLastState;
    private boolean mGoToTopMoved;
    private Rect mGoToTopRect;
    private int mGoToTopSize;
    private int mGoToTopState;
    private boolean mGoToToping;
    private boolean mHasNestedScrollRange;
    private int mHoverBottomAreaHeight;
    private Handler mHoverHandler;
    private long mHoverRecognitionCurrentTime;
    private long mHoverRecognitionDurationTime;
    private long mHoverRecognitionStartTime;
    private int mHoverScrollDirection;
    private boolean mHoverScrollEnable;
    private int mHoverScrollSpeed;
    private long mHoverScrollStartTime;
    private boolean mHoverScrollStateChanged;
    private int mHoverScrollStateForListener;
    private long mHoverScrollTimeInterval;
    private int mHoverTopAreaHeight;
    private boolean mHoveringEnabled;
    private boolean mIgnoreMotionEventTillDown;
    private int mInitialTopOffsetOfScreen;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mInterceptRequestLayoutDepth;
    private boolean mIsArrowKeyPressed;
    private boolean mIsCloseChildSetted;
    private boolean mIsCtrlKeyPressed;
    private boolean mIsCtrlMultiSelection;
    private boolean mIsEnabledPaddingInHoverScroll;
    private boolean mIsFirstMultiSelectionMove;
    private boolean mIsFirstPenMoveEvent;
    private boolean mIsHoverOverscrolled;
    private boolean mIsLongPressMultiSelection;
    private boolean mIsMouseWheel;
    private boolean mIsNeedPenSelectIconSet;
    private boolean mIsNeedPenSelection;
    private boolean mIsPenDragBlockEnabled;
    private boolean mIsPenHovered;
    private boolean mIsPenPressed;
    private boolean mIsPenSelectPointerSetted;
    private boolean mIsPenSelectionEnabled;
    private boolean mIsSendHoverScrollState;
    private RecyclerView.ItemAnimator.ItemAnimatorListener mItemAnimatorListener;
    private Runnable mItemAnimatorRunner;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mLayoutOrScrollCounter;
    private SamsungEdgeEffect mLeftGlow;
    private RecyclerView.SeslLongPressMultiSelectionListener mLongPressMultiSelectionListener;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private int mNavigationBarHeight;
    private boolean mNeedsHoverScroll;
    private boolean mNestedScroll;
    private int mNestedScrollRange;
    private boolean mNewTextViewHoverState;
    private int mOldHoverScrollDirection;
    private boolean mOldTextViewHoverState;
    private List<RecyclerView.OnChildAttachStateChangeListener> mOnChildAttachStateListeners;
    private RecyclerView.OnFlingListener mOnFlingListener;
    private RecyclerView.SeslOnMultiSelectedListener mOnMultiSelectedListener;
    private int mPenDistanceFromTrackedChildTop;
    private int mPenDragBlockBottom;
    private Drawable mPenDragBlockImage;
    private int mPenDragBlockLeft;
    private Rect mPenDragBlockRect;
    private int mPenDragBlockRight;
    private int mPenDragBlockTop;
    private int mPenDragEndX;
    private int mPenDragEndY;
    private long mPenDragScrollTimeInterval;
    private ArrayList<Integer> mPenDragSelectedItemArray;
    private int mPenDragSelectedViewPosition;
    private int mPenDragStartX;
    private int mPenDragStartY;
    private View mPenTrackedChild;
    private int mPenTrackedChildPosition;
    private RecyclerView.SavedState mPendingSavedState;
    private boolean mPreserveFocusAfterLayout;
    private int mRectColor;
    private Paint mRectPaint;
    private int mRemainNestedScrollRange;
    private SamsungEdgeEffect mRightGlow;
    private View mRootViewCheckForDialog;
    private float mScaledHorizontalScrollFactor;
    private float mScaledVerticalScrollFactor;
    private RecyclerView.OnScrollListener mScrollListener;
    private List<RecyclerView.OnScrollListener> mScrollListeners;
    private int mScrollPointerId;
    private int mScrollState;
    private NestedScrollingChildHelper mScrollingChildHelper;
    private RecyclerView.SeslOnGoToTopClickListener mSeslOnGoToTopClickListener;
    private int mSeslPagingTouchSlop;
    private SeslSubheaderRoundedCorner mSeslRoundedCorner;
    private int mSeslTouchSlop;
    private int mShowFadeOutGTP;
    private boolean mSizeChnage;
    private SamsungEdgeEffect mTopGlow;
    private int mTouchSlop;
    private boolean mUsePagingTouchSlopForStylus;
    private VelocityTracker mVelocityTracker;

    public RecyclerView(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public RecyclerView(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
    }

    @SuppressLint("WrongConstant")
    public RecyclerView(Context var1, AttributeSet var2, int var3) {
        super(var1, var2, var3);
        mIsOneUI4 = var1.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        this.mSeslTouchSlop = 0;
        this.mSeslPagingTouchSlop = 0;
        this.mUsePagingTouchSlopForStylus = false;
        this.mObserver = new RecyclerView.RecyclerViewDataObserver();
        this.mRecycler = new RecyclerView.Recycler();
        this.mViewInfoStore = new SeslViewInfoStore();
        this.mUpdateChildViewsRunnable = new Runnable() {
            public void run() {
                if (RecyclerView.this.mFirstLayoutComplete && !RecyclerView.this.isLayoutRequested()) {
                    if (!RecyclerView.this.mIsAttached) {
                        RecyclerView.this.requestLayout();
                    } else if (RecyclerView.this.mLayoutFrozen) {
                        RecyclerView.this.mLayoutWasDefered = true;
                    } else {
                        RecyclerView.this.consumePendingUpdateOperations();
                    }
                }

            }
        };
        this.mTempRect = new Rect();
        this.mTempRect2 = new Rect();
        this.mTempRectF = new RectF();
        this.mItemDecorations = new ArrayList();
        this.mOnItemTouchListeners = new ArrayList();
        this.mSeslOnGoToTopClickListener = null;
        this.mInterceptRequestLayoutDepth = 0;
        this.mDataSetHasChangedAfterLayout = false;
        this.mDispatchItemsChangedEvent = false;
        this.mLayoutOrScrollCounter = 0;
        this.mDispatchScrollCounter = 0;
        this.mItemAnimator = new DefaultItemAnimator();
        this.mScrollState = 0;
        this.mScrollPointerId = -1;
        this.mScaledHorizontalScrollFactor = 1.4E-45F;
        this.mScaledVerticalScrollFactor = 1.4E-45F;
        this.mPreserveFocusAfterLayout = true;
        this.mViewFlinger = new RecyclerView.ViewFlinger();
        SeslGapWorker.LayoutPrefetchRegistryImpl var4;
        if (ALLOW_THREAD_GAP_WORK) {
            var4 = new SeslGapWorker.LayoutPrefetchRegistryImpl();
        } else {
            var4 = null;
        }

        this.mPrefetchRegistry = var4;
        this.mState = new RecyclerView.State();
        this.mItemsAddedOrRemoved = false;
        this.mItemsChanged = false;
        this.mItemAnimatorListener = new RecyclerView.ItemAnimatorRestoreListener();
        this.mPostedAnimatorRunner = false;
        this.mMinMaxLayoutPositions = new int[2];
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mNestedOffsets = new int[2];
        this.mWindowOffsets = new int[2];
        this.mIsPenSelectionEnabled = true;
        this.mIsPenPressed = false;
        this.mIsFirstPenMoveEvent = true;
        this.mIsNeedPenSelection = false;
        this.mPenDragSelectedViewPosition = -1;
        this.mIsPenDragBlockEnabled = true;
        this.mPenDragStartX = 0;
        this.mPenDragStartY = 0;
        this.mPenDragEndX = 0;
        this.mPenDragEndY = 0;
        this.mPenDragBlockLeft = 0;
        this.mPenDragBlockTop = 0;
        this.mPenDragBlockRight = 0;
        this.mPenDragBlockBottom = 0;
        this.mPenTrackedChild = null;
        this.mPenTrackedChildPosition = -1;
        this.mPenDistanceFromTrackedChildTop = 0;
        this.mPenDragBlockRect = new Rect();
        this.mInitialTopOffsetOfScreen = 0;
        this.mRemainNestedScrollRange = 0;
        this.mNestedScrollRange = 0;
        this.mHasNestedScrollRange = false;
        this.mIsCtrlKeyPressed = false;
        this.mIsLongPressMultiSelection = false;
        this.mIsFirstMultiSelectionMove = true;
        this.mIsCtrlMultiSelection = false;
        this.mIsMouseWheel = false;
        this.mNestedScroll = false;
        this.mFastScrollerEnabled = false;
        this.mEnableGoToTop = false;
        this.mSizeChnage = false;
        this.mGoToToping = false;
        this.mGoToTopMoved = false;
        this.mGoToTopRect = new Rect();
        this.mGoToTopState = 0;
        this.mGoToTopLastState = 0;
        this.mShowGTPAtFirstTime = false;
        this.mShowFadeOutGTP = 0;
        this.GO_TO_TOP_HIDE = 2500;
        this.mDrawLastRoundedCorner = true;
        this.mDrawRect = false;
        this.mDrawWhiteTheme = true;
        this.mDrawReverse = false;
        this.mBlackTop = -1;
        this.mLastBlackTop = -1;
        this.mAnimatedBlackTop = -1;
        this.mRectPaint = new Paint();
        this.HOVERSCROLL_SPEED = 10.0F;
        this.mHoveringEnabled = true;
        this.mIsPenHovered = false;
        this.mAlwaysDisableHoverHighlight = false;
        this.mRootViewCheckForDialog = null;
        this.mIsPenSelectPointerSetted = false;
        this.mIsNeedPenSelectIconSet = false;
        this.mOldTextViewHoverState = false;
        this.mNewTextViewHoverState = false;
        this.mHoverScrollSpeed = 0;
        this.mHoverRecognitionDurationTime = 0L;
        this.mHoverRecognitionCurrentTime = 0L;
        this.mHoverRecognitionStartTime = 0L;
        this.mHoverScrollTimeInterval = 300L;
        this.mPenDragScrollTimeInterval = 500L;
        this.mHoverScrollStartTime = 0L;
        this.mHoverScrollDirection = -1;
        this.mIsHoverOverscrolled = false;
        this.mIsSendHoverScrollState = false;
        this.HOVERSCROLL_DELAY = 0;
        this.mHoverScrollStateForListener = 0;
        this.mIsEnabledPaddingInHoverScroll = false;
        this.mHoverAreaEnter = false;
        this.mSelectorRect = new Rect();
        this.mHoverScrollEnable = true;
        this.mHoverScrollStateChanged = false;
        this.mNeedsHoverScroll = false;
        this.mHoverTopAreaHeight = 0;
        this.mHoverBottomAreaHeight = 0;
        this.mListPadding = new Rect();
        this.mExtraPaddingInTopHoverArea = 0;
        this.mExtraPaddingInBottomHoverArea = 0;
        this.mIsCloseChildSetted = false;
        this.mOldHoverScrollDirection = -1;
        this.mCloseChildByTop = null;
        this.mCloseChildPositionByTop = -1;
        this.mDistanceFromCloseChildTop = 0;
        this.mCloseChildByBottom = null;
        this.mCloseChildPositionByBottom = -1;
        this.mDistanceFromCloseChildBottom = 0;
        this.mHoverHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message var1) {
                switch (var1.what) {
                    case 0:
                        if (RecyclerView.this.mAdapter == null) {
                            Log.e("SeslRecyclerView", "No adapter attached; skipping MSG_HOVERSCROLL_MOVE");
                        } else {
                            RecyclerView.this.mHoverRecognitionCurrentTime = System.currentTimeMillis();
                            RecyclerView.this.mHoverRecognitionDurationTime = (RecyclerView.this.mHoverRecognitionCurrentTime - RecyclerView.this.mHoverRecognitionStartTime) / 1000L;
                            if ((!RecyclerView.this.mIsPenHovered || RecyclerView.this.mHoverRecognitionCurrentTime - RecyclerView.this.mHoverScrollStartTime >= RecyclerView.this.mHoverScrollTimeInterval) && (!RecyclerView.this.mIsPenPressed || RecyclerView.this.mHoverRecognitionCurrentTime - RecyclerView.this.mHoverScrollStartTime >= RecyclerView.this.mPenDragScrollTimeInterval)) {
                                if (RecyclerView.this.mIsPenHovered && !RecyclerView.this.mIsSendHoverScrollState) {
                                    if (RecyclerView.this.mScrollListener != null) {
                                        RecyclerView.this.mHoverScrollStateForListener = 1;
                                        RecyclerView.this.mScrollListener.onScrollStateChanged(RecyclerView.this, 1);
                                    }

                                    RecyclerView.this.mIsSendHoverScrollState = true;
                                }

                                int var2 = RecyclerView.this.getChildCount();
                                boolean var3;
                                if (RecyclerView.this.findFirstChildPosition() + var2 < RecyclerView.this.mAdapter.getItemCount()) {
                                    var3 = true;
                                } else {
                                    var3 = false;
                                }

                                boolean var4 = var3;
                                if (!var3) {
                                    var4 = var3;
                                    if (var2 > 0) {
                                        View var6 = RecyclerView.this.getChildAt(var2 - 1);
                                        if (var6.getBottom() <= RecyclerView.this.getBottom() - RecyclerView.this.mListPadding.bottom && var6.getBottom() <= RecyclerView.this.getHeight() - RecyclerView.this.mListPadding.bottom) {
                                            var4 = false;
                                        } else {
                                            var4 = true;
                                        }
                                    }
                                }

                                boolean var7;
                                if (RecyclerView.this.findFirstChildPosition() > 0) {
                                    var7 = true;
                                } else {
                                    var7 = false;
                                }

                                var3 = var7;
                                if (!var7) {
                                    var3 = var7;
                                    if (RecyclerView.this.getChildCount() > 0) {
                                        if (RecyclerView.this.getChildAt(0).getTop() < RecyclerView.this.mListPadding.top) {
                                            var3 = true;
                                        } else {
                                            var3 = false;
                                        }
                                    }
                                }

                                RecyclerView.this.mHoverScrollSpeed = (int) (TypedValue.applyDimension(1, RecyclerView.this.HOVERSCROLL_SPEED, RecyclerView.this.mContext.getResources().getDisplayMetrics()) + 0.5F);
                                if (RecyclerView.this.mHoverRecognitionDurationTime > 2L && RecyclerView.this.mHoverRecognitionDurationTime < 4L) {
                                    RecyclerView.this.mHoverScrollSpeed = RecyclerView.this.mHoverScrollSpeed + (int) ((double) RecyclerView.this.mHoverScrollSpeed * 0.1D);
                                } else if (RecyclerView.this.mHoverRecognitionDurationTime >= 4L && RecyclerView.this.mHoverRecognitionDurationTime < 5L) {
                                    RecyclerView.this.mHoverScrollSpeed = RecyclerView.this.mHoverScrollSpeed + (int) ((double) RecyclerView.this.mHoverScrollSpeed * 0.2D);
                                } else if (RecyclerView.this.mHoverRecognitionDurationTime >= 5L) {
                                    RecyclerView.this.mHoverScrollSpeed = RecyclerView.this.mHoverScrollSpeed + (int) ((double) RecyclerView.this.mHoverScrollSpeed * 0.3D);
                                }

                                int var5;
                                if (RecyclerView.this.mHoverScrollDirection == 2) {
                                    label193:
                                    {
                                        var5 = RecyclerView.this.mHoverScrollSpeed * -1;
                                        if (RecyclerView.this.mPenTrackedChild != null || RecyclerView.this.mCloseChildByBottom == null) {
                                            var2 = var5;
                                            if (RecyclerView.this.mOldHoverScrollDirection == RecyclerView.this.mHoverScrollDirection) {
                                                break label193;
                                            }

                                            var2 = var5;
                                            if (!RecyclerView.this.mIsCloseChildSetted) {
                                                break label193;
                                            }
                                        }

                                        RecyclerView.this.mPenTrackedChild = RecyclerView.this.mCloseChildByBottom;
                                        RecyclerView.this.mPenDistanceFromTrackedChildTop = RecyclerView.this.mDistanceFromCloseChildBottom;
                                        RecyclerView.this.mPenTrackedChildPosition = RecyclerView.this.mCloseChildPositionByBottom;
                                        RecyclerView.this.mOldHoverScrollDirection = RecyclerView.this.mHoverScrollDirection;
                                        RecyclerView.this.mIsCloseChildSetted = true;
                                        var2 = var5;
                                    }
                                } else {
                                    label194:
                                    {
                                        var5 = RecyclerView.this.mHoverScrollSpeed * 1;
                                        if (RecyclerView.this.mPenTrackedChild != null || RecyclerView.this.mCloseChildByTop == null) {
                                            var2 = var5;
                                            if (RecyclerView.this.mOldHoverScrollDirection == RecyclerView.this.mHoverScrollDirection) {
                                                break label194;
                                            }

                                            var2 = var5;
                                            if (!RecyclerView.this.mIsCloseChildSetted) {
                                                break label194;
                                            }
                                        }

                                        RecyclerView.this.mPenTrackedChild = RecyclerView.this.mCloseChildByTop;
                                        RecyclerView.this.mPenDistanceFromTrackedChildTop = RecyclerView.this.mDistanceFromCloseChildTop;
                                        RecyclerView.this.mPenTrackedChildPosition = RecyclerView.this.mCloseChildPositionByTop;
                                        RecyclerView.this.mOldHoverScrollDirection = RecyclerView.this.mHoverScrollDirection;
                                        RecyclerView.this.mIsCloseChildSetted = true;
                                        var2 = var5;
                                    }
                                }

                                if (RecyclerView.this.getChildAt(RecyclerView.this.getChildCount() - 1) != null) {
                                    if ((var2 >= 0 || !var3) && (var2 <= 0 || !var4)) {
                                        int var8 = RecyclerView.this.getOverScrollMode();
                                        if (var8 == 0 || var8 == 1 && !RecyclerView.this.contentFits()) {
                                            var4 = true;
                                        } else {
                                            var4 = false;
                                        }

                                        if (var4 && !RecyclerView.this.mIsHoverOverscrolled) {
                                            RecyclerView.this.ensureTopGlow();
                                            RecyclerView.this.ensureBottomGlow();
                                            if (RecyclerView.this.mHoverScrollDirection == 2) {
                                                RecyclerView.this.mTopGlow.setSize(RecyclerView.this.getWidth(), RecyclerView.this.getHeight());
                                                RecyclerView.this.mTopGlow.onPullCallOnRelease(0.4F, 0.5F, 0);
                                                if (!RecyclerView.this.mBottomGlow.isFinished()) {
                                                    RecyclerView.this.mBottomGlow.onRelease();
                                                }
                                            } else if (RecyclerView.this.mHoverScrollDirection == 1) {
                                                RecyclerView.this.mBottomGlow.setSize(RecyclerView.this.getWidth(), RecyclerView.this.getHeight());
                                                RecyclerView.this.mBottomGlow.onPullCallOnRelease(0.4F, 0.5F, 0);
                                                RecyclerView.this.setupGoToTop(1);
                                                RecyclerView.this.autoHide(1);
                                                if (!RecyclerView.this.mTopGlow.isFinished()) {
                                                    RecyclerView.this.mTopGlow.onRelease();
                                                }
                                            }

                                            RecyclerView.this.invalidate();
                                            RecyclerView.this.mIsHoverOverscrolled = true;
                                        }

                                        if (RecyclerView.this.mScrollState == 1) {
                                            RecyclerView.this.setScrollState(0);
                                        }

                                        if (!var4 && !RecyclerView.this.mIsHoverOverscrolled) {
                                            RecyclerView.this.mIsHoverOverscrolled = true;
                                        }
                                    } else {
                                        RecyclerView.this.startNestedScroll(2, 1);
                                        if (!RecyclerView.this.dispatchNestedPreScroll(0, var2, (int[]) null, (int[]) null, 1)) {
                                            RecyclerView.this.scrollByInternal(0, var2, (MotionEvent) null);
                                            RecyclerView.this.setScrollState(1);
                                            if (RecyclerView.this.mIsLongPressMultiSelection) {
                                                RecyclerView.this.updateLongPressMultiSelection(RecyclerView.this.mPenDragEndX, RecyclerView.this.mPenDragEndY, false);
                                            }
                                        } else {
                                            RecyclerView.this.adjustNestedScrollRangeBy(var2);
                                        }

                                        RecyclerView.this.mHoverHandler.sendEmptyMessageDelayed(0, (long) RecyclerView.this.HOVERSCROLL_DELAY);
                                    }
                                }
                            }
                        }
                    default:
                }
            }
        };
        this.mPendingAccessibilityImportanceChange = new ArrayList();
        this.mItemAnimatorRunner = new Runnable() {
            public void run() {
                if (RecyclerView.this.mItemAnimator != null) {
                    RecyclerView.this.mItemAnimator.runPendingAnimations();
                }

                RecyclerView.this.mPostedAnimatorRunner = false;
            }
        };
        this.mViewInfoProcessCallback = new SeslViewInfoStore.ProcessCallback() {
            public void processAppeared(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3) {
                RecyclerView.this.animateAppearance(var1, var2, var3);
            }

            public void processDisappeared(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3) {
                RecyclerView.this.mRecycler.unscrapView(var1);
                RecyclerView.this.animateDisappearance(var1, var2, var3);
            }

            public void processPersistent(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3) {
                var1.setIsRecyclable(false);
                if (RecyclerView.this.mDataSetHasChangedAfterLayout) {
                    if (RecyclerView.this.mItemAnimator.animateChange(var1, var1, var2, var3)) {
                        RecyclerView.this.postAnimationRunner();
                    }
                } else if (RecyclerView.this.mItemAnimator.animatePersistence(var1, var2, var3)) {
                    RecyclerView.this.postAnimationRunner();
                }

            }

            public void unused(RecyclerView.ViewHolder var1) {
                RecyclerView.this.mLayout.removeAndRecycleView(var1.itemView, RecyclerView.this.mRecycler);
            }
        };
        this.mGoToToFadeOutRunnable = new Runnable() {
            public void run() {
                RecyclerView.this.playGotoToFadeOut();
            }
        };
        this.mGoToToFadeInRunnable = new Runnable() {
            public void run() {
                RecyclerView.this.playGotoToFadeIn();
            }
        };
        this.mAutoHide = new Runnable() {
            public void run() {
                RecyclerView.this.setupGoToTop(0);
            }
        };
        this.mContext = var1;
        if (var2 != null) {
            TypedArray var9 = var1.obtainStyledAttributes(var2, CLIP_TO_PADDING_ATTR, var3, 0);
            this.mClipToPadding = var9.getBoolean(0, true);
            var9.recycle();
        } else {
            this.mClipToPadding = true;
        }

        this.setScrollContainer(true);
        this.setFocusableInTouchMode(true);
        this.seslInitConfigurations(var1);
        boolean var5;
        if (this.getOverScrollMode() == 2) {
            var5 = true;
        } else {
            var5 = false;
        }

        this.setWillNotDraw(var5);
        this.mItemAnimator.setListener(this.mItemAnimatorListener);
        this.initAdapterManager();
        this.initChildrenHelper();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }

        this.mAccessibilityManager = (AccessibilityManager) this.getContext().getSystemService("accessibility");
        this.setAccessibilityDelegateCompat(new SeslRecyclerViewAccessibilityDelegate(this));
        var5 = true;
        if (var2 != null) {
            TypedArray var6 = var1.obtainStyledAttributes(var2, R.styleable.RecyclerView, var3, 0);
            String var10 = var6.getString(R.styleable.RecyclerView_layoutManager);
            if (var6.getInt(R.styleable.RecyclerView_android_descendantFocusability, -1) == -1) {
                this.setDescendantFocusability(262144);
            }

            var6.recycle();
            this.createLayoutManager(var1, var10, var2, var3, 0);
            if (VERSION.SDK_INT >= 21) {
                TypedArray var7 = var1.obtainStyledAttributes(var2, NESTED_SCROLLING_ATTRS, var3, 0);
                var5 = var7.getBoolean(0, true);
                var7.recycle();
            }
        } else {
            this.setDescendantFocusability(262144);
        }

        Resources var8 = var1.getResources();
        TypedValue var11 = new TypedValue();
        this.mPenDragBlockImage = var8.getDrawable(R.drawable.sesl_pen_block_selection, null);
        if (var1.getTheme().resolveAttribute(R.attr.goToTopStyle, var11, true)) {
            this.mGoToTopImageLight = var8.getDrawable(var11.resourceId, null);
        }

        this.mRectColor = var8.getColor(R.color.background_color, null);

        this.mRectPaint.setColor(this.mRectColor);
        this.mRectPaint.setStyle(Style.FILL_AND_STROKE);
        this.mItemAnimator.setHostView(this);
        this.mSeslRoundedCorner = new SeslSubheaderRoundedCorner(this.getContext());
        this.mSeslRoundedCorner.setRoundedCorners(12);
        this.setNestedScrollingEnabled(var5);
    }

    static void clearNestedRecyclerViewIfNotNested(RecyclerView.ViewHolder var0) {
        if (var0.mNestedRecyclerView != null) {
            View var1 = (View) var0.mNestedRecyclerView.get();

            while (true) {
                if (var1 == null) {
                    var0.mNestedRecyclerView = null;
                    break;
                }

                if (var1 == var0.itemView) {
                    break;
                }

                ViewParent var2 = var1.getParent();
                if (var2 instanceof View) {
                    var1 = (View) var2;
                } else {
                    var1 = null;
                }
            }
        }

    }

    static RecyclerView findNestedRecyclerView(View var0) {
        RecyclerView var4;
        if (!(var0 instanceof ViewGroup)) {
            var4 = null;
        } else if (var0 instanceof RecyclerView) {
            var4 = (RecyclerView) var0;
        } else {
            ViewGroup var1 = (ViewGroup) var0;
            int var2 = var1.getChildCount();
            int var3 = 0;

            while (true) {
                if (var3 >= var2) {
                    var4 = null;
                    break;
                }

                var4 = findNestedRecyclerView(var1.getChildAt(var3));
                if (var4 != null) {
                    break;
                }

                ++var3;
            }
        }

        return var4;
    }

    public static RecyclerView.ViewHolder getChildViewHolderInt(View var0) {
        RecyclerView.ViewHolder var1;
        if (var0 == null) {
            var1 = null;
        } else {
            var1 = ((RecyclerView.LayoutParams) var0.getLayoutParams()).mViewHolder;
        }

        return var1;
    }

    static void getDecoratedBoundsWithMarginsInt(View var0, Rect var1) {
        RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var0.getLayoutParams();
        Rect var3 = var2.mDecorInsets;
        var1.set(var0.getLeft() - var3.left - var2.leftMargin, var0.getTop() - var3.top - var2.topMargin, var0.getRight() + var3.right + var2.rightMargin, var0.getBottom() + var3.bottom + var2.bottomMargin);
    }

    private void addAnimatingView(RecyclerView.ViewHolder var1) {
        View var2 = var1.itemView;
        boolean var3;
        if (var2.getParent() == this) {
            var3 = true;
        } else {
            var3 = false;
        }

        this.mRecycler.unscrapView(this.getChildViewHolder(var2));
        if (var1.isTmpDetached()) {
            this.mChildHelper.attachViewToParent(var2, -1, var2.getLayoutParams(), true);
        } else if (!var3) {
            this.mChildHelper.addView(var2, true);
        } else {
            this.mChildHelper.hide(var2);
        }

    }

    private void adjustNestedScrollRange() {
        this.getLocationInWindow(this.mWindowOffsets);
        this.mRemainNestedScrollRange = this.mNestedScrollRange - (this.mInitialTopOffsetOfScreen - this.mWindowOffsets[1]);
        if (this.mInitialTopOffsetOfScreen - this.mWindowOffsets[1] < 0) {
            this.mNestedScrollRange = this.mRemainNestedScrollRange;
            this.mInitialTopOffsetOfScreen = this.mWindowOffsets[1];
        }

    }

    private void adjustNestedScrollRangeBy(int var1) {
        if (this.mHasNestedScrollRange && (!this.canScrollUp() || this.mRemainNestedScrollRange != 0)) {
            this.mRemainNestedScrollRange -= var1;
            if (this.mRemainNestedScrollRange < 0) {
                this.mRemainNestedScrollRange = 0;
            } else if (this.mRemainNestedScrollRange > this.mNestedScrollRange) {
                this.mRemainNestedScrollRange = this.mNestedScrollRange;
            }
        }

    }

    private void animateChange(RecyclerView.ViewHolder var1, RecyclerView.ViewHolder var2, RecyclerView.ItemAnimator.ItemHolderInfo var3, RecyclerView.ItemAnimator.ItemHolderInfo var4, boolean var5, boolean var6) {
        var1.setIsRecyclable(false);
        if (var5) {
            this.addAnimatingView(var1);
        }

        if (var1 != var2) {
            if (var6) {
                this.addAnimatingView(var2);
            }

            var1.mShadowedHolder = var2;
            this.addAnimatingView(var1);
            this.mRecycler.unscrapView(var1);
            var2.setIsRecyclable(false);
            var2.mShadowingHolder = var1;
        }

        if (this.mItemAnimator.animateChange(var1, var2, var3, var4)) {
            this.postAnimationRunner();
        }

    }

    private void autoHide(int var1) {
        if (this.mEnableGoToTop) {
            if (var1 == 0) {
                if (!this.seslIsFastScrollerEnabled()) {
                    this.removeCallbacks(this.mAutoHide);
                    this.postDelayed(this.mAutoHide, (long) this.GO_TO_TOP_HIDE);
                }
            } else if (var1 == 1) {
                this.removeCallbacks(this.mAutoHide);
                this.postDelayed(this.mAutoHide, (long) this.GO_TO_TOP_HIDE);
            }
        }

    }

    private boolean canScrollDown() {
        boolean var1 = false;
        int var2 = this.getChildCount();
        if (this.mAdapter == null) {
            Log.e("SeslRecyclerView", "No adapter attached; skipping canScrollDown");
        } else {
            boolean var3;
            if (this.findFirstChildPosition() + var2 < this.mAdapter.getItemCount()) {
                var3 = true;
            } else {
                var3 = false;
            }

            var1 = var3;
            if (!var3) {
                var1 = var3;
                if (var2 > 0) {
                    if (this.getChildAt(var2 - 1).getBottom() > this.getBottom() - this.mListPadding.bottom) {
                        var1 = true;
                    } else {
                        var1 = false;
                    }
                }
            }
        }

        return var1;
    }

    private boolean canScrollUp() {
        boolean var1;
        if (this.findFirstChildPosition() > 0) {
            var1 = true;
        } else {
            var1 = false;
        }

        boolean var2 = var1;
        if (!var1) {
            var2 = var1;
            if (this.getChildCount() > 0) {
                if (this.getChildAt(0).getTop() < this.getPaddingTop()) {
                    var2 = true;
                } else {
                    var2 = false;
                }
            }
        }

        return var2;
    }

    private void cancelTouch() {
        this.resetTouch();
        this.setScrollState(0);
    }

    private boolean contentFits() {
        boolean var1 = true;
        int var2 = this.getChildCount();
        if (var2 != 0) {
            if (var2 != this.mAdapter.getItemCount()) {
                var1 = false;
            } else if (this.getChildAt(0).getTop() < this.mListPadding.top || this.getChildAt(var2 - 1).getBottom() > this.getHeight() - this.mListPadding.bottom) {
                var1 = false;
            }
        }

        return var1;
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

    private boolean didChildRangeChange(int var1, int var2) {
        boolean var3 = false;
        this.findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        if (this.mMinMaxLayoutPositions[0] != var1 || this.mMinMaxLayoutPositions[1] != var2) {
            var3 = true;
        }

        return var3;
    }

    @SuppressLint("WrongConstant")
    private void dispatchContentChangedIfNecessary() {
        int var1 = this.mEatenAccessibilityChangeFlags;
        this.mEatenAccessibilityChangeFlags = 0;
        if (var1 != 0 && this.isAccessibilityEnabled()) {
            AccessibilityEvent var2 = AccessibilityEvent.obtain();
            var2.setEventType(2048);
            AccessibilityEventCompat.setContentChangeTypes(var2, var1);
            this.sendAccessibilityEventUnchecked(var2);
        }

    }

    private void dispatchLayoutStep1() {
        this.mState.assertLayoutStep(1);
        this.fillRemainingScrollValues(this.mState);
        this.mState.mIsMeasuring = false;
        this.startInterceptRequestLayout();
        this.mViewInfoStore.clear();
        this.onEnterLayoutOrScroll();
        this.processAdapterUpdatesAndSetAnimationFlags();
        this.saveFocusInfo();
        RecyclerView.State var1 = this.mState;
        boolean var2;
        if (this.mState.mRunSimpleAnimations && this.mItemsChanged) {
            var2 = true;
        } else {
            var2 = false;
        }

        var1.mTrackOldChangeHolders = var2;
        this.mItemsChanged = false;
        this.mItemsAddedOrRemoved = false;
        this.mState.mInPreLayout = this.mState.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        int var3;
        int var4;
        if (this.mState.mRunSimpleAnimations) {
            var3 = this.mChildHelper.getChildCount();

            for (var4 = 0; var4 < var3; ++var4) {
                RecyclerView.ViewHolder var5 = getChildViewHolderInt(this.mChildHelper.getChildAt(var4));
                if (!var5.shouldIgnore() && (!var5.isInvalid() || this.mAdapter.hasStableIds())) {
                    RecyclerView.ItemAnimator.ItemHolderInfo var9 = this.mItemAnimator.recordPreLayoutInformation(this.mState, var5, RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(var5), var5.getUnmodifiedPayloads());
                    this.mViewInfoStore.addToPreLayout(var5, var9);
                    if (this.mState.mTrackOldChangeHolders && var5.isUpdated() && !var5.isRemoved() && !var5.shouldIgnore() && !var5.isInvalid()) {
                        long var6 = this.getChangedHolderKey(var5);
                        this.mViewInfoStore.addToOldChangeHolders(var6, var5);
                    }
                }
            }
        }

        if (this.mState.mRunPredictiveAnimations) {
            this.saveOldPositions();
            var2 = this.mState.mStructureChanged;
            this.mState.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
            this.mState.mStructureChanged = var2;

            for (var4 = 0; var4 < this.mChildHelper.getChildCount(); ++var4) {
                RecyclerView.ViewHolder var10 = getChildViewHolderInt(this.mChildHelper.getChildAt(var4));
                if (!var10.shouldIgnore() && !this.mViewInfoStore.isInPreLayout(var10)) {
                    int var8 = RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(var10);
                    var2 = var10.hasAnyOfTheFlags(8192);
                    var3 = var8;
                    if (!var2) {
                        var3 = var8 | 4096;
                    }

                    RecyclerView.ItemAnimator.ItemHolderInfo var11 = this.mItemAnimator.recordPreLayoutInformation(this.mState, var10, var3, var10.getUnmodifiedPayloads());
                    if (var2) {
                        this.recordAnimationInfoIfBouncedHiddenView(var10, var11);
                    } else {
                        this.mViewInfoStore.addToAppearedInPreLayoutHolders(var10, var11);
                    }
                }
            }

            this.clearOldPositions();
        } else {
            this.clearOldPositions();
        }

        this.onExitLayoutOrScroll();
        this.stopInterceptRequestLayout(false);
        this.mState.mLayoutStep = 2;
    }

    private void dispatchLayoutStep2() {
        this.startInterceptRequestLayout();
        this.onEnterLayoutOrScroll();
        this.mState.assertLayoutStep(6);
        this.mAdapterHelper.consumeUpdatesInOnePass();
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        this.mState.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
        this.mState.mStructureChanged = false;
        this.mPendingSavedState = null;
        RecyclerView.State var1 = this.mState;
        boolean var2;
        if (this.mState.mRunSimpleAnimations && this.mItemAnimator != null) {
            var2 = true;
        } else {
            var2 = false;
        }

        var1.mRunSimpleAnimations = var2;
        this.mState.mLayoutStep = 4;
        this.onExitLayoutOrScroll();
        this.stopInterceptRequestLayout(false);
    }

    private void dispatchLayoutStep3() {
        this.mState.assertLayoutStep(4);
        this.startInterceptRequestLayout();
        this.onEnterLayoutOrScroll();
        this.mState.mLayoutStep = 1;
        int var1;
        if (this.mState.mRunSimpleAnimations) {
            for (var1 = this.mChildHelper.getChildCount() - 1; var1 >= 0; --var1) {
                RecyclerView.ViewHolder var2 = getChildViewHolderInt(this.mChildHelper.getChildAt(var1));
                if (!var2.shouldIgnore()) {
                    long var3 = this.getChangedHolderKey(var2);
                    RecyclerView.ItemAnimator.ItemHolderInfo var5 = this.mItemAnimator.recordPostLayoutInformation(this.mState, var2);
                    RecyclerView.ViewHolder var6 = this.mViewInfoStore.getFromOldChangeHolders(var3);
                    if (var6 != null && !var6.shouldIgnore()) {
                        boolean var7 = this.mViewInfoStore.isDisappearing(var6);
                        boolean var8 = this.mViewInfoStore.isDisappearing(var2);
                        if (var7 && var6 == var2) {
                            this.mViewInfoStore.addToPostLayout(var2, var5);
                        } else {
                            RecyclerView.ItemAnimator.ItemHolderInfo var9 = this.mViewInfoStore.popFromPreLayout(var6);
                            this.mViewInfoStore.addToPostLayout(var2, var5);
                            var5 = this.mViewInfoStore.popFromPostLayout(var2);
                            if (var9 == null) {
                                this.handleMissingPreInfoForChangeError(var3, var2, var6);
                            } else {
                                this.animateChange(var6, var2, var9, var5, var7, var8);
                            }
                        }
                    } else {
                        this.mViewInfoStore.addToPostLayout(var2, var5);
                    }
                }
            }

            this.mViewInfoStore.process(this.mViewInfoProcessCallback);
        }

        this.mLastBlackTop = this.mBlackTop;
        this.mBlackTop = -1;
        if (this.mDrawRect && !this.canScrollVertically(-1) && !this.canScrollVertically(1)) {
            var1 = this.mAdapter.getItemCount() - 1;
            SeslLinearLayoutManager var11 = (SeslLinearLayoutManager) this.mLayout;
            if (var11.mReverseLayout && var11.mStackFromEnd) {
                this.mDrawReverse = true;
                var1 = 0;
            } else if (var11.mReverseLayout || var11.mStackFromEnd) {
                this.mDrawRect = false;
                var1 = -1;
            }

            if (var1 >= 0 && var1 <= this.findLastVisibleItemPosition()) {
                View var12 = this.mChildHelper.getChildAt(var1);
                if (var12 != null) {
                    this.mBlackTop = var12.getBottom();
                    var12 = this.getChildAt(var1);
                    int var10 = -1;
                    if (var12 != null) {
                        var10 = var12.getBottom();
                    }

                    Log.d("SeslRecyclerView", "dispatchLayoutStep3, lastPosition : " + var1 + ", mBlackTop : " + this.mBlackTop + " tempView bottom : " + var10 + ", mDrawReverse : " + this.mDrawReverse);
                }
            }
        }

        this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        this.mState.mPreviousLayoutItemCount = this.mState.mItemCount;
        this.mDataSetHasChangedAfterLayout = false;
        this.mDispatchItemsChangedEvent = false;
        this.mState.mRunSimpleAnimations = false;
        this.mState.mRunPredictiveAnimations = false;
        this.mLayout.mRequestedSimpleAnimations = false;
        if (this.mRecycler.mChangedScrap != null) {
            this.mRecycler.mChangedScrap.clear();
        }

        if (this.mLayout.mPrefetchMaxObservedInInitialPrefetch) {
            this.mLayout.mPrefetchMaxCountObserved = 0;
            this.mLayout.mPrefetchMaxObservedInInitialPrefetch = false;
            this.mRecycler.updateViewCacheSize();
        }

        this.mLayout.onLayoutCompleted(this.mState);
        this.onExitLayoutOrScroll();
        this.stopInterceptRequestLayout(false);
        this.mViewInfoStore.clear();
        if (this.didChildRangeChange(this.mMinMaxLayoutPositions[0], this.mMinMaxLayoutPositions[1])) {
            this.dispatchOnScrolled(0, 0);
        }

        this.recoverFocusFromState();
        this.resetFocusInfo();
    }

    private boolean dispatchOnItemTouch(MotionEvent var1) {
        boolean var2 = true;
        int var3 = var1.getAction();
        boolean var6;
        if (this.mActiveOnItemTouchListener != null) {
            if (var3 != 0) {
                this.mActiveOnItemTouchListener.onTouchEvent(this, var1);
                if (var3 != 3) {
                    var6 = var2;
                    if (var3 != 1) {
                        return var6;
                    }
                }

                this.mActiveOnItemTouchListener = null;
                var6 = var2;
                return var6;
            }

            this.mActiveOnItemTouchListener = null;
        }

        if (var3 != 0) {
            int var4 = this.mOnItemTouchListeners.size();

            for (var3 = 0; var3 < var4; ++var3) {
                RecyclerView.OnItemTouchListener var5 = (RecyclerView.OnItemTouchListener) this.mOnItemTouchListeners.get(var3);
                if (var5.onInterceptTouchEvent(this, var1)) {
                    this.mActiveOnItemTouchListener = var5;
                    var6 = var2;
                    return var6;
                }
            }
        }

        var6 = false;
        return var6;
    }

    private boolean dispatchOnItemTouchIntercept(MotionEvent var1) {
        int var2 = var1.getAction();
        if (var2 == 3 || var2 == 0) {
            this.mActiveOnItemTouchListener = null;
        }

        int var3 = this.mOnItemTouchListeners.size();
        int var4 = 0;

        boolean var6;
        while (true) {
            if (var4 >= var3) {
                var6 = false;
                break;
            }

            RecyclerView.OnItemTouchListener var5 = (RecyclerView.OnItemTouchListener) this.mOnItemTouchListeners.get(var4);
            if (var5.onInterceptTouchEvent(this, var1) && var2 != 3) {
                this.mActiveOnItemTouchListener = var5;
                var6 = true;
                break;
            }

            ++var4;
        }

        return var6;
    }

    private void drawGoToTop() {
        int var1 = this.getScrollY();
        this.mGoToTopView.setTranslationY((float) var1);
        if (this.mGoToTopState != 0 && !this.canScrollUp()) {
            this.setupGoToTop(0);
        }

        this.mGoToTopView.invalidate();
    }

    private int findFirstChildPosition() {
        int var1 = 0;
        if (this.mLayout instanceof SeslLinearLayoutManager) {
            var1 = ((SeslLinearLayoutManager) this.mLayout).findFirstVisibleItemPosition();
        } else if (this.mLayout instanceof StaggeredGridLayoutManager) {
            if (this.mLayout.getLayoutDirection() == 1) {
                var1 = ((StaggeredGridLayoutManager) this.mLayout).getSpanCount() - 1;
            } else {
                var1 = 0;
            }

            var1 = ((StaggeredGridLayoutManager) this.mLayout).findFirstVisibleItemPositions((int[]) null)[var1];
        }

        int var2 = var1;
        if (var1 == -1) {
            var2 = 0;
        }

        return var2;
    }

    private void findMinMaxChildLayoutPositions(int[] var1) {
        int var2 = this.mChildHelper.getChildCount();
        if (var2 == 0) {
            var1[0] = -1;
            var1[1] = -1;
        } else {
            int var3 = 2147483647;
            int var4 = -2147483648;

            int var7;
            for (int var5 = 0; var5 < var2; var4 = var7) {
                RecyclerView.ViewHolder var6 = getChildViewHolderInt(this.mChildHelper.getChildAt(var5));
                if (var6.shouldIgnore()) {
                    var7 = var4;
                } else {
                    int var8 = var6.getLayoutPosition();
                    int var9 = var3;
                    if (var8 < var3) {
                        var9 = var8;
                    }

                    var7 = var4;
                    var3 = var9;
                    if (var8 > var4) {
                        var7 = var8;
                        var3 = var9;
                    }
                }

                ++var5;
            }

            var1[0] = var3;
            var1[1] = var4;
        }

    }

    private View findNextViewToFocus() {
        Object var1 = null;
        int var2;
        if (this.mState.mFocusedItemPosition != -1) {
            var2 = this.mState.mFocusedItemPosition;
        } else {
            var2 = 0;
        }

        int var3 = this.mState.getItemCount();
        int var4 = var2;

        while (true) {
            RecyclerView.ViewHolder var5;
            View var6;
            if (var4 < var3) {
                var5 = this.findViewHolderForAdapterPosition(var4);
                if (var5 != null) {
                    if (var5.itemView.hasFocusable()) {
                        var6 = var5.itemView;
                        return var6;
                    }

                    ++var4;
                    continue;
                }
            }

            var2 = Math.min(var3, var2) - 1;

            while (true) {
                var6 = (View) var1;
                if (var2 < 0) {
                    return var6;
                }

                var5 = this.findViewHolderForAdapterPosition(var2);
                if (var5 == null) {
                    var6 = (View) var1;
                    return var6;
                }

                if (var5.itemView.hasFocusable()) {
                    var6 = var5.itemView;
                    return var6;
                }

                --var2;
            }
        }
    }

    private int getDeepestFocusedViewWithId(View var1) {
        int var2 = var1.getId();

        while (!var1.isFocused() && var1 instanceof ViewGroup && var1.hasFocus()) {
            View var3 = ((ViewGroup) var1).getFocusedChild();
            var1 = var3;
            if (var3.getId() != -1) {
                var2 = var3.getId();
                var1 = var3;
            }
        }

        return var2;
    }

    private String getFullClassName(Context var1, String var2) {
        String var3;
        if (var2.charAt(0) == '.') {
            var3 = var1.getPackageName() + var2;
        } else {
            var3 = var2;
            if (!var2.contains(".")) {
                var3 = RecyclerView.class.getPackage().getName() + '.' + var2;
            }
        }

        return var3;
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (this.mScrollingChildHelper == null) {
            this.mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }

        return this.mScrollingChildHelper;
    }

    private void handleMissingPreInfoForChangeError(long var1, RecyclerView.ViewHolder var3, RecyclerView.ViewHolder var4) {
        int var5 = this.mChildHelper.getChildCount();

        for (int var6 = 0; var6 < var5; ++var6) {
            RecyclerView.ViewHolder var7 = getChildViewHolderInt(this.mChildHelper.getChildAt(var6));
            if (var7 != var3 && this.getChangedHolderKey(var7) == var1) {
                if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
                    throw new IllegalStateException("Two different ViewHolders have the same stable ID. Stable IDs in your adapter MUST BE unique and SHOULD NOT change.\n ViewHolder 1:" + var7 + " \n View Holder 2:" + var3 + this.exceptionLabel());
                }

                throw new IllegalStateException("Two different ViewHolders have the same change ID. This might happen due to inconsistent Adapter update events or if the LayoutManager lays out the same View multiple times.\n ViewHolder 1:" + var7 + " \n View Holder 2:" + var3 + this.exceptionLabel());
            }
        }

        Log.e("SeslRecyclerView", "Problem while matching changed view holders with the newones. The pre-layout information for the change holder " + var4 + " cannot be found but it is necessary for " + var3 + this.exceptionLabel());
    }

    private boolean hasUpdatedView() {
        int var1 = this.mChildHelper.getChildCount();
        int var2 = 0;

        boolean var4;
        while (true) {
            if (var2 >= var1) {
                var4 = false;
                break;
            }

            RecyclerView.ViewHolder var3 = getChildViewHolderInt(this.mChildHelper.getChildAt(var2));
            if (var3 != null && !var3.shouldIgnore() && var3.isUpdated()) {
                var4 = true;
                break;
            }

            ++var2;
        }

        return var4;
    }

    private void initChildrenHelper() {
        this.mChildHelper = new SeslChildHelper(new SeslChildHelper.Callback() {
            public void addView(View var1, int var2) {
                RecyclerView.this.addView(var1, var2);
                RecyclerView.this.dispatchChildAttached(var1);
            }

            public void attachViewToParent(View var1, int var2, android.view.ViewGroup.LayoutParams var3) {
                RecyclerView.ViewHolder var4 = RecyclerView.getChildViewHolderInt(var1);
                if (var4 != null) {
                    if (!var4.isTmpDetached() && !var4.shouldIgnore()) {
                        throw new IllegalArgumentException("Called attach on a child which is not detached: " + var4 + RecyclerView.this.exceptionLabel());
                    }

                    var4.clearTmpDetachFlag();
                }

                RecyclerView.this.attachViewToParent(var1, var2, var3);
            }

            public void detachViewFromParent(int var1) {
                View var2 = this.getChildAt(var1);
                if (var2 != null) {
                    RecyclerView.ViewHolder var3 = RecyclerView.getChildViewHolderInt(var2);
                    if (var3 != null) {
                        if (var3.isTmpDetached() && !var3.shouldIgnore()) {
                            throw new IllegalArgumentException("called detach on an already detached child " + var3 + RecyclerView.this.exceptionLabel());
                        }

                        var3.addFlags(256);
                    }
                }

                RecyclerView.this.detachViewFromParent(var1);
            }

            public View getChildAt(int var1) {
                return RecyclerView.this.getChildAt(var1);
            }

            public int getChildCount() {
                return RecyclerView.this.getChildCount();
            }

            public RecyclerView.ViewHolder getChildViewHolder(View var1) {
                return RecyclerView.getChildViewHolderInt(var1);
            }

            public int indexOfChild(View var1) {
                return RecyclerView.this.indexOfChild(var1);
            }

            public void onEnteredHiddenState(View var1) {
                RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
                if (var2 != null) {
                    var2.onEnteredHiddenState(RecyclerView.this);
                }

            }

            public void onLeftHiddenState(View var1) {
                RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
                if (var2 != null) {
                    var2.onLeftHiddenState(RecyclerView.this);
                }

            }

            public void removeAllViews() {
                int var1 = this.getChildCount();

                for (int var2 = 0; var2 < var1; ++var2) {
                    View var3 = this.getChildAt(var2);
                    RecyclerView.this.dispatchChildDetached(var3);
                    var3.clearAnimation();
                }

                RecyclerView.this.removeAllViews();
            }

            public void removeViewAt(int var1) {
                View var2 = RecyclerView.this.getChildAt(var1);
                if (var2 != null) {
                    RecyclerView.this.dispatchChildDetached(var2);
                    var2.clearAnimation();
                }

                RecyclerView.this.removeViewAt(var1);
            }
        });
    }

    private boolean isInDialog() {
        boolean var1 = false;
        if (this.mRootViewCheckForDialog == null) {
            this.mRootViewCheckForDialog = this.getRootView();
            if (this.mRootViewCheckForDialog == null) {
                var1 = false;
            } else {
                Context var2 = this.mRootViewCheckForDialog.getContext();
                if (var2 instanceof Activity && ((Activity) var2).getWindow().getAttributes().type == 1) {
                    var1 = false;
                } else {
                    var1 = true;
                }
            }
        }

        return var1;
    }

    private boolean isNavigationBarHide(Context var1) {
        boolean var2 = true;
        boolean var3 = var2;
        if (this.isSupportSoftNavigationBar(var1)) {
            if (Global.getInt(var1.getContentResolver(), "navigationbar_hide_bar_enabled", 0) == 1) {
                var3 = var2;
            } else {
                var3 = false;
            }
        }

        return var3;
    }

    private boolean isPreferredNextFocus(View var1, View var2, int var3) {
        boolean var4 = true;
        boolean var5 = false;
        boolean var6 = false;
        boolean var7;
        if (var2 != null && var2 != this && var1 != var2) {
            if (this.findContainingItemView(var2) == null) {
                var7 = false;
            } else {
                var7 = var4;
                if (var1 != null) {
                    var7 = var4;
                    if (this.findContainingItemView(var1) != null) {
                        this.mTempRect.set(0, 0, var1.getWidth(), var1.getHeight());
                        this.mTempRect2.set(0, 0, var2.getWidth(), var2.getHeight());
                        this.offsetDescendantRectToMyCoords(var1, this.mTempRect);
                        this.offsetDescendantRectToMyCoords(var2, this.mTempRect2);
                        byte var8;
                        if (this.mLayout.getLayoutDirection() == 1) {
                            var8 = -1;
                        } else {
                            var8 = 1;
                        }

                        byte var9 = 0;
                        byte var10;
                        if ((this.mTempRect.left < this.mTempRect2.left || this.mTempRect.right <= this.mTempRect2.left) && this.mTempRect.right < this.mTempRect2.right) {
                            var10 = 1;
                        } else {
                            label109:
                            {
                                if (this.mTempRect.right <= this.mTempRect2.right) {
                                    var10 = var9;
                                    if (this.mTempRect.left < this.mTempRect2.right) {
                                        break label109;
                                    }
                                }

                                var10 = var9;
                                if (this.mTempRect.left > this.mTempRect2.left) {
                                    var10 = -1;
                                }
                            }
                        }

                        byte var11 = 0;
                        byte var12;
                        if ((this.mTempRect.top < this.mTempRect2.top || this.mTempRect.bottom <= this.mTempRect2.top) && this.mTempRect.bottom < this.mTempRect2.bottom) {
                            var12 = 1;
                        } else {
                            label110:
                            {
                                if (this.mTempRect.bottom <= this.mTempRect2.bottom) {
                                    var12 = var11;
                                    if (this.mTempRect.top < this.mTempRect2.bottom) {
                                        break label110;
                                    }
                                }

                                var12 = var11;
                                if (this.mTempRect.top > this.mTempRect2.top) {
                                    var12 = -1;
                                }
                            }
                        }

                        switch (var3) {
                            case 1:
                                if (var12 >= 0) {
                                    var7 = var5;
                                    if (var12 != 0) {
                                        break;
                                    }

                                    var7 = var5;
                                    if (var10 * var8 > 0) {
                                        break;
                                    }
                                }

                                var7 = true;
                                break;
                            case 2:
                                if (var12 <= 0) {
                                    var7 = var6;
                                    if (var12 != 0) {
                                        break;
                                    }

                                    var7 = var6;
                                    if (var10 * var8 < 0) {
                                        break;
                                    }
                                }

                                var7 = true;
                                break;
                            case 17:
                                var7 = var4;
                                if (var10 >= 0) {
                                    var7 = false;
                                }
                                break;
                            case 33:
                                var7 = var4;
                                if (var12 >= 0) {
                                    var7 = false;
                                }
                                break;
                            case 66:
                                var7 = var4;
                                if (var10 <= 0) {
                                    var7 = false;
                                }
                                break;
                            case 130:
                                var7 = var4;
                                if (var12 <= 0) {
                                    var7 = false;
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid direction: " + var3 + this.exceptionLabel());
                        }
                    }
                }
            }
        } else {
            var7 = false;
        }

        return var7;
    }

    private boolean isSupportGotoTop() {
        boolean var1;
        if (!this.isTalkBackIsRunning() && this.mEnableGoToTop) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    private boolean isSupportSoftNavigationBar(Context var1) {
        int var2 = var1.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        boolean var3;
        if (var2 > 0 && var1.getResources().getBoolean(var2)) {
            var3 = true;
        } else {
            var3 = false;
        }

        return var3;
    }

    private boolean isTalkBackIsRunning() {
        AccessibilityManager var1 = (AccessibilityManager) this.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        boolean var2;
        if (var1 != null && var1.isEnabled()) {
            String var3 = Secure.getString(this.getContext().getContentResolver(), "enabled_accessibility_services");
            if (var3 != null && (var3.matches("(?i).*com.samsung.accessibility/com.samsung.android.app.talkback.TalkBackService.*") || var3.matches("(?i).*com.google.android.marvin.talkback.TalkBackService.*") || var3.matches("(?i).*com.samsung.accessibility/com.samsung.accessibility.universalswitch.UniversalSwitchService.*"))) {
                var2 = true;
                return var2;
            }
        }

        var2 = false;
        return var2;
    }

    private void multiSelection(int var1, int var2, int var3, int var4, boolean var5) {
        if (this.mIsNeedPenSelection) {
            if (this.mIsFirstPenMoveEvent) {
                this.mPenDragStartX = var1;
                this.mPenDragStartY = var2;
                this.mIsPenPressed = true;
                this.mPenTrackedChild = this.findChildViewUnder((float) var1, (float) var2);
                if (this.mPenTrackedChild == null) {
                    this.mPenTrackedChild = this.seslFindNearChildViewUnder((float) var1, (float) var2);
                    if (this.mPenTrackedChild == null) {
                        Log.e("SeslRecyclerView", "multiSelection, mPenTrackedChild is NULL");
                        this.mIsPenPressed = false;
                        this.mIsFirstPenMoveEvent = false;
                        return;
                    }
                }

                if (this.mOnMultiSelectedListener != null) {
                    this.mOnMultiSelectedListener.onMultiSelectStart(var1, var2);
                }

                this.mPenTrackedChildPosition = this.getChildLayoutPosition(this.mPenTrackedChild);
                this.mPenDistanceFromTrackedChildTop = this.mPenDragStartY - this.mPenTrackedChild.getTop();
                this.mIsFirstPenMoveEvent = false;
            }

            if (this.mPenDragStartX == 0 && this.mPenDragStartY == 0) {
                this.mPenDragStartX = var1;
                this.mPenDragStartY = var2;
                if (this.mOnMultiSelectedListener != null) {
                    this.mOnMultiSelectedListener.onMultiSelectStart(var1, var2);
                }

                this.mIsPenPressed = true;
            }

            this.mPenDragEndX = var1;
            this.mPenDragEndY = var2;
            if (this.mPenDragEndY < 0) {
                this.mPenDragEndY = 0;
            } else if (this.mPenDragEndY > var4) {
                this.mPenDragEndY = var4;
            }

            if (this.mPenDragStartX < this.mPenDragEndX) {
                var1 = this.mPenDragStartX;
            } else {
                var1 = this.mPenDragEndX;
            }

            this.mPenDragBlockLeft = var1;
            if (this.mPenDragStartY < this.mPenDragEndY) {
                var1 = this.mPenDragStartY;
            } else {
                var1 = this.mPenDragEndY;
            }

            this.mPenDragBlockTop = var1;
            if (this.mPenDragEndX > this.mPenDragStartX) {
                var1 = this.mPenDragEndX;
            } else {
                var1 = this.mPenDragStartX;
            }

            this.mPenDragBlockRight = var1;
            if (this.mPenDragEndY > this.mPenDragStartY) {
                var1 = this.mPenDragEndY;
            } else {
                var1 = this.mPenDragStartY;
            }

            this.mPenDragBlockBottom = var1;
            var5 = true;
        }

        if (var5) {
            if (var2 <= this.mHoverTopAreaHeight + var3) {
                if (!this.mHoverAreaEnter) {
                    this.mHoverAreaEnter = true;
                    this.mHoverScrollStartTime = System.currentTimeMillis();
                    if (this.mScrollListener != null) {
                        this.mScrollListener.onScrollStateChanged(this, 1);
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
                    if (this.mScrollListener != null) {
                        this.mScrollListener.onScrollStateChanged(this, 1);
                    }
                }

                if (!this.mHoverHandler.hasMessages(0)) {
                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                    this.mHoverScrollDirection = 1;
                    this.mHoverHandler.sendEmptyMessage(0);
                }
            } else {
                if (this.mHoverAreaEnter && this.mScrollListener != null) {
                    this.mScrollListener.onScrollStateChanged(this, 0);
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

    private void multiSelectionEnd(int var1, int var2) {
        if (this.mIsPenPressed && this.mOnMultiSelectedListener != null) {
            this.mOnMultiSelectedListener.onMultiSelectStop(var1, var2);
        }

        this.mIsPenPressed = false;
        this.mIsFirstPenMoveEvent = true;
        this.mPenDragSelectedViewPosition = -1;
        this.mPenDragSelectedItemArray.clear();
        this.mPenDragStartX = 0;
        this.mPenDragStartY = 0;
        this.mPenDragEndX = 0;
        this.mPenDragEndY = 0;
        this.mPenDragBlockLeft = 0;
        this.mPenDragBlockTop = 0;
        this.mPenDragBlockRight = 0;
        this.mPenDragBlockBottom = 0;
        this.mPenTrackedChild = null;
        this.mPenDistanceFromTrackedChildTop = 0;
        if (this.mIsPenDragBlockEnabled) {
            this.invalidate();
        }

        if (this.mHoverHandler.hasMessages(0)) {
            this.mHoverHandler.removeMessages(0);
        }

    }

    private void onPointerUp(MotionEvent var1) {
        int var2 = var1.getActionIndex();
        if (var1.getPointerId(var2) == this.mScrollPointerId) {
            byte var4;
            if (var2 == 0) {
                var4 = 1;
            } else {
                var4 = 0;
            }

            this.mScrollPointerId = var1.getPointerId(var4);
            int var3 = (int) (var1.getX(var4) + 0.5F);
            this.mLastTouchX = var3;
            this.mInitialTouchX = var3;
            var2 = (int) (var1.getY(var4) + 0.5F);
            this.mLastTouchY = var2;
            this.mInitialTouchY = var2;
        }

    }

    private boolean pageScroll(int var1) {
        boolean var2 = false;
        boolean var3;
        if (this.mAdapter == null) {
            Log.e("SeslRecyclerView", "No adapter attached; skipping pageScroll");
            var3 = var2;
        } else {
            int var4 = this.mAdapter.getItemCount();
            var3 = var2;
            if (var4 > 0) {
                switch (var1) {
                    case 0:
                        var1 = this.findFirstVisibleItemPosition() - this.getChildCount();
                        break;
                    case 1:
                        var1 = this.findLastVisibleItemPosition() + this.getChildCount();
                        break;
                    case 2:
                        var1 = 0;
                        break;
                    case 3:
                        var1 = var4 - 1;
                        break;
                    default:
                        var3 = var2;
                        return var3;
                }

                if (var1 > var4 - 1) {
                    --var4;
                } else {
                    var4 = var1;
                    if (var1 < 0) {
                        var4 = 0;
                    }
                }

                this.mLayout.mRecyclerView.scrollToPosition(var4);
                this.mLayout.mRecyclerView.post(new Runnable() {
                    public void run() {
                        View var1 = RecyclerView.this.getChildAt(0);
                        if (var1 != null) {
                            var1.requestFocus();
                        }

                    }
                });
                var3 = true;
            }
        }

        return var3;
    }

    private void playGotoToFadeIn() {
        if (!this.mGoToTopFadeInAnimator.isRunning()) {
            if (this.mGoToTopFadeOutAnimator.isRunning()) {
                this.mGoToTopFadeOutAnimator.cancel();
            }

            this.mGoToTopFadeInAnimator.setFloatValues(new float[]{this.mGoToTopView.getAlpha(), 1.0F});
            this.mGoToTopFadeInAnimator.start();
        }
    }

    private void playGotoToFadeOut() {
        if (!this.mGoToTopFadeOutAnimator.isRunning()) {
            if (this.mGoToTopFadeInAnimator.isRunning()) {
                this.mGoToTopFadeOutAnimator.cancel();
            }

            this.mGoToTopFadeOutAnimator.setFloatValues(new float[]{this.mGoToTopView.getAlpha(), 0.0F});
            this.mGoToTopFadeOutAnimator.start();
        }
    }

    private boolean predictiveItemAnimationsEnabled() {
        boolean var1;
        if (this.mItemAnimator != null && this.mLayout.supportsPredictiveItemAnimations()) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        boolean var1 = true;
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            if (this.mDispatchItemsChangedEvent) {
                this.mLayout.onItemsChanged(this);
            }
        }

        if (this.predictiveItemAnimationsEnabled()) {
            this.mAdapterHelper.preProcess();
        } else {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        }

        boolean var2;
        if (!this.mItemsAddedOrRemoved && !this.mItemsChanged) {
            var2 = false;
        } else {
            var2 = true;
        }

        RecyclerView.State var3 = this.mState;
        boolean var4;
        if (!this.mFirstLayoutComplete || this.mItemAnimator == null || !this.mDataSetHasChangedAfterLayout && !var2 && !this.mLayout.mRequestedSimpleAnimations || this.mDataSetHasChangedAfterLayout && !this.mAdapter.hasStableIds()) {
            var4 = false;
        } else {
            var4 = true;
        }

        var3.mRunSimpleAnimations = var4;
        var3 = this.mState;
        if (this.mState.mRunSimpleAnimations && var2 && !this.mDataSetHasChangedAfterLayout && this.predictiveItemAnimationsEnabled()) {
            var4 = var1;
        } else {
            var4 = false;
        }

        var3.mRunPredictiveAnimations = var4;
    }

    private void pullGlows(float var1, float var2, float var3, float var4) {
        boolean var5 = false;
        if (var2 < 0.0F) {
            this.ensureLeftGlow();
            this.mLeftGlow.onPull(-var2 / (float) this.getWidth(), 1.0F - var3 / (float) this.getHeight());
            var5 = true;
        } else if (var2 > 0.0F) {
            this.ensureRightGlow();
            this.mRightGlow.onPull(var2 / (float) this.getWidth(), var3 / (float) this.getHeight());
            var5 = true;
        }

        if (var4 < 0.0F) {
            this.ensureTopGlow();
            this.mTopGlow.onPull(-var4 / (float) this.getHeight(), var1 / (float) this.getWidth());
            var5 = true;
        } else if (var4 > 0.0F) {
            this.ensureBottomGlow();
            this.mBottomGlow.onPull(var4 / (float) this.getHeight(), 1.0F - var1 / (float) this.getWidth());
            var5 = true;
        }

        if (var5 || var2 != 0.0F || var4 != 0.0F) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    // KANG FROM OLD JAVA
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

    private void releaseGlows() {
        boolean var1 = false;
        if (this.mLeftGlow != null) {
            this.mLeftGlow.onRelease();
            var1 = this.mLeftGlow.isFinished();
        }

        boolean var2 = var1;
        if (this.mTopGlow != null) {
            this.mTopGlow.onRelease();
            var2 = var1 | this.mTopGlow.isFinished();
        }

        var1 = var2;
        if (this.mRightGlow != null) {
            this.mRightGlow.onRelease();
            var1 = var2 | this.mRightGlow.isFinished();
        }

        var2 = var1;
        if (this.mBottomGlow != null) {
            this.mBottomGlow.onRelease();
            var2 = var1 | this.mBottomGlow.isFinished();
        }

        if (var2) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    private void requestChildOnScreen(View var1, View var2) {
        boolean var3 = true;
        View var4;
        if (var2 != null) {
            var4 = var2;
        } else {
            var4 = var1;
        }

        this.mTempRect.set(0, 0, var4.getWidth(), var4.getHeight());
        android.view.ViewGroup.LayoutParams var7 = var4.getLayoutParams();
        Rect var5;
        if (var7 instanceof RecyclerView.LayoutParams) {
            RecyclerView.LayoutParams var8 = (RecyclerView.LayoutParams) var7;
            if (!var8.mInsetsDirty) {
                Rect var9 = var8.mDecorInsets;
                var5 = this.mTempRect;
                var5.left -= var9.left;
                var5 = this.mTempRect;
                var5.right += var9.right;
                var5 = this.mTempRect;
                var5.top -= var9.top;
                var5 = this.mTempRect;
                var5.bottom += var9.bottom;
            }
        }

        if (var2 != null) {
            this.offsetDescendantRectToMyCoords(var2, this.mTempRect);
            this.offsetRectIntoDescendantCoords(var1, this.mTempRect);
        }

        RecyclerView.LayoutManager var10 = this.mLayout;
        var5 = this.mTempRect;
        boolean var6;
        if (!this.mFirstLayoutComplete) {
            var6 = true;
        } else {
            var6 = false;
        }

        if (var2 != null) {
            var3 = false;
        }

        var10.requestChildRectangleOnScreen(this, var1, var5, var6, var3);
    }

    private void resetFocusInfo() {
        this.mState.mFocusedItemId = -1L;
        this.mState.mFocusedItemPosition = -1;
        this.mState.mFocusedSubChildId = -1;
    }

    @SuppressLint("WrongConstant")
    private void resetTouch() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
        }

        this.stopNestedScroll(0);
        this.releaseGlows();
    }

    // KANG FROM OLD JAVA
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
            mState.mFocusedItemPosition = mDataSetHasChangedAfterLayout ? NO_POSITION : (focusedVh.isRemoved() ? focusedVh.mOldPosition : focusedVh.getAdapterPosition());
            mState.mFocusedSubChildId = getDeepestFocusedViewWithId(focusedVh.itemView);
        }
    }

    private void setAdapterInternal(RecyclerView.Adapter var1, boolean var2, boolean var3) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterAdapterDataObserver(this.mObserver);
            this.mAdapter.onDetachedFromRecyclerView(this);
        }

        if (!var2 || var3) {
            this.removeAndRecycleViews();
        }

        this.mAdapterHelper.reset();
        RecyclerView.Adapter var4 = this.mAdapter;
        this.mAdapter = var1;
        if (var1 != null) {
            var1.registerAdapterDataObserver(this.mObserver);
            var1.onAttachedToRecyclerView(this);
        }

        if (this.mLayout != null) {
            this.mLayout.onAdapterChanged(var4, this.mAdapter);
        }

        this.mRecycler.onAdapterChanged(var4, this.mAdapter, var2);
        this.mState.mStructureChanged = true;
    }

    private void setupGoToTop(int var1) {
        if (this.mEnableGoToTop) {
            this.removeCallbacks(this.mAutoHide);
            int var2 = this.mGoToTopLastState;
            var2 = var1;
            if (var1 == 1) {
                var2 = var1;
                if (!this.canScrollUp()) {
                    var2 = 0;
                }
            }

            if (var2 == -1 && this.mSizeChnage) {
                if (!this.canScrollUp() && !this.canScrollDown()) {
                    var1 = 0;
                } else {
                    var1 = this.mGoToTopLastState;
                }
            } else {
                var1 = var2;
                if (var2 == -1) {
                    label74:
                    {
                        if (!this.canScrollUp()) {
                            var1 = var2;
                            if (!this.canScrollDown()) {
                                break label74;
                            }
                        }

                        var1 = 1;
                    }
                }
            }

            if (var1 != 0) {
                this.removeCallbacks(this.mGoToToFadeOutRunnable);
            } else if (var1 != 1) {
                this.removeCallbacks(this.mGoToToFadeInRunnable);
            }

            if (this.mShowFadeOutGTP == 0 && var1 == 0 && this.mGoToTopLastState != 0) {
                this.post(this.mGoToToFadeOutRunnable);
            }

            if (var1 != 2) {
                this.mGoToTopView.setPressed(false);
            }

            this.mGoToTopState = var1;
            int var3 = this.getWidth();
            var2 = this.getPaddingLeft();
            int var4 = this.getPaddingRight();
            int var5 = this.getPaddingLeft() + (var3 - var2 - var4) / 2;
            Rect var6;
            if (var1 != 0) {
                if (var1 == 1 || var1 == 2) {
                    this.removeCallbacks(this.mGoToToFadeOutRunnable);
                    var2 = this.getHeight();
                    var6 = this.mGoToTopRect;
                    int var7 = this.mGoToTopSize;
                    int var8 = var7 / 2;
                    var4 = this.mGoToTopBottomPadding;
                    var3 = this.mGoToTopImmersiveBottomPadding;
                    var6.set(var5 - var8, var2 - var7 - var4 - var3, var5 + var7 / 2, var2 - var4 - var3);
                }
            } else if (this.mShowFadeOutGTP == 2) {
                this.mGoToTopRect.set(0, 0, 0, 0);
            }

            if (this.mShowFadeOutGTP == 2) {
                this.mShowFadeOutGTP = 0;
            }

            ImageView var9 = this.mGoToTopView;
            var6 = this.mGoToTopRect;
            var9.layout(var6.left, var6.top, var6.right, var6.bottom);
            if (var1 == 1 && (this.mGoToTopLastState == 0 || this.mGoToTopView.getAlpha() == 0.0F || this.mSizeChnage)) {
                this.post(this.mGoToToFadeInRunnable);
            }

            this.mSizeChnage = false;
            this.mGoToTopLastState = this.mGoToTopState;
        }
    }

    private boolean showPointerIcon(MotionEvent ev, int iconId) {
        final String methodName;

        if (VERSION.SDK_INT >= 29)
            methodName = "hidden_semSetPointerDevice";
        else if (VERSION.SDK_INT >= 28)
            methodName = "semSetPointerDevice";
        else
            methodName = "setPointerDevice";

        ReflectUtils.genericInvokeMethod(InputDevice.class, methodName, ev.getDevice(), iconId);
        return true;
    }

    private void stopScrollersInternal() {
        this.mViewFlinger.stop();
        if (this.mLayout != null) {
            this.mLayout.stopSmoothScroller();
        }

    }

    @SuppressLint("WrongConstant")
    private void updateLongPressMultiSelection(int var1, int var2, boolean var3) {
        int var4 = this.mChildHelper.getChildCount();
        if (this.mIsFirstMultiSelectionMove) {
            this.mPenDragStartX = var1;
            this.mPenDragStartY = var2;
            this.mPenTrackedChild = this.findChildViewUnder((float) var1, (float) var2);
            if (this.mPenTrackedChild == null) {
                this.mPenTrackedChild = this.seslFindNearChildViewUnder((float) var1, (float) var2);
                if (this.mPenTrackedChild == null) {
                    Log.e("SeslRecyclerView", "updateLongPressMultiSelection, mPenTrackedChild is NULL");
                    this.mIsFirstMultiSelectionMove = false;
                    return;
                }
            }

            if (this.mLongPressMultiSelectionListener != null) {
                this.mLongPressMultiSelectionListener.onLongPressMultiSelectionStarted(var1, var2);
            }

            this.mPenTrackedChildPosition = this.getChildLayoutPosition(this.mPenTrackedChild);
            this.mPenDragSelectedViewPosition = this.mPenTrackedChildPosition;
            this.mPenDistanceFromTrackedChildTop = this.mPenDragStartY - this.mPenTrackedChild.getTop();
            this.mIsFirstMultiSelectionMove = false;
        }

        int var5;
        int var6;
        if (this.mIsEnabledPaddingInHoverScroll) {
            var5 = this.mListPadding.top;
            var6 = this.getHeight() - this.mListPadding.bottom;
        } else {
            var5 = 0;
            var6 = this.getHeight();
        }

        this.mPenDragEndX = var1;
        this.mPenDragEndY = var2;
        if (this.mPenDragEndY < 0) {
            this.mPenDragEndY = 0;
        } else if (this.mPenDragEndY > var6) {
            this.mPenDragEndY = var6;
        }

        View var7 = this.findChildViewUnder((float) this.mPenDragEndX, (float) this.mPenDragEndY);
        View var8 = var7;
        if (var7 == null) {
            var7 = this.seslFindNearChildViewUnder((float) this.mPenDragEndX, (float) this.mPenDragEndY);
            var8 = var7;
            if (var7 == null) {
                Log.e("SeslRecyclerView", "updateLongPressMultiSelection, touchedView is NULL");
                return;
            }
        }

        var1 = this.getChildLayoutPosition(var8);
        if (var1 != -1) {
            this.mPenDragSelectedViewPosition = var1;
            int var9;
            if (this.mPenTrackedChildPosition < this.mPenDragSelectedViewPosition) {
                var9 = this.mPenTrackedChildPosition;
                var1 = this.mPenDragSelectedViewPosition;
            } else {
                var9 = this.mPenDragSelectedViewPosition;
                var1 = this.mPenTrackedChildPosition;
            }

            int var10;
            if (this.mPenDragStartX < this.mPenDragEndX) {
                var10 = this.mPenDragStartX;
            } else {
                var10 = this.mPenDragEndX;
            }

            this.mPenDragBlockLeft = var10;
            if (this.mPenDragStartY < this.mPenDragEndY) {
                var10 = this.mPenDragStartY;
            } else {
                var10 = this.mPenDragEndY;
            }

            this.mPenDragBlockTop = var10;
            if (this.mPenDragEndX > this.mPenDragStartX) {
                var10 = this.mPenDragEndX;
            } else {
                var10 = this.mPenDragStartX;
            }

            this.mPenDragBlockRight = var10;
            if (this.mPenDragEndY > this.mPenDragStartY) {
                var10 = this.mPenDragEndY;
            } else {
                var10 = this.mPenDragStartY;
            }

            this.mPenDragBlockBottom = var10;

            for (var10 = 0; var10 < var4; ++var10) {
                var8 = this.getChildAt(var10);
                if (var8 != null) {
                    this.mPenDragSelectedViewPosition = this.getChildLayoutPosition(var8);
                    if (var8.getVisibility() == 0) {
                        boolean var11 = false;
                        boolean var12 = var11;
                        if (var9 <= this.mPenDragSelectedViewPosition) {
                            var12 = var11;
                            if (this.mPenDragSelectedViewPosition <= var1) {
                                var12 = var11;
                                if (this.mPenDragSelectedViewPosition != this.mPenTrackedChildPosition) {
                                    var12 = true;
                                }
                            }
                        }

                        if (var12) {
                            if (this.mPenDragSelectedViewPosition != -1 && !this.mPenDragSelectedItemArray.contains(this.mPenDragSelectedViewPosition)) {
                                this.mPenDragSelectedItemArray.add(this.mPenDragSelectedViewPosition);
                                if (this.mLongPressMultiSelectionListener != null) {
                                    this.mLongPressMultiSelectionListener.onItemSelected(this, var8, this.mPenDragSelectedViewPosition, this.getChildItemId(var8));
                                }
                            }
                        } else if (this.mPenDragSelectedViewPosition != -1 && this.mPenDragSelectedItemArray.contains(this.mPenDragSelectedViewPosition)) {
                            this.mPenDragSelectedItemArray.remove((Object) this.mPenDragSelectedViewPosition);
                            if (this.mLongPressMultiSelectionListener != null) {
                                this.mLongPressMultiSelectionListener.onItemSelected(this, var8, this.mPenDragSelectedViewPosition, this.getChildItemId(var8));
                            }
                        }
                    }
                }
            }

            if (var3) {
                if (var2 <= this.mHoverTopAreaHeight + var5) {
                    if (!this.mHoverAreaEnter) {
                        this.mHoverAreaEnter = true;
                        this.mHoverScrollStartTime = System.currentTimeMillis();
                        if (this.mScrollListener != null) {
                            this.mScrollListener.onScrollStateChanged(this, 1);
                        }
                    }

                    if (!this.mHoverHandler.hasMessages(0)) {
                        this.mHoverRecognitionStartTime = System.currentTimeMillis();
                        this.mHoverScrollDirection = 2;
                        this.mHoverHandler.sendEmptyMessage(0);
                    }
                } else if (var2 >= var6 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange) {
                    if (!this.mHoverAreaEnter) {
                        this.mHoverAreaEnter = true;
                        this.mHoverScrollStartTime = System.currentTimeMillis();
                        if (this.mScrollListener != null) {
                            this.mScrollListener.onScrollStateChanged(this, 1);
                        }
                    }

                    if (!this.mHoverHandler.hasMessages(0)) {
                        this.mHoverRecognitionStartTime = System.currentTimeMillis();
                        this.mHoverScrollDirection = 1;
                        this.mHoverHandler.sendEmptyMessage(0);
                    }
                } else {
                    if (this.mHoverAreaEnter && this.mScrollListener != null) {
                        this.mScrollListener.onScrollStateChanged(this, 0);
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
            }

            this.invalidate();
        } else {
            Log.e("SeslRecyclerView", "touchedPosition is NO_POSITION");
        }

    }

    void absorbGlows(int var1, int var2) {
        if (var1 < 0) {
            this.ensureLeftGlow();
            this.mLeftGlow.onAbsorb(-var1);
        } else if (var1 > 0) {
            this.ensureRightGlow();
            this.mRightGlow.onAbsorb(var1);
        }

        if (var2 < 0) {
            this.ensureTopGlow();
            this.mTopGlow.onAbsorb(-var2);
        } else if (var2 > 0) {
            this.ensureBottomGlow();
            this.mBottomGlow.onAbsorb(var2);
        }

        if (var1 != 0 || var2 != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    public void addFocusables(ArrayList<View> var1, int var2, int var3) {
        if (this.mLayout == null || !this.mLayout.onAddFocusables(this, var1, var2, var3)) {
            super.addFocusables(var1, var2, var3);
        }

    }

    public void addItemDecoration(RecyclerView.ItemDecoration var1) {
        this.addItemDecoration(var1, -1);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration var1, int var2) {
        if (this.mLayout != null) {
            this.mLayout.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or layout");
        }

        if (this.mItemDecorations.isEmpty()) {
            this.setWillNotDraw(false);
        }

        if (var2 < 0) {
            this.mItemDecorations.add(var1);
        } else {
            this.mItemDecorations.add(var2, var1);
        }

        this.markItemDecorInsetsDirty();
        this.requestLayout();
    }

    public void addOnChildAttachStateChangeListener(RecyclerView.OnChildAttachStateChangeListener var1) {
        if (this.mOnChildAttachStateListeners == null) {
            this.mOnChildAttachStateListeners = new ArrayList();
        }

        this.mOnChildAttachStateListeners.add(var1);
    }

    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener var1) {
        this.mOnItemTouchListeners.add(var1);
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener var1) {
        if (this.mScrollListeners == null) {
            this.mScrollListeners = new ArrayList();
        }

        this.mScrollListeners.add(var1);
    }

    void animateAppearance(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3) {
        var1.setIsRecyclable(false);
        if (this.mItemAnimator.animateAppearance(var1, var2, var3)) {
            this.postAnimationRunner();
        }

    }

    void animateDisappearance(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3) {
        this.addAnimatingView(var1);
        var1.setIsRecyclable(false);
        if (this.mItemAnimator.animateDisappearance(var1, var2, var3)) {
            this.postAnimationRunner();
        }

    }

    void assertInLayoutOrScroll(String var1) {
        if (!this.isComputingLayout()) {
            if (var1 == null) {
                throw new IllegalStateException("Cannot call this method unless SeslRecyclerView is computing a layout or scrolling" + this.exceptionLabel());
            } else {
                throw new IllegalStateException(var1 + this.exceptionLabel());
            }
        }
    }

    void assertNotInLayoutOrScroll(String var1) {
        if (this.isComputingLayout()) {
            if (var1 == null) {
                throw new IllegalStateException("Cannot call this method while SeslRecyclerView is computing a layout or scrolling" + this.exceptionLabel());
            } else {
                throw new IllegalStateException(var1);
            }
        } else {
            if (this.mDispatchScrollCounter > 0) {
                Log.w("SeslRecyclerView", "Cannot call this method in a scroll callback. Scroll callbacks mightbe run during a measure & layout pass where you cannot change theRecyclerView data. Any method call that might change the structureof the SeslRecyclerView or the adapter contents should be postponed tothe next frame.", new IllegalStateException("" + this.exceptionLabel()));
            }

        }
    }

    boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder var1) {
        boolean var2;
        if (this.mItemAnimator != null && !this.mItemAnimator.canReuseUpdatedViewHolder(var1, var1.getUnmodifiedPayloads())) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        boolean var2;
        if (var1 instanceof RecyclerView.LayoutParams && this.mLayout.checkLayoutParams((RecyclerView.LayoutParams) var1)) {
            var2 = true;
        } else {
            var2 = false;
        }

        return var2;
    }

    void clearOldPositions() {
        int var1 = this.mChildHelper.getUnfilteredChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            RecyclerView.ViewHolder var3 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var2));
            if (!var3.shouldIgnore()) {
                var3.clearOldPosition();
            }
        }

        this.mRecycler.clearOldPositions();
    }

    public void clearOnChildAttachStateChangeListeners() {
        if (this.mOnChildAttachStateListeners != null) {
            this.mOnChildAttachStateListeners.clear();
        }

    }

    public void clearOnScrollListeners() {
        if (this.mScrollListeners != null) {
            this.mScrollListeners.clear();
        }

    }

    public int computeHorizontalScrollExtent() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollHorizontally()) {
            var1 = this.mLayout.computeHorizontalScrollExtent(this.mState);
        }

        return var1;
    }

    public int computeHorizontalScrollOffset() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollHorizontally()) {
            var1 = this.mLayout.computeHorizontalScrollOffset(this.mState);
        }

        return var1;
    }

    public int computeHorizontalScrollRange() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollHorizontally()) {
            var1 = this.mLayout.computeHorizontalScrollRange(this.mState);
        }

        return var1;
    }

    public int computeVerticalScrollExtent() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollVertically()) {
            var1 = this.mLayout.computeVerticalScrollExtent(this.mState);
        }

        return var1;
    }

    public int computeVerticalScrollOffset() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollVertically()) {
            var1 = this.mLayout.computeVerticalScrollOffset(this.mState);
        }

        return var1;
    }

    public int computeVerticalScrollRange() {
        int var1 = 0;
        if (this.mLayout != null && this.mLayout.canScrollVertically()) {
            var1 = this.mLayout.computeVerticalScrollRange(this.mState);
        }

        return var1;
    }

    void considerReleasingGlowsOnScroll(int var1, int var2) {
        boolean var3 = false;
        boolean var4 = var3;
        if (this.mLeftGlow != null) {
            var4 = var3;
            if (!this.mLeftGlow.isFinished()) {
                var4 = var3;
                if (var1 > 0) {
                    this.mLeftGlow.onRelease();
                    var4 = this.mLeftGlow.isFinished();
                }
            }
        }

        var3 = var4;
        if (this.mRightGlow != null) {
            var3 = var4;
            if (!this.mRightGlow.isFinished()) {
                var3 = var4;
                if (var1 < 0) {
                    this.mRightGlow.onRelease();
                    var3 = var4 | this.mRightGlow.isFinished();
                }
            }
        }

        var4 = var3;
        if (this.mTopGlow != null) {
            var4 = var3;
            if (!this.mTopGlow.isFinished()) {
                var4 = var3;
                if (var2 > 0) {
                    this.mTopGlow.onRelease();
                    var4 = var3 | this.mTopGlow.isFinished();
                }
            }
        }

        var3 = var4;
        if (this.mBottomGlow != null) {
            var3 = var4;
            if (!this.mBottomGlow.isFinished()) {
                var3 = var4;
                if (var2 < 0) {
                    this.mBottomGlow.onRelease();
                    var3 = var4 | this.mBottomGlow.isFinished();
                }
            }
        }

        if (var3) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    void consumePendingUpdateOperations() {
        if (this.mFirstLayoutComplete && !this.mDataSetHasChangedAfterLayout) {
            if (this.mAdapterHelper.hasPendingUpdates()) {
                if (this.mAdapterHelper.hasAnyUpdateTypes(4) && !this.mAdapterHelper.hasAnyUpdateTypes(11)) {
                    TraceCompat.beginSection("RV PartialInvalidate");
                    this.startInterceptRequestLayout();
                    this.onEnterLayoutOrScroll();
                    this.mAdapterHelper.preProcess();
                    if (!this.mLayoutWasDefered) {
                        if (this.hasUpdatedView()) {
                            this.dispatchLayout();
                        } else {
                            this.mAdapterHelper.consumePostponedUpdates();
                        }
                    }

                    this.stopInterceptRequestLayout(true);
                    this.onExitLayoutOrScroll();
                    TraceCompat.endSection();
                } else if (this.mAdapterHelper.hasPendingUpdates()) {
                    TraceCompat.beginSection("RV FullInvalidate");
                    this.dispatchLayout();
                    TraceCompat.endSection();
                }
            }
        } else {
            TraceCompat.beginSection("RV FullInvalidate");
            this.dispatchLayout();
            TraceCompat.endSection();
        }

    }

    void defaultOnMeasure(int var1, int var2) {
        this.setMeasuredDimension(RecyclerView.LayoutManager.chooseSize(var1, this.getPaddingLeft() + this.getPaddingRight(), ViewCompat.getMinimumWidth(this)), RecyclerView.LayoutManager.chooseSize(var2, this.getPaddingTop() + this.getPaddingBottom(), ViewCompat.getMinimumHeight(this)));
    }

    void dispatchChildAttached(View var1) {
        RecyclerView.ViewHolder var2 = getChildViewHolderInt(var1);
        this.onChildAttachedToWindow(var1);
        if (this.mAdapter != null && var2 != null) {
            this.mAdapter.onViewAttachedToWindow(var2);
        }

        if (this.mOnChildAttachStateListeners != null) {
            for (int var3 = this.mOnChildAttachStateListeners.size() - 1; var3 >= 0; --var3) {
                ((RecyclerView.OnChildAttachStateChangeListener) this.mOnChildAttachStateListeners.get(var3)).onChildViewAttachedToWindow(var1);
            }
        }

    }

    void dispatchChildDetached(View var1) {
        RecyclerView.ViewHolder var2 = getChildViewHolderInt(var1);
        this.onChildDetachedFromWindow(var1);
        if (this.mAdapter != null && var2 != null) {
            this.mAdapter.onViewDetachedFromWindow(var2);
        }

        if (this.mOnChildAttachStateListeners != null) {
            for (int var3 = this.mOnChildAttachStateListeners.size() - 1; var3 >= 0; --var3) {
                ((RecyclerView.OnChildAttachStateChangeListener) this.mOnChildAttachStateListeners.get(var3)).onChildViewDetachedFromWindow(var1);
            }
        }

    }

    protected void dispatchDraw(Canvas var1) {
        super.dispatchDraw(var1);
        int var2 = this.mItemDecorations.size();

        int var3;
        for (var3 = 0; var3 < var2; ++var3) {
            ((RecyclerView.ItemDecoration) this.mItemDecorations.get(var3)).seslOnDispatchDraw(var1, this, this.mState);
        }

        if (this.mDrawRect && (this.mBlackTop != -1 || this.mLastBlackTop != -1) && !this.canScrollVertically(-1) && !this.canScrollVertically(1)) {
            this.mAnimatedBlackTop = this.mBlackTop;
            if (this.isAnimating()) {
                View var6;
                if (this.mDrawReverse) {
                    if (this.mBlackTop != -1) {
                        var6 = this.mChildHelper.getChildAt(0);
                    } else {
                        var6 = this.getChildAt(0);
                    }
                } else if (this.mBlackTop != -1) {
                    var6 = this.mChildHelper.getChildAt(this.mChildHelper.getChildCount() - 1);
                } else {
                    var6 = this.getChildAt(this.getChildCount() - 1);
                }

                if (var6 != null) {
                    this.mAnimatedBlackTop = Math.round(var6.getY()) + var6.getHeight();
                }
            }

            if (this.mBlackTop != -1 || this.mAnimatedBlackTop != this.mBlackTop) {
                var1.drawRect(0.0F, (float) this.mAnimatedBlackTop, (float) this.getRight(), (float) this.getBottom(), this.mRectPaint);
                if (this.mDrawLastRoundedCorner) {
                    this.mSeslRoundedCorner.drawRoundedCorner(0, this.mAnimatedBlackTop, getWidth(), getBottom(), var1);
                }
            }
        }
    }

    protected boolean dispatchHoverEvent(MotionEvent var1) {
        boolean var2;
        if (this.mAdapter == null) {
            Log.d("SeslRecyclerView", "No adapter attached; skipping hover scroll");
            var2 = super.dispatchHoverEvent(var1);
        } else {
            int var3 = var1.getAction();
            int var4 = var1.getToolType(0);
            this.mIsMouseWheel = false;
            if ((var3 == 7 || var3 == 9) && var4 == 2) {
                this.mIsPenHovered = true;
            } else if (var3 == 10) {
                this.mIsPenHovered = false;
            }

            this.mNewTextViewHoverState = (boolean) ReflectUtils.genericInvokeMethod(TextView.class, VERSION.SDK_INT >= 29 ? "hidden_semIsTextViewHovered" : "semIsTextViewHovered");
            if (!this.mNewTextViewHoverState && this.mOldTextViewHoverState && this.mIsPenDragBlockEnabled && (var1.getButtonState() == 32 || var1.getButtonState() == 2)) {
                this.mIsNeedPenSelectIconSet = true;
            } else {
                this.mIsNeedPenSelectIconSet = false;
            }

            this.mOldTextViewHoverState = this.mNewTextViewHoverState;
            boolean var6;
            boolean var7;
            int var8;
            if (var3 != 9 && !this.mHoverScrollStateChanged) {
                if (var3 != 7) {
                    if (var3 == 10 && this.mIsPenSelectPointerSetted) {
                        this.showPointerIcon(var1, 0x4e21);
                        this.mIsPenSelectPointerSetted = false;
                    }
                } else if ((!this.mIsPenDragBlockEnabled || this.mIsPenSelectPointerSetted || var1.getToolType(0) != 2 || var1.getButtonState() != 32 && var1.getButtonState() != 2) && !this.mIsNeedPenSelectIconSet) {
                    if (this.mIsPenDragBlockEnabled && this.mIsPenSelectPointerSetted && var1.getButtonState() != 32 && var1.getButtonState() != 2) {
                        this.showPointerIcon(var1, 0x4e21);
                        this.mIsPenSelectPointerSetted = false;
                    }
                } else {
                    this.showPointerIcon(var1, 0x4e35);
                    this.mIsPenSelectPointerSetted = true;
                }
            } else {
                this.mNeedsHoverScroll = true;
                this.mHoverScrollStateChanged = false;
                if (this.mHasNestedScrollRange) {
                    this.adjustNestedScrollRange();
                }

                if (!(boolean) ReflectUtils.genericInvokeMethod(View.class, this, "isHoveringUIEnabled") || !this.mHoverScrollEnable) {
                    this.mNeedsHoverScroll = false;
                }

                if (this.mNeedsHoverScroll && var4 == 2) {
                    String var5 = "pen_hovering";
                    if (android.provider.Settings.System.getInt(this.mContext.getContentResolver(), var5, 0) == 1) {
                        var6 = true;
                    } else {
                        var6 = false;
                    }

                    var7 = false;

                    label347:
                    {
                        try {
                            var8 = android.provider.Settings.System.getInt(this.mContext.getContentResolver(), "car_mode_on");
                        } catch (SettingNotFoundException var13) {
                            Log.i("SeslRecyclerView", "dispatchHoverEvent car_mode_on SettingNotFoundException");
                            break label347;
                        }

                        if (var8 == 1) {
                            var7 = true;
                        } else {
                            var7 = false;
                        }
                    }

                    if (!var6 || var7) {
                        this.mNeedsHoverScroll = false;
                    }

                    if (var6 && this.mIsPenDragBlockEnabled && !this.mIsPenSelectPointerSetted && (var1.getButtonState() == 32 || var1.getButtonState() == 2)) {
                        this.showPointerIcon(var1, 0x4e35);
                        this.mIsPenSelectPointerSetted = true;
                    }
                }

                if (this.mNeedsHoverScroll && var4 == 3) {
                    this.mNeedsHoverScroll = false;
                }
            }

            if (!this.mNeedsHoverScroll) {
                var2 = super.dispatchHoverEvent(var1);
            } else {
                int var9 = (int) var1.getX();
                int var10 = (int) var1.getY();
                int var11 = this.getChildCount();
                int var12;
                if (this.mIsEnabledPaddingInHoverScroll) {
                    var12 = this.mListPadding.top;
                    var8 = this.getHeight() - this.mListPadding.bottom;
                } else {
                    var12 = this.mExtraPaddingInTopHoverArea;
                    var8 = this.getHeight() - this.mExtraPaddingInBottomHoverArea;
                }

                if (this.findFirstChildPosition() + var11 < this.mAdapter.getItemCount()) {
                    var6 = true;
                } else {
                    var6 = false;
                }

                var7 = var6;
                if (!var6) {
                    var7 = var6;
                    if (var11 > 0) {
                        View var15 = this.getChildAt(var11 - 1);
                        if (var15.getBottom() <= this.getBottom() - this.mListPadding.bottom && var15.getBottom() <= this.getHeight() - this.mListPadding.bottom) {
                            var7 = false;
                        } else {
                            var7 = true;
                        }
                    }
                }

                boolean var14;
                if (this.findFirstChildPosition() > 0) {
                    var14 = true;
                } else {
                    var14 = false;
                }

                var6 = var14;
                if (!var14) {
                    var6 = var14;
                    if (var11 > 0) {
                        if (this.getChildAt(0).getTop() < this.mListPadding.top) {
                            var6 = true;
                        } else {
                            var6 = false;
                        }
                    }
                }

                if (var1.getToolType(0) == 2) {
                    var14 = true;
                } else {
                    var14 = false;
                }

                if ((var10 <= this.mHoverTopAreaHeight + var12 || var10 >= var8 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange) && var9 > 0 && var9 <= this.getRight() && (var6 || var7) && (var10 < var12 || var10 > this.mHoverTopAreaHeight + var12 || var6 || !this.mIsHoverOverscrolled) && (var10 < var8 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange || var10 > var8 - this.mRemainNestedScrollRange || var7 || !this.mIsHoverOverscrolled) && (!var14 || var1.getButtonState() != 32 && var1.getButtonState() != 2) && var14 && !this.isLockScreenMode()) {
                    if (this.mHasNestedScrollRange && this.mRemainNestedScrollRange > 0 && this.mRemainNestedScrollRange != this.mNestedScrollRange) {
                        this.adjustNestedScrollRange();
                    }

                    if (!this.mHoverAreaEnter) {
                        this.mHoverScrollStartTime = System.currentTimeMillis();
                    }

                    switch (var3) {
                        case 7:
                            if (!this.mHoverAreaEnter) {
                                this.mHoverAreaEnter = true;
                                var1.setAction(10);
                                var2 = super.dispatchHoverEvent(var1);
                                return var2;
                            }

                            if (var10 >= var12 && var10 <= this.mHoverTopAreaHeight + var12) {
                                if (!this.mHoverHandler.hasMessages(0)) {
                                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                    if (!this.mIsHoverOverscrolled || this.mHoverScrollDirection == 1) {
                                        this.showPointerIcon(var1, 0x4e2b);
                                    }

                                    this.mHoverScrollDirection = 2;
                                    this.mHoverHandler.sendEmptyMessage(0);
                                }
                            } else if (var10 >= var8 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange && var10 <= var8 - this.mRemainNestedScrollRange) {
                                if (!this.mHoverHandler.hasMessages(0)) {
                                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                    if (!this.mIsHoverOverscrolled || this.mHoverScrollDirection == 2) {
                                        this.showPointerIcon(var1, 0x4e2f);
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

                                this.showPointerIcon(var1, 0x4e21);
                                this.mHoverRecognitionStartTime = 0L;
                                this.mHoverScrollStartTime = 0L;
                                this.mIsHoverOverscrolled = false;
                                this.mHoverAreaEnter = false;
                                this.mIsSendHoverScrollState = false;
                            }
                        case 8:
                        default:
                            break;
                        case 9:
                            this.mHoverAreaEnter = true;
                            if (var10 >= var12 && var10 <= this.mHoverTopAreaHeight + var12) {
                                if (!this.mHoverHandler.hasMessages(0)) {
                                    this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                    this.showPointerIcon(var1, 0x4e2b);
                                    this.mHoverScrollDirection = 2;
                                    this.mHoverHandler.sendEmptyMessage(0);
                                }
                            } else if (var10 >= var8 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange && var10 <= var8 - this.mRemainNestedScrollRange && !this.mHoverHandler.hasMessages(0)) {
                                this.mHoverRecognitionStartTime = System.currentTimeMillis();
                                this.showPointerIcon(var1, 0x4e2f);
                                this.mHoverScrollDirection = 1;
                                this.mHoverHandler.sendEmptyMessage(0);
                            }
                            break;
                        case 10:
                            if (this.mHoverHandler.hasMessages(0)) {
                                this.mHoverHandler.removeMessages(0);
                            }

                            if (this.mScrollState == 1) {
                                this.setScrollState(0);
                            }

                            this.showPointerIcon(var1, 0x4e21);
                            this.mHoverRecognitionStartTime = 0L;
                            this.mHoverScrollStartTime = 0L;
                            this.mIsHoverOverscrolled = false;
                            this.mHoverAreaEnter = false;
                            this.mIsSendHoverScrollState = false;
                            if (this.mHoverScrollStateForListener != 0) {
                                this.mHoverScrollStateForListener = 0;
                                if (this.mScrollListener != null) {
                                    this.mScrollListener.onScrollStateChanged(this, 0);
                                }
                            }

                            var2 = super.dispatchHoverEvent(var1);
                            return var2;
                    }

                    var2 = true;
                } else {
                    if (this.mHasNestedScrollRange && this.mRemainNestedScrollRange > 0 && this.mRemainNestedScrollRange != this.mNestedScrollRange) {
                        this.adjustNestedScrollRange();
                    }

                    if (this.mHoverHandler.hasMessages(0)) {
                        this.mHoverHandler.removeMessages(0);
                        this.showPointerIcon(var1, 0x4e21);
                        if (this.mScrollState == 1) {
                            this.setScrollState(0);
                        }
                    }

                    if (var10 > this.mHoverTopAreaHeight + var12 && var10 < var8 - this.mHoverBottomAreaHeight - this.mRemainNestedScrollRange || var9 <= 0 || var9 > this.getRight()) {
                        this.mIsHoverOverscrolled = false;
                    }

                    if (this.mHoverAreaEnter || this.mHoverScrollStartTime != 0L) {
                        this.showPointerIcon(var1, 0x4e21);
                    }

                    this.mHoverRecognitionStartTime = 0L;
                    this.mHoverScrollStartTime = 0L;
                    this.mHoverAreaEnter = false;
                    this.mIsSendHoverScrollState = false;
                    if (var3 == 10 && this.mHoverScrollStateForListener != 0) {
                        this.mHoverScrollStateForListener = 0;
                        if (this.mScrollListener != null) {
                            this.mScrollListener.onScrollStateChanged(this, 0);
                        }
                    }

                    var2 = super.dispatchHoverEvent(var1);
                }
            }
        }

        return var2;
    }

    public boolean dispatchKeyEvent(KeyEvent var1) {
        switch (var1.getKeyCode()) {
            case 19:
            case 20:
                if (var1.getAction() == 0) {
                    this.mIsArrowKeyPressed = true;
                }
            default:
                return super.dispatchKeyEvent(var1);
        }
    }

    void dispatchLayout() {
        if (this.mAdapter == null) {
            Log.e("SeslRecyclerView", "No adapter attached; skipping layout");
        } else if (this.mLayout == null) {
            Log.e("SeslRecyclerView", "No layout manager attached; skipping layout");
        } else {
            this.mState.mIsMeasuring = false;
            if (this.mState.mLayoutStep == 1) {
                this.dispatchLayoutStep1();
                this.mLayout.setExactMeasureSpecsFrom(this);
                this.dispatchLayoutStep2();
            } else if (!this.mAdapterHelper.hasUpdates() && this.mLayout.getWidth() == this.getWidth() && this.mLayout.getHeight() == this.getHeight()) {
                this.mLayout.setExactMeasureSpecsFrom(this);
            } else {
                this.mLayout.setExactMeasureSpecsFrom(this);
                this.dispatchLayoutStep2();
            }

            this.dispatchLayoutStep3();
        }

    }

    public boolean dispatchNestedFling(float var1, float var2, boolean var3) {
        return this.getScrollingChildHelper().dispatchNestedFling(var1, var2, var3);
    }

    public boolean dispatchNestedPreFling(float var1, float var2) {
        return this.getScrollingChildHelper().dispatchNestedPreFling(var1, var2);
    }

    public boolean dispatchNestedPreScroll(int var1, int var2, int[] var3, int[] var4) {
        return this.getScrollingChildHelper().dispatchNestedPreScroll(var1, var2, var3, var4);
    }

    public boolean dispatchNestedPreScroll(int var1, int var2, int[] var3, int[] var4, int var5) {
        return this.getScrollingChildHelper().dispatchNestedPreScroll(var1, var2, var3, var4, var5);
    }

    public boolean dispatchNestedScroll(int var1, int var2, int var3, int var4, int[] var5) {
        return this.getScrollingChildHelper().dispatchNestedScroll(var1, var2, var3, var4, var5);
    }

    public boolean dispatchNestedScroll(int var1, int var2, int var3, int var4, int[] var5, int var6) {
        return this.getScrollingChildHelper().dispatchNestedScroll(var1, var2, var3, var4, var5, var6);
    }

    void dispatchOnScrollStateChanged(int var1) {
        if (this.mLayout != null) {
            this.mLayout.onScrollStateChanged(var1);
        }

        this.onScrollStateChanged(var1);
        if (this.mScrollListener != null) {
            this.mScrollListener.onScrollStateChanged(this, var1);
        }

        if (this.mScrollListeners != null) {
            for (int var2 = this.mScrollListeners.size() - 1; var2 >= 0; --var2) {
                ((RecyclerView.OnScrollListener) this.mScrollListeners.get(var2)).onScrollStateChanged(this, var1);
            }
        }

    }

    void dispatchOnScrolled(int var1, int var2) {
        ++this.mDispatchScrollCounter;
        int var3 = this.getScrollX();
        int var4 = this.getScrollY();
        this.onScrollChanged(var3, var4, var3, var4);
        this.onScrolled(var1, var2);
        if (this.mFastScroller != null && this.mAdapter != null) {
            this.mFastScroller.onScroll(this.findFirstVisibleItemPosition(), this.getChildCount(), this.mAdapter.getItemCount());
        }

        if (this.mScrollListener != null) {
            this.mScrollListener.onScrolled(this, var1, var2);
        }

        if (this.mScrollListeners != null) {
            for (var4 = this.mScrollListeners.size() - 1; var4 >= 0; --var4) {
                ((RecyclerView.OnScrollListener) this.mScrollListeners.get(var4)).onScrolled(this, var1, var2);
            }
        }

        --this.mDispatchScrollCounter;
    }

    @SuppressLint("WrongConstant")
    void dispatchPendingImportantForAccessibilityChanges() {
        for (int var1 = this.mPendingAccessibilityImportanceChange.size() - 1; var1 >= 0; --var1) {
            RecyclerView.ViewHolder var2 = (RecyclerView.ViewHolder) this.mPendingAccessibilityImportanceChange.get(var1);
            if (var2.itemView.getParent() == this && !var2.shouldIgnore()) {
                int var3 = var2.mPendingAccessibilityState;
                if (var3 != -1) {
                    ViewCompat.setImportantForAccessibility(var2.itemView, var3);
                    var2.mPendingAccessibilityState = -1;
                }
            }
        }

        this.mPendingAccessibilityImportanceChange.clear();
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> var1) {
        this.dispatchThawSelfOnly(var1);
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> var1) {
        this.dispatchFreezeSelfOnly(var1);
    }

    public boolean dispatchTouchEvent(MotionEvent var1) {
        boolean var2;
        if (this.mItemAnimator != null && this.mItemAnimator.isRunning() && this.mItemAnimator.getItemAnimationTypeInternal() == 2) {
            Log.d("SeslRecyclerView", "dispatchTouchEvent : itemAnimator is running, return..");
            var2 = true;
        } else if (this.mLayout == null) {
            Log.d("SeslRecyclerView", "No layout manager attached; skipping gototop & multiselection");
            var2 = super.dispatchTouchEvent(var1);
        } else {
            int var3 = var1.getActionMasked();
            int var4 = (int) (var1.getX() + 0.5F);
            int var5 = (int) (var1.getY() + 0.5F);
            if (this.mPenDragSelectedItemArray == null) {
                this.mPenDragSelectedItemArray = new ArrayList();
            }

            int var6;
            int var7;
            if (this.mIsEnabledPaddingInHoverScroll) {
                var6 = this.mListPadding.top;
                var7 = this.getHeight() - this.mListPadding.bottom;
            } else {
                var6 = 0;
                var7 = this.getHeight();
            }

            switch (var3) {
                case 0:
                    if (this.isSupportGotoTop()) {
                        this.mGoToTopMoved = false;
                        this.mGoToToping = false;
                    }

                    if (this.isSupportGotoTop() && this.mGoToTopState != 2 && this.mGoToTopRect.contains(var4, var5)) {
                        this.setupGoToTop(2);
                        this.mGoToTopView.setPressed(true);
                        return true;
                    }

                    if (this.mIsCtrlKeyPressed && var1.getToolType(0) == 3) {
                        this.mIsCtrlMultiSelection = true;
                        this.mIsNeedPenSelection = true;
                        this.multiSelection(var4, var5, var6, var7, false);
                        var2 = true;
                        return var2;
                    }

                    if (this.mIsLongPressMultiSelection) {
                        this.mIsLongPressMultiSelection = false;
                    }
                    break;
                case 2:
                    if (this.mIsCtrlMultiSelection) {
                        this.multiSelection(var4, var5, var6, var7, false);
                        var2 = true;
                        return var2;
                    }

                    if (this.mIsLongPressMultiSelection) {
                        this.updateLongPressMultiSelection(var4, var5, true);
                        var2 = true;
                        return var2;
                    }

                    if (this.isSupportGotoTop() && this.mGoToTopState == 2) {
                        if (!this.mGoToTopRect.contains(var4, var5)) {
                            this.mGoToTopState = 1;
                            this.mGoToTopView.setPressed(false);
                            this.autoHide(1);
                            this.mGoToTopMoved = true;
                        }

                        var2 = true;
                        return var2;
                    }
                    break;
                case 3:
                    if (this.isSupportGotoTop() && this.mGoToTopState != 0) {
                        if (this.mGoToTopState == 2) {
                            this.mGoToTopState = 1;
                        }

                        this.mGoToTopView.setPressed(false);
                    }
                case 1:
                    if (this.mIsCtrlMultiSelection) {
                        this.multiSelectionEnd(var4, var5);
                        this.mIsCtrlMultiSelection = false;
                        var2 = true;
                        return var2;
                    }

                    if (this.mIsLongPressMultiSelection) {
                        if (this.mLongPressMultiSelectionListener != null) {
                            this.mLongPressMultiSelectionListener.onLongPressMultiSelectionEnded(var4, var5);
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
                case 212:
                    if (this.isSupportGotoTop() && this.mGoToTopState == 2) {
                        if (this.canScrollUp()) {
                            RecyclerView.SeslOnGoToTopClickListener var10 = this.mSeslOnGoToTopClickListener;
                            if (var10 != null && var10.onGoToTopClick(this)) {
                                return true;
                            }

                            Log.d("SeslRecyclerView", " can scroll top ");
                            var5 = this.getChildCount();
                            if (this.computeVerticalScrollOffset() != 0) {
                                this.stopScroll();
                                RecyclerView.LayoutManager var11 = this.mLayout;
                                if (var11 instanceof StaggeredGridLayoutManager) {
                                    ((StaggeredGridLayoutManager) var11).scrollToPositionWithOffset(0, 0);
                                } else {
                                    this.mGoToToping = true;
                                    if (var5 > 0 && var5 < this.findFirstVisibleItemPosition()) {
                                        var11 = this.mLayout;
                                        if (var11 instanceof SeslLinearLayoutManager) {
                                            ((SeslLinearLayoutManager) var11).scrollToPositionWithOffset(var5, 0);
                                        } else {
                                            this.scrollToPosition(var5);
                                        }
                                    }

                                    this.post(new Runnable() {
                                        public void run() {
                                            RecyclerView.this.smoothScrollToPosition(0);
                                        }
                                    });
                                }

                                this.seslShowGoToTopEdge(500.0F / (float) this.getHeight(), (float) var3 / (float) this.getWidth(), 150);
                            }
                        }

                        this.seslHideGoToTop();
                        this.playSoundEffect(0);
                        return true;
                    }

                    if (this.mGoToTopMoved) {
                        this.mGoToTopMoved = false;
                        if (this.mVelocityTracker != null) {
                            this.mVelocityTracker.clear();
                        }
                    }

                    this.multiSelectionEnd(var4, var5);
                    break;
                case 211:
                    if (!(boolean) ReflectUtils.genericInvokeMethod(TextView.class, VERSION.SDK_INT >= 29 ? "hidden_semIsTextSelectionProgressing" : "semIsTextSelectionProgressing") && this.mIsPenSelectionEnabled) {
                        this.mIsNeedPenSelection = true;
                    } else {
                        this.mIsNeedPenSelection = false;
                    }

                    if (this.mPenDragSelectedItemArray == null) {
                        this.mPenDragSelectedItemArray = new ArrayList();
                    }
                    break;
                case 213:
                    this.multiSelection(var4, var5, var6, var7, false);
            }

            var2 = super.dispatchTouchEvent(var1);
        }

        return var2;
    }

    public void draw(Canvas var1) {
        boolean var2 = true;
        super.draw(var1);
        int var3 = this.mItemDecorations.size();

        int var4;
        for (var4 = 0; var4 < var3; ++var4) {
            ((RecyclerView.ItemDecoration) this.mItemDecorations.get(var4)).onDrawOver(var1, this, this.mState);
        }

        boolean var8 = false;
        boolean var7 = var8;
        int var5;
        if (this.mLeftGlow != null) {
            var7 = var8;
            if (!this.mLeftGlow.isFinished()) {
                var5 = var1.save();
                if (this.mClipToPadding) {
                    var4 = this.getPaddingBottom();
                } else {
                    var4 = 0;
                }

                var1.rotate(270.0F);
                var1.translate((float) (-this.getHeight() + var4), 0.0F);
                if (this.mLeftGlow != null && this.mLeftGlow.draw(var1)) {
                    var7 = true;
                } else {
                    var7 = false;
                }

                var1.restoreToCount(var5);
            }
        }

        var8 = var7;
        if (this.mTopGlow != null) {
            var8 = var7;
            if (!this.mTopGlow.isFinished()) {
                var5 = var1.save();
                if (this.mClipToPadding) {
                    var1.translate((float) this.getPaddingLeft(), (float) this.getPaddingTop());
                }

                if (this.mTopGlow != null && this.mTopGlow.draw(var1)) {
                    var8 = true;
                } else {
                    var8 = false;
                }

                var8 |= var7;
                var1.restoreToCount(var5);
            }
        }

        var7 = var8;
        if (this.mRightGlow != null) {
            var7 = var8;
            if (!this.mRightGlow.isFinished()) {
                var5 = var1.save();
                int var6 = this.getWidth();
                if (this.mClipToPadding) {
                    var3 = this.getPaddingTop();
                } else {
                    var3 = 0;
                }

                var1.rotate(90.0F);
                var1.translate((float) (-var3), (float) (-var6));
                if (this.mRightGlow != null && this.mRightGlow.draw(var1)) {
                    var7 = true;
                } else {
                    var7 = false;
                }

                var7 |= var8;
                var1.restoreToCount(var5);
            }
        }

        var8 = var7;
        if (this.mBottomGlow != null) {
            var8 = var7;
            if (!this.mBottomGlow.isFinished()) {
                var5 = var1.save();
                var1.rotate(180.0F);
                if (this.mClipToPadding) {
                    var1.translate((float) (-this.getWidth() + this.getPaddingRight()), (float) (-this.getHeight() + this.getPaddingBottom()));
                } else {
                    var1.translate((float) (-this.getWidth()), (float) (-this.getHeight()));
                }

                if (this.mBottomGlow != null && this.mBottomGlow.draw(var1)) {
                    var8 = var2;
                } else {
                    var8 = false;
                }

                var8 |= var7;
                var1.restoreToCount(var5);
            }
        }

        var7 = var8;
        if (!var8) {
            var7 = var8;
            if (this.mItemAnimator != null) {
                var7 = var8;
                if (this.mItemDecorations.size() > 0) {
                    var7 = var8;
                    if (this.mItemAnimator.isRunning()) {
                        var7 = true;
                    }
                }
            }
        }

        if (var7) {
            ViewCompat.postInvalidateOnAnimation(this);
        }

        if (this.mEnableGoToTop) {
            this.drawGoToTop();
        }

        if (this.mIsPenDragBlockEnabled && !this.mIsLongPressMultiSelection && this.mLayout != null && (this.mPenDragBlockLeft != 0 || this.mPenDragBlockTop != 0)) {
            var4 = this.findFirstVisibleItemPosition();
            var3 = this.mLayout.getChildCount();
            if (this.mPenTrackedChildPosition >= var4 && this.mPenTrackedChildPosition <= var3 + var4 - 1) {
                this.mPenTrackedChild = this.mLayout.getChildAt(this.mPenTrackedChildPosition - var4);
                var4 = 0;
                if (this.mPenTrackedChild != null) {
                    var4 = this.mPenTrackedChild.getTop();
                }

                this.mPenDragStartY = this.mPenDistanceFromTrackedChildTop + var4;
            }

            if (this.mPenDragStartY < this.mPenDragEndY) {
                var4 = this.mPenDragStartY;
            } else {
                var4 = this.mPenDragEndY;
            }

            this.mPenDragBlockTop = var4;
            if (this.mPenDragEndY > this.mPenDragStartY) {
                var4 = this.mPenDragEndY;
            } else {
                var4 = this.mPenDragStartY;
            }

            this.mPenDragBlockBottom = var4;
            this.mPenDragBlockRect.set(this.mPenDragBlockLeft, this.mPenDragBlockTop, this.mPenDragBlockRight, this.mPenDragBlockBottom);
            this.mPenDragBlockImage.setBounds(this.mPenDragBlockRect);
            this.mPenDragBlockImage.draw(var1);
        }

    }

    public boolean drawChild(Canvas var1, View var2, long var3) {
        return super.drawChild(var1, var2, var3);
    }

    void ensureBottomGlow() {
        if (this.mBottomGlow == null) {
            this.mBottomGlow = new SamsungEdgeEffect(this.getContext());
            this.mBottomGlow.setHostView(this, true);
            if (this.mClipToPadding) {
                this.mBottomGlow.setSize(this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight(), this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom());
            } else {
                this.mBottomGlow.setSize(this.getMeasuredWidth(), this.getMeasuredHeight());
            }
        }

    }

    void ensureLeftGlow() {
        if (this.mLeftGlow == null) {
            this.mLeftGlow = new SamsungEdgeEffect(this.getContext());
            this.mLeftGlow.setHostView(this, false);
            if (this.mClipToPadding) {
                this.mLeftGlow.setSize(this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom(), this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight());
            } else {
                this.mLeftGlow.setSize(this.getMeasuredHeight(), this.getMeasuredWidth());
            }
        }

    }

    void ensureRightGlow() {
        if (this.mRightGlow == null) {
            this.mRightGlow = new SamsungEdgeEffect(this.getContext());
            this.mRightGlow.setHostView(this, false);
            if (this.mClipToPadding) {
                this.mRightGlow.setSize(this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom(), this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight());
            } else {
                this.mRightGlow.setSize(this.getMeasuredHeight(), this.getMeasuredWidth());
            }
        }

    }

    void ensureTopGlow() {
        if (this.mTopGlow == null) {
            this.mTopGlow = new SamsungEdgeEffect(this.getContext());
            this.mTopGlow.setHostView(this, true);
            if (this.mClipToPadding) {
                this.mTopGlow.setSize(this.getMeasuredWidth() - this.getPaddingLeft() - this.getPaddingRight(), this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom());
            } else {
                this.mTopGlow.setSize(this.getMeasuredWidth(), this.getMeasuredHeight());
            }
        }

    }

    String exceptionLabel() {
        return " " + super.toString() + ", adapter:" + this.mAdapter + ", layout:" + this.mLayout + ", context:" + this.getContext();
    }

    final void fillRemainingScrollValues(RecyclerView.State var1) {
        if (this.getScrollState() == 2) {
            SeslOverScroller var2 = this.mViewFlinger.mScroller;
            var1.mRemainingScrollHorizontal = var2.getFinalX() - var2.getCurrX();
            var1.mRemainingScrollVertical = var2.getFinalY() - var2.getCurrY();
        } else {
            var1.mRemainingScrollHorizontal = 0;
            var1.mRemainingScrollVertical = 0;
        }

    }

    public View findChildViewUnder(float var1, float var2) {
        int var3 = this.mChildHelper.getChildCount() - 1;

        View var4;
        while (true) {
            if (var3 < 0) {
                var4 = null;
                break;
            }

            var4 = this.mChildHelper.getChildAt(var3);
            float var5 = var4.getTranslationX();
            float var6 = var4.getTranslationY();
            if (var1 >= (float) var4.getLeft() + var5 && var1 <= (float) var4.getRight() + var5 && var2 >= (float) var4.getTop() + var6 && var2 <= (float) var4.getBottom() + var6) {
                break;
            }

            --var3;
        }

        return var4;
    }

    public View findContainingItemView(View var1) {
        ViewParent var2;
        for (var2 = var1.getParent(); var2 != null && var2 != this && var2 instanceof View; var2 = var1.getParent()) {
            var1 = (View) var2;
        }

        if (var2 != this) {
            var1 = null;
        }

        return var1;
    }

    public RecyclerView.ViewHolder findContainingViewHolder(View var1) {
        var1 = this.findContainingItemView(var1);
        RecyclerView.ViewHolder var2;
        if (var1 == null) {
            var2 = null;
        } else {
            var2 = this.getChildViewHolder(var1);
        }

        return var2;
    }

    public int findFirstVisibleItemPosition() {
        int var1;
        if (this.mLayout instanceof SeslLinearLayoutManager) {
            var1 = ((SeslLinearLayoutManager) this.mLayout).findFirstVisibleItemPosition();
        } else if (this.mLayout instanceof StaggeredGridLayoutManager) {
            var1 = ((StaggeredGridLayoutManager) this.mLayout).findFirstVisibleItemPositions((int[]) null)[0];
        } else {
            var1 = -1;
        }

        return var1;
    }

    public int findLastVisibleItemPosition() {
        int var1;
        if (this.mLayout instanceof SeslLinearLayoutManager) {
            var1 = ((SeslLinearLayoutManager) this.mLayout).findLastVisibleItemPosition();
        } else if (this.mLayout instanceof StaggeredGridLayoutManager) {
            var1 = ((StaggeredGridLayoutManager) this.mLayout).findLastVisibleItemPositions((int[]) null)[0];
        } else {
            var1 = -1;
        }

        return var1;
    }

    public RecyclerView.ViewHolder findViewHolderForAdapterPosition(int var1) {
        RecyclerView.ViewHolder var2;
        if (this.mDataSetHasChangedAfterLayout) {
            var2 = null;
        } else {
            int var3 = this.mChildHelper.getUnfilteredChildCount();
            var2 = null;

            RecyclerView.ViewHolder var6;
            for (int var4 = 0; var4 < var3; var2 = var6) {
                RecyclerView.ViewHolder var5 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var4));
                var6 = var2;
                if (var5 != null) {
                    var6 = var2;
                    if (!var5.isRemoved()) {
                        var6 = var2;
                        if (this.getAdapterPositionFor(var5) == var1) {
                            var2 = var5;
                            if (!this.mChildHelper.isHidden(var5.itemView)) {
                                break;
                            }

                            var6 = var5;
                        }
                    }
                }

                ++var4;
            }
        }

        return var2;
    }

    public RecyclerView.ViewHolder findViewHolderForItemId(long var1) {
        RecyclerView.ViewHolder var3;
        if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
            int var4 = this.mChildHelper.getUnfilteredChildCount();
            var3 = null;

            RecyclerView.ViewHolder var7;
            for (int var5 = 0; var5 < var4; var3 = var7) {
                RecyclerView.ViewHolder var6 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var5));
                var7 = var3;
                if (var6 != null) {
                    var7 = var3;
                    if (!var6.isRemoved()) {
                        var7 = var3;
                        if (var6.getItemId() == var1) {
                            var3 = var6;
                            if (!this.mChildHelper.isHidden(var6.itemView)) {
                                break;
                            }

                            var7 = var6;
                        }
                    }
                }

                ++var5;
            }
        } else {
            var3 = null;
        }

        return var3;
    }

    public RecyclerView.ViewHolder findViewHolderForLayoutPosition(int var1) {
        return this.findViewHolderForPosition(var1, false);
    }

    @Deprecated
    public RecyclerView.ViewHolder findViewHolderForPosition(int var1) {
        return this.findViewHolderForPosition(var1, false);
    }

    RecyclerView.ViewHolder findViewHolderForPosition(int var1, boolean var2) {
        int var3 = this.mChildHelper.getUnfilteredChildCount();
        RecyclerView.ViewHolder var4 = null;

        RecyclerView.ViewHolder var7;
        for (int var5 = 0; var5 < var3; var4 = var7) {
            RecyclerView.ViewHolder var6 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var5));
            var7 = var4;
            if (var6 != null) {
                var7 = var4;
                if (!var6.isRemoved()) {
                    label34:
                    {
                        if (var2) {
                            if (var6.mPosition != var1) {
                                var7 = var4;
                                break label34;
                            }
                        } else {
                            var7 = var4;
                            if (var6.getLayoutPosition() != var1) {
                                break label34;
                            }
                        }

                        var4 = var6;
                        if (!this.mChildHelper.isHidden(var6.itemView)) {
                            break;
                        }

                        var7 = var6;
                    }
                }
            }

            ++var5;
        }

        return var4;
    }

    @SuppressLint("WrongConstant")
    public boolean fling(int var1, int var2) {
        boolean var3 = false;
        boolean var4;
        if (this.mLayout == null) {
            Log.e("SeslRecyclerView", "Cannot fling without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            var4 = var3;
        } else {
            var4 = var3;
            if (!this.mLayoutFrozen) {
                boolean var5;
                boolean var6;
                int var7;
                label57:
                {
                    var5 = this.mLayout.canScrollHorizontally();
                    var6 = this.mLayout.canScrollVertically();
                    if (var5) {
                        var7 = var1;
                        if (Math.abs(var1) >= this.mMinFlingVelocity) {
                            break label57;
                        }
                    }

                    var7 = 0;
                }

                int var8;
                label52:
                {
                    if (var6) {
                        var8 = var2;
                        if (Math.abs(var2) >= this.mMinFlingVelocity) {
                            break label52;
                        }
                    }

                    var8 = 0;
                }

                if (var7 == 0) {
                    var4 = var3;
                    if (var8 == 0) {
                        return var4;
                    }
                }

                var4 = var3;
                if (!this.dispatchNestedPreFling((float) var7, (float) var8)) {
                    boolean var9;
                    if (!var5 && !var6) {
                        var9 = false;
                    } else {
                        var9 = true;
                    }

                    this.dispatchNestedFling((float) var7, (float) var8, var9);
                    if (this.mOnFlingListener != null && this.mOnFlingListener.onFling(var7, var8)) {
                        var4 = true;
                    } else {
                        var4 = var3;
                        if (var9) {
                            var1 = 0;
                            if (var5) {
                                var1 = 0 | 1;
                            }

                            var2 = var1;
                            if (var6) {
                                var2 = var1 | 2;
                            }

                            this.startNestedScroll(var2, 1);
                            var1 = Math.max(-this.mMaxFlingVelocity, Math.min(var7, this.mMaxFlingVelocity));
                            var2 = Math.max(-this.mMaxFlingVelocity, Math.min(var8, this.mMaxFlingVelocity));
                            this.mViewFlinger.fling(var1, var2);
                            var4 = true;
                        }
                    }
                }
            }
        }

        return var4;
    }

    public View focusSearch(View var1, int var2) {
        View var3 = this.mLayout.onInterceptFocusSearch(var1, var2);
        if (var3 != null) {
            var1 = var3;
        } else {
            boolean var4;
            if (this.mAdapter != null && this.mLayout != null && !this.isComputingLayout() && !this.mLayoutFrozen) {
                var4 = true;
            } else {
                var4 = false;
            }

            FocusFinder var10 = FocusFinder.getInstance();
            int var6;
            View var8;
            if (var4 && (var2 == 2 || var2 == 1)) {
                boolean var5 = false;
                int var12 = var2;
                boolean var7;
                if (this.mLayout.canScrollVertically()) {
                    short var13;
                    if (var2 == 2) {
                        var13 = 130;
                    } else {
                        var13 = 33;
                    }

                    if (var10.findNextFocus(this, var1, var13) == null) {
                        var7 = true;
                    } else {
                        var7 = false;
                    }

                    var5 = var7;
                    var12 = var2;
                    if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                        var12 = var13;
                        var5 = var7;
                    }
                }

                var7 = var5;
                var6 = var12;
                if (!var5) {
                    var7 = var5;
                    var6 = var12;
                    if (this.mLayout.canScrollHorizontally()) {
                        boolean var9;
                        if (this.mLayout.getLayoutDirection() == 1) {
                            var9 = true;
                        } else {
                            var9 = false;
                        }

                        if (var12 == 2) {
                            var5 = true;
                        } else {
                            var5 = false;
                        }

                        byte var11;
                        if (var5 ^ var9) {
                            var11 = 66;
                        } else {
                            var11 = 17;
                        }

                        if (var10.findNextFocus(this, var1, var11) == null) {
                            var5 = true;
                        } else {
                            var5 = false;
                        }

                        var7 = var5;
                        var6 = var12;
                        if (FORCE_ABS_FOCUS_SEARCH_DIRECTION) {
                            var6 = var11;
                            var7 = var5;
                        }
                    }
                }

                if (var7) {
                    this.consumePendingUpdateOperations();
                    if (this.findContainingItemView(var1) == null) {
                        var1 = null;
                        return var1;
                    }

                    this.startInterceptRequestLayout();
                    this.mLayout.onFocusSearchFailed(var1, var6, this.mRecycler, this.mState);
                    this.stopInterceptRequestLayout(false);
                }

                var3 = var10.findNextFocus(this, var1, var6);
            } else {
                var8 = var10.findNextFocus(this, var1, var2);
                var3 = var8;
                var6 = var2;
                if (var8 == null) {
                    var3 = var8;
                    var6 = var2;
                    if (var4) {
                        this.consumePendingUpdateOperations();
                        if (this.findContainingItemView(var1) == null) {
                            var1 = null;
                            return var1;
                        }

                        this.startInterceptRequestLayout();
                        var3 = this.mLayout.onFocusSearchFailed(var1, var2, this.mRecycler, this.mState);
                        this.stopInterceptRequestLayout(false);
                        var6 = var2;
                    }
                }
            }

            if (var3 != null && !var3.hasFocusable()) {
                if (this.getFocusedChild() == null) {
                    var1 = super.focusSearch(var1, var6);
                } else {
                    this.requestChildOnScreen(var3, (View) null);
                }
            } else {
                var8 = var3;
                if (!this.isPreferredNextFocus(var1, var3, var6)) {
                    var8 = super.focusSearch(var1, var6);
                }

                if (this.mIsArrowKeyPressed && var8 == null && this.mLayout instanceof StaggeredGridLayoutManager) {
                    var2 = 0;
                    if (var6 == 130) {
                        var2 = this.getFocusedChild().getBottom() - this.getBottom();
                    } else if (var6 == 33) {
                        var2 = this.getFocusedChild().getTop() - this.getTop();
                    }

                    ((StaggeredGridLayoutManager) this.mLayout).scrollBy(var2, this.mRecycler, this.mState);
                    this.mIsArrowKeyPressed = false;
                }

                var1 = var8;
            }
        }

        return var1;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (this.mLayout == null) {
            throw new IllegalStateException("SeslRecyclerView has no LayoutManager" + this.exceptionLabel());
        } else {
            return this.mLayout.generateDefaultLayoutParams();
        }
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet var1) {
        if (this.mLayout == null) {
            throw new IllegalStateException("SeslRecyclerView has no LayoutManager" + this.exceptionLabel());
        } else {
            return this.mLayout.generateLayoutParams(this.getContext(), var1);
        }
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams var1) {
        if (this.mLayout == null) {
            throw new IllegalStateException("SeslRecyclerView has no LayoutManager" + this.exceptionLabel());
        } else {
            return this.mLayout.generateLayoutParams(var1);
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return this.mAdapter;
    }

    public void setAdapter(RecyclerView.Adapter var1) {
        this.setLayoutFrozen(false);
        this.setAdapterInternal(var1, false, true);
        this.processDataSetCompletelyChanged(false);
        this.requestLayout();
    }

    int getAdapterPositionFor(RecyclerView.ViewHolder var1) {
        int var2;
        if (!var1.hasAnyOfTheFlags(524) && var1.isBound()) {
            var2 = this.mAdapterHelper.applyPendingUpdatesToPosition(var1.mPosition);
        } else {
            var2 = -1;
        }

        return var2;
    }

    public int getBaseline() {
        int var1;
        if (this.mLayout != null) {
            var1 = this.mLayout.getBaseline();
        } else {
            var1 = super.getBaseline();
        }

        return var1;
    }

    long getChangedHolderKey(RecyclerView.ViewHolder var1) {
        long var2;
        if (this.mAdapter.hasStableIds()) {
            var2 = var1.getItemId();
        } else {
            var2 = (long) var1.mPosition;
        }

        return var2;
    }

    public int getChildAdapterPosition(View var1) {
        RecyclerView.ViewHolder var3 = getChildViewHolderInt(var1);
        int var2;
        if (var3 != null) {
            var2 = var3.getAdapterPosition();
        } else {
            var2 = -1;
        }

        return var2;
    }

    protected int getChildDrawingOrder(int var1, int var2) {
        if (this.mChildDrawingOrderCallback == null) {
            var1 = super.getChildDrawingOrder(var1, var2);
        } else {
            var1 = this.mChildDrawingOrderCallback.onGetChildDrawingOrder(var1, var2);
        }

        return var1;
    }

    public long getChildItemId(View var1) {
        long var2 = -1L;
        long var4 = var2;
        if (this.mAdapter != null) {
            if (!this.mAdapter.hasStableIds()) {
                var4 = var2;
            } else {
                RecyclerView.ViewHolder var6 = getChildViewHolderInt(var1);
                var4 = var2;
                if (var6 != null) {
                    var4 = var6.getItemId();
                }
            }
        }

        return var4;
    }

    public int getChildLayoutPosition(View var1) {
        RecyclerView.ViewHolder var3 = getChildViewHolderInt(var1);
        int var2;
        if (var3 != null) {
            var2 = var3.getLayoutPosition();
        } else {
            var2 = -1;
        }

        return var2;
    }

    @Deprecated
    public int getChildPosition(View var1) {
        return this.getChildAdapterPosition(var1);
    }

    public RecyclerView.ViewHolder getChildViewHolder(View var1) {
        ViewParent var2 = var1.getParent();
        if (var2 != null && var2 != this) {
            throw new IllegalArgumentException("View " + var1 + " is not a direct child of " + this);
        } else {
            return getChildViewHolderInt(var1);
        }
    }

    public boolean getClipToPadding() {
        return this.mClipToPadding;
    }

    public void setClipToPadding(boolean var1) {
        if (var1 != this.mClipToPadding) {
            this.invalidateGlows();
        }

        this.mClipToPadding = var1;
        super.setClipToPadding(var1);
        if (this.mFirstLayoutComplete) {
            this.requestLayout();
        }

    }

    public SeslRecyclerViewAccessibilityDelegate getCompatAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public void getDecoratedBoundsWithMargins(View var1, Rect var2) {
        getDecoratedBoundsWithMarginsInt(var1, var2);
    }

    public RecyclerView.ItemAnimator getItemAnimator() {
        return this.mItemAnimator;
    }

    public void setItemAnimator(RecyclerView.ItemAnimator var1) {
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
            this.mItemAnimator.setListener((RecyclerView.ItemAnimator.ItemAnimatorListener) null);
        }

        this.mItemAnimator = var1;
        if (this.mItemAnimator != null) {
            this.mItemAnimator.setListener(this.mItemAnimatorListener);
        }

    }

    Rect getItemDecorInsetsForChild(View var1) {
        RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var1.getLayoutParams();
        Rect var6;
        if (!var2.mInsetsDirty) {
            var6 = var2.mDecorInsets;
        } else if (this.mState.isPreLayout() && (var2.isItemChanged() || var2.isViewInvalid())) {
            var6 = var2.mDecorInsets;
        } else {
            Rect var3 = var2.mDecorInsets;
            var3.set(0, 0, 0, 0);
            int var4 = this.mItemDecorations.size();

            for (int var5 = 0; var5 < var4; ++var5) {
                this.mTempRect.set(0, 0, 0, 0);
                ((RecyclerView.ItemDecoration) this.mItemDecorations.get(var5)).getItemOffsets(this.mTempRect, var1, this, this.mState);
                var3.left += this.mTempRect.left;
                var3.top += this.mTempRect.top;
                var3.right += this.mTempRect.right;
                var3.bottom += this.mTempRect.bottom;
            }

            var2.mInsetsDirty = false;
            var6 = var3;
        }

        return var6;
    }

    public RecyclerView.ItemDecoration getItemDecorationAt(int var1) {
        int var2 = this.getItemDecorationCount();
        if (var1 >= 0 && var1 < var2) {
            return (RecyclerView.ItemDecoration) this.mItemDecorations.get(var1);
        } else {
            throw new IndexOutOfBoundsException(var1 + " is an invalid index for size " + var2);
        }
    }

    public int getItemDecorationCount() {
        return this.mItemDecorations.size();
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return this.mLayout;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager != this.mLayout) {
            boolean z = layoutManager instanceof SeslLinearLayoutManager;
            boolean z2 = true;
            this.mDrawRect = this.mDrawRect && z;
            if (!this.mDrawLastRoundedCorner || !z) {
                z2 = false;
            }
            this.mDrawLastRoundedCorner = z2;
            stopScroll();
            if (this.mLayout != null) {
                ItemAnimator itemAnimator = this.mItemAnimator;
                if (itemAnimator != null) {
                    itemAnimator.endAnimations();
                }
                this.mLayout.removeAndRecycleAllViews(this.mRecycler);
                this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
                this.mRecycler.clear();
                if (this.mIsAttached) {
                    this.mLayout.dispatchDetachedFromWindow(this, this.mRecycler);
                }
                this.mLayout.setRecyclerView(null);
                this.mLayout = null;
            } else {
                this.mRecycler.clear();
            }
            this.mChildHelper.removeAllViewsUnfiltered();
            this.mLayout = layoutManager;
            if (layoutManager != null) {
                if (layoutManager.mRecyclerView == null) {
                    layoutManager.setRecyclerView(this);
                    if (this.mIsAttached) {
                        this.mLayout.dispatchAttachedToWindow(this);
                    }
                } else {
                    throw new IllegalArgumentException("LayoutManager " + layoutManager + " is already attached to a RecyclerView:" + layoutManager.mRecyclerView.exceptionLabel());
                }
            }
            this.mRecycler.updateViewCacheSize();
            requestLayout();
        }
    }

    public final RecyclerView.SeslLongPressMultiSelectionListener getLongPressMultiSelectionListener() {
        return this.mLongPressMultiSelectionListener;
    }

    public int getMaxFlingVelocity() {
        return this.mMaxFlingVelocity;
    }

    public int getMinFlingVelocity() {
        return this.mMinFlingVelocity;
    }

    public long getNanoTime() {
        long var1;
        if (ALLOW_THREAD_GAP_WORK) {
            var1 = System.nanoTime();
        } else {
            var1 = 0L;
        }

        return var1;
    }

    public RecyclerView.OnFlingListener getOnFlingListener() {
        return this.mOnFlingListener;
    }

    public void setOnFlingListener(RecyclerView.OnFlingListener var1) {
        this.mOnFlingListener = var1;
    }

    public boolean getPreserveFocusAfterLayout() {
        return this.mPreserveFocusAfterLayout;
    }

    public void setPreserveFocusAfterLayout(boolean var1) {
        this.mPreserveFocusAfterLayout = var1;
    }

    public RecyclerView.RecycledViewPool getRecycledViewPool() {
        return this.mRecycler.getRecycledViewPool();
    }

    public void setRecycledViewPool(RecyclerView.RecycledViewPool var1) {
        this.mRecycler.setRecycledViewPool(var1);
    }

    public int getScrollState() {
        return this.mScrollState;
    }

    void setScrollState(int var1) {
        if (var1 != this.mScrollState) {
            Log.d("SeslRecyclerView", "setting scroll state to " + var1 + " from " + this.mScrollState);
            this.mScrollState = var1;
            if (var1 != 2) {
                this.stopScrollersInternal();
            }

            this.dispatchOnScrollStateChanged(var1);
        }

    }

    public boolean hasFixedSize() {
        return this.mHasFixedSize;
    }

    public boolean hasNestedScrollingParent() {
        return this.getScrollingChildHelper().hasNestedScrollingParent();
    }

    public boolean hasNestedScrollingParent(int var1) {
        return this.getScrollingChildHelper().hasNestedScrollingParent(var1);
    }

    public boolean hasPendingAdapterUpdates() {
        boolean var1;
        if (this.mFirstLayoutComplete && !this.mDataSetHasChangedAfterLayout && !this.mAdapterHelper.hasPendingUpdates()) {
            var1 = false;
        } else {
            var1 = true;
        }

        return var1;
    }

    void initAdapterManager() {
        this.mAdapterHelper = new SeslAdapterHelper(new SeslAdapterHelper.Callback() {
            void dispatchUpdate(SeslAdapterHelper.UpdateOp var1) {
                switch (var1.cmd) {
                    case 1:
                        RecyclerView.this.mLayout.onItemsAdded(RecyclerView.this, var1.positionStart, var1.itemCount);
                        break;
                    case 2:
                        RecyclerView.this.mLayout.onItemsRemoved(RecyclerView.this, var1.positionStart, var1.itemCount);
                    case 3:
                    case 5:
                    case 6:
                    case 7:
                    default:
                        break;
                    case 4:
                        RecyclerView.this.mLayout.onItemsUpdated(RecyclerView.this, var1.positionStart, var1.itemCount, var1.payload);
                        break;
                    case 8:
                        RecyclerView.this.mLayout.onItemsMoved(RecyclerView.this, var1.positionStart, var1.itemCount, 1);
                }

            }

            public RecyclerView.ViewHolder findViewHolder(int var1) {
                RecyclerView.ViewHolder var2 = RecyclerView.this.findViewHolderForPosition(var1, true);
                RecyclerView.ViewHolder var3;
                if (var2 == null) {
                    var3 = null;
                } else {
                    var3 = var2;
                    if (RecyclerView.this.mChildHelper.isHidden(var2.itemView)) {
                        var3 = null;
                    }
                }

                return var3;
            }

            public void markViewHoldersUpdated(int var1, int var2, Object var3) {
                RecyclerView.this.viewRangeUpdate(var1, var2, var3);
                RecyclerView.this.mItemsChanged = true;
            }

            public void offsetPositionsForAdd(int var1, int var2) {
                RecyclerView.this.offsetPositionRecordsForInsert(var1, var2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void offsetPositionsForMove(int var1, int var2) {
                RecyclerView.this.offsetPositionRecordsForMove(var1, var2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void offsetPositionsForRemovingInvisible(int var1, int var2) {
                RecyclerView.this.offsetPositionRecordsForRemove(var1, var2, true);
                RecyclerView.this.mItemsAddedOrRemoved = true;
                RecyclerView.State var3 = RecyclerView.this.mState;
                var3.mDeletedInvisibleItemCountSincePreviousLayout += var2;
            }

            public void offsetPositionsForRemovingLaidOutOrNewView(int var1, int var2) {
                RecyclerView.this.offsetPositionRecordsForRemove(var1, var2, false);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void onDispatchFirstPass(SeslAdapterHelper.UpdateOp var1) {
                this.dispatchUpdate(var1);
            }

            public void onDispatchSecondPass(SeslAdapterHelper.UpdateOp var1) {
                this.dispatchUpdate(var1);
            }
        });
    }

    void initFastScroller(StateListDrawable var1, Drawable var2, StateListDrawable var3, Drawable var4) {
        if (var1 != null && var2 != null && var3 != null && var4 != null) {
            Resources var5 = this.getContext().getResources();
            new FastScroller(this, var1, var2, var3, var4, var5.getDimensionPixelSize(R.dimen.fastscroll_default_thickness), var5.getDimensionPixelSize(R.dimen.fastscroll_minimum_range), var5.getDimensionPixelOffset(R.dimen.fastscroll_margin));
        } else {
            throw new IllegalArgumentException("Trying to set fast scroller without both required drawables." + this.exceptionLabel());
        }
    }

    void invalidateGlows() {
        this.mBottomGlow = null;
        this.mTopGlow = null;
        this.mRightGlow = null;
        this.mLeftGlow = null;
    }

    public void invalidateItemDecorations() {
        if (this.mItemDecorations.size() != 0) {
            if (this.mLayout != null) {
                this.mLayout.assertNotInLayoutOrScroll("Cannot invalidate item decorations during a scroll or layout");
            }

            this.markItemDecorInsetsDirty();
            this.requestLayout();
        }

    }

    boolean isAccessibilityEnabled() {
        boolean var1;
        if (this.mAccessibilityManager != null && this.mAccessibilityManager.isEnabled()) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public boolean isAnimating() {
        boolean var1;
        if (this.mItemAnimator != null && this.mItemAnimator.isRunning()) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public boolean isAttachedToWindow() {
        return this.mIsAttached;
    }

    public boolean isComputingLayout() {
        boolean var1;
        if (this.mLayoutOrScrollCounter > 0) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public boolean isInScrollingContainer() {
        ViewParent var1 = this.getParent();

        boolean var2;
        while (true) {
            if (var1 == null || !(var1 instanceof ViewGroup)) {
                var2 = false;
                break;
            }

            if (((ViewGroup) var1).shouldDelayChildPressedState()) {
                var2 = true;
                break;
            }

            var1 = var1.getParent();
        }

        return var2;
    }

    public boolean isLayoutFrozen() {
        return this.mLayoutFrozen;
    }

    public void setLayoutFrozen(boolean var1) {
        if (var1 != this.mLayoutFrozen) {
            this.assertNotInLayoutOrScroll("Do not setLayoutFrozen in layout or scroll");
            if (!var1) {
                this.mLayoutFrozen = false;
                if (this.mLayoutWasDefered && this.mLayout != null && this.mAdapter != null) {
                    this.requestLayout();
                }

                this.mLayoutWasDefered = false;
            } else {
                long var2 = SystemClock.uptimeMillis();
                this.onTouchEvent(MotionEvent.obtain(var2, var2, 3, 0.0F, 0.0F, 0));
                this.mLayoutFrozen = true;
                this.mIgnoreMotionEventTillDown = true;
                this.stopScroll();
            }
        }

    }

    public boolean isLockScreenMode() {
        Context var1 = this.mContext;
        Context var2 = this.mContext;
        boolean var3;
        if (!((KeyguardManager) var1.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode() && true) {
            var3 = false;
        } else {
            var3 = true;
        }

        return var3;
    }

    public boolean isNestedScrollingEnabled() {
        return this.getScrollingChildHelper().isNestedScrollingEnabled();
    }

    public void setNestedScrollingEnabled(boolean var1) {
        this.getScrollingChildHelper().setNestedScrollingEnabled(var1);
    }

    public boolean isVerticalScrollBarEnabled() {
        boolean var1;
        if (!this.mFastScrollerEnabled && super.isVerticalScrollBarEnabled()) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    void jumpToPositionForSmoothScroller(int var1) {
        if (this.mLayout != null) {
            this.mLayout.scrollToPosition(var1);
            this.awakenScrollBars();
        }

    }

    void markItemDecorInsetsDirty() {
        int var1 = this.mChildHelper.getUnfilteredChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            ((RecyclerView.LayoutParams) this.mChildHelper.getUnfilteredChildAt(var2).getLayoutParams()).mInsetsDirty = true;
        }

        this.mRecycler.markItemDecorInsetsDirty();
    }

    void markKnownViewsInvalid() {
        int var1 = this.mChildHelper.getUnfilteredChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            RecyclerView.ViewHolder var3 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var2));
            if (var3 != null && !var3.shouldIgnore()) {
                var3.addFlags(6);
            }
        }

        this.markItemDecorInsetsDirty();
        this.mRecycler.markKnownViewsInvalid();
    }

    public void offsetChildrenHorizontal(int var1) {
        int var2 = this.mChildHelper.getChildCount();

        for (int var3 = 0; var3 < var2; ++var3) {
            this.mChildHelper.getChildAt(var3).offsetLeftAndRight(var1);
        }

    }

    public void offsetChildrenVertical(int var1) {
        int var2 = this.mChildHelper.getChildCount();

        for (int var3 = 0; var3 < var2; ++var3) {
            this.mChildHelper.getChildAt(var3).offsetTopAndBottom(var1);
        }

    }

    void offsetPositionRecordsForInsert(int var1, int var2) {
        int var3 = this.mChildHelper.getUnfilteredChildCount();

        for (int var4 = 0; var4 < var3; ++var4) {
            RecyclerView.ViewHolder var5 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var4));
            if (var5 != null && !var5.shouldIgnore() && var5.mPosition >= var1) {
                var5.offsetPosition(var2, false);
                this.mState.mStructureChanged = true;
            }
        }

        this.mRecycler.offsetPositionRecordsForInsert(var1, var2);
        this.requestLayout();
    }

    void offsetPositionRecordsForMove(int var1, int var2) {
        int var3 = this.mChildHelper.getUnfilteredChildCount();
        int var4;
        int var5;
        byte var6;
        if (var1 < var2) {
            var4 = var1;
            var5 = var2;
            var6 = -1;
        } else {
            var4 = var2;
            var5 = var1;
            var6 = 1;
        }

        for (int var7 = 0; var7 < var3; ++var7) {
            RecyclerView.ViewHolder var8 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var7));
            if (var8 != null && var8.mPosition >= var4 && var8.mPosition <= var5) {
                if (var8.mPosition == var1) {
                    var8.offsetPosition(var2 - var1, false);
                } else {
                    var8.offsetPosition(var6, false);
                }

                this.mState.mStructureChanged = true;
            }
        }

        this.mRecycler.offsetPositionRecordsForMove(var1, var2);
        this.requestLayout();
    }

    void offsetPositionRecordsForRemove(int var1, int var2, boolean var3) {
        int var4 = this.mChildHelper.getUnfilteredChildCount();

        for (int var5 = 0; var5 < var4; ++var5) {
            RecyclerView.ViewHolder var6 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var5));
            if (var6 != null && !var6.shouldIgnore()) {
                if (var6.mPosition >= var1 + var2) {
                    var6.offsetPosition(-var2, var3);
                    this.mState.mStructureChanged = true;
                } else if (var6.mPosition >= var1) {
                    var6.flagRemovedAndOffsetPosition(var1 - 1, -var2, var3);
                    this.mState.mStructureChanged = true;
                }
            }
        }

        this.mRecycler.offsetPositionRecordsForRemove(var1, var2, var3);
        this.requestLayout();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mLayoutOrScrollCounter = 0;
        this.mIsAttached = true;
        boolean var1;
        if (this.mFirstLayoutComplete && !this.isLayoutRequested()) {
            var1 = true;
        } else {
            var1 = false;
        }

        this.mFirstLayoutComplete = var1;
        if (this.mLayout != null) {
            this.mLayout.dispatchAttachedToWindow(this);
        }

        this.mPostedAnimatorRunner = false;
        if (ALLOW_THREAD_GAP_WORK) {
            this.mGapWorker = (SeslGapWorker) SeslGapWorker.sGapWorker.get();
            if (this.mGapWorker == null) {
                this.mGapWorker = new SeslGapWorker();
                Display var2 = ViewCompat.getDisplay(this);
                float var3 = 60.0F;
                float var4 = var3;
                if (!this.isInEditMode()) {
                    var4 = var3;
                    if (var2 != null) {
                        float var5 = var2.getRefreshRate();
                        var4 = var3;
                        if (var5 >= 30.0F) {
                            var4 = var5;
                        }
                    }
                }

                this.mGapWorker.mFrameIntervalNs = (long) (1.0E9F / var4);
                SeslGapWorker.sGapWorker.set(this.mGapWorker);
            }

            this.mGapWorker.add(this);
            if (this.mLayout != null && this.mLayout.getLayoutDirection() == 1 && this.mFastScroller != null) {
                this.mFastScroller.setScrollbarPosition(this.getVerticalScrollbarPosition());
            }
        }

    }

    public void onChildAttachedToWindow(View var1) {
    }

    public void onChildDetachedFromWindow(View var1) {
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
        }

        this.stopScroll();
        this.mIsAttached = false;
        if (this.mLayout != null) {
            this.mLayout.dispatchDetachedFromWindow(this, this.mRecycler);
        }

        this.mPendingAccessibilityImportanceChange.clear();
        this.removeCallbacks(this.mItemAnimatorRunner);
        this.mViewInfoStore.onDetach();
        if (ALLOW_THREAD_GAP_WORK && this.mGapWorker != null) {
            this.mGapWorker.remove(this);
            this.mGapWorker = null;
        }

    }

    public void onDraw(Canvas var1) {
        super.onDraw(var1);

        int var2 = this.mItemDecorations.size();

        for (int var3 = 0; var3 < var2; ++var3) {
            ((RecyclerView.ItemDecoration) this.mItemDecorations.get(var3)).onDraw(var1, this, this.mState);
        }

    }

    public void onEnterLayoutOrScroll() {
        ++this.mLayoutOrScrollCounter;
    }

    void onExitLayoutOrScroll() {
        this.onExitLayoutOrScroll(true);
    }

    public void onExitLayoutOrScroll(boolean var1) {
        --this.mLayoutOrScrollCounter;
        if (this.mLayoutOrScrollCounter < 1) {
            this.mLayoutOrScrollCounter = 0;
            if (var1) {
                this.dispatchContentChangedIfNecessary();
                this.dispatchPendingImportantForAccessibilityChanges();
            }
        }

    }

    @SuppressLint("WrongConstant")
    public boolean onGenericMotionEvent(MotionEvent var1) {
        if (this.mLayout != null && !this.mLayoutFrozen && var1.getAction() == 8) {
            this.mIsMouseWheel = true;
            float var2;
            float var3;
            if ((var1.getSource() & 2) != 0) {
                if (this.mLayout.canScrollVertically()) {
                    var2 = -var1.getAxisValue(9);
                } else {
                    var2 = 0.0F;
                }

                if (this.mLayout.canScrollHorizontally()) {
                    var3 = var1.getAxisValue(10);
                } else {
                    var3 = 0.0F;
                }
            } else if ((var1.getSource() & 4194304) != 0) {
                var3 = var1.getAxisValue(26);
                if (this.mLayout.canScrollVertically()) {
                    var2 = -var3;
                    var3 = 0.0F;
                } else if (this.mLayout.canScrollHorizontally()) {
                    var2 = 0.0F;
                } else {
                    var2 = 0.0F;
                    var3 = 0.0F;
                }
            } else {
                var2 = 0.0F;
                var3 = 0.0F;
            }

            if (var2 != 0.0F || var3 != 0.0F) {
                byte var4;
                if (var2 != 0.0F) {
                    var4 = 2;
                } else {
                    var4 = 1;
                }

                this.startNestedScroll(var4, 1);
                if (!this.dispatchNestedPreScroll((int) (this.mScaledHorizontalScrollFactor * var3), (int) (this.mScaledVerticalScrollFactor * var2), (int[]) null, (int[]) null, 1)) {
                    this.scrollByInternal((int) (this.mScaledHorizontalScrollFactor * var3), (int) (this.mScaledVerticalScrollFactor * var2), var1);
                }
            }
        }

        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent var1) {
        boolean var2;
        if (this.mLayoutFrozen) {
            var2 = false;
        } else if (this.dispatchOnItemTouchIntercept(var1)) {
            this.cancelTouch();
            var2 = true;
        } else if (this.mLayout == null) {
            var2 = false;
        } else {
            boolean var3 = this.mLayout.canScrollHorizontally();
            var2 = this.mLayout.canScrollVertically();
            boolean var4;
            if (var1 != null && MotionEventCompat.isFromSource(var1, 8194)) {
                var4 = true;
            } else {
                var4 = false;
            }

            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }

            this.mVelocityTracker.addMovement(var1);
            int var5 = var1.getActionMasked();
            int var6 = var1.getActionIndex();
            MotionEvent var7 = MotionEvent.obtain(var1);
            if (this.mFastScroller != null && this.mFastScroller.onInterceptTouchEvent(var1)) {
                var2 = true;
            } else {
                int var14;
                byte var17;
                switch (var5) {
                    case 0:
                        if (this.mIgnoreMotionEventTillDown) {
                            this.mIgnoreMotionEventTillDown = false;
                        }

                        this.mScrollPointerId = var1.getPointerId(0);
                        var6 = (int) (var1.getX() + 0.5F);
                        this.mLastTouchX = var6;
                        this.mInitialTouchX = var6;
                        var6 = (int) (var1.getY() + 0.5F);
                        this.mLastTouchY = var6;
                        this.mInitialTouchY = var6;
                        if (this.mUsePagingTouchSlopForStylus) {
                            if (var1.isFromSource(16386)) {
                                this.mTouchSlop = this.mSeslPagingTouchSlop;
                            } else {
                                this.mTouchSlop = this.mSeslTouchSlop;
                            }
                        }

                        if (this.mScrollState == 2) {
                            this.getParent().requestDisallowInterceptTouchEvent(true);
                            this.setScrollState(1);
                        }

                        int[] var13 = this.mNestedOffsets;
                        this.mNestedOffsets[1] = 0;
                        var13[0] = 0;
                        if (this.mHasNestedScrollRange) {
                            this.adjustNestedScrollRange();
                        }

                        var6 = 0;
                        if (var3) {
                            var6 = 0 | 1;
                        }

                        var5 = var6;
                        if (var2) {
                            var5 = var6 | 2;
                        }

                        if (var4) {
                            var17 = 1;
                        } else {
                            var17 = 0;
                        }

                        this.startNestedScroll(var5, var17);
                        break;
                    case 1:
                        this.mVelocityTracker.clear();
                        if (var4) {
                            var17 = 1;
                        } else {
                            var17 = 0;
                        }

                        this.stopNestedScroll(var17);
                        break;
                    case 2:
                        var6 = var1.findPointerIndex(this.mScrollPointerId);
                        if (var6 < 0) {
                            Log.e("SeslRecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                            var2 = false;
                            return var2;
                        }

                        int var8 = (int) (var1.getX(var6) + 0.5F);
                        int var9 = (int) (var1.getY(var6) + 0.5F);
                        int var10 = this.mLastTouchX - var8;
                        int var11 = this.mLastTouchY - var9;
                        var6 = var10;
                        var5 = var11;
                        if (this.mScrollState != 1) {
                            boolean var15 = false;
                            var14 = var10;
                            boolean var16 = var15;
                            if (var3) {
                                var14 = var10;
                                var16 = var15;
                                if (Math.abs(var10) > this.mTouchSlop) {
                                    if (var10 > 0) {
                                        var14 = var10 - this.mTouchSlop;
                                    } else {
                                        var14 = var10 + this.mTouchSlop;
                                    }

                                    var16 = true;
                                }
                            }

                            var10 = var11;
                            boolean var12 = var16;
                            if (var2) {
                                var10 = var11;
                                var12 = var16;
                                if (Math.abs(var11) > this.mTouchSlop) {
                                    if (var11 > 0) {
                                        var10 = var11 - this.mTouchSlop;
                                    } else {
                                        var10 = var11 + this.mTouchSlop;
                                    }

                                    var12 = true;
                                }
                            }

                            var6 = var14;
                            var5 = var10;
                            if (var12) {
                                this.setScrollState(1);
                                var5 = var10;
                                var6 = var14;
                            }
                        }

                        if (this.mScrollState == 1) {
                            this.mLastTouchX = var8 - this.mScrollOffset[0];
                            this.mLastTouchY = var9 - this.mScrollOffset[1];
                            if (!this.mGoToTopMoved) {
                                if (var3) {
                                    var14 = var6;
                                } else {
                                    var14 = 0;
                                }

                                if (var2) {
                                    var10 = var5;
                                } else {
                                    var10 = 0;
                                }

                                if (this.scrollByInternal(var14, var10, var7)) {
                                    this.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                            }

                            if (this.mGapWorker != null && (var6 != 0 || var5 != 0)) {
                                this.mGapWorker.postFromTraversal(this, var6, var5);
                            }
                        }

                        this.adjustNestedScrollRangeBy(var5);
                        break;
                    case 3:
                        this.cancelTouch();
                    case 4:
                    default:
                        break;
                    case 5:
                        this.mScrollPointerId = var1.getPointerId(var6);
                        var14 = (int) (var1.getX(var6) + 0.5F);
                        this.mLastTouchX = var14;
                        this.mInitialTouchX = var14;
                        var6 = (int) (var1.getY(var6) + 0.5F);
                        this.mLastTouchY = var6;
                        this.mInitialTouchY = var6;
                        break;
                    case 6:
                        this.onPointerUp(var1);
                }

                if (this.mScrollState == 1) {
                    var2 = true;
                } else {
                    var2 = false;
                }
            }
        }

        return var2;
    }

    public boolean onKeyDown(int var1, KeyEvent var2) {
        switch (var1) {
            case 92:
                if (var2.hasNoModifiers()) {
                    this.pageScroll(0);
                }
                break;
            case 93:
                if (var2.hasNoModifiers()) {
                    this.pageScroll(1);
                }
                break;
            case 113:
            case 114:
                this.mIsCtrlKeyPressed = true;
                break;
            case 122:
                if (var2.hasNoModifiers()) {
                    this.pageScroll(2);
                }
                break;
            case 123:
                if (var2.hasNoModifiers()) {
                    this.pageScroll(3);
                }
        }

        return super.onKeyDown(var1, var2);
    }

    public boolean onKeyUp(int var1, KeyEvent var2) {
        switch (var1) {
            case 113:
            case 114:
                this.mIsCtrlKeyPressed = false;
            default:
                return super.onKeyUp(var1, var2);
        }
    }

    protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
        TraceCompat.beginSection("RV OnLayout");
        this.dispatchLayout();
        TraceCompat.endSection();
        this.mFirstLayoutComplete = true;
        if (this.mFastScroller != null && this.mAdapter != null) {
            this.mFastScroller.onItemCountChanged(this.getChildCount(), this.mAdapter.getItemCount());
        }

        if (var1) {
            this.mSizeChnage = true;
            this.seslSetImmersiveScrollBottomPadding(0);
            this.setupGoToTop(-1);
            this.autoHide(1);
            this.mHasNestedScrollRange = false;

            for (ViewParent var6 = this.getParent(); var6 != null && var6 instanceof ViewGroup; var6 = var6.getParent()) {
                if (var6 instanceof NestedScrollingParent2) {
                    ((ViewGroup) var6).getLocationInWindow(this.mWindowOffsets);
                    var2 = this.mWindowOffsets[1];
                    var3 = ((ViewGroup) var6).getHeight();
                    this.getLocationInWindow(this.mWindowOffsets);
                    this.mInitialTopOffsetOfScreen = this.mWindowOffsets[1];
                    this.mRemainNestedScrollRange = this.getHeight() - (var2 + var3 - this.mInitialTopOffsetOfScreen);
                    if (this.mRemainNestedScrollRange < 0) {
                        this.mRemainNestedScrollRange = 0;
                    }

                    this.mNestedScrollRange = this.mRemainNestedScrollRange;
                    this.mHasNestedScrollRange = true;
                    break;
                }
            }

            if (!this.mHasNestedScrollRange) {
                this.mInitialTopOffsetOfScreen = 0;
                this.mRemainNestedScrollRange = 0;
                this.mNestedScrollRange = 0;
            }
        }

    }

    @SuppressLint("WrongConstant")
    protected void onMeasure(int var1, int var2) {
        boolean var3 = false;
        if (this.mLayout == null) {
            this.defaultOnMeasure(var1, var2);
        } else {
            Rect var4 = this.mListPadding;
            var4.left = this.getPaddingLeft();
            var4.right = this.getPaddingRight();
            var4.top = this.getPaddingTop();
            var4.bottom = this.getPaddingBottom();
            if (this.getResources().getDisplayMetrics().heightPixels < this.getMeasuredHeight()) {
                Log.d("SeslRecyclerView", "h = " + this.getMeasuredHeight() + "auto = " + this.mLayout.isAutoMeasureEnabled() + ", fixedSize = " + this.mHasFixedSize);
                if (this.getParent() != null) {
                    Log.d("SeslRecyclerView", "p = " + this.getParent() + ", ph =" + ((View) this.getParent()).getMeasuredHeight());
                }
            }

            if (this.mLayout.isAutoMeasureEnabled()) {
                int var5 = MeasureSpec.getMode(var1);
                int var6 = MeasureSpec.getMode(var2);
                this.mLayout.onMeasure(this.mRecycler, this.mState, var1, var2);
                boolean var7 = var3;
                if (var5 == 1073741824) {
                    var7 = var3;
                    if (var6 == 1073741824) {
                        var7 = true;
                    }
                }

                if (!var7 && this.mAdapter != null) {
                    if (this.mState.mLayoutStep == 1) {
                        this.dispatchLayoutStep1();
                    }

                    this.mLayout.setMeasureSpecs(var1, var2);
                    this.mState.mIsMeasuring = true;
                    this.dispatchLayoutStep2();
                    this.mLayout.setMeasuredDimensionFromChildren(var1, var2);
                    if (this.mLayout.shouldMeasureTwice()) {
                        this.mLayout.setMeasureSpecs(MeasureSpec.makeMeasureSpec(this.getMeasuredWidth(), 1073741824), MeasureSpec.makeMeasureSpec(this.getMeasuredHeight(), 1073741824));
                        this.mState.mIsMeasuring = true;
                        this.dispatchLayoutStep2();
                        this.mLayout.setMeasuredDimensionFromChildren(var1, var2);
                    }
                }
            } else if (this.mHasFixedSize) {
                this.mLayout.onMeasure(this.mRecycler, this.mState, var1, var2);
            } else {
                if (this.mAdapterUpdateDuringMeasure) {
                    this.startInterceptRequestLayout();
                    this.onEnterLayoutOrScroll();
                    this.processAdapterUpdatesAndSetAnimationFlags();
                    this.onExitLayoutOrScroll();
                    if (this.mState.mRunPredictiveAnimations) {
                        this.mState.mInPreLayout = true;
                    } else {
                        this.mAdapterHelper.consumeUpdatesInOnePass();
                        this.mState.mInPreLayout = false;
                    }

                    this.mAdapterUpdateDuringMeasure = false;
                    this.stopInterceptRequestLayout(false);
                } else if (this.mState.mRunPredictiveAnimations) {
                    this.setMeasuredDimension(this.getMeasuredWidth(), this.getMeasuredHeight());
                    return;
                }

                if (this.mAdapter != null) {
                    this.mState.mItemCount = this.mAdapter.getItemCount();
                } else {
                    this.mState.mItemCount = 0;
                }

                this.startInterceptRequestLayout();
                this.mLayout.onMeasure(this.mRecycler, this.mState, var1, var2);
                this.stopInterceptRequestLayout(false);
                this.mState.mInPreLayout = false;
            }
        }

    }

    protected boolean onRequestFocusInDescendants(int var1, Rect var2) {
        boolean var3;
        if (this.isComputingLayout()) {
            var3 = false;
        } else {
            var3 = super.onRequestFocusInDescendants(var1, var2);
        }

        return var3;
    }

    protected void onRestoreInstanceState(Parcelable var1) {
        if (!(var1 instanceof RecyclerView.SavedState)) {
            super.onRestoreInstanceState(var1);
        } else {
            this.mPendingSavedState = (RecyclerView.SavedState) var1;
            super.onRestoreInstanceState(this.mPendingSavedState.getSuperState());
            if (this.mLayout != null && this.mPendingSavedState.mLayoutState != null) {
                this.mLayout.onRestoreInstanceState(this.mPendingSavedState.mLayoutState);
            }
        }

    }

    protected Parcelable onSaveInstanceState() {
        RecyclerView.SavedState var1 = new RecyclerView.SavedState(super.onSaveInstanceState());
        if (this.mPendingSavedState != null) {
            var1.copyFrom(this.mPendingSavedState);
        } else if (this.mLayout != null) {
            var1.mLayoutState = this.mLayout.onSaveInstanceState();
        } else {
            var1.mLayoutState = null;
        }

        return var1;
    }

    public void onScrollStateChanged(int var1) {
    }

    public void onScrolled(int var1, int var2) {
    }

    protected void onSizeChanged(int var1, int var2, int var3, int var4) {
        super.onSizeChanged(var1, var2, var3, var4);
        if (var1 != var3 || var2 != var4) {
            this.invalidateGlows();
        }

        if (this.mFastScroller != null) {
            this.mFastScroller.onSizeChanged(var1, var2, var3, var4);
        }

    }

    @SuppressLint("WrongConstant")
    public boolean onTouchEvent(MotionEvent var1) {
        boolean var2;
        if (!this.mLayoutFrozen && !this.mIgnoreMotionEventTillDown) {
            if (this.dispatchOnItemTouch(var1)) {
                this.cancelTouch();
                var2 = true;
            } else if (this.mLayout == null) {
                var2 = false;
            } else {
                this.mIsMouseWheel = false;
                var2 = this.mLayout.canScrollHorizontally();
                boolean var3 = this.mLayout.canScrollVertically();
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                }

                boolean var4 = false;
                MotionEvent var5 = MotionEvent.obtain(var1);
                int var6 = var1.getActionMasked();
                int var7 = var1.getActionIndex();
                if (var6 == 0) {
                    int[] var8 = this.mNestedOffsets;
                    this.mNestedOffsets[1] = 0;
                    var8[0] = 0;
                }

                var5.offsetLocation((float) this.mNestedOffsets[0], (float) this.mNestedOffsets[1]);
                int var21;
                if (this.mFastScroller != null && this.mFastScroller.onTouchEvent(var1)) {
                    if (this.mFastScrollerEventListener != null) {
                        label181:
                        {
                            if (var1.getActionMasked() == 0 || var1.getActionMasked() == 2) {
                                var21 = this.mFastScroller.getEffectState();
                                SeslRecyclerViewFastScroller var20 = this.mFastScroller;
                                if (var21 == 1) {
                                    this.mFastScrollerEventListener.onPressed(this.mFastScroller.getScrollY());
                                    break label181;
                                }
                            }

                            if (var1.getActionMasked() == 1) {
                                var21 = this.mFastScroller.getEffectState();
                                SeslRecyclerViewFastScroller var18 = this.mFastScroller;
                                if (var21 == 0) {
                                    this.mFastScrollerEventListener.onReleased(this.mFastScroller.getScrollY());
                                }
                            }
                        }
                    }

                    var5.recycle();
                    var2 = true;
                } else {
                    boolean var9 = var4;
                    switch (var6) {
                        case 0:
                            this.mScrollPointerId = var1.getPointerId(0);
                            var21 = (int) (var1.getX() + 0.5F);
                            this.mLastTouchX = var21;
                            this.mInitialTouchX = var21;
                            var21 = (int) (var1.getY() + 0.5F);
                            this.mLastTouchY = var21;
                            this.mInitialTouchY = var21;
                            if (this.mHasNestedScrollRange) {
                                this.adjustNestedScrollRange();
                            }

                            var21 = 0;
                            if (var2) {
                                var21 = 0 | 1;
                            }

                            var7 = var21;
                            if (var3) {
                                var7 = var21 | 2;
                            }

                            this.startNestedScroll(var7, 0);
                            var9 = var4;
                            break;
                        case 1:
                            this.mVelocityTracker.addMovement(var5);
                            var9 = true;
                            this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaxFlingVelocity);
                            float var15;
                            if (var2) {
                                var15 = -this.mVelocityTracker.getXVelocity(this.mScrollPointerId);
                            } else {
                                var15 = 0.0F;
                            }

                            float var16;
                            if (var3) {
                                var16 = -this.mVelocityTracker.getYVelocity(this.mScrollPointerId);
                            } else {
                                var16 = 0.0F;
                            }

                            if (var15 == 0.0F && var16 == 0.0F || !this.fling((int) var15, (int) var16)) {
                                this.setScrollState(0);
                            }

                            Log.d("SeslRecyclerView", "onTouchUp() velocity : " + var16);
                            this.resetTouch();
                            break;
                        case 2:
                            var21 = var1.findPointerIndex(this.mScrollPointerId);
                            if (var21 < 0) {
                                Log.e("SeslRecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                                var2 = false;
                                return var2;
                            }

                            int var10 = (int) (var1.getX(var21) + 0.5F);
                            int var11 = (int) (var1.getY(var21) + 0.5F);
                            var7 = this.mLastTouchX - var10;
                            var21 = this.mLastTouchY - var11;
                            if (this.dispatchNestedPreScroll(var7, var21, this.mScrollConsumed, this.mScrollOffset, 0)) {
                                var7 -= this.mScrollConsumed[0];
                                var21 -= this.mScrollConsumed[1];
                                var5.offsetLocation((float) this.mScrollOffset[0], (float) this.mScrollOffset[1]);
                                int[] var17 = this.mNestedOffsets;
                                var17[0] += this.mScrollOffset[0];
                                var17 = this.mNestedOffsets;
                                var17[1] += this.mScrollOffset[1];
                                this.adjustNestedScrollRangeBy(this.mScrollConsumed[1]);
                            } else {
                                this.adjustNestedScrollRangeBy(var21);
                            }

                            var6 = var7;
                            int var12 = var21;
                            if (this.mScrollState != 1) {
                                boolean var22 = false;
                                int var13 = var7;
                                boolean var19 = var22;
                                if (var2) {
                                    var13 = var7;
                                    var19 = var22;
                                    if (Math.abs(var7) > this.mTouchSlop) {
                                        if (var7 > 0) {
                                            var13 = var7 - this.mTouchSlop;
                                        } else {
                                            var13 = var7 + this.mTouchSlop;
                                        }

                                        var19 = true;
                                    }
                                }

                                var7 = var21;
                                boolean var14 = var19;
                                if (var3) {
                                    var7 = var21;
                                    var14 = var19;
                                    if (Math.abs(var21) > this.mTouchSlop) {
                                        if (var21 > 0) {
                                            var7 = var21 - this.mTouchSlop;
                                        } else {
                                            var7 = var21 + this.mTouchSlop;
                                        }

                                        var14 = true;
                                    }
                                }

                                var6 = var13;
                                var12 = var7;
                                if (var14) {
                                    this.setScrollState(1);
                                    var12 = var7;
                                    var6 = var13;
                                }
                            }

                            var9 = var4;
                            if (this.mScrollState == 1) {
                                this.mLastTouchX = var10 - this.mScrollOffset[0];
                                this.mLastTouchY = var11 - this.mScrollOffset[1];
                                if (!this.mGoToTopMoved) {
                                    if (var2) {
                                        var21 = var6;
                                    } else {
                                        var21 = 0;
                                    }

                                    if (var3) {
                                        var7 = var12;
                                    } else {
                                        var7 = 0;
                                    }

                                    if (this.scrollByInternal(var21, var7, var5)) {
                                        this.getParent().requestDisallowInterceptTouchEvent(true);
                                    }
                                }

                                var9 = var4;
                                if (this.mGapWorker != null) {
                                    if (var6 == 0) {
                                        var9 = var4;
                                        if (var12 == 0) {
                                            break;
                                        }
                                    }

                                    this.mGapWorker.postFromTraversal(this, var6, var12);
                                    var9 = var4;
                                }
                            }
                            break;
                        case 3:
                            this.cancelTouch();
                            var9 = var4;
                        case 4:
                            break;
                        case 5:
                            this.mScrollPointerId = var1.getPointerId(var7);
                            var21 = (int) (var1.getX(var7) + 0.5F);
                            this.mLastTouchX = var21;
                            this.mInitialTouchX = var21;
                            var21 = (int) (var1.getY(var7) + 0.5F);
                            this.mLastTouchY = var21;
                            this.mInitialTouchY = var21;
                            var9 = var4;
                            break;
                        case 6:
                            this.onPointerUp(var1);
                            var9 = var4;
                            break;
                        default:
                            var9 = var4;
                    }

                    if (!var9) {
                        this.mVelocityTracker.addMovement(var5);
                    }

                    var5.recycle();
                    var2 = true;
                }
            }
        } else {
            var2 = false;
        }

        return var2;
    }

    void postAnimationRunner() {
        if (!this.mPostedAnimatorRunner && this.mIsAttached) {
            ViewCompat.postOnAnimation(this, this.mItemAnimatorRunner);
            this.mPostedAnimatorRunner = true;
        }

    }

    void processDataSetCompletelyChanged(boolean var1) {
        this.mDispatchItemsChangedEvent |= var1;
        this.mDataSetHasChangedAfterLayout = true;
        this.markKnownViewsInvalid();
    }

    void recordAnimationInfoIfBouncedHiddenView(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2) {
        var1.setFlags(0, 8192);
        if (this.mState.mTrackOldChangeHolders && var1.isUpdated() && !var1.isRemoved() && !var1.shouldIgnore()) {
            long var3 = this.getChangedHolderKey(var1);
            this.mViewInfoStore.addToOldChangeHolders(var3, var1);
        }

        this.mViewInfoStore.addToPreLayout(var1, var2);
    }

    public void removeAndRecycleViews() {
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
        }

        if (this.mLayout != null) {
            this.mLayout.removeAndRecycleAllViews(this.mRecycler);
            this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        }

        this.mRecycler.clear();
    }

    boolean removeAnimatingView(View var1) {
        this.startInterceptRequestLayout();
        boolean var2 = this.mChildHelper.removeViewIfHidden(var1);
        if (var2) {
            RecyclerView.ViewHolder var4 = getChildViewHolderInt(var1);
            this.mRecycler.unscrapView(var4);
            this.mRecycler.recycleViewHolderInternal(var4);
        }

        boolean var3;
        if (!var2) {
            var3 = true;
        } else {
            var3 = false;
        }

        this.stopInterceptRequestLayout(var3);
        return var2;
    }

    protected void removeDetachedView(View var1, boolean var2) {
        RecyclerView.ViewHolder var3 = getChildViewHolderInt(var1);
        if (var3 != null) {
            if (var3.isTmpDetached()) {
                var3.clearTmpDetachFlag();
            } else if (!var3.shouldIgnore()) {
                throw new IllegalArgumentException("Called removeDetachedView with a view which is not flagged as tmp detached." + var3 + this.exceptionLabel());
            }
        }

        var1.clearAnimation();
        this.dispatchChildDetached(var1);
        super.removeDetachedView(var1, var2);
    }

    public void removeItemDecoration(RecyclerView.ItemDecoration var1) {
        if (this.mLayout != null) {
            this.mLayout.assertNotInLayoutOrScroll("Cannot remove item decoration during a scroll  or layout");
        }

        this.mItemDecorations.remove(var1);
        if (this.mItemDecorations.isEmpty()) {
            boolean var2;
            if (this.getOverScrollMode() == 2) {
                var2 = true;
            } else {
                var2 = false;
            }

            this.setWillNotDraw(var2);
        }

        this.markItemDecorInsetsDirty();
        this.requestLayout();
    }

    public void removeItemDecorationAt(int var1) {
        int var2 = this.getItemDecorationCount();
        if (var1 >= 0 && var1 < var2) {
            this.removeItemDecoration(this.getItemDecorationAt(var1));
        } else {
            throw new IndexOutOfBoundsException(var1 + " is an invalid index for size " + var2);
        }
    }

    public void removeOnChildAttachStateChangeListener(RecyclerView.OnChildAttachStateChangeListener var1) {
        if (this.mOnChildAttachStateListeners != null) {
            this.mOnChildAttachStateListeners.remove(var1);
        }

    }

    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener var1) {
        this.mOnItemTouchListeners.remove(var1);
        if (this.mActiveOnItemTouchListener == var1) {
            this.mActiveOnItemTouchListener = null;
        }

    }

    public void removeOnScrollListener(RecyclerView.OnScrollListener var1) {
        if (this.mScrollListeners != null) {
            this.mScrollListeners.remove(var1);
        }

    }

    void repositionShadowingViews() {
        int var1 = this.mChildHelper.getChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            View var3 = this.mChildHelper.getChildAt(var2);
            RecyclerView.ViewHolder var4 = this.getChildViewHolder(var3);
            if (var4 != null && var4.mShadowingHolder != null) {
                View var7 = var4.mShadowingHolder.itemView;
                int var5 = var3.getLeft();
                int var6 = var3.getTop();
                if (var5 != var7.getLeft() || var6 != var7.getTop()) {
                    var7.layout(var5, var6, var7.getWidth() + var5, var7.getHeight() + var6);
                }
            }
        }

    }

    public void requestChildFocus(View var1, View var2) {
        if (!this.mLayout.onRequestChildFocus(this, this.mState, var1, var2) && var2 != null) {
            this.requestChildOnScreen(var1, var2);
        }

        super.requestChildFocus(var1, var2);
    }

    public boolean requestChildRectangleOnScreen(View var1, Rect var2, boolean var3) {
        return this.mLayout.requestChildRectangleOnScreen(this, var1, var2, var3);
    }

    public void requestDisallowInterceptTouchEvent(boolean var1) {
        int var2 = this.mOnItemTouchListeners.size();

        for (int var3 = 0; var3 < var2; ++var3) {
            ((RecyclerView.OnItemTouchListener) this.mOnItemTouchListeners.get(var3)).onRequestDisallowInterceptTouchEvent(var1);
        }

        super.requestDisallowInterceptTouchEvent(var1);
    }

    public void requestLayout() {
        if (this.mInterceptRequestLayoutDepth == 0 && !this.mLayoutFrozen) {
            super.requestLayout();
        } else {
            this.mLayoutWasDefered = true;
        }

    }

    void saveOldPositions() {
        int var1 = this.mChildHelper.getUnfilteredChildCount();

        for (int var2 = 0; var2 < var1; ++var2) {
            RecyclerView.ViewHolder var3 = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(var2));
            if (!var3.shouldIgnore()) {
                var3.saveOldPosition();
            }
        }

    }

    public void scrollBy(int var1, int var2) {
        if (this.mLayout == null) {
            Log.e("SeslRecyclerView", "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (!this.mLayoutFrozen) {
            boolean var3 = this.mLayout.canScrollHorizontally();
            boolean var4 = this.mLayout.canScrollVertically();
            if (var3 || var4) {
                if (!var3) {
                    var1 = 0;
                }

                if (!var4) {
                    var2 = 0;
                }

                this.scrollByInternal(var1, var2, (MotionEvent) null);
            }
        }

    }

    boolean scrollByInternal(int var1, int var2, MotionEvent var3) {
        boolean var4 = false;
        int var5 = 0;
        byte var6 = 0;
        int var7 = 0;
        byte var8 = 0;
        int var9 = 0;
        byte var10 = 0;
        int var11 = 0;
        byte var12 = 0;
        this.consumePendingUpdateOperations();
        if (this.mAdapter != null) {
            this.startInterceptRequestLayout();
            this.onEnterLayoutOrScroll();
            TraceCompat.beginSection("RV Scroll");
            this.fillRemainingScrollValues(this.mState);
            var9 = var10;
            var5 = var6;
            if (var1 != 0) {
                var9 = this.mLayout.scrollHorizontallyBy(var1, this.mRecycler, this.mState);
                var5 = var1 - var9;
            }

            var11 = var12;
            var7 = var8;
            if (var2 != 0) {
                int var17 = this.mLayout.scrollVerticallyBy(var2, this.mRecycler, this.mState);
                int var15 = var2 - var17;
                var11 = var17;
                var7 = var15;
                if (this.mGoToTopState == 0) {
                    this.setupGoToTop(1);
                    this.autoHide(1);
                    var7 = var15;
                    var11 = var17;
                }
            }

            TraceCompat.endSection();
            this.repositionShadowingViews();
            this.onExitLayoutOrScroll();
            this.stopInterceptRequestLayout(false);
        }

        if (!this.mItemDecorations.isEmpty()) {
            this.invalidate();
        }

        boolean var18 = true;
        boolean var16 = var18;
        if (this.mIsMouseWheel) {
            var16 = var18;
            if (var7 < 0) {
                var16 = false;
            }
        }

        if (var3 != null && MotionEventCompat.isFromSource(var3, 8194)) {
            var18 = true;
        } else {
            var18 = false;
        }

        label83:
        {
            if (var16) {
                int[] var13 = this.mScrollOffset;
                if (var18) {
                    var8 = 1;
                } else {
                    var8 = 0;
                }

                if (this.dispatchNestedScroll(var9, var11, var5, var7, var13, var8)) {
                    this.mLastTouchX -= this.mScrollOffset[0];
                    this.mLastTouchY -= this.mScrollOffset[1];
                    if (var3 != null) {
                        var3.offsetLocation((float) this.mScrollOffset[0], (float) this.mScrollOffset[1]);
                    }

                    int[] var14 = this.mNestedOffsets;
                    var14[0] += this.mScrollOffset[0];
                    var14 = this.mNestedOffsets;
                    var14[1] += this.mScrollOffset[1];
                    this.mNestedScroll = true;
                    break label83;
                }
            }

            if (this.getOverScrollMode() != 2) {
                if (var3 != null && !var18) {
                    this.pullGlows(var3.getX(), (float) var5, var3.getY(), (float) var7);
                }

                this.considerReleasingGlowsOnScroll(var1, var2);
            }
        }

        if (var9 != 0 || var11 != 0) {
            this.dispatchOnScrolled(var9, var11);
        }

        if (!this.awakenScrollBars()) {
            this.invalidate();
        }

        if (this.mLayout instanceof StaggeredGridLayoutManager && (!this.canScrollVertically(-1) || !this.canScrollVertically(1))) {
            this.mLayout.onScrollStateChanged(0);
        }

        if (var9 != 0 || var11 != 0) {
            var4 = true;
        }

        return var4;
    }

    public void scrollTo(int var1, int var2) {
        Log.w("SeslRecyclerView", "SeslRecyclerView does not support scrolling to an absolute position. Use scrollToPosition instead");
    }

    public void scrollToPosition(int var1) {
        if (!this.mLayoutFrozen) {
            this.stopScroll();
            if (this.mLayout == null) {
                Log.e("SeslRecyclerView", "Cannot scroll to position a LayoutManager set. Call setLayoutManager with a non-null argument.");
            } else {
                this.mLayout.scrollToPosition(var1);
                this.awakenScrollBars();
                if (this.mFastScroller != null && this.mAdapter != null) {
                    this.mFastScroller.onScroll(this.findFirstVisibleItemPosition(), this.getChildCount(), this.mAdapter.getItemCount());
                }
            }
        }

    }

    public void sendAccessibilityEventUnchecked(AccessibilityEvent var1) {
        if (!this.shouldDeferAccessibilityEvent(var1)) {
            super.sendAccessibilityEventUnchecked(var1);
        }

    }

    public View seslFindNearChildViewUnder(float var1, float var2) {
        int var3 = this.mChildHelper.getChildCount();
        int var4 = (int) (0.5F + var1);
        int var5 = (int) (0.5F + var2);
        int var6 = var5;
        int var7 = 2147483647;
        int var8 = 0;

        int var9;
        View var10;
        int var11;
        int var12;
        int var13;
        for (var9 = var3 - 1; var9 >= 0; var8 = var13) {
            var10 = this.getChildAt(var9);
            var11 = var6;
            var12 = var7;
            var13 = var8;
            if (var10 != null) {
                var11 = (var10.getTop() + var10.getBottom()) / 2;
                if (var8 == var11) {
                    var13 = var8;
                    var12 = var7;
                    var11 = var6;
                } else {
                    var12 = Math.abs(var5 - var11);
                    if (var12 >= var7) {
                        break;
                    }

                    var13 = var11;
                }
            }

            --var9;
            var6 = var11;
            var7 = var12;
        }

        var7 = 0;
        var12 = 0;
        var8 = 0;
        var11 = 0;
        var9 = var3 - 1;

        while (true) {
            if (var9 < 0) {
                Log.e("SeslRecyclerView", "findNearChildViewUnder didn't find valid child view! " + var1 + ", " + var2);
                var10 = null;
                break;
            }

            var10 = this.getChildAt(var9);
            int var14 = var8;
            int var15 = var11;
            int var16 = var7;
            int var17 = var12;
            if (var10 != null) {
                label69:
                {
                    var15 = var10.getTop();
                    var14 = var10.getBottom();
                    var16 = var10.getLeft();
                    var17 = var10.getRight();
                    if (var9 == var3 - 1) {
                        var8 = var3 - 1;
                        var11 = var3 - 1;
                        var7 = Math.abs(var4 - var16);
                        var12 = Math.abs(var4 - var17);
                    }

                    int var18 = var8;
                    int var19 = var11;
                    var13 = var7;
                    var5 = var12;
                    if (var6 >= var15) {
                        var18 = var8;
                        var19 = var11;
                        var13 = var7;
                        var5 = var12;
                        if (var6 <= var14) {
                            var13 = Math.abs(var4 - var16);
                            var16 = Math.abs(var4 - var17);
                            var17 = var8;
                            var8 = var7;
                            if (var13 <= var7) {
                                var17 = var9;
                                var8 = var13;
                            }

                            var18 = var17;
                            var19 = var11;
                            var13 = var8;
                            var5 = var12;
                            if (var16 <= var12) {
                                var19 = var9;
                                var5 = var16;
                                var13 = var8;
                                var18 = var17;
                            }
                        }
                    }

                    if (var6 <= var14) {
                        var14 = var18;
                        var15 = var19;
                        var16 = var13;
                        var17 = var5;
                        if (var9 != 0) {
                            break label69;
                        }
                    }

                    if (var13 < var5) {
                        var10 = this.mChildHelper.getChildAt(var18);
                    } else {
                        var10 = this.mChildHelper.getChildAt(var19);
                    }
                    break;
                }
            }

            --var9;
            var8 = var14;
            var11 = var15;
            var7 = var16;
            var12 = var17;
        }

        return var10;
    }

    public int seslGetGoToTopBottomPadding() {
        return this.mGoToTopBottomPadding;
    }

    public int seslGetHoverBottomPadding() {
        return this.mHoverBottomAreaHeight;
    }

    public int seslGetHoverTopPadding() {
        return this.mHoverTopAreaHeight;
    }

    public final RecyclerView.SeslOnMultiSelectedListener seslGetOnMultiSelectedListener() {
        return this.mOnMultiSelectedListener;
    }

    public void seslHideGoToTop() {
        this.autoHide(0);
        this.mGoToTopView.setPressed(false);
    }

    public void seslInitConfigurations(Context var1) {
        ViewConfiguration var2 = ViewConfiguration.get(var1);
        Resources var3 = var1.getResources();
        this.mTouchSlop = var2.getScaledTouchSlop();
        this.mSeslTouchSlop = var2.getScaledTouchSlop();
        this.mSeslPagingTouchSlop = var2.getScaledPagingTouchSlop();
        this.mScaledHorizontalScrollFactor = ViewConfigurationCompat.getScaledHorizontalScrollFactor(var2, var1);
        this.mScaledVerticalScrollFactor = ViewConfigurationCompat.getScaledVerticalScrollFactor(var2, var1);
        this.mMinFlingVelocity = var2.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = var2.getScaledMaximumFlingVelocity();
        this.mHoverTopAreaHeight = (int) (TypedValue.applyDimension(1, 25.0F, var3.getDisplayMetrics()) + 0.5F);
        this.mHoverBottomAreaHeight = (int) (TypedValue.applyDimension(1, 25.0F, var3.getDisplayMetrics()) + 0.5F);
        this.mGoToTopBottomPadding = var3.getDimensionPixelSize(R.dimen.sesl_go_to_top_scrollable_view_gap);
        this.mGoToTopImmersiveBottomPadding = 0;
        this.mGoToTopSize = var3.getDimensionPixelSize(R.dimen.sesl_go_to_top_scrollable_view_size);
        this.mGoToTopElevation = var3.getDimension(R.dimen.sesl_go_to_top_elevation);
        this.mNavigationBarHeight = var3.getDimensionPixelSize(R.dimen.sesl_navigation_bar_height);
    }

    public boolean seslIsFastScrollerEnabled() {
        return this.mFastScrollerEnabled;
    }

    public boolean seslIsPagingTouchSlopForStylusEnabled() {
        return this.mUsePagingTouchSlopForStylus;
    }

    public void seslSetCtrlkeyPressed(boolean var1) {
        this.mIsCtrlKeyPressed = var1;
    }

    public void seslSetFastScrollerEnabled(boolean var1) {
        if (this.mFastScroller != null) {
            this.mFastScroller.setEnabled(var1);
        } else if (var1) {
            this.mFastScroller = new SeslRecyclerViewFastScroller(this, R.style.RecyclerViewFastScrollStyle);
            this.mFastScroller.setEnabled(true);
            this.mFastScroller.setScrollbarPosition(this.getVerticalScrollbarPosition());
        }

        this.mFastScrollerEnabled = var1;
        if (this.mFastScroller != null) {
            this.mFastScroller.updateLayout();
        }

    }

    public void seslSetFastScrollerEventListener(RecyclerView.SeslFastScrollerEventListener var1) {
        this.mFastScrollerEventListener = var1;
    }

    public void seslSetFillBottomColor(int var1) {
        this.mRectPaint.setColor(var1);
        if (!this.mDrawWhiteTheme) {
            this.mSeslRoundedCorner.setRoundedCornerColor(15, var1);
        }

    }

    public void seslSetFillBottomEnabled(boolean var1) {
        if (this.mLayout instanceof SeslLinearLayoutManager) {
            this.mDrawRect = var1;
            if (!this.mDrawWhiteTheme) {
                this.mRectPaint.setColor(this.getResources().getColor(R.color.sesl_round_and_bgcolor, null));
            }

            this.requestLayout();
        }

    }

    public void seslSetGoToTopBottomPadding(int var1) {
        this.mGoToTopBottomPadding = var1;
    }

    public void seslSetGoToTopEnabled(boolean var1) {
        this.seslSetGoToTopEnabled(var1, true);
    }

    public void seslSetGoToTopEnabled(boolean var1, boolean var2) {
        Drawable var3;
        if (var2) {
            var3 = this.mGoToTopImageLight;
        } else {
            var3 = this.mContext.getResources().getDrawable(mIsOneUI4 ? R.drawable.sesl4_list_go_to_top : R.drawable.sesl_list_go_to_top, null);
        }

        this.mGoToTopImage = var3;
        if (this.mGoToTopImage != null) {
            if (!var1) {
                if (this.mEnableGoToTop) {
                    this.getOverlay().remove(this.mGoToTopView);
                }
            } else {
                if (this.mGoToTopView == null) {
                    this.mGoToTopView = new ImageView(this.mContext);

                    if (VERSION.SDK_INT >= 26) {
                        var3 = this.mContext.getResources().getDrawable(R.drawable.sesl_go_to_top_background, (Resources.Theme) null);
                        this.mGoToTopView.setBackground(var3);
                        this.mGoToTopView.setElevation(this.mGoToTopElevation);
                    }

                    this.mGoToTopView.setImageDrawable(this.mGoToTopImage);
                }

                this.mGoToTopView.setAlpha(0.0F);
                if (!this.mEnableGoToTop) {
                    this.getOverlay().add(this.mGoToTopView);
                }
            }

            this.mEnableGoToTop = var1;
            this.mGoToTopFadeInAnimator = ValueAnimator.ofFloat(new float[]{0.0F, 1.0F});
            this.mGoToTopFadeInAnimator.setDuration(333L);
            this.mGoToTopFadeInAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);
            this.mGoToTopFadeInAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    try {
                        RecyclerView.this.mGoToTopView.setAlpha((Float) var1.getAnimatedValue());
                    } catch (Exception var2) {
                    }

                }
            });
            this.mGoToTopFadeOutAnimator = ValueAnimator.ofFloat(new float[]{1.0F, 0.0F});
            this.mGoToTopFadeOutAnimator.setDuration(333L);
            this.mGoToTopFadeOutAnimator.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);
            this.mGoToTopFadeOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator var1) {
                    try {
                        RecyclerView.this.mGoToTopView.setAlpha((Float) var1.getAnimatedValue());
                    } catch (Exception var2) {
                    }

                }
            });
            this.mGoToTopFadeOutAnimator.addListener(new AnimatorListener() {
                public void onAnimationCancel(Animator var1) {
                }

                public void onAnimationEnd(Animator var1) {
                    try {
                        RecyclerView.this.mShowFadeOutGTP = 2;
                        RecyclerView.this.setupGoToTop(0);
                    } catch (Exception var2) {
                    }

                }

                public void onAnimationRepeat(Animator var1) {
                }

                public void onAnimationStart(Animator var1) {
                    try {
                        RecyclerView.this.mShowFadeOutGTP = 1;
                    } catch (Exception var2) {
                    }

                }
            });
        }

    }

    public void seslSetHoverBottomPadding(int var1) {
        this.mHoverBottomAreaHeight = var1;
    }

    public void seslSetHoverScrollEnabled(boolean var1) {
        this.mHoverScrollEnable = var1;
        this.mHoverScrollStateChanged = true;
    }

    public void seslSetHoverTopPadding(int var1) {
        this.mHoverTopAreaHeight = var1;
    }

    public void seslSetImmersiveScrollBottomPadding(int var1) {
        if (var1 >= 0) {
            if (this.mEnableGoToTop) {
                int var2 = this.getHeight() - this.mGoToTopSize - this.mGoToTopBottomPadding - var1;
                if (var2 < 0) {
                    this.mGoToTopImmersiveBottomPadding = 0;
                    StringBuilder var9 = new StringBuilder();
                    var9.append("The Immersive padding value (");
                    var9.append(var1);
                    var9.append(") was too large to draw GoToTop.");
                    Log.e("SeslRecyclerView", var9.toString());
                    return;
                }

                this.mGoToTopImmersiveBottomPadding = var1;
                int var4 = this.getWidth();
                int var5 = this.getPaddingLeft();
                int var6 = this.getPaddingRight();
                var6 = this.getPaddingLeft() + (var4 - var5 - var6) / 2;
                Rect var3 = this.mGoToTopRect;
                var4 = this.mGoToTopSize;
                var3.set(var6 - var4 / 2, var2, var6 + var4 / 2, var4 + var2);
                ImageView var7 = this.mGoToTopView;
                var3 = this.mGoToTopRect;
                var7.layout(var3.left, var3.top, var3.right, var3.bottom);
            }

            SeslRecyclerViewFastScroller var8 = this.mFastScroller;
            if (var8 != null && this.mAdapter != null) {
                var8.setImmersiveBottomPadding(var1);
                this.mFastScroller.onScroll(this.findFirstVisibleItemPosition(), this.getChildCount(), this.mAdapter.getItemCount());
            }
        }

    }

    public void seslSetLastRoundedCorner(boolean z) {
        this.mDrawLastRoundedCorner = z;
    }

    public void seslSetLongPressMultiSelectionListener(RecyclerView.SeslLongPressMultiSelectionListener var1) {
        this.mLongPressMultiSelectionListener = var1;
    }

    public void seslSetOnGoToTopClickListener(RecyclerView.SeslOnGoToTopClickListener var1) {
        this.mSeslOnGoToTopClickListener = var1;
    }

    public void seslSetOnMultiSelectedListener(RecyclerView.SeslOnMultiSelectedListener var1) {
        this.mOnMultiSelectedListener = var1;
    }

    public void seslSetPagingTouchSlopForStylus(boolean var1) {
        this.mUsePagingTouchSlopForStylus = var1;
    }

    public void seslSetPenSelectionEnabled(boolean var1) {
        this.mIsPenSelectionEnabled = var1;
    }

    public void seslSetRegulationEnabled(boolean var1) {
        if (this.mViewFlinger != null) {
            this.mViewFlinger.mScroller.setRegulationEnabled(var1);
        }

    }

    public void seslSetSmoothScrollEnabled(boolean var1) {
        if (this.mViewFlinger != null) {
            this.mViewFlinger.mScroller.setSmoothScrollEnabled(var1);
        }

    }

    public void seslShowGoToTopEdge(float var1, float var2, int var3) {
        this.ensureTopGlow();
        this.mTopGlow.onPullCallOnRelease(var1, var2, var3);
        this.invalidate(0, 0, this.getWidth(), 500);
    }

    public void seslStartLongPressMultiSelection() {
        this.mIsLongPressMultiSelection = true;
    }

    public void setAccessibilityDelegateCompat(SeslRecyclerViewAccessibilityDelegate var1) {
        this.mAccessibilityDelegate = var1;
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityDelegate);
    }

    public void setChildDrawingOrderCallback(RecyclerView.ChildDrawingOrderCallback var1) {
        if (var1 != this.mChildDrawingOrderCallback) {
            this.mChildDrawingOrderCallback = var1;
            boolean var2;
            if (this.mChildDrawingOrderCallback != null) {
                var2 = true;
            } else {
                var2 = false;
            }

            this.setChildrenDrawingOrderEnabled(var2);
        }

    }

    boolean setChildImportantForAccessibilityInternal(RecyclerView.ViewHolder var1, int var2) {
        boolean var3;
        if (this.isComputingLayout()) {
            var1.mPendingAccessibilityState = var2;
            this.mPendingAccessibilityImportanceChange.add(var1);
            var3 = false;
        } else {
            ViewCompat.setImportantForAccessibility(var1.itemView, var2);
            var3 = true;
        }

        return var3;
    }

    public void setHasFixedSize(boolean var1) {
        this.mHasFixedSize = var1;
    }

    public void setItemViewCacheSize(int var1) {
        this.mRecycler.setViewCacheSize(var1);
    }

    @Deprecated
    public void setOnScrollListener(RecyclerView.OnScrollListener var1) {
        this.mScrollListener = var1;
    }

    public void setRecyclerListener(RecyclerView.RecyclerListener var1) {
        this.mRecyclerListener = var1;
    }

    public void setScrollingTouchSlop(int var1) {
        ViewConfiguration var2 = ViewConfiguration.get(this.getContext());
        Log.d("SeslRecyclerView", "setScrollingTouchSlop(): slopConstant[" + var1 + "]");
        this.seslSetPagingTouchSlopForStylus(false);
        switch (var1) {
            case 1:
                this.mTouchSlop = var2.getScaledPagingTouchSlop();
                break;
            default:
                Log.w("SeslRecyclerView", "setScrollingTouchSlop(): bad argument constant " + var1 + "; using default value");
            case 0:
                this.mTouchSlop = var2.getScaledTouchSlop();
        }

    }

    public void setViewCacheExtension(RecyclerView.ViewCacheExtension var1) {
        this.mRecycler.setViewCacheExtension(var1);
    }

    boolean shouldDeferAccessibilityEvent(AccessibilityEvent var1) {
        boolean var4;
        if (this.isComputingLayout()) {
            int var2 = 0;
            if (var1 != null) {
                var2 = AccessibilityEventCompat.getContentChangeTypes(var1);
            }

            int var3 = var2;
            if (var2 == 0) {
                var3 = 0;
            }

            this.mEatenAccessibilityChangeFlags |= var3;
            var4 = true;
        } else {
            var4 = false;
        }

        return var4;
    }

    public void showGoToTop() {
        if (this.mEnableGoToTop && this.canScrollUp() && this.mGoToTopState != 2) {
            this.setupGoToTop(1);
            this.autoHide(1);
        }

    }

    public void smoothScrollBy(int var1, int var2) {
        this.smoothScrollBy(var1, var2, (Interpolator) null);
    }

    public void smoothScrollBy(int var1, int var2, Interpolator var3) {
        if (this.mLayout == null) {
            Log.e("SeslRecyclerView", "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else if (!this.mLayoutFrozen) {
            if (!this.mLayout.canScrollHorizontally()) {
                var1 = 0;
            }

            if (!this.mLayout.canScrollVertically()) {
                var2 = 0;
            }

            if (var1 != 0 || var2 != 0) {
                this.mViewFlinger.smoothScrollBy(var1, var2, var3);
                this.showGoToTop();
            }
        }

    }

    public void smoothScrollToPosition(int var1) {
        if (!this.mLayoutFrozen) {
            if (this.mLayout == null) {
                Log.e("SeslRecyclerView", "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            } else {
                this.mLayout.smoothScrollToPosition(this, this.mState, var1);
            }
        }

    }

    void startInterceptRequestLayout() {
        ++this.mInterceptRequestLayoutDepth;
        if (this.mInterceptRequestLayoutDepth == 1 && !this.mLayoutFrozen) {
            this.mLayoutWasDefered = false;
        }

    }

    public boolean startNestedScroll(int var1) {
        return this.getScrollingChildHelper().startNestedScroll(var1);
    }

    public boolean startNestedScroll(int var1, int var2) {
        return this.getScrollingChildHelper().startNestedScroll(var1, var2);
    }

    void stopInterceptRequestLayout(boolean var1) {
        if (this.mInterceptRequestLayoutDepth < 1) {
            this.mInterceptRequestLayoutDepth = 1;
        }

        if (!var1 && !this.mLayoutFrozen) {
            this.mLayoutWasDefered = false;
        }

        if (this.mInterceptRequestLayoutDepth == 1) {
            if (var1 && this.mLayoutWasDefered && !this.mLayoutFrozen && this.mLayout != null && this.mAdapter != null) {
                this.dispatchLayout();
            }

            if (!this.mLayoutFrozen) {
                this.mLayoutWasDefered = false;
            }
        }

        --this.mInterceptRequestLayoutDepth;
    }

    public void stopNestedScroll() {
        this.getScrollingChildHelper().stopNestedScroll();
    }

    public void stopNestedScroll(int var1) {
        this.getScrollingChildHelper().stopNestedScroll(var1);
    }

    public void stopScroll() {
        this.setScrollState(0);
        this.stopScrollersInternal();
    }

    public void swapAdapter(RecyclerView.Adapter var1, boolean var2) {
        this.setLayoutFrozen(false);
        this.setAdapterInternal(var1, true, var2);
        this.processDataSetCompletelyChanged(true);
        this.requestLayout();
    }

    public boolean verifyDrawable(Drawable var1) {
        boolean var2;
        if (this.mGoToTopImage != var1 && !super.verifyDrawable(var1)) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    void viewRangeUpdate(int var1, int var2, Object var3) {
        int var4 = this.mChildHelper.getUnfilteredChildCount();

        for (int var5 = 0; var5 < var4; ++var5) {
            View var6 = this.mChildHelper.getUnfilteredChildAt(var5);
            RecyclerView.ViewHolder var7 = getChildViewHolderInt(var6);
            if (var7 != null && !var7.shouldIgnore() && var7.mPosition >= var1 && var7.mPosition < var1 + var2) {
                var7.addFlags(2);
                var7.addChangePayload(var3);
                ((RecyclerView.LayoutParams) var6.getLayoutParams()).mInsetsDirty = true;
            }
        }

        this.mRecycler.viewRangeUpdate(var1, var2);
    }

    public interface ChildDrawingOrderCallback {
        int onGetChildDrawingOrder(int var1, int var2);
    }

    public interface OnChildAttachStateChangeListener {
        void onChildViewAttachedToWindow(View var1);

        void onChildViewDetachedFromWindow(View var1);
    }

    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(RecyclerView var1, MotionEvent var2);

        void onRequestDisallowInterceptTouchEvent(boolean var1);

        void onTouchEvent(RecyclerView var1, MotionEvent var2);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Orientation {
    }

    public interface RecyclerListener {
        void onViewRecycled(RecyclerView.ViewHolder var1);
    }

    public interface SeslFastScrollerEventListener {
        void onPressed(float var1);

        void onReleased(float var1);
    }

    public interface SeslLongPressMultiSelectionListener {
        void onItemSelected(RecyclerView var1, View var2, int var3, long var4);

        void onLongPressMultiSelectionEnded(int var1, int var2);

        void onLongPressMultiSelectionStarted(int var1, int var2);
    }

    public interface SeslOnGoToTopClickListener {
        boolean onGoToTopClick(RecyclerView var1);
    }

    public interface SeslOnMultiSelectedListener {
        void onMultiSelectStart(int var1, int var2);

        void onMultiSelectStop(int var1, int var2);

        void onMultiSelected(RecyclerView var1, View var2, int var3, long var4);
    }

    public abstract static class Adapter<VH extends RecyclerView.ViewHolder> {
        private final RecyclerView.AdapterDataObservable mObservable = new RecyclerView.AdapterDataObservable();
        private boolean mHasStableIds = false;

        public Adapter() {
        }

        public final void bindViewHolder(VH var1, int var2) {
            var1.mPosition = var2;
            if (this.hasStableIds()) {
                var1.mItemId = this.getItemId(var2);
            }

            var1.setFlags(1, 519);
            TraceCompat.beginSection("RV OnBindView");
            this.onBindViewHolder(var1, var2, var1.getUnmodifiedPayloads());
            var1.clearPayload();
            android.view.ViewGroup.LayoutParams var3 = var1.itemView.getLayoutParams();
            if (var3 instanceof RecyclerView.LayoutParams) {
                ((RecyclerView.LayoutParams) var3).mInsetsDirty = true;
            }

            TraceCompat.endSection();
        }

        public final VH createViewHolder(ViewGroup parent, int viewType) {
            try {
                TraceCompat.beginSection(TRACE_CREATE_VIEW_TAG);
                final VH holder = onCreateViewHolder(parent, viewType);
                if (holder.itemView.getParent() != null) {
                    throw new IllegalStateException("ViewHolder views must not be attached when"
                            + " created. Ensure that you are not passing 'true' to the attachToRoot"
                            + " parameter of LayoutInflater.inflate(..., boolean attachToRoot)");
                }
                holder.mItemViewType = viewType;
                return holder;
            } finally {
                TraceCompat.endSection();
            }
        }

        public abstract int getItemCount();

        public long getItemId(int var1) {
            return -1L;
        }

        public int getItemViewType(int var1) {
            return 0;
        }

        public final boolean hasObservers() {
            return this.mObservable.hasObservers();
        }

        public final boolean hasStableIds() {
            return this.mHasStableIds;
        }

        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }

        public final void notifyItemChanged(int var1) {
            this.mObservable.notifyItemRangeChanged(var1, 1);
        }

        public final void notifyItemChanged(int var1, Object var2) {
            this.mObservable.notifyItemRangeChanged(var1, 1, var2);
        }

        public final void notifyItemInserted(int var1) {
            this.mObservable.notifyItemRangeInserted(var1, 1);
        }

        public final void notifyItemMoved(int var1, int var2) {
            this.mObservable.notifyItemMoved(var1, var2);
        }

        public final void notifyItemRangeChanged(int var1, int var2) {
            this.mObservable.notifyItemRangeChanged(var1, var2);
        }

        public final void notifyItemRangeChanged(int var1, int var2, Object var3) {
            this.mObservable.notifyItemRangeChanged(var1, var2, var3);
        }

        public final void notifyItemRangeInserted(int var1, int var2) {
            this.mObservable.notifyItemRangeInserted(var1, var2);
        }

        public final void notifyItemRangeRemoved(int var1, int var2) {
            this.mObservable.notifyItemRangeRemoved(var1, var2);
        }

        public final void notifyItemRemoved(int var1) {
            this.mObservable.notifyItemRangeRemoved(var1, 1);
        }

        public void onAttachedToRecyclerView(RecyclerView var1) {
        }

        public abstract void onBindViewHolder(VH var1, int var2);

        public void onBindViewHolder(VH var1, int var2, List<Object> var3) {
            this.onBindViewHolder(var1, var2);
        }

        public abstract VH onCreateViewHolder(ViewGroup var1, int var2);

        public void onDetachedFromRecyclerView(RecyclerView var1) {
        }

        public boolean onFailedToRecycleView(VH var1) {
            return false;
        }

        public void onViewAttachedToWindow(VH var1) {
        }

        public void onViewDetachedFromWindow(VH var1) {
        }

        public void onViewRecycled(VH var1) {
        }

        public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver var1) {
            this.mObservable.registerObserver(var1);
        }

        public void setHasStableIds(boolean var1) {
            if (this.hasObservers()) {
                throw new IllegalStateException("Cannot change whether this adapter has stable IDs while the adapter has registered observers.");
            } else {
                this.mHasStableIds = var1;
            }
        }

        public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver var1) {
            this.mObservable.unregisterObserver(var1);
        }
    }

    static class AdapterDataObservable extends Observable<RecyclerView.AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public boolean hasObservers() {
            boolean var1;
            if (!this.mObservers.isEmpty()) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public void notifyChanged() {
            for (int var1 = this.mObservers.size() - 1; var1 >= 0; --var1) {
                ((RecyclerView.AdapterDataObserver) this.mObservers.get(var1)).onChanged();
            }

        }

        public void notifyItemMoved(int var1, int var2) {
            for (int var3 = this.mObservers.size() - 1; var3 >= 0; --var3) {
                ((RecyclerView.AdapterDataObserver) this.mObservers.get(var3)).onItemRangeMoved(var1, var2, 1);
            }

        }

        public void notifyItemRangeChanged(int var1, int var2) {
            this.notifyItemRangeChanged(var1, var2, (Object) null);
        }

        public void notifyItemRangeChanged(int var1, int var2, Object var3) {
            for (int var4 = this.mObservers.size() - 1; var4 >= 0; --var4) {
                ((RecyclerView.AdapterDataObserver) this.mObservers.get(var4)).onItemRangeChanged(var1, var2, var3);
            }

        }

        public void notifyItemRangeInserted(int var1, int var2) {
            for (int var3 = this.mObservers.size() - 1; var3 >= 0; --var3) {
                ((RecyclerView.AdapterDataObserver) this.mObservers.get(var3)).onItemRangeInserted(var1, var2);
            }

        }

        public void notifyItemRangeRemoved(int var1, int var2) {
            for (int var3 = this.mObservers.size() - 1; var3 >= 0; --var3) {
                ((RecyclerView.AdapterDataObserver) this.mObservers.get(var3)).onItemRangeRemoved(var1, var2);
            }

        }
    }

    public abstract static class AdapterDataObserver {
        public AdapterDataObserver() {
        }

        public void onChanged() {
        }

        public void onItemRangeChanged(int var1, int var2) {
        }

        public void onItemRangeChanged(int var1, int var2, Object var3) {
            this.onItemRangeChanged(var1, var2);
        }

        public void onItemRangeInserted(int var1, int var2) {
        }

        public void onItemRangeMoved(int var1, int var2, int var3) {
        }

        public void onItemRangeRemoved(int var1, int var2) {
        }
    }

    public abstract static class ItemAnimator {
        public static final int FLAG_APPEARED_IN_PRE_LAYOUT = 4096;
        public static final int FLAG_CHANGED = 2;
        public static final int FLAG_INVALIDATED = 4;
        public static final int FLAG_MOVED = 2048;
        public static final int FLAG_REMOVED = 8;
        static final int ANIMATION_TYPE_DEFAULT = 1;
        static final int ANIMATION_TYPE_EXPAND_COLLAPSE = 2;
        private long mAddDuration = 120L;
        private int mAnimationType = 1;
        private long mChangeDuration = 250L;
        private long mExpandCollapseDuration = 700L;
        private ArrayList<RecyclerView.ItemAnimator.ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList();
        private RecyclerView.ViewHolder mGroupViewHolder = null;
        private View mHostView = null;
        private RecyclerView.ItemAnimator.ItemAnimatorListener mListener = null;
        private long mMoveDuration = 250L;
        private long mRemoveDuration = 120L;

        public ItemAnimator() {
        }

        static int buildAdapterChangeFlagsForAnimations(RecyclerView.ViewHolder var0) {
            int var1 = var0.mFlags & 14;
            int var2;
            if (var0.isInvalid()) {
                var2 = 4;
            } else {
                var2 = var1;
                if ((var1 & 4) == 0) {
                    int var3 = var0.getOldPosition();
                    int var4 = var0.getAdapterPosition();
                    var2 = var1;
                    if (var3 != -1) {
                        var2 = var1;
                        if (var4 != -1) {
                            var2 = var1;
                            if (var3 != var4) {
                                var2 = var1 | 2048;
                            }
                        }
                    }
                }
            }

            return var2;
        }

        public abstract boolean animateAppearance(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3);

        public abstract boolean animateChange(RecyclerView.ViewHolder var1, RecyclerView.ViewHolder var2, RecyclerView.ItemAnimator.ItemHolderInfo var3, RecyclerView.ItemAnimator.ItemHolderInfo var4);

        public abstract boolean animateDisappearance(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3);

        public abstract boolean animatePersistence(RecyclerView.ViewHolder var1, RecyclerView.ItemAnimator.ItemHolderInfo var2, RecyclerView.ItemAnimator.ItemHolderInfo var3);

        public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder var1) {
            return true;
        }

        public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder var1, List<Object> var2) {
            return this.canReuseUpdatedViewHolder(var1);
        }

        public void clearGroupViewHolderInternal() {
            this.mGroupViewHolder = null;
        }

        public final void dispatchAnimationFinished(RecyclerView.ViewHolder var1) {
            this.onAnimationFinished(var1);
            if (this.mListener != null) {
                this.mListener.onAnimationFinished(var1);
            }

        }

        public final void dispatchAnimationStarted(RecyclerView.ViewHolder var1) {
            this.onAnimationStarted(var1);
        }

        public final void dispatchAnimationsFinished() {
            int var1 = this.mFinishedListeners.size();

            for (int var2 = 0; var2 < var1; ++var2) {
                ((RecyclerView.ItemAnimator.ItemAnimatorFinishedListener) this.mFinishedListeners.get(var2)).onAnimationsFinished();
            }

            this.mFinishedListeners.clear();
        }

        public abstract void endAnimation(RecyclerView.ViewHolder var1);

        public abstract void endAnimations();

        public long getAddDuration() {
            return this.mAddDuration;
        }

        public void setAddDuration(long var1) {
            this.mAddDuration = var1;
        }

        public long getChangeDuration() {
            return this.mChangeDuration;
        }

        public void setChangeDuration(long var1) {
            this.mChangeDuration = var1;
        }

        public long getExpandCollapseDuration() {
            return this.mExpandCollapseDuration;
        }

        public RecyclerView.ViewHolder getGroupViewHolderInternal() {
            return this.mGroupViewHolder;
        }

        public void setGroupViewHolderInternal(RecyclerView.ViewHolder var1) {
            this.mGroupViewHolder = var1;
        }

        public View getHostView() {
            return this.mHostView;
        }

        public void setHostView(View var1) {
            this.mHostView = var1;
        }

        public int getItemAnimationTypeInternal() {
            return this.mAnimationType;
        }

        public void setItemAnimationTypeInternal(int var1) {
            this.mAnimationType = var1;
        }

        public long getMoveDuration() {
            return this.mMoveDuration;
        }

        public void setMoveDuration(long var1) {
            this.mMoveDuration = var1;
        }

        public long getRemoveDuration() {
            return this.mRemoveDuration;
        }

        public void setRemoveDuration(long var1) {
            this.mRemoveDuration = var1;
        }

        public abstract boolean isRunning();

        public final boolean isRunning(RecyclerView.ItemAnimator.ItemAnimatorFinishedListener var1) {
            boolean var2 = this.isRunning();
            if (var1 != null) {
                if (!var2) {
                    var1.onAnimationsFinished();
                } else {
                    this.mFinishedListeners.add(var1);
                }
            }

            return var2;
        }

        public RecyclerView.ItemAnimator.ItemHolderInfo obtainHolderInfo() {
            return new RecyclerView.ItemAnimator.ItemHolderInfo();
        }

        public void onAnimationFinished(RecyclerView.ViewHolder var1) {
        }

        public void onAnimationStarted(RecyclerView.ViewHolder var1) {
        }

        public RecyclerView.ItemAnimator.ItemHolderInfo recordPostLayoutInformation(RecyclerView.State var1, RecyclerView.ViewHolder var2) {
            return this.obtainHolderInfo().setFrom(var2);
        }

        public RecyclerView.ItemAnimator.ItemHolderInfo recordPreLayoutInformation(RecyclerView.State var1, RecyclerView.ViewHolder var2, int var3, List<Object> var4) {
            return this.obtainHolderInfo().setFrom(var2);
        }

        public abstract void runPendingAnimations();

        void setListener(RecyclerView.ItemAnimator.ItemAnimatorListener var1) {
            this.mListener = var1;
        }

        @Retention(RetentionPolicy.SOURCE)
        public @interface AdapterChanges {
        }

        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        interface ItemAnimatorListener {
            void onAnimationFinished(RecyclerView.ViewHolder var1);
        }

        public static class ItemHolderInfo {
            public int bottom;
            public int changeFlags;
            public int left;
            public int right;
            public int top;

            public ItemHolderInfo() {
            }

            public RecyclerView.ItemAnimator.ItemHolderInfo setFrom(RecyclerView.ViewHolder var1) {
                return this.setFrom(var1, 0);
            }

            public RecyclerView.ItemAnimator.ItemHolderInfo setFrom(RecyclerView.ViewHolder var1, int var2) {
                View var3 = var1.itemView;
                this.left = var3.getLeft();
                this.top = var3.getTop();
                this.right = var3.getRight();
                this.bottom = var3.getBottom();
                return this;
            }
        }
    }

    public abstract static class ItemDecoration {
        public ItemDecoration() {
        }

        @Deprecated
        public void getItemOffsets(Rect var1, int var2, RecyclerView var3) {
            var1.set(0, 0, 0, 0);
        }

        public void getItemOffsets(Rect var1, View var2, RecyclerView var3, RecyclerView.State var4) {
            this.getItemOffsets(var1, ((RecyclerView.LayoutParams) var2.getLayoutParams()).getViewLayoutPosition(), var3);
        }

        @Deprecated
        public void onDraw(Canvas var1, RecyclerView var2) {
        }

        public void onDraw(Canvas var1, RecyclerView var2, RecyclerView.State var3) {
            this.onDraw(var1, var2);
        }

        @Deprecated
        public void onDrawOver(Canvas var1, RecyclerView var2) {
        }

        public void onDrawOver(Canvas var1, RecyclerView var2, RecyclerView.State var3) {
            this.onDrawOver(var1, var2);
        }

        public void seslOnDispatchDraw(Canvas var1, RecyclerView var2, RecyclerView.State var3) {
        }
    }

    public abstract static class LayoutManager {
        public SeslViewBoundsCheck mHorizontalBoundCheck;
        public int mPrefetchMaxCountObserved;
        public boolean mPrefetchMaxObservedInInitialPrefetch;
        public RecyclerView mRecyclerView;
        public SeslViewBoundsCheck mVerticalBoundCheck;
        boolean mAutoMeasure;
        SeslChildHelper mChildHelper;
        boolean mIsAttachedToWindow;
        boolean mRequestedSimpleAnimations;
        RecyclerView.SmoothScroller mSmoothScroller;
        private int mHeight;
        private final SeslViewBoundsCheck.Callback mVerticalBoundCheckCallback = new SeslViewBoundsCheck.Callback() {
            public View getChildAt(int var1) {
                return LayoutManager.this.getChildAt(var1);
            }

            public int getChildCount() {
                return LayoutManager.this.getChildCount();
            }

            public int getChildEnd(View var1) {
                RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var1.getLayoutParams();
                return LayoutManager.this.getDecoratedBottom(var1) + var2.bottomMargin;
            }

            public int getChildStart(View var1) {
                RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var1.getLayoutParams();
                return LayoutManager.this.getDecoratedTop(var1) - var2.topMargin;
            }

            public View getParent() {
                return LayoutManager.this.mRecyclerView;
            }

            public int getParentEnd() {
                return LayoutManager.this.getHeight() - LayoutManager.this.getPaddingBottom();
            }

            public int getParentStart() {
                return LayoutManager.this.getPaddingTop();
            }
        };
        private int mHeightMode;
        private boolean mItemPrefetchEnabled;
        private boolean mMeasurementCacheEnabled;
        private int mWidth;
        private final SeslViewBoundsCheck.Callback mHorizontalBoundCheckCallback = new SeslViewBoundsCheck.Callback() {
            public View getChildAt(int var1) {
                return LayoutManager.this.getChildAt(var1);
            }

            public int getChildCount() {
                return LayoutManager.this.getChildCount();
            }

            public int getChildEnd(View var1) {
                RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var1.getLayoutParams();
                return LayoutManager.this.getDecoratedRight(var1) + var2.rightMargin;
            }

            public int getChildStart(View var1) {
                RecyclerView.LayoutParams var2 = (RecyclerView.LayoutParams) var1.getLayoutParams();
                return LayoutManager.this.getDecoratedLeft(var1) - var2.leftMargin;
            }

            public View getParent() {
                return LayoutManager.this.mRecyclerView;
            }

            public int getParentEnd() {
                return LayoutManager.this.getWidth() - LayoutManager.this.getPaddingRight();
            }

            public int getParentStart() {
                return LayoutManager.this.getPaddingLeft();
            }
        };
        private int mWidthMode;

        public LayoutManager() {
            this.mHorizontalBoundCheck = new SeslViewBoundsCheck(this.mHorizontalBoundCheckCallback);
            this.mVerticalBoundCheck = new SeslViewBoundsCheck(this.mVerticalBoundCheckCallback);
            this.mRequestedSimpleAnimations = false;
            this.mIsAttachedToWindow = false;
            this.mAutoMeasure = false;
            this.mMeasurementCacheEnabled = true;
            this.mItemPrefetchEnabled = true;
        }

        public static int chooseSize(int var0, int var1, int var2) {
            int var3 = MeasureSpec.getMode(var0);
            int var4 = MeasureSpec.getSize(var0);
            var0 = var4;
            switch (var3) {
                case -2147483648:
                    var0 = Math.min(var4, Math.max(var1, var2));
                case 1073741824:
                    break;
                default:
                    var0 = Math.max(var1, var2);
            }

            return var0;
        }

        public static int getChildMeasureSpec(int var0, int var1, int var2, int var3, boolean var4) {
            int var5 = Math.max(0, var0 - var2);
            var2 = 0;
            var0 = 0;
            if (var4) {
                if (var3 >= 0) {
                    var2 = var3;
                    var0 = 1073741824;
                } else if (var3 == -1) {
                    switch (var1) {
                        case -2147483648:
                        case 1073741824:
                            var2 = var5;
                            var0 = var1;
                            break;
                        case 0:
                            var2 = 0;
                            var0 = 0;
                    }
                } else if (var3 == -2) {
                    var2 = 0;
                    var0 = 0;
                }
            } else if (var3 >= 0) {
                var2 = var3;
                var0 = 1073741824;
            } else if (var3 == -1) {
                var2 = var5;
                var0 = var1;
            } else if (var3 == -2) {
                var2 = var5;
                if (var1 != -2147483648 && var1 != 1073741824) {
                    var0 = 0;
                } else {
                    var0 = -2147483648;
                }
            }

            return MeasureSpec.makeMeasureSpec(var2, var0);
        }

        @Deprecated
        public static int getChildMeasureSpec(int var0, int var1, int var2, boolean var3) {
            int var4 = Math.max(0, var0 - var1);
            var1 = 0;
            var0 = 0;
            if (var3) {
                if (var2 >= 0) {
                    var1 = var2;
                    var0 = 1073741824;
                } else {
                    var1 = 0;
                    var0 = 0;
                }
            } else if (var2 >= 0) {
                var1 = var2;
                var0 = 1073741824;
            } else if (var2 == -1) {
                var1 = var4;
                var0 = 1073741824;
            } else if (var2 == -2) {
                var1 = var4;
                var0 = -2147483648;
            }

            return MeasureSpec.makeMeasureSpec(var1, var0);
        }

        public static RecyclerView.LayoutManager.Properties getProperties(Context var0, AttributeSet var1, int var2, int var3) {
            RecyclerView.LayoutManager.Properties var4 = new RecyclerView.LayoutManager.Properties();
            TypedArray var5 = var0.obtainStyledAttributes(var1, R.styleable.RecyclerView, var2, var3);
            var4.orientation = var5.getInt(R.styleable.RecyclerView_android_orientation, 1);
            var4.spanCount = var5.getInt(R.styleable.RecyclerView_spanCount, 1);
            var4.reverseLayout = var5.getBoolean(R.styleable.RecyclerView_reverseLayout, false);
            var4.stackFromEnd = var5.getBoolean(R.styleable.RecyclerView_stackFromEnd, false);
            var5.recycle();
            return var4;
        }

        private static boolean isMeasurementUpToDate(int var0, int var1, int var2) {
            boolean var3 = true;
            int var4 = MeasureSpec.getMode(var1);
            var1 = MeasureSpec.getSize(var1);
            boolean var5;
            if (var2 > 0 && var0 != var2) {
                var5 = false;
            } else {
                var5 = var3;
                switch (var4) {
                    case -2147483648:
                        var5 = var3;
                        if (var1 < var0) {
                            var5 = false;
                        }
                    case 0:
                        break;
                    case 1073741824:
                        var5 = var3;
                        if (var1 != var0) {
                            var5 = false;
                        }
                        break;
                    default:
                        var5 = false;
                }
            }

            return var5;
        }

        private void addViewInt(View var1, int var2, boolean var3) {
            RecyclerView.ViewHolder var4 = RecyclerView.getChildViewHolderInt(var1);
            if (!var3 && !var4.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(var4);
            } else {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(var4);
            }

            RecyclerView.LayoutParams var5 = (RecyclerView.LayoutParams) var1.getLayoutParams();
            if (!var4.wasReturnedFromScrap() && !var4.isScrap()) {
                if (var1.getParent() == this.mRecyclerView) {
                    int var6 = this.mChildHelper.indexOfChild(var1);
                    int var7 = var2;
                    if (var2 == -1) {
                        var7 = this.mChildHelper.getChildCount();
                    }

                    if (var6 == -1) {
                        throw new IllegalStateException("Added View has SeslRecyclerView as parent but view is not a real child. Unfiltered index:" + this.mRecyclerView.indexOfChild(var1) + this.mRecyclerView.exceptionLabel());
                    }

                    if (var6 != var7) {
                        this.mRecyclerView.mLayout.moveView(var6, var7);
                    }
                } else {
                    this.mChildHelper.addView(var1, var2, false);
                    var5.mInsetsDirty = true;
                    if (this.mSmoothScroller != null && this.mSmoothScroller.isRunning()) {
                        this.mSmoothScroller.onChildAttachedToWindow(var1);
                    }
                }
            } else {
                if (var4.isScrap()) {
                    var4.unScrap();
                } else {
                    var4.clearReturnedFromScrapFlag();
                }

                this.mChildHelper.attachViewToParent(var1, var2, var1.getLayoutParams(), false);
            }

            if (var5.mPendingInvalidate) {
                var4.itemView.invalidate();
                var5.mPendingInvalidate = false;
            }

        }

        private void detachViewInternal(int var1, View var2) {
            this.mChildHelper.detachViewFromParent(var1);
        }

        private int[] getChildRectangleOnScreenScrollAmount(RecyclerView var1, View var2, Rect var3, boolean var4) {
            int var5 = this.getPaddingLeft();
            int var6 = this.getPaddingTop();
            int var7 = this.getWidth() - this.getPaddingRight();
            int var8 = this.getHeight();
            int var9 = this.getPaddingBottom();
            int var10 = var2.getLeft() + var3.left - var2.getScrollX();
            int var11 = var2.getTop() + var3.top - var2.getScrollY();
            int var12 = var10 + var3.width();
            int var13 = var3.height();
            int var14 = Math.min(0, var10 - var5);
            int var15 = Math.min(0, var11 - var6);
            int var16 = Math.max(0, var12 - var7);
            var9 = Math.max(0, var11 + var13 - (var8 - var9));
            if (this.getLayoutDirection() == 1) {
                if (var16 == 0) {
                    var16 = Math.max(var14, var12 - var7);
                }
            } else if (var14 != 0) {
                var16 = var14;
            } else {
                var16 = Math.min(var10 - var5, var16);
            }

            if (var15 == 0) {
                var15 = Math.min(var11 - var6, var9);
            }

            return new int[]{var16, var15};
        }

        private boolean isFocusedChildVisibleAfterScrolling(RecyclerView var1, int var2, int var3) {
            boolean var4 = false;
            View var5 = var1.getFocusedChild();
            boolean var6;
            if (var5 == null) {
                var6 = var4;
            } else {
                int var7 = this.getPaddingLeft();
                int var8 = this.getPaddingTop();
                int var9 = this.getWidth();
                int var10 = this.getPaddingRight();
                int var11 = this.getHeight();
                int var12 = this.getPaddingBottom();
                Rect var13 = this.mRecyclerView.mTempRect;
                this.getDecoratedBoundsWithMargins(var5, var13);
                var6 = var4;
                if (var13.left - var2 < var9 - var10) {
                    var6 = var4;
                    if (var13.right - var2 > var7) {
                        var6 = var4;
                        if (var13.top - var3 < var11 - var12) {
                            var6 = var4;
                            if (var13.bottom - var3 > var8) {
                                var6 = true;
                            }
                        }
                    }
                }
            }

            return var6;
        }

        private void onSmoothScrollerStopped(RecyclerView.SmoothScroller var1) {
            if (this.mSmoothScroller == var1) {
                this.mSmoothScroller = null;
            }

        }

        private void scrapOrRecycleView(RecyclerView.Recycler var1, int var2, View var3) {
            RecyclerView.ViewHolder var4 = RecyclerView.getChildViewHolderInt(var3);
            if (!var4.shouldIgnore()) {
                if (var4.isInvalid() && !var4.isRemoved() && !this.mRecyclerView.mAdapter.hasStableIds()) {
                    this.removeViewAt(var2);
                    var1.recycleViewHolderInternal(var4);
                } else {
                    this.detachViewAt(var2);
                    var1.scrapView(var3);
                    this.mRecyclerView.mViewInfoStore.onViewDetached(var4);
                }
            }

        }

        public void addDisappearingView(View var1) {
            this.addDisappearingView(var1, -1);
        }

        public void addDisappearingView(View var1, int var2) {
            this.addViewInt(var1, var2, true);
        }

        public void addView(View var1) {
            this.addView(var1, -1);
        }

        public void addView(View var1, int var2) {
            this.addViewInt(var1, var2, false);
        }

        public void assertInLayoutOrScroll(String var1) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.assertInLayoutOrScroll(var1);
            }

        }

        public void assertNotInLayoutOrScroll(String var1) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.assertNotInLayoutOrScroll(var1);
            }

        }

        public void attachView(View var1) {
            this.attachView(var1, -1);
        }

        public void attachView(View var1, int var2) {
            this.attachView(var1, var2, (RecyclerView.LayoutParams) var1.getLayoutParams());
        }

        public void attachView(View var1, int var2, RecyclerView.LayoutParams var3) {
            RecyclerView.ViewHolder var4 = RecyclerView.getChildViewHolderInt(var1);
            if (var4.isRemoved()) {
                this.mRecyclerView.mViewInfoStore.addToDisappearedInLayout(var4);
            } else {
                this.mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(var4);
            }

            this.mChildHelper.attachViewToParent(var1, var2, var3, var4.isRemoved());
        }

        public void calculateItemDecorationsForChild(View var1, Rect var2) {
            if (this.mRecyclerView == null) {
                var2.set(0, 0, 0, 0);
            } else {
                var2.set(this.mRecyclerView.getItemDecorInsetsForChild(var1));
            }

        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public boolean checkLayoutParams(RecyclerView.LayoutParams var1) {
            boolean var2;
            if (var1 != null) {
                var2 = true;
            } else {
                var2 = false;
            }

            return var2;
        }

        public void collectAdjacentPrefetchPositions(int var1, int var2, RecyclerView.State var3, RecyclerView.LayoutManager.LayoutPrefetchRegistry var4) {
        }

        public void collectInitialPrefetchPositions(int var1, RecyclerView.LayoutManager.LayoutPrefetchRegistry var2) {
        }

        public int computeHorizontalScrollExtent(RecyclerView.State var1) {
            return 0;
        }

        public int computeHorizontalScrollOffset(RecyclerView.State var1) {
            return 0;
        }

        public int computeHorizontalScrollRange(RecyclerView.State var1) {
            return 0;
        }

        public int computeVerticalScrollExtent(RecyclerView.State var1) {
            return 0;
        }

        public int computeVerticalScrollOffset(RecyclerView.State var1) {
            return 0;
        }

        public int computeVerticalScrollRange(RecyclerView.State var1) {
            return 0;
        }

        public void detachAndScrapAttachedViews(RecyclerView.Recycler var1) {
            for (int var2 = this.getChildCount() - 1; var2 >= 0; --var2) {
                this.scrapOrRecycleView(var1, var2, this.getChildAt(var2));
            }

        }

        public void detachAndScrapView(View var1, RecyclerView.Recycler var2) {
            this.scrapOrRecycleView(var2, this.mChildHelper.indexOfChild(var1), var1);
        }

        public void detachAndScrapViewAt(int var1, RecyclerView.Recycler var2) {
            this.scrapOrRecycleView(var2, var1, this.getChildAt(var1));
        }

        public void detachView(View var1) {
            int var2 = this.mChildHelper.indexOfChild(var1);
            if (var2 >= 0) {
                this.detachViewInternal(var2, var1);
            }

        }

        public void detachViewAt(int var1) {
            this.detachViewInternal(var1, this.getChildAt(var1));
        }

        void dispatchAttachedToWindow(RecyclerView var1) {
            this.mIsAttachedToWindow = true;
            this.onAttachedToWindow(var1);
        }

        void dispatchDetachedFromWindow(RecyclerView var1, RecyclerView.Recycler var2) {
            this.mIsAttachedToWindow = false;
            this.onDetachedFromWindow(var1, var2);
        }

        public void endAnimation(View var1) {
            if (this.mRecyclerView.mItemAnimator != null) {
                this.mRecyclerView.mItemAnimator.endAnimation(RecyclerView.getChildViewHolderInt(var1));
            }

        }

        public View findContainingItemView(View var1) {
            if (this.mRecyclerView == null) {
                var1 = null;
            } else {
                View var2 = this.mRecyclerView.findContainingItemView(var1);
                if (var2 == null) {
                    var1 = null;
                } else {
                    var1 = var2;
                    if (this.mChildHelper.isHidden(var2)) {
                        var1 = null;
                    }
                }
            }

            return var1;
        }

        public View findViewByPosition(int var1) {
            int var2 = this.getChildCount();
            int var3 = 0;

            View var6;
            while (true) {
                if (var3 >= var2) {
                    var6 = null;
                    break;
                }

                View var4 = this.getChildAt(var3);
                RecyclerView.ViewHolder var5 = RecyclerView.getChildViewHolderInt(var4);
                if (var5 != null && var5.getLayoutPosition() == var1 && !var5.shouldIgnore()) {
                    var6 = var4;
                    if (this.mRecyclerView.mState.isPreLayout()) {
                        break;
                    }

                    if (!var5.isRemoved()) {
                        var6 = var4;
                        break;
                    }
                }

                ++var3;
            }

            return var6;
        }

        public abstract RecyclerView.LayoutParams generateDefaultLayoutParams();

        public RecyclerView.LayoutParams generateLayoutParams(Context var1, AttributeSet var2) {
            return new RecyclerView.LayoutParams(var1, var2);
        }

        public RecyclerView.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams var1) {
            RecyclerView.LayoutParams var2;
            if (var1 instanceof RecyclerView.LayoutParams) {
                var2 = new RecyclerView.LayoutParams((RecyclerView.LayoutParams) var1);
            } else if (var1 instanceof MarginLayoutParams) {
                var2 = new RecyclerView.LayoutParams((MarginLayoutParams) var1);
            } else {
                var2 = new RecyclerView.LayoutParams(var1);
            }

            return var2;
        }

        public int getBaseline() {
            return -1;
        }

        public int getBottomDecorationHeight(View var1) {
            return ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets.bottom;
        }

        public View getChildAt(int var1) {
            View var2;
            if (this.mChildHelper != null) {
                var2 = this.mChildHelper.getChildAt(var1);
            } else {
                var2 = null;
            }

            return var2;
        }

        public int getChildCount() {
            int var1;
            if (this.mChildHelper != null) {
                var1 = this.mChildHelper.getChildCount();
            } else {
                var1 = 0;
            }

            return var1;
        }

        public boolean getClipToPadding() {
            boolean var1;
            if (this.mRecyclerView != null && this.mRecyclerView.mClipToPadding) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public int getColumnCountForAccessibility(RecyclerView.Recycler var1, RecyclerView.State var2) {
            byte var3 = 1;
            int var4 = var3;
            if (this.mRecyclerView != null) {
                if (this.mRecyclerView.mAdapter == null) {
                    var4 = var3;
                } else {
                    var4 = var3;
                    if (this.canScrollHorizontally()) {
                        var4 = this.mRecyclerView.mAdapter.getItemCount();
                    }
                }
            }

            return var4;
        }

        public int getDecoratedBottom(View var1) {
            return var1.getBottom() + this.getBottomDecorationHeight(var1);
        }

        public void getDecoratedBoundsWithMargins(View var1, Rect var2) {
            RecyclerView.getDecoratedBoundsWithMarginsInt(var1, var2);
        }

        public int getDecoratedLeft(View var1) {
            return var1.getLeft() - this.getLeftDecorationWidth(var1);
        }

        public int getDecoratedMeasuredHeight(View var1) {
            Rect var2 = ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets;
            return var1.getMeasuredHeight() + var2.top + var2.bottom;
        }

        public int getDecoratedMeasuredWidth(View var1) {
            Rect var2 = ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets;
            return var1.getMeasuredWidth() + var2.left + var2.right;
        }

        public int getDecoratedRight(View var1) {
            return var1.getRight() + this.getRightDecorationWidth(var1);
        }

        public int getDecoratedTop(View var1) {
            return var1.getTop() - this.getTopDecorationHeight(var1);
        }

        public View getFocusedChild() {
            View var1;
            if (this.mRecyclerView == null) {
                var1 = null;
            } else {
                View var2 = this.mRecyclerView.getFocusedChild();
                if (var2 != null) {
                    var1 = var2;
                    if (!this.mChildHelper.isHidden(var2)) {
                        return var1;
                    }
                }

                var1 = null;
            }

            return var1;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public int getHeightMode() {
            return this.mHeightMode;
        }

        public int getItemCount() {
            RecyclerView.Adapter var1;
            if (this.mRecyclerView != null) {
                var1 = this.mRecyclerView.getAdapter();
            } else {
                var1 = null;
            }

            int var2;
            if (var1 != null) {
                var2 = var1.getItemCount();
            } else {
                var2 = 0;
            }

            return var2;
        }

        public int getItemViewType(View var1) {
            return RecyclerView.getChildViewHolderInt(var1).getItemViewType();
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(this.mRecyclerView);
        }

        public int getLeftDecorationWidth(View var1) {
            return ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets.left;
        }

        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(this.mRecyclerView);
        }

        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(this.mRecyclerView);
        }

        public int getPaddingBottom() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = this.mRecyclerView.getPaddingBottom();
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPaddingEnd() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = ViewCompat.getPaddingEnd(this.mRecyclerView);
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPaddingLeft() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = this.mRecyclerView.getPaddingLeft();
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPaddingRight() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = this.mRecyclerView.getPaddingRight();
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPaddingStart() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = ViewCompat.getPaddingStart(this.mRecyclerView);
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPaddingTop() {
            int var1;
            if (this.mRecyclerView != null) {
                var1 = this.mRecyclerView.getPaddingTop();
            } else {
                var1 = 0;
            }

            return var1;
        }

        public int getPosition(View var1) {
            return ((RecyclerView.LayoutParams) var1.getLayoutParams()).getViewLayoutPosition();
        }

        public int getRightDecorationWidth(View var1) {
            return ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets.right;
        }

        public int getRowCountForAccessibility(RecyclerView.Recycler var1, RecyclerView.State var2) {
            byte var3 = 1;
            int var4 = var3;
            if (this.mRecyclerView != null) {
                if (this.mRecyclerView.mAdapter == null) {
                    var4 = var3;
                } else {
                    var4 = var3;
                    if (this.canScrollVertically()) {
                        var4 = this.mRecyclerView.mAdapter.getItemCount();
                    }
                }
            }

            return var4;
        }

        public int getSelectionModeForAccessibility(RecyclerView.Recycler var1, RecyclerView.State var2) {
            return 0;
        }

        public int getTopDecorationHeight(View var1) {
            return ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets.top;
        }

        public void getTransformedBoundingBox(View var1, boolean var2, Rect var3) {
            if (var2) {
                Rect var4 = ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets;
                var3.set(-var4.left, -var4.top, var1.getWidth() + var4.right, var1.getHeight() + var4.bottom);
            } else {
                var3.set(0, 0, var1.getWidth(), var1.getHeight());
            }

            if (this.mRecyclerView != null) {
                Matrix var6 = var1.getMatrix();
                if (var6 != null && !var6.isIdentity()) {
                    RectF var5 = this.mRecyclerView.mTempRectF;
                    var5.set(var3);
                    var6.mapRect(var5);
                    var3.set((int) Math.floor((double) var5.left), (int) Math.floor((double) var5.top), (int) Math.ceil((double) var5.right), (int) Math.ceil((double) var5.bottom));
                }
            }

            var3.offset(var1.getLeft(), var1.getTop());
        }

        public int getWidth() {
            return this.mWidth;
        }

        public int getWidthMode() {
            return this.mWidthMode;
        }

        public boolean hasFlexibleChildInBothOrientations() {
            int var1 = this.getChildCount();
            int var2 = 0;

            boolean var4;
            while (true) {
                if (var2 >= var1) {
                    var4 = false;
                    break;
                }

                android.view.ViewGroup.LayoutParams var3 = this.getChildAt(var2).getLayoutParams();
                if (var3.width < 0 && var3.height < 0) {
                    var4 = true;
                    break;
                }

                ++var2;
            }

            return var4;
        }

        public boolean hasFocus() {
            boolean var1;
            if (this.mRecyclerView != null && this.mRecyclerView.hasFocus()) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public void ignoreView(View var1) {
            if (var1.getParent() == this.mRecyclerView && this.mRecyclerView.indexOfChild(var1) != -1) {
                RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
                var2.addFlags(128);
                this.mRecyclerView.mViewInfoStore.removeViewHolder(var2);
            } else {
                throw new IllegalArgumentException("View should be fully attached to be ignored" + this.mRecyclerView.exceptionLabel());
            }
        }

        public boolean isAttachedToWindow() {
            return this.mIsAttachedToWindow;
        }

        public boolean isAutoMeasureEnabled() {
            return this.mAutoMeasure;
        }

        @Deprecated
        public void setAutoMeasureEnabled(boolean var1) {
            this.mAutoMeasure = var1;
        }

        public boolean isFocused() {
            boolean var1;
            if (this.mRecyclerView != null && this.mRecyclerView.isFocused()) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public final boolean isItemPrefetchEnabled() {
            return this.mItemPrefetchEnabled;
        }

        public final void setItemPrefetchEnabled(boolean var1) {
            if (var1 != this.mItemPrefetchEnabled) {
                this.mItemPrefetchEnabled = var1;
                this.mPrefetchMaxCountObserved = 0;
                if (this.mRecyclerView != null) {
                    this.mRecyclerView.mRecycler.updateViewCacheSize();
                }
            }

        }

        public boolean isLayoutHierarchical(RecyclerView.Recycler var1, RecyclerView.State var2) {
            return false;
        }

        public boolean isMeasurementCacheEnabled() {
            return this.mMeasurementCacheEnabled;
        }

        public void setMeasurementCacheEnabled(boolean var1) {
            this.mMeasurementCacheEnabled = var1;
        }

        public boolean isSmoothScrolling() {
            boolean var1;
            if (this.mSmoothScroller != null && this.mSmoothScroller.isRunning()) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public boolean isViewPartiallyVisible(View var1, boolean var2, boolean var3) {
            boolean var4 = true;
            if (this.mHorizontalBoundCheck.isViewWithinBoundFlags(var1, 24579) && this.mVerticalBoundCheck.isViewWithinBoundFlags(var1, 24579)) {
                var3 = true;
            } else {
                var3 = false;
            }

            if (var2) {
                var2 = var3;
            } else if (!var3) {
                var2 = var4;
            } else {
                var2 = false;
            }

            return var2;
        }

        public void layoutDecorated(View var1, int var2, int var3, int var4, int var5) {
            Rect var6 = ((RecyclerView.LayoutParams) var1.getLayoutParams()).mDecorInsets;
            var1.layout(var6.left + var2, var6.top + var3, var4 - var6.right, var5 - var6.bottom);
        }

        public void layoutDecoratedWithMargins(View var1, int var2, int var3, int var4, int var5) {
            RecyclerView.LayoutParams var6 = (RecyclerView.LayoutParams) var1.getLayoutParams();
            Rect var7 = var6.mDecorInsets;
            var1.layout(var7.left + var2 + var6.leftMargin, var7.top + var3 + var6.topMargin, var4 - var7.right - var6.rightMargin, var5 - var7.bottom - var6.bottomMargin);
        }

        public void measureChild(View var1, int var2, int var3) {
            RecyclerView.LayoutParams var4 = (RecyclerView.LayoutParams) var1.getLayoutParams();
            Rect var5 = this.mRecyclerView.getItemDecorInsetsForChild(var1);
            int var6 = var5.left;
            int var7 = var5.right;
            int var8 = var5.top;
            int var9 = var5.bottom;
            var2 = getChildMeasureSpec(this.getWidth(), this.getWidthMode(), this.getPaddingLeft() + this.getPaddingRight() + var2 + var6 + var7, var4.width, this.canScrollHorizontally());
            var3 = getChildMeasureSpec(this.getHeight(), this.getHeightMode(), this.getPaddingTop() + this.getPaddingBottom() + var3 + var8 + var9, var4.height, this.canScrollVertically());
            if (this.shouldMeasureChild(var1, var2, var3, var4)) {
                var1.measure(var2, var3);
            }

        }

        public void measureChildWithMargins(View var1, int var2, int var3) {
            RecyclerView.LayoutParams var4 = (RecyclerView.LayoutParams) var1.getLayoutParams();
            Rect var5 = this.mRecyclerView.getItemDecorInsetsForChild(var1);
            int var6 = var5.left;
            int var7 = var5.right;
            int var8 = var5.top;
            int var9 = var5.bottom;
            var2 = getChildMeasureSpec(this.getWidth(), this.getWidthMode(), this.getPaddingLeft() + this.getPaddingRight() + var4.leftMargin + var4.rightMargin + var2 + var6 + var7, var4.width, this.canScrollHorizontally());
            var3 = getChildMeasureSpec(this.getHeight(), this.getHeightMode(), this.getPaddingTop() + this.getPaddingBottom() + var4.topMargin + var4.bottomMargin + var3 + var8 + var9, var4.height, this.canScrollVertically());
            if (this.shouldMeasureChild(var1, var2, var3, var4)) {
                var1.measure(var2, var3);
            }

        }

        public void moveView(int var1, int var2) {
            View var3 = this.getChildAt(var1);
            if (var3 == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:" + var1 + this.mRecyclerView.toString());
            } else {
                this.detachViewAt(var1);
                this.attachView(var3, var2);
            }
        }

        public void offsetChildrenHorizontal(int var1) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenHorizontal(var1);
            }

        }

        public void offsetChildrenVertical(int var1) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenVertical(var1);
            }

        }

        public void onAdapterChanged(RecyclerView.Adapter var1, RecyclerView.Adapter var2) {
        }

        public boolean onAddFocusables(RecyclerView var1, ArrayList<View> var2, int var3, int var4) {
            return false;
        }

        public void onAttachedToWindow(RecyclerView var1) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView var1) {
        }

        public void onDetachedFromWindow(RecyclerView var1, RecyclerView.Recycler var2) {
            this.onDetachedFromWindow(var1);
        }

        public View onFocusSearchFailed(View var1, int var2, RecyclerView.Recycler var3, RecyclerView.State var4) {
            return null;
        }

        public void onInitializeAccessibilityEvent(RecyclerView.Recycler var1, RecyclerView.State var2, AccessibilityEvent var3) {
            boolean var4 = true;
            if (this.mRecyclerView != null && var3 != null) {
                boolean var5 = var4;
                if (!this.mRecyclerView.canScrollVertically(1)) {
                    var5 = var4;
                    if (!this.mRecyclerView.canScrollVertically(-1)) {
                        var5 = var4;
                        if (!this.mRecyclerView.canScrollHorizontally(-1)) {
                            if (this.mRecyclerView.canScrollHorizontally(1)) {
                                var5 = var4;
                            } else {
                                var5 = false;
                            }
                        }
                    }
                }

                var3.setScrollable(var5);
                if (this.mRecyclerView.mAdapter != null) {
                    var3.setItemCount(this.mRecyclerView.mAdapter.getItemCount());
                }
            }

        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent var1) {
            this.onInitializeAccessibilityEvent(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, var1);
        }

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat var1) {
            this.onInitializeAccessibilityNodeInfo(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, var1);
        }

        public void onInitializeAccessibilityNodeInfo(RecyclerView.Recycler var1, RecyclerView.State var2, AccessibilityNodeInfoCompat var3) {
            if (this.mRecyclerView.canScrollVertically(-1) || this.mRecyclerView.canScrollHorizontally(-1)) {
                var3.addAction(8192);
                var3.setScrollable(true);
            }

            if (this.mRecyclerView.canScrollVertically(1) || this.mRecyclerView.canScrollHorizontally(1)) {
                var3.addAction(4096);
                var3.setScrollable(true);
            }

            var3.setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(this.getRowCountForAccessibility(var1, var2), this.getColumnCountForAccessibility(var1, var2), this.isLayoutHierarchical(var1, var2), this.getSelectionModeForAccessibility(var1, var2)));
        }

        public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler var1, RecyclerView.State var2, View var3, AccessibilityNodeInfoCompat var4) {
            int var5;
            if (this.canScrollVertically()) {
                var5 = this.getPosition(var3);
            } else {
                var5 = 0;
            }

            int var6;
            if (this.canScrollHorizontally()) {
                var6 = this.getPosition(var3);
            } else {
                var6 = 0;
            }

            var4.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(var5, 1, var6, 1, false, false));
        }

        public void onInitializeAccessibilityNodeInfoForItem(View var1, AccessibilityNodeInfoCompat var2) {
            RecyclerView.ViewHolder var3 = RecyclerView.getChildViewHolderInt(var1);
            if (var3 != null && !var3.isRemoved() && !this.mChildHelper.isHidden(var3.itemView)) {
                this.onInitializeAccessibilityNodeInfoForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, var1, var2);
            }

        }

        public View onInterceptFocusSearch(View var1, int var2) {
            return null;
        }

        public void onItemsAdded(RecyclerView var1, int var2, int var3) {
        }

        public void onItemsChanged(RecyclerView var1) {
        }

        public void onItemsMoved(RecyclerView var1, int var2, int var3, int var4) {
        }

        public void onItemsRemoved(RecyclerView var1, int var2, int var3) {
        }

        public void onItemsUpdated(RecyclerView var1, int var2, int var3) {
        }

        public void onItemsUpdated(RecyclerView var1, int var2, int var3, Object var4) {
            this.onItemsUpdated(var1, var2, var3);
        }

        public void onLayoutChildren(RecyclerView.Recycler var1, RecyclerView.State var2) {
            Log.e("SeslRecyclerView", "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public void onLayoutCompleted(RecyclerView.State var1) {
        }

        public void onMeasure(RecyclerView.Recycler var1, RecyclerView.State var2, int var3, int var4) {
            this.mRecyclerView.defaultOnMeasure(var3, var4);
        }

        public boolean onRequestChildFocus(RecyclerView var1, RecyclerView.State var2, View var3, View var4) {
            return this.onRequestChildFocus(var1, var3, var4);
        }

        @Deprecated
        public boolean onRequestChildFocus(RecyclerView var1, View var2, View var3) {
            boolean var4;
            if (!this.isSmoothScrolling() && !var1.isComputingLayout()) {
                var4 = false;
            } else {
                var4 = true;
            }

            return var4;
        }

        public void onRestoreInstanceState(Parcelable var1) {
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onScrollStateChanged(int var1) {
        }

        public boolean performAccessibilityAction(int var1, Bundle var2) {
            return this.performAccessibilityAction(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, var1, var2);
        }

        public boolean performAccessibilityAction(RecyclerView.Recycler var1, RecyclerView.State var2, int var3, Bundle var4) {
            boolean var5 = false;
            if (this.mRecyclerView != null) {
                byte var6 = 0;
                byte var7 = 0;
                int var8 = 0;
                int var9 = 0;
                switch (var3) {
                    case 4096:
                        var3 = var7;
                        if (this.mRecyclerView.canScrollVertically(1)) {
                            var3 = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
                        }

                        var8 = var3;
                        if (this.mRecyclerView.canScrollHorizontally(1)) {
                            var9 = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
                            var8 = var3;
                        }
                        break;
                    case 8192:
                        var3 = var6;
                        if (this.mRecyclerView.canScrollVertically(-1)) {
                            var3 = -(this.getHeight() - this.getPaddingTop() - this.getPaddingBottom());
                        }

                        var8 = var3;
                        if (this.mRecyclerView.canScrollHorizontally(-1)) {
                            var9 = -(this.getWidth() - this.getPaddingLeft() - this.getPaddingRight());
                            var8 = var3;
                        }
                }

                if (var8 != 0 || var9 != 0) {
                    this.mRecyclerView.scrollBy(var9, var8);
                    var5 = true;
                }
            }

            return var5;
        }

        public boolean performAccessibilityActionForItem(RecyclerView.Recycler var1, RecyclerView.State var2, View var3, int var4, Bundle var5) {
            return false;
        }

        public boolean performAccessibilityActionForItem(View var1, int var2, Bundle var3) {
            return this.performAccessibilityActionForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, var1, var2, var3);
        }

        public void postOnAnimation(Runnable var1) {
            if (this.mRecyclerView != null) {
                ViewCompat.postOnAnimation(this.mRecyclerView, var1);
            }

        }

        public void removeAllViews() {
            for (int var1 = this.getChildCount() - 1; var1 >= 0; --var1) {
                this.mChildHelper.removeViewAt(var1);
            }

        }

        public void removeAndRecycleAllViews(RecyclerView.Recycler var1) {
            for (int var2 = this.getChildCount() - 1; var2 >= 0; --var2) {
                if (!RecyclerView.getChildViewHolderInt(this.getChildAt(var2)).shouldIgnore()) {
                    this.removeAndRecycleViewAt(var2, var1);
                }
            }

        }

        void removeAndRecycleScrapInt(RecyclerView.Recycler var1) {
            int var2 = var1.getScrapCount();

            for (int var3 = var2 - 1; var3 >= 0; --var3) {
                View var4 = var1.getScrapViewAt(var3);
                RecyclerView.ViewHolder var5 = RecyclerView.getChildViewHolderInt(var4);
                if (!var5.shouldIgnore()) {
                    var5.setIsRecyclable(false);
                    if (var5.isTmpDetached()) {
                        this.mRecyclerView.removeDetachedView(var4, false);
                    }

                    if (this.mRecyclerView.mItemAnimator != null) {
                        this.mRecyclerView.mItemAnimator.endAnimation(var5);
                    }

                    var5.setIsRecyclable(true);
                    var1.quickRecycleScrapView(var4);
                }
            }

            var1.clearScrap();
            if (var2 > 0) {
                this.mRecyclerView.invalidate();
            }

        }

        public void removeAndRecycleView(View var1, RecyclerView.Recycler var2) {
            this.removeView(var1);
            var2.recycleView(var1);
        }

        public void removeAndRecycleViewAt(int var1, RecyclerView.Recycler var2) {
            View var3 = this.getChildAt(var1);
            this.removeViewAt(var1);
            var2.recycleView(var3);
        }

        public boolean removeCallbacks(Runnable var1) {
            boolean var2;
            if (this.mRecyclerView != null) {
                var2 = this.mRecyclerView.removeCallbacks(var1);
            } else {
                var2 = false;
            }

            return var2;
        }

        public void removeDetachedView(View var1) {
            this.mRecyclerView.removeDetachedView(var1, false);
        }

        public void removeView(View var1) {
            this.mChildHelper.removeView(var1);
        }

        public void removeViewAt(int var1) {
            if (this.getChildAt(var1) != null) {
                this.mChildHelper.removeViewAt(var1);
            }

        }

        public boolean requestChildRectangleOnScreen(RecyclerView var1, View var2, Rect var3, boolean var4) {
            return this.requestChildRectangleOnScreen(var1, var2, var3, var4, false);
        }

        public boolean requestChildRectangleOnScreen(RecyclerView var1, View var2, Rect var3, boolean var4, boolean var5) {
            boolean var6 = false;
            int[] var9 = this.getChildRectangleOnScreenScrollAmount(var1, var2, var3, var4);
            int var7 = var9[0];
            int var8 = var9[1];
            if (var5) {
                var5 = var6;
                if (!this.isFocusedChildVisibleAfterScrolling(var1, var7, var8)) {
                    return var5;
                }
            }

            if (var7 == 0) {
                var5 = var6;
                if (var8 == 0) {
                    return var5;
                }
            }

            if (var4) {
                var1.scrollBy(var7, var8);
            } else {
                var1.smoothScrollBy(var7, var8);
            }

            var5 = true;
            return var5;
        }

        public void requestLayout() {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.requestLayout();
            }

        }

        public void requestSimpleAnimationsInNextLayout() {
            this.mRequestedSimpleAnimations = true;
        }

        public int scrollHorizontallyBy(int var1, RecyclerView.Recycler var2, RecyclerView.State var3) {
            return 0;
        }

        public void scrollToPosition(int var1) {
        }

        public int scrollVerticallyBy(int var1, RecyclerView.Recycler var2, RecyclerView.State var3) {
            return 0;
        }

        @SuppressLint("WrongConstant")
        void setExactMeasureSpecsFrom(RecyclerView var1) {
            this.setMeasureSpecs(MeasureSpec.makeMeasureSpec(var1.getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(var1.getHeight(), 1073741824));
        }

        void setMeasureSpecs(int var1, int var2) {
            this.mWidth = MeasureSpec.getSize(var1);
            this.mWidthMode = MeasureSpec.getMode(var1);
            if (this.mWidthMode == 0 && !RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                this.mWidth = 0;
            }

            this.mHeight = MeasureSpec.getSize(var2);
            this.mHeightMode = MeasureSpec.getMode(var2);
            if (this.mHeightMode == 0 && !RecyclerView.ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                this.mHeight = 0;
            }

        }

        public void setMeasuredDimension(int var1, int var2) {
            this.mRecyclerView.setMeasuredDimension(var1, var2);
        }

        public void setMeasuredDimension(Rect var1, int var2, int var3) {
            int var4 = var1.width();
            int var5 = this.getPaddingLeft();
            int var6 = this.getPaddingRight();
            int var7 = var1.height();
            int var8 = this.getPaddingTop();
            int var9 = this.getPaddingBottom();
            this.setMeasuredDimension(chooseSize(var2, var4 + var5 + var6, this.getMinimumWidth()), chooseSize(var3, var7 + var8 + var9, this.getMinimumHeight()));
        }

        void setMeasuredDimensionFromChildren(int var1, int var2) {
            int var3 = this.getChildCount();
            if (var3 == 0) {
                this.mRecyclerView.defaultOnMeasure(var1, var2);
            } else {
                int var4 = 2147483647;
                int var5 = 2147483647;
                int var6 = -2147483648;
                int var7 = -2147483648;

                int var12;
                for (int var8 = 0; var8 < var3; var5 = var12) {
                    View var9 = this.getChildAt(var8);
                    Rect var10 = this.mRecyclerView.mTempRect;
                    this.getDecoratedBoundsWithMargins(var9, var10);
                    int var11 = var4;
                    if (var10.left < var4) {
                        var11 = var10.left;
                    }

                    var4 = var6;
                    if (var10.right > var6) {
                        var4 = var10.right;
                    }

                    var12 = var5;
                    if (var10.top < var5) {
                        var12 = var10.top;
                    }

                    var5 = var7;
                    if (var10.bottom > var7) {
                        var5 = var10.bottom;
                    }

                    ++var8;
                    var6 = var4;
                    var7 = var5;
                    var4 = var11;
                }

                this.mRecyclerView.mTempRect.set(var4, var5, var6, var7);
                this.setMeasuredDimension(this.mRecyclerView.mTempRect, var1, var2);
            }

        }

        void setRecyclerView(RecyclerView var1) {
            if (var1 == null) {
                this.mRecyclerView = null;
                this.mChildHelper = null;
                this.mWidth = 0;
                this.mHeight = 0;
            } else {
                this.mRecyclerView = var1;
                this.mChildHelper = var1.mChildHelper;
                this.mWidth = var1.getWidth();
                this.mHeight = var1.getHeight();
            }

            this.mWidthMode = 1073741824;
            this.mHeightMode = 1073741824;
        }

        public boolean shouldMeasureChild(View var1, int var2, int var3, RecyclerView.LayoutParams var4) {
            boolean var5;
            if (!var1.isLayoutRequested() && this.mMeasurementCacheEnabled && isMeasurementUpToDate(var1.getWidth(), var2, var4.width) && isMeasurementUpToDate(var1.getHeight(), var3, var4.height)) {
                var5 = false;
            } else {
                var5 = true;
            }

            return var5;
        }

        public boolean shouldMeasureTwice() {
            return false;
        }

        public boolean shouldReMeasureChild(View var1, int var2, int var3, RecyclerView.LayoutParams var4) {
            boolean var5;
            if (this.mMeasurementCacheEnabled && isMeasurementUpToDate(var1.getMeasuredWidth(), var2, var4.width) && isMeasurementUpToDate(var1.getMeasuredHeight(), var3, var4.height)) {
                var5 = false;
            } else {
                var5 = true;
            }

            return var5;
        }

        public void smoothScrollToPosition(RecyclerView var1, RecyclerView.State var2, int var3) {
            Log.e("SeslRecyclerView", "You must override smoothScrollToPosition to support smooth scrolling");
        }

        public void startSmoothScroll(RecyclerView.SmoothScroller var1) {
            if (this.mSmoothScroller != null && var1 != this.mSmoothScroller && this.mSmoothScroller.isRunning()) {
                this.mSmoothScroller.stop();
            }

            this.mSmoothScroller = var1;
            this.mSmoothScroller.start(this.mRecyclerView, this);
        }

        public void stopIgnoringView(View var1) {
            RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
            var2.stopIgnoring();
            var2.resetInternal();
            var2.addFlags(4);
        }

        void stopSmoothScroller() {
            if (this.mSmoothScroller != null) {
                this.mSmoothScroller.stop();
            }

        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public interface LayoutPrefetchRegistry {
            void addPosition(int var1, int var2);
        }

        public static class Properties {
            public int orientation;
            public boolean reverseLayout;
            public int spanCount;
            public boolean stackFromEnd;

            public Properties() {
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        public final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;
        boolean mPendingInvalidate = false;
        RecyclerView.ViewHolder mViewHolder;

        public LayoutParams(int var1, int var2) {
            super(var1, var2);
        }

        public LayoutParams(Context var1, AttributeSet var2) {
            super(var1, var2);
        }

        public LayoutParams(RecyclerView.LayoutParams var1) {
            super(var1);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams var1) {
            super(var1);
        }

        public LayoutParams(MarginLayoutParams var1) {
            super(var1);
        }

        public int getViewAdapterPosition() {
            return this.mViewHolder.getAdapterPosition();
        }

        public int getViewLayoutPosition() {
            return this.mViewHolder.getLayoutPosition();
        }

        @Deprecated
        public int getViewPosition() {
            return this.mViewHolder.getPosition();
        }

        public boolean isItemChanged() {
            return this.mViewHolder.isUpdated();
        }

        public boolean isItemRemoved() {
            return this.mViewHolder.isRemoved();
        }

        public boolean isViewInvalid() {
            return this.mViewHolder.isInvalid();
        }

        public boolean viewNeedsUpdate() {
            return this.mViewHolder.needsUpdate();
        }
    }

    public abstract static class OnFlingListener {
        public OnFlingListener() {
        }

        public abstract boolean onFling(int var1, int var2);
    }

    public abstract static class OnScrollListener {
        public OnScrollListener() {
        }

        public void onScrollStateChanged(RecyclerView var1, int var2) {
        }

        public void onScrolled(RecyclerView var1, int var2, int var3) {
        }
    }

    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;
        SparseArray<RecyclerView.RecycledViewPool.ScrapData> mScrap = new SparseArray();
        private int mAttachCount = 0;

        public RecycledViewPool() {
        }

        private RecyclerView.RecycledViewPool.ScrapData getScrapDataForType(int var1) {
            RecyclerView.RecycledViewPool.ScrapData var2 = (RecyclerView.RecycledViewPool.ScrapData) this.mScrap.get(var1);
            RecyclerView.RecycledViewPool.ScrapData var3 = var2;
            if (var2 == null) {
                var3 = new RecyclerView.RecycledViewPool.ScrapData();
                this.mScrap.put(var1, var3);
            }

            return var3;
        }

        void attach(RecyclerView.Adapter var1) {
            ++this.mAttachCount;
        }

        public void clear() {
            for (int var1 = 0; var1 < this.mScrap.size(); ++var1) {
                ((RecyclerView.RecycledViewPool.ScrapData) this.mScrap.valueAt(var1)).mScrapHeap.clear();
            }

        }

        void detach() {
            --this.mAttachCount;
        }

        void factorInBindTime(int var1, long var2) {
            RecyclerView.RecycledViewPool.ScrapData var4 = this.getScrapDataForType(var1);
            var4.mBindRunningAverageNs = this.runningAverage(var4.mBindRunningAverageNs, var2);
        }

        void factorInCreateTime(int var1, long var2) {
            RecyclerView.RecycledViewPool.ScrapData var4 = this.getScrapDataForType(var1);
            var4.mCreateRunningAverageNs = this.runningAverage(var4.mCreateRunningAverageNs, var2);
        }

        public RecyclerView.ViewHolder getRecycledView(int var1) {
            RecyclerView.RecycledViewPool.ScrapData var2 = (RecyclerView.RecycledViewPool.ScrapData) this.mScrap.get(var1);
            RecyclerView.ViewHolder var3;
            if (var2 != null && !var2.mScrapHeap.isEmpty()) {
                ArrayList var4 = var2.mScrapHeap;
                var3 = (RecyclerView.ViewHolder) var4.remove(var4.size() - 1);
            } else {
                var3 = null;
            }

            return var3;
        }

        public int getRecycledViewCount(int var1) {
            return this.getScrapDataForType(var1).mScrapHeap.size();
        }

        void onAdapterChanged(RecyclerView.Adapter var1, RecyclerView.Adapter var2, boolean var3) {
            if (var1 != null) {
                this.detach();
            }

            if (!var3 && this.mAttachCount == 0) {
                this.clear();
            }

            if (var2 != null) {
                this.attach(var2);
            }

        }

        public void putRecycledView(RecyclerView.ViewHolder var1) {
            int var2 = var1.getItemViewType();
            ArrayList var3 = this.getScrapDataForType(var2).mScrapHeap;
            if (((RecyclerView.RecycledViewPool.ScrapData) this.mScrap.get(var2)).mMaxScrap > var3.size()) {
                var1.resetInternal();
                var3.add(var1);
            }

        }

        long runningAverage(long var1, long var3) {
            if (var1 != 0L) {
                var3 = var1 / 4L * 3L + var3 / 4L;
            }

            return var3;
        }

        public void setMaxRecycledViews(int var1, int var2) {
            RecyclerView.RecycledViewPool.ScrapData var3 = this.getScrapDataForType(var1);
            var3.mMaxScrap = var2;
            ArrayList var4 = var3.mScrapHeap;

            while (var4.size() > var2) {
                var4.remove(var4.size() - 1);
            }

        }

        int size() {
            int var1 = 0;

            int var4;
            for (int var2 = 0; var2 < this.mScrap.size(); var1 = var4) {
                ArrayList var3 = ((RecyclerView.RecycledViewPool.ScrapData) this.mScrap.valueAt(var2)).mScrapHeap;
                var4 = var1;
                if (var3 != null) {
                    var4 = var1 + var3.size();
                }

                ++var2;
            }

            return var1;
        }

        boolean willBindInTime(int var1, long var2, long var4) {
            long var6 = this.getScrapDataForType(var1).mBindRunningAverageNs;
            boolean var8;
            if (var6 != 0L && var2 + var6 >= var4) {
                var8 = false;
            } else {
                var8 = true;
            }

            return var8;
        }

        boolean willCreateInTime(int var1, long var2, long var4) {
            long var6 = this.getScrapDataForType(var1).mCreateRunningAverageNs;
            boolean var8;
            if (var6 != 0L && var2 + var6 >= var4) {
                var8 = false;
            } else {
                var8 = true;
            }

            return var8;
        }

        static class ScrapData {
            final ArrayList<RecyclerView.ViewHolder> mScrapHeap = new ArrayList();
            long mBindRunningAverageNs = 0L;
            long mCreateRunningAverageNs = 0L;
            int mMaxScrap = 5;

            ScrapData() {
            }
        }
    }

    public static class SavedState extends AbsSavedState {
        public static final Creator<RecyclerView.SavedState> CREATOR = new ClassLoaderCreator<RecyclerView.SavedState>() {
            public RecyclerView.SavedState createFromParcel(Parcel var1) {
                return new RecyclerView.SavedState(var1, (ClassLoader) null);
            }

            public RecyclerView.SavedState createFromParcel(Parcel var1, ClassLoader var2) {
                return new RecyclerView.SavedState(var1, var2);
            }

            public RecyclerView.SavedState[] newArray(int var1) {
                return new RecyclerView.SavedState[var1];
            }
        };
        Parcelable mLayoutState;

        SavedState(Parcel var1, ClassLoader var2) {
            super(var1, var2);
            if (var2 == null) {
                var2 = RecyclerView.LayoutManager.class.getClassLoader();
            }

            this.mLayoutState = var1.readParcelable(var2);
        }

        SavedState(Parcelable var1) {
            super(var1);
        }

        void copyFrom(RecyclerView.SavedState var1) {
            this.mLayoutState = var1.mLayoutState;
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            var1.writeParcelable(this.mLayoutState, 0);
        }
    }

    public static class SimpleOnItemTouchListener implements RecyclerView.OnItemTouchListener {
        public SimpleOnItemTouchListener() {
        }

        public boolean onInterceptTouchEvent(RecyclerView var1, MotionEvent var2) {
            return false;
        }

        public void onRequestDisallowInterceptTouchEvent(boolean var1) {
        }

        public void onTouchEvent(RecyclerView var1, MotionEvent var2) {
        }
    }

    public abstract static class SmoothScroller {
        private final RecyclerView.SmoothScroller.Action mRecyclingAction = new RecyclerView.SmoothScroller.Action(0, 0);
        private RecyclerView.LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private RecyclerView mRecyclerView;
        private boolean mRunning;
        private int mTargetPosition = -1;
        private View mTargetView;

        public SmoothScroller() {
        }

        private void onAnimation(int var1, int var2) {
            RecyclerView var3 = this.mRecyclerView;
            if (!this.mRunning || this.mTargetPosition == -1 || var3 == null) {
                this.stop();
            }

            this.mPendingInitialRun = false;
            if (this.mTargetView != null) {
                if (this.getChildPosition(this.mTargetView) == this.mTargetPosition) {
                    this.onTargetFound(this.mTargetView, var3.mState, this.mRecyclingAction);
                    this.mRecyclingAction.runIfNecessary(var3);
                    this.stop();
                } else {
                    Log.e("SeslRecyclerView", "Passed over target position while smooth scrolling.");
                    this.mTargetView = null;
                }
            }

            if (this.mRunning) {
                this.onSeekTargetStep(var1, var2, var3.mState, this.mRecyclingAction);
                boolean var4 = this.mRecyclingAction.hasJumpTarget();
                this.mRecyclingAction.runIfNecessary(var3);
                if (var4) {
                    if (this.mRunning) {
                        this.mPendingInitialRun = true;
                        var3.mViewFlinger.postOnAnimation();
                    } else {
                        this.stop();
                    }
                }
            }

        }

        public View findViewByPosition(int var1) {
            return this.mRecyclerView.mLayout.findViewByPosition(var1);
        }

        public int getChildCount() {
            return this.mRecyclerView.mLayout.getChildCount();
        }

        public int getChildPosition(View var1) {
            return this.mRecyclerView.getChildLayoutPosition(var1);
        }

        public RecyclerView.LayoutManager getLayoutManager() {
            return this.mLayoutManager;
        }

        public int getTargetPosition() {
            return this.mTargetPosition;
        }

        public void setTargetPosition(int var1) {
            this.mTargetPosition = var1;
        }

        @Deprecated
        public void instantScrollToPosition(int var1) {
            this.mRecyclerView.scrollToPosition(var1);
        }

        public boolean isPendingInitialRun() {
            return this.mPendingInitialRun;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        protected void normalize(PointF var1) {
            float var2 = (float) Math.sqrt((double) (var1.x * var1.x + var1.y * var1.y));
            var1.x /= var2;
            var1.y /= var2;
        }

        protected void onChildAttachedToWindow(View var1) {
            if (this.getChildPosition(var1) == this.getTargetPosition()) {
                this.mTargetView = var1;
            }

        }

        protected abstract void onSeekTargetStep(int var1, int var2, RecyclerView.State var3, RecyclerView.SmoothScroller.Action var4);

        protected abstract void onStart();

        protected abstract void onStop();

        protected abstract void onTargetFound(View var1, RecyclerView.State var2, RecyclerView.SmoothScroller.Action var3);

        void start(RecyclerView var1, RecyclerView.LayoutManager var2) {
            this.mRecyclerView = var1;
            this.mLayoutManager = var2;
            if (this.mTargetPosition == -1) {
                throw new IllegalArgumentException("Invalid target position");
            } else {
                this.mRecyclerView.mState.mTargetPosition = this.mTargetPosition;
                this.mRunning = true;
                this.mPendingInitialRun = true;
                this.mTargetView = this.findViewByPosition(this.getTargetPosition());
                this.onStart();
                this.mRecyclerView.mViewFlinger.postOnAnimation();
            }
        }

        protected final void stop() {
            if (this.mRunning) {
                this.mRunning = false;
                this.onStop();
                this.mRecyclerView.mState.mTargetPosition = -1;
                this.mTargetView = null;
                this.mTargetPosition = -1;
                this.mPendingInitialRun = false;
                this.mLayoutManager.onSmoothScrollerStopped(this);
                this.mLayoutManager = null;
                this.mRecyclerView = null;
            }

        }

        public interface ScrollVectorProvider {
            PointF computeScrollVectorForPosition(int var1);
        }

        public static class Action {
            public static final int UNDEFINED_DURATION = -2147483648;
            private boolean mChanged;
            private int mConsecutiveUpdates;
            private int mDuration;
            private int mDx;
            private int mDy;
            private Interpolator mInterpolator;
            private int mJumpToPosition;

            public Action(int var1, int var2) {
                this(var1, var2, -2147483648, (Interpolator) null);
            }

            public Action(int var1, int var2, int var3) {
                this(var1, var2, var3, (Interpolator) null);
            }

            public Action(int var1, int var2, int var3, Interpolator var4) {
                this.mJumpToPosition = -1;
                this.mChanged = false;
                this.mConsecutiveUpdates = 0;
                this.mDx = var1;
                this.mDy = var2;
                this.mDuration = var3;
                this.mInterpolator = var4;
            }

            private void validate() {
                if (this.mInterpolator != null && this.mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                } else if (this.mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            public int getDuration() {
                return this.mDuration;
            }

            public void setDuration(int var1) {
                this.mChanged = true;
                this.mDuration = var1;
            }

            public int getDx() {
                return this.mDx;
            }

            public void setDx(int var1) {
                this.mChanged = true;
                this.mDx = var1;
            }

            public int getDy() {
                return this.mDy;
            }

            public void setDy(int var1) {
                this.mChanged = true;
                this.mDy = var1;
            }

            public Interpolator getInterpolator() {
                return this.mInterpolator;
            }

            public void setInterpolator(Interpolator var1) {
                this.mChanged = true;
                this.mInterpolator = var1;
            }

            boolean hasJumpTarget() {
                boolean var1;
                if (this.mJumpToPosition >= 0) {
                    var1 = true;
                } else {
                    var1 = false;
                }

                return var1;
            }

            public void jumpTo(int var1) {
                this.mJumpToPosition = var1;
            }

            void runIfNecessary(RecyclerView var1) {
                if (this.mJumpToPosition >= 0) {
                    int var2 = this.mJumpToPosition;
                    this.mJumpToPosition = -1;
                    var1.jumpToPositionForSmoothScroller(var2);
                    this.mChanged = false;
                } else if (this.mChanged) {
                    this.validate();
                    if (this.mInterpolator == null) {
                        if (this.mDuration == -2147483648) {
                            var1.mViewFlinger.smoothScrollBy(this.mDx, this.mDy);
                        } else {
                            var1.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration);
                        }
                    } else {
                        var1.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration, this.mInterpolator);
                    }

                    ++this.mConsecutiveUpdates;
                    if (this.mConsecutiveUpdates > 10) {
                        Log.e("SeslRecyclerView", "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                    }

                    this.mChanged = false;
                } else {
                    this.mConsecutiveUpdates = 0;
                }

            }

            public void update(int var1, int var2, int var3, Interpolator var4) {
                this.mDx = var1;
                this.mDy = var2;
                this.mDuration = var3;
                this.mInterpolator = var4;
                this.mChanged = true;
            }
        }
    }

    public static class State {
        static final int STEP_ANIMATIONS = 4;
        static final int STEP_LAYOUT = 2;
        static final int STEP_START = 1;
        int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        long mFocusedItemId;
        int mFocusedItemPosition;
        int mFocusedSubChildId;
        boolean mInPreLayout = false;
        boolean mIsMeasuring = false;
        int mItemCount = 0;
        int mLayoutStep = 1;
        int mPreviousLayoutItemCount = 0;
        int mRemainingScrollHorizontal;
        int mRemainingScrollVertical;
        boolean mRunPredictiveAnimations = false;
        boolean mRunSimpleAnimations = false;
        boolean mStructureChanged = false;
        boolean mTrackOldChangeHolders = false;
        private SparseArray<Object> mData;
        private int mTargetPosition = -1;

        public State() {
        }

        void assertLayoutStep(int var1) {
            if ((this.mLayoutStep & var1) == 0) {
                throw new IllegalStateException("Layout state should be one of " + Integer.toBinaryString(var1) + " but it is " + Integer.toBinaryString(this.mLayoutStep));
            }
        }

        public boolean didStructureChange() {
            return this.mStructureChanged;
        }

        @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
        public <T> T get(int resourceId) {
            if (mData == null) {
                return null;
            }
            return (T) mData.get(resourceId);
        }

        public int getItemCount() {
            int var1;
            if (this.mInPreLayout) {
                var1 = this.mPreviousLayoutItemCount - this.mDeletedInvisibleItemCountSincePreviousLayout;
            } else {
                var1 = this.mItemCount;
            }

            return var1;
        }

        public int getRemainingScrollHorizontal() {
            return this.mRemainingScrollHorizontal;
        }

        public int getRemainingScrollVertical() {
            return this.mRemainingScrollVertical;
        }

        public int getTargetScrollPosition() {
            return this.mTargetPosition;
        }

        public boolean hasTargetScrollPosition() {
            boolean var1;
            if (this.mTargetPosition != -1) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public boolean isMeasuring() {
            return this.mIsMeasuring;
        }

        public boolean isPreLayout() {
            return this.mInPreLayout;
        }

        public void prepareForNestedPrefetch(RecyclerView.Adapter var1) {
            this.mLayoutStep = 1;
            this.mItemCount = var1.getItemCount();
            this.mInPreLayout = false;
            this.mTrackOldChangeHolders = false;
            this.mIsMeasuring = false;
        }

        public void put(int var1, Object var2) {
            if (this.mData == null) {
                this.mData = new SparseArray();
            }

            this.mData.put(var1, var2);
        }

        public void remove(int var1) {
            if (this.mData != null) {
                this.mData.remove(var1);
            }

        }

        RecyclerView.State reset() {
            this.mTargetPosition = -1;
            if (this.mData != null) {
                this.mData.clear();
            }

            this.mItemCount = 0;
            this.mStructureChanged = false;
            this.mIsMeasuring = false;
            return this;
        }

        public String toString() {
            return "State{mTargetPosition=" + this.mTargetPosition + ", mData=" + this.mData + ", mItemCount=" + this.mItemCount + ", mIsMeasuring=" + this.mIsMeasuring + ", mPreviousLayoutItemCount=" + this.mPreviousLayoutItemCount + ", mDeletedInvisibleItemCountSincePreviousLayout=" + this.mDeletedInvisibleItemCountSincePreviousLayout + ", mStructureChanged=" + this.mStructureChanged + ", mInPreLayout=" + this.mInPreLayout + ", mRunSimpleAnimations=" + this.mRunSimpleAnimations + ", mRunPredictiveAnimations=" + this.mRunPredictiveAnimations + '}';
        }

        public boolean willRunPredictiveAnimations() {
            return this.mRunPredictiveAnimations;
        }

        public boolean willRunSimpleAnimations() {
            return this.mRunSimpleAnimations;
        }

        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutState {
        }
    }

    public abstract static class ViewCacheExtension {
        public ViewCacheExtension() {
        }

        public abstract View getViewForPositionAndType(RecyclerView.Recycler var1, int var2, int var3);
    }

    public abstract static class ViewHolder {
        static final int FLAG_ADAPTER_FULLUPDATE = 1024;
        static final int FLAG_ADAPTER_POSITION_UNKNOWN = 512;
        static final int FLAG_APPEARED_IN_PRE_LAYOUT = 4096;
        static final int FLAG_BOUNCED_FROM_HIDDEN_LIST = 8192;
        static final int FLAG_BOUND = 1;
        static final int FLAG_IGNORE = 128;
        static final int FLAG_INVALID = 4;
        static final int FLAG_MOVED = 2048;
        static final int FLAG_NOT_RECYCLABLE = 16;
        static final int FLAG_REMOVED = 8;
        static final int FLAG_RETURNED_FROM_SCRAP = 32;
        static final int FLAG_SET_A11Y_ITEM_DELEGATE = 16384;
        static final int FLAG_TMP_DETACHED = 256;
        static final int FLAG_UPDATE = 2;
        static final int PENDING_ACCESSIBILITY_STATE_NOT_SET = -1;
        private static final List<Object> FULLUPDATE_PAYLOADS;

        static {
            FULLUPDATE_PAYLOADS = Collections.EMPTY_LIST;
        }

        public final View itemView;
        public WeakReference<RecyclerView> mNestedRecyclerView;
        public int mPosition = -1;
        long mItemId = -1L;
        int mItemViewType = -1;
        int mOldPosition = -1;
        RecyclerView mOwnerRecyclerView;
        List<Object> mPayloads = null;
        int mPendingAccessibilityState = -1;
        int mPreLayoutPosition = -1;
        RecyclerView.ViewHolder mShadowedHolder = null;
        RecyclerView.ViewHolder mShadowingHolder = null;
        List<Object> mUnmodifiedPayloads = null;
        private int mFlags;
        private boolean mInChangeScrap = false;
        private int mIsRecyclableCount = 0;
        private RecyclerView.Recycler mScrapContainer = null;
        private int mWasImportantForAccessibilityBeforeHidden = 0;

        public ViewHolder(View var1) {
            if (var1 == null) {
                throw new IllegalArgumentException("itemView may not be null");
            } else {
                this.itemView = var1;
            }
        }

        private void createPayloadsIfNeeded() {
            if (this.mPayloads == null) {
                this.mPayloads = new ArrayList();
                this.mUnmodifiedPayloads = Collections.unmodifiableList(this.mPayloads);
            }

        }

        private boolean doesTransientStatePreventRecycling() {
            boolean var1;
            if ((this.mFlags & 16) == 0 && ViewCompat.hasTransientState(this.itemView)) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        private void onEnteredHiddenState(RecyclerView var1) {
            if (this.mPendingAccessibilityState != -1) {
                this.mWasImportantForAccessibilityBeforeHidden = this.mPendingAccessibilityState;
            } else {
                this.mWasImportantForAccessibilityBeforeHidden = ViewCompat.getImportantForAccessibility(this.itemView);
            }

            var1.setChildImportantForAccessibilityInternal(this, 4);
        }

        private void onLeftHiddenState(RecyclerView var1) {
            var1.setChildImportantForAccessibilityInternal(this, this.mWasImportantForAccessibilityBeforeHidden);
            this.mWasImportantForAccessibilityBeforeHidden = 0;
        }

        private boolean shouldBeKeptAsChild() {
            boolean var1;
            if ((this.mFlags & 16) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        void addChangePayload(Object var1) {
            if (var1 == null) {
                this.addFlags(1024);
            } else if ((this.mFlags & 1024) == 0) {
                this.createPayloadsIfNeeded();
                this.mPayloads.add(var1);
            }

        }

        void addFlags(int var1) {
            this.mFlags |= var1;
        }

        void clearOldPosition() {
            this.mOldPosition = -1;
            this.mPreLayoutPosition = -1;
        }

        void clearPayload() {
            if (this.mPayloads != null) {
                this.mPayloads.clear();
            }

            this.mFlags &= -1025;
        }

        void clearReturnedFromScrapFlag() {
            this.mFlags &= -33;
        }

        void clearTmpDetachFlag() {
            this.mFlags &= -257;
        }

        void flagRemovedAndOffsetPosition(int var1, int var2, boolean var3) {
            this.addFlags(8);
            this.offsetPosition(var2, var3);
            this.mPosition = var1;
        }

        public final int getAdapterPosition() {
            int var1;
            if (this.mOwnerRecyclerView == null) {
                var1 = -1;
            } else {
                var1 = this.mOwnerRecyclerView.getAdapterPositionFor(this);
            }

            return var1;
        }

        public final long getItemId() {
            return this.mItemId;
        }

        public final int getItemViewType() {
            return this.mItemViewType;
        }

        public final int getLayoutPosition() {
            int var1;
            if (this.mPreLayoutPosition == -1) {
                var1 = this.mPosition;
            } else {
                var1 = this.mPreLayoutPosition;
            }

            return var1;
        }

        public final int getOldPosition() {
            return this.mOldPosition;
        }

        @Deprecated
        public final int getPosition() {
            int var1;
            if (this.mPreLayoutPosition == -1) {
                var1 = this.mPosition;
            } else {
                var1 = this.mPreLayoutPosition;
            }

            return var1;
        }

        List<Object> getUnmodifiedPayloads() {
            List var1;
            if ((this.mFlags & 1024) == 0) {
                if (this.mPayloads != null && this.mPayloads.size() != 0) {
                    var1 = this.mUnmodifiedPayloads;
                } else {
                    var1 = FULLUPDATE_PAYLOADS;
                }
            } else {
                var1 = FULLUPDATE_PAYLOADS;
            }

            return var1;
        }

        boolean hasAnyOfTheFlags(int var1) {
            boolean var2;
            if ((this.mFlags & var1) != 0) {
                var2 = true;
            } else {
                var2 = false;
            }

            return var2;
        }

        boolean isAdapterPositionUnknown() {
            boolean var1;
            if ((this.mFlags & 512) == 0 && !this.isInvalid()) {
                var1 = false;
            } else {
                var1 = true;
            }

            return var1;
        }

        public boolean isBound() {
            boolean var1;
            if ((this.mFlags & 1) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public boolean isInvalid() {
            boolean var1;
            if ((this.mFlags & 4) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public final boolean isRecyclable() {
            boolean var1;
            if ((this.mFlags & 16) == 0 && !ViewCompat.hasTransientState(this.itemView)) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        public boolean isRemoved() {
            boolean var1;
            if ((this.mFlags & 8) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        boolean isScrap() {
            boolean var1;
            if (this.mScrapContainer != null) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        boolean isTmpDetached() {
            boolean var1;
            if ((this.mFlags & 256) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        boolean isUpdated() {
            boolean var1;
            if ((this.mFlags & 2) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        boolean needsUpdate() {
            boolean var1;
            if ((this.mFlags & 2) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        void offsetPosition(int var1, boolean var2) {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }

            if (this.mPreLayoutPosition == -1) {
                this.mPreLayoutPosition = this.mPosition;
            }

            if (var2) {
                this.mPreLayoutPosition += var1;
            }

            this.mPosition += var1;
            if (this.itemView.getLayoutParams() != null) {
                ((RecyclerView.LayoutParams) this.itemView.getLayoutParams()).mInsetsDirty = true;
            }

        }

        void resetInternal() {
            this.mFlags = 0;
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1L;
            this.mPreLayoutPosition = -1;
            this.mIsRecyclableCount = 0;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
            this.clearPayload();
            this.mWasImportantForAccessibilityBeforeHidden = 0;
            this.mPendingAccessibilityState = -1;
            RecyclerView.clearNestedRecyclerViewIfNotNested(this);
        }

        void saveOldPosition() {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }

        }

        void setFlags(int var1, int var2) {
            this.mFlags = this.mFlags & ~var2 | var1 & var2;
        }

        public final void setIsRecyclable(boolean var1) {
            int var2;
            if (var1) {
                var2 = this.mIsRecyclableCount - 1;
            } else {
                var2 = this.mIsRecyclableCount + 1;
            }

            this.mIsRecyclableCount = var2;
            if (this.mIsRecyclableCount < 0) {
                this.mIsRecyclableCount = 0;
                Log.e("View", "isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
            } else if (!var1 && this.mIsRecyclableCount == 1) {
                this.mFlags |= 16;
            } else if (var1 && this.mIsRecyclableCount == 0) {
                this.mFlags &= -17;
            }

        }

        void setScrapContainer(RecyclerView.Recycler var1, boolean var2) {
            this.mScrapContainer = var1;
            this.mInChangeScrap = var2;
        }

        public boolean shouldIgnore() {
            boolean var1;
            if ((this.mFlags & 128) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }

        void stopIgnoring() {
            this.mFlags &= -129;
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder("ViewHolder{" + Integer.toHexString(this.hashCode()) + " position=" + this.mPosition + " id=" + this.mItemId + ", oldPos=" + this.mOldPosition + ", pLpos:" + this.mPreLayoutPosition);
            if (this.isScrap()) {
                StringBuilder var2 = var1.append(" scrap ");
                String var3;
                if (this.mInChangeScrap) {
                    var3 = "[changeScrap]";
                } else {
                    var3 = "[attachedScrap]";
                }

                var2.append(var3);
            }

            if (this.isInvalid()) {
                var1.append(" invalid");
            }

            if (!this.isBound()) {
                var1.append(" unbound");
            }

            if (this.needsUpdate()) {
                var1.append(" update");
            }

            if (this.isRemoved()) {
                var1.append(" removed");
            }

            if (this.shouldIgnore()) {
                var1.append(" ignored");
            }

            if (this.isTmpDetached()) {
                var1.append(" tmpDetached");
            }

            if (!this.isRecyclable()) {
                var1.append(" not recyclable(" + this.mIsRecyclableCount + ")");
            }

            if (this.isAdapterPositionUnknown()) {
                var1.append(" undefined adapter position");
            }

            if (this.itemView.getParent() == null) {
                var1.append(" no parent");
            }

            var1.append("}");
            return var1.toString();
        }

        void unScrap() {
            this.mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            boolean var1;
            if ((this.mFlags & 32) != 0) {
                var1 = true;
            } else {
                var1 = false;
            }

            return var1;
        }
    }

    private class ItemAnimatorRestoreListener implements RecyclerView.ItemAnimator.ItemAnimatorListener {
        ItemAnimatorRestoreListener() {
        }

        public void onAnimationFinished(RecyclerView.ViewHolder var1) {
            var1.setIsRecyclable(true);
            if (var1.mShadowedHolder != null && var1.mShadowingHolder == null) {
                var1.mShadowedHolder = null;
            }

            var1.mShadowingHolder = null;
            if (!var1.shouldBeKeptAsChild() && !RecyclerView.this.removeAnimatingView(var1.itemView) && var1.isTmpDetached()) {
                RecyclerView.this.removeDetachedView(var1.itemView, false);
            }

        }
    }

    public final class Recycler {
        static final int DEFAULT_CACHE_SIZE = 2;
        final ArrayList<RecyclerView.ViewHolder> mAttachedScrap = new ArrayList();
        final ArrayList<RecyclerView.ViewHolder> mCachedViews = new ArrayList();
        private final List<RecyclerView.ViewHolder> mUnmodifiableAttachedScrap;
        ArrayList<RecyclerView.ViewHolder> mChangedScrap = null;
        RecyclerView.RecycledViewPool mRecyclerPool;
        int mViewCacheMax;
        private int mRequestedCacheMax;
        private RecyclerView.ViewCacheExtension mViewCacheExtension;

        public Recycler() {
            this.mUnmodifiableAttachedScrap = Collections.unmodifiableList(this.mAttachedScrap);
            this.mRequestedCacheMax = 2;
            this.mViewCacheMax = 2;
        }

        @SuppressLint("WrongConstant")
        private void attachAccessibilityDelegateOnBind(RecyclerView.ViewHolder var1) {
            if (RecyclerView.this.isAccessibilityEnabled()) {
                View var2 = var1.itemView;
                if (ViewCompat.getImportantForAccessibility(var2) == 0) {
                    ViewCompat.setImportantForAccessibility(var2, 1);
                }

                if (RecyclerView.this.mAccessibilityDelegate == null) {
                    RecyclerView.this.setAccessibilityDelegateCompat(new SeslRecyclerViewAccessibilityDelegate(RecyclerView.this));
                    Log.d("SeslRecyclerView", "attachAccessibilityDelegate: mAccessibilityDelegate is null, so re create");
                }

                if (RecyclerView.this.mAccessibilityDelegate != null && !ViewCompat.hasAccessibilityDelegate(var2)) {
                    var1.addFlags(16384);
                    ViewCompat.setAccessibilityDelegate(var2, RecyclerView.this.mAccessibilityDelegate.getItemDelegate());
                }
            }

        }

        private void invalidateDisplayListInt(RecyclerView.ViewHolder var1) {
            if (var1.itemView instanceof ViewGroup) {
                this.invalidateDisplayListInt((ViewGroup) var1.itemView, false);
            }

        }

        @SuppressLint("WrongConstant")
        private void invalidateDisplayListInt(ViewGroup var1, boolean var2) {
            int var3;
            for (var3 = var1.getChildCount() - 1; var3 >= 0; --var3) {
                View var4 = var1.getChildAt(var3);
                if (var4 instanceof ViewGroup) {
                    this.invalidateDisplayListInt((ViewGroup) var4, true);
                }
            }

            if (var2) {
                if (var1.getVisibility() == 4) {
                    var1.setVisibility(0);
                    var1.setVisibility(4);
                } else {
                    var3 = var1.getVisibility();
                    var1.setVisibility(4);
                    var1.setVisibility(var3);
                }
            }

        }

        private boolean tryBindViewHolderByDeadline(RecyclerView.ViewHolder var1, int var2, int var3, long var4) {
            var1.mOwnerRecyclerView = RecyclerView.this;
            int var6 = var1.getItemViewType();
            long var7 = RecyclerView.this.getNanoTime();
            boolean var9;
            if (var4 != 9223372036854775807L && !this.mRecyclerPool.willBindInTime(var6, var7, var4)) {
                var9 = false;
            } else {
                RecyclerView.this.mAdapter.bindViewHolder(var1, var2);
                var4 = RecyclerView.this.getNanoTime();
                this.mRecyclerPool.factorInBindTime(var1.getItemViewType(), var4 - var7);
                this.attachAccessibilityDelegateOnBind(var1);
                if (RecyclerView.this.mState.isPreLayout()) {
                    var1.mPreLayoutPosition = var3;
                }

                var9 = true;
            }

            return var9;
        }

        public void addViewHolderToRecycledViewPool(RecyclerView.ViewHolder var1, boolean var2) {
            RecyclerView.clearNestedRecyclerViewIfNotNested(var1);
            if (var1.hasAnyOfTheFlags(16384)) {
                var1.setFlags(0, 16384);
                ViewCompat.setAccessibilityDelegate(var1.itemView, (AccessibilityDelegateCompat) null);
            }

            if (var2) {
                this.dispatchViewRecycled(var1);
            }

            var1.mOwnerRecyclerView = null;
            this.getRecycledViewPool().putRecycledView(var1);
        }

        public void bindViewToPosition(View var1, int var2) {
            RecyclerView.ViewHolder var3 = RecyclerView.getChildViewHolderInt(var1);
            if (var3 == null) {
                throw new IllegalArgumentException("The view does not have a ViewHolder. You cannot pass arbitrary views to this method, they should be created by the Adapter" + RecyclerView.this.exceptionLabel());
            } else {
                int var4 = RecyclerView.this.mAdapterHelper.findPositionOffset(var2);
                if (var4 >= 0 && var4 < RecyclerView.this.mAdapter.getItemCount()) {
                    this.tryBindViewHolderByDeadline(var3, var4, var2, 9223372036854775807L);
                    android.view.ViewGroup.LayoutParams var6 = var3.itemView.getLayoutParams();
                    RecyclerView.LayoutParams var7;
                    if (var6 == null) {
                        var7 = (RecyclerView.LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                        var3.itemView.setLayoutParams(var7);
                    } else if (!RecyclerView.this.checkLayoutParams(var6)) {
                        var7 = (RecyclerView.LayoutParams) RecyclerView.this.generateLayoutParams(var6);
                        var3.itemView.setLayoutParams(var7);
                    } else {
                        var7 = (RecyclerView.LayoutParams) var6;
                    }

                    var7.mInsetsDirty = true;
                    var7.mViewHolder = var3;
                    boolean var5;
                    if (var3.itemView.getParent() == null) {
                        var5 = true;
                    } else {
                        var5 = false;
                    }

                    var7.mPendingInvalidate = var5;
                } else {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + var2 + "(offset:" + var4 + ").state:" + RecyclerView.this.mState.getItemCount() + RecyclerView.this.exceptionLabel());
                }
            }
        }

        public void clear() {
            this.mAttachedScrap.clear();
            this.recycleAndClearCachedViews();
        }

        void clearOldPositions() {
            int var1 = this.mCachedViews.size();

            int var2;
            for (var2 = 0; var2 < var1; ++var2) {
                ((RecyclerView.ViewHolder) this.mCachedViews.get(var2)).clearOldPosition();
            }

            var1 = this.mAttachedScrap.size();

            for (var2 = 0; var2 < var1; ++var2) {
                ((RecyclerView.ViewHolder) this.mAttachedScrap.get(var2)).clearOldPosition();
            }

            if (this.mChangedScrap != null) {
                var1 = this.mChangedScrap.size();

                for (var2 = 0; var2 < var1; ++var2) {
                    ((RecyclerView.ViewHolder) this.mChangedScrap.get(var2)).clearOldPosition();
                }
            }

        }

        void clearScrap() {
            this.mAttachedScrap.clear();
            if (this.mChangedScrap != null) {
                this.mChangedScrap.clear();
            }

        }

        public int convertPreLayoutPositionToPostLayout(int var1) {
            if (var1 >= 0 && var1 < RecyclerView.this.mState.getItemCount()) {
                if (RecyclerView.this.mState.isPreLayout()) {
                    var1 = RecyclerView.this.mAdapterHelper.findPositionOffset(var1);
                }

                return var1;
            } else {
                throw new IndexOutOfBoundsException("invalid position " + var1 + ". State item count is " + RecyclerView.this.mState.getItemCount() + RecyclerView.this.exceptionLabel());
            }
        }

        void dispatchViewRecycled(RecyclerView.ViewHolder var1) {
            if (RecyclerView.this.mRecyclerListener != null) {
                RecyclerView.this.mRecyclerListener.onViewRecycled(var1);
            }

            if (RecyclerView.this.mAdapter != null) {
                RecyclerView.this.mAdapter.onViewRecycled(var1);
            }

            if (RecyclerView.this.mState != null) {
                RecyclerView.this.mViewInfoStore.removeViewHolder(var1);
            }

        }

        RecyclerView.ViewHolder getChangedScrapViewForPosition(int var1) {
            RecyclerView.ViewHolder var3;
            if (this.mChangedScrap != null) {
                int var2 = this.mChangedScrap.size();
                if (var2 != 0) {
                    for (int var4 = 0; var4 < var2; ++var4) {
                        var3 = (RecyclerView.ViewHolder) this.mChangedScrap.get(var4);
                        if (!var3.wasReturnedFromScrap() && var3.getLayoutPosition() == var1) {
                            var3.addFlags(32);
                            return var3;
                        }
                    }

                    if (RecyclerView.this.mAdapter.hasStableIds()) {
                        var1 = RecyclerView.this.mAdapterHelper.findPositionOffset(var1);
                        if (var1 > 0 && var1 < RecyclerView.this.mAdapter.getItemCount()) {
                            long var5 = RecyclerView.this.mAdapter.getItemId(var1);

                            for (var1 = 0; var1 < var2; ++var1) {
                                var3 = (RecyclerView.ViewHolder) this.mChangedScrap.get(var1);
                                if (!var3.wasReturnedFromScrap() && var3.getItemId() == var5) {
                                    var3.addFlags(32);
                                    return var3;
                                }
                            }
                        }
                    }

                    var3 = null;
                    return var3;
                }
            }

            var3 = null;
            return var3;
        }

        RecyclerView.RecycledViewPool getRecycledViewPool() {
            if (this.mRecyclerPool == null) {
                this.mRecyclerPool = new RecyclerView.RecycledViewPool();
            }

            return this.mRecyclerPool;
        }

        void setRecycledViewPool(RecyclerView.RecycledViewPool var1) {
            if (this.mRecyclerPool != null) {
                this.mRecyclerPool.detach();
            }

            this.mRecyclerPool = var1;
            if (var1 != null) {
                this.mRecyclerPool.attach(RecyclerView.this.getAdapter());
            }

        }

        int getScrapCount() {
            return this.mAttachedScrap.size();
        }

        public List<RecyclerView.ViewHolder> getScrapList() {
            return this.mUnmodifiableAttachedScrap;
        }

        RecyclerView.ViewHolder getScrapOrCachedViewForId(long var1, int var3, boolean var4) {
            int var5 = this.mAttachedScrap.size() - 1;

            RecyclerView.ViewHolder var7;
            while (true) {
                RecyclerView.ViewHolder var6;
                if (var5 < 0) {
                    for (var5 = this.mCachedViews.size() - 1; var5 >= 0; --var5) {
                        var6 = (RecyclerView.ViewHolder) this.mCachedViews.get(var5);
                        if (var6.getItemId() == var1) {
                            if (var3 == var6.getItemViewType()) {
                                var7 = var6;
                                if (!var4) {
                                    this.mCachedViews.remove(var5);
                                    var7 = var6;
                                }

                                return var7;
                            }

                            if (!var4) {
                                this.recycleCachedViewAt(var5);
                                var7 = null;
                                return var7;
                            }
                        }
                    }

                    var7 = null;
                    break;
                }

                var6 = (RecyclerView.ViewHolder) this.mAttachedScrap.get(var5);
                if (var6.getItemId() == var1 && !var6.wasReturnedFromScrap()) {
                    if (var3 == var6.getItemViewType()) {
                        var6.addFlags(32);
                        var7 = var6;
                        if (var6.isRemoved()) {
                            var7 = var6;
                            if (!RecyclerView.this.mState.isPreLayout()) {
                                var6.setFlags(2, 14);
                                var7 = var6;
                            }
                        }
                        break;
                    }

                    if (!var4) {
                        this.mAttachedScrap.remove(var5);
                        RecyclerView.this.removeDetachedView(var6.itemView, false);
                        this.quickRecycleScrapView(var6.itemView);
                    }
                }

                --var5;
            }

            return var7;
        }

        RecyclerView.ViewHolder getScrapOrHiddenOrCachedHolderForPosition(int var1, boolean var2) {
            int var3 = this.mAttachedScrap.size();
            int var4 = 0;

            RecyclerView.ViewHolder var5;
            while (true) {
                if (var4 >= var3) {
                    if (!var2) {
                        View var6 = RecyclerView.this.mChildHelper.findHiddenNonRemovedView(var1);
                        if (var6 != null) {
                            var5 = RecyclerView.getChildViewHolderInt(var6);
                            RecyclerView.this.mChildHelper.unhide(var6);
                            var1 = RecyclerView.this.mChildHelper.indexOfChild(var6);
                            if (var1 == -1) {
                                throw new IllegalStateException("layout index should not be -1 after unhiding a view:" + var5 + RecyclerView.this.exceptionLabel());
                            }

                            RecyclerView.this.mChildHelper.detachViewFromParent(var1);
                            this.scrapView(var6);
                            var5.addFlags(8224);
                            break;
                        }
                    }

                    var3 = this.mCachedViews.size();

                    for (var4 = 0; var4 < var3; ++var4) {
                        RecyclerView.ViewHolder var7 = (RecyclerView.ViewHolder) this.mCachedViews.get(var4);
                        if (!var7.isInvalid() && var7.getLayoutPosition() == var1) {
                            var5 = var7;
                            if (!var2) {
                                this.mCachedViews.remove(var4);
                                var5 = var7;
                            }

                            return var5;
                        }
                    }

                    var5 = null;
                    break;
                }

                var5 = (RecyclerView.ViewHolder) this.mAttachedScrap.get(var4);
                if (!var5.wasReturnedFromScrap() && var5.getLayoutPosition() == var1 && !var5.isInvalid() && (RecyclerView.this.mState.mInPreLayout || !var5.isRemoved())) {
                    var5.addFlags(32);
                    break;
                }

                ++var4;
            }

            return var5;
        }

        View getScrapViewAt(int var1) {
            return ((RecyclerView.ViewHolder) this.mAttachedScrap.get(var1)).itemView;
        }

        public View getViewForPosition(int var1) {
            return this.getViewForPosition(var1, false);
        }

        View getViewForPosition(int var1, boolean var2) {
            return this.tryGetViewHolderForPositionByDeadline(var1, var2, 9223372036854775807L).itemView;
        }

        void markItemDecorInsetsDirty() {
            int var1 = this.mCachedViews.size();

            for (int var2 = 0; var2 < var1; ++var2) {
                RecyclerView.LayoutParams var3 = (RecyclerView.LayoutParams) ((RecyclerView.ViewHolder) this.mCachedViews.get(var2)).itemView.getLayoutParams();
                if (var3 != null) {
                    var3.mInsetsDirty = true;
                }
            }

        }

        void markKnownViewsInvalid() {
            int var1 = this.mCachedViews.size();

            for (int var2 = 0; var2 < var1; ++var2) {
                RecyclerView.ViewHolder var3 = (RecyclerView.ViewHolder) this.mCachedViews.get(var2);
                if (var3 != null) {
                    var3.addFlags(6);
                    var3.addChangePayload((Object) null);
                }
            }

            if (RecyclerView.this.mAdapter == null || !RecyclerView.this.mAdapter.hasStableIds()) {
                this.recycleAndClearCachedViews();
            }

        }

        void offsetPositionRecordsForInsert(int var1, int var2) {
            int var3 = this.mCachedViews.size();

            for (int var4 = 0; var4 < var3; ++var4) {
                RecyclerView.ViewHolder var5 = (RecyclerView.ViewHolder) this.mCachedViews.get(var4);
                if (var5 != null && var5.mPosition >= var1) {
                    var5.offsetPosition(var2, true);
                }
            }

        }

        void offsetPositionRecordsForMove(int var1, int var2) {
            int var3;
            int var4;
            byte var5;
            if (var1 < var2) {
                var3 = var1;
                var4 = var2;
                var5 = -1;
            } else {
                var3 = var2;
                var4 = var1;
                var5 = 1;
            }

            int var6 = this.mCachedViews.size();

            for (int var7 = 0; var7 < var6; ++var7) {
                RecyclerView.ViewHolder var8 = (RecyclerView.ViewHolder) this.mCachedViews.get(var7);
                if (var8 != null && var8.mPosition >= var3 && var8.mPosition <= var4) {
                    if (var8.mPosition == var1) {
                        var8.offsetPosition(var2 - var1, false);
                    } else {
                        var8.offsetPosition(var5, false);
                    }
                }
            }

        }

        void offsetPositionRecordsForRemove(int var1, int var2, boolean var3) {
            for (int var4 = this.mCachedViews.size() - 1; var4 >= 0; --var4) {
                RecyclerView.ViewHolder var5 = (RecyclerView.ViewHolder) this.mCachedViews.get(var4);
                if (var5 != null) {
                    if (var5.mPosition >= var1 + var2) {
                        var5.offsetPosition(-var2, var3);
                    } else if (var5.mPosition >= var1) {
                        var5.addFlags(8);
                        this.recycleCachedViewAt(var4);
                    }
                }
            }

        }

        void onAdapterChanged(RecyclerView.Adapter var1, RecyclerView.Adapter var2, boolean var3) {
            this.clear();
            this.getRecycledViewPool().onAdapterChanged(var1, var2, var3);
        }

        void quickRecycleScrapView(View var1) {
            RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
            var2.mScrapContainer = null;
            var2.mInChangeScrap = false;
            var2.clearReturnedFromScrapFlag();
            this.recycleViewHolderInternal(var2);
        }

        void recycleAndClearCachedViews() {
            for (int var1 = this.mCachedViews.size() - 1; var1 >= 0; --var1) {
                this.recycleCachedViewAt(var1);
            }

            this.mCachedViews.clear();
            if (RecyclerView.ALLOW_THREAD_GAP_WORK) {
                RecyclerView.this.mPrefetchRegistry.clearPrefetchPositions();
            }

        }

        void recycleCachedViewAt(int var1) {
            this.addViewHolderToRecycledViewPool((RecyclerView.ViewHolder) this.mCachedViews.get(var1), true);
            this.mCachedViews.remove(var1);
        }

        public void recycleView(View var1) {
            RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
            if (var2.isTmpDetached()) {
                RecyclerView.this.removeDetachedView(var1, false);
            }

            if (var2.isScrap()) {
                var2.unScrap();
            } else if (var2.wasReturnedFromScrap()) {
                var2.clearReturnedFromScrapFlag();
            }

            this.recycleViewHolderInternal(var2);
        }

        void recycleViewHolderInternal(RecyclerView.ViewHolder var1) {
            boolean var2 = false;
            if (!var1.isScrap() && var1.itemView.getParent() == null) {
                if (var1.isTmpDetached()) {
                    throw new IllegalArgumentException("Tmp detached view should be removed from SeslRecyclerView before it can be recycled: " + var1 + RecyclerView.this.exceptionLabel());
                } else if (var1.shouldIgnore()) {
                    throw new IllegalArgumentException("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle." + RecyclerView.this.exceptionLabel());
                } else {
                    var2 = var1.doesTransientStatePreventRecycling();
                    boolean var4;
                    if (RecyclerView.this.mAdapter != null && var2 && RecyclerView.this.mAdapter.onFailedToRecycleView(var1)) {
                        var4 = true;
                    } else {
                        var4 = false;
                    }

                    boolean var5;
                    boolean var8;
                    label95:
                    {
                        var5 = false;
                        boolean var6 = false;
                        boolean var7 = false;
                        if (!var4) {
                            var8 = var7;
                            if (!var1.isRecyclable()) {
                                break label95;
                            }
                        }

                        var4 = var6;
                        if (this.mViewCacheMax > 0) {
                            var4 = var6;
                            if (!var1.hasAnyOfTheFlags(526)) {
                                int var9 = this.mCachedViews.size();
                                int var10 = var9;
                                if (var9 >= this.mViewCacheMax) {
                                    var10 = var9;
                                    if (var9 > 0) {
                                        this.recycleCachedViewAt(0);
                                        var10 = var9 - 1;
                                    }
                                }

                                int var11 = var10;
                                if (RecyclerView.ALLOW_THREAD_GAP_WORK) {
                                    var11 = var10;
                                    if (var10 > 0) {
                                        var11 = var10;
                                        if (!RecyclerView.this.mPrefetchRegistry.lastPrefetchIncludedPosition(var1.mPosition)) {
                                            --var10;

                                            while (var10 >= 0) {
                                                var9 = ((RecyclerView.ViewHolder) this.mCachedViews.get(var10)).mPosition;
                                                if (!RecyclerView.this.mPrefetchRegistry.lastPrefetchIncludedPosition(var9)) {
                                                    break;
                                                }

                                                --var10;
                                            }

                                            var11 = var10 + 1;
                                        }
                                    }
                                }

                                this.mCachedViews.add(var11, var1);
                                var4 = true;
                            }
                        }

                        var5 = var4;
                        var8 = var7;
                        if (!var4) {
                            this.addViewHolderToRecycledViewPool(var1, true);
                            var8 = true;
                            var5 = var4;
                        }
                    }

                    RecyclerView.this.mViewInfoStore.removeViewHolder(var1);
                    if (!var5 && !var8 && var2) {
                        var1.mOwnerRecyclerView = null;
                    }

                }
            } else {
                StringBuilder var3 = (new StringBuilder()).append("Scrapped or attached views may not be recycled. isScrap:").append(var1.isScrap()).append(" isAttached:");
                if (var1.itemView.getParent() != null) {
                    var2 = true;
                }

                throw new IllegalArgumentException(var3.append(var2).append(RecyclerView.this.exceptionLabel()).toString());
            }
        }

        void recycleViewInternal(View var1) {
            this.recycleViewHolderInternal(RecyclerView.getChildViewHolderInt(var1));
        }

        void scrapView(View var1) {
            RecyclerView.ViewHolder var2 = RecyclerView.getChildViewHolderInt(var1);
            if (!var2.hasAnyOfTheFlags(12) && var2.isUpdated() && !RecyclerView.this.canReuseUpdatedViewHolder(var2)) {
                if (this.mChangedScrap == null) {
                    this.mChangedScrap = new ArrayList();
                }

                var2.setScrapContainer(this, true);
                this.mChangedScrap.add(var2);
            } else {
                if (var2.isInvalid() && !var2.isRemoved() && !RecyclerView.this.mAdapter.hasStableIds()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool." + RecyclerView.this.exceptionLabel());
                }

                var2.setScrapContainer(this, false);
                this.mAttachedScrap.add(var2);
            }

        }

        void setViewCacheExtension(RecyclerView.ViewCacheExtension var1) {
            this.mViewCacheExtension = var1;
        }

        public void setViewCacheSize(int var1) {
            this.mRequestedCacheMax = var1;
            this.updateViewCacheSize();
        }

        public RecyclerView.ViewHolder tryGetViewHolderForPositionByDeadline(int var1, boolean var2, long var3) {
            if (var1 >= 0 && var1 < RecyclerView.this.mState.getItemCount()) {
                boolean var5 = false;
                RecyclerView.ViewHolder var6 = null;
                if (RecyclerView.this.mState.isPreLayout()) {
                    var6 = this.getChangedScrapViewForPosition(var1);
                    if (var6 != null) {
                        var5 = true;
                    } else {
                        var5 = false;
                    }
                }

                RecyclerView.ViewHolder var7 = var6;
                boolean var8 = var5;
                if (var6 == null) {
                    var6 = this.getScrapOrHiddenOrCachedHolderForPosition(var1, var2);
                    var7 = var6;
                    var8 = var5;
                    if (var6 != null) {
                        if (!this.validateViewHolderForOffsetPosition(var6)) {
                            if (!var2) {
                                var6.addFlags(4);
                                if (var6.isScrap()) {
                                    RecyclerView.this.removeDetachedView(var6.itemView, false);
                                    var6.unScrap();
                                } else if (var6.wasReturnedFromScrap()) {
                                    var6.clearReturnedFromScrapFlag();
                                }

                                this.recycleViewHolderInternal(var6);
                            }

                            var7 = null;
                            var8 = var5;
                        } else {
                            var8 = true;
                            var7 = var6;
                        }
                    }
                }

                var6 = var7;
                var5 = var8;
                if (var7 == null) {
                    int var9 = RecyclerView.this.mAdapterHelper.findPositionOffset(var1);
                    if (var9 < 0 || var9 >= RecyclerView.this.mAdapter.getItemCount()) {
                        throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + var1 + "(offset:" + var9 + ").state:" + RecyclerView.this.mState.getItemCount() + RecyclerView.this.exceptionLabel());
                    }

                    int var10 = RecyclerView.this.mAdapter.getItemViewType(var9);
                    var6 = var7;
                    var5 = var8;
                    if (RecyclerView.this.mAdapter.hasStableIds()) {
                        var7 = this.getScrapOrCachedViewForId(RecyclerView.this.mAdapter.getItemId(var9), var10, var2);
                        var6 = var7;
                        var5 = var8;
                        if (var7 != null) {
                            var7.mPosition = var9;
                            var5 = true;
                            var6 = var7;
                        }
                    }

                    var7 = var6;
                    if (var6 == null) {
                        var7 = var6;
                        if (this.mViewCacheExtension != null) {
                            View var11 = this.mViewCacheExtension.getViewForPositionAndType(this, var1, var10);
                            var7 = var6;
                            if (var11 != null) {
                                var6 = RecyclerView.this.getChildViewHolder(var11);
                                if (var6 == null) {
                                    throw new IllegalArgumentException("getViewForPositionAndType returned a view which does not have a ViewHolder" + RecyclerView.this.exceptionLabel());
                                }

                                var7 = var6;
                                if (var6.shouldIgnore()) {
                                    throw new IllegalArgumentException("getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view." + RecyclerView.this.exceptionLabel());
                                }
                            }
                        }
                    }

                    var6 = var7;
                    if (var7 == null) {
                        var7 = this.getRecycledViewPool().getRecycledView(var10);
                        var6 = var7;
                        if (var7 != null) {
                            var7.resetInternal();
                            var6 = var7;
                            if (RecyclerView.FORCE_INVALIDATE_DISPLAY_LIST) {
                                this.invalidateDisplayListInt(var7);
                                var6 = var7;
                            }
                        }
                    }

                    if (var6 == null) {
                        long var12 = RecyclerView.this.getNanoTime();
                        if (var3 != 9223372036854775807L && !this.mRecyclerPool.willCreateInTime(var10, var12, var3)) {
                            var6 = null;
                            return var6;
                        }

                        var6 = RecyclerView.this.mAdapter.createViewHolder(RecyclerView.this, var10);
                        if (RecyclerView.ALLOW_THREAD_GAP_WORK) {
                            RecyclerView var17 = RecyclerView.findNestedRecyclerView(var6.itemView);
                            if (var17 != null) {
                                var6.mNestedRecyclerView = new WeakReference(var17);
                            }
                        }

                        long var14 = RecyclerView.this.getNanoTime();
                        this.mRecyclerPool.factorInCreateTime(var10, var14 - var12);
                    }
                }

                if (var5 && !RecyclerView.this.mState.isPreLayout() && var6.hasAnyOfTheFlags(8192)) {
                    var6.setFlags(0, 8192);
                    if (RecyclerView.this.mState.mRunSimpleAnimations) {
                        int var16 = RecyclerView.ItemAnimator.buildAdapterChangeFlagsForAnimations(var6);
                        RecyclerView.ItemAnimator.ItemHolderInfo var18 = RecyclerView.this.mItemAnimator.recordPreLayoutInformation(RecyclerView.this.mState, var6, var16 | 4096, var6.getUnmodifiedPayloads());
                        RecyclerView.this.recordAnimationInfoIfBouncedHiddenView(var6, var18);
                    }
                }

                var2 = false;
                if (RecyclerView.this.mState.isPreLayout() && var6.isBound()) {
                    var6.mPreLayoutPosition = var1;
                } else if (!var6.isBound() || var6.needsUpdate() || var6.isInvalid()) {
                    var2 = this.tryBindViewHolderByDeadline(var6, RecyclerView.this.mAdapterHelper.findPositionOffset(var1), var1, var3);
                }

                android.view.ViewGroup.LayoutParams var19 = var6.itemView.getLayoutParams();
                RecyclerView.LayoutParams var20;
                if (var19 == null) {
                    var20 = (RecyclerView.LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                    var6.itemView.setLayoutParams(var20);
                } else if (!RecyclerView.this.checkLayoutParams(var19)) {
                    var20 = (RecyclerView.LayoutParams) RecyclerView.this.generateLayoutParams(var19);
                    var6.itemView.setLayoutParams(var20);
                } else {
                    var20 = (RecyclerView.LayoutParams) var19;
                }

                var20.mViewHolder = var6;
                if (var5 && var2) {
                    var2 = true;
                } else {
                    var2 = false;
                }

                var20.mPendingInvalidate = var2;
                return var6;
            } else {
                throw new IndexOutOfBoundsException("Invalid item position " + var1 + "(" + var1 + "). Item count:" + RecyclerView.this.mState.getItemCount() + RecyclerView.this.exceptionLabel());
            }
        }

        void unscrapView(RecyclerView.ViewHolder var1) {
            if (var1.mInChangeScrap) {
                this.mChangedScrap.remove(var1);
            } else {
                this.mAttachedScrap.remove(var1);
            }

            var1.mScrapContainer = null;
            var1.mInChangeScrap = false;
            var1.clearReturnedFromScrapFlag();
        }

        public void updateViewCacheSize() {
            int var1;
            if (RecyclerView.this.mLayout != null) {
                var1 = RecyclerView.this.mLayout.mPrefetchMaxCountObserved;
            } else {
                var1 = 0;
            }

            this.mViewCacheMax = this.mRequestedCacheMax + var1;

            for (var1 = this.mCachedViews.size() - 1; var1 >= 0 && this.mCachedViews.size() > this.mViewCacheMax; --var1) {
                this.recycleCachedViewAt(var1);
            }

        }

        boolean validateViewHolderForOffsetPosition(RecyclerView.ViewHolder var1) {
            boolean var2 = true;
            boolean var3;
            if (var1.isRemoved()) {
                var3 = RecyclerView.this.mState.isPreLayout();
            } else {
                if (var1.mPosition < 0 || var1.mPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder adapter position" + var1 + RecyclerView.this.exceptionLabel());
                }

                if (!RecyclerView.this.mState.isPreLayout() && RecyclerView.this.mAdapter.getItemViewType(var1.mPosition) != var1.getItemViewType()) {
                    var3 = false;
                } else {
                    var3 = var2;
                    if (RecyclerView.this.mAdapter.hasStableIds()) {
                        var3 = var2;
                        if (var1.getItemId() != RecyclerView.this.mAdapter.getItemId(var1.mPosition)) {
                            var3 = false;
                        }
                    }
                }
            }

            return var3;
        }

        void viewRangeUpdate(int var1, int var2) {
            for (int var3 = this.mCachedViews.size() - 1; var3 >= 0; --var3) {
                RecyclerView.ViewHolder var4 = (RecyclerView.ViewHolder) this.mCachedViews.get(var3);
                if (var4 != null) {
                    int var5 = var4.mPosition;
                    if (var5 >= var1 && var5 < var1 + var2) {
                        var4.addFlags(2);
                        this.recycleCachedViewAt(var3);
                    }
                }
            }

        }
    }

    private class RecyclerViewDataObserver extends RecyclerView.AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        public void onChanged() {
            RecyclerView.this.assertNotInLayoutOrScroll((String) null);
            RecyclerView.this.mState.mStructureChanged = true;
            RecyclerView.this.processDataSetCompletelyChanged(true);
            if (!RecyclerView.this.mAdapterHelper.hasPendingUpdates()) {
                RecyclerView.this.requestLayout();
            }

            if (RecyclerView.this.mFastScroller != null) {
                RecyclerView.this.mFastScroller.onSectionsChanged();
            }

        }

        public void onItemRangeChanged(int var1, int var2, Object var3) {
            RecyclerView.this.assertNotInLayoutOrScroll((String) null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeChanged(var1, var2, var3)) {
                this.triggerUpdateProcessor();
            }

        }

        public void onItemRangeInserted(int var1, int var2) {
            RecyclerView.this.assertNotInLayoutOrScroll((String) null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeInserted(var1, var2)) {
                this.triggerUpdateProcessor();
            }

        }

        public void onItemRangeMoved(int var1, int var2, int var3) {
            RecyclerView.this.assertNotInLayoutOrScroll((String) null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeMoved(var1, var2, var3)) {
                this.triggerUpdateProcessor();
            }

        }

        public void onItemRangeRemoved(int var1, int var2) {
            RecyclerView.this.assertNotInLayoutOrScroll((String) null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeRemoved(var1, var2)) {
                this.triggerUpdateProcessor();
            }

        }

        void triggerUpdateProcessor() {
            if (RecyclerView.POST_UPDATES_ON_ANIMATION && RecyclerView.this.mHasFixedSize && RecyclerView.this.mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, RecyclerView.this.mUpdateChildViewsRunnable);
            } else {
                RecyclerView.this.mAdapterUpdateDuringMeasure = true;
                RecyclerView.this.requestLayout();
            }

        }
    }

    class ViewFlinger implements Runnable {
        Interpolator mInterpolator;
        private boolean mEatRunOnAnimationRequest;
        private int mLastFlingX;
        private int mLastFlingY;
        private boolean mReSchedulePostAnimationCallback;
        private SeslOverScroller mScroller;

        ViewFlinger() {
            this.mInterpolator = RecyclerView.sQuinticInterpolator;
            this.mEatRunOnAnimationRequest = false;
            this.mReSchedulePostAnimationCallback = false;
            this.mScroller = new SeslOverScroller(RecyclerView.this.getContext(), RecyclerView.sQuinticInterpolator);
        }

        private int computeScrollDuration(int var1, int var2, int var3, int var4) {
            int var5 = Math.abs(var1);
            int var6 = Math.abs(var2);
            boolean var7;
            if (var5 > var6) {
                var7 = true;
            } else {
                var7 = false;
            }

            var3 = (int) Math.sqrt((double) (var3 * var3 + var4 * var4));
            var2 = (int) Math.sqrt((double) (var1 * var1 + var2 * var2));
            if (var7) {
                var1 = RecyclerView.this.getWidth();
            } else {
                var1 = RecyclerView.this.getHeight();
            }

            var4 = var1 / 2;
            float var8 = Math.min(1.0F, 1.0F * (float) var2 / (float) var1);
            float var9 = (float) var4;
            float var10 = (float) var4;
            var8 = this.distanceInfluenceForSnapDuration(var8);
            if (var3 > 0) {
                var1 = Math.round(1000.0F * Math.abs((var9 + var10 * var8) / (float) var3)) * 4;
            } else {
                if (var7) {
                    var2 = var5;
                } else {
                    var2 = var6;
                }

                var1 = (int) (((float) var2 / (float) var1 + 1.0F) * 300.0F);
            }

            return Math.min(var1, 2000);
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private float distanceInfluenceForSnapDuration(float var1) {
            return (float) Math.sin((double) ((var1 - 0.5F) * 0.47123894F));
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                this.postOnAnimation();
            }

        }

        public void fling(int var1, int var2) {
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, var1, var2, -2147483648, 2147483647, -2147483648, 2147483647);
            this.postOnAnimation();
        }

        void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
            } else {
                RecyclerView.this.removeCallbacks(this);
                ViewCompat.postOnAnimation(RecyclerView.this, this);
            }

        }

        @SuppressLint("WrongConstant")
        public void run() {
            if (RecyclerView.this.mLayout == null) {
                this.stop();
            } else {
                this.disableRunOnAnimationRequests();
                RecyclerView.this.consumePendingUpdateOperations();
                SeslOverScroller var1 = this.mScroller;
                RecyclerView.SmoothScroller var2 = RecyclerView.this.mLayout.mSmoothScroller;
                if (var1.computeScrollOffset()) {
                    int[] var3 = RecyclerView.this.mScrollConsumed;
                    int var4 = var1.getCurrX();
                    int var5 = var1.getCurrY();
                    int var6 = var4 - this.mLastFlingX;
                    int var7 = var5 - this.mLastFlingY;
                    int var8 = 0;
                    int var9 = 0;
                    this.mLastFlingX = var4;
                    this.mLastFlingY = var5;
                    int var10 = 0;
                    int var11 = 0;
                    int var12 = 0;
                    int var13 = 0;
                    if (RecyclerView.this.dispatchNestedPreScroll(var6, var7, var3, (int[]) null, 1)) {
                        var6 -= var3[0];
                        var7 -= var3[1];
                        RecyclerView.this.adjustNestedScrollRangeBy(var3[1]);
                    } else {
                        RecyclerView.this.adjustNestedScrollRangeBy(var7);
                    }

                    int var14;
                    int var15;
                    if (RecyclerView.this.mAdapter != null) {
                        RecyclerView.this.startInterceptRequestLayout();
                        RecyclerView.this.onEnterLayoutOrScroll();
                        TraceCompat.beginSection("RV Scroll");
                        RecyclerView.this.fillRemainingScrollValues(RecyclerView.this.mState);
                        if (var6 != 0) {
                            var14 = RecyclerView.this.mLayout.scrollHorizontallyBy(var6, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                            var11 = var6 - var14;
                        } else {
                            var14 = 0;
                        }

                        if (var7 != 0) {
                            var12 = RecyclerView.this.mLayout.scrollVerticallyBy(var7, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                            var10 = var7 - var12;
                            var9 = var12;
                            var13 = var10;
                            if (RecyclerView.this.mGoToTopState == 0) {
                                RecyclerView.this.setupGoToTop(1);
                                RecyclerView.this.autoHide(1);
                                var13 = var10;
                                var9 = var12;
                            }
                        }

                        TraceCompat.endSection();
                        RecyclerView.this.repositionShadowingViews();
                        RecyclerView.this.onExitLayoutOrScroll();
                        RecyclerView.this.stopInterceptRequestLayout(false);
                        var15 = var14;
                        var8 = var9;
                        var10 = var11;
                        var12 = var13;
                        if (var2 != null) {
                            var15 = var14;
                            var8 = var9;
                            var10 = var11;
                            var12 = var13;
                            if (!var2.isPendingInitialRun()) {
                                var15 = var14;
                                var8 = var9;
                                var10 = var11;
                                var12 = var13;
                                if (var2.isRunning()) {
                                    var10 = RecyclerView.this.mState.getItemCount();
                                    if (var10 == 0) {
                                        var2.stop();
                                        var12 = var13;
                                        var10 = var11;
                                        var8 = var9;
                                        var15 = var14;
                                    } else if (var2.getTargetPosition() >= var10) {
                                        var2.setTargetPosition(var10 - 1);
                                        var2.onAnimation(var6 - var11, var7 - var13);
                                        var15 = var14;
                                        var8 = var9;
                                        var10 = var11;
                                        var12 = var13;
                                    } else {
                                        var2.onAnimation(var6 - var11, var7 - var13);
                                        var15 = var14;
                                        var8 = var9;
                                        var10 = var11;
                                        var12 = var13;
                                    }
                                }
                            }
                        }
                    } else {
                        var15 = 0;
                    }

                    if (!RecyclerView.this.mItemDecorations.isEmpty()) {
                        RecyclerView.this.invalidate();
                    }

                    if (RecyclerView.this.getOverScrollMode() != 2) {
                        RecyclerView.this.considerReleasingGlowsOnScroll(var6, var7);
                    }

                    boolean var16 = RecyclerView.this.dispatchNestedScroll(var15, var8, var10, var12, (int[]) null, 1);
                    if (var16) {
                        RecyclerView.this.mScrollOffset[0] = 0;
                        RecyclerView.this.mScrollOffset[1] = 0;
                    }

                    if (var10 != 0 || var12 != 0) {
                        var9 = (int) var1.getCurrVelocity();
                        var14 = 0;
                        if (var10 != var4) {
                            if (var10 < 0) {
                                var14 = -var9;
                            } else if (var10 > 0) {
                                var14 = var9;
                            } else {
                                var14 = 0;
                            }
                        }

                        var11 = 0;
                        if (var12 != var5) {
                            if (var12 < 0) {
                                var11 = -var9;
                            } else if (var12 > 0) {
                                var11 = var9;
                            } else {
                                var11 = 0;
                            }
                        }

                        if ((!var16 || var12 >= 0) && RecyclerView.this.getOverScrollMode() != 2) {
                            RecyclerView.this.absorbGlows(var14, var11);
                        }

                        if ((!var16 || var12 >= 0) && (var14 != 0 || var10 == var4 || var1.getFinalX() == 0) && (var11 != 0 || var12 == var5 || var1.getFinalY() == 0)) {
                            var1.abortAnimation();
                        }
                    }

                    if (var15 != 0 || var8 != 0) {
                        RecyclerView.this.dispatchOnScrolled(var15, var8);
                    }

                    if (!RecyclerView.this.awakenScrollBars()) {
                        RecyclerView.this.invalidate();
                    }

                    boolean var18;
                    if (var7 != 0 && RecyclerView.this.mLayout.canScrollVertically() && var8 == var7) {
                        var18 = true;
                    } else {
                        var18 = false;
                    }

                    boolean var17;
                    if (var6 != 0 && RecyclerView.this.mLayout.canScrollHorizontally() && var15 == var6) {
                        var17 = true;
                    } else {
                        var17 = false;
                    }

                    if ((var6 != 0 || var7 != 0) && !var17 && !var18) {
                        var18 = false;
                    } else {
                        var18 = true;
                    }

                    if (var1.isFinished() || !var18 && !RecyclerView.this.hasNestedScrollingParent(1)) {
                        if (RecyclerView.this.getOverScrollMode() != 2 && RecyclerView.this.mNestedScroll) {
                            RecyclerView.this.pullGlows((float) var6, (float) var10, (float) var7, (float) var12);
                            RecyclerView.this.considerReleasingGlowsOnScroll(var4, var5);
                        }

                        RecyclerView.this.setScrollState(0);
                        if (RecyclerView.ALLOW_THREAD_GAP_WORK) {
                            RecyclerView.this.mPrefetchRegistry.clearPrefetchPositions();
                        }

                        RecyclerView.this.stopNestedScroll(1);
                    } else {
                        this.postOnAnimation();
                        if (RecyclerView.this.mGapWorker != null) {
                            RecyclerView.this.mGapWorker.postFromTraversal(RecyclerView.this, var6, var7);
                        }
                    }
                }

                if (var2 != null) {
                    if (var2.isPendingInitialRun()) {
                        var2.onAnimation(0, 0);
                    }

                    if (!this.mReSchedulePostAnimationCallback) {
                        var2.stop();
                    }
                }

                this.enableRunOnAnimationRequests();
            }

        }

        public void smoothScrollBy(int var1, int var2) {
            this.smoothScrollBy(var1, var2, 0, 0);
        }

        public void smoothScrollBy(int var1, int var2, int var3) {
            this.smoothScrollBy(var1, var2, var3, RecyclerView.sQuinticInterpolator);
        }

        public void smoothScrollBy(int var1, int var2, int var3, int var4) {
            this.smoothScrollBy(var1, var2, this.computeScrollDuration(var1, var2, var3, var4));
        }

        @SuppressLint("WrongConstant")
        public void smoothScrollBy(int var1, int var2, int var3, Interpolator var4) {
            byte var5;
            if (var1 != 0) {
                var5 = 2;
            } else {
                var5 = 1;
            }

            RecyclerView.this.startNestedScroll(var5, 1);
            if (!RecyclerView.this.dispatchNestedPreScroll(var1, var2, (int[]) null, (int[]) null, 1)) {
                if (this.mInterpolator != var4) {
                    this.mInterpolator = var4;
                    this.mScroller = new SeslOverScroller(RecyclerView.this.getContext(), var4);
                }

                RecyclerView.this.setScrollState(2);
                this.mLastFlingY = 0;
                this.mLastFlingX = 0;
                this.mScroller.startScroll(0, 0, var1, var2, var3);
                if (VERSION.SDK_INT < 23) {
                    this.mScroller.computeScrollOffset();
                }

                this.postOnAnimation();
            }

            RecyclerView.this.adjustNestedScrollRangeBy(var2);
        }

        public void smoothScrollBy(int var1, int var2, Interpolator var3) {
            int var4 = this.computeScrollDuration(var1, var2, 0, 0);
            Interpolator var5 = var3;
            if (var3 == null) {
                var5 = RecyclerView.sQuinticInterpolator;
            }

            this.smoothScrollBy(var1, var2, var4, var5);
        }

        public void stop() {
            RecyclerView.this.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }
}
