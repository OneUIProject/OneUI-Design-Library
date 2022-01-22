package de.dlyt.yanndroid.oneui.utils;

import android.view.MotionEvent;
import android.view.View;

import de.dlyt.yanndroid.oneui.widget.BottomNavigationView;

public abstract class CustomButtonClickListener implements View.OnTouchListener {
    private BottomNavigationView mBnv;

    public CustomButtonClickListener(BottomNavigationView bnv) {
        mBnv = bnv;
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
                mBnv.fullScroll(mBnv.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? View.FOCUS_LEFT : View.FOCUS_RIGHT);
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setPressed(false);
                break;
        }
        return true;
    }

    public abstract void onClick(View v);
}