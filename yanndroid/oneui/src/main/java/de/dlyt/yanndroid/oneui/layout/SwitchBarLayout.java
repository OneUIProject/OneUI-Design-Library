package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.content.res.TypedArray;
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

        LayoutInflater.from(mContext).inflate(R.layout.oui_toolbarlayout_switchbar, mainContainer, true);
        switchBar = findViewById(R.id.switchbar_switchbarlayout);
        content = findViewById(R.id.switchbarlayout_container);
    }

    //
    // Layout methods
    //
    @Override
    protected void initLayoutAttrs(@Nullable AttributeSet attrs) {
        TypedArray attr = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.SwitchBarLayout, 0, 0);

        try {
            mLayout = attr.getResourceId(R.styleable.SwitchBarLayout_android_layout, R.layout.oui_toolbarlayout_appbar);
            mExpandable = attr.getBoolean(R.styleable.SwitchBarLayout_toolbar_expandable, true);
            mExpanded = attr.getBoolean(R.styleable.SwitchBarLayout_toolbar_expanded, mExpandable);
            mNavigationIcon = attr.getDrawable(R.styleable.SwitchBarLayout_toolbar_navigationIcon);
            mTitle = attr.getString(R.styleable.SwitchBarLayout_toolbar_title);
            mSubtitle = attr.getString(R.styleable.SwitchBarLayout_toolbar_subtitle);
        } finally {
            attr.recycle();
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (content == null) {
            super.addView(child, index, params);
        } else {
            Drawer_Toolbar_LayoutParams lp = (Drawer_Toolbar_LayoutParams) params;
            switch (lp.layout_location) {
                default:
                case 1:
                case 2:
                case 3:
                case 4:
                    super.addView(child, index, params);
                    break;
                case 0:
                    content.addView(child, index, params);
                    break;
            }
        }
    }

    public SwitchBar getSwitchBar() {
        return switchBar;
    }
}
