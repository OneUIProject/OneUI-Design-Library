package de.dlyt.yanndroid.oneuiexample.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import de.dlyt.yanndroid.oneui.SamsungPreferenceFragment;
import de.dlyt.yanndroid.oneui.preference.SwitchPreferenceScreen;
import de.dlyt.yanndroid.oneui.preference.internal.PreferencesRelatedCard;
import de.dlyt.yanndroid.oneuiexample.MainActivity;
import de.dlyt.yanndroid.oneuiexample.R;

import androidx.appcompat.util.SeslMisc;

import de.dlyt.yanndroid.oneui.ThemeColor;
import de.dlyt.yanndroid.oneui.preference.HorizontalRadioPreference;
import de.dlyt.yanndroid.oneui.preference.Preference;
import de.dlyt.yanndroid.oneui.preference.PreferenceGroup;
import de.dlyt.yanndroid.oneui.preference.SwitchPreference;
import de.dlyt.yanndroid.oneui.preference.TipsCardViewPreference;

public class MainActivitySecondFragment extends SamsungPreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private long mLastClickTime = 0L;
    private MainActivity mActivity;
    private Context mContext;
    private PreferencesRelatedCard mRelatedCard;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof MainActivity)
            mActivity = ((MainActivity) getActivity());
        mContext = getContext();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(R.xml.inner_preferences);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        int darkMode = ThemeColor.getDarkMode(mContext);

        TipsCardViewPreference tipCard = (TipsCardViewPreference) findPreference("tip_card");
        PreferenceGroup parent = getParent(getPreferenceScreen(), tipCard);
        tipCard.setTipsCardListener(new TipsCardViewPreference.TipsCardListener() {
            @Override
            public void onCancelClicked(View view) {
                if (parent != null) {
                    parent.removePreference(tipCard);
                    parent.removePreference(findPreference("spacing"));
                }
            }
        });

        HorizontalRadioPreference darkModePref = (HorizontalRadioPreference) findPreference("dark_mode");
        darkModePref.setOnPreferenceChangeListener(this);
        darkModePref.setDividerEnabled(false);
        darkModePref.setTouchEffectEnabled(false);
        darkModePref.setEnabled(darkMode != ThemeColor.DARK_MODE_AUTO);
        darkModePref.setValue(SeslMisc.isLightTheme(mContext) ? "0" : "1");

        SwitchPreference autoDarkModePref = (SwitchPreference) findPreference("dark_mode_auto");
        autoDarkModePref.setOnPreferenceChangeListener(this);
        autoDarkModePref.setChecked(darkMode == ThemeColor.DARK_MODE_AUTO);

        SwitchPreferenceScreen switchPreferenceScreen = (SwitchPreferenceScreen) findPreference("switch_preference_screen");
        switchPreferenceScreen.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 600L) {
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (preference.getKey()) {
            case "switch_preference_screen":
                Toast.makeText(mContext, "onPreferenceClick", Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String currentDarkMode = String.valueOf(ThemeColor.getDarkMode(mContext));
        HorizontalRadioPreference darkModePref = (HorizontalRadioPreference) findPreference("dark_mode");

        switch (preference.getKey()) {
            case "dark_mode":
                if (currentDarkMode != newValue) {
                    ThemeColor.setDarkMode(mActivity, ((String) newValue).equals("0") ? ThemeColor.DARK_MODE_DISABLED : ThemeColor.DARK_MODE_ENABLED);
                }
                return true;
            case "dark_mode_auto":
                if ((boolean) newValue) {
                    darkModePref.setEnabled(false);
                    ThemeColor.setDarkMode(mActivity, ThemeColor.DARK_MODE_AUTO);
                } else {
                    darkModePref.setEnabled(true);
                }
                return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        setRelatedCardView();
        super.onResume();
    }

    private void setRelatedCardView() {
        if (mRelatedCard == null) {
            createRelatedCard(mContext)
                    .addButton("This", null)
                    .addButton("That", null)
                    .addButton("There", null)
                    .show(this);
        }
    }
}

