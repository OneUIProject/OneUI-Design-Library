package de.dlyt.yanndroid.oneuiexample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.layout.SplashView;
import de.dlyt.yanndroid.oneui.utils.ThemeColor;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SplashView splashView = findViewById(R.id.splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(splashView::startSplashAnimation, 500);

        splashView.setSplashAnimationListener(new Animation.AnimationListener() {
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