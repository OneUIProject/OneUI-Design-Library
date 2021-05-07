package de.dlyt.yanndroid.samsung;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

public class SplashView extends LinearLayout {

    private Drawable mImage;
    private String mText;

    private MaterialTextView textView;
    private ImageView imageview;

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SplashView, 0, 0);

        try {
            mText = attr.getString(R.styleable.SplashView_text);
            mImage = attr.getDrawable(R.styleable.SplashView_image);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sesl_splashview, this, true);


        textView = findViewById(R.id.sesl_splash_text);

        imageview = findViewById(R.id.sesl_splash_image);

        textView.setText(mText);
        imageview.setImageDrawable(mImage);

    }

    public void setText(String mText) {
        this.mText = mText;
        textView.setText(mText);
    }

    public String getText() {
        return mText;
    }

    public void setImage(Drawable mImage) {
        this.mImage = mImage;
        imageview.setImageDrawable(mImage);
    }
}
