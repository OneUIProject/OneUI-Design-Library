package de.dlyt.yanndroid.oneui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.menu.widget.MenuDivider;

class PopupMenuItemView extends LinearLayout {

    private boolean mIsOneUI4;

    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

    private Context context;
    private MenuItem menuItem;
    private LinearLayout containerView;

    private MenuDivider menuDivider;

    private LinearLayout contentView;
    private ImageView iconView;
    private TextView titleView;

    private TextView badgeView;
    private CheckBox checkBox;
    private ImageView arrowView;

    private boolean mGroupDividerEnabled;

    PopupMenuItemView(Context context, MenuItem menuItem, boolean groupDividerEnabled) {
        super(context);
        this.context = context;
        this.menuItem = menuItem;
        mGroupDividerEnabled = groupDividerEnabled;

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.oui_menu_item_popup_item, this, true);

        containerView = findViewById(R.id.menu_item_container);
        menuDivider = findViewById(R.id.menu_group_divider);
        contentView = findViewById(R.id.menu_item_content);
        contentView.setBackground(context.getResources().getDrawable(mIsOneUI4 ? R.drawable.sesl4_list_selector : R.drawable.sesl_list_selector, context.getTheme()));
        iconView = findViewById(R.id.menu_item_icon);
        titleView = findViewById(R.id.menu_item_title);
        titleView.setTextSize(0, context.getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_popup_menu_item_text_size : R.dimen.sesl_popup_menu_item_text_size));
        badgeView = findViewById(R.id.menu_item_badge);
        checkBox = findViewById(R.id.menu_item_check_box);
        arrowView = findViewById(R.id.menu_item_arrow);
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL)
            arrowView.setImageResource(R.drawable.ic_samsung_arrow_left);

        updateView();
    }

    MenuItem getMenuItem() {
        return menuItem;
    }

    void setGroupDividerEnabled(boolean enabled) {
        mGroupDividerEnabled = enabled;
        updateView();
    }

    void updateView() {
        //visible state
        containerView.setVisibility(menuItem.isVisible() ? VISIBLE : GONE);

        //enabled state
        boolean enabled = menuItem.isEnabled();
        setEnabled(enabled);
        titleView.setEnabled(enabled);
        iconView.setEnabled(enabled);
        checkBox.setEnabled(enabled);
        badgeView.setEnabled(enabled);
        arrowView.setEnabled(enabled);
        setClickable(!enabled);

        //divider
        menuDivider.setVisibility(mGroupDividerEnabled ? VISIBLE : GONE);

        //icon
        iconView.setVisibility(menuItem.getIcon() == null ? GONE : VISIBLE);
        iconView.setImageDrawable(menuItem.getIcon());

        //title
        titleView.setText(menuItem.getTitle());

        //badge
        int badgeCount = menuItem.getBadge();

        if (badgeCount > 0) {
            if (badgeCount > 99) badgeCount = 99;

            String countString = numberFormat.format(badgeCount);
            badgeView.setText(countString);

            int width = (int) (context.getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * context.getResources().getDimension(R.dimen.sesl_badge_additional_width));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) badgeView.getLayoutParams();
            lp.width = width;
            badgeView.setLayoutParams(lp);
            badgeView.setVisibility(VISIBLE);
        } else if (badgeCount == -1) {
            badgeView.setText("N");
            badgeView.setVisibility(VISIBLE);
        } else {
            badgeView.setVisibility(GONE);
        }

        //checkbox
        checkBox.setVisibility(menuItem.isCheckable() ? VISIBLE : GONE);
        checkBox.setChecked(menuItem.isChecked());

        //submenu arrow
        arrowView.setVisibility(menuItem.hasSubMenu() ? VISIBLE : GONE);
    }
}
