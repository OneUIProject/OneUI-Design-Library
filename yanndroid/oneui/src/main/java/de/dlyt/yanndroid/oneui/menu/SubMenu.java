package de.dlyt.yanndroid.oneui.menu;

import android.graphics.drawable.Drawable;

public class SubMenu extends Menu {

    private CharSequence headerTitle;
    private Drawable headerIcon;

    SubMenu(android.view.SubMenu subMenu) { //only for MenuItem
        super(subMenu);
        this.headerTitle = subMenu.getItem().getTitle();
        this.headerIcon = subMenu.getItem().getIcon();
    }

    public SubMenu(CharSequence headerTitle, Drawable headerIcon) {
        super();
        this.headerTitle = headerTitle;
        this.headerIcon = headerIcon;
    }

    public CharSequence getHeaderTitle() {
        return headerTitle;
    }

    public Drawable getHeaderIcon() {
        return headerIcon;
    }

}
