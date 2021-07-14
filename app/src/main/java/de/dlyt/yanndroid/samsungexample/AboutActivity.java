package de.dlyt.yanndroid.samsungexample;

import android.os.Bundle;

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

        MaterialButton about_btn1 = findViewById(R.id.about_btn1);
        MaterialButton about_btn2 = findViewById(R.id.about_btn2);
        MaterialButton about_btn3 = findViewById(R.id.about_btn3);
        about_btn1.setOnClickListener(v -> about_page.setUpdateState(AboutPage.LOADING));
        about_btn2.setOnClickListener(v -> about_page.setUpdateState(AboutPage.NO_UPDATE));
        about_btn3.setOnClickListener(v -> about_page.setUpdateState(AboutPage.UPDATE_AVAILABLE));

    }
}