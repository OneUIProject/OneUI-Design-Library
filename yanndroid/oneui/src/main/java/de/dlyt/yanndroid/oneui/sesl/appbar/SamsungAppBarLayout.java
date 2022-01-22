package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.reflect.content.res.SeslConfigurationReflector;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.AppBarLayoutBehavior;
import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.SamsungCoordinatorLayout;

public class SamsungAppBarLayout extends LinearLayout implements SamsungCoordinatorLayout.AttachedBehavior, AppBarLayoutBehavior {
  private boolean mIsOneUI4;
  private static final float DEFAULT_HEIGHT_RATIO_TO_SCREEN = 0.39f;
  private static final int DEF_STYLE_RES = R.style.OneUI4_AppBarLayoutStyle;
  public static final int IMMERSIVE_DETACH_OPTION_SET_FIT_SYSTEM_WINDOW = 1;
  private static final int INVALID_SCROLL_RANGE = -1;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 4;
  static final int PENDING_ACTION_COLLAPSED = 2;
  static final int PENDING_ACTION_COLLAPSED_IMM = 512;
  static final int PENDING_ACTION_EXPANDED = 1;
  static final int PENDING_ACTION_FORCE = 8;
  static final int PENDING_ACTION_NONE = 0;
  public static final int SESL_STATE_COLLAPSED = 0;
  public static final int SESL_STATE_EXPANDED = 1;
  public static final int SESL_STATE_HIDE = 2;
  public static final int SESL_STATE_IDLE = 3;
  private static final String TAG = "AppBarLayout";
  private int currentOffset;
  private int downPreScrollRange = INVALID_SCROLL_RANGE;
  private int downScrollRange = INVALID_SCROLL_RANGE;
  @Nullable private ValueAnimator elevationOverlayAnimator;
  private boolean haveChildWithInterpolator;
  private boolean isMouse = false;
  @Nullable private WindowInsetsCompat lastInsets;
  private boolean liftOnScroll;
  @Nullable private WeakReference<View> liftOnScrollTargetView;
  @IdRes private int liftOnScrollTargetViewId;
  private boolean liftable;
  private boolean liftableOverride;
  private boolean lifted = false;
  private List<BaseOnOffsetChangedListener> listeners;
  private SeslAppbarState mAppbarState;
  private Drawable mBackground;
  private int mBottomPadding = 0;
  private float mCollapsedHeight;
  private int mCurrentOrientation;
  private int mCurrentScreenHeight;
  private int mCustomHeight = -1;
  private float mCustomHeightProportion;
  private float mHeightProportion;
  private boolean mImmHideStatusBar = false;
  private List<SeslBaseOnImmOffsetChangedListener> mImmOffsetListener;
  private int mImmersiveTopInset = 0;
  private boolean mIsActivatedByUser = false;
  private boolean mIsActivatedImmersiveScroll = false;
  private boolean mIsCanScroll = false;
  private boolean mIsDetachedState = false;
  private boolean mIsReservedImmersiveDetachOption = false;
  private boolean mReservedFitSystemWindow = false;
  private Resources mResources;
  private boolean mRestoreAnim = false;
  private int mSeslTCScrollRange = 0;
  private boolean mSetCustomHeight;
  private boolean mSetCustomProportion;
  private boolean mUseCollapsedHeight = false;
  private boolean mUseCustomHeight;
  private boolean mUseCustomPadding;
  private int pendingAction = PENDING_ACTION_NONE;
  @Nullable
  private Drawable statusBarForeground;
  private int[] tmpStatesArray;
  private int totalScrollRange = INVALID_SCROLL_RANGE;

  public interface BaseOnOffsetChangedListener<T extends SamsungAppBarLayout> {
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<SamsungAppBarLayout> {
    void onOffsetChanged(SamsungAppBarLayout appBarLayout, int verticalOffset);
  }

  public interface SeslBaseOnImmOffsetChangedListener<T extends SamsungAppBarLayout> {
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  public interface SeslOnImmOffsetChangedListener extends SeslBaseOnImmOffsetChangedListener<SamsungAppBarLayout> {
    void onOffsetChanged(SamsungAppBarLayout appBarLayout, int verticalOffset);
  }

  public static class SeslAppbarState {
    private int mCurrentState = SESL_STATE_IDLE;

    void onStateChanged(int state) {
      mCurrentState = state;
    }

    public int getState() {
      return mCurrentState;
    }
  }

  public SamsungAppBarLayout(@NonNull Context context) {
    this(context, null);
  }

  public SamsungAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.appBarLayoutStyle);
  }

  @SuppressLint("RestrictedApi")
  public SamsungAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    context = getContext();
    mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
    setOrientation(VERTICAL);

    if (Build.VERSION.SDK_INT >= 21) {
      ViewUtilsLollipop.setStateListAnimatorFromAttrs(this, attrs, defStyleAttr, DEF_STYLE_RES);
    }

    final TypedArray a = ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.SamsungAppBarLayout, defStyleAttr, DEF_STYLE_RES);

    mAppbarState = new SeslAppbarState();
    mResources = getResources();

    if (a.hasValue(R.styleable.SamsungAppBarLayout_android_background)) {
      mBackground = a.getDrawable(R.styleable.SamsungAppBarLayout_android_background);;
      ViewCompat.setBackground(this, mBackground);
    } else {
      mBackground = null;
      setBackgroundColor(mResources.getColor(mIsOneUI4 ? R.color.sesl4_action_bar_background_color : R.color.sesl_action_bar_background_color));
    }

    if (getBackground() instanceof ColorDrawable) {
      ColorDrawable background = (ColorDrawable) getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

    if (a.hasValue(R.styleable.SamsungAppBarLayout_expanded)) {
      setExpanded(a.getBoolean(R.styleable.SamsungAppBarLayout_expanded, false), false, false);
    }

    if (a.hasValue(R.styleable.SamsungAppBarLayout_seslUseCustomHeight)) {
      mUseCustomHeight = a.getBoolean(R.styleable.SamsungAppBarLayout_seslUseCustomHeight, false);
    }

    if (a.hasValue(R.styleable.SamsungAppBarLayout_seslHeightProportion)) {
      mSetCustomProportion = true;
      mCustomHeightProportion = a.getFloat(R.styleable.SamsungAppBarLayout_seslHeightProportion, DEFAULT_HEIGHT_RATIO_TO_SCREEN);
    } else {
      mSetCustomProportion = false;
      mCustomHeightProportion = DEFAULT_HEIGHT_RATIO_TO_SCREEN;
    }

    mHeightProportion = ResourcesCompat.getFloat(mResources, mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion);

    if (a.hasValue(R.styleable.SamsungAppBarLayout_seslUseCustomPadding)) {
      mUseCustomPadding = a.getBoolean(R.styleable.SamsungAppBarLayout_seslUseCustomPadding, false);
    }

    if (mUseCustomPadding) {
      mBottomPadding = a.getDimensionPixelSize(R.styleable.SamsungAppBarLayout_android_paddingBottom, 0);
    } else {
      mBottomPadding = mIsOneUI4 ? 0 : mResources.getDimensionPixelOffset(R.dimen.sesl_extended_appbar_bottom_padding);
    }

    setPadding(0, 0, 0, mBottomPadding);

    mCollapsedHeight =  (float) (mResources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding) + mBottomPadding);

    seslSetCollapsedHeight(mCollapsedHeight, false);

    if (Build.VERSION.SDK_INT >= 21 && a.hasValue(R.styleable.SamsungAppBarLayout_elevation)) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, a.getDimensionPixelSize(R.styleable.SamsungAppBarLayout_elevation, 0));
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (a.hasValue(R.styleable.SamsungAppBarLayout_android_keyboardNavigationCluster)) {
        setKeyboardNavigationCluster(a.getBoolean(R.styleable.SamsungAppBarLayout_android_keyboardNavigationCluster, false));
      }
      if (a.hasValue(R.styleable.SamsungAppBarLayout_android_touchscreenBlocksFocus)) {
        setTouchscreenBlocksFocus(a.getBoolean(R.styleable.SamsungAppBarLayout_android_touchscreenBlocksFocus, false));
      }
    }

    liftOnScroll = a.getBoolean(R.styleable.SamsungAppBarLayout_liftOnScroll, false);
    liftOnScrollTargetViewId = a.getResourceId(R.styleable.SamsungAppBarLayout_liftOnScrollTargetViewId, View.NO_ID);

    setStatusBarForeground(a.getDrawable(R.styleable.SamsungAppBarLayout_statusBarForeground));
    a.recycle();

    ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
      @Override
      public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        return onWindowInsetChanged(insets);
      }
    });

    mCurrentOrientation = mResources.getConfiguration().orientation;
    mCurrentScreenHeight = mResources.getConfiguration().screenHeightDp;
  }

  void updateInternalCollapsedHeight() {
    if (useCollapsedHeight()) {
      return;
    }
    if (getImmBehavior() == null || !getCanScroll()) {
      float height = (float) (getHeight() - getTotalScrollRange());
      if (height != seslGetCollapsedHeight() && height > 0.0f) {
        Log.i(TAG, "Internal collapsedHeight/ oldCollapsedHeight :" + seslGetCollapsedHeight() + " newCollapsedHeight :" + height);
        seslSetCollapsedHeight(height, false);
        updateInternalHeight();
      }
    }
  }

  void updateInternalCollapsedHeightOnce() {
    if (useCollapsedHeight()) {
      return;
    }
    if (getImmBehavior() == null || !getCanScroll()) {
      Log.i(TAG, "update InternalCollapsedHeight from updateInternalHeight() : " + seslGetCollapsedHeight());
      seslSetCollapsedHeight(seslGetCollapsedHeight(), false);
    }
  }

  public SeslAppbarState seslGetAppBarState() {
    return mAppbarState;
  }

  public void seslSetCustomHeightProportion(boolean enabled, float proportion) {
    if (proportion > 1.0f) {
      Log.e(TAG, "Height proportion float range is 0..1");
      return;
    }
    mUseCustomHeight = enabled;
    mSetCustomProportion = enabled;
    mSetCustomHeight = false;
    mCustomHeightProportion = proportion;
    updateInternalHeight();
    requestLayout();
  }

  public void seslSetCustomHeight(int height) {
    mCustomHeight = height;
    mUseCustomHeight = true;
    mSetCustomHeight = true;
    mSetCustomProportion = false;

    SamsungCoordinatorLayout.LayoutParams lp;
    try {
      lp = (SamsungCoordinatorLayout.LayoutParams) getLayoutParams();
    } catch (ClassCastException e) {
      Log.e(TAG, Log.getStackTraceString(e));
      lp = null;
    }
    if (lp != null) {
      lp.height = height;
      setLayoutParams(lp);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    addOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  public void seslAddOnImmOffsetChangedListener(@Nullable SeslBaseOnImmOffsetChangedListener listener) {
    if (mImmOffsetListener == null) {
      mImmOffsetListener = new ArrayList();
    }
    if (listener != null && !mImmOffsetListener.contains(listener)) {
      mImmOffsetListener.add(listener);
    }
  }

  public void seslAddOnImmOffsetChangedListener(SeslOnImmOffsetChangedListener listener) {
    seslAddOnImmOffsetChangedListener((SeslBaseOnImmOffsetChangedListener) listener);
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners != null && listener != null) {
      listeners.remove(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    removeOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  public void seslRemoveOnImmOffsetChangedListener(SeslBaseOnImmOffsetChangedListener listener) {
    if (mImmOffsetListener != null && listener != null) {
      mImmOffsetListener.remove(listener);
    }
  }

  public void seslRemoveOnImmOffsetChangedListener(OnOffsetChangedListener listener) {
    seslRemoveOnImmOffsetChangedListener((SeslBaseOnImmOffsetChangedListener) listener);
  }

  public void setStatusBarForeground(@Nullable Drawable drawable) {
    if (statusBarForeground != drawable) {
      if (statusBarForeground != null) {
        statusBarForeground.setCallback(null);
      }
      statusBarForeground = drawable != null ? drawable.mutate() : null;
      if (statusBarForeground != null) {
        if (statusBarForeground.isStateful()) {
          statusBarForeground.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarForeground, ViewCompat.getLayoutDirection(this));
        statusBarForeground.setVisible(getVisibility() == VISIBLE, false);
        statusBarForeground.setCallback(this);
      }
      updateWillNotDraw();
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  public void setStatusBarForegroundColor(@ColorInt int color) {
    setStatusBarForeground(new ColorDrawable(color));
  }

  public void setStatusBarForegroundResource(@DrawableRes int resId) {
    setStatusBarForeground(AppCompatResources.getDrawable(getContext(), resId));
  }

  @Nullable
  public Drawable getStatusBarForeground() {
    return statusBarForeground;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    if (shouldDrawStatusBarForeground()) {
      int saveCount = canvas.save();
      canvas.translate(0f, -currentOffset);
      statusBarForeground.draw(canvas);
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();

    Drawable d = statusBarForeground;
    if (d != null && d.isStateful() && d.setState(state)) {
      invalidateDrawable(d);
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == statusBarForeground;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarForeground != null) {
      statusBarForeground.setVisible(visible, false);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    updateInternalHeight();

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode != MeasureSpec.EXACTLY && ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
      int newHeight = getMeasuredHeight();
      switch (heightMode) {
        case MeasureSpec.AT_MOST:
          newHeight = MathUtils.clamp(getMeasuredHeight() + getTopInset(), 0, MeasureSpec.getSize(heightMeasureSpec));
          break;
        case MeasureSpec.UNSPECIFIED:
          newHeight += getTopInset();
          break;
        default:
      }
      setMeasuredDimension(getMeasuredWidth(), newHeight);
    }

    invalidateScrollRanges();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
      final int topInset = getTopInset();
      for (int z = getChildCount() - 1; z >= 0; z--) {
        ViewCompat.offsetTopAndBottom(getChildAt(z), topInset);
      }
    }

    invalidateScrollRanges();

    haveChildWithInterpolator = false;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
      final Interpolator interpolator = childLp.getScrollInterpolator();

      if (interpolator != null) {
        haveChildWithInterpolator = true;
        break;
      }
    }

    if (statusBarForeground != null) {
      statusBarForeground.setBounds(0, 0, getWidth(), getTopInset());
    }

    if (!liftableOverride) {
      setLiftableState(liftOnScroll || hasCollapsibleChild());
    }
  }

  private void updateWillNotDraw() {
    setWillNotDraw(!shouldDrawStatusBarForeground());
  }

  private boolean shouldDrawStatusBarForeground() {
    return statusBarForeground != null && getTopInset() > 0;
  }

  private boolean hasCollapsibleChild() {
    for (int i = 0, z = getChildCount(); i < z; i++) {
      if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
        return true;
      }
    }
    return false;
  }

  private void invalidateScrollRanges() {
    totalScrollRange = INVALID_SCROLL_RANGE;
    downPreScrollRange = INVALID_SCROLL_RANGE;
    downScrollRange = INVALID_SCROLL_RANGE;
  }

  @Override
  public void setOrientation(int orientation) {
    if (orientation != VERTICAL) {
      throw new IllegalArgumentException("AppBarLayout is always vertical and does not support horizontal orientation");
    }
    super.setOrientation(orientation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mIsDetachedState = false;
    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  @NonNull
  public SamsungCoordinatorLayout.Behavior<SamsungAppBarLayout> getBehavior() {
    return new SamsungAppBarLayout.Behavior();
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
  }

  public void setExpanded(boolean expanded) {
    setExpanded(expanded, ViewCompat.isLaidOut(this));
  }

  public void setExpanded(boolean expanded, boolean animate) {
    setExpanded(expanded, animate, true);
  }

  private void setExpanded(boolean expanded, boolean animate, boolean force) {
    setLifted(!expanded);
    pendingAction = (expanded ? PENDING_ACTION_EXPANDED : seslGetImmersiveScroll() ? PENDING_ACTION_COLLAPSED_IMM : PENDING_ACTION_COLLAPSED) | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0) | (force ? PENDING_ACTION_FORCE : 0);
    requestLayout();
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
      return new LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) p);
    }
    return new LayoutParams(p);
  }

  public void seslReserveImmersiveDetachOption(int flag) {
    if (flag != 0) {
      mIsReservedImmersiveDetachOption = true;
      mReservedFitSystemWindow = (flag & IMMERSIVE_DETACH_OPTION_SET_FIT_SYSTEM_WINDOW) != 0;
      return;
    }
    mIsReservedImmersiveDetachOption = false;
  }

  @Override
  protected void onDetachedFromWindow() {
    mIsDetachedState = true;
    if (mIsReservedImmersiveDetachOption && getImmBehavior() != null && mReservedFitSystemWindow) {
      Log.i(TAG, "fits system window Immersive detached");
      getImmBehavior().setupDecorFitsSystemWindow(true);
    }

    super.onDetachedFromWindow();

    clearLiftOnScrollTargetView();
  }

  boolean hasChildWithInterpolator() {
    return haveChildWithInterpolator;
  }

  public final int getTotalScrollRange() {
    if (totalScrollRange != INVALID_SCROLL_RANGE) {
      return totalScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        range += childHeight + lp.topMargin + lp.bottomMargin;

        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          range -= getTopInset();
        }
        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          if (getCanScroll()) {
            range += getTopInset() + mBottomPadding + seslGetTCScrollRange();
          } else {
            range -= ViewCompat.getMinimumHeight(child);
          }
          break;
        }
      } else {
        break;
      }
    }
    return totalScrollRange = Math.max(0, range);
  }

  boolean hasScrollableChildren() {
    return getTotalScrollRange() != 0;
  }

  int getUpNestedPreScrollRange() {
    return getTotalScrollRange();
  }

  // kang
  int getDownNestedPreScrollRange() {
    int var1 = this.downPreScrollRange;
    if (var1 != -1) {
      return var1;
    } else {
      int var2 = this.getChildCount() - 1;
      var1 = 0;

      int var3;
      while(true) {
        var3 = var1;
        if (var2 < 0) {
          break;
        }

        View var4 = this.getChildAt(var2);
        SamsungAppBarLayout.LayoutParams var5 = (SamsungAppBarLayout.LayoutParams)var4.getLayoutParams();
        int var6 = var4.getMeasuredHeight();
        var3 = var5.scrollFlags;
        if ((var3 & 5) != 5) {
          var3 = var1;
          if (this.getCanScroll()) {
            var3 = (int)((float)var1 + this.seslGetCollapsedHeight() + (float)this.seslGetTCScrollRange());
          }
          break;
        }

        int var7;
        label36: {
          var7 = var5.topMargin + var5.bottomMargin;
          if ((var3 & 8) != 0) {
            var3 = ViewCompat.getMinimumHeight(var4);
          } else {
            if ((var3 & 2) == 0) {
              var3 = var7 + var6;
              break label36;
            }

            var3 = var6 - ViewCompat.getMinimumHeight(var4);
          }

          var3 += var7;
        }

        var7 = var3;
        if (var2 == 0) {
          var7 = var3;
          if (ViewCompat.getFitsSystemWindows(var4)) {
            var7 = Math.min(var3, var6 - this.getTopInset());
          }
        }

        var1 += var7;
        --var2;
      }

      var1 = Math.max(0, var3);
      this.downPreScrollRange = var1;
      return var1;
    }
  }
  // kang

  int getDownNestedScrollRange() {
    if (downScrollRange != INVALID_SCROLL_RANGE) {
      return downScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      int childHeight = child.getMeasuredHeight();
      childHeight += lp.topMargin + lp.bottomMargin;

      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        range += childHeight;

        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          int newRange;
          if (!mIsCanScroll || !(child instanceof SamsungCollapsingToolbarLayout)) {
            newRange = ViewCompat.getMinimumHeight(child);
          } else {
            newRange = ((SamsungCollapsingToolbarLayout) child).seslGetMinimumHeightWithoutMargin();
          }
          range -= newRange;
          break;
        }
      } else {
        break;
      }
    }
    return downScrollRange = Math.max(0, range);
  }

  void onOffsetChanged(int offset) {
    currentOffset = offset;

    int totalScrollRange = getTotalScrollRange();
    int height = getHeight() - ((int) seslGetCollapsedHeight());
    if (Math.abs(offset) >= totalScrollRange) {
      if (getCanScroll()) {
        if (mAppbarState.getState() != SESL_STATE_HIDE) {
          mAppbarState.onStateChanged(SESL_STATE_HIDE);
        }
      } else if (mAppbarState.getState() != SESL_STATE_COLLAPSED) {
        mAppbarState.onStateChanged(SESL_STATE_COLLAPSED);
      }
    } else if (Math.abs(offset) >= height) {
      if (mAppbarState.getState() != SESL_STATE_COLLAPSED) {
        mAppbarState.onStateChanged(SESL_STATE_COLLAPSED);
      }
    } else if (Math.abs(offset) == 0) {
      if (mAppbarState.getState() != SESL_STATE_EXPANDED) {
        mAppbarState.onStateChanged(SESL_STATE_EXPANDED);
      }
    } else if (mAppbarState.getState() != SESL_STATE_IDLE) {
      mAppbarState.onStateChanged(SESL_STATE_IDLE);
    }

    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

    if (listeners != null) {
      for (int i = 0, z = listeners.size(); i < z; i++) {
        final BaseOnOffsetChangedListener listener = listeners.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }

  void onImmOffsetChanged(int offset) {
    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

    if (mImmOffsetListener != null) {
      for (int i = 0, z = mImmOffsetListener.size(); i < z; i++) {
        final SeslBaseOnImmOffsetChangedListener listener = mImmOffsetListener.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }

  public final int getMinimumHeightForVisibleOverlappingContent() {
    final int topInset = getTopInset();
    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight != 0) {
      return (minHeight * 2) + topInset;
    }

    final int childCount = getChildCount();
    final int lastChildMinHeight = childCount >= 1 ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) : 0;
    if (lastChildMinHeight != 0) {
      return (lastChildMinHeight * 2) + topInset;
    }

    return getHeight() / 3;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (tmpStatesArray == null) {
      tmpStatesArray = new int[4];
    }
    final int[] extraStates = tmpStatesArray;
    final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

    extraStates[0] = liftable ? R.attr.state_liftable : -R.attr.state_liftable;
    extraStates[1] = liftable && lifted ? R.attr.state_lifted : -R.attr.state_lifted;

    extraStates[2] = liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
    extraStates[3] = liftable && lifted ? R.attr.state_collapsed : -R.attr.state_collapsed;

    return mergeDrawableStates(states, extraStates);
  }

  public boolean setLiftable(boolean liftable) {
    liftableOverride = true;
    return setLiftableState(liftable);
  }

  public void setLiftableOverrideEnabled(boolean enabled) {
    liftableOverride = enabled;
  }

  private boolean setLiftableState(boolean liftable) {
    if (this.liftable != liftable) {
      this.liftable = liftable;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  public boolean setLifted(boolean lifted) {
    return setLiftedState(lifted);
  }

  public boolean isLifted() {
    return lifted;
  }

  boolean setLiftedState(boolean lifted) {
    if (this.lifted == lifted) {
      return false;
    }
    this.lifted = lifted;
    refreshDrawableState();
    if (liftOnScroll && getBackground() instanceof MaterialShapeDrawable) {
      startLiftOnScrollElevationOverlayAnimation((MaterialShapeDrawable) getBackground(), lifted);
    }
    return true;
  }

  private void startLiftOnScrollElevationOverlayAnimation(@NonNull final MaterialShapeDrawable background, boolean lifted) {
    float appBarElevation = getResources().getDimension(R.dimen.sesl_appbar_elevation);
    float fromElevation = lifted ? 0 : appBarElevation;
    float toElevation = lifted ? appBarElevation : 0;

    if (elevationOverlayAnimator != null) {
      elevationOverlayAnimator.cancel();
    }

    elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation);
    elevationOverlayAnimator.setDuration(getResources().getInteger(R.integer.app_bar_elevation_anim_duration));
    elevationOverlayAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    elevationOverlayAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
        float elevation = (float) valueAnimator.getAnimatedValue();
        background.setElevation(elevation);
      }
    });
    elevationOverlayAnimator.start();
  }

  public void setLiftOnScroll(boolean liftOnScroll) {
    this.liftOnScroll = liftOnScroll;
  }

  public boolean isLiftOnScroll() {
    return liftOnScroll;
  }

  public void setLiftOnScrollTargetViewId(@IdRes int liftOnScrollTargetViewId) {
    this.liftOnScrollTargetViewId = liftOnScrollTargetViewId;
    clearLiftOnScrollTargetView();
  }

  @IdRes
  public int getLiftOnScrollTargetViewId() {
    return liftOnScrollTargetViewId;
  }

  boolean shouldLift(@Nullable View defaultScrollingView) {
    View scrollingView = findLiftOnScrollTargetView(defaultScrollingView);
    if (scrollingView == null) {
      scrollingView = defaultScrollingView;
    }
    return scrollingView != null && (scrollingView.canScrollVertically(-1) || scrollingView.getScrollY() > 0);
  }

  @Nullable
  private View findLiftOnScrollTargetView(@Nullable View defaultScrollingView) {
    if (liftOnScrollTargetView == null && liftOnScrollTargetViewId != View.NO_ID) {
      View targetView = null;
      if (defaultScrollingView != null) {
        targetView = defaultScrollingView.findViewById(liftOnScrollTargetViewId);
      }
      if (targetView == null && getParent() instanceof ViewGroup) {
        targetView = ((ViewGroup) getParent()).findViewById(liftOnScrollTargetViewId);
      }
      if (targetView != null) {
        liftOnScrollTargetView = new WeakReference<>(targetView);
      }
    }
    return liftOnScrollTargetView != null ? liftOnScrollTargetView.get() : null;
  }

  private void clearLiftOnScrollTargetView() {
    if (liftOnScrollTargetView != null) {
      liftOnScrollTargetView.clear();
    }
    liftOnScrollTargetView = null;
  }

  @Deprecated
  public void setTargetElevation(float elevation) {
    if (Build.VERSION.SDK_INT >= 21) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, elevation);
    }
  }

  @Deprecated
  public float getTargetElevation() {
    return 0;
  }

  int getPendingAction() {
    return pendingAction;
  }

  void resetPendingAction() {
    pendingAction = PENDING_ACTION_NONE;
  }

  final int getTopInset() {
    return lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
  }

  private boolean shouldOffsetFirstChild() {
    if (getChildCount() > 0) {
      final View firstChild = getChildAt(0);
      return firstChild.getVisibility() != GONE && !ViewCompat.getFitsSystemWindows(firstChild);
    }
    return false;
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      newInsets = insets;
    }

    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      updateWillNotDraw();
      requestLayout();
    }

    return insets;
  }

  protected int getCurrentOrientation() {
    return mCurrentOrientation;
  }

  @Deprecated
  public void seslRestoreTopAndBottom(View view) {
    seslRestoreTopAndBottom();
  }

  private SeslImmersiveScrollBehavior getImmBehavior() {
    ViewGroup.LayoutParams lp = getLayoutParams();
    if (lp instanceof SamsungCoordinatorLayout.LayoutParams) {
      SamsungCoordinatorLayout.Behavior behavior = ((SamsungCoordinatorLayout.LayoutParams) lp).getBehavior();
      if (behavior instanceof SeslImmersiveScrollBehavior) {
        return (SeslImmersiveScrollBehavior) behavior;
      }
    }

    return null;
  }

  public boolean seslHaveImmersiveBehavior() {
    return getImmBehavior() != null;
  }

  public void seslSetWindowInsetsAnimationCallback(Object callback) {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      if (callback == null) {
        behavior.setWindowInsetsAnimationCallback(this, null);
      }
      if (callback instanceof WindowInsetsAnimationCompat.Callback) {
        behavior.setWindowInsetsAnimationCallback(this, (WindowInsetsAnimationCompat.Callback) callback);
      }
    }
  }

  public void seslRestoreTopAndBottom() {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslRestoreTopAndBottom();
    }
  }

  public void seslRestoreTopAndBottom(boolean restore) {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslRestoreTopAndBottom(restore);
    }
  }

  public void resetAppBarAndInsets() {
    seslResetAppBarAndInsets(true);
  }

  public void seslResetAppBarAndInsets(boolean force) {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      Log.i(TAG, "seslResetAppBarAndInsets() force = " + force);
      behavior.seslRestoreTopAndBottom();
      behavior.showWindowInset(force);
    }
  }

  public void seslCancelWindowInsetsAnimationController() {
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      Log.i(TAG, "seslCancelWindowInsetsAnimationController");
      behavior.cancelWindowInsetsAnimationController();
    }
  }

  public void seslImmHideStatusBarForLandscape(boolean hide) {
    mImmHideStatusBar = hide;
  }

  boolean isImmHideStatusBarForLandscape() {
    return mImmHideStatusBar;
  }

  @Deprecated
  public void seslSetBottomView(View view, View bottomView) {
    seslSetBottomView(bottomView);
  }

  public void seslSetBottomView(View bottomView) {
    if (bottomView == null) {
      Log.w(TAG, "bottomView is null");
    }
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      behavior.seslSetBottomView(bottomView);
    }
  }

  protected void internalActivateImmersiveScroll(boolean activate, boolean byUser) {
    mIsActivatedImmersiveScroll = activate;
    mIsActivatedByUser = byUser;

    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    Log.i(TAG, "internalActivateImmersiveScroll : " + activate + " , byUser : " + byUser + " , behavior : " + behavior);
    if (behavior == null) {
      return;
    }
    if (!activate || behavior.isAppBarHide()) {
      behavior.seslRestoreTopAndBottom(mRestoreAnim);
    }
  }

  public void seslActivateImmersiveScroll(boolean activate, boolean byUser) {
    if (isDexEnabled()) {
      Log.i(TAG, "Dex Enabled Set false ImmersiveScroll");
      activate = false;
    }

    mRestoreAnim = byUser;
    internalActivateImmersiveScroll(activate, true);

    boolean z = true;
    SeslImmersiveScrollBehavior behavior = getImmBehavior();
    if (behavior != null) {
      z = behavior.dispatchImmersiveScrollEnable();
    }

    if (z || !activate) {
      setCanScroll(activate);
    }
  }

  public void seslSetImmersiveScroll(boolean activate, boolean byUser) {
    seslActivateImmersiveScroll(activate, byUser);
  }

  public void seslActivateImmersiveScroll(boolean activate) {
    seslActivateImmersiveScroll(activate, true);
  }

  public void seslSetImmersiveScroll(boolean activate) {
    seslActivateImmersiveScroll(activate);
  }

  public boolean isActivatedImmsersiveScroll() {
    return mIsActivatedImmersiveScroll;
  }

  public boolean seslGetImmersiveScroll() {
    return isActivatedImmsersiveScroll();
  }

  protected boolean isImmersiveActivatedByUser() {
    return mIsActivatedByUser;
  }

  protected void setCanScroll(boolean canScroll) {
    if (mIsCanScroll != canScroll) {
      mIsCanScroll = canScroll;
      invalidateScrollRanges();
      requestLayout();
    }
  }

  public void seslSetTCScrollRange(int range) {
    mSeslTCScrollRange = range;
  }

  protected int seslGetTCScrollRange() {
    return mSeslTCScrollRange;
  }

  protected boolean getCanScroll() {
    return mIsCanScroll;
  }

  public void seslSetCollapsedHeight(float height) {
    Log.i(TAG, "seslSetCollapsedHeight, height : " + height);
    seslSetCollapsedHeight(height, true);
  }

  private void seslSetCollapsedHeight(float height, boolean custom) {
    mUseCollapsedHeight = custom;
    mCollapsedHeight = height;
  }

  void internalProportion(float proportion) {
    if (!mUseCustomHeight && mHeightProportion != proportion) {
      mHeightProportion = proportion;
      updateInternalHeight();
    }
  }

  void setImmersiveTopInset(int top) {
    mImmersiveTopInset = top;
  }

  final int getImmersiveTopInset() {
    if (mIsCanScroll) {
      return mImmersiveTopInset;
    }
    return 0;
  }

  public float seslGetCollapsedHeight() {
    return mCollapsedHeight + ((float) getImmersiveTopInset());
  }

  public float seslGetHeightProPortion() {
    return mHeightProportion;
  }

  boolean useCollapsedHeight() {
    return mUseCollapsedHeight;
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    if (mBackground != null) {
      setBackgroundDrawable(mBackground == getBackground() ? mBackground : getBackground());
    } else if (getBackground() != null) {
      mBackground = getBackground();
      setBackgroundDrawable(mBackground);
    } else {
      mBackground = null;
      setBackgroundColor(mResources.getColor(R.color.sesl_action_bar_background_color));
    }

    if (mCurrentScreenHeight != newConfig.screenHeightDp || mCurrentOrientation != newConfig.orientation) {
      if (!mUseCustomPadding && !mUseCollapsedHeight) {
        Log.i(TAG, "Update bottom padding");
        mBottomPadding = mResources.getDimensionPixelSize(R.dimen.sesl_extended_appbar_bottom_padding);
        setPadding(0, 0, 0, mBottomPadding);
        mCollapsedHeight = (float) (mResources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding) + mBottomPadding);
        seslSetCollapsedHeight(mCollapsedHeight, false);
      } else if (mUseCustomPadding && mBottomPadding == 0 && !mUseCollapsedHeight) {
        mCollapsedHeight = (float) mResources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding);
        seslSetCollapsedHeight(mCollapsedHeight, false);
      }
    }

    if (!mSetCustomProportion) {
      mHeightProportion = ResourcesCompat.getFloat(mResources, mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion);
    }

    updateInternalHeight();

    if (lifted || (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)) {
      setExpanded(false, false, true);
    } else {
      setExpanded(true, false, true);
    }

    mCurrentOrientation = newConfig.orientation;
    mCurrentScreenHeight = newConfig.screenHeightDp;
  }

  @Override
  public boolean dispatchGenericMotionEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
      if (liftOnScrollTargetView != null) {
        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
          setExpanded(false);
        } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f && !canScrollVertically(-1)) {
          setExpanded(true);
        }
      } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
        setExpanded(false);
      } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f) {
        setExpanded(true);
      }
    }

    return super.dispatchGenericMotionEvent(event);
  }

  private void updateInternalHeight() {
    int windowHeight = getWindowHeight();

    float proportion;
    if (mUseCustomHeight) {
      if (mCustomHeightProportion != 0.0f) {
        proportion = mCustomHeightProportion + (getCanScroll() ? getDifferImmHeightRatio() : 0.0f);
      } else {
        proportion = 0.0f;
      }
    } else {
      proportion = mHeightProportion;
    }

    float collapsedHeight = ((float) windowHeight) * proportion;
    if (collapsedHeight == 0.0f) {
      updateInternalCollapsedHeightOnce();
      collapsedHeight = seslGetCollapsedHeight();
    }

    SamsungCoordinatorLayout.LayoutParams lp;
    try {
      lp = (SamsungCoordinatorLayout.LayoutParams) getLayoutParams();
    } catch (ClassCastException e) {
      Log.e(TAG, Log.getStackTraceString(e));
      lp = null;
    }

    String logStr = "[updateInternalHeight] orientation : " + mResources.getConfiguration().orientation + ", density : " + mResources.getConfiguration().densityDpi + ", windowHeight : " + windowHeight;
    if (mUseCustomHeight) {
      if (mSetCustomProportion) {
        if (lp != null) {
          lp.height = (int) collapsedHeight;
          setLayoutParams(lp);
          logStr += ", [1]updateInternalHeight: lp.height : " + lp.height + ", mCustomHeightProportion : " + mCustomHeightProportion;
        }
      } else if (mSetCustomHeight && lp != null) {
        lp.height = mCustomHeight + getImmersiveTopInset();
        setLayoutParams(lp);
        logStr += ", [2]updateInternalHeight: CustomHeight : " + mCustomHeight + "lp.height : " + lp.height;
      }
    } else if (lp != null) {
      lp.height = (int) collapsedHeight;
      setLayoutParams(lp);
      logStr += ", [3]updateInternalHeight: lp.height : " + lp.height + ", mHeightProportion : " + mHeightProportion;
    }
    logStr += " , mIsImmersiveScroll : " + mIsActivatedImmersiveScroll + " , mIsSetByUser : " + mIsActivatedByUser;
    Log.i(TAG, logStr);
  }

  private float getDifferImmHeightRatio() {
    float windowHeight = (float) getWindowHeight();
    if (windowHeight == 0.0f) {
      windowHeight = 1.0f;
    }
    return getImmersiveTopInset() / windowHeight;
  }

  private int getWindowHeight() {
    return mResources.getDisplayMetrics().heightPixels;
  }

  @Override
  public void seslSetExpanded(boolean expanded) {
    setExpanded(expanded);
  }

  @Override
  public boolean seslIsCollapsed() {
    return lifted;
  }

  @Override
  public void seslSetIsMouse(boolean mouse) {
    isMouse = mouse;
  }

  protected boolean getIsMouse() {
    return isMouse;
  }


  public static class LayoutParams extends LinearLayout.LayoutParams {
    static final int COLLAPSIBLE_FLAGS = 10;
    private static final int FLAG_NO_SCROLL_HOLD = 65536;
    private static final int FLAG_NO_SNAP = 4096;
    static final int FLAG_QUICK_RETURN = 5;
    static final int FLAG_SNAP = 17;
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 4;
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 8;
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 2;
    public static final int SCROLL_FLAG_NO_SCROLL = 0;
    public static final int SCROLL_FLAG_SCROLL = 1;
    public static final int SCROLL_FLAG_SNAP = 16;
    public static final int SCROLL_FLAG_SNAP_MARGINS = 32;
    public static final int SESL_SCROLL_FLAG_NO_SCROLL_HOLD = 65536;
    public static final int SESL_SCROLL_FLAG_NO_SNAP = 4096;
    int scrollFlags = SCROLL_FLAG_SCROLL;
    Interpolator scrollInterpolator;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {}

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SamsungAppBarLayout_Layout);
      scrollFlags = a.getInt(R.styleable.SamsungAppBarLayout_Layout_layout_scrollFlags, 0);

      if (a.hasValue(R.styleable.SamsungAppBarLayout_Layout_layout_scrollInterpolator)) {
        int resId = a.getResourceId(R.styleable.SamsungAppBarLayout_Layout_layout_scrollInterpolator, 0);
        scrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
      }
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LinearLayout.LayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(@NonNull LayoutParams source) {
      super(source);
      scrollFlags = source.scrollFlags;
      scrollInterpolator = source.scrollInterpolator;
    }

    public void setScrollFlags(@ScrollFlags int flags) {
      scrollFlags = flags;
    }

    @ScrollFlags
    public int getScrollFlags() {
      return scrollFlags;
    }

    public void setScrollInterpolator(Interpolator interpolator) {
      scrollInterpolator = interpolator;
    }

    public Interpolator getScrollInterpolator() {
      return scrollInterpolator;
    }

    boolean isCollapsible() {
      return (scrollFlags & SCROLL_FLAG_SCROLL) == SCROLL_FLAG_SCROLL && (scrollFlags & COLLAPSIBLE_FLAGS) != 0;
    }
  }


  public static class Behavior extends BaseBehavior<SamsungAppBarLayout> {
    public abstract static class DragCallback extends BaseBehavior.BaseDragCallback<SamsungAppBarLayout> {}

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  protected static class BaseBehavior<T extends SamsungAppBarLayout> extends HeaderBehavior<T> {
    private static final int INVALID_POSITION = -1;
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600;
    @Nullable private WeakReference<View> lastNestedScrollingChildRef;
    @ViewCompat.NestedScrollType private int lastStartedType;
    private float mDiffY_Touch;
    private float mLastMotionY_Touch;
    private boolean mLifted;
    private boolean mToolisMouse;
    private ValueAnimator offsetAnimator;
    private int offsetDelta;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;
    private BaseDragCallback onDragCallback;
    private float touchX;
    private float touchY;
    private int offsetToChildIndexOnLayout = -1;
    private boolean mIsFlingScrollDown = false;
    private boolean mIsFlingScrollUp = false;
    private boolean mDirectTouchAppbar = false;
    private int mTouchSlop = -1;
    private float mVelocity = 0.0f;
    private boolean mIsSetStaticDuration = false;
    private boolean mIsScrollHold = false;

    public abstract static class BaseDragCallback<T extends SamsungAppBarLayout> {
      public abstract boolean canDrag(@NonNull T appBarLayout);
    }

    public BaseBehavior() {}

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull SamsungCoordinatorLayout parent, @NonNull T child, @NonNull View directTargetChild, View target, int nestedScrollAxes, int type) {
      final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && (child.isLiftOnScroll() || canScrollChildren(parent, child, directTargetChild));

      if (started && offsetAnimator != null) {
        offsetAnimator.cancel();
      }

      if (((float) child.getBottom()) <= child.seslGetCollapsedHeight()) {
        mLifted = true;
        child.setLifted(true);
        mDiffY_Touch = 0.0f;
      } else {
        mLifted = false;
        child.setLifted(false);
      }

      child.updateInternalCollapsedHeight();

      lastNestedScrollingChildRef = null;
      lastStartedType = type;
      mToolisMouse = child.getIsMouse();

      return started;
    }

    private boolean canScrollChildren(@NonNull SamsungCoordinatorLayout parent, @NonNull T child, @NonNull View directTargetChild) {
      return child.hasScrollableChildren() && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
    }

    @Override
    public void onNestedPreScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull T child, View target, int dx, int dy, int[] consumed, int type) {
      if (dy != 0) {
        int min;
        int max;
        if (dy < 0) {
          min = -child.getTotalScrollRange();
          max = min + child.getDownNestedPreScrollRange();

          mIsFlingScrollDown = true;
          mIsFlingScrollUp = false;
          if (((double) child.getBottom()) >= ((double) child.getHeight()) * 0.52d) {
            mIsSetStaticDuration = true;
          }
          if (dy < -30) {
            mIsFlingScrollDown = true;
          } else {
            mVelocity = 0.0f;
            mIsFlingScrollDown = false;
          }
        } else {
          min = -child.getUpNestedPreScrollRange();
          max = 0;

          mIsFlingScrollDown = false;
          mIsFlingScrollUp = true;
          if (((double) child.getBottom()) <= ((double) child.getHeight()) * 0.43d) {
            mIsSetStaticDuration = true;
          }
          if (dy > 30) {
            mIsFlingScrollUp = true;
          } else {
            mVelocity = 0.0f;
            mIsFlingScrollUp = false;
          }
          if (getTopAndBottomOffset() == min) {
            mIsScrollHold = true;
          }
        }
        if (isFlingRunnable()) {
          onFlingFinished(coordinatorLayout, child);
        }
        if (min != max) {
          consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
        }
      }
      if (child.isLiftOnScroll()) {
        child.setLiftedState(child.shouldLift(target));
      }
      stopNestedScrollIfNeeded(dy, child, target, type);
    }

    @Override
    public void onNestedScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull T child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
      if (isScrollHoldMode(child)) {
        if (dyUnconsumed >= 0 || mIsScrollHold) {
          ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
        } else {
          consumed[1] = scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
          stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
        }
      } else if (dyUnconsumed < 0) {
        consumed[1] = scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
      }

      if (dyUnconsumed == 0) {
        updateAccessibilityActions(coordinatorLayout, child);
      }
    }

    private void stopNestedScrollIfNeeded(int dy, @NonNull T child, View target, int type) {
      if (type == ViewCompat.TYPE_NON_TOUCH) {
        if ((dy < 0 && getTopBottomOffsetForScrollingSibling() == 0) || (dy > 0 && getTopBottomOffsetForScrollingSibling() == (-child.getDownNestedScrollRange()))) {
          ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
        }
      }
    }

    @Override
    public void onStopNestedScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull T abl, View target, int type) {
      if (mLastTouchEvent == MotionEvent.ACTION_CANCEL || mLastTouchEvent == MotionEvent.ACTION_UP || mLastInterceptTouchEvent == MotionEvent.ACTION_CANCEL || mLastInterceptTouchEvent == MotionEvent.ACTION_UP) {
        snapToChildIfNeeded(coordinatorLayout, abl);
      }
      if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        if (abl.isLiftOnScroll()) {
          abl.setLiftedState(abl.shouldLift(target));
        }
        if (mIsScrollHold) {
          mIsScrollHold = false;
        }
      }

      lastNestedScrollingChildRef = new WeakReference<>(target);
    }

    public void setDragCallback(@Nullable BaseDragCallback callback) {
      onDragCallback = callback;
    }

    // kang
    private void animateOffsetTo(final SamsungCoordinatorLayout coordinatorLayout, @NonNull final T child, final int offset, float velocity) {
      int i = (Math.abs(this.mVelocity) > 0.0f ? 1 : (Math.abs(this.mVelocity) == 0.0f ? 0 : -1));
      int i2 = 250;
      int abs = (i <= 0 || Math.abs(this.mVelocity) > 3000.0f) ? 250 : (int) (((double) (3000.0f - Math.abs(this.mVelocity))) * 0.4d);
      if (abs <= 250) {
        abs = 250;
      }
      if (this.mIsSetStaticDuration) {
        this.mIsSetStaticDuration = false;
      } else {
        i2 = abs;
      }
      if (Math.abs(this.mVelocity) < 2000.0f) {
        animateOffsetWithDuration(coordinatorLayout, child, offset, i2);
      }
      this.mVelocity = 0.0f;
    }
    // kang

    private void animateOffsetWithDuration(final SamsungCoordinatorLayout coordinatorLayout, final T child, final int offset, final int duration) {
      final int currentOffset = getTopBottomOffsetForScrollingSibling();
      if (currentOffset == offset) {
        if (offsetAnimator != null && offsetAnimator.isRunning()) {
          offsetAnimator.cancel();
        }
        return;
      }

      if (offsetAnimator == null) {
        offsetAnimator = new ValueAnimator();
        offsetAnimator.setInterpolator(SeslAnimationUtils.SINE_OUT_80);
        offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animator) {
            setHeaderTopBottomOffset(coordinatorLayout, child, (int) animator.getAnimatedValue());
          }
        });
      } else {
        offsetAnimator.cancel();
      }

      offsetAnimator.setDuration(Math.min(duration, MAX_OFFSET_ANIMATION_DURATION));
      offsetAnimator.setIntValues(currentOffset, offset);
      offsetAnimator.start();
    }

    private int getChildIndexOnOffset(@NonNull T abl, int offset) {
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        int top = child.getTop();
        int bottom = child.getBottom();

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (checkFlag(lp.getScrollFlags(), LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
          top -= lp.topMargin;
          bottom += lp.bottomMargin;
        }

        if (abl.seslGetTCScrollRange() != 0) {
          bottom += abl.seslGetTCScrollRange();
        }

        offset += (abl.isLifted() ? abl.getPaddingBottom() : 0);

        if (top <= -offset && bottom >= -offset) {
          return i;
        }
      }
      return -1;
    }

    // kang
    private void snapToChildIfNeeded(SamsungCoordinatorLayout coordinatorLayout, T t) {
      int topBottomOffsetForScrollingSibling = getTopBottomOffsetForScrollingSibling();
      int childIndexOnOffset = getChildIndexOnOffset(t, topBottomOffsetForScrollingSibling);
      View childAt = coordinatorLayout.getChildAt(1);
      if (childIndexOnOffset >= 0) {
        View childAt2 = t.getChildAt(childIndexOnOffset);
        LayoutParams layoutParams = (LayoutParams) childAt2.getLayoutParams();
        int scrollFlags = layoutParams.getScrollFlags();
        if ((scrollFlags & 4096) == 4096) {
          seslHasNoSnapFlag(true);
          return;
        }
        seslHasNoSnapFlag(false);
        int seslGetTCScrollRange = t.getCanScroll() ? t.seslGetTCScrollRange() : 0;
        if (((float) t.getBottom()) >= t.seslGetCollapsedHeight()) {
          int i = -childAt2.getTop();
          int i2 = -childAt2.getBottom();
          if (childIndexOnOffset == t.getChildCount() - 1) {
            i2 += t.getTopInset();
          }
          if (checkFlag(scrollFlags, 2)) {
            if (t.getCanScroll()) {
              i2 = (int) (((float) i2) + (t.seslGetCollapsedHeight() - ((float) t.getPaddingBottom())));
            } else {
              i2 += ViewCompat.getMinimumHeight(childAt2);
            }
          } else if (checkFlag(scrollFlags, 5)) {
            int minimumHeight = ViewCompat.getMinimumHeight(childAt2) + i2;
            if (topBottomOffsetForScrollingSibling < minimumHeight) {
              i = minimumHeight;
            } else {
              i2 = minimumHeight;
            }
          }
          if (checkFlag(scrollFlags, 32)) {
            i += layoutParams.topMargin;
            i2 -= layoutParams.bottomMargin;
          }
          int i3 = (!this.mLifted ? ((double) topBottomOffsetForScrollingSibling) >= ((double) (i2 + i)) * 0.43d : ((double) topBottomOffsetForScrollingSibling) >= ((double) (i2 + i)) * 0.52d) ? i : i2;
          if (childAt == null) {
            Log.w(TAG, "coordinatorLayout.getChildAt(1) is null");
            i = i3;
          } else {
            if (this.mIsFlingScrollUp) {
              this.mIsFlingScrollUp = false;
              this.mIsFlingScrollDown = false;
            } else {
              i2 = i3;
            }
            if (!this.mIsFlingScrollDown || ((float) childAt.getTop()) <= t.seslGetCollapsedHeight()) {
              i = i2;
            } else {
              this.mIsFlingScrollDown = false;
            }
          }
          animateOffsetTo(coordinatorLayout, t, MathUtils.clamp(i, -t.getTotalScrollRange(), 0), 0.0f);
        } else if (t.getCanScroll()) {
          int seslGetCollapsedHeight = (((int) t.seslGetCollapsedHeight()) - t.getTotalScrollRange()) + seslGetTCScrollRange;
          int i4 = -t.getTotalScrollRange();
          int i5 = ((double) (t.getBottom() + seslGetTCScrollRange)) >= ((double) t.seslGetCollapsedHeight()) * 0.48d ? seslGetCollapsedHeight : i4;
          if (!this.mIsFlingScrollUp) {
            i4 = i5;
          }
          if (!this.mIsFlingScrollDown) {
            seslGetCollapsedHeight = i4;
          }
          animateOffsetTo(coordinatorLayout, t, MathUtils.clamp(seslGetCollapsedHeight, -t.getTotalScrollRange(), 0), 0.0f);
        }
      }
    }
    // kang

    private static boolean checkFlag(final int flags, final int check) {
      return (flags & check) == check;
    }

    @Override
    public boolean onMeasureChild(@NonNull SamsungCoordinatorLayout parent, @NonNull T child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
      final SamsungCoordinatorLayout.LayoutParams lp = (SamsungCoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (lp.height == SamsungCoordinatorLayout.LayoutParams.WRAP_CONTENT) {
        parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightUsed);
        return true;
      }

      return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private int getImmPendingActionOffset(SamsungAppBarLayout appBarLayout) {
      Behavior behavior = (Behavior) ((SamsungCoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();
      if (appBarLayout.getCanScroll() && (behavior instanceof SeslImmersiveScrollBehavior)) {
        return ((int) appBarLayout.seslGetCollapsedHeight()) + appBarLayout.seslGetTCScrollRange();
      }
      return 0;
    }

    @Override
    public boolean onLayoutChild(@NonNull SamsungCoordinatorLayout parent, @NonNull T abl, int layoutDirection) {
      boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

      final int pendingAction = abl.getPendingAction();
      if (offsetToChildIndexOnLayout >= 0 && (pendingAction & PENDING_ACTION_FORCE) == 0) {
        View childAt = abl.getChildAt(offsetToChildIndexOnLayout);

        int i;
        if (offsetToChildIndexOnLayoutIsMinHeight) {
          i = ViewCompat.getMinimumHeight(childAt) + abl.getTopInset();
        } else {
          i = Math.round(((float) childAt.getHeight()) * offsetToChildIndexOnLayoutPerc);
        }
        setHeaderTopBottomOffset(parent, abl, (-childAt.getBottom()) + i);
      } else if (pendingAction != PENDING_ACTION_NONE) {
        final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
        if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
          int offset = ((-abl.getTotalScrollRange()) + getImmPendingActionOffset(abl)) - abl.getImmersiveTopInset();
          if (animate) {
            animateOffsetTo(parent, abl, offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, offset);
          }
        } else if ((pendingAction & PENDING_ACTION_COLLAPSED_IMM) != 0) {
          int offset = (-abl.getTotalScrollRange()) + getImmPendingActionOffset(abl);
          if (parent.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && abl.getImmersiveTopInset() == 0 && abl.seslGetHeightProPortion() == 0.0f) {
            offset = 0;
          }
          if (animate) {
            animateOffsetTo(parent, abl, offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, offset);
          }
        } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
          if (animate) {
            animateOffsetTo(parent, abl, 0, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, 0);
          }
        }
      }

      abl.resetPendingAction();
      offsetToChildIndexOnLayout = -1;

      setTopAndBottomOffset(MathUtils.clamp(getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));

      updateAppBarLayoutDrawableState(parent, abl, getTopAndBottomOffset(), 0, true);

      abl.onOffsetChanged(getTopAndBottomOffset());

      updateAccessibilityActions(parent, abl);
      return handled;
    }

    private void updateAccessibilityActions(SamsungCoordinatorLayout coordinatorLayout, @NonNull T appBarLayout) {
      ViewCompat.removeAccessibilityAction(coordinatorLayout, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD.getId());
      ViewCompat.removeAccessibilityAction(coordinatorLayout, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD.getId());
      View scrollingView = findFirstScrollingChild(coordinatorLayout);

      if (scrollingView == null || appBarLayout.getTotalScrollRange() == 0) {
        return;
      }
      SamsungCoordinatorLayout.LayoutParams lp = (SamsungCoordinatorLayout.LayoutParams) scrollingView.getLayoutParams();
      if (!(lp.getBehavior() instanceof ScrollingViewBehavior)) {
        return;
      }
      addAccessibilityScrollActions(coordinatorLayout, appBarLayout, scrollingView);
    }

    private void addAccessibilityScrollActions(final SamsungCoordinatorLayout coordinatorLayout, @NonNull final T appBarLayout, @NonNull final View scrollingView) {
      if (getTopBottomOffsetForScrollingSibling() != -appBarLayout.getTotalScrollRange() && scrollingView.canScrollVertically(1)) {
        addActionToExpand(coordinatorLayout, appBarLayout, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD, false);
      }
      if (getTopBottomOffsetForScrollingSibling() != 0) {
        if (scrollingView.canScrollVertically(-1)) {
          final int dy = -appBarLayout.getDownNestedPreScrollRange();
          if (dy != 0) {
            ViewCompat.replaceAccessibilityAction(coordinatorLayout, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD, null, new AccessibilityViewCommand() {
              @Override
              public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
                onNestedPreScroll(coordinatorLayout, appBarLayout, scrollingView, 0, dy, new int[] {0, 0}, ViewCompat.TYPE_NON_TOUCH);
                return true;
              }
            });
          }
        } else {
          addActionToExpand(coordinatorLayout, appBarLayout, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD, true);
        }
      }
    }

    private void addActionToExpand(SamsungCoordinatorLayout parent, @NonNull final T appBarLayout, @NonNull AccessibilityNodeInfoCompat.AccessibilityActionCompat action, final boolean expand) {
      ViewCompat.replaceAccessibilityAction(parent, action, null, new AccessibilityViewCommand() {
        @Override
        public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
          appBarLayout.setExpanded(expand);
          return true;
        }
      });
    }

    @Override
    boolean canDragView(T view) {
      if (onDragCallback != null) {
        return onDragCallback.canDrag(view);
      }

      if (lastNestedScrollingChildRef != null) {
        final View scrollingView = lastNestedScrollingChildRef.get();
        return scrollingView != null && scrollingView.isShown() && !scrollingView.canScrollVertically(-1);
      } else {
        return true;
      }
    }

    @Override
    void onFlingFinished(@NonNull SamsungCoordinatorLayout parent, @NonNull T layout) {
      if (scroller != null) {
        scroller.forceFinished(true);
      }
    }

    @Override
    int getMaxDragOffset(@NonNull T view) {
      return -view.getDownNestedScrollRange();
    }

    @Override
    int getScrollRangeForDragFling(@NonNull T view) {
      return view.getTotalScrollRange();
    }

    @Override
    int setHeaderTopBottomOffset(@NonNull SamsungCoordinatorLayout coordinatorLayout, @NonNull T appBarLayout, int newOffset, int minOffset, int maxOffset) {
      final int curOffset = getTopBottomOffsetForScrollingSibling();
      int consumed = 0;

      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
        if (curOffset != newOffset) {
          final int interpolatedOffset = appBarLayout.hasChildWithInterpolator() ? interpolateOffset(appBarLayout, newOffset) : newOffset;

          final boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

          consumed = curOffset - newOffset;
          offsetDelta = newOffset - interpolatedOffset;

          if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
            coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
          }

          appBarLayout.onOffsetChanged(getTopAndBottomOffset());

          updateAppBarLayoutDrawableState(coordinatorLayout, appBarLayout, newOffset, newOffset < curOffset ? -1 : 1, false);
        }
      } else {
        offsetDelta = 0;
      }

      updateAccessibilityActions(coordinatorLayout, appBarLayout);
      return consumed;
    }

    boolean isOffsetAnimatorRunning() {
      return offsetAnimator != null && offsetAnimator.isRunning();
    }

    private int interpolateOffset(@NonNull T layout, final int offset) {
      final int absOffset = Math.abs(offset);

      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        final SamsungAppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final Interpolator interpolator = childLp.getScrollInterpolator();

        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          if (interpolator != null) {
            int childScrollableHeight = 0;
            final int flags = childLp.getScrollFlags();
            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
              childScrollableHeight += child.getHeight() + childLp.topMargin + childLp.bottomMargin;

              if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
              }
            }

            if (ViewCompat.getFitsSystemWindows(child)) {
              childScrollableHeight -= layout.getTopInset();
            }

            if (childScrollableHeight > 0) {
              final int offsetForView = absOffset - child.getTop();
              final int interpolatedDiff = Math.round(childScrollableHeight * interpolator.getInterpolation(offsetForView / (float) childScrollableHeight));

              return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
            }
          }

          break;
        }
      }

      return offset;
    }

    private void updateAppBarLayoutDrawableState(@NonNull final SamsungCoordinatorLayout parent, @NonNull final T layout, final int offset, final int direction, final boolean forceJump) {
      final View child = getAppBarChildOnOffset(layout, offset);
      boolean lifted = false;
      if (child != null) {
        final SamsungAppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final int flags = childLp.getScrollFlags();

        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
          final int minHeight = ViewCompat.getMinimumHeight(child);

          if (direction > 0 && (flags & (LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)) != 0) {
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset() - layout.getImmersiveTopInset();
          } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset() - layout.getImmersiveTopInset();
          }
        }

        if (layout.isLiftOnScroll()) {
          lifted = layout.shouldLift(findFirstScrollingChild(parent));
        }

        final boolean changed = layout.setLiftedState(lifted);

        if (forceJump || (changed && shouldJumpElevationState(parent, layout))) {
          layout.jumpDrawablesToCurrentState();
        }
      }
    }

    private boolean shouldJumpElevationState(@NonNull SamsungCoordinatorLayout parent, @NonNull T layout) {
      final List<View> dependencies = parent.getDependents(layout);
      for (int i = 0, size = dependencies.size(); i < size; i++) {
        final View dependency = dependencies.get(i);
        final SamsungCoordinatorLayout.LayoutParams lp = (SamsungCoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        final SamsungCoordinatorLayout.Behavior behavior = lp.getBehavior();

        if (behavior instanceof ScrollingViewBehavior) {
          return ((ScrollingViewBehavior) behavior).getOverlayTop() != 0;
        }
      }
      return false;
    }

    @Nullable
    private static View getAppBarChildOnOffset(@NonNull final SamsungAppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);
      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          return child;
        }
      }
      return null;
    }

    @Nullable
    private View findFirstScrollingChild(@NonNull SamsungCoordinatorLayout parent) {
      for (int i = 0, z = parent.getChildCount(); i < z; i++) {
        final View child = parent.getChildAt(i);
        if (child instanceof NestedScrollingChild || child instanceof ListView || child instanceof ScrollView) {
          return child;
        }
      }
      return null;
    }

    @Override
    int getTopBottomOffsetForScrollingSibling() {
      return getTopAndBottomOffset() + offsetDelta;
    }

    @Override
    public Parcelable onSaveInstanceState(@NonNull SamsungCoordinatorLayout parent, @NonNull T abl) {
      Parcelable superState = super.onSaveInstanceState(parent, abl);

      for (int i = 0; i < abl.getChildCount(); i++) {
        View child = abl.getChildAt(i);
        int bottom = child.getBottom() + getTopAndBottomOffset();

        if (child.getTop() + getTopAndBottomOffset() <= 0 && bottom >= 0) {
          SavedState savedState = new SavedState(superState);
          savedState.firstVisibleChildIndex = i;
          savedState.firstVisibleChildAtMinimumHeight = bottom == ViewCompat.getMinimumHeight(child) + abl.getTopInset();
          savedState.firstVisibleChildPercentageShown = ((float) bottom) / ((float) child.getHeight());
          return savedState;
        }
      }

      return superState;
    }

    @Override
    public void onRestoreInstanceState(@NonNull SamsungCoordinatorLayout parent, @NonNull T appBarLayout, Parcelable state) {
      if (state instanceof SavedState) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(parent, appBarLayout, savedState.getSuperState());
        offsetToChildIndexOnLayout = savedState.firstVisibleChildIndex;
        offsetToChildIndexOnLayoutPerc = savedState.firstVisibleChildPercentageShown;
        offsetToChildIndexOnLayoutIsMinHeight = savedState.firstVisibleChildAtMinimumHeight;
      } else {
        super.onRestoreInstanceState(parent, appBarLayout, state);
        offsetToChildIndexOnLayout = -1;
      }
    }

    protected static class SavedState extends AbsSavedState {
      boolean firstVisibleChildAtMinimumHeight;
      int firstVisibleChildIndex;
      float firstVisibleChildPercentageShown;

      public SavedState(@NonNull Parcel source, ClassLoader loader) {
        super(source, loader);
        firstVisibleChildIndex = source.readInt();
        firstVisibleChildPercentageShown = source.readFloat();
        firstVisibleChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      @Override
      public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(firstVisibleChildIndex);
        dest.writeFloat(firstVisibleChildPercentageShown);
        dest.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
      }

      public static final Creator<SavedState> CREATOR = new ClassLoaderCreator<SavedState>() {
        @NonNull
        @Override
        public SavedState createFromParcel(@NonNull Parcel source, ClassLoader loader) {
          return new SavedState(source, loader);
        }

        @Nullable
        @Override
        public SavedState createFromParcel(@NonNull Parcel source) {
          return new SavedState(source, null);
        }

        @NonNull
        @Override
        public SavedState[] newArray(int size) {
          return new SavedState[size];
        }
      };
    }

    // kang
    private boolean isScrollHoldMode(T appBarLayout) {
      if (mToolisMouse) {
        return false;
      }
      int childIndexOnOffset = getChildIndexOnOffset(appBarLayout, getTopBottomOffsetForScrollingSibling());
      return childIndexOnOffset < 0 || (((LayoutParams) appBarLayout.getChildAt(childIndexOnOffset).getLayoutParams()).getScrollFlags() & 65536) != 65536;
    }

    @Override
    public boolean onNestedPreFling(SamsungCoordinatorLayout coordinatorLayout, T t, View view, float f, float f2) {
      this.mVelocity = f2;
      if (f2 < -300.0f) {
        this.mIsFlingScrollDown = true;
        this.mIsFlingScrollUp = false;
      } else if (f2 > 300.0f) {
        this.mIsFlingScrollDown = false;
        this.mIsFlingScrollUp = true;
      } else {
        this.mVelocity = 0.0f;
        this.mIsFlingScrollDown = false;
        this.mIsFlingScrollUp = false;
        return true;
      }
      return super.onNestedPreFling(coordinatorLayout, t, view, f, f2);
    }

    @Override
    public boolean onTouchEvent(SamsungCoordinatorLayout var1, T var2, MotionEvent var3) {
      if (this.mTouchSlop < 0) {
        this.mTouchSlop = ViewConfiguration.get(var1.getContext()).getScaledTouchSlop();
      }

      int var4 = var3.getAction();
      this.mToolisMouse = var2.getIsMouse();
      float var6;
      if (var4 != 0) {
        if (var4 != 1) {
          if (var4 == 2) {
            this.mDirectTouchAppbar = true;
            float var5 = var3.getY();
            var6 = this.mLastMotionY_Touch;
            if (var5 - var6 != 0.0F) {
              this.mDiffY_Touch = var5 - var6;
            }

            if (Math.abs(this.mDiffY_Touch) > (float)this.mTouchSlop) {
              this.mLastMotionY_Touch = var5;
            }

            return super.onTouchEvent(var1, var2, var3);
          }

          if (var4 != 3) {
            return super.onTouchEvent(var1, var2, var3);
          }
        }

        if (Math.abs(this.mDiffY_Touch) > 21.0F) {
          var6 = this.mDiffY_Touch;
          if (var6 < 0.0F) {
            this.mIsFlingScrollUp = true;
            this.mIsFlingScrollDown = false;
          } else if (var6 > 0.0F) {
            this.mIsFlingScrollUp = false;
            this.mIsFlingScrollDown = true;
          }
        } else {
          this.touchX = 0.0F;
          this.touchY = 0.0F;
          this.mIsFlingScrollUp = false;
          this.mIsFlingScrollDown = false;
          this.mLastMotionY_Touch = 0.0F;
        }

        if (this.mDirectTouchAppbar) {
          this.mDirectTouchAppbar = false;
          this.snapToChildIfNeeded(var1, var2);
        }
      } else {
        this.mDirectTouchAppbar = true;
        this.touchX = var3.getX();
        var6 = var3.getY();
        this.touchY = var6;
        this.mLastMotionY_Touch = var6;
        this.mDiffY_Touch = 0.0F;
      }

      return super.onTouchEvent(var1, var2, var3);
    }
    // kang
  }

  public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {
    public ScrollingViewBehavior() {}

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);

      final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
      setOverlayTop(a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
      a.recycle();
    }

    @Override
    public boolean layoutDependsOn(SamsungCoordinatorLayout parent, View child, View dependency) {
      return dependency instanceof SamsungAppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull SamsungCoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      offsetChildAsNeeded(child, dependency);
      updateLiftedStateIfNeeded(child, dependency);
      return false;
    }

    @Override
    public void onDependentViewRemoved(@NonNull SamsungCoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      if (dependency instanceof SamsungAppBarLayout) {
        ViewCompat.removeAccessibilityAction(parent, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD.getId());
        ViewCompat.removeAccessibilityAction(parent, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD.getId());
      }
    }

    @Override
    public boolean onRequestChildRectangleOnScreen(@NonNull SamsungCoordinatorLayout parent, @NonNull View child, @NonNull Rect rectangle, boolean immediate) {
      final SamsungAppBarLayout header = findFirstDependency(parent.getDependencies(child));
      if (header != null) {
        rectangle.offset(child.getLeft(), child.getTop());

        final Rect parentRect = tempRect1;
        parentRect.set(0, 0, parent.getWidth(), parent.getHeight());

        if (!parentRect.contains(rectangle)) {
          header.setExpanded(false, !immediate);
          return true;
        }
      }
      return false;
    }

    private void offsetChildAsNeeded(@NonNull View child, @NonNull View dependency) {
      final SamsungCoordinatorLayout.Behavior behavior = ((SamsungCoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        final BaseBehavior ablBehavior = (BaseBehavior) behavior;
        ViewCompat.offsetTopAndBottom(child, (dependency.getBottom() - child.getTop()) + ablBehavior.offsetDelta + getVerticalLayoutGap() - getOverlapPixelsForOffset(dependency));
      }
    }

    @Override
    float getOverlapRatioForOffset(final View header) {
      if (header instanceof SamsungAppBarLayout) {
        final SamsungAppBarLayout abl = (SamsungAppBarLayout) header;
        final int totalScrollRange = abl.getTotalScrollRange();
        final int preScrollDown = abl.getDownNestedPreScrollRange();
        final int offset = getAppBarLayoutOffset(abl);

        if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
          return 0;
        } else {
          final int availScrollRange = totalScrollRange - preScrollDown;
          if (availScrollRange != 0) {
            return 1f + (offset / (float) availScrollRange);
          }
        }
      }
      return 0f;
    }

    private static int getAppBarLayoutOffset(@NonNull SamsungAppBarLayout abl) {
      final SamsungCoordinatorLayout.Behavior behavior = ((SamsungCoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        return ((BaseBehavior) behavior).getTopBottomOffsetForScrollingSibling();
      }
      return 0;
    }

    @Nullable
    @Override
    SamsungAppBarLayout findFirstDependency(@NonNull List<View> views) {
      for (int i = 0, z = views.size(); i < z; i++) {
        View view = views.get(i);
        if (view instanceof SamsungAppBarLayout) {
          return (SamsungAppBarLayout) view;
        }
      }
      return null;
    }

    @Override
    int getScrollRange(View v) {
      if (v instanceof SamsungAppBarLayout) {
        return ((SamsungAppBarLayout) v).getTotalScrollRange();
      } else {
        return super.getScrollRange(v);
      }
    }

    private void updateLiftedStateIfNeeded(View child, View dependency) {
      if (dependency instanceof SamsungAppBarLayout) {
        SamsungAppBarLayout appBarLayout = (SamsungAppBarLayout) dependency;
        if (appBarLayout.isLiftOnScroll()) {
          appBarLayout.setLiftedState(appBarLayout.shouldLift(child));
        }
      }
    }
  }


  private boolean isDexEnabled() {
    if (getContext() == null) {
      return false;
    }
    return SeslConfigurationReflector.isDexEnabled(getContext().getResources().getConfiguration());
  }

  protected boolean isDetachedState() {
    return mIsDetachedState;
  }
}
