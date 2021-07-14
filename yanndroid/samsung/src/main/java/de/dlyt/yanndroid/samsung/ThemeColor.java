package de.dlyt.yanndroid.samsung;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.ColorInt;

public class ThemeColor {
    private static final String NAME = "ThemeColor", KEY = "color";

    @ColorInt
    private int color;

    public ThemeColor(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString(KEY, "0381fe");
        color = Color.parseColor("#" + stringColor);

        context.setTheme(context.getResources().getIdentifier("Color_" + stringColor, "style", context.getPackageName()));
    }

    public static void setColor(Activity activity, int red, int green, int blue) {
        red = Math.round(red / 15.0f) * 15;
        green = Math.round(green / 15.0f) * 15;
        blue = Math.round(blue / 15.0f) * 15;

        SharedPreferences.Editor editor = activity.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY, Integer.toHexString(Color.rgb(red, green, blue)).substring(2)).apply();

        activity.recreate();
    }

    public static void setColor(Activity activity, float red, float green, float blue) {
        setColor(activity, (int) (red * 255), (int) (green * 255), (int) (blue * 255));
    }

    public static void setColor(Activity activity, float[] hsv) {
        Color c = Color.valueOf(Color.HSVToColor(hsv));
        setColor(activity, (int) (c.red() * 255), (int) (c.green() * 255), (int) (c.blue() * 255));
    }

}