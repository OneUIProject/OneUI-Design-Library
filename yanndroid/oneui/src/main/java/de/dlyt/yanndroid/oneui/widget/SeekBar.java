package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.widget.SeslAbsSeekBar;

public class SeekBar extends SeslAbsSeekBar {
    private int mOldValue;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private OnSeekBarHoverListener mOnSeekBarHoverListener;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public interface OnSeekBarHoverListener {
        void onHoverChanged(SeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingHover(SeekBar seekBar, int progress);

        void onStopTrackingHover(SeekBar seekBar);
    }

    public SeekBar(Context context) {
        this(context, null);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onProgressRefresh(float scale, boolean fromUser, int progress) {
        super.onProgressRefresh(scale, fromUser, progress);

        if (!mIsSeamless) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
            }
        } else {
            progress = Math.round(((float) progress) / 1000.0f);
            if (mOldValue != progress) {
                mOldValue = progress;
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
                }
            }
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    @Override
    protected void onStartTrackingTouch() {
        super.onStartTrackingTouch();
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    @Override
    protected void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return android.widget.SeekBar.class.getName();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);

        if (Build.VERSION.SDK_INT >= 24 && canUserSetProgress()) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
        }
    }

    public void setOnSeekBarHoverListener(OnSeekBarHoverListener l) {
        mOnSeekBarHoverListener = l;
    }

    @Override
    protected void onStartTrackingHover(int level, int x, int y) {
        if (mOnSeekBarHoverListener != null) {
            mOnSeekBarHoverListener.onStartTrackingHover(this, level);
        }
        super.onStartTrackingHover(level, x, y);
    }

    @Override
    protected void onStopTrackingHover() {
        if (mOnSeekBarHoverListener != null) {
            mOnSeekBarHoverListener.onStopTrackingHover(this);
        }
        super.onStopTrackingHover();
    }

    @Override
    protected void onHoverChanged(int level, int x, int y) {
        if (mOnSeekBarHoverListener != null) {
            mOnSeekBarHoverListener.onHoverChanged(this, level, true);
        }
        super.onHoverChanged(level, x, y);
    }
}
