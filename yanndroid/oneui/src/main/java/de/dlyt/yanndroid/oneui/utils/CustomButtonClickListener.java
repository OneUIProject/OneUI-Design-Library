package de.dlyt.yanndroid.oneui.utils;

import android.view.MotionEvent;
import android.view.View;

import de.dlyt.yanndroid.oneui.widget.TabLayout;

public abstract class CustomButtonClickListener implements View.OnTouchListener {
    private TabLayout mTabLayout;

    public CustomButtonClickListener(TabLayout tabLayout) {
        mTabLayout = tabLayout;
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                onClick(v);
                mTabLayout.fullScroll(mTabLayout.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? View.FOCUS_LEFT : View.FOCUS_RIGHT);
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setPressed(false);
                break;
        }
        return true;
    }

    public abstract void onClick(View v);
}