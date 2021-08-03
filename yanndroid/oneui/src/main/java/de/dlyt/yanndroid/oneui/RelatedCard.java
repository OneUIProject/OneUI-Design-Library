package de.dlyt.yanndroid.oneui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

public class RelatedCard extends LinearLayout {

    private MaterialTextView related_title;
    private LinearLayout related_content;
    private String mTitle;

    public RelatedCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RelatedCard, 0, 0);
        try {
            mTitle = attr.getString(R.styleable.RelatedCard_title);
        } finally {
            attr.recycle();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_related_card, this, true);

        related_title = findViewById(R.id.related_title);
        related_content = findViewById(R.id.related_content);
        related_title.setText(mTitle);

    }

    public void setTitle(String title) {
        this.mTitle = title;
        related_title.setText(mTitle);
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (related_content == null) {
            super.addView(child, index, params);
        } else {
            related_content.addView(child, index, params);
        }
    }

}
