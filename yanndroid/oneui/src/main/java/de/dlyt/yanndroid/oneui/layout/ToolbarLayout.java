package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.appbar.SamsungAppBarLayout;
import de.dlyt.yanndroid.oneui.appbar.SamsungCollapsingToolbarLayout;
import de.dlyt.yanndroid.oneui.widget.ToolbarImageButton;

public class ToolbarLayout extends LinearLayout {
    private static String TAG = "ToolbarLayout";

    private Context mContext;

    private Drawable mNavigationIcon;
    private CharSequence mTitle;
    private CharSequence mSubtitle;
    private Boolean mExpandable;
    private Boolean mExpanded;

    private ToolbarImageButton navigation_icon;
    private TextView navigation_icon_Badge;
    private RelativeLayout navigation_icon_container;
    private MaterialTextView collapsed_title;
    private androidx.appcompat.widget.Toolbar toolbar;
    private SamsungAppBarLayout AppBar;
    private SamsungCollapsingToolbarLayout CollapsingToolbar;
    private LinearLayout main_container;


    public static final int NAVIGATION_ICON = 0;
    public static final int COLLAPSED_TITLE = 1;
    public static final int TOOLBAR = 2;
    public static final int APPBAR_LAYOUT = 3;
    public static final int COLLAPSING_TOOLBAR = 4;
    public static final int CONTENT_LAYOUT = 5;

    @IntDef({NAVIGATION_ICON, COLLAPSED_TITLE, TOOLBAR, APPBAR_LAYOUT, COLLAPSING_TOOLBAR, CONTENT_LAYOUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToolbarLayoutView {
    }

    public View getView(@ToolbarLayoutView int view) {
        switch (view) {
            case NAVIGATION_ICON:
                return navigation_icon;
            case COLLAPSED_TITLE:
                return collapsed_title;
            case TOOLBAR:
                return toolbar;
            case APPBAR_LAYOUT:
                return AppBar;
            case COLLAPSING_TOOLBAR:
                return CollapsingToolbar;
            case CONTENT_LAYOUT:
                return main_container;
            default:
                return null;
        }
    }


    public ToolbarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray attr = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mTitle = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
            mExpandable = attr.getBoolean(R.styleable.ToolBarLayout_expandable, true);
            mExpanded = attr.getBoolean(R.styleable.ToolBarLayout_expanded, true);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(mExpandable ? R.layout.samsung_appbar_toolbarlayout : R.layout.samsung_toolbar_toolbarlayout, this, true);

        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        if (mExpandable) {
            AppBar = findViewById(R.id.app_bar);
            CollapsingToolbar = findViewById(R.id.toolbar_layout);
        }

        navigation_icon = findViewById(R.id.navigationIcon);
        collapsed_title = findViewById(R.id.collapsed_title);
        navigation_icon_Badge = findViewById(R.id.navigationIcon_badge);
        navigation_icon_container = findViewById(R.id.navigationIcon_container);

        main_container = findViewById(R.id.main_container);

        setTitle(mTitle);
        setSubtitle(mSubtitle);
        setNavigationIcon(mNavigationIcon);

        init();

    }

    private void init() {
        int content_margin = (int) ((double) this.getResources().getDisplayMetrics().widthPixels * ((double) getResources().getInteger(R.integer.content_margin) / 1000));
        MarginLayoutParams params = (MarginLayoutParams) main_container.getLayoutParams();
        params.leftMargin = content_margin;
        params.rightMargin = content_margin;

        if (mExpandable) {
            AppBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
                int layoutPosition = Math.abs(appBarLayout.getTop());
                float alphaRange = ((float) CollapsingToolbar.getHeight()) * 0.17999999f;
                float toolbarTitleAlphaStart = ((float) CollapsingToolbar.getHeight()) * 0.35f;

                if (appBarLayout.getHeight() <= ((int) getResources().getDimension(R.dimen.sesl_action_bar_height_with_padding))) {
                    collapsed_title.setAlpha(1.0f);
                } else {
                    float collapsedTitleAlpha = ((150.0f / alphaRange) * (((float) layoutPosition) - toolbarTitleAlphaStart));

                    if (collapsedTitleAlpha >= 0.0f && collapsedTitleAlpha <= 255.0f) {
                        collapsedTitleAlpha /= 255.0f;
                        collapsed_title.setAlpha(collapsedTitleAlpha);
                    }
                    else if (collapsedTitleAlpha < 0.0f)
                        collapsed_title.setAlpha(0.0f);
                    else
                        collapsed_title.setAlpha(1.0f);
                }
            });
        }
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
        if (navigationIcon != null) {
            toolbar.setPadding(0, 0, 0, 0);
            navigation_icon_container.setVisibility(VISIBLE);
        } else
            navigation_icon_container.setVisibility(GONE);

    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        if (mExpandable) {
            CollapsingToolbar.setTitle(mTitle);
        }
        collapsed_title.setText(mTitle);
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        if (mExpandable) {
            CollapsingToolbar.setSubtitle(mSubtitle);
        }
    }

    public void setExpanded(boolean expanded, boolean animate) {
        if (mExpandable) {
            this.mExpanded = expanded;
            AppBar.setExpanded(expanded, animate);
        } else {
            Log.d(TAG, "setExpanded: mExpanded is " + mExpanded);
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
        setExpanded(mExpanded, false);
    }

}
