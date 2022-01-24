package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.SwitchBar;

public class SwitchBarLayout extends ToolbarLayout {
    private static final String TAG = "SwitchBarLayout";
    private SwitchBar switchBar;
    private FrameLayout content;

    public SwitchBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(mContext).inflate(R.layout.oui_switchbarlayout, mainContainer, true);
        switchBar = findViewById(R.id.switchbar_switchbarlayout);
        content = findViewById(R.id.switchbarlayout_container);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (content == null) {
            super.addView(child, index, params);
        } else {
            if (((ToolbarLayoutParams) params).layout_location == 0) {
                content.addView(child, params);
            } else {
                super.addView(child, index, params);
            }
        }
    }

    public SwitchBar getSwitchBar() {
        return switchBar;
    }
}
