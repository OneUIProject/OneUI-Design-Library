package de.dlyt.yanndroid.oneui.appbar;

import android.view.View;

import androidx.core.view.ViewCompat;

public class ViewOffsetHelper {
    public final View view;
    public boolean horizontalOffsetEnabled = true;
    public int layoutLeft;
    public int layoutTop;
    public int offsetLeft;
    public int offsetTop;
    public boolean verticalOffsetEnabled = true;

    public ViewOffsetHelper(View var1) {
        this.view = var1;
    }

    public int getLayoutTop() {
        return this.layoutTop;
    }

    public int getTopAndBottomOffset() {
        return this.offsetTop;
    }

    public void onViewLayout() {
        this.layoutTop = this.view.getTop();
        this.layoutLeft = this.view.getLeft();
        this.updateOffsets();
    }

    public boolean setLeftAndRightOffset(int var1) {
        if (this.horizontalOffsetEnabled && this.offsetLeft != var1) {
            this.offsetLeft = var1;
            this.updateOffsets();
            return true;
        } else {
            return false;
        }
    }

    public boolean setTopAndBottomOffset(int var1) {
        if (this.verticalOffsetEnabled && this.offsetTop != var1) {
            this.offsetTop = var1;
            this.updateOffsets();
            return true;
        } else {
            return false;
        }
    }

    public final void updateOffsets() {
        View var1 = this.view;
        ViewCompat.offsetTopAndBottom(var1, this.offsetTop - (var1.getTop() - this.layoutTop));
        var1 = this.view;
        ViewCompat.offsetLeftAndRight(var1, this.offsetLeft - (var1.getLeft() - this.layoutLeft));
    }
}
