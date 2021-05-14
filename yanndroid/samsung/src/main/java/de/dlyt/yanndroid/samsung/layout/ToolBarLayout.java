package de.dlyt.yanndroid.samsung.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;

import de.dlyt.yanndroid.samsung.R;

public class ToolBarLayout extends Toolbar {

    private Drawable mNavigationIcon;
    private String mTitle;
    private String mSubtitle;

    private ImageView navigation_icon;
    private MaterialTextView expanded_title;
    private MaterialTextView expanded_subtitle;
    private MaterialTextView collapsed_title;

    private LinearLayout main_container;


    public ToolBarLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);


        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0);

        try {
            mTitle = attr.getString(R.styleable.ToolBarLayout_title);
            mSubtitle = attr.getString(R.styleable.ToolBarLayout_subtitle);
            mNavigationIcon = attr.getDrawable(R.styleable.ToolBarLayout_navigationIcon);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_toolbarlayout, this, true);

        main_container = findViewById(R.id.main_container);

        navigation_icon = findViewById(R.id.navigationIcon);
        expanded_title = findViewById(R.id.expanded_title);
        expanded_subtitle = findViewById(R.id.expanded_subtitle);
        collapsed_title = findViewById(R.id.collapsed_title);

        expanded_title.setText(mTitle);
        collapsed_title.setText(mTitle);
        navigation_icon.setImageDrawable(mNavigationIcon);

        //init();

    }






}
