package de.dlyt.yanndroid.oneui.menu;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import androidx.appcompat.view.menu.MenuItemImpl;

public class MenuItem {

    public static final int N_BADGE = -1;

    private int id;
    private int groupId;
    private CharSequence title;
    private Drawable icon;

    private boolean checkable = false;
    private boolean checked = false;
    private int badge = 0;
    private SubMenu subMenu;
    private Menu parentMenu;

    private boolean enabled = true;
    private boolean visible = true;
    private boolean actionButton = false;

    private MenuItemListener menuItemListener;

    @SuppressLint("RestrictedApi")
    MenuItem(android.view.MenuItem menuItem) { //convert android.view.MenuItem to MenuItem for inflating MenuRes
        this.id = menuItem.getItemId();
        this.groupId = menuItem.getGroupId();
        this.title = menuItem.getTitle();
        this.icon = menuItem.getIcon();
        this.checkable = menuItem.isCheckable();
        this.checked = menuItem.isChecked();
        this.enabled = menuItem.isEnabled();
        this.visible = menuItem.isVisible();
        this.actionButton = ((MenuItemImpl) menuItem).requiresActionButton();

        if (menuItem.hasSubMenu()) this.subMenu = new SubMenu(this, menuItem.getSubMenu());
    }

    public MenuItem(int id, CharSequence title, Drawable icon) {
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

    void setParentMenu(Menu parentMenu) {
        this.parentMenu = parentMenu;
    }

    void toggleChecked() {
        setChecked(!checked);
    }

    public interface MenuItemListener {
        void onUpdate(MenuItem menuItem);
    }

    //setter
    public void setMenuItemListener(MenuItemListener menuItemListener) {
        this.menuItemListener = menuItemListener;
    }
    
    public void setTitle(CharSequence title) {
        this.title = title;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setBadge(int badge) {
        this.badge = badge;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
        if (parentMenu instanceof SubMenu) ((SubMenu) parentMenu).updateBadge();
    }

    public void setSubMenu(SubMenu subMenu) {
        this.subMenu = subMenu;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (menuItemListener != null) menuItemListener.onUpdate(this);
    }

    //getter
    public int getItemId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public CharSequence getTitle() {
        return title;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isCheckable() {
        return checkable;
    }

    public boolean isChecked() {
        return checked;
    }

    public int getBadge() {
        return badge;
    }

    public SubMenu getSubMenu() {
        return subMenu;
    }

    public boolean hasSubMenu() {
        return subMenu != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isActionButton() {
        return actionButton;
    }

    public Menu getParentMenu() {
        return parentMenu;
    }
}
