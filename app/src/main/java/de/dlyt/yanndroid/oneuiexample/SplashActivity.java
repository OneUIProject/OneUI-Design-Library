package de.dlyt.yanndroid.oneuiexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.utils.ThemeColor;
import de.dlyt.yanndroid.oneui.layout.SplashViewAnimated;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SplashViewAnimated splashViewAnimated = findViewById(R.id.splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(splashViewAnimated::startSplashAnimation, 500);

        splashViewAnimated.setSplashAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
}