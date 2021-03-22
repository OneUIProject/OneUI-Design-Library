package androidx.appcompat.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import androidx.annotation.RestrictTo;

import de.dlyt.yanndroid.samsung.R;

public class SeslSeekBar extends SeslAbsSeekBar {
    private int mOldValue;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private OnSeekBarHoverListener mOnSeekBarHoverListener;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeslSeekBar seslSeekBar, int i, boolean z);

        void onStartTrackingTouch(SeslSeekBar seslSeekBar);

        void onStopTrackingTouch(SeslSeekBar seslSeekBar);
    }

    public interface OnSeekBarHoverListener {
        void onHoverChanged(SeslSeekBar seslSeekBar, int i, boolean z);

        void onStartTrackingHover(SeslSeekBar seslSeekBar, int i);

        void onStopTrackingHover(SeslSeekBar seslSeekBar);
    }

    public SeslSeekBar(Context context) {
        this(context, null);
    }

    public SeslSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.seekBarStyle);
    }

    public SeslSeekBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SeslSeekBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    @Override // androidx.appcompat.widget.SeslProgressBar, androidx.appcompat.widget.SeslAbsSeekBar
    public CharSequence getAccessibilityClassName() {
        return SeekBar.class.getName();
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslAbsSeekBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onHoverChanged(int i, int i2, int i3) {
        OnSeekBarHoverListener onSeekBarHoverListener = this.mOnSeekBarHoverListener;
        if (onSeekBarHoverListener != null) {
            onSeekBarHoverListener.onHoverChanged(this, i, true);
        }
        super.onHoverChanged(i, i2, i3);
    }

    @Override // androidx.appcompat.widget.SeslProgressBar, androidx.appcompat.widget.SeslAbsSeekBar
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (Build.VERSION.SDK_INT >= 24 && canUserSetProgress()) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslProgressBar, androidx.appcompat.widget.SeslAbsSeekBar
    public void onProgressRefresh(float f, boolean z, int i) {
        super.onProgressRefresh(f, z, i);
        if (!this.mIsSeamless) {
            OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
            if (onSeekBarChangeListener != null) {
                onSeekBarChangeListener.onProgressChanged(this, i, z);
                return;
            }
            return;
        }
        int round = Math.round(((float) i) / 1000.0f);
        if (this.mOldValue != round) {
            this.mOldValue = round;
            OnSeekBarChangeListener onSeekBarChangeListener2 = this.mOnSeekBarChangeListener;
            if (onSeekBarChangeListener2 != null) {
                onSeekBarChangeListener2.onProgressChanged(this, round, z);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslAbsSeekBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onStartTrackingHover(int i, int i2, int i3) {
        OnSeekBarHoverListener onSeekBarHoverListener = this.mOnSeekBarHoverListener;
        if (onSeekBarHoverListener != null) {
            onSeekBarHoverListener.onStartTrackingHover(this, i);
        }
        super.onStartTrackingHover(i, i2, i3);
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslAbsSeekBar
    public void onStartTrackingTouch() {
        super.onStartTrackingTouch();
        OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslAbsSeekBar
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public void onStopTrackingHover() {
        OnSeekBarHoverListener onSeekBarHoverListener = this.mOnSeekBarHoverListener;
        if (onSeekBarHoverListener != null) {
            onSeekBarHoverListener.onStopTrackingHover(this);
        }
        super.onStopTrackingHover();
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.appcompat.widget.SeslAbsSeekBar
    public void onStopTrackingTouch() {
        super.onStopTrackingTouch();
        OnSeekBarChangeListener onSeekBarChangeListener = this.mOnSeekBarChangeListener;
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        this.mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    public void setOnSeekBarHoverListener(OnSeekBarHoverListener onSeekBarHoverListener) {
        this.mOnSeekBarHoverListener = onSeekBarHoverListener;
    }
}
