package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.util.SeslMisc;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textview.MaterialTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class OptionButton extends LinearLayout {

    public static final int TEXTVIEW = 0;
    public static final int IMAGEVIEW = 1;
    public static final int COUNTER = 2;
    private Drawable mIcon;
    private String mText;
    private Boolean mSelected;
    private Boolean mCounterEnabled;
    private Integer mCounter;
    private int mOnTextColor;
    private int mOffTextColor;
    private LinearLayout optionbutton;
    private MaterialTextView textView;
    private ImageView imageview;
    private MaterialTextView counter;

    public OptionButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setClickable(true);
        setFocusable(true);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OptionButton, 0, 0);
        mText = attr.getString(R.styleable.OptionButton_text);
        mIcon = attr.getDrawable(R.styleable.OptionButton_icon);
        mSelected = attr.getBoolean(R.styleable.OptionButton_selected, false);
        mCounterEnabled = attr.getBoolean(R.styleable.OptionButton_counterEnabled, false);
        mCounter = attr.getInteger(R.styleable.OptionButton_counter, 0);
        attr.recycle();

        mOnTextColor = ContextCompat.getColor(getContext(), R.color.sesl_switchbar_text_color);
        mOffTextColor = ContextCompat.getColor(getContext(), R.color.sesl_switchbar_off_text_color);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_drawer_optionbutton, this, true);

        optionbutton = findViewById(R.id.optionbutton);
        textView = findViewById(R.id.optionbutton_text);
        imageview = findViewById(R.id.optionbutton_icon);
        counter = findViewById(R.id.optionbutton_counter);

        textView.setText(mText);
        imageview.setImageDrawable(mIcon);
        counter.setVisibility(mCounterEnabled ? VISIBLE : GONE);
        counter.setText(String.valueOf(mCounter));
        setButtonSelected(mSelected);

    }

    public View getView(@OptionButtonView int view) {
        switch (view) {
            case TEXTVIEW:
                return textView;
            case IMAGEVIEW:
                return imageview;
            case COUNTER:
                return counter;
            default:
                return null;
        }
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
        textView.setText(mText);
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        imageview.setImageDrawable(mIcon);
    }

    public void setButtonSelected(Boolean selected) {
        this.mSelected = selected;
        setTextColor(mSelected);
        textView.setTypeface(Typeface.create(getResources().getString(R.string.sesl_font_family_regular), mSelected ? Typeface.BOLD : Typeface.NORMAL));
    }

    public void toggleButtonSelected() {
        setButtonSelected(!mSelected);
    }

    public Boolean isButtonSelected() {
        return mSelected;
    }

    public void setButtonEnabled(Boolean enabled) {
        setEnabled(enabled);
        optionbutton.setEnabled(enabled);
        imageview.setEnabled(enabled);
        counter.setEnabled(enabled);
        setTextColor(mSelected);
    }

    private void setTextColor(boolean z) {
        float f;
        textView.setTextColor(z ? this.mOnTextColor : this.mOffTextColor);
        if (isEnabled()) {
            f = 1.0f;
        } else if (!SeslMisc.isLightTheme(getContext()) || !z) {
            f = 0.4f;
        } else {
            f = 0.55f;
        }
        textView.setAlpha(f);
    }

    public Integer getCounter() {
        return mCounter;
    }

    public void setCounter(Integer integer) {
        this.mCounter = integer;
        counter.setText(String.valueOf(mCounter));
    }

    public void setCounterEnabled(Boolean enabled) {
        this.mCounterEnabled = enabled;
        counter.setVisibility(mCounterEnabled ? VISIBLE : GONE);
    }

    public void toggleCounterEnabled() {
        setCounterEnabled(!mCounterEnabled);
    }

    public Boolean isCounterEnabled() {
        return mCounterEnabled;
    }

    @IntDef({TEXTVIEW, IMAGEVIEW, COUNTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OptionButtonView {
    }

}
