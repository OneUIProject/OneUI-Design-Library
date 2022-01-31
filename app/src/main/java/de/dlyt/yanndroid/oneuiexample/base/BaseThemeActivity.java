package de.dlyt.yanndroid.oneuiexample.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.utils.ThemeUtil;
import de.dlyt.yanndroid.oneuiexample.BuildConfig;
import de.dlyt.yanndroid.oneuiexample.R;

public class BaseThemeActivity extends AppCompatActivity {
    private static String SP_NAME = BuildConfig.APPLICATION_ID + "_preferences";
    private SharedPreferences sp;
    public boolean mUseAltTheme;
    public boolean mUseOUI4Theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mUseOUI4Theme = sp.getBoolean("use_oui4_theme", true);

        int normalTheme = mUseOUI4Theme ? R.style.OneUI4Theme : R.style.OneUI3Theme;
        int altTheme = mUseOUI4Theme ? R.style.OneUI4AboutTheme : R.style.OneUI3AboutTheme;
        setTheme(mUseAltTheme ? altTheme : normalTheme);
        new ThemeUtil(this);

        super.onCreate(savedInstanceState);

        int normalThemeNavBar = mUseOUI4Theme ? R.color.sesl4_round_and_bgcolor : R.color.sesl_round_and_bgcolor;
        int altThemeNavBar = R.color.splash_background;
        getWindow().setNavigationBarColor(getResources().getColor(mUseAltTheme ? altThemeNavBar : normalThemeNavBar));
    }

    protected void switchOUITheme() {
        sp.edit().putBoolean("use_oui4_theme", !mUseOUI4Theme).apply();
        recreate();
    }
}
