package de.dlyt.yanndroid.oneui.layout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class DrawerLayout extends LinearLayout {

    private Drawable mDrawerIcon;
    private String mToolbarTitle;
    private String mToolbarSubtitle;
    private Boolean mToolbarExpandable;
    private Boolean mToolbarExpanded;

    private ImageView drawerIcon;
    private ToolbarLayout toolbarLayout;
    private TextView drawerIcon_badge;

    private LinearLayout drawer_container;

    private int viewIdForDrawer;

    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private View drawer;


    public static final int DRAWER_ICON = 0;
    public static final int TOOLBAR = 1;
    public static final int CONTENT_LAYOUT = 2;
    public static final int DRAWER_LAYOUT = 3;
    public static final int DRAWER = 4;

    @IntDef({DRAWER_ICON, TOOLBAR, CONTENT_LAYOUT, DRAWER_LAYOUT, DRAWER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawerLayoutView {
    }

    public View getView(@DrawerLayoutView int view) {
        switch (view) {
            case DRAWER_ICON:
                return drawerIcon;
            case TOOLBAR:
                return toolbarLayout;
            case CONTENT_LAYOUT:
                return drawer_container;
            case DRAWER_LAYOUT:
                return drawerLayout;
            case DRAWER:
                return drawer;
            default:
                return null;
        }
    }


    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawerLayout, 0, 0);

        try {
            mToolbarTitle = attr.getString(R.styleable.DrawerLayout_toolbar_title);
            mToolbarSubtitle = attr.getString(R.styleable.DrawerLayout_toolbar_subtitle);
            mDrawerIcon = attr.getDrawable(R.styleable.DrawerLayout_drawer_icon);
            viewIdForDrawer = attr.getResourceId(R.styleable.DrawerLayout_drawer_viewId, -2);
            mToolbarExpandable = attr.getBoolean(R.styleable.DrawerLayout_toolbar_expandable, true);
            mToolbarExpanded = attr.getBoolean(R.styleable.DrawerLayout_toolbar_expanded, true);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_drawerlayout, this, true);

        drawer_container = findViewById(R.id.drawer_container);
        toolbarLayout = findViewById(R.id.drawer_toolbarlayout);
        drawerIcon_badge = findViewById(R.id.drawerIcon_badge);


        toolbarLayout.setTitle(mToolbarTitle);
        toolbarLayout.setSubtitle(mToolbarSubtitle);
        toolbarLayout.setExpandable(mToolbarExpandable);
        toolbarLayout.setNavigationIconTooltip(getResources().getText(R.string.sesl_navigation_drawer));
        toolbarLayout.setExpanded(mToolbarExpanded, false);
        drawerIcon = findViewById(R.id.drawerIcon);
        drawerIcon.setImageDrawable(mDrawerIcon);



        /*drawer logic*/
        View content = findViewById(R.id.main_content);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawer = findViewById(R.id.drawer);

        drawerLayout.setScrimColor(ContextCompat.getColor(getContext(), R.color.drawer_dim_color));
        drawerLayout.setDrawerElevation(0);

        init();

        Boolean isRtl = getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        Window window = getActivity().getWindow();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.opened, R.string.closed) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                if (isRtl) slideX *= -1;
                content.setTranslationX(slideX);

                float[] hsv = new float[3];
                Color.colorToHSV(ContextCompat.getColor(getContext(), R.color.background_color), hsv);
                hsv[2] *= 1f - (slideOffset * 0.2f);
                window.setStatusBarColor(Color.HSVToColor(hsv));
                window.setNavigationBarColor(Color.HSVToColor(hsv));

            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        toolbarLayout.setNavigationOnClickListener(v -> drawerLayout.openDrawer(drawer, true));
    }

    private void init() {
        ViewGroup.LayoutParams layoutParams = drawer.getLayoutParams();
        layoutParams.width = Math.min((int) ((double) this.getResources().getDisplayMetrics().widthPixels * ((double) getResources().getInteger(R.integer.drawerMaxWidth) / 1000)), this.getResources().getDimensionPixelSize(R.dimen.drawer_width));
    }


    public androidx.appcompat.widget.Toolbar getToolbar() {
        return toolbarLayout.getToolbar();
    }


    public void setDrawerIconOnClickListener(OnClickListener listener) {
        drawerIcon.setOnClickListener(listener);
    }


    public void setToolbarTitle(String title) {
        toolbarLayout.setTitle(title);
    }

    public void setToolbarSubtitle(String subtitle) {
        toolbarLayout.setSubtitle(subtitle);
    }

    public void setToolbarSubtitleColor(int color) {
        toolbarLayout.setSubtitleColor(color);
    }

    public void setToolbarExpanded(boolean expanded, boolean animate) {
        toolbarLayout.setExpanded(expanded, animate);
    }

    public void setToolbarExpandable(boolean expandable) {
        toolbarLayout.setExpandable(expandable);
    }


    public void showIconNotification(boolean navigationIcon, boolean drawerIcon) {
        toolbarLayout.showNavIconNotification(navigationIcon);
        drawerIcon_badge.setVisibility(drawerIcon ? VISIBLE : GONE);
    }

    public void setDrawerOpen(Boolean open, Boolean animate) {
        if (open) {
            drawerLayout.openDrawer(drawer, animate);
        } else {
            drawerLayout.closeDrawer(drawer, animate);
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


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (toolbarLayout == null) {
            super.addView(child, index, params);
        } else {
            if (viewIdForDrawer == child.getId()) {
                drawer_container.addView(child, index, params);
            } else {
                toolbarLayout.addView(child, index, params);
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init();
    }

}
