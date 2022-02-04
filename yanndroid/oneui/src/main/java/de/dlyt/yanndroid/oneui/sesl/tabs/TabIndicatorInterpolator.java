package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ViewUtils;

import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungTabLayout.TabView;

class TabIndicatorInterpolator {
  @Dimension(unit = Dimension.DP)
  private static final int MIN_INDICATOR_WIDTH = 24;

  @SuppressLint("RestrictedApi")
  static RectF calculateTabViewContentBounds(@NonNull TabView tabView, @Dimension(unit = Dimension.DP) int minWidth) {
    int tabViewContentWidth = tabView.getContentWidth();
    int tabViewContentHeight = tabView.getContentHeight();
    int minWidthPx = (int) ViewUtils.dpToPx(tabView.getContext(), minWidth);

    if (tabViewContentWidth < minWidthPx) {
      tabViewContentWidth = minWidthPx;
    }

    int tabViewCenterX = (tabView.getLeft() + tabView.getRight()) / 2;
    int tabViewCenterY = (tabView.getTop() + tabView.getBottom()) / 2;
    int contentLeftBounds = tabViewCenterX - (tabViewContentWidth / 2);
    int contentTopBounds = tabViewCenterY - (tabViewContentHeight / 2);
    int contentRightBounds = tabViewCenterX + (tabViewContentWidth / 2);
    int contentBottomBounds = tabViewCenterY + (tabViewCenterX / 2);

    return new RectF(contentLeftBounds, contentTopBounds, contentRightBounds, contentBottomBounds);
  }

  static RectF calculateIndicatorWidthForTab(SamsungTabLayout tabLayout, @Nullable View tab) {
    if (tab == null) {
      return new RectF();
    }

    if (!tabLayout.isTabIndicatorFullWidth() && tab instanceof TabView) {
      return calculateTabViewContentBounds((TabView) tab, MIN_INDICATOR_WIDTH);
    }

    return new RectF(tab.getLeft(), tab.getTop(), tab.getRight(), tab.getBottom());
  }

  void setIndicatorBoundsForTab(SamsungTabLayout tabLayout, View tab, @NonNull Drawable indicator) {
    RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, tab);
    indicator.setBounds((int) startIndicator.left, indicator.getBounds().top, (int) startIndicator.right, indicator.getBounds().bottom);
  }

  @SuppressLint("RestrictedApi")
  void setIndicatorBoundsForOffset(SamsungTabLayout tabLayout, View startTitle, View endTitle, @FloatRange(from = 0.0, to = 1.0) float offset, @NonNull Drawable indicator) {
    RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, startTitle);
    RectF endIndicator = calculateIndicatorWidthForTab(tabLayout, endTitle);
    indicator.setBounds(AnimationUtils.lerp((int) startIndicator.left, (int) endIndicator.left, offset), indicator.getBounds().top, AnimationUtils.lerp((int) startIndicator.right, (int) endIndicator.right, offset), indicator.getBounds().bottom);
  }
}
