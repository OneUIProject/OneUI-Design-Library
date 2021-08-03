package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.R;


class SpenColorSwatchItemView extends View {
    private static final int SELECT_COLOR = -1;
    private final int mSelectStrokeSize;
    private int mSelectorColor = -1;

    public SpenColorSwatchItemView(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSelectStrokeSize = context.getResources().getDimensionPixelSize(R.dimen.setting_color_picker_swatch_item_selected_size);
    }

    public void setSelectorColor(int i) {
        this.mSelectorColor = i;
    }

    public void setSelected(boolean z) {
        super.setSelected(z);
    }
}
