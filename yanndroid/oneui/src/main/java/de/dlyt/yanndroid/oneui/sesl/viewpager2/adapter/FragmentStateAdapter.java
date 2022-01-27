package de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter;

import static androidx.core.util.Preconditions.checkArgument;
import static androidx.lifecycle.Lifecycle.State.RESUMED;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static de.dlyt.yanndroid.oneui.view.RecyclerView.NO_ID;
import static de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter.FragmentStateAdapter.FragmentTransactionCallback.OnPostEventListener;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.collection.LongSparseArray;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import de.dlyt.yanndroid.oneui.sesl.viewpager2.widget.SeslViewPager2;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public abstract class FragmentStateAdapter extends RecyclerView.Adapter<FragmentViewHolder> implements StatefulAdapter {
    private static final String KEY_PREFIX_FRAGMENT = "f#";
    private static final String KEY_PREFIX_STATE = "s#";
    private static final long GRACE_WINDOW_TIME_MS = 10_000;
    @SuppressWarnings("WeakerAccess")
    final Lifecycle mLifecycle;
    @SuppressWarnings("WeakerAccess")
    final FragmentManager mFragmentManager;
    @SuppressWarnings("WeakerAccess")
    final LongSparseArray<Fragment> mFragments = new LongSparseArray<>();
    private final LongSparseArray<Fragment.SavedState> mSavedStates = new LongSparseArray<>();
    private final LongSparseArray<Integer> mItemIdToViewHolder = new LongSparseArray<>();
    private FragmentMaxLifecycleEnforcer mFragmentMaxLifecycleEnforcer;
    @SuppressWarnings("WeakerAccess")
    FragmentEventDispatcher mFragmentEventDispatcher = new FragmentEventDispatcher();
    @SuppressWarnings("WeakerAccess")
    boolean mIsInGracePeriod = false;
    private boolean mHasStaleFragments = false;

    public FragmentStateAdapter(@NonNull FragmentActivity fragmentActivity) {
        this(fragmentActivity.getSupportFragmentManager(), fragmentActivity.getLifecycle());
    }

    public FragmentStateAdapter(@NonNull Fragment fragment) {
        this(fragment.getChildFragmentManager(), fragment.getLifecycle());
    }

    public FragmentStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        mFragmentManager = fragmentManager;
        mLifecycle = lifecycle;
        super.setHasStableIds(true);
    }

    @SuppressLint("RestrictedApi")
    @CallSuper
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        checkArgument(mFragmentMaxLifecycleEnforcer == null);
        mFragmentMaxLifecycleEnforcer = new FragmentMaxLifecycleEnforcer();
        mFragmentMaxLifecycleEnforcer.register(recyclerView);
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mFragmentMaxLifecycleEnforcer.unregister(recyclerView);
        mFragmentMaxLifecycleEnforcer = null;
    }

    public abstract @NonNull Fragment createFragment(int position);

    @NonNull
    @Override
    public final FragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return FragmentViewHolder.create(parent);
    }

    @Override
    public final void onBindViewHolder(final @NonNull FragmentViewHolder holder, int position) {
        final long itemId = holder.getItemId();
        final int viewHolderId = holder.getContainer().getId();
        final Long boundItemId = itemForViewHolder(viewHolderId);
        if (boundItemId != null && boundItemId != itemId) {
            removeFragment(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }

        mItemIdToViewHolder.put(itemId, viewHolderId);
        ensureFragment(position);

        final FrameLayout container = holder.getContainer();
        if (ViewCompat.isAttachedToWindow(container)) {
            if (container.getParent() != null) {
                throw new IllegalStateException("Design assumption violated.");
            }
            container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (container.getParent() != null) {
                        container.removeOnLayoutChangeListener(this);
                        placeFragmentInViewHolder(holder);
                    }
                }
            });
        }

        gcFragments();
    }

    @SuppressWarnings("WeakerAccess")
    void gcFragments() {
        if (!mHasStaleFragments || shouldDelayFragmentTransactions()) {
            return;
        }

        Set<Long> toRemove = new ArraySet<>();
        for (int ix = 0; ix < mFragments.size(); ix++) {
            long itemId = mFragments.keyAt(ix);
            if (!containsItem(itemId)) {
                toRemove.add(itemId);
                mItemIdToViewHolder.remove(itemId);
            }
        }

        if (!mIsInGracePeriod) {
            mHasStaleFragments = false;

            for (int ix = 0; ix < mFragments.size(); ix++) {
                long itemId = mFragments.keyAt(ix);
                if (!isFragmentViewBound(itemId)) {
                    toRemove.add(itemId);
                }
            }
        }

        for (Long itemId : toRemove) {
            removeFragment(itemId);
        }
    }

    private boolean isFragmentViewBound(long itemId) {
        if (mItemIdToViewHolder.containsKey(itemId)) {
            return true;
        }

        Fragment fragment = mFragments.get(itemId);
        if (fragment == null) {
            return false;
        }

        View view = fragment.getView();
        if (view == null) {
            return false;
        }

        return view.getParent() != null;
    }

    private Long itemForViewHolder(int viewHolderId) {
        Long boundItemId = null;
        for (int ix = 0; ix < mItemIdToViewHolder.size(); ix++) {
            if (mItemIdToViewHolder.valueAt(ix) == viewHolderId) {
                if (boundItemId != null) {
                    throw new IllegalStateException("Design assumption violated: a ViewHolder can only be bound to one item at a time.");
                }
                boundItemId = mItemIdToViewHolder.keyAt(ix);
            }
        }
        return boundItemId;
    }

    private void ensureFragment(int position) {
        long itemId = getItemId(position);
        if (!mFragments.containsKey(itemId)) {
            Fragment newFragment = createFragment(position);
            newFragment.setInitialSavedState(mSavedStates.get(itemId));
            mFragments.put(itemId, newFragment);
        }
    }

    @Override
    public final void onViewAttachedToWindow(@NonNull final FragmentViewHolder holder) {
        placeFragmentInViewHolder(holder);
        gcFragments();
    }

    @SuppressWarnings("WeakerAccess")
    void placeFragmentInViewHolder(@NonNull final FragmentViewHolder holder) {
        Fragment fragment = mFragments.get(holder.getItemId());
        if (fragment == null) {
            throw new IllegalStateException("Design assumption violated.");
        }
        FrameLayout container = holder.getContainer();
        View view = fragment.getView();

        if (!fragment.isAdded() && view != null) {
            throw new IllegalStateException("Design assumption violated.");
        }

        if (fragment.isAdded() && view == null) {
            scheduleViewAttach(fragment, container);
            return;
        }

        if (fragment.isAdded() && view.getParent() != null) {
            if (view.getParent() != container) {
                addViewToContainer(view, container);
            }
            return;
        }

        if (fragment.isAdded()) {
            addViewToContainer(view, container);
            return;
        }

        if (!shouldDelayFragmentTransactions()) {
            scheduleViewAttach(fragment, container);
            List<OnPostEventListener> onPost = mFragmentEventDispatcher.dispatchPreAdded(fragment);
            try {
                fragment.setMenuVisibility(false);
                mFragmentManager.beginTransaction().add(fragment, "f" + holder.getItemId()).setMaxLifecycle(fragment, STARTED).commitNow();
                mFragmentMaxLifecycleEnforcer.updateFragmentMaxLifecycle(false);
            } finally {
                mFragmentEventDispatcher.dispatchPostEvents(onPost);
            }
        } else {
            if (mFragmentManager.isDestroyed()) {
                return;
            }
            mLifecycle.addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (shouldDelayFragmentTransactions()) {
                        return;
                    }
                    source.getLifecycle().removeObserver(this);
                    if (ViewCompat.isAttachedToWindow(holder.getContainer())) {
                        placeFragmentInViewHolder(holder);
                    }
                }
            });
        }
    }

    private void scheduleViewAttach(final Fragment fragment, @NonNull final FrameLayout container) {
        mFragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @SuppressWarnings("ReferenceEquality")
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                if (f == fragment) {
                    fm.unregisterFragmentLifecycleCallbacks(this);
                    addViewToContainer(v, container);
                }
            }
        }, false);
    }

    @SuppressWarnings("WeakerAccess")
    void addViewToContainer(@NonNull View v, @NonNull FrameLayout container) {
        if (container.getChildCount() > 1) {
            throw new IllegalStateException("Design assumption violated.");
        }

        if (v.getParent() == container) {
            return;
        }

        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        if (v.getParent() != null) {
            ((ViewGroup) v.getParent()).removeView(v);
        }

        container.addView(v);
    }

    @Override
    public final void onViewRecycled(@NonNull FragmentViewHolder holder) {
        final int viewHolderId = holder.getContainer().getId();
        final Long boundItemId = itemForViewHolder(viewHolderId);
        if (boundItemId != null) {
            removeFragment(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }
    }

    @Override
    public final boolean onFailedToRecycleView(@NonNull FragmentViewHolder holder) {
        return true;
    }

    private void removeFragment(long itemId) {
        Fragment fragment = mFragments.get(itemId);

        if (fragment == null) {
            return;
        }

        if (fragment.getView() != null) {
            ViewParent viewParent = fragment.getView().getParent();
            if (viewParent != null) {
                ((FrameLayout) viewParent).removeAllViews();
            }
        }

        if (!containsItem(itemId)) {
            mSavedStates.remove(itemId);
        }

        if (!fragment.isAdded()) {
            mFragments.remove(itemId);
            return;
        }

        if (shouldDelayFragmentTransactions()) {
            mHasStaleFragments = true;
            return;
        }

        if (fragment.isAdded() && containsItem(itemId)) {
            mSavedStates.put(itemId, mFragmentManager.saveFragmentInstanceState(fragment));
        }
        List<OnPostEventListener> onPost = mFragmentEventDispatcher.dispatchPreRemoved(fragment);
        try {
            mFragmentManager.beginTransaction().remove(fragment).commitNow();
            mFragments.remove(itemId);
        } finally {
            mFragmentEventDispatcher.dispatchPostEvents(onPost);
        }
    }

    @SuppressWarnings("WeakerAccess")
    boolean shouldDelayFragmentTransactions() {
        return mFragmentManager.isStateSaved();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < getItemCount();
    }

    @Override
    public final void setHasStableIds(boolean hasStableIds) {
        throw new UnsupportedOperationException("Stable Ids are required for the adapter to function properly, and the adapter takes care of setting the flag.");
    }

    @Override
    public final @NonNull Parcelable saveState() {
        Bundle savedState = new Bundle(mFragments.size() + mSavedStates.size());

        for (int ix = 0; ix < mFragments.size(); ix++) {
            long itemId = mFragments.keyAt(ix);
            Fragment fragment = mFragments.get(itemId);
            if (fragment != null && fragment.isAdded()) {
                String key = createKey(KEY_PREFIX_FRAGMENT, itemId);
                mFragmentManager.putFragment(savedState, key, fragment);
            }
        }

        for (int ix = 0; ix < mSavedStates.size(); ix++) {
            long itemId = mSavedStates.keyAt(ix);
            if (containsItem(itemId)) {
                String key = createKey(KEY_PREFIX_STATE, itemId);
                savedState.putParcelable(key, mSavedStates.get(itemId));
            }
        }

        return savedState;
    }

    @Override
    public final void restoreState(@NonNull Parcelable savedState) {
        if (!mSavedStates.isEmpty() || !mFragments.isEmpty()) {
            throw new IllegalStateException("Expected the adapter to be 'fresh' while restoring state.");
        }

        Bundle bundle = (Bundle) savedState;
        if (bundle.getClassLoader() == null) {
            bundle.setClassLoader(getClass().getClassLoader());
        }

        for (String key : bundle.keySet()) {
            if (isValidKey(key, KEY_PREFIX_FRAGMENT)) {
                long itemId = parseIdFromKey(key, KEY_PREFIX_FRAGMENT);
                Fragment fragment = mFragmentManager.getFragment(bundle, key);
                mFragments.put(itemId, fragment);
                continue;
            }

            if (isValidKey(key, KEY_PREFIX_STATE)) {
                long itemId = parseIdFromKey(key, KEY_PREFIX_STATE);
                Fragment.SavedState state = bundle.getParcelable(key);
                if (containsItem(itemId)) {
                    mSavedStates.put(itemId, state);
                }
                continue;
            }

            throw new IllegalArgumentException("Unexpected key in savedState: " + key);
        }

        if (!mFragments.isEmpty()) {
            mHasStaleFragments = true;
            mIsInGracePeriod = true;
            gcFragments();
            scheduleGracePeriodEnd();
        }
    }

    private void scheduleGracePeriodEnd() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mIsInGracePeriod = false;
                gcFragments();
            }
        };

        mLifecycle.addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    handler.removeCallbacks(runnable);
                    source.getLifecycle().removeObserver(this);
                }
            }
        });

        handler.postDelayed(runnable, GRACE_WINDOW_TIME_MS);
    }

    private static @NonNull String createKey(@NonNull String prefix, long id) {
        return prefix + id;
    }

    private static boolean isValidKey(@NonNull String key, @NonNull String prefix) {
        return key.startsWith(prefix) && key.length() > prefix.length();
    }

    private static long parseIdFromKey(@NonNull String key, @NonNull String prefix) {
        return Long.parseLong(key.substring(prefix.length()));
    }

    class FragmentMaxLifecycleEnforcer {
        private SeslViewPager2.OnPageChangeCallback mPageChangeCallback;
        private RecyclerView.AdapterDataObserver mDataObserver;
        private LifecycleEventObserver mLifecycleObserver;
        private SeslViewPager2 mViewPager;

        private long mPrimaryItemId = NO_ID;

        void register(@NonNull RecyclerView recyclerView) {
            mViewPager = inferViewPager(recyclerView);

            mPageChangeCallback = new SeslViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    updateFragmentMaxLifecycle(false);
                }

                @Override
                public void onPageSelected(int position) {
                    updateFragmentMaxLifecycle(false);
                }
            };
            mViewPager.registerOnPageChangeCallback(mPageChangeCallback);

            mDataObserver = new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    updateFragmentMaxLifecycle(true);
                }
            };
            registerAdapterDataObserver(mDataObserver);

            mLifecycleObserver = new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    updateFragmentMaxLifecycle(false);
                }
            };
            mLifecycle.addObserver(mLifecycleObserver);
        }

        void unregister(@NonNull RecyclerView recyclerView) {
            SeslViewPager2 viewPager = inferViewPager(recyclerView);
            viewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
            unregisterAdapterDataObserver(mDataObserver);
            mLifecycle.removeObserver(mLifecycleObserver);
            mViewPager = null;
        }

        void updateFragmentMaxLifecycle(boolean dataSetChanged) {
            if (shouldDelayFragmentTransactions()) {
                return;
            }

            if (mViewPager.getScrollState() != SeslViewPager2.SCROLL_STATE_IDLE) {
                return;
            }

            if (mFragments.isEmpty() || getItemCount() == 0) {
                return;
            }

            final int currentItem = mViewPager.getCurrentItem();
            if (currentItem >= getItemCount()) {
                return;
            }

            long currentItemId = getItemId(currentItem);
            if (currentItemId == mPrimaryItemId && !dataSetChanged) {
                return;
            }

            Fragment currentItemFragment = mFragments.get(currentItemId);
            if (currentItemFragment == null || !currentItemFragment.isAdded()) {
                return;
            }

            mPrimaryItemId = currentItemId;
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            Fragment toResume = null;
            List<List<OnPostEventListener>> onPost = new ArrayList<>();
            for (int ix = 0; ix < mFragments.size(); ix++) {
                long itemId = mFragments.keyAt(ix);
                Fragment fragment = mFragments.valueAt(ix);

                if (!fragment.isAdded()) {
                    continue;
                }

                if (itemId != mPrimaryItemId) {
                    transaction.setMaxLifecycle(fragment, STARTED);
                    onPost.add(mFragmentEventDispatcher.dispatchMaxLifecyclePreUpdated(fragment, STARTED));
                } else {
                    toResume = fragment;
                }

                fragment.setMenuVisibility(itemId == mPrimaryItemId);
            }
            if (toResume != null) {
                transaction.setMaxLifecycle(toResume, RESUMED);
                onPost.add(mFragmentEventDispatcher.dispatchMaxLifecyclePreUpdated(toResume, RESUMED));
            }

            if (!transaction.isEmpty()) {
                transaction.commitNow();
                Collections.reverse(onPost);
                for (List<OnPostEventListener> event : onPost) {
                    mFragmentEventDispatcher.dispatchPostEvents(event);
                }
            }
        }
        @NonNull
        private SeslViewPager2 inferViewPager(@NonNull RecyclerView recyclerView) {
            ViewParent parent = recyclerView.getParent();
            if (parent instanceof SeslViewPager2) {
                return (SeslViewPager2) parent;
            }
            throw new IllegalStateException("Expected ViewPager2 instance. Got: " + parent);
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

    @SuppressWarnings("WeakerAccess")
    static class FragmentEventDispatcher {
        private List<FragmentTransactionCallback> mCallbacks = new CopyOnWriteArrayList<>();

        public void registerCallback(FragmentTransactionCallback callback) {
            mCallbacks.add(callback);
        }

        public void unregisterCallback(FragmentTransactionCallback callback) {
            mCallbacks.remove(callback);
        }

        public List<OnPostEventListener> dispatchMaxLifecyclePreUpdated(Fragment fragment, Lifecycle.State maxState) {
            List<OnPostEventListener> result = new ArrayList<>();
            for (FragmentTransactionCallback callback : mCallbacks) {
                result.add(callback.onFragmentMaxLifecyclePreUpdated(fragment, maxState));
            }
            return result;
        }

        public void dispatchPostEvents(List<OnPostEventListener> entries) {
            for (OnPostEventListener entry : entries) {
                entry.onPost();
            }
        }

        public List<OnPostEventListener> dispatchPreAdded(Fragment fragment) {
            List<OnPostEventListener> result = new ArrayList<>();
            for (FragmentTransactionCallback callback : mCallbacks) {
                result.add(callback.onFragmentPreAdded(fragment));
            }
            return result;
        }

        public List<OnPostEventListener> dispatchPreRemoved(Fragment fragment) {
            List<OnPostEventListener> result = new ArrayList<>();
            for (FragmentTransactionCallback callback : mCallbacks) {
                result.add(callback.onFragmentPreRemoved(fragment));
            }
            return result;
        }
    }

    public abstract static class FragmentTransactionCallback {
        private static final @NonNull OnPostEventListener NO_OP = new OnPostEventListener() {
            @Override
            public void onPost() {
            }
        };

        @NonNull
        public OnPostEventListener onFragmentPreAdded(@NonNull Fragment fragment) {
            return NO_OP;
        }

        @NonNull
        public OnPostEventListener onFragmentPreRemoved(@NonNull Fragment fragment) {
            return NO_OP;
        }

        @NonNull
        public OnPostEventListener onFragmentMaxLifecyclePreUpdated(@NonNull Fragment fragment, @NonNull Lifecycle.State maxLifecycleState) {
            return NO_OP;
        }

        public interface OnPostEventListener {
            void onPost();
        }
    }

    public void registerFragmentTransactionCallback(@NonNull FragmentTransactionCallback callback) {
        mFragmentEventDispatcher.registerCallback(callback);
    }

    public void unregisterFragmentTransactionCallback(@NonNull FragmentTransactionCallback callback) {
        mFragmentEventDispatcher.unregisterCallback(callback);
    }
}
