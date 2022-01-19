package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import java.util.ArrayList;

public class SeslRecentColorInfo {
    private Integer mSelectedColor = null;
    private Integer mCurrentColor = null;
    private Integer mNewColor = null;
    private ArrayList<Integer> mRecentColorInfo = new ArrayList<>();

    ArrayList<Integer> getRecentColorInfo() {
        return mRecentColorInfo;
    }

    Integer getCurrentColor() {
        return mCurrentColor;
    }

    Integer getNewColor() {
        return mNewColor;
    }

    public Integer getSelectedColor() {
        return mSelectedColor;
    }

    public void setCurrentColor(Integer num) {
        mCurrentColor = num;
    }

    public void setNewColor(Integer num) {
        mNewColor = num;
    }

    public void saveSelectedColor(int i) {
        mSelectedColor = i;
    }

    public void initRecentColorInfo(int[] iArr) {
        if (iArr != null) {
            int i = 0;
            if (iArr.length <= SeslColorPicker.RECENT_COLOR_SLOT_COUNT) {
                int length = iArr.length;
                while (i < length) {
                    this.mRecentColorInfo.add(iArr[i]);
                    i++;
                }
                return;
            }
            while (i < SeslColorPicker.RECENT_COLOR_SLOT_COUNT) {
                mRecentColorInfo.add(iArr[i]);
                i++;
            }
        }
    }
}
