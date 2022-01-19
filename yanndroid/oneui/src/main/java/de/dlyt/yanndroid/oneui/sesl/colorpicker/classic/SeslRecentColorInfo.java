package de.dlyt.yanndroid.oneui.sesl.colorpicker.classic;

import java.util.ArrayList;

public class SeslRecentColorInfo {
    private Integer mSelectedColor = null;
    private Integer mCurrentColor = null;
    private Integer mNewColor = null;
    private ArrayList<Integer> mRecentColorInfo = new ArrayList<>();

    ArrayList<Integer> getRecentColorInfo() {
        return this.mRecentColorInfo;
    }

    Integer getCurrentColor() {
        return this.mCurrentColor;
    }

    Integer getNewColor() {
        return this.mNewColor;
    }

    public Integer getSelectedColor() {
        return this.mSelectedColor;
    }

    public void setCurrentColor(Integer num) {
        this.mCurrentColor = num;
    }

    public void setNewColor(Integer num) {
        this.mNewColor = num;
    }

    public void saveSelectedColor(int i) {
        this.mSelectedColor = Integer.valueOf(i);
    }

    public void initRecentColorInfo(int[] iArr) {
        if (iArr != null) {
            int i = 0;
            if (iArr.length <= 6) {
                int length = iArr.length;
                while (i < length) {
                    this.mRecentColorInfo.add(Integer.valueOf(iArr[i]));
                    i++;
                }
                return;
            }
            while (i < 6) {
                this.mRecentColorInfo.add(Integer.valueOf(iArr[i]));
                i++;
            }
        }
    }
}
