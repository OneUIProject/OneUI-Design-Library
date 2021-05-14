package de.dlyt.yanndroid.samsung;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;

public class DrawerLayout extends LinearLayout {


    private Drawable mDrawerIcon;
    private String mToolbarTitle;
    private String mToolbarSubtitle;


    private ImageView drawer_icon;


    private LinearLayout main_container;
    private LinearLayout drawer_container;

    AttributeSet attrsss;


    public DrawerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        attrsss = attrs;

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawerLayout, 0, 0);

        try {
            mToolbarTitle = attr.getString(R.styleable.DrawerLayout_toolbar_title);
            mToolbarSubtitle = attr.getString(R.styleable.DrawerLayout_toolbar_subtitle);
            mDrawerIcon = attr.getDrawable(R.styleable.DrawerLayout_drawer_icon);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_drawerlayout, this, true);

        main_container = findViewById(R.id.main_container);
        drawer_container = findViewById(R.id.drawer_container);

        drawer_icon = findViewById(R.id.drawer_icon);
        drawer_icon.setImageDrawable(mDrawerIcon);

        initFunction();

    }

    private void initFunction() {
        View content = findViewById(R.id.main_content);
        androidx.drawerlayout.widget.DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        View drawer = findViewById(R.id.drawer);

        /*ImageView navigationIcon = findViewById(R.id.navigationIcon);
        View navigationIcon_Badge = findViewById(R.id.navigationIcon_new_badge);
        navigationIcon_Badge.setVisibility(View.VISIBLE);*/

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

        /*navigationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawer, true);
            }
        });*/


        /*View drawer_settings = findViewById(R.id.drawer_icon);
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent().setClass(getApplicationContext(), SettingsActivity.class));
            }
        });*/

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
        if (main_container == null) {
            super.addView(child, index, params);
        } else {
            //drawer_container.addView(child, index, params);
            main_container.addView(child, index, params);
        }
    }


}
