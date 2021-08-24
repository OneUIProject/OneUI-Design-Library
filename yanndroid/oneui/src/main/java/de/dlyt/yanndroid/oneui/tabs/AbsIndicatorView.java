package de.dlyt.yanndroid.oneui.tabs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class AbsIndicatorView extends View {
    public AbsIndicatorView(Context context) {
        super(context);
    }

    public AbsIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AbsIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    abstract void onHide();

    abstract void onSetSelectedIndicatorColor(int i);

    abstract void onShow();

    abstract void startPressEffect();

    abstract void startReleaseEffect();

    public void setSelectedIndicatorColor(int color) {
        onSetSelectedIndicatorColor(color);
    }

    public void setPressed() {
        startPressEffect();
    }

    public void setReleased() {
        startReleaseEffect();
    }

    public void setHide() {
        onHide();
    }

    public void setShow() {
        onShow();
    }
}
