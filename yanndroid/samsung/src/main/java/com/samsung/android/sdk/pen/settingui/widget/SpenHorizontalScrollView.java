package com.samsung.android.sdk.pen.settingui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class SpenHorizontalScrollView extends HorizontalScrollView {
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    private static final String TAG = "SpenHorizontalScrollView";
    private float[] mRadii = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    public SpenHorizontalScrollView(Context context) {
        super(context);
    }

    public SpenHorizontalScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SpenHorizontalScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public SpenHorizontalScrollView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void setRadii(float[] fArr) {
        this.mRadii = fArr;
    }

    public void onDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(canvas.getClipBounds()), this.mRadii, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
