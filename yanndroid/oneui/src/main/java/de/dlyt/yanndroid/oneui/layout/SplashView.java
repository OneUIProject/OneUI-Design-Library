package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class SplashView extends LinearLayout {

    private boolean animated;
    private Drawable mImage_foreground;
    private Drawable mImage_background;
    private String mText;
    private Animation splash_anim;
    private MaterialTextView textView;
    private ImageView imageview_foreground;
    private ImageView imageview_background;
    private Drawable mImage;
    private ImageView imageview;

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SplashView, 0, 0);

        try {
            animated = attr.getBoolean(R.styleable.SplashView_animated, true);

            if (animated) {
                mText = attr.getString(R.styleable.SplashView_text);
                mImage_foreground = attr.getDrawable(R.styleable.SplashView_foreground_image);
                mImage_background = attr.getDrawable(R.styleable.SplashView_background_image);
                splash_anim = AnimationUtils.loadAnimation(context, attr.getResourceId(R.styleable.SplashView_animation, R.anim.sesl_splash_animation));
            } else {
                mText = attr.getString(R.styleable.SplashView_text);
                mImage = attr.getDrawable(R.styleable.SplashView_image);
            }

        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(animated ? R.layout.samsung_splashview_animated : R.layout.samsung_splashview_simple, this, true);


        textView = findViewById(R.id.sesl_splash_text);
        textView.setText(mText);

        if (animated) {
            imageview_foreground = findViewById(R.id.sesl_splash_image_foreground);
            imageview_background = findViewById(R.id.sesl_splash_image_background);

            imageview_foreground.setImageDrawable(mImage_foreground);
            imageview_background.setImageDrawable(mImage_background);
        } else {
            imageview = findViewById(R.id.sesl_splash_image);
            imageview.setImageDrawable(mImage);
        }


    }

    public void setSplashAnimationListener(Animation.AnimationListener listener) {
        if (animated) splash_anim.setAnimationListener(listener);
    }

    public void startSplashAnimation() {
        if (animated) imageview_foreground.startAnimation(splash_anim);
    }

    public void clearSplashAnimation() {
        if (animated) imageview_foreground.clearAnimation();
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        textView.setText(mText);
    }

    public void setImage(Drawable foreground, Drawable background) {
        if (animated) {
            this.mImage_foreground = foreground;
            this.mImage_background = background;
            imageview_foreground.setImageDrawable(foreground);
            imageview_background.setImageDrawable(background);
        }
    }

    public void setImage(Drawable image) {
        if (!animated) {
            this.mImage = image;
            imageview.setImageDrawable(image);
        }
    }
}
