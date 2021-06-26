package com.samsung.android.sdk.pen.util.color;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import java.util.HashMap;

public class SpenReverseColorTheme implements SpenIColorTheme {
    private static final float STANDARD_AREA_MAX = 0.6f;
    private static final float STANDARD_AREA_MIN = 0.4f;
    private static final int TABLE_LIST_SIZE = 21;
    private static final String TAG = "SpenReverseUIColorTheme";
    public final int SCOPE_ALL = 8;
    public final int SCOPE_PALETTE = 2;
    public final int SCOPE_PICKER = 1;
    public final int SCOPE_REVERSER_LIGHT = 8;
    private HashMap<Integer, Integer> mPaletteColorHash = new HashMap<>();
    private HashMap<Integer, Integer> mPickerColorHash = new HashMap<>();
    private int mSearchScope = 8;

    public SpenReverseColorTheme(Context context) {
        initPickerHash(context);
        initPaletteHash(context);
    }

    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public void close() {
        this.mPickerColorHash = null;
        this.mPaletteColorHash = null;
    }

    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public int getColor(int i) {
        int i2;
        boolean z;
        int findColor;
        int findColor2;
        int i3 = (16777215 & i) | -16777216;
        int i4 = (i >> 24) & 255;
        if (!containsScope(2) || (findColor2 = findColor(this.mPaletteColorHash, i4, i3)) == -1) {
            i2 = i;
            z = false;
        } else {
            Log.i(TAG, "getColor() :: Find in Palette!");
            i2 = findColor2;
            z = true;
        }
        if (!z && containsScope(1) && (findColor = findColor(this.mPickerColorHash, i4, i3)) != -1) {
            Log.i(TAG, "getColor() :: Find in Picker!");
            z = true;
            i2 = findColor;
        }
        if (!z && containsScope(8)) {
            i2 = GetColorByLightControl(i4, i3);
            Log.i(TAG, "getColor() :: Find by LightControl!");
            z = true;
        }
        Log.i(TAG, " isFind=" + z + "[" + String.format(" #%08X", Integer.valueOf(i & -1)) + ", " + String.format(" #%08X", Integer.valueOf(i2 & -1)) + "] ");
        return i2;
    }

    private int GetColorByLightControl(int i, int i2) {
        float[] fArr = {0.0f, 0.0f, 0.0f};
        SpenColorUtil.RGBToHSL(i2, fArr);
        if (STANDARD_AREA_MIN > fArr[2] || fArr[2] > STANDARD_AREA_MAX) {
            fArr[2] = 1.0f - fArr[2];
            i2 = SpenColorUtil.HSLToRGB(fArr);
        }
        return ((i << 24) & -16777216) | (i2 & 16777215);
    }

    @Override // com.samsung.android.sdk.pen.util.color.SpenIColorTheme
    public boolean getColor(float[] fArr, float[] fArr2) {
        if (fArr == null && fArr2 == null) {
            return false;
        }
        Color.colorToHSV(getColor(SpenColorUtil.HSVToColor(fArr)), fArr2);
        return true;
    }

    public void setSearchScope(int i) {
        this.mSearchScope = i;
    }

    /* access modifiers changed from: protected */
    public boolean isContainsPickerColor(int i) {
        return findColor(this.mPickerColorHash, (i >> 24) & 255, (16777215 & i) | -16777216) != -1;
    }

    /* access modifiers changed from: protected */
    public HashMap<Integer, Integer> getPaletteHash() {
        return this.mPaletteColorHash;
    }

    private boolean containsScope(int i) {
        return (this.mSearchScope & i) == i;
    }

    private int findColor(HashMap<Integer, Integer> hashMap, int i, int i2) {
        Integer num;
        if (hashMap == null || (num = hashMap.get(Integer.valueOf(i2))) == null) {
            return -1;
        }
        return (num.intValue() & 16777215) | ((i << 24) & -16777216);
    }

    private void initPickerHash(Context context) {
        if (context != null) {
            Resources resources = context.getResources();
            String packageName = context.getPackageName();
            int identifier = resources.getIdentifier("spen_adaptive_light_color", "array", packageName);
            int identifier2 = resources.getIdentifier("spen_adaptive_dark_color", "array", packageName);
            if (identifier > 0 && identifier2 > 0) {
                int[] intArray = resources.getIntArray(identifier);
                int[] intArray2 = resources.getIntArray(identifier2);
                for (int i = 0; i < intArray.length; i++) {
                    int i2 = intArray[i] & -1;
                    int i3 = intArray2[i] & -1;
                    this.mPickerColorHash.put(Integer.valueOf(i2), Integer.valueOf(i3));
                    this.mPickerColorHash.put(Integer.valueOf(i3), Integer.valueOf(i2));
                }
            }
            int identifier3 = resources.getIdentifier("spen_adaptive_standard_color", "array", packageName);
            if (identifier3 > 0) {
                int[] intArray3 = resources.getIntArray(identifier3);
                for (int i4 = 0; i4 < intArray3.length; i4++) {
                    this.mPickerColorHash.put(Integer.valueOf(intArray3[i4]), Integer.valueOf(intArray3[i4]));
                }
            }
        }
    }

    private void initPaletteHash(Context context) {
        if (context != null) {
            Resources resources = context.getResources();
            String packageName = context.getPackageName();
            for (int i = 1; i <= 21; i++) {
                int identifier = resources.getIdentifier("spen_setting_swatch_" + Integer.toString(i), "array", packageName);
                int identifier2 = resources.getIdentifier("spen_setting_swatch_adaptive_" + Integer.toString(i), "array", packageName);
                if (identifier > 0 && identifier2 > 0) {
                    int[] intArray = resources.getIntArray(identifier);
                    int[] intArray2 = resources.getIntArray(identifier2);
                    for (int i2 = 0; i2 < intArray.length; i2++) {
                        this.mPaletteColorHash.put(Integer.valueOf(intArray[i2] & -1), Integer.valueOf(intArray2[i2] & -1));
                    }
                }
            }
        }
    }
}
