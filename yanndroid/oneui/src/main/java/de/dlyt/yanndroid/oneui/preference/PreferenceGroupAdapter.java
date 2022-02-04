package de.dlyt.yanndroid.oneui.preference;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.DiffUtil;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceViewHolder>
        implements Preference.OnPreferenceChangeInternalListener, PreferenceGroup.PreferencePositionCallback {
    private final PreferenceGroup mPreferenceGroup;
    private int mCategoryLayoutId = R.layout.sesl_preference_category;
    private boolean mIsCategoryAfter = false;
    private Preference mNextPreference = null;
    private Preference mNextGroupPreference = null;
    private final Handler mHandler;
    private List<Preference> mPreferences;
    private List<Preference> mVisiblePreferences;
    private final List<PreferenceResourceDescriptor> mPreferenceResourceDescriptors;

    private final Runnable mSyncRunnable = new Runnable() {
        @Override
        public void run() {
            updatePreferences();
        }
    };

    public PreferenceGroupAdapter(@NonNull PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mHandler = new Handler(Looper.getMainLooper());

        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferences = new ArrayList<>();
        mVisiblePreferences = new ArrayList<>();
        mPreferenceResourceDescriptors = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen) {
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).shouldUseGeneratedIds());
        } else {
            setHasStableIds(true);
        }
        updatePreferences();
    }

    @SuppressWarnings("WeakerAccess")
    void updatePreferences() {
        for (final Preference preference : mPreferences) {
            preference.setOnPreferenceChangeInternalListener(null);
        }
        final int size = mPreferences.size();
        mPreferences = new ArrayList<>(size);
        flattenPreferenceGroup(mPreferences, mPreferenceGroup);

        final List<Preference> oldVisibleList = mVisiblePreferences;

        final List<Preference> visiblePreferenceList = createVisiblePreferencesList(mPreferenceGroup);

        mVisiblePreferences = visiblePreferenceList;

        final PreferenceManager preferenceManager = mPreferenceGroup.getPreferenceManager();
        if (preferenceManager != null && preferenceManager.getPreferenceComparisonCallback() != null) {
            final PreferenceManager.PreferenceComparisonCallback comparisonCallback = preferenceManager.getPreferenceComparisonCallback();
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldVisibleList.size();
                }

                @Override
                public int getNewListSize() {
                    return visiblePreferenceList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return comparisonCallback.arePreferenceItemsTheSame(oldVisibleList.get(oldItemPosition), visiblePreferenceList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return comparisonCallback.arePreferenceContentsTheSame(oldVisibleList.get(oldItemPosition), visiblePreferenceList.get(newItemPosition));
                }
            });

            result.dispatchUpdatesTo(this);
        } else {
            notifyDataSetChanged();
        }

        for (final Preference preference : mPreferences) {
            preference.clearWasDetached();
        }
    }

    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup group) {
        group.sortPreferences();
        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            if (i == groupSize - 1) {
                mNextPreference = null;
                if (mIsCategoryAfter && preference == mNextGroupPreference) {
                    mNextGroupPreference = null;
                }
            } else {
                mNextPreference = group.getPreference(i + 1);
                if (preference == mNextGroupPreference) {
                    mNextGroupPreference = null;
                }
            }

            if (preference instanceof PreferenceCategory && !preference.mIsRoundChanged) {
                preference.seslSetSubheaderRoundedBackground(SeslRoundedCorner.ROUNDED_CORNER_ALL);
            }

            preferences.add(preference);

            if (preference instanceof PreferenceCategory && TextUtils.isEmpty(preference.getTitle()) && mCategoryLayoutId == preference.getLayoutResource()) {
                preference.setLayoutResource(R.layout.sesl_preference_category_empty);
            }

            final PreferenceResourceDescriptor descriptor = new PreferenceResourceDescriptor(preference);
            if (!mPreferenceResourceDescriptors.contains(descriptor)) {
                mPreferenceResourceDescriptors.add(descriptor);
            }

            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup nestedGroup = (PreferenceGroup) preference;
                if (nestedGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, nestedGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    private List<Preference> createVisiblePreferencesList(PreferenceGroup group) {
        int visiblePreferenceCount = 0;
        final List<Preference> visiblePreferences = new ArrayList<>();
        final List<Preference> collapsedPreferences = new ArrayList<>();

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            if (!preference.isVisible()) {
                continue;
            }

            if (!isGroupExpandable(group) || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                visiblePreferences.add(preference);
            } else {
                collapsedPreferences.add(preference);
            }

            if (!(preference instanceof PreferenceGroup)) {
                visiblePreferenceCount++;
                continue;
            }

            PreferenceGroup innerGroup = (PreferenceGroup) preference;
            if (!innerGroup.isOnSameScreenAsChildren()) {
                continue;
            }

            if (isGroupExpandable(group) && isGroupExpandable(innerGroup)) {
                throw new IllegalStateException("Nesting an expandable group inside of another expandable group is not supported!");
            }

            final List<Preference> innerList = createVisiblePreferencesList(innerGroup);

            for (Preference inner : innerList) {
                if (!isGroupExpandable(group) || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                    visiblePreferences.add(inner);
                } else {
                    collapsedPreferences.add(inner);
                }
                visiblePreferenceCount++;
            }
        }

        if (isGroupExpandable(group) && visiblePreferenceCount > group.getInitialExpandedChildrenCount()) {
            final ExpandButton expandButton = createExpandButton(group, collapsedPreferences);
            visiblePreferences.add(expandButton);
        }
        return visiblePreferences;
    }

    private ExpandButton createExpandButton(final PreferenceGroup group, List<Preference> collapsedPreferences) {
        final ExpandButton preference = new ExpandButton(group.getContext(), collapsedPreferences, group.getId());
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                group.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
                onPreferenceHierarchyChange(preference);
                final PreferenceGroup.OnExpandButtonClickListener listener = group.getOnExpandButtonClickListener();
                if (listener != null) {
                    listener.onExpandButtonClick();
                }
                return true;
            }
        });
        return preference;
    }

    private boolean isGroupExpandable(PreferenceGroup preferenceGroup) {
        return preferenceGroup.getInitialExpandedChildrenCount() != Integer.MAX_VALUE;
    }

    @Nullable
    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mVisiblePreferences.get(position);
    }

    @Override
    public int getItemCount() {
        return mVisiblePreferences.size();
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds()) {
            return RecyclerView.NO_ID;
        }
        return this.getItem(position).getId();
    }

    @Override
    public void onPreferenceChange(@NonNull Preference preference) {
        final int index = mVisiblePreferences.indexOf(preference);
        if (index != -1) {
            notifyItemChanged(index, preference);
        }
    }

    @Override
    public void onPreferenceHierarchyChange(@NonNull Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public void onPreferenceVisibilityChange(@NonNull Preference preference) {
        onPreferenceHierarchyChange(preference);
    }

    @Override
    public int getItemViewType(int position) {
        final Preference preference = this.getItem(position);

        PreferenceResourceDescriptor descriptor = new PreferenceResourceDescriptor(preference);

        int viewType = mPreferenceResourceDescriptors.indexOf(descriptor);
        if (viewType != -1) {
            return viewType;
        } else {
            viewType = mPreferenceResourceDescriptors.size();
            mPreferenceResourceDescriptors.add(descriptor);
            return viewType;
        }
    }

    @Override
    @NonNull
    public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final PreferenceResourceDescriptor descriptor = mPreferenceResourceDescriptors.get(viewType);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(descriptor.mLayoutResId, parent, false);

        final ViewGroup widgetFrame = view.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (descriptor.mWidgetLayoutResId != 0) {
                inflater.inflate(descriptor.mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }

        return new PreferenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        final Preference preference = getItem(position);
        preference.onBindViewHolder(holder);
    }

    @Override
    public int getPreferenceAdapterPosition(@NonNull String key) {
        final int size = mVisiblePreferences.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mVisiblePreferences.get(i);
            if (TextUtils.equals(key, candidate.getKey())) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getPreferenceAdapterPosition(@NonNull Preference preference) {
        final int size = mVisiblePreferences.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mVisiblePreferences.get(i);
            if (candidate != null && candidate.equals(preference)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private static class PreferenceResourceDescriptor {
        int mLayoutResId;
        int mWidgetLayoutResId;
        String mClassName;

        PreferenceResourceDescriptor(@NonNull Preference preference) {
            mClassName = preference.getClass().getName();
            mLayoutResId = preference.getLayoutResource();
            mWidgetLayoutResId = preference.getWidgetLayoutResource();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PreferenceResourceDescriptor)) {
                return false;
            }
            final PreferenceResourceDescriptor other = (PreferenceResourceDescriptor) o;
            return mLayoutResId == other.mLayoutResId && mWidgetLayoutResId == other.mWidgetLayoutResId && TextUtils.equals(mClassName, other.mClassName);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + mLayoutResId;
            result = 31 * result + mWidgetLayoutResId;
            result = 31 * result + mClassName.hashCode();
            return result;
        }
    }
}
