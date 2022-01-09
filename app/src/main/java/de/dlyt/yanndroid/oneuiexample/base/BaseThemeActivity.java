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
    public boolean mUseOUI4Theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        mUseOUI4Theme = sp.getBoolean("use_oui4_theme", true);

        setTheme(mUseOUI4Theme ? R.style.OneUI4Theme : R.style.OneUI3Theme);
        new ThemeUtil(this);

        super.onCreate(savedInstanceState);
    }

    protected void switchOUITheme() {
        sp.edit().putBoolean("use_oui4_theme", !mUseOUI4Theme).apply();
        recreate();
    }
}
