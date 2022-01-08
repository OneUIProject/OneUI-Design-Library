package de.dlyt.yanndroid.oneui.menu.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.R;

public class MenuDivider extends ImageView {
    private static final float CIRCLE_INTERVAL = 3.0f;
    private static final float DIAMETER_SIZE = 1.5f;
    private final int mDiameter;
    private final int mInterval;
    private Paint mPaint;

    public MenuDivider(Context context) {
        this(context, null);
    }

    public MenuDivider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuDivider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        mDiameter = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIAMETER_SIZE, displayMetrics);
        mInterval = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_INTERVAL, displayMetrics);

        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.sesl_popup_menu_divider_color));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // kang from androidx.appcompat.widget.SeslMenuDivider
        int i;
        int i2;
        int width = (getWidth() - getPaddingStart()) - getPaddingEnd();
        int height = getHeight();
        int i3 = this.mDiameter;
        int i4 = ((width - i3) / (this.mInterval + i3)) + 1;
        int i5 = i4 - 1;
        int paddingStart = ((int) ((((float) i3) / 2.0f) + 0.5f)) + getPaddingStart();
        int i6 = this.mDiameter;
        int i7 = (width - i6) - ((this.mInterval + i6) * i5);
        if (i6 % 2 != 0) {
            i7--;
        }
        if (i5 > 0) {
            i = i7 / i5;
            i2 = i7 % i5;
        } else {
            i2 = 0;
            i = 0;
        }
        int i8 = 0;
        for (int i9 = 0; i9 < i4; i9++) {
            canvas.drawCircle((float) (paddingStart + i8), (float) (height / 2), ((float) this.mDiameter) / 2.0f, this.mPaint);
            i8 += this.mDiameter + this.mInterval + i;
            if (i9 < i2) {
                i8++;
            }
        }
    }
}
