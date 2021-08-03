package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class SplashViewSimple extends LinearLayout {

    private Drawable mImage;
    private String mText;

    private MaterialTextView textView;
    private ImageView imageview;

    public static final int TEXTVIEW = 0;
    public static final int IMAGEVIEW = 1;

    @IntDef({TEXTVIEW, IMAGEVIEW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SplashViewSimpleView {
    }

    public View getView(@SplashViewSimpleView int view) {
        switch (view) {
            case TEXTVIEW:
                return textView;
            case IMAGEVIEW:
                return imageview;
            default:
                return null;
        }
    }

    public SplashViewSimple(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SplashViewSimple, 0, 0);

        try {
            mText = attr.getString(R.styleable.SplashViewSimple_text);
            mImage = attr.getDrawable(R.styleable.SplashViewSimple_image);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_splashview_simple, this, true);


        textView = findViewById(R.id.sesl_splash_text);

        imageview = findViewById(R.id.sesl_splash_image);

        textView.setText(mText);
        imageview.setImageDrawable(mImage);

    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        textView.setText(mText);
    }

    public void setImage(Drawable mImage) {
        this.mImage = mImage;
        imageview.setImageDrawable(mImage);
    }
}
