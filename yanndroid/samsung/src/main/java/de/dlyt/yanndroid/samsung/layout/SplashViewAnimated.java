package de.dlyt.yanndroid.samsung.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import de.dlyt.yanndroid.samsung.R;

public class SplashViewAnimated extends LinearLayout {

    private Drawable mImage_foreground;
    private Drawable mImage_background;
    private String mText;

    private Animation splash_anim;

    private MaterialTextView textView;
    private ImageView imageview_foreground;
    private ImageView imageview_background;

    public SplashViewAnimated(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SplashViewAnimated, 0, 0);

        try {
            mText = attr.getString(R.styleable.SplashViewAnimated_text);
            mImage_foreground = attr.getDrawable(R.styleable.SplashViewAnimated_foreground_image);
            mImage_background = attr.getDrawable(R.styleable.SplashViewAnimated_background_image);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_splashview_animated, this, true);


        textView = findViewById(R.id.sesl_splash_text);

        imageview_foreground = findViewById(R.id.sesl_splash_image_foreground);
        imageview_background = findViewById(R.id.sesl_splash_image_background);

        textView.setText(mText);
        imageview_foreground.setImageDrawable(mImage_foreground);
        imageview_background.setImageDrawable(mImage_background);


        splash_anim = AnimationUtils.loadAnimation(context, R.anim.sesl_splash_animation);


    }


    public void setSplashAnimationListener(Animation.AnimationListener listener) {
        splash_anim.setAnimationListener(listener);
    }

    public void startSplashAnimation() {
        imageview_foreground.startAnimation(splash_anim);

    }

    public void clearSplashAnimation() {
        imageview_foreground.clearAnimation();
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        textView.setText(mText);
    }

    public void setImage(Drawable foreground, Drawable background) {
        this.mImage_foreground = foreground;
        this.mImage_background = background;
        imageview_foreground.setImageDrawable(foreground);
        imageview_background.setImageDrawable(background);
    }
}
