package de.dlyt.yanndroid.oneui.sesl.tabs;

import static com.google.android.material.animation.AnimationUtils.lerp;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

class ElasticTabIndicatorInterpolator extends TabIndicatorInterpolator {
  private static float decInterp(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    return (float) Math.sin((fraction * Math.PI) / 2.0);
  }

  private static float accInterp(@FloatRange(from = 0.0, to = 1.0) float fraction) {
    return (float) (1.0 - Math.cos((fraction * Math.PI) / 2.0));
  }

  @SuppressLint("RestrictedApi")
  @Override
  void setIndicatorBoundsForOffset(SamsungTabLayout tabLayout, View startTitle, View endTitle, float offset, @NonNull Drawable indicator) {
      RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, startTitle);
      RectF endIndicator = calculateIndicatorWidthForTab(tabLayout, endTitle);

      float leftFraction;
      float rightFraction;

      final boolean isMovingRight = startIndicator.left < endIndicator.left;
      if (isMovingRight) {
        leftFraction = accInterp(offset);
        rightFraction = decInterp(offset);
      } else {
        leftFraction = decInterp(offset);
        rightFraction = accInterp(offset);
      }
      indicator.setBounds(lerp((int) startIndicator.left, (int) endIndicator.left, leftFraction), indicator.getBounds().top, lerp((int) startIndicator.right, (int) endIndicator.right, rightFraction), indicator.getBounds().bottom);
  }
}
