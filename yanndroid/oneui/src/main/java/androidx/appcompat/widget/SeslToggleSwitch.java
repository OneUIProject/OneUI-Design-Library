package androidx.appcompat.widget;

import android.content.Context;
import android.util.AttributeSet;

import de.dlyt.yanndroid.oneui.view.Switch;

public class SeslToggleSwitch extends Switch {
    private OnBeforeCheckedChangeListener mOnBeforeListener;

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

    public interface OnBeforeCheckedChangeListener {
        boolean onBeforeCheckedChanged(SeslToggleSwitch seslToggleSwitch, boolean z);
    }
}
