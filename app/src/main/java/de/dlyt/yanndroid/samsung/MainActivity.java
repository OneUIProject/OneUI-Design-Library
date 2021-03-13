package de.dlyt.yanndroid.samsung;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslProgressBar;
import androidx.appcompat.widget.SeslSeekBar;
import androidx.appcompat.widget.SeslSwitchBar;
import androidx.appcompat.widget.SeslToggleSwitch;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.AppBarLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        initDrawer();


        demo();


    }

    public void demo(){
        SeslSeekBar seslSeekBar = findViewById(R.id.seekbar1);
        seslSeekBar.setMode(5);
        seslSeekBar.setOverlapPointForDualColor(70);

        SeekBar seekBar = findViewById(R.id.seekbar2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seslSeekBar.setSecondaryProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeslSwitchBar seslSwitchbar = findViewById(R.id.switchbar1);
        seslSwitchbar.addOnSwitchChangeListener(new SeslSwitchBar.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean z) {
                seslSwitchbar.setEnabled(false);
                seslSwitchbar.setProgressBarVisible(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        seslSwitchbar.setEnabled(true);
                        seslSwitchbar.setProgressBarVisible(false);
                    }
                }, 700);
            }
        });
    }



    public void initDrawer() {
        View content = findViewById(R.id.main_content);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        View drawer = findViewById(R.id.drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        drawerLayout.setScrimColor(ContextCompat.getColor(getBaseContext(), R.color.drawer_dim_color));
        drawerLayout.setDrawerElevation(0);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.opend, R.string.closed) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                content.setTranslationX(slideX);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawer, true);
            }
        });


        /**Items*/
        View drawer_settings = findViewById(R.id.drawer_settings);
        drawer_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**settings*/
            }
        });

    }

    public void initToolbar() {
        /** Def */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout AppBar = findViewById(R.id.app_bar);

        TextView expanded_title = findViewById(R.id.expanded_title);
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        TextView collapsed_title = findViewById(R.id.collapsed_title);

        /** 1/3 of the Screen */
        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);


        /** Collapsing */
        AppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = (AppBar.getY() / AppBar.getTotalScrollRange());
                expanded_title.setAlpha(1 - (percentage * 2 * -1));
                expanded_subtitle.setAlpha(1 - (percentage * 2 * -1));
                collapsed_title.setAlpha(percentage * -1);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

}