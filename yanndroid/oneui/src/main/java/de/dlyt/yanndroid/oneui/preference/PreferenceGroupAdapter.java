package de.dlyt.yanndroid.oneui.preference;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.recyclerview.DiffUtil;
import de.dlyt.yanndroid.oneui.recyclerview.SeslRecyclerView;

public class PreferenceGroupAdapter extends SeslRecyclerView.Adapter<PreferenceViewHolder> implements Preference.OnPreferenceChangeInternalListener, PreferenceGroup.PreferencePositionCallback {
    boolean mIsCategoryAfter = false;
    Preference mNextGroupPreference = null;
    Preference mNextPreference = null;
    Preference mPrevPreference = null;
    private int mCategoryLayoutId = R.layout.sesl_preference_category;
    private Handler mHandler = new Handler();
    private PreferenceGroup mPreferenceGroup;
    private List<PreferenceLayout> mPreferenceLayouts;
    private List<Preference> mPreferenceList;
    private List<Preference> mPreferenceListInternal;
    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();
    private Runnable mSyncRunnable = new Runnable() {
        @Override
        public void run() {
            syncMyPreferences();
        }
    };

    public PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferenceList = new ArrayList<>();
        mPreferenceListInternal = new ArrayList<>();
        mPreferenceLayouts = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen) {
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).shouldUseGeneratedIds());
        } else {
            setHasStableIds(true);
        }

        syncMyPreferences();
    }

    private void syncMyPreferences() {
        for (final Preference preference : mPreferenceListInternal) {
            preference.setOnPreferenceChangeInternalListener(null);
        }
        final List<Preference> fullPreferenceList = new ArrayList<>(mPreferenceListInternal.size());
        flattenPreferenceGroup(fullPreferenceList, mPreferenceGroup);

        final List<Preference> visiblePreferenceList = new ArrayList<>(fullPreferenceList.size());
        for (final Preference preference : fullPreferenceList) {
            if (preference.isVisible()) {
                visiblePreferenceList.add(preference);
            }
        }

        final List<Preference> oldVisibleList = mPreferenceList;
        mPreferenceList = visiblePreferenceList;
        mPreferenceListInternal = fullPreferenceList;

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

        for (final Preference preference : fullPreferenceList) {
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

            if (preference instanceof PreferenceCategory) {
                if (!preference.mIsRoundChanged) {
                    preference.seslSetSubheaderRoundedBg(15);
                }
                preference.mIsSolidRoundedCorner = group.mIsSolidRoundedCorner;
                preference.seslSetSubheaderColor(group.mSubheaderColor);
            }
            preferences.add(preference);

            if ((preference instanceof PreferenceCategory) && TextUtils.isEmpty(preference.getTitle()) && mCategoryLayoutId == preference.getLayoutResource()) {
                preference.setLayoutResource(R.layout.sesl_preference_category_empty);
            }
            addPreferenceClassName(preference);

            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup preferenceAsGroup = (PreferenceGroup) preference;
                if (preferenceAsGroup.isOnSameScreenAsChildren()) {
                    mNextGroupPreference = mNextPreference;
                    flattenPreferenceGroup(preferences, preferenceAsGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    private PreferenceLayout createPreferenceLayout(Preference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private void addPreferenceClassName(Preference preference) {
        final PreferenceLayout pl = createPreferenceLayout(preference, null);
        if (!mPreferenceLayouts.contains(pl)) {
            mPreferenceLayouts.add(pl);
        }
    }

    @Override
    public int getItemCount() {
        return mPreferenceList.size();
    }

    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mPreferenceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds()) {
            return SeslRecyclerView.NO_ID;
        }
        return this.getItem(position).getId();
    }

    @Override
    public void onPreferenceChange(Preference preference) {
        final int index = mPreferenceList.indexOf(preference);
        if (index != -1) {
            notifyItemChanged(index, preference);
        }
    }

    @Override
    public void onPreferenceHierarchyChange(Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public int getItemViewType(int position) {
        final Preference preference = this.getItem(position);

        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout);

        int viewType = mPreferenceLayouts.indexOf(mTempPreferenceLayout);
        if (viewType != -1) {
            return viewType;
        } else {
            viewType = mPreferenceLayouts.size();
            mPreferenceLayouts.add(new PreferenceLayout(mTempPreferenceLayout));
            return viewType;
        }
    }

    public PreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final PreferenceLayout pl = mPreferenceLayouts.get(viewType);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View view = inflater.inflate(pl.resId, parent, false);

        final ViewGroup widgetFrame = (ViewGroup) view.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (pl.widgetResId != 0) {
                inflater.inflate(pl.widgetResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }

        return new PreferenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder, int position) {
        final Preference preference = getItem(position);
        preference.onBindViewHolder(holder);
    }

    @Override
    public int getPreferenceAdapterPosition(String key) {
        final int size = mPreferenceList.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mPreferenceList.get(i);
            if (TextUtils.equals(key, candidate.getKey())) {
                return i;
            }
        }
        return SeslRecyclerView.NO_POSITION;
    }

    @Override
    public int getPreferenceAdapterPosition(Preference preference) {
        final int size = mPreferenceList.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mPreferenceList.get(i);
            if (candidate != null && candidate.equals(preference)) {
                return i;
            }
        }
        return SeslRecyclerView.NO_POSITION;
    }


    private static class PreferenceLayout {
        private String name;
        private int resId;
        private int widgetResId;

        public PreferenceLayout() {
        }

        public PreferenceLayout(PreferenceLayout other) {
            resId = other.resId;
            widgetResId = other.widgetResId;
            name = other.name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PreferenceLayout)) {
                return false;
            }
            final PreferenceLayout other = (PreferenceLayout) o;
            return resId == other.resId
                    && widgetResId == other.widgetResId
                    && TextUtils.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + resId;
            result = 31 * result + widgetResId;
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
