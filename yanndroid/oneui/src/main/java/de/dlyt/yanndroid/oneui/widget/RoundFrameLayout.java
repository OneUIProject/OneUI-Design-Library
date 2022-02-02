package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;

import de.dlyt.yanndroid.oneui.R;

public class RoundFrameLayout extends FrameLayout {
    SeslRoundedCorner mSeslRoundedCorner;
    private Context mContext;

    public RoundFrameLayout(Context context) {
        super(context);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray obtainStyledAttributes = mContext.obtainStyledAttributes(attrs, R.styleable.RoundFrameLayout);

        int roundedCorners = obtainStyledAttributes.getInt(R.styleable.RoundFrameLayout_roundedCorners, 15);

        mSeslRoundedCorner = new SeslRoundedCorner(mContext);
        mSeslRoundedCorner.setRoundedCorners(roundedCorners);

        obtainStyledAttributes.recycle();
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mSeslRoundedCorner.drawRoundedCorner(canvas);
    }

}
