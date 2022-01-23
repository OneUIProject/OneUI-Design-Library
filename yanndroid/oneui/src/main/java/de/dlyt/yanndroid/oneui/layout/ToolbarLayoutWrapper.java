package de.dlyt.yanndroid.oneui.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResult;
import androidx.annotation.MenuRes;

import com.google.android.material.appbar.MaterialToolbar;

import de.dlyt.yanndroid.oneui.menu.Menu;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungAppBarLayout;
import de.dlyt.yanndroid.oneui.sesl.appbar.SamsungCollapsingToolbarLayout;
import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.SamsungCoordinatorLayout;

class ToolbarLayoutWrapper extends SamsungCoordinatorLayout {

    private ToolbarLayout toolbarLayout;

    ToolbarLayoutWrapper(Context var1, AttributeSet var2) {
        super(var1, var2);
    }

    void setToolbarLayout(ToolbarLayout toolbarLayout) {
        this.toolbarLayout = toolbarLayout;
    }

    //methods

    public SamsungAppBarLayout getAppBarLayout() {
        return toolbarLayout.getAppBarLayout();
    }

    public MaterialToolbar getToolbar() {
        return toolbarLayout.getToolbar();
    }

    public void setTitle(CharSequence title) {
        toolbarLayout.setTitle(title, title);
    }

    public void setTitle(CharSequence expandedTitle, CharSequence collapsedTitle) {
        toolbarLayout.setTitle(expandedTitle, collapsedTitle);
    }

    public void setSubtitle(CharSequence subtitle) {
        toolbarLayout.setSubtitle(subtitle);
    }

    public void setExpanded(boolean expanded, boolean animate) {
        toolbarLayout.setExpanded(expanded, animate);
    }

    public boolean isExpanded() {
        return toolbarLayout.isExpanded();
    }

    public void setCustomTitleView(View view) {
        toolbarLayout.setCustomTitleView(view);
    }

    public void setCustomTitleView(View view, SamsungCollapsingToolbarLayout.LayoutParams params) {
        toolbarLayout.setCustomTitleView(view, params);
    }

    public void setImmersiveScroll(boolean activate) {
        toolbarLayout.setImmersiveScroll(activate);
    }

    public boolean isImmersiveScroll() {
        return toolbarLayout.isImmersiveScroll();
    }

    public void showSelectMode() {
        toolbarLayout.showSelectMode();
    }

    public void setSelectModeBottomMenu(@MenuRes int menuRes, ToolbarLayout.OnMenuItemClickListener listener) {
        toolbarLayout.setSelectModeBottomMenu(menuRes, listener);
    }

    public void setSelectModeBottomMenu(Menu menu, ToolbarLayout.OnMenuItemClickListener listener) {
        toolbarLayout.setSelectModeBottomMenu(menu, listener);
    }

    public Menu getSelectModeBottomMenu() {
        return toolbarLayout.getSelectModeBottomMenu();
    }

    public void dismissSelectMode() {
        toolbarLayout.dismissSelectMode();
    }

    public void setSelectModeCount(int count) {
        toolbarLayout.setSelectModeCount(count);
    }

    public void setSelectModeAllCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        toolbarLayout.setSelectModeAllCheckedChangeListener(listener);
    }

    public void setSelectModeAllChecked(boolean checked) {
        toolbarLayout.setSelectModeAllChecked(checked);
    }

    public void showSearchMode() {
        toolbarLayout.showSearchMode();
    }

    public void setSearchModeListener(ToolbarLayout.SearchModeListener listener) {
        toolbarLayout.setSearchModeListener(listener);
    }

    public void dismissSearchMode() {
        toolbarLayout.dismissSearchMode();
    }

    public boolean isSearchMode() {
        return toolbarLayout.isSearchMode();
    }

    public void onSearchModeVoiceInputResult(ActivityResult result) {
        toolbarLayout.onSearchModeVoiceInputResult(result);
    }

    public void setNavigationButtonIcon(Drawable navigationIcon) {
        toolbarLayout.setNavigationButtonIcon(navigationIcon);
    }

    public void setNavigationButtonVisible(boolean visible) {
        toolbarLayout.setNavigationButtonVisible(visible);
    }

    public void setNavigationButtonBadge(int count) {
        toolbarLayout.setNavigationButtonBadge(count);
    }

    public void setNavigationButtonTooltip(CharSequence tooltipText) {
        toolbarLayout.setNavigationButtonTooltip(tooltipText);
    }

    public void setNavigationButtonOnClickListener(OnClickListener listener) {
        toolbarLayout.setNavigationButtonOnClickListener(listener);
    }

    public void setOnToolbarMenuItemClickListener(ToolbarLayout.OnMenuItemClickListener listener) {
        toolbarLayout.setOnToolbarMenuItemClickListener(listener);
    }

    public Menu getToolbarMenu() {
        return toolbarLayout.getToolbarMenu();
    }

    public void inflateToolbarMenu(@MenuRes int menuRes) {
        toolbarLayout.inflateToolbarMenu(menuRes);
    }

    public void inflateToolbarMenu(Menu menu) {
        toolbarLayout.inflateToolbarMenu(menu);
    }

}
