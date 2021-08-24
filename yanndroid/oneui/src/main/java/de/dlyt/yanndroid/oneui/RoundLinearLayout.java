package de.dlyt.yanndroid.oneui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.appcompat.util.SeslRoundedCorner;

public class RoundLinearLayout extends LinearLayout {
    SeslRoundedCorner mSeslRoundedCorner;
    private Context mContext;

    public RoundLinearLayout(Context context) {
        super(context);
    }

    public RoundLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray obtainStyledAttributes = mContext.obtainStyledAttributes(attrs, R.styleable.RoundLinearLayout);

        int roundedCorners = obtainStyledAttributes.getInt(R.styleable.RoundLinearLayout_roundedCorners, 15);

        mSeslRoundedCorner = new SeslRoundedCorner(mContext);
        mSeslRoundedCorner.setRoundedCorners(roundedCorners);

        obtainStyledAttributes.recycle();
    }

    public RoundLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mSeslRoundedCorner.drawRoundedCorner(canvas);
    }
}
