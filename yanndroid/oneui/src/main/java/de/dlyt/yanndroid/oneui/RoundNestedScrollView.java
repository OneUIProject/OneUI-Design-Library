package de.dlyt.yanndroid.oneui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.util.SeslRoundedCorner;

public class RoundNestedScrollView extends NestedScrollView {
    SeslRoundedCorner mSeslRoundedCorner;
    private Context mContext;

    public RoundNestedScrollView(Context context) {
        super(context);
    }

    public RoundNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray obtainStyledAttributes = mContext.obtainStyledAttributes(attrs, R.styleable.RoundNestedScrollView);

        int roundedCorners = obtainStyledAttributes.getInt(R.styleable.RoundFrameLayout_roundedCorners, 15);

        mSeslRoundedCorner = new SeslRoundedCorner(mContext);
        mSeslRoundedCorner.setRoundedCorners(roundedCorners);

        obtainStyledAttributes.recycle();
    }

    public RoundNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mSeslRoundedCorner.drawRoundedCorner(canvas);
    }

}
