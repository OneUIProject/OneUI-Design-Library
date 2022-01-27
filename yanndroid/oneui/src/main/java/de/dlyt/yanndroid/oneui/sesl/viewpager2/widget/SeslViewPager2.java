package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import static de.dlyt.yanndroid.oneui.view.RecyclerView.NO_POSITION;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;

import java.lang.annotation.Retention;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.PagerSnapHelper;
import de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.StatefulAdapter;
import de.dlyt.yanndroid.oneui.view.RecyclerView;
import de.dlyt.yanndroid.oneui.view.RecyclerView.Adapter;
import de.dlyt.yanndroid.oneui.view.RecyclerView.ItemDecoration;

public class SeslViewPager2 extends ViewGroup {
    @Retention(SOURCE)
    @IntDef({ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL})
    public @interface Orientation {
    }
    public static final int ORIENTATION_HORIZONTAL = RecyclerView.HORIZONTAL;
    public static final int ORIENTATION_VERTICAL = RecyclerView.VERTICAL;
    @Retention(SOURCE)
    @IntDef({SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING})
    public @interface ScrollState {
    }
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;
    @SuppressWarnings("WeakerAccess")
    @Retention(SOURCE)
    @IntDef({OFFSCREEN_PAGE_LIMIT_DEFAULT})
    @IntRange(from = 1)
    public @interface OffscreenPageLimit {
    }
    public static final int OFFSCREEN_PAGE_LIMIT_DEFAULT = -1;
    static boolean sFeatureEnhancedA11yEnabled = true;
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();

    private CompositeOnPageChangeCallback mExternalPageChangeCallbacks = new CompositeOnPageChangeCallback(3);
    int mCurrentItem;
    boolean mCurrentItemDirty = false;
    private RecyclerView.AdapterDataObserver mCurrentItemDataSetChangeObserver = new DataSetChangeObserver() {
        @Override
        public void onChanged() {
            mCurrentItemDirty = true;
            mScrollEventAdapter.notifyDataSetChangeHappened();
        }
    };
    LinearLayoutManager mLayoutManager;
    private int mPendingCurrentItem = NO_POSITION;
    private Parcelable mPendingAdapterState;
    RecyclerView mRecyclerView;
    private PagerSnapHelper mPagerSnapHelper;
    ScrollEventAdapter mScrollEventAdapter;
    private CompositeOnPageChangeCallback mPageChangeEventDispatcher;
    private FakeDrag mFakeDragger;
    private PageTransformerAdapter mPageTransformerAdapter;
    private RecyclerView.ItemAnimator mSavedItemAnimator = null;
    private boolean mSavedItemAnimatorPresent = false;
    private boolean mUserInputEnabled = true;
    private @OffscreenPageLimit int mOffscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT;
    AccessibilityProvider mAccessibilityProvider;

    public SeslViewPager2(@NonNull Context context) {
        super(context);
        initialize(context, null);
    }

    public SeslViewPager2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public SeslViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    @RequiresApi(21)
    public SeslViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        mAccessibilityProvider = sFeatureEnhancedA11yEnabled ? new PageAwareAccessibilityProvider() : new BasicAccessibilityProvider();

        mRecyclerView = new RecyclerViewImpl(context);
        mRecyclerView.setId(ViewCompat.generateViewId());
        mRecyclerView.setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);

        mLayoutManager = new LinearLayoutManagerImpl(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        setOrientation(context, attrs);

        mRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mRecyclerView.addOnChildAttachStateChangeListener(enforceChildFillListener());

        mScrollEventAdapter = new ScrollEventAdapter(this);
        mFakeDragger = new FakeDrag(this, mScrollEventAdapter, mRecyclerView);
        mPagerSnapHelper = new PagerSnapHelperImpl();
        mPagerSnapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnScrollListener(mScrollEventAdapter);

        mPageChangeEventDispatcher = new CompositeOnPageChangeCallback(3);
        mScrollEventAdapter.setOnPageChangeCallback(mPageChangeEventDispatcher);

        final OnPageChangeCallback currentItemUpdater = new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (mCurrentItem != position) {
                    mCurrentItem = position;
                    mAccessibilityProvider.onSetNewCurrentItem();
                }
            }

            @Override
            public void onPageScrollStateChanged(int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    updateCurrentItem();
                }
            }
        };

        final OnPageChangeCallback focusClearer = new OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                clearFocus();
                if (hasFocus()) {
                    mRecyclerView.requestFocus(View.FOCUS_FORWARD);
                }
            }
        };

        mPageChangeEventDispatcher.addOnPageChangeCallback(currentItemUpdater);
        mPageChangeEventDispatcher.addOnPageChangeCallback(focusClearer);
        mAccessibilityProvider.onInitialize(mPageChangeEventDispatcher, mRecyclerView);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mExternalPageChangeCallbacks);

        mPageTransformerAdapter = new PageTransformerAdapter(mLayoutManager);
        mPageChangeEventDispatcher.addOnPageChangeCallback(mPageTransformerAdapter);

        attachViewToParent(mRecyclerView, 0, mRecyclerView.getLayoutParams());
    }

    private RecyclerView.OnChildAttachStateChangeListener enforceChildFillListener() {
        return new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (layoutParams.width != LayoutParams.MATCH_PARENT || layoutParams.height != LayoutParams.MATCH_PARENT) {
                    throw new IllegalStateException("Pages must fill the whole ViewPager2 (use match_parent)");
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        };
    }

    @RequiresApi(23)
    @Override
    public CharSequence getAccessibilityClassName() {
        if (mAccessibilityProvider.handlesGetAccessibilityClassName()) {
            return mAccessibilityProvider.onGetAccessibilityClassName();
        }
        return super.getAccessibilityClassName();
    }

    private void setOrientation(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeslViewPager2);
        ViewCompat.saveAttributeDataForStyleable(this, context, R.styleable.SeslViewPager2, attrs, a, 0, 0);
        try {
            setOrientation(a.getInt(R.styleable.SeslViewPager2_android_orientation, ORIENTATION_HORIZONTAL));
        } finally {
            a.recycle();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.mRecyclerViewId = mRecyclerView.getId();
        ss.mCurrentItem = mPendingCurrentItem == NO_POSITION ? mCurrentItem : mPendingCurrentItem;

        if (mPendingAdapterState != null) {
            ss.mAdapterState = mPendingAdapterState;
        } else {
            Adapter<?> adapter = mRecyclerView.getAdapter();
            if (adapter instanceof StatefulAdapter) {
                ss.mAdapterState = ((StatefulAdapter) adapter).saveState();
            }
        }

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mPendingCurrentItem = ss.mCurrentItem;
        mPendingAdapterState = ss.mAdapterState;
    }

    private void restorePendingState() {
        if (mPendingCurrentItem == NO_POSITION) {
            return;
        }
        Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        if (mPendingAdapterState != null) {
            if (adapter instanceof StatefulAdapter) {
                ((StatefulAdapter) adapter).restoreState(mPendingAdapterState);
            }
            mPendingAdapterState = null;
        }
        mCurrentItem = Math.max(0, Math.min(mPendingCurrentItem, adapter.getItemCount() - 1));
        mPendingCurrentItem = NO_POSITION;
        mRecyclerView.scrollToPosition(mCurrentItem);
        mAccessibilityProvider.onRestorePendingState();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        Parcelable state = container.get(getId());
        if (state instanceof SavedState) {
            final int previousRvId = ((SavedState) state).mRecyclerViewId;
            final int currentRvId = mRecyclerView.getId();
            container.put(currentRvId, container.get(previousRvId));
            container.remove(previousRvId);
        }

        super.dispatchRestoreInstanceState(container);

        restorePendingState();
    }

    static class SavedState extends BaseSavedState {
        int mRecyclerViewId;
        int mCurrentItem;
        Parcelable mAdapterState;

        @RequiresApi(24)
        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            readValues(source, loader);
        }

        SavedState(Parcel source) {
            super(source);
            readValues(source, null);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("deprecation")
        private void readValues(Parcel source, ClassLoader loader) {
            mRecyclerViewId = source.readInt();
            mCurrentItem = source.readInt();
            mAdapterState = source.readParcelable(loader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mRecyclerViewId);
            out.writeInt(mCurrentItem);
            out.writeParcelable(mAdapterState, flags);
        }

        public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return Build.VERSION.SDK_INT >= 24 ? new SavedState(source, loader) : new SavedState(source);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setAdapter(@Nullable @SuppressWarnings("rawtypes") Adapter adapter) {
        final Adapter<?> currentAdapter = mRecyclerView.getAdapter();
        mAccessibilityProvider.onDetachAdapter(currentAdapter);
        unregisterCurrentItemDataSetTracker(currentAdapter);
        mRecyclerView.setAdapter(adapter);
        mCurrentItem = 0;
        restorePendingState();
        mAccessibilityProvider.onAttachAdapter(adapter);
        registerCurrentItemDataSetTracker(adapter);
    }

    private void registerCurrentItemDataSetTracker(@Nullable Adapter<?> adapter) {
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
    }

    private void unregisterCurrentItemDataSetTracker(@Nullable Adapter<?> adapter) {
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(mCurrentItemDataSetChangeObserver);
        }
    }

    @SuppressWarnings("rawtypes")
    public @Nullable Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    @Override
    public void onViewAdded(View child) {
        throw new IllegalStateException(getClass().getSimpleName() + " does not support direct child views");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChild(mRecyclerView, widthMeasureSpec, heightMeasureSpec);
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();
        int childState = mRecyclerView.getMeasuredState();

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();

        width = Math.max(width, getSuggestedMinimumWidth());
        height = Math.max(height, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSizeAndState(width, widthMeasureSpec, childState), resolveSizeAndState(height, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mRecyclerView.getMeasuredWidth();
        int height = mRecyclerView.getMeasuredHeight();

        mTmpContainerRect.left = getPaddingLeft();
        mTmpContainerRect.right = r - l - getPaddingRight();
        mTmpContainerRect.top = getPaddingTop();
        mTmpContainerRect.bottom = b - t - getPaddingBottom();

        Gravity.apply(Gravity.TOP | Gravity.START, width, height, mTmpContainerRect, mTmpChildRect);
        mRecyclerView.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);

        if (mCurrentItemDirty) {
            updateCurrentItem();
        }
    }

    void updateCurrentItem() {
        if (mPagerSnapHelper == null) {
            throw new IllegalStateException("Design assumption violated.");
        }

        View snapView = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (snapView == null) {
            return;
        }
        int snapPosition = mLayoutManager.getPosition(snapView);

        if (snapPosition != mCurrentItem && getScrollState() == SCROLL_STATE_IDLE) {
            mPageChangeEventDispatcher.onPageSelected(snapPosition);
        }

        mCurrentItemDirty = false;
    }

    int getPageSize() {
        final RecyclerView rv = mRecyclerView;
        return getOrientation() == ORIENTATION_HORIZONTAL ? rv.getWidth() - rv.getPaddingLeft() - rv.getPaddingRight() : rv.getHeight() - rv.getPaddingTop() - rv.getPaddingBottom();
    }

    public void setOrientation(@Orientation int orientation) {
        mLayoutManager.setOrientation(orientation);
        mAccessibilityProvider.onSetOrientation();
    }

    public int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    boolean isRtl() {
        return mLayoutManager.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (isFakeDragging()) {
            throw new IllegalStateException("Cannot change current item when ViewPager2 is fake dragging");
        }
        setCurrentItemInternal(item, smoothScroll);
    }

    void setCurrentItemInternal(int item, boolean smoothScroll) {
        Adapter<?> adapter = getAdapter();
        if (adapter == null) {
            if (mPendingCurrentItem != NO_POSITION) {
                mPendingCurrentItem = Math.max(item, 0);
            }
            return;
        }
        if (adapter.getItemCount() <= 0) {
            return;
        }
        item = Math.max(item, 0);
        item = Math.min(item, adapter.getItemCount() - 1);

        if (item == mCurrentItem && mScrollEventAdapter.isIdle()) {
            return;
        }
        if (item == mCurrentItem && smoothScroll) {
            return;
        }

        double previousItem = mCurrentItem;
        mCurrentItem = item;
        mAccessibilityProvider.onSetNewCurrentItem();

        if (!mScrollEventAdapter.isIdle()) {
            previousItem = mScrollEventAdapter.getRelativeScrollPosition();
        }

        mScrollEventAdapter.notifyProgrammaticScroll(item, smoothScroll);
        if (!smoothScroll) {
            mRecyclerView.scrollToPosition(item);
            return;
        }

        if (Math.abs(item - previousItem) > 3) {
            mRecyclerView.scrollToPosition(item > previousItem ? item - 3 : item + 3);
            mRecyclerView.post(new SmoothScrollToPosition(item, mRecyclerView));
        } else {
            mRecyclerView.smoothScrollToPosition(item);
        }
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    @ScrollState
    public int getScrollState() {
        return mScrollEventAdapter.getScrollState();
    }

    public boolean beginFakeDrag() {
        return mFakeDragger.beginFakeDrag();
    }

    public boolean fakeDragBy(@SuppressLint("SupportAnnotationUsage") @Px float offsetPxFloat) {
        return mFakeDragger.fakeDragBy(offsetPxFloat);
    }

    public boolean endFakeDrag() {
        return mFakeDragger.endFakeDrag();
    }

    public boolean isFakeDragging() {
        return mFakeDragger.isFakeDragging();
    }

    void snapToPage() {
        View view = mPagerSnapHelper.findSnapView(mLayoutManager);
        if (view == null) {
            return;
        }
        int[] snapDistance = mPagerSnapHelper.calculateDistanceToFinalSnap(mLayoutManager, view);
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            mRecyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
        }
    }

    public void setUserInputEnabled(boolean enabled) {
        mUserInputEnabled = enabled;
        mAccessibilityProvider.onSetUserInputEnabled();
    }

    public boolean isUserInputEnabled() {
        return mUserInputEnabled;
    }

    public void setOffscreenPageLimit(@OffscreenPageLimit int limit) {
        if (limit < 1 && limit != OFFSCREEN_PAGE_LIMIT_DEFAULT) {
            throw new IllegalArgumentException("Offscreen page limit must be OFFSCREEN_PAGE_LIMIT_DEFAULT or a number > 0");
        }
        mOffscreenPageLimit = limit;
        mRecyclerView.requestLayout();
    }

    @OffscreenPageLimit
    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return mRecyclerView.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return mRecyclerView.canScrollVertically(direction);
    }

    public void registerOnPageChangeCallback(@NonNull OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.addOnPageChangeCallback(callback);
    }

    public void unregisterOnPageChangeCallback(@NonNull OnPageChangeCallback callback) {
        mExternalPageChangeCallbacks.removeOnPageChangeCallback(callback);
    }

    public void setPageTransformer(@Nullable PageTransformer transformer) {
        if (transformer != null) {
            if (!mSavedItemAnimatorPresent) {
                mSavedItemAnimator = mRecyclerView.getItemAnimator();
                mSavedItemAnimatorPresent = true;
            }
            mRecyclerView.setItemAnimator(null);
        } else {
            if (mSavedItemAnimatorPresent) {
                mRecyclerView.setItemAnimator(mSavedItemAnimator);
                mSavedItemAnimator = null;
                mSavedItemAnimatorPresent = false;
            }
        }

        if (transformer == mPageTransformerAdapter.getPageTransformer()) {
            return;
        }
        mPageTransformerAdapter.setPageTransformer(transformer);
        requestTransform();
    }

    public void requestTransform() {
        if (mPageTransformerAdapter.getPageTransformer() == null) {
            return;
        }
        double relativePosition = mScrollEventAdapter.getRelativeScrollPosition();
        int position = (int) relativePosition;
        float positionOffset = (float) (relativePosition - position);
        int positionOffsetPx = Math.round(getPageSize() * positionOffset);
        mPageTransformerAdapter.onPageScrolled(position, positionOffset, positionOffsetPx);
    }

    @Override
    @RequiresApi(17)
    public void setLayoutDirection(int layoutDirection) {
        super.setLayoutDirection(layoutDirection);
        mAccessibilityProvider.onSetLayoutDirection();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        mAccessibilityProvider.onInitializeAccessibilityNodeInfo(info);
    }

    @RequiresApi(16)
    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (mAccessibilityProvider.handlesPerformAccessibilityAction(action, arguments)) {
            return mAccessibilityProvider.onPerformAccessibilityAction(action, arguments);
        }
        return super.performAccessibilityAction(action, arguments);
    }

    private class RecyclerViewImpl extends RecyclerView {
        RecyclerViewImpl(@NonNull Context context) {
            super(context);
        }

        @RequiresApi(23)
        @Override
        public CharSequence getAccessibilityClassName() {
            if (mAccessibilityProvider.handlesRvGetAccessibilityClassName()) {
                return mAccessibilityProvider.onRvGetAccessibilityClassName();
            }
            return super.getAccessibilityClassName();
        }

        @Override
        public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            event.setFromIndex(mCurrentItem);
            event.setToIndex(mCurrentItem);
            mAccessibilityProvider.onRvInitializeAccessibilityEvent(event);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return isUserInputEnabled() && super.onTouchEvent(event);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return isUserInputEnabled() && super.onInterceptTouchEvent(ev);
        }
    }

    private class LinearLayoutManagerImpl extends LinearLayoutManager {
        LinearLayoutManagerImpl(Context context) {
            super(context);
        }

        @Override
        public boolean performAccessibilityAction(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int action, @Nullable Bundle args) {
            if (mAccessibilityProvider.handlesLmPerformAccessibilityAction(action)) {
                return mAccessibilityProvider.onLmPerformAccessibilityAction(action);
            }
            return super.performAccessibilityAction(recycler, state, action, args);
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(recycler, state, info);
            mAccessibilityProvider.onLmInitializeAccessibilityNodeInfo(info);
        }

        @Override
        public void onInitializeAccessibilityNodeInfoForItem(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
            mAccessibilityProvider.onLmInitializeAccessibilityNodeInfoForItem(host, info);
        }

        @Override
        protected void calculateExtraLayoutSpace(@NonNull RecyclerView.State state, @NonNull int[] extraLayoutSpace) {
            int pageLimit = getOffscreenPageLimit();
            if (pageLimit == OFFSCREEN_PAGE_LIMIT_DEFAULT) {
                super.calculateExtraLayoutSpace(state, extraLayoutSpace);
                return;
            }
            final int offscreenSpace = getPageSize() * pageLimit;
            extraLayoutSpace[0] = offscreenSpace;
            extraLayoutSpace[1] = offscreenSpace;
        }

        @Override
        public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull View child, @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
            return false;
        }
    }

    private class PagerSnapHelperImpl extends PagerSnapHelper {
        PagerSnapHelperImpl() {
        }

        @Nullable
        @Override
        public View findSnapView(RecyclerView.LayoutManager layoutManager) {
            return isFakeDragging() ? null : super.findSnapView(layoutManager);
        }
    }

    private static class SmoothScrollToPosition implements Runnable {
        private final int mPosition;
        private final RecyclerView mRecyclerView;

        SmoothScrollToPosition(int position, RecyclerView recyclerView) {
            mPosition = position;
            mRecyclerView = recyclerView;
        }

        @Override
        public void run() {
            mRecyclerView.smoothScrollToPosition(mPosition);
        }
    }

    public abstract static class OnPageChangeCallback {
        public void onPageScrolled(int position, float positionOffset, @Px int positionOffsetPixels) {
        }

        public void onPageSelected(int position) {
        }

        public void onPageScrollStateChanged(@ScrollState int state) {
        }
    }

    public interface PageTransformer {
        void transformPage(@NonNull View page, float position);
    }

    public void addItemDecoration(@NonNull ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
    }

    public void addItemDecoration(@NonNull ItemDecoration decor, int index) {
        mRecyclerView.addItemDecoration(decor, index);
    }

    @NonNull
    public ItemDecoration getItemDecorationAt(int index) {
        return mRecyclerView.getItemDecorationAt(index);
    }

    public int getItemDecorationCount() {
        return mRecyclerView.getItemDecorationCount();
    }

    public void invalidateItemDecorations() {
        mRecyclerView.invalidateItemDecorations();
    }

    public void removeItemDecorationAt(int index) {
        mRecyclerView.removeItemDecorationAt(index);
    }

    public void removeItemDecoration(@NonNull ItemDecoration decor) {
        mRecyclerView.removeItemDecoration(decor);
    }

    @SuppressWarnings({"ClassCanBeStatic", "InnerClassMayBeStatic"})
    private abstract class AccessibilityProvider {
        void onInitialize(@NonNull CompositeOnPageChangeCallback pageChangeEventDispatcher,
                @NonNull RecyclerView recyclerView) {
        }

        boolean handlesGetAccessibilityClassName() {
            return false;
        }

        String onGetAccessibilityClassName() {
            throw new IllegalStateException("Not implemented.");
        }

        void onRestorePendingState() {
        }

        void onAttachAdapter(@Nullable Adapter<?> newAdapter) {
        }

        void onDetachAdapter(@Nullable Adapter<?> oldAdapter) {
        }

        void onSetOrientation() {
        }

        void onSetNewCurrentItem() {
        }

        void onSetUserInputEnabled() {
        }

        void onSetLayoutDirection() {
        }

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        }

        boolean handlesPerformAccessibilityAction(int action, Bundle arguments) {
            return false;
        }

        boolean onPerformAccessibilityAction(int action, Bundle arguments) {
            throw new IllegalStateException("Not implemented.");
        }

        void onRvInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
        }

        boolean handlesLmPerformAccessibilityAction(int action) {
            return false;
        }

        boolean onLmPerformAccessibilityAction(int action) {
            throw new IllegalStateException("Not implemented.");
        }

        void onLmInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfoCompat info) {
        }

        void onLmInitializeAccessibilityNodeInfoForItem(@NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
        }

        boolean handlesRvGetAccessibilityClassName() {
            return false;
        }

        CharSequence onRvGetAccessibilityClassName() {
            throw new IllegalStateException("Not implemented.");
        }
    }

    class BasicAccessibilityProvider extends AccessibilityProvider {
        @Override
        public boolean handlesLmPerformAccessibilityAction(int action) {
            return (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD || action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD) && !isUserInputEnabled();
        }

        @Override
        public boolean onLmPerformAccessibilityAction(int action) {
            if (!handlesLmPerformAccessibilityAction(action)) {
                throw new IllegalStateException();
            }
            return false;
        }

        @Override
        public void onLmInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfoCompat info) {
            if (!isUserInputEnabled()) {
                info.removeAction(AccessibilityActionCompat.ACTION_SCROLL_BACKWARD);
                info.removeAction(AccessibilityActionCompat.ACTION_SCROLL_FORWARD);
                info.setScrollable(false);
            }
        }

        @Override
        public boolean handlesRvGetAccessibilityClassName() {
            return true;
        }

        @Override
        public CharSequence onRvGetAccessibilityClassName() {
            if (!handlesRvGetAccessibilityClassName()) {
                throw new IllegalStateException();
            }
            return "androidx.viewpager.widget.SeslViewPager";
        }
    }

    class PageAwareAccessibilityProvider extends AccessibilityProvider {
        private final AccessibilityViewCommand mActionPageForward = new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
                SeslViewPager2 viewPager = (SeslViewPager2) view;
                setCurrentItemFromAccessibilityCommand(viewPager.getCurrentItem() + 1);
                return true;
            }
        };

        private final AccessibilityViewCommand mActionPageBackward = new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view,
                                   @Nullable CommandArguments arguments) {
                SeslViewPager2 viewPager = (SeslViewPager2) view;
                setCurrentItemFromAccessibilityCommand(viewPager.getCurrentItem() - 1);
                return true;
            }
        };

        private RecyclerView.AdapterDataObserver mAdapterDataObserver;

        @Override
        public void onInitialize(@NonNull CompositeOnPageChangeCallback pageChangeEventDispatcher, @NonNull RecyclerView recyclerView) {
            ViewCompat.setImportantForAccessibility(recyclerView, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO);

            mAdapterDataObserver = new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    updatePageAccessibilityActions();
                }
            };

            if (ViewCompat.getImportantForAccessibility(SeslViewPager2.this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                ViewCompat.setImportantForAccessibility(SeslViewPager2.this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }
        }

        @Override
        public boolean handlesGetAccessibilityClassName() {
            return true;
        }

        @Override
        public String onGetAccessibilityClassName() {
            if (!handlesGetAccessibilityClassName()) {
                throw new IllegalStateException();
            }
            return "androidx.viewpager.widget.SeslViewPager";
        }

        @Override
        public void onRestorePendingState() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onAttachAdapter(@Nullable Adapter<?> newAdapter) {
            updatePageAccessibilityActions();
            if (newAdapter != null) {
                newAdapter.registerAdapterDataObserver(mAdapterDataObserver);
            }
        }

        @Override
        public void onDetachAdapter(@Nullable Adapter<?> oldAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
            }
        }

        @Override
        public void onSetOrientation() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onSetNewCurrentItem() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onSetUserInputEnabled() {
            updatePageAccessibilityActions();
            if (Build.VERSION.SDK_INT < 21) {
                sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
            }
        }

        @Override
        public void onSetLayoutDirection() {
            updatePageAccessibilityActions();
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
            addCollectionInfo(infoCompat);
            if (Build.VERSION.SDK_INT >= 16) {
                addScrollActions(infoCompat);
            }
        }

        @Override
        void onLmInitializeAccessibilityNodeInfoForItem(@NonNull View host,
                @NonNull AccessibilityNodeInfoCompat info) {
            addCollectionItemInfo(host, info);
        }

        @Override
        public boolean handlesPerformAccessibilityAction(int action, Bundle arguments) {
            return action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD || action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD;
        }

        @Override
        public boolean onPerformAccessibilityAction(int action, Bundle arguments) {
            if (!handlesPerformAccessibilityAction(action, arguments)) {
                throw new IllegalStateException();
            }

            int nextItem = (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD) ? getCurrentItem() - 1 : getCurrentItem() + 1;
            setCurrentItemFromAccessibilityCommand(nextItem);
            return true;
        }

        @Override
        public void onRvInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
            event.setSource(SeslViewPager2.this);
            event.setClassName(onGetAccessibilityClassName());
        }

        void setCurrentItemFromAccessibilityCommand(int item) {
            if (isUserInputEnabled()) {
                setCurrentItemInternal(item, true);
            }
        }

        void updatePageAccessibilityActions() {
            SeslViewPager2 viewPager = SeslViewPager2.this;

            @SuppressLint("InlinedApi")
            final int actionIdPageLeft = android.R.id.accessibilityActionPageLeft;
            @SuppressLint("InlinedApi")
            final int actionIdPageRight = android.R.id.accessibilityActionPageRight;
            @SuppressLint("InlinedApi")
            final int actionIdPageUp = android.R.id.accessibilityActionPageUp;
            @SuppressLint("InlinedApi")
            final int actionIdPageDown = android.R.id.accessibilityActionPageDown;

            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageLeft);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageRight);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageUp);
            ViewCompat.removeAccessibilityAction(viewPager, actionIdPageDown);

            if (getAdapter() == null) {
                return;
            }

            int itemCount = getAdapter().getItemCount();
            if (itemCount == 0) {
                return;
            }

            if (!isUserInputEnabled()) {
                return;
            }

            if (getOrientation() == ORIENTATION_HORIZONTAL) {
                boolean isLayoutRtl = isRtl();
                int actionIdPageForward = isLayoutRtl ? actionIdPageLeft : actionIdPageRight;
                int actionIdPageBackward = isLayoutRtl ? actionIdPageRight : actionIdPageLeft;

                if (mCurrentItem < itemCount - 1) {
                    ViewCompat.replaceAccessibilityAction(viewPager, new AccessibilityActionCompat(actionIdPageForward, null), null, mActionPageForward);
                }
                if (mCurrentItem > 0) {
                    ViewCompat.replaceAccessibilityAction(viewPager, new AccessibilityActionCompat(actionIdPageBackward, null), null, mActionPageBackward);
                }
            } else {
                if (mCurrentItem < itemCount - 1) {
                    ViewCompat.replaceAccessibilityAction(viewPager, new AccessibilityActionCompat(actionIdPageDown, null), null, mActionPageForward);
                }
                if (mCurrentItem > 0) {
                    ViewCompat.replaceAccessibilityAction(viewPager, new AccessibilityActionCompat(actionIdPageUp, null), null, mActionPageBackward);
                }
            }
        }

        private void addCollectionInfo(AccessibilityNodeInfoCompat infoCompat) {
            int rowCount = 0;
            int colCount = 0;
            if (getAdapter() != null) {
                if (getOrientation() == ORIENTATION_VERTICAL) {
                    rowCount = getAdapter().getItemCount();
                    colCount = 1;
                } else {
                    colCount = getAdapter().getItemCount();
                    rowCount = 1;
                }
            }
            AccessibilityNodeInfoCompat.CollectionInfoCompat collectionInfo = AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(rowCount, colCount, false, AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_NONE);
            infoCompat.setCollectionInfo(collectionInfo);
        }

        private void addCollectionItemInfo(View host, AccessibilityNodeInfoCompat infoCompat) {
            int rowIndex = (getOrientation() == ORIENTATION_VERTICAL) ? mLayoutManager.getPosition(host) : 0;
            int colIndex = (getOrientation() == ORIENTATION_HORIZONTAL) ? mLayoutManager.getPosition(host) : 0;
            AccessibilityNodeInfoCompat.CollectionItemInfoCompat collectionItemInfoCompat = AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(rowIndex, 1, colIndex, 1, false, false);
            infoCompat.setCollectionItemInfo(collectionItemInfoCompat);
        }

        private void addScrollActions(AccessibilityNodeInfoCompat infoCompat) {
            final Adapter<?> adapter = getAdapter();
            if (adapter == null) {
                return;
            }
            int itemCount = adapter.getItemCount();
            if (itemCount == 0 || !isUserInputEnabled()) {
                return;
            }
            if (mCurrentItem > 0) {
                infoCompat.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
            }
            if (mCurrentItem < itemCount - 1) {
                infoCompat.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
            }
            infoCompat.setScrollable(true);
        }
    }

    private abstract static class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            onChanged();
        }

        @Override
        public final void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    }
}
