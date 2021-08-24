package de.dlyt.yanndroid.oneui.layout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.ToolbarImageButton;

public class DrawerLayout extends LinearLayout {

    private String mToolbarTitle;
    private String mToolbarSubtitle;
    private Boolean mToolbarExpanded;

    private Drawable mDrawerIcon;
    private FrameLayout drawerButtonContainer;
    private ToolbarImageButton drawerButton;
    private ViewGroup drawerIconBadgeBackground;
    private TextView drawerIconBadgeText;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    public static int N_BADGE = -1;

    private ToolbarLayout toolbarLayout;
    private LinearLayout drawer_container;

    private int viewIdForDrawer;

    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private View drawer;


    public static final int DRAWER_BUTTON = 0;
    public static final int TOOLBAR = 1;
    public static final int CONTENT_LAYOUT = 2;
    public static final int DRAWER_LAYOUT = 3;
    public static final int DRAWER = 4;

    @IntDef({DRAWER_BUTTON, TOOLBAR, CONTENT_LAYOUT, DRAWER_LAYOUT, DRAWER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DrawerLayoutView {
    }

    public View getView(@DrawerLayoutView int view) {
        switch (view) {
            case DRAWER_BUTTON:
                return drawerButton;
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
            mToolbarExpanded = attr.getBoolean(R.styleable.DrawerLayout_toolbar_expanded, true);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_drawerlayout, this, true);

        drawer_container = findViewById(R.id.drawer_container);
        toolbarLayout = findViewById(R.id.drawer_toolbarlayout);

        toolbarLayout.setTitle(mToolbarTitle);
        toolbarLayout.setSubtitle(mToolbarSubtitle);
        toolbarLayout.setNavigationButtonIcon(getResources().getDrawable(R.drawable.ic_samsung_drawer, context.getTheme()));
        toolbarLayout.setNavigationIconTooltip(getResources().getText(R.string.sesl_navigation_drawer));
        toolbarLayout.setExpanded(mToolbarExpanded, false);
        drawerButtonContainer = findViewById(R.id.drawer_layout_drawerButton_container);
        drawerButton = findViewById(R.id.drawer_layout_drawerButton);

        drawerButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.drawer_icon_color)));
        setDrawerButtonIcon(mDrawerIcon);


        /*drawer logic*/
        View content = findViewById(R.id.toolbar_layout_coordinator_layout);
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
        drawerButton.setOnClickListener(listener);
    }

    public void setDrawerButtonTooltip(CharSequence tooltipText) {
        drawerButton.setTooltipText(tooltipText);
    }


    public void setToolbarTitle(String title) {
        toolbarLayout.setTitle(title);
    }

    public void setToolbarSubtitle(String subtitle) {
        toolbarLayout.setSubtitle(subtitle);
    }

    public void setToolbarExpanded(boolean expanded, boolean animate) {
        toolbarLayout.setExpanded(expanded, animate);
    }


    public void setButtonBadges(int navigationIcon, int drawerIcon) {
        toolbarLayout.setNavigationButtonBadge(navigationIcon);
        setDrawerButtonBadge(drawerIcon);
    }

    public void setDrawerButtonIcon(Drawable navigationIcon) {
        mDrawerIcon = navigationIcon;
        drawerButton.setImageDrawable(mDrawerIcon);
        drawerButtonContainer.setVisibility(navigationIcon != null ? View.VISIBLE : View.GONE);
    }

    public void setDrawerButtonBadge(int count) {
        if (drawerIconBadgeBackground == null) {
            drawerIconBadgeBackground = (ViewGroup) ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.navigation_button_badge_layout, drawerButtonContainer, false);
            drawerIconBadgeText = (TextView) drawerIconBadgeBackground.getChildAt(0);
            drawerIconBadgeText.setTextSize(0, (float) ((int) getResources().getDimension(R.dimen.sesl_menu_item_badge_text_size)));
            drawerButtonContainer.addView(drawerIconBadgeBackground);
        }
        if (drawerIconBadgeText != null) {
            if (count > 0) {
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                drawerIconBadgeText.setText(countString);
                int width = (int) (getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * getResources().getDimension(R.dimen.sesl_badge_additional_width));
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) drawerIconBadgeBackground.getLayoutParams();
                lp.width = width;
                lp.height = (int) getResources().getDimension(R.dimen.sesl_menu_item_badge_size);
                drawerIconBadgeBackground.setLayoutParams(lp);
                drawerIconBadgeBackground.setVisibility(View.VISIBLE);
            } else if (count == N_BADGE) {
                drawerIconBadgeText.setText(getResources().getString(R.string.sesl_action_menu_overflow_badge_text_n));
                drawerIconBadgeBackground.setVisibility(View.VISIBLE);
            } else {
                drawerIconBadgeBackground.setVisibility(View.GONE);
            }
        }
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
