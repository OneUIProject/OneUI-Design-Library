package de.dlyt.yanndroid.samsung.drawer;

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

public class OptionButton extends LinearLayout {

    private Drawable mIcon;
    private String mText;
    private MaterialTextView textView;
    private ImageView imageview;

    public OptionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OptionButton, 0, 0);

        try {
            mText = attr.getString(R.styleable.OptionButton_text);
            mIcon = attr.getDrawable(R.styleable.OptionButton_icon);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sesl_drawer_optionbutton, this, true);

        textView = findViewById(R.id.optionbutton_text);
        imageview = findViewById(R.id.optionbutton_icon);

        textView.setText(mText);
        imageview.setImageDrawable(mIcon);

    }
}
