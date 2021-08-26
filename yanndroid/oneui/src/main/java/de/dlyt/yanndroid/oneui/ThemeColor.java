package de.dlyt.yanndroid.oneui;

import android.app.UiModeManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeColor {
    public static final int DARK_MODE_AUTO = 2;
    public static final int DARK_MODE_DISABLED = 0;
    public static final int DARK_MODE_ENABLED = 1;

    private static final String NAME = "ThemeColor", KEY_COLOR = "color", KEY_DARK_MODE = "dark_mode";

    public ThemeColor(AppCompatActivity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        int darkMode = sharedPreferences.getInt(KEY_DARK_MODE, DARK_MODE_AUTO);
        String stringColor = sharedPreferences.getString(KEY_COLOR, "0381fe");

        if (darkMode != DARK_MODE_AUTO) {
            AppCompatDelegate.setDefaultNightMode(darkMode == DARK_MODE_ENABLED ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            activity.getDelegate().applyDayNight();
        }

        activity.setTheme(activity.getResources().getIdentifier("Color_" + stringColor, "style", activity.getPackageName()));
    }

    public static Configuration createDarkModeConfig(Context context, Configuration config) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        int darkMode = sharedPreferences.getInt(KEY_DARK_MODE, DARK_MODE_AUTO);

        int uiModeNight;
        if (darkMode == DARK_MODE_DISABLED) {
            uiModeNight = Configuration.UI_MODE_NIGHT_NO;
        } else if (darkMode == DARK_MODE_ENABLED) {
            uiModeNight = Configuration.UI_MODE_NIGHT_YES;
        } else {
            uiModeNight = ((UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE)).getCurrentModeType() & Configuration.UI_MODE_NIGHT_MASK;
        }

        Configuration newConfig = new Configuration(config);
        int newUiMode = newConfig.uiMode & -Configuration.UI_MODE_NIGHT_MASK | Configuration.UI_MODE_TYPE_NORMAL;
        newConfig.uiMode = newUiMode | uiModeNight;
        return newConfig;
    }

    public static ContextWrapper createDarkModeContextWrapper(Context context) {
        Configuration newConfig = createDarkModeConfig(context, context.getResources().getConfiguration());

        if (newConfig != null)
            return new ContextWrapper(context.createConfigurationContext(newConfig));
        else
            return new ContextWrapper(context);
    }

    public static void setDarkMode(AppCompatActivity activity, int mode) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_DARK_MODE, mode).apply();

        activity.recreate();
    }

    public static void setColor(AppCompatActivity activity, int red, int green, int blue) {
        red = Math.round(red / 15.0f) * 15;
        green = Math.round(green / 15.0f) * 15;
        blue = Math.round(blue / 15.0f) * 15;

        SharedPreferences.Editor editor = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_COLOR, Integer.toHexString(Color.rgb(red, green, blue)).substring(2)).apply();

        activity.recreate();
    }

    public static void setColor(AppCompatActivity activity, float red, float green, float blue) {
        setColor(activity, (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    public static void setColor(AppCompatActivity activity, float[] hsv) {
        int c = Color.HSVToColor(hsv);
        setColor(activity, Color.red(c) * 255, Color.green(c) * 255, Color.blue(c) * 255);
    }

}