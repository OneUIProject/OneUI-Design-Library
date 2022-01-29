package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UnPressableLinearLayout extends LinearLayout {
    public UnPressableLinearLayout(@NonNull Context context) {
        this(context, null);
    }

    public UnPressableLinearLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
    }
}
