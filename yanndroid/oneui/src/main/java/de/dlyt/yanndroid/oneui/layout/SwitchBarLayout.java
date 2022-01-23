package de.dlyt.yanndroid.oneui.layout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.SwitchBar;

public class SwitchBarLayout extends ToolbarLayoutWrapper {

    private int mLayout;
    private String mToolbarTitle;
    private String mToolbarSubtitle;
    private Boolean mToolbarExpanded;
    private SwitchBar switchBar;
    private FrameLayout mainContainer;

    public SwitchBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwitchBarLayout, 0, 0);

        try {
            mLayout = attr.getResourceId(R.styleable.SwitchBarLayout_android_layout, R.layout.samsung_switchbarlayout);
            mToolbarTitle = attr.getString(R.styleable.SwitchBarLayout_toolbar_title);
            mToolbarSubtitle = attr.getString(R.styleable.SwitchBarLayout_toolbar_subtitle);
            mToolbarExpanded = attr.getBoolean(R.styleable.SwitchBarLayout_toolbar_expanded, false);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(mLayout, this, true);

        setToolbarLayout(findViewById(R.id.toolbar_switchbarlayout));

        setTitle(mToolbarTitle);
        setSubtitle(mToolbarSubtitle);
        setExpanded(mToolbarExpanded, false);
        setNavigationButtonTooltip(getResources().getText(R.string.sesl_navigate_up));
        setNavigationButtonOnClickListener(v -> getActivity().onBackPressed());

        switchBar = findViewById(R.id.switchbar_switchbarlayout);

        mainContainer = findViewById(R.id.switchbar_container);
    }

    public SwitchBar getSwitchBar() {
        return switchBar;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mainContainer == null) {
            super.addView(child, index, params);
        } else {
            mainContainer.addView(child, index, params);
        }
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
