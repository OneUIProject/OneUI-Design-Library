package de.dlyt.yanndroid.oneui.sesl.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;

public class SeslSubheaderRoundedCorner extends SeslRoundedCorner {
    private static final String TAG = "SeslSubheaderRoundedCorner";

    public SeslSubheaderRoundedCorner(Context context) {
        super(context);
    }

    private void drawRoundedCornerInternal(Canvas canvas) {
        Rect rect = this.mRoundedCornerBounds;
        int i = rect.left;
        int i2 = rect.right;
        int i3 = rect.top;
        int i4 = rect.bottom;
        if ((this.mRoundedCornerMode & 1) != 0) {
            Drawable drawable = this.mTopLeftRound;
            int i5 = this.mRoundRadius;
            drawable.setBounds(i, i4, i + i5, i5 + i4);
            this.mTopLeftRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 2) != 0) {
            Drawable drawable2 = this.mTopRightRound;
            int i6 = this.mRoundRadius;
            drawable2.setBounds(i2 - i6, i4, i2, i6 + i4);
            this.mTopRightRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 4) != 0) {
            Drawable drawable3 = this.mBottomLeftRound;
            int i7 = this.mRoundRadius;
            drawable3.setBounds(i, i3 - i7, i7 + i, i3);
            this.mBottomLeftRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 8) != 0) {
            Drawable drawable4 = this.mBottomRightRound;
            int i8 = this.mRoundRadius;
            drawable4.setBounds(i2 - i8, i3 - i8, i2, i3);
            this.mBottomRightRound.draw(canvas);
        }
    }

    public void drawRoundedCorner(int i, int i2, int i3, int i4, Canvas canvas) {
        this.mRoundedCornerBounds.set(i, i2, i3, i4);
        drawRoundedCornerInternal(canvas);
    }

    @Override
    public void drawRoundedCorner(View view, Canvas canvas) {
        if (view.getTranslationY() != 0.0f) {
            this.mX = Math.round(view.getX());
            this.mY = Math.round(view.getY());
        } else {
            this.mX = view.getLeft();
            this.mY = view.getTop();
        }
        Rect rect = this.mRoundedCornerBounds;
        int i = this.mX;
        rect.set(i, this.mY, view.getWidth() + i, this.mY + view.getHeight());
        drawRoundedCornerInternal(canvas);
    }
}
