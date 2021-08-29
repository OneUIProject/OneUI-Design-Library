package de.dlyt.yanndroid.oneui.sesl.colorpicker;

import java.util.ArrayList;

public class SeslRecentColorInfo {
    private Integer mCurrentColor = null;
    private Integer mNewColor = null;
    private ArrayList<Integer> mRecentColorInfo = new ArrayList();
    private Integer mSelectedColor = null;

    public SeslRecentColorInfo() {
    }

    public Integer getCurrentColor() {
        return this.mCurrentColor;
    }

    public void setCurrentColor(Integer var1) {
        this.mCurrentColor = var1;
    }

    public Integer getNewColor() {
        return this.mNewColor;
    }

    public void setNewColor(Integer var1) {
        this.mNewColor = var1;
    }

    public ArrayList<Integer> getRecentColorInfo() {
        return this.mRecentColorInfo;
    }

    public Integer getSelectedColor() {
        return this.mSelectedColor;
    }

    public void initRecentColorInfo(int[] var1) {
        if (var1 != null) {
            int var2 = var1.length;
            int var3 = 0;
            byte var4 = 0;
            if (var2 <= 6) {
                var2 = var1.length;

                for (var3 = var4; var3 < var2; ++var3) {
                    int var5 = var1[var3];
                    this.mRecentColorInfo.add(var5);
                }
            } else {
                while (var3 < 6) {
                    this.mRecentColorInfo.add(var1[var3]);
                    ++var3;
                }
            }
        }

    }

    public void saveSelectedColor(int var1) {
        this.mSelectedColor = var1;
    }
}
