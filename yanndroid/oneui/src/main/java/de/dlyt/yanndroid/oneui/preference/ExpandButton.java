package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;

final class ExpandButton extends Preference {
    private long mId;

    ExpandButton(@NonNull Context context, List<Preference> collapsedPreferences, long parentId) {
        super(context);
        initLayout();
        setSummary(collapsedPreferences);
        mId = parentId + 1000000;
    }

    private void initLayout() {
        setLayoutResource(R.layout.expand_button);
        setIcon(R.drawable.ic_oui_arrow_down);
        setTitle(R.string.expand_button_title);
        setOrder(999);
    }

    private void setSummary(List<Preference> collapsedPreferences) {
        CharSequence summary = null;
        final List<PreferenceGroup> parents = new ArrayList<>();

        for (Preference preference : collapsedPreferences) {
            final CharSequence title = preference.getTitle();
            if (preference instanceof PreferenceGroup && !TextUtils.isEmpty(title)) {
                parents.add((PreferenceGroup) preference);
            }
            if (parents.contains(preference.getParent())) {
                if (preference instanceof PreferenceGroup) {
                    parents.add((PreferenceGroup) preference);
                }
                continue;
            }
            if (!TextUtils.isEmpty(title)) {
                if (summary == null) {
                    summary = title;
                } else {
                    summary = getContext().getString(R.string.summary_collapsed_preference_list, summary, title);
                }
            }
        }
        setSummary(summary);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
    }

    @Override
    long getId() {
        return mId;
    }
}
