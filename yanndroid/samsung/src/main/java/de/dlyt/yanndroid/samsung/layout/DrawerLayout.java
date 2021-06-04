package de.dlyt.yanndroid.samsung.layout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;

import de.dlyt.yanndroid.samsung.R;


/**
 * Usage xml:
 * <pre>
 *     app:drawer_icon="..."        Drawer icon in the top right corner
 *     app:drawer_viewId="..."      Id of the view which goes in the drawer
 *     app:toolbar_subtitle="..."   Toolbar subtitle
 *     app:toolbar_title="..."      Toolbar title
 * </pre>
 *
 * <p>For more help, see <a
 * href="https://github.com/Yanndroid/SamsungDesign/">SamsungDesign</a>on Github.
 */

public class DrawerLayout extends LinearLayout {


    private Drawable mDrawerIcon;
    private String mToolbarTitle;
    private String mToolbarSubtitle;


    private ImageView drawerIcon;
    private ToolbarLayout toolbarLayout;
    private TextView drawerIcon_badge;


    private LinearLayout drawer_container;


    private int viewIdForDrawer;


    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;
    private View drawer;


    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawerLayout, 0, 0);

        try {
            mToolbarTitle = attr.getString(R.styleable.DrawerLayout_toolbar_title);
            mToolbarSubtitle = attr.getString(R.styleable.DrawerLayout_toolbar_subtitle);
            mDrawerIcon = attr.getDrawable(R.styleable.DrawerLayout_drawer_icon);
            viewIdForDrawer = attr.getResourceId(R.styleable.DrawerLayout_drawer_viewId, -2);
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
        drawerIcon = findViewById(R.id.drawerIcon);
        drawerIcon.setImageDrawable(mDrawerIcon);




        /*drawer logic*/
        View content = findViewById(R.id.main_content);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawer = findViewById(R.id.drawer);

        ViewGroup.LayoutParams layoutParams = drawer.getLayoutParams();
        layoutParams.width = (int) ((double) this.getResources().getDisplayMetrics().widthPixels / 1.19);
        drawerLayout.setScrimColor(ContextCompat.getColor(getContext(), R.color.drawer_dim_color));
        drawerLayout.setDrawerElevation(0);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.opend, R.string.closed) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                content.setTranslationX(slideX);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        toolbarLayout.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawer, true);
            }
        });

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

    public void setToolbarExpanded(boolean expanded, boolean animate) {
        toolbarLayout.setExpanded(expanded, animate);
    }


    public void showIconNotification(boolean navigationIcon, boolean drawerIcon) {
        toolbarLayout.showNavIconNotification(navigationIcon);
        drawerIcon_badge.setVisibility(drawerIcon ? VISIBLE : GONE);
    }

    public void setDrawerOpen(Boolean open, Boolean animate) {
        if (open){
            drawerLayout.openDrawer(drawer, animate);
        }else {
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

}
