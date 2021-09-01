package de.dlyt.yanndroid.oneui.layout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;

public class DrawerLayout extends LinearLayout {


    public static final int N_BADGE = -1;
    private Context mContext;
    private int mLayout;
    private String mToolbarTitle;
    private String mToolbarSubtitle;
    private Boolean mToolbarExpanded;
    private Drawable mDrawerIcon;
    private FrameLayout drawerButtonContainer;
    private ToolbarImageButton drawerButton;
    private ViewGroup drawerIconBadgeBackground;
    private TextView drawerIconBadgeText;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private ToolbarLayout toolbarLayout;
    private LinearLayout drawer_container;
    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private View drawer;

    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawerLayout, 0, 0);

        try {
            mLayout = attr.getResourceId(R.styleable.DrawerLayout_android_layout, R.layout.samsung_drawerlayout);
            mToolbarTitle = attr.getString(R.styleable.DrawerLayout_toolbar_title);
            mToolbarSubtitle = attr.getString(R.styleable.DrawerLayout_toolbar_subtitle);
            mDrawerIcon = attr.getDrawable(R.styleable.DrawerLayout_drawer_icon);
            mToolbarExpanded = attr.getBoolean(R.styleable.DrawerLayout_toolbar_expanded, true);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(mLayout, this, true);

        drawer_container = findViewById(R.id.drawer_container);
        toolbarLayout = findViewById(R.id.drawer_toolbarlayout);

        toolbarLayout.setTitle(mToolbarTitle);
        toolbarLayout.setSubtitle(mToolbarSubtitle);
        toolbarLayout.setNavigationButtonTooltip(getResources().getText(R.string.sesl_navigation_drawer));
        toolbarLayout.setExpanded(mToolbarExpanded, false);
        drawerButtonContainer = findViewById(R.id.drawer_layout_drawerButton_container);
        drawerButton = findViewById(R.id.drawer_layout_drawerButton);

        drawerButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.drawer_icon_color)));
        setDrawerButtonIcon(mDrawerIcon);


        /*drawer logic*/
        View translationView = findViewById(R.id.drawer_custom_translation);
        if (translationView == null)
            translationView = toolbarLayout;

        View content = translationView;

        drawerLayout = findViewById(R.id.drawerLayout);
        drawer = findViewById(R.id.drawer);

        drawerLayout.setScrimColor(ContextCompat.getColor(getContext(), R.color.drawer_dim_color));
        drawerLayout.setDrawerElevation(0);

        setDrawerWidth();

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

    private void setDrawerWidth() {
        ViewGroup.LayoutParams layoutParams = drawer.getLayoutParams();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int displayWidth = p.x;
        float density = getResources().getDisplayMetrics().density;
        float dpi = (float) displayWidth / density;

        double widthRate;
        if (dpi >= 1920.0F) {
            widthRate = 0.22D;
        } else if (dpi >= 960.0F && dpi < 1920.0F) {
            widthRate = 0.2734D;
        } else if (dpi >= 600.0F && dpi < 960.0F) {
            widthRate = 0.46D;
        } else if (dpi >= 480.0F && dpi < 600.0F) {
            widthRate = 0.5983D;
        } else {
            widthRate = 0.844D;
        }

        layoutParams.width = (int) ((double) displayWidth * widthRate);
    }

    public ToolbarLayout getToolbarLayout() {
        return toolbarLayout;
    }

    public void setDrawerButtonOnClickListener(OnClickListener listener) {
        drawerButton.setOnClickListener(listener);
    }

    public void setDrawerButtonTooltip(CharSequence tooltipText) {
        drawerButton.setTooltipText(tooltipText);
    }

    public void setToolbarTitle(CharSequence title) {
        toolbarLayout.setTitle(title);
    }

    public void setToolbarTitle(CharSequence expandedTitle, CharSequence collapsedTitle) {
        toolbarLayout.setTitle(expandedTitle, collapsedTitle);
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

    public void setDrawerButtonIcon(Drawable drawerIcon) {
        mDrawerIcon = drawerIcon;
        drawerButton.setImageDrawable(mDrawerIcon);
        drawerButtonContainer.setVisibility(drawerIcon != null ? View.VISIBLE : View.GONE);
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
        if (toolbarLayout == null || drawer_container == null) {
            super.addView(child, index, params);
        } else {
            ToolbarLayout.Drawer_Toolbar_LayoutParams lp = (ToolbarLayout.Drawer_Toolbar_LayoutParams) params;
            switch (lp.layout_location) {
                case 0:
                case 1:
                    toolbarLayout.addView(child, index, params);
                    break;
                case 2:
                    drawer_container.addView(child, index, params);
                    break;
            }
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new ToolbarLayout.Drawer_Toolbar_LayoutParams(getContext(), null);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ToolbarLayout.Drawer_Toolbar_LayoutParams(getContext(), attrs);
    }


    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDrawerWidth();
    }


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

}
