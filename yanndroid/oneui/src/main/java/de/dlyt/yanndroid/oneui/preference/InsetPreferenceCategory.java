package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;

import de.dlyt.yanndroid.oneui.R;

public class InsetPreferenceCategory extends PreferenceCategory {
    private int mHeight;

    public InsetPreferenceCategory(Context context) {
        this(context, null);
    }

    public InsetPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHeight = (int) context.getResources().getDimension(R.dimen.sesl_list_subheader_min_height);
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.InsetPreferenceCategory);
            mHeight = styledAttrs.getDimensionPixelSize(R.styleable.InsetPreferenceCategory_height, mHeight);
            styledAttrs.recycle();

            TypedArray categoryAttrs = context.obtainStyledAttributes(attrs, R.styleable.PreferenceCategory);
            seslSetSubheaderRoundedBackground(categoryAttrs.getInt(R.styleable.PreferenceCategory_roundStroke, SeslRoundedCorner.ROUNDED_CORNER_ALL));
            categoryAttrs.recycle();
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.height = mHeight;
        holder.itemView.setLayoutParams(params);
    }

    public void setHeight(int height) {
        if (height >= 0) {
            mHeight = height;
        }
    }
}