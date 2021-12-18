package de.dlyt.yanndroid.oneui.menu;

import android.content.Context;
import android.widget.ListView;

class PopupListView extends ListView {
    private int mMaxHeight;

    PopupListView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        invalidate();
    }
}
