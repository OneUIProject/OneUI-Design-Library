package de.dlyt.yanndroid.oneui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class RelatedCard extends LinearLayout {

    private TextView mRelatedCardTitle;
    private LinearLayout mRelatedCardContainer;
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

        mRelatedCardTitle = findViewById(R.id.related_card_title);
        mRelatedCardContainer = findViewById(R.id.related_card_container);
        mRelatedCardTitle.setText(mTitle);

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
        mRelatedCardTitle.setText(mTitle);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mRelatedCardContainer == null) {
            super.addView(child, index, params);
        } else {
            mRelatedCardContainer.addView(child, index, params);
        }
    }

}
