package de.dlyt.yanndroid.oneui.tabs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.TintTypedArray;

import de.dlyt.yanndroid.oneui.R;

public class TabItem extends View {
    public final CharSequence text;
    public final Drawable icon;
    public final int customLayout;

    public TabItem(Context context) {
        this(context, null);
    }

    @SuppressLint("RestrictedApi")
    public TabItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.TabItem);
        text = a.getText(R.styleable.TabItem_android_text);
        icon = a.getDrawable(R.styleable.TabItem_android_icon);
        customLayout = a.getResourceId(R.styleable.TabItem_android_layout, 0);
        a.recycle();
    }
}