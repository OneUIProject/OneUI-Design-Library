package androidx.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SeslToggleSwitch extends SwitchCompat {
    private OnBeforeCheckedChangeListener mOnBeforeListener;

    public interface OnBeforeCheckedChangeListener {
        boolean onBeforeCheckedChanged(SeslToggleSwitch seslToggleSwitch, boolean z);
    }

    public SeslToggleSwitch(Context context) {
        super(context);
    }

    public SeslToggleSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SeslToggleSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public void setOnBeforeCheckedChangeListener(OnBeforeCheckedChangeListener onBeforeCheckedChangeListener) {
        this.mOnBeforeListener = onBeforeCheckedChangeListener;
    }

    @Override
    public void setChecked(boolean z) {
        OnBeforeCheckedChangeListener onBeforeCheckedChangeListener = this.mOnBeforeListener;
        if (onBeforeCheckedChangeListener == null || !onBeforeCheckedChangeListener.onBeforeCheckedChanged(this, z)) {
            super.setChecked(z);
        }
    }

    public void setCheckedInternal(boolean z) {
        super.setChecked(z);
    }
}
