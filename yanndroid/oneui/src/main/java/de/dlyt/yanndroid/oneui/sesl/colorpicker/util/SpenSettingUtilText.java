package de.dlyt.yanndroid.oneui.sesl.colorpicker.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.TextView;

public class SpenSettingUtilText {
    public static final String STYLE_CONDENSED_BOLD = "/system/fonts/RobotoCondensed-Bold.ttf";
    public static final String STYLE_MEDIUM = "/system/fonts/Roboto-Medium.ttf";
    public static final String STYLE_REGULAR = "/system/fonts/Roboto-Regular.ttf";
    public static final String ZIP_FILE_SEPARATOR = "/";
    private static final String FONT_SIZE = "font_size";
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static int mDeviceCorpCheck = -1;
    private static float mFontScale = -1.0f;
    private static int mLargeFontIndex = -1;

    public static void setTypeFace(Context context, String str, TextView... textViewArr) {
        Typeface fontTypeFace = getFontTypeFace(context, str);
        if (fontTypeFace != null) {
            for (TextView textView : textViewArr) {
                textView.setTypeface(fontTypeFace);
            }
        }
    }

    public static Typeface getFontTypeFace(Context context, String str) {
        return Typeface.DEFAULT;
    }

    public static void findMinValue(int i, TextView... textViewArr) {
        float[] fArr = new float[textViewArr.length];
        for (int i2 = 0; i2 < textViewArr.length; i2++) {
            fArr[i2] = textViewArr[i2].getTextSize();
        }
        float f = 0.0f;
        while (true) {
            int i3 = 0;
            for (int i4 = 0; i4 < textViewArr.length; i4++) {
                textViewArr[i4].measure(0, 0);
                i3 += textViewArr[i4].getMeasuredWidth();
            }
            if (i3 > i) {
                f += 1.0f;
                for (int i5 = 0; i5 < textViewArr.length; i5++) {
                    textViewArr[i5].setTextSize(0, fArr[i5] - f);
                }
            } else {
                return;
            }
        }
    }

    private static boolean isEllipsis(TextView textView) {
        return textView.getLayout() != null && textView.getLayout().getEllipsisCount(textView.getLineCount() - 1) > 0;
    }

    public static void resizeTextSize(TextView textView) {
        if (isEllipsis(textView)) {
            int ellipsizedWidth = textView.getLayout().getEllipsizedWidth() - 10;
            float textSize = textView.getTextSize();
            Paint paint = new Paint();
            Rect rect = new Rect();
            String charSequence = textView.getText().toString();
            paint.setTypeface(textView.getTypeface());
            do {
                textSize -= 1.0f;
                paint.setTextSize(textSize);
                paint.getTextBounds(charSequence, 0, charSequence.length(), rect);
            } while (rect.width() >= ellipsizedWidth);
            textView.setEllipsize(null);
            textView.setTextSize(0, textSize);
        }
    }

    public static void applyUpToLargeLevel(Context context, float f, TextView... textViewArr) {
        if (!(context == null || textViewArr == null)) {
            int i = 0;
            if (mDeviceCorpCheck == 0) {
                while (i < textViewArr.length) {
                    textViewArr[i].setTextSize(2, f);
                    i++;
                }
                return;
            }
            int i2 = Settings.Global.getInt(context.getContentResolver(), FONT_SIZE, 0);
            int largeFontIndex = getLargeFontIndex(context);
            if (i2 <= largeFontIndex) {
                while (i < textViewArr.length) {
                    textViewArr[i].setTextSize(2, f);
                    i++;
                }
                return;
            }
            float fontScale = getFontScale(context, largeFontIndex);
            for (TextView textView : textViewArr) {
                textView.setTextSize(0, f * fontScale * context.getResources().getDisplayMetrics().density);
            }
        }
    }

    private static int getLargeFontIndex(Context context) {
        int i = mLargeFontIndex;
        if (i > -1) {
            return i;
        }
        String[] systemFontIndex = getSystemFontIndex(context);
        String str = Build.VERSION.SDK_INT < 28 ? "Large" : "1.3";
        int i2 = 0;
        if (systemFontIndex != null) {
            mDeviceCorpCheck = 1;
            int length = systemFontIndex.length;
            while (true) {
                if (i2 >= length) {
                    break;
                } else if (str.equals(systemFontIndex[i2])) {
                    mLargeFontIndex = i2;
                    break;
                } else {
                    i2++;
                }
            }
            if (mLargeFontIndex == -1) {
                mLargeFontIndex = 4;
            }
        } else {
            mDeviceCorpCheck = 0;
        }
        return mLargeFontIndex;
    }

    private static String[] getSystemFontIndex(Context context) {
        if (Build.VERSION.SDK_INT < 28) {
            return getStringArrayFromPackage(context, SETTINGS_PACKAGE, "entry_7_step_font_size");
        }
        return getStringArrayFromPackage(context, SETTINGS_PACKAGE, "sec_entryvalues_8_step_font_size");
    }

    private static float getFontScale(Context context, int i) {
        float f = mFontScale;
        if (f > -1.0f) {
            return f;
        }
        String[] systemFontScale = getSystemFontScale(context);
        if (systemFontScale != null) {
            mDeviceCorpCheck = 1;
            if (i >= systemFontScale.length) {
                i = systemFontScale.length - 1;
            }
            mFontScale = Float.parseFloat(systemFontScale[i]);
            return mFontScale;
        }
        mDeviceCorpCheck = 0;
        mFontScale = 1.0f;
        return mFontScale;
    }

    private static String[] getSystemFontScale(Context context) {
        if (Build.VERSION.SDK_INT < 28) {
            return getStringArrayFromPackage(context, SETTINGS_PACKAGE, "entryvalues_7_step_font_size");
        }
        return getStringArrayFromPackage(context, SETTINGS_PACKAGE, "sec_entryvalues_8_step_font_size");
    }

    private static String[] getStringArrayFromPackage(Context context, String str, String str2) {
        int identifier;
        if (context != null && !TextUtils.isEmpty(str) && !TextUtils.isEmpty(str2)) {
            try {
                Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
                if (resourcesForApplication != null && (identifier = resourcesForApplication.getIdentifier(str2, "array", str)) > 0) {
                    return resourcesForApplication.getStringArray(identifier);
                }
            } catch (PackageManager.NameNotFoundException unused) {
            }
        }
        return null;
    }

    public static void adjustCharLineSeparation(TextView textView, int i) {
        if (textView != null && textView.getWidth() != 0 && textView.getText() != null) {
            TextPaint paint = textView.getPaint();
            String charSequence = textView.getText().toString();
            float width = (float) textView.getWidth();
            int breakText = paint.breakText(charSequence, true, width, null);
            String substring = charSequence.substring(0, breakText);
            int i2 = 1;
            while (true) {
                charSequence = charSequence.substring(breakText);
                if (charSequence.length() == 0) {
                    break;
                }
                i2++;
                if (i2 > i) {
                    substring = substring.substring(0, substring.length() - 2) + "...";
                    break;
                }
                breakText = paint.breakText(charSequence, true, width, null);
                substring = substring + "\n" + charSequence.substring(0, breakText);
            }
            textView.setText(substring);
        }
    }
}
