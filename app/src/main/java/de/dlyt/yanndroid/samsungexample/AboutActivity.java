package de.dlyt.yanndroid.samsungexample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import de.dlyt.yanndroid.samsung.ThemeColor;
import de.dlyt.yanndroid.samsung.layout.AboutPage;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ThemeColor(this);
        setContentView(R.layout.activity_about);

        AboutPage about_page = findViewById(R.id.about_page);
        about_page.initAboutPage(this);

        ((MaterialButton) findViewById(R.id.about_btn1)).setOnClickListener(v -> about_page.setUpdateState(AboutPage.LOADING));
        ((MaterialButton) findViewById(R.id.about_btn2)).setOnClickListener(v -> about_page.setUpdateState(AboutPage.NO_UPDATE));
        ((MaterialButton) findViewById(R.id.about_btn3)).setOnClickListener(v -> about_page.setUpdateState(AboutPage.UPDATE_AVAILABLE));


        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

    }
}