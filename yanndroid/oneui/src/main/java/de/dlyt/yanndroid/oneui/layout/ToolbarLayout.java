package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.ToolbarImageButton;

public class ToolbarLayout extends LinearLayout {

    private Drawable mNavigationIcon;
    private String mTitle;
    private String mSubtitle;
    private Boolean mExpandable;
    private Boolean mExpanded;

    private ToolbarImageButton navigation_icon;
    private TextView navigation_icon_Badge;
    private RelativeLayout navigation_icon_container;
    private MaterialTextView expanded_title;
    private MaterialTextView expanded_subtitle;
    private MaterialTextView collapsed_title;
    private androidx.appcompat.widget.Toolbar toolbar;
    private AppBarLayout AppBar;
    private LinearLayout main_container;
    private LinearLayout expand_container;

    private ViewGroup.LayoutParams appBarLayoutParams;


    public static final int NAVIGATION_ICON = 0;
    public static final int EXPANDED_TITLE = 1;
    public static final int EXPANDED_SUBTITLE = 2;
    public static final int COLLAPSED_TITLE = 3;
    public static final int TOOLBAR = 4;
    public static final int APPBAR_LAYOUT = 5;
    public static final int CONTENT_LAYOUT = 6;
    public static final int EXPANDED_CONTENT = 7;

    @IntDef({NAVIGATION_ICON, EXPANDED_TITLE, EXPANDED_SUBTITLE, COLLAPSED_TITLE, TOOLBAR, APPBAR_LAYOUT, CONTENT_LAYOUT, EXPANDED_CONTENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToolbarLayoutView {
    }

    public View getView(@ToolbarLayoutView int view) {
        switch (view) {
            case NAVIGATION_ICON:
                return navigation_icon;
            case EXPANDED_TITLE:
                return expanded_title;
            case EXPANDED_SUBTITLE:
                return expanded_subtitle;
            case COLLAPSED_TITLE:
                return collapsed_title;
            case TOOLBAR:
                return toolbar;
            case APPBAR_LAYOUT:
                return AppBar;
            case CONTENT_LAYOUT:
                return main_container;
            case EXPANDED_CONTENT:
                return expand_container;
            default:
                return null;
        }
    }


    public ToolbarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mTitle = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
            mExpandable = attr.getBoolean(R.styleable.ToolBarLayout_expandable, true);
            mExpanded = attr.getBoolean(R.styleable.ToolBarLayout_expanded, true);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_toolbarlayout, this, true);

        main_container = findViewById(R.id.main_container);

        navigation_icon = findViewById(R.id.navigationIcon);
        expanded_title = findViewById(R.id.expanded_title);
        expanded_subtitle = findViewById(R.id.expanded_subtitle);
        collapsed_title = findViewById(R.id.collapsed_title);
        navigation_icon_Badge = findViewById(R.id.navigationIcon_badge);
        navigation_icon_container = findViewById(R.id.navigationIcon_container);
        expand_container = findViewById(R.id.expand_container);


        setTitle(mTitle);
        setSubtitle(mSubtitle);
        setNavigationIcon(mNavigationIcon);


        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        AppBar = findViewById(R.id.app_bar);

        appBarLayoutParams = AppBar.getLayoutParams();

        init();

    }

    private void init() {
        setExpandable(mExpandable);

        int content_margin = (int) ((double) this.getResources().getDisplayMetrics().widthPixels * ((double) getResources().getInteger(R.integer.content_margin) / 1000));
        MarginLayoutParams params = (MarginLayoutParams) main_container.getLayoutParams();
        params.leftMargin = content_margin;
        params.rightMargin = content_margin;
        View bottom_corners = findViewById(R.id.bottom_corners);
        MarginLayoutParams paramsCorners = (MarginLayoutParams) bottom_corners.getLayoutParams();
        paramsCorners.leftMargin = content_margin;
        paramsCorners.rightMargin = content_margin;

        AppBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float percentage = (AppBar.getY() / AppBar.getTotalScrollRange());
            expand_container.setAlpha((float) (1.1 - (percentage * -2)));
            expand_container.setTranslationY(percentage * -120);
            collapsed_title.setAlpha((float) ((percentage * -2) - 0.8));
        });

        setExpanded(mExpanded, false);
    }


    public void setNavigationIconTooltip(CharSequence tooltipText) {
        navigation_icon.setTooltipText(tooltipText);
    }

    public androidx.appcompat.widget.Toolbar getToolbar() {
        return toolbar;
    }


    public void setNavigationOnClickListener(OnClickListener listener) {
        navigation_icon.setOnClickListener(listener);
    }


    public void showNavIconNotification(boolean showNotification) {
        navigation_icon_Badge.setVisibility(showNotification ? VISIBLE : GONE);
    }


    public void setNavigationIcon(Drawable navigationIcon) {
        this.mNavigationIcon = navigationIcon;
        navigation_icon.setImageDrawable(mNavigationIcon);
        navigation_icon_container.setVisibility(navigationIcon == null ? GONE : VISIBLE);
    }

    public void setTitle(String title) {
        this.mTitle = title;
        expanded_title.setText(mTitle);
        collapsed_title.setText(mTitle);
    }

    public void setSubtitle(String subtitle) {
        this.mSubtitle = subtitle;
        expanded_subtitle.setText(mSubtitle);
        expanded_subtitle.setVisibility(subtitle == null || subtitle.equals("") ? INVISIBLE : VISIBLE);
    }

    public void setSubtitleColor(int color) {
        expanded_subtitle.setTextColor(color);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        this.mExpanded = expanded;
        AppBar.setExpanded(expanded, animate);
    }

    public void setExpandable(boolean expandable) {
        this.mExpandable = expandable;
        if (expandable && getResources().getInteger(R.integer.appBarHeight) != 0) {
            appBarLayoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels * ((double) getResources().getInteger(R.integer.appBarHeight) / 1000));
        } else {
            float height = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize}).getDimension(0, 0);
            if (getResources().getConfiguration().smallestScreenWidthDp < 600 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                height *= 7.0 / 6;
            }
            appBarLayoutParams.height = (int) height;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (main_container == null) {
            super.addView(child, index, params);
        } else {
            main_container.addView(child, index, params);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init();
    }

}
