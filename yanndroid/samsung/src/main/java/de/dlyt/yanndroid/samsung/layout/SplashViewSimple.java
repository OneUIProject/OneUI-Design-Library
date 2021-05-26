package de.dlyt.yanndroid.samsung.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import de.dlyt.yanndroid.samsung.R;


/**
 * Usage xml:
 * <pre>
 *     app:image="..."      Splash image
 *     app:text="..."       Splash text
 * </pre>
 *
 * <p>For more help, see <a
 * href="https://github.com/Yanndroid/SamsungDesign/">SamsungDesign</a>on Github.
 */


public class SplashViewSimple extends LinearLayout {

    private Drawable mImage;
    private String mText;

    private MaterialTextView textView;
    private ImageView imageview;

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
