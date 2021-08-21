package de.dlyt.yanndroid.oneuiexample.fragments;

import android.os.Bundle;
import android.view.View;

import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceGroup;
import de.dlyt.yanndroid.oneui.preference.SeslPreferenceFragmentCompat;
import de.dlyt.yanndroid.oneui.preference.TipsCardViewPreference;
import de.dlyt.yanndroid.oneuiexample.R;

public class InnerPreferenceFragment extends SeslPreferenceFragmentCompat {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().seslSetGoToTopEnabled(true);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.inner_preferences);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final TipsCardViewPreference tipCard = (TipsCardViewPreference) findPreference("tip_card");
        final PreferenceGroup parent = getParent(getPreferenceScreen(), tipCard);
        tipCard.setTipsCardListener(new TipsCardViewPreference.TipsCardListener() {
            @Override
            public void onCancelClicked(View view) {
                if (parent != null) {
                    parent.removePreference(tipCard);
                }
            }
        });
    }

    protected PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
        for (int i = 0; i < groupToSearchIn.getPreferenceCount(); i++) {
            Preference child = groupToSearchIn.getPreference(i);

            if (child == preference)
                return groupToSearchIn;

            if (child instanceof PreferenceGroup) {
                PreferenceGroup childGroup = (PreferenceGroup)child;
                PreferenceGroup result = getParent(childGroup, preference);
                if (result != null)
                    return result;
            }
        }

        return null;
    }

}
