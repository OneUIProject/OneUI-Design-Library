package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

import de.dlyt.yanndroid.samsung.R;

public class SpenColorSwatchSelectorView extends View {
    private final int mOutlineColor;
    private final int mOutlineSize;
    private boolean mIsShowOutline = false;

    public SpenColorSwatchSelectorView(Context context) {
        super(context);
        this.mOutlineSize = context.getResources().getDimensionPixelSize(R.dimen.setting_color_picker_swatch_selected_stroke_size);
        this.mOutlineColor = SpenSettingUtil.getColor(context, R.color.setting_color_picker_swatch_selected_stroke_color);
    }

    public void setBackgroundColor(int i) {
        updateOutline(i);
        super.setBackgroundColor(i);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIsShowOutline) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) this.mOutlineSize);
            paint.setColor(this.mOutlineColor);
            canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
        }
    }

    private void updateOutline(int i) {
        float[] fArr = new float[3];
        Color.colorToHSV(i, fArr);
        if (fArr[1] > 0.0f && fArr[2] <= 0.64f) {
            this.mIsShowOutline = true;
        } else this.mIsShowOutline = fArr[1] == 0.0f && !(fArr[2] > 0.15f);
    }
}
