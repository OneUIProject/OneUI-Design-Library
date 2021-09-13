package de.dlyt.yanndroid.oneui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.SeslViewPager;

public class ViewPager extends SeslViewPager {
    private boolean mIsPagingEnabled = true;

    public ViewPager(Context context) {
        super(context);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsPagingEnabled && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mIsPagingEnabled && super.onTouchEvent(ev);
    }

    public void setPagingEnabled(boolean enabled){
        mIsPagingEnabled = enabled;
    }

    public boolean isPagingEnabled(){
        return mIsPagingEnabled;
    }
}
