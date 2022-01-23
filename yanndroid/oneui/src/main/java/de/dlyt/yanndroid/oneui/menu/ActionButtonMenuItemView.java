package de.dlyt.yanndroid.oneui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.widget.ToolbarImageButton;

public class ActionButtonMenuItemView extends LinearLayout {

    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());

    private Context context;
    private Menu menu;
    private MenuItem menuItem;
    private ToolbarImageButton iconView;
    private TextView badgeView;
    private TextView titleView;

    public ActionButtonMenuItemView(Context context, MenuItem menuItem) {
        super(context);
        this.context = context;
        this.menuItem = menuItem;

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initView();
        updateView();
    }

    public ActionButtonMenuItemView(Context context, Menu menu, CharSequence text, @DrawableRes int resId) {
        super(context);
        this.context = context;
        this.menu = menu;

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMarginStart(getResources().getDimensionPixelSize(R.dimen.overflow_button_start_margin));
        setLayoutParams(lp);

        initView();
        iconView.setImageResource(resId);
        iconView.setTooltipText(text);
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.oui_menu_item_action_button, this, true);
        iconView = findViewById(R.id.menu_item_icon);
        badgeView = findViewById(R.id.menu_item_badge);
        titleView = findViewById(R.id.menu_item_title);

        iconView.setBackgroundResource(R.drawable.sesl_action_bar_item_background);
    }

    public void updateView() {
        int badgeCount = 0;

        if (menuItem != null) {
            setVisibility(menuItem.isVisible() ? VISIBLE : GONE);

            boolean enabled = menuItem.isEnabled();
            setEnabled(enabled);
            badgeView.setEnabled(enabled);
            iconView.setEnabled(enabled);

            iconView.setImageDrawable(menuItem.getIcon());
            iconView.setTooltipText(menuItem.getTitle());

            if (menuItem.getIcon() == null) {
                iconView.setVisibility(GONE);
                titleView.setVisibility(VISIBLE);
                titleView.setText(menuItem.getTitle());
            } else {
                iconView.setVisibility(VISIBLE);
                titleView.setVisibility(GONE);
            }


            badgeCount = menuItem.getBadge();
        } else if (menu != null) {
            badgeCount = menu.getTotalBadgeCount();
        }

        if (badgeCount > 0) {
            if (badgeCount > 99) badgeCount = 99;

            String countString = numberFormat.format(badgeCount);
            badgeView.setText(countString);

            int width = (int) (context.getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * context.getResources().getDimension(R.dimen.sesl_badge_additional_width));
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) badgeView.getLayoutParams();
            lp.width = width;
            badgeView.setLayoutParams(lp);
            badgeView.setVisibility(VISIBLE);
        } else if (badgeCount == -1) {
            badgeView.setText("N");
            badgeView.setVisibility(VISIBLE);
        } else {
            badgeView.setVisibility(GONE);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        iconView.setOnClickListener(l);
        titleView.setOnClickListener(l);
    }
}
