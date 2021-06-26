package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class SpenColorPickerDataManager {
    private static final String KEY_RECENT_COLORS = "RECENT_COLORS_V52";
    private static final String RECENT_COLORS = "RECENT_COLORS";
    private static int RECENT_COLOR_BUTTON_MAX = 6;
    public static final String TAG = "SpenColorPickerDataManager";
    private ArrayList<SpenHSVColor> mColorTableSet = null;
    private boolean mIsLoaded = false;
    private final SharedPreferences mSharedPreferences;
    public static final String CACHE_VALUE_SEPARATOR = "-";


    public SpenColorPickerDataManager(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(context.getPackageName() + RECENT_COLORS, 0);
    }

    public void close() {
        if (this.mColorTableSet != null) {
            this.mColorTableSet = null;
        }
    }

    public boolean isLoadComplete() {
        return this.mIsLoaded;
    }

    public int getRecentColorCount() {
        if (isLoadComplete()) {
            return this.mColorTableSet.size();
        }
        return 0;
    }

    public boolean getRecentColor(int i, float[] fArr) {
        ArrayList<SpenHSVColor> arrayList;
        if (fArr == null || i < 0 || (arrayList = this.mColorTableSet) == null || i >= arrayList.size()) {
            return false;
        }
        return this.mColorTableSet.get(i).getHSV(fArr);
    }

    private ArrayList<Integer> getRecentColors() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (!this.mIsLoaded) {
            Log.i(TAG, "Need loadRecentColors() in advance.");
        } else {
            for (int i = 0; i < this.mColorTableSet.size(); i++) {
                arrayList.add(new Integer(this.mColorTableSet.get(i).getRGB()));
            }
        }
        return arrayList;
    }

    public void saveRecentColors(float[] fArr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(fArr[0]);
        stringBuffer.append(CACHE_VALUE_SEPARATOR);
        stringBuffer.append(fArr[1]);
        stringBuffer.append(CACHE_VALUE_SEPARATOR);
        stringBuffer.append(fArr[2]);
        stringBuffer.append(";");
        if (this.mColorTableSet != null) {
            int i = 0;
            while (true) {
                if (i >= this.mColorTableSet.size()) {
                    i = -1;
                    break;
                } else if (this.mColorTableSet.get(i).isSameColor(fArr)) {
                    break;
                } else {
                    i++;
                }
            }
            if (i > -1) {
                this.mColorTableSet.remove(i);
            }
            float[] fArr2 = new float[3];
            int i2 = 0;
            while (i2 < this.mColorTableSet.size() && i2 < RECENT_COLOR_BUTTON_MAX - 1) {
                this.mColorTableSet.get(i2).getHSV(fArr2);
                stringBuffer.append(fArr2[0]);
                stringBuffer.append(CACHE_VALUE_SEPARATOR);
                stringBuffer.append(fArr2[1]);
                stringBuffer.append(CACHE_VALUE_SEPARATOR);
                stringBuffer.append(fArr2[2]);
                stringBuffer.append(";");
                i2++;
            }
        }
        SharedPreferences.Editor edit = this.mSharedPreferences.edit();
        edit.putString(KEY_RECENT_COLORS, stringBuffer.toString());
        edit.commit();
    }

    public ArrayList<Integer> loadRecentColors() {
        if (loadRecentColors_52()) {
            Log.i(TAG, "loadRecentColors() - v52");
            this.mIsLoaded = true;
        } else if (loadRecentColors_51()) {
            Log.i(TAG, "loadRecentColors() - v51");
            this.mIsLoaded = true;
        } else {
            Log.i(TAG, "loadRecentColors() - not exist recent colors");
        }
        return getRecentColors();
    }

    private boolean loadRecentColors_51() {
        String string = this.mSharedPreferences.getString(RECENT_COLORS, "");
        if (string.isEmpty()) {
            return false;
        }
        String[] split = string.split(" ");
        int length = split.length;
        int i = RECENT_COLOR_BUTTON_MAX;
        if (length < i) {
            i = split.length;
        }
        if (i > 0 && this.mColorTableSet == null) {
            this.mColorTableSet = new ArrayList<>();
        }
        for (int i2 = 0; i2 < i; i2++) {
            int parseInt = Integer.parseInt(split[i2]);
            if (parseInt != 0) {
                this.mColorTableSet.add(new SpenHSVColor(parseInt));
            }
        }
        return true;
    }

    private boolean loadRecentColors_52() {
        String string = this.mSharedPreferences.getString(KEY_RECENT_COLORS, "");
        if (string.isEmpty()) {
            return false;
        }
        String[] split = string.split(";");
        int length = split.length;
        int i = RECENT_COLOR_BUTTON_MAX;
        if (length < i) {
            i = split.length;
        }
        if (i <= 0 || this.mColorTableSet != null) {
            this.mColorTableSet.clear();
        } else {
            this.mColorTableSet = new ArrayList<>();
        }
        for (int i2 = 0; i2 < i; i2++) {
            float[] fArr = new float[3];
            String[] split2 = split[i2].split(CACHE_VALUE_SEPARATOR);
            if (split2.length == 3) {
                fArr[0] = Float.valueOf(split2[0]).floatValue();
                fArr[1] = Float.valueOf(split2[1]).floatValue();
                fArr[2] = Float.valueOf(split2[2]).floatValue();
                this.mColorTableSet.add(new SpenHSVColor(fArr));
            }
        }
        return true;
    }
}
