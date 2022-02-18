package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SeslSpinner;

public class Spinner extends SeslSpinner {
    public Spinner(@NonNull Context context) {
        super(context);
    }

    public Spinner(@NonNull Context context, int mode) {
        super(context, mode);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public Spinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    public void setBlurEffectEnabled(boolean enabled) {
        mBlurEffectEnabled = enabled;
    }
}
