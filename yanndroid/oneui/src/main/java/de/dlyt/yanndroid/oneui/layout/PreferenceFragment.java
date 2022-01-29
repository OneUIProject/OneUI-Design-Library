package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.view.View;

import de.dlyt.yanndroid.oneui.preference.InsetPreferenceCategory;
import de.dlyt.yanndroid.oneui.preference.LayoutPreference;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceFragmentCompat;
import de.dlyt.yanndroid.oneui.preference.PreferenceGroup;
import de.dlyt.yanndroid.oneui.preference.internal.PreferencesRelatedCard;

public abstract class PreferenceFragment extends PreferenceFragmentCompat {
    private int mRelatedCardViewCount = 0;

    private LayoutPreference mFooter;

    public PreferencesRelatedCard createRelatedCard(Context context) {
        return new PreferencesRelatedCard(context);
    }

    protected PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
        for (int i = 0; i < groupToSearchIn.getPreferenceCount(); i++) {
            Preference child = groupToSearchIn.getPreference(i);

            if (child == preference)
                return groupToSearchIn;

            if (child instanceof PreferenceGroup) {
                PreferenceGroup childGroup = (PreferenceGroup) child;
                PreferenceGroup result = getParent(childGroup, preference);
                if (result != null)
                    return result;
            }
        }

        return null;
    }

    protected final Context getPrefContext() {
        return getPreferenceManager().getContext();
    }

    public LayoutPreference getFooterView() {
        return mFooter;
    }

    public void setFooterView(View view, boolean isRelativeLinkView) {
        if (isRelativeLinkView) {
            LayoutPreference layoutPreference = new LayoutPreference(getPrefContext(), view, isRelativeLinkView);
            mFooter = layoutPreference;
            layoutPreference.seslSetSubheaderRoundedBackground(0);

            InsetPreferenceCategory insetPreferenceCategory = new InsetPreferenceCategory(getContext());
            insetPreferenceCategory.setOrder(2147483645 - (mRelatedCardViewCount * 2));
            insetPreferenceCategory.seslSetSubheaderRoundedBackground(12);

            if (getPreferenceScreen() != null) {
                getPreferenceScreen().addPreference(insetPreferenceCategory);
            }
            if (getListView() != null) {
                getListView().seslSetLastRoundedCorner(false);
            }
        } else {
            mFooter = new LayoutPreference(getPrefContext(), view);
        }
        addPreferenceToBottom(mFooter);
    }

    public void setRelatedCardViewCount(int count) {
        mRelatedCardViewCount = count;
    }

    private void addPreferenceToBottom(LayoutPreference layoutPreference) {
        layoutPreference.setOrder(2147483646 - (mRelatedCardViewCount * 2));
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().addPreference(layoutPreference);
            setRelatedCardViewCount(mRelatedCardViewCount + 1);
        }
    }
}
