package com.samsung.android.sdk.pen.settingui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class SpenScrollView extends ScrollView {
    private static final int SDK_VERSION = Build.VERSION.SDK_INT;
    private static final String TAG = "SpenScrollView";
    private float[] mRadii = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    public SpenScrollView(Context context) {
        super(context);
    }

    public SpenScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SpenScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public SpenScrollView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void setRadii(float[] fArr) {
        this.mRadii = fArr;
    }

    public boolean canScrollVertically(int i) {
        boolean canScrollVertically = super.canScrollVertically(i);
        if (canScrollVertically || i < 0) {
            return canScrollVertically;
        }
        int computeVerticalScrollOffset = computeVerticalScrollOffset();
        int computeVerticalScrollRange = computeVerticalScrollRange() - computeVerticalScrollExtent();
        if (computeVerticalScrollRange != 0 && computeVerticalScrollOffset < computeVerticalScrollRange) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(canvas.getClipBounds()), this.mRadii, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
