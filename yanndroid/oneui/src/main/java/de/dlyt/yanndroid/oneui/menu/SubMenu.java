package de.dlyt.yanndroid.oneui.menu;

import android.graphics.drawable.Drawable;
import android.view.View;

public class SubMenu extends Menu {

    private CharSequence headerTitle;
    private Drawable headerIcon;
    private MenuItem parentItem;

    SubMenu(MenuItem parentItem, android.view.SubMenu subMenu) { //only for MenuItem
        super(subMenu);
        this.parentItem = parentItem;
        this.headerTitle = subMenu.getItem().getTitle();
        this.headerIcon = subMenu.getItem().getIcon();
    }

    public SubMenu(CharSequence headerTitle, Drawable headerIcon) {
        super();
        this.parentItem = new MenuItem(View.generateViewId(), headerTitle, headerIcon);
        this.parentItem.setSubMenu(this);
        this.headerTitle = headerTitle;
        this.headerIcon = headerIcon;
    }

    public CharSequence getHeaderTitle() {
        return headerTitle;
    }

    public Drawable getHeaderIcon() {
        return headerIcon;
    }

    public MenuItem getParentMenuItem() {
        return parentItem;
    }

    void updateBadge() {
        parentItem.setBadge(getTotalBadgeCount());
    }

}
