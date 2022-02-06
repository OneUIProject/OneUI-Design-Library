package de.dlyt.yanndroid.oneui.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MenuInflater;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;

import java.util.ArrayList;

public class Menu {

    public ArrayList<MenuItem> menuItems;

    Menu(android.view.Menu menu) { //only for SubMenu
        menuItems = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = new MenuItem(menu.getItem(i));
            item.setParentMenu(this);
            menuItems.add(item);
        }
    }

    public Menu() { //create menu to add MenuItems programmatically later
        menuItems = new ArrayList<>();
    }

    @SuppressLint("RestrictedApi")
    public Menu(@MenuRes int menuRes, Context context) { //create Menu from MenuRes
        android.view.Menu menu = new MenuBuilder(context);
        MenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(menuRes, menu);
        menuItems = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = new MenuItem(menu.getItem(i));
            item.setParentMenu(this);
            menuItems.add(item);
        }
    }

    //setter
    public void addMenuItem(MenuItem menuItem) {
        menuItem.setParentMenu(this);
        menuItems.add(menuItem);
    }

    public void removeMenuItem(MenuItem menuItem) {
        menuItems.remove(menuItem);
    }

    public void addSubMenu(SubMenu subMenu) {
        addMenuItem(subMenu.getParentMenuItem());
    }

    public void setGroupCheckable(int group, boolean checkable) {
        for (MenuItem menuItem : menuItems)
            if (group == menuItem.getGroupId()) menuItem.setCheckable(checkable);
    }

    public void setGroupVisible(int group, boolean visible) {
        for (MenuItem menuItem : menuItems)
            if (group == menuItem.getGroupId()) menuItem.setVisible(visible);
    }

    public void setGroupEnabled(int group, boolean enabled) {
        for (MenuItem menuItem : menuItems)
            if (group == menuItem.getGroupId()) menuItem.setEnabled(enabled);
    }


    //getter
    public int size() {
        return menuItems.size();
    }

    public MenuItem getItem(int index) {
        return menuItems.get(index);
    }

    public MenuItem findItem(@IdRes int id) {
        for (MenuItem menuItem : menuItems) if (id == menuItem.getItemId()) return menuItem;
        return null;
    }

    public Integer getTotalBadgeCount() {
        int count = 0;
        boolean n = false;
        for (MenuItem menuItem : menuItems) {
            int b = menuItem.getBadge();
            if (b > 0) count += b;
            if (b == -1) n = true;
        }
        return (count == 0 ? (n ? -1 : 0) : count);
    }

}
