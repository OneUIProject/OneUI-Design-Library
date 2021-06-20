package de.dlyt.yanndroid.samsung.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.textview.MaterialTextView;

import de.dlyt.yanndroid.samsung.R;

public class ToolbarLayout extends LinearLayout {

    private Drawable mNavigationIcon;
    private String mTitle;
    private String mSubtitle;

    private ImageView navigation_icon;
    private TextView navigation_icon_Badge;
    private RelativeLayout navigation_icon_container;
    private MaterialTextView expanded_title;
    private MaterialTextView expanded_subtitle;
    private MaterialTextView collapsed_title;
    private androidx.appcompat.widget.Toolbar toolbar;
    private AppBarLayout AppBar;
    private LinearLayout main_container;
    private LinearLayout expand_container;


    public ToolbarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mTitle = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
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
        //setSupportActionBar(toolbar);
        AppBar = findViewById(R.id.app_bar);

        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);

        AppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = (AppBar.getY() / AppBar.getTotalScrollRange());
                expand_container.setAlpha((float) (1.1 - (percentage * -2)));
                expand_container.setTranslationY(percentage * -120);
                collapsed_title.setAlpha((float) ((percentage * -2) - 0.8));
            }
        });

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
        expanded_subtitle.setVisibility(subtitle == null || subtitle.equals("") ? GONE : VISIBLE);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        AppBar.setExpanded(expanded, animate);
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (main_container == null) {
            super.addView(child, index, params);
        } else {
            main_container.addView(child, index, params);
        }
    }


}
