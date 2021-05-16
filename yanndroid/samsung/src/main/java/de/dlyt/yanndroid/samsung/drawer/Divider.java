package de.dlyt.yanndroid.samsung.drawer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.samsung.R;

public class Divider extends View {
    public Divider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.sesl_drawer_divider);
    }
}
