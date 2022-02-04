package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.res.TypedArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;

public abstract class PreferenceGroup extends Preference {
    private static final String TAG = "PreferenceGroup";
    @SuppressWarnings("WeakerAccess")
    final SimpleArrayMap<String, Long> mIdRecycleCache = new SimpleArrayMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final List<Preference> mPreferences;
    private boolean mOrderingAsAdded = true;
    private int mCurrentPreferenceOrder = 0;
    private boolean mAttachedToHierarchy = false;
    private int mInitialExpandedChildrenCount = Integer.MAX_VALUE;
    private OnExpandButtonClickListener mOnExpandButtonClickListener = null;

    private final Runnable mClearRecycleCacheRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mIdRecycleCache.clear();
            }
        }
    };

    @SuppressLint("RestrictedApi")
    public PreferenceGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mPreferences = new ArrayList<>();

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceGroup, defStyleAttr, defStyleRes);

        mOrderingAsAdded = TypedArrayUtils.getBoolean(a, R.styleable.PreferenceGroup_orderingFromXml, R.styleable.PreferenceGroup_orderingFromXml, true);

        if (a.hasValue(R.styleable.PreferenceGroup_initialExpandedChildrenCount)) {
            setInitialExpandedChildrenCount(TypedArrayUtils.getInt(a, R.styleable.PreferenceGroup_initialExpandedChildrenCount, R.styleable.PreferenceGroup_initialExpandedChildrenCount, Integer.MAX_VALUE));
        }
        a.recycle();
    }

    public PreferenceGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        mOrderingAsAdded = orderingAsAdded;
    }

    public boolean isOrderingAsAdded() {
        return mOrderingAsAdded;
    }

    public void setInitialExpandedChildrenCount(int expandedCount) {
        if (expandedCount != Integer.MAX_VALUE && !hasKey()) {
            Log.e(TAG, getClass().getSimpleName() + " should have a key defined if it contains an expandable preference");
        }
        mInitialExpandedChildrenCount = expandedCount;
    }

    public int getInitialExpandedChildrenCount() {
        return mInitialExpandedChildrenCount;
    }

    public void addItemFromInflater(@NonNull Preference preference) {
        addPreference(preference);
    }

    public int getPreferenceCount() {
        return mPreferences.size();
    }

    @NonNull
    public Preference getPreference(int index) {
        return mPreferences.get(index);
    }

    public boolean addPreference(@NonNull Preference preference) {
        if (mPreferences.contains(preference)) {
            return true;
        }
        if (preference.getKey() != null) {
            PreferenceGroup root = this;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            final String key = preference.getKey();
            if (root.findPreference(key) != null) {
                Log.e(TAG, "Found duplicated key: \"" + key + "\". This can cause unintended behaviour, please use unique keys for every preference.");
            }
        }

        if (preference.getOrder() == DEFAULT_ORDER) {
            if (mOrderingAsAdded) {
                preference.setOrder(mCurrentPreferenceOrder++);
            }

            if (preference instanceof PreferenceGroup) {
                ((PreferenceGroup) preference).setOrderingAsAdded(mOrderingAsAdded);
            }
        }

        int insertionIndex = Collections.binarySearch(mPreferences, preference);
        if (insertionIndex < 0) {
            insertionIndex = insertionIndex * -1 - 1;
        }

        if (!onPrepareAddPreference(preference)) {
            return false;
        }

        synchronized (this) {
            mPreferences.add(insertionIndex, preference);
        }

        final PreferenceManager preferenceManager = getPreferenceManager();
        final String key = preference.getKey();
        final long id;
        if (key != null && mIdRecycleCache.containsKey(key)) {
            id = mIdRecycleCache.get(key);
            mIdRecycleCache.remove(key);
        } else {
            id = preferenceManager.getNextId();
        }
        preference.onAttachedToHierarchy(preferenceManager, id);
        preference.assignParent(this);

        if (mAttachedToHierarchy) {
            preference.onAttached();
        }

        notifyHierarchyChanged();

        return true;
    }

    public boolean removePreference(@NonNull Preference preference) {
        final boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }

    public boolean removePreferenceRecursively(@NonNull CharSequence key) {
        final Preference preference = findPreference(key);
        if (preference == null) {
            return false;
        }
        return preference.getParent().removePreference(preference);
    }

    private boolean removePreferenceInt(@NonNull Preference preference) {
        synchronized (this) {
            preference.onPrepareForRemoval();
            if (preference.getParent() == this) {
                preference.assignParent(null);
            }
            boolean success = mPreferences.remove(preference);
            if (success) {
                final String key = preference.getKey();
                if (key != null) {
                    mIdRecycleCache.put(key, preference.getId());
                    mHandler.removeCallbacks(mClearRecycleCacheRunnable);
                    mHandler.post(mClearRecycleCacheRunnable);
                }
                if (mAttachedToHierarchy) {
                    preference.onDetached();
                }
            }

            return success;
        }
    }

    public void removeAll() {
        synchronized (this) {
            List<Preference> preferences = mPreferences;
            for (int i = preferences.size() - 1; i >= 0; i--) {
                removePreferenceInt(preferences.get(0));
            }
        }
        notifyHierarchyChanged();
    }

    protected boolean onPrepareAddPreference(@NonNull Preference preference) {
        preference.onParentChanged(this, shouldDisableDependents());
        return true;
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    @Nullable
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (TextUtils.equals(getKey(), key)) {
            return (T) this;
        }
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = getPreference(i);
            final String curKey = preference.getKey();

            if (TextUtils.equals(curKey, key)) {
                return (T) preference;
            }

            if (preference instanceof PreferenceGroup) {
                final T returnedPreference = ((PreferenceGroup) preference).findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }
        return null;
    }

    protected boolean isOnSameScreenAsChildren() {
        return true;
    }

    public boolean isAttached() {
        return mAttachedToHierarchy;
    }

    public void setOnExpandButtonClickListener(@Nullable OnExpandButtonClickListener onExpandButtonClickListener) {
        mOnExpandButtonClickListener = onExpandButtonClickListener;
    }

    @Nullable
    public OnExpandButtonClickListener getOnExpandButtonClickListener() {
        return mOnExpandButtonClickListener;
    }

    @Override
    public void onAttached() {
        super.onAttached();

        mAttachedToHierarchy = true;

        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttached();
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();

        mAttachedToHierarchy = false;

        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onDetached();
        }
    }

    @Override
    public void notifyDependencyChange(boolean disableDependents) {
        super.notifyDependencyChange(disableDependents);

        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onParentChanged(this, disableDependents);
        }
    }

    void sortPreferences() {
        synchronized (this) {
            Collections.sort(mPreferences);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(@NonNull Bundle container) {
        super.dispatchSaveInstanceState(container);

        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(@NonNull Bundle container) {
        super.dispatchRestoreInstanceState(container);

        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }

    @NonNull
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mInitialExpandedChildrenCount);
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState groupState = (SavedState) state;
        mInitialExpandedChildrenCount = groupState.mInitialExpandedChildrenCount;
        super.onRestoreInstanceState(groupState.getSuperState());
    }

    public interface PreferencePositionCallback {
        int getPreferenceAdapterPosition(@NonNull String key);

        int getPreferenceAdapterPosition(@NonNull Preference preference);
    }

    public interface OnExpandButtonClickListener {
        void onExpandButtonClick();
    }

    static class SavedState extends Preference.BaseSavedState {
        int mInitialExpandedChildrenCount;

        SavedState(Parcel source) {
            super(source);
            mInitialExpandedChildrenCount = source.readInt();
        }

        SavedState(Parcelable superState, int initialExpandedChildrenCount) {
            super(superState);
            mInitialExpandedChildrenCount = initialExpandedChildrenCount;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mInitialExpandedChildrenCount);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
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
}
