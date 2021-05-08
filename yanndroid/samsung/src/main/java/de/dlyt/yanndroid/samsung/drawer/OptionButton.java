package de.dlyt.yanndroid.samsung.drawer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SeslToggleSwitch;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.R;
import de.dlyt.yanndroid.samsung.SwitchBar;

public class OptionButton extends LinearLayout {

    private Drawable mIcon;
    private String mText;
    private Boolean mSelected;
    private MaterialTextView textView;
    private ImageView imageview;

    public OptionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OptionButton, 0, 0);

        try {
            mText = attr.getString(R.styleable.OptionButton_text);
            mIcon = attr.getDrawable(R.styleable.OptionButton_icon);
            mSelected = attr.getBoolean(R.styleable.OptionButton_selected, false);
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


    public void setText(String text) {
        this.mText = text;
        textView.setText(mText);
    }

    public String getText() {
        return mText;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        imageview.setImageDrawable(mIcon);
    }

    public void setButtonSelected(Boolean selected) {
        this.mSelected = selected;
        textView.setTextColor(ContextCompat.getColor(getContext(), mSelected ? R.color.sesl_primary_color : R.color.item_color));
        textView.setTypeface(null, mSelected ? Typeface.BOLD : Typeface.NORMAL);
    }

    public void toggleButtonSelected() {
        setButtonSelected(!mSelected);
    }

    public Boolean isButtonSelcted() {
        return mSelected;
    }
}
