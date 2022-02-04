package de.dlyt.yanndroid.oneui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import de.dlyt.yanndroid.oneui.R;

public class BottomBarMenuItemView extends LinearLayout {

    private Context context;
    private MenuItem menuItem;
    private ImageView iconView;
    private TextView titleView;

    public BottomBarMenuItemView(Context context, MenuItem menuItem) {
        super(context);
        this.context = context;
        this.menuItem = menuItem;

        initView();
        updateView();
    }

    public BottomBarMenuItemView(Context context, CharSequence text, @DrawableRes int resId) {
        super(context);
        this.context = context;

        initView();
        titleView.setText(text);
        iconView.setImageResource(resId);
    }

    private void initView() {
        int hMargin = (int) getResources().getDimension(R.dimen.bottom_bar_item_spacing);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
        containerParams.setMargins(hMargin, 0, hMargin, 0);
        setLayoutParams(containerParams);
        setBackgroundResource(R.drawable.oui_light_view_rectangle_bottomitem);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.oui_menu_item_bottom_bar, this, true);
        iconView = findViewById(R.id.menu_item_icon);
        titleView = findViewById(R.id.menu_item_title);
    }

    public void updateView() {
        if (menuItem != null) {
            setVisibility(menuItem.isVisible() ? VISIBLE : GONE);

            boolean enabled = menuItem.isEnabled();
            setEnabled(enabled);
            titleView.setEnabled(enabled);
            iconView.setEnabled(enabled);

            iconView.setImageDrawable(menuItem.getIcon());
            titleView.setText(menuItem.getTitle());
        }
    }
}
