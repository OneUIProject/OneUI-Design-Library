package de.dlyt.yanndroid.oneui.sesl.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import de.dlyt.yanndroid.oneui.R;

public class ActionModeBottomBarButton extends LinearLayout {

    private ImageView icon;
    private TextView textView;

    public ActionModeBottomBarButton(Context context) {
        super(context);

        int hMargin = (int) getResources().getDimension(R.dimen.bottom_bar_item_spacing);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        containerParams.setMargins(hMargin, 0, hMargin, 0);
        setLayoutParams(containerParams);
        setBackgroundResource(R.drawable.ripple_light_view_rectangle_bottomitem);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        icon = new ImageView(getContext());
        int iconSize = (int) getResources().getDimension(R.dimen.bottom_bar_item_image_size);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        containerParams.setMargins(0, 0, 0, 1);
        icon.setLayoutParams(iconParams);
        icon.setColorFilter(ContextCompat.getColor(getContext(), R.color.bottom_bar_item_image_color), android.graphics.PorterDuff.Mode.SRC_IN);
        addView(icon);

        textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setMaxLines(1);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.bottom_bar_item_text_size));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.bottom_bar_item_text_color));
        addView(textView);
    }

    public void setText(CharSequence text) {
        this.textView.setText(text);
    }

    public void setIcon(Drawable icon) {
        this.icon.setImageDrawable(icon);
    }

}
