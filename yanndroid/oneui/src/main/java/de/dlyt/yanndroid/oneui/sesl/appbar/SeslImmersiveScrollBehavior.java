package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimationController;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.PathInterpolator;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsAnimationCompat;
import androidx.core.view.WindowInsetsAnimationControlListenerCompat;
import androidx.core.view.WindowInsetsAnimationControllerCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.view.SeslDecorViewReflector;

import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.SamsungCoordinatorLayout;
import de.dlyt.yanndroid.oneui.sesl.support.WindowManagerSupport;

public final class SeslImmersiveScrollBehavior extends SamsungAppBarLayout.Behavior {
    private boolean mIsOneUI4;
    private static final int MSG_APPEAR_ANIMATION = 100;
    private static final String TAG = "SeslImmersiveScrollBehavior";
    private boolean isRoundedCornerHide = false;
    private WindowInsetsAnimationControllerCompat mAnimationController;
    private SamsungAppBarLayout mAppBarLayout;
    private View mBottomArea;
    boolean mCalledHideShowOnLayoutChlid = false;
    private boolean mCanImmersiveScroll;
    private CancellationSignal mCancellationSignal;
    private SamsungCollapsingToolbarLayout mCollapsingToolbarLayout;
    private View mContentView;
    private Context mContext;
    private SamsungCoordinatorLayout mCoordinatorLayout;
    private float mCurOffset = 0.0f;
    private WindowInsetsAnimationCompat.Callback mCustomWindowInsetsAnimation = null;
    private View mDecorView;
    private WindowInsetsCompat mDecorViewInset;
    private float mHeightProportion;
    private boolean mIsDeskTopMode;
    private boolean mIsMultiWindow;
    private View mNavigationBarBg;
    private int mNavigationBarHeight;
    private boolean mNeedRestoreAnim = true;
    private ValueAnimator mOffsetAnimator;
    private WindowInsetsAnimationController mPendingRequestOnReady;
    private int mPrevOffset;
    private int mPrevOrientation;
    private boolean mShownAtDown;
    private View mStatusBarBg;
    private int mStatusBarHeight;
    private View mTargetView;
    private boolean mToolIsMouse;
    private WindowInsetsControllerCompat mWindowInsetsController = null;
    private boolean useCustomAnimationCallback = false;

    private Handler mAnimationHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            if (message.what == MSG_APPEAR_ANIMATION) {
                startRestoreAnimation();
            }
        }
    };

    // kang
    @SuppressLint("LongLogTag")
    private SamsungAppBarLayout.OnOffsetChangedListener mOffsetChangedListener = new SamsungAppBarLayout.OnOffsetChangedListener() {
        @Override
        public void onOffsetChanged(SamsungAppBarLayout appBarLayout, int verticalOffset) {
            if (mAppBarLayout != null && mAppBarLayout.isDetachedState()) {
                if (!useCustomAnimationCallback) {
                    boolean var3 = mCanImmersiveScroll;
                    float var4 = 0.0F;
                    float var10;
                    if (var3) {
                        View var5 = mBottomArea;
                        byte var6 = 0;
                        int var7;
                        if (var5 != null) {
                            var7 = mBottomArea.getHeight();
                        } else {
                            var7 = 0;
                        }

                        var4 = appBarLayout.seslGetCollapsedHeight();
                        float var8 = (float)(mNavigationBarHeight + var7);
                        float var21;
                        int var9 = (var21 = var4 - 0.0F) == 0.0F ? 0 : (var21 < 0.0F ? -1 : 1);
                        if (var9 == 0) {
                            var10 = 1.0F;
                        } else {
                            var10 = var4;
                        }

                        var10 = var8 / var10;
                        var8 = (float)(appBarLayout.getTotalScrollRange() - appBarLayout.seslGetTCScrollRange() + verticalOffset) - var4;
                        float var11 = (float)mStatusBarHeight + var8;
                        float var12 = (var10 + 1.0F) * var8;
                        float var13 = Math.min((float)mStatusBarHeight, (float)mStatusBarHeight + var8);
                        float var14 = Math.max(Math.min((float)mNavigationBarHeight, (float)mNavigationBarHeight + var12), 0.0F);
                        var10 = (float)mNavigationBarHeight;
                        int var15;
                        if (mNavigationBarHeight != 0) {
                            var15 = mNavigationBarHeight;
                        } else {
                            var15 = 1;
                        }

                        float var16 = (var10 - var14) / (float)var15;
                        if ((float)appBarLayout.getBottom() <= var4) {
                            if (dispatchImmersiveScrollEnable()) {
                                if (mBottomArea != null && mBottomArea.getVisibility() != View.GONE && var7 != 0) {
                                    var10 = Math.min((float)var7 + var12, var14);
                                    mBottomArea.setTranslationY(-var10);
                                    if (mBottomArea.getVisibility() != View.VISIBLE) {
                                        var7 = 0;
                                    }

                                    var10 = Math.max((float)var7 + var10, 0.0F);
                                    var7 = appBarLayout.getTotalScrollRange();
                                } else {
                                    var10 = Math.max(var14, 0.0F);
                                    var7 = appBarLayout.getTotalScrollRange();
                                }

                                var4 = (float)var7;
                                float var17 = (float)verticalOffset;
                                if (mNavigationBarBg != null) {
                                    if (mDecorViewInset.getDisplayCutout() != null) {
                                        mNavigationBarBg.setTranslationY(-Math.min(0.0F, var12));
                                    } else {
                                        mNavigationBarBg.setTranslationY(0.0F);
                                    }
                                } else if (mNavigationBarHeight != 0) {
                                    findSystemBarsBackground();
                                    if (mNavigationBarBg != null) {
                                        mNavigationBarBg.setTranslationY(0.0F);
                                    }
                                }

                                if (mStatusBarBg != null) {
                                    mStatusBarBg.setTranslationY(Math.min(0.0F, var8));
                                }

                                if (mCurOffset != var11) {
                                    mCurOffset = var11;
                                    if (mAnimationController != null) {
                                        if (mAnimationController.isFinished()) {
                                            Log.e(TAG, "AnimationController is already finished by App side");
                                        } else {
                                            label137: {
                                                var9 = (int)var14;
                                                forceHideRoundedCorner(var9);
                                                if (isPinEdgeEnabled(mContext)) {
                                                    Insets var19 = mDecorViewInset.getInsets(WindowInsetsCompat.Type.navigationBars());
                                                    verticalOffset = getPinnedEdgeWidth(mContext);
                                                    var7 = getEdgeArea(mContext);
                                                    if (verticalOffset == var19.left && var7 == 0) {
                                                        byte var20 = 0;
                                                        var7 = verticalOffset;
                                                        verticalOffset = var20;
                                                        break label137;
                                                    }

                                                    if (verticalOffset == var19.right && var7 == 1) {
                                                        var7 = var6;
                                                        break label137;
                                                    }
                                                }

                                                verticalOffset = 0;
                                                var7 = var6;
                                            }

                                            mAnimationController.setInsetsAndAlpha(Insets.of(var7, (int)var13, verticalOffset, var9), 1.0F, var16);
                                        }
                                    }
                                }

                                var10 = var10 + var4 + var17;
                            } else {
                                if (mStatusBarBg != null) {
                                    mStatusBarBg.setTranslationY(0.0F);
                                }

                                if (mNavigationBarBg != null) {
                                    mNavigationBarBg.setTranslationY(0.0F);
                                }

                                var8 = (float) mAppBarLayout.getTotalScrollRange() + verticalOffset;
                                var10 = var8;
                                if (mBottomArea != null) {
                                    var10 = (float)var7;
                                    if (var9 == 0) {
                                        var4 = 1.0F;
                                    }

                                    var4 = var10 / var4;
                                    var10 -= (float) mAppBarLayout.getBottom() * var4;
                                    mBottomArea.setTranslationY(Math.max(var10, 0.0F));
                                    var10 = (float)((int)(var8 + (float)mBottomArea.getHeight() - Math.max(var10, 0.0F)));
                                }

                                finishWindowInsetsAnimationController();
                            }
                        } else {
                            var10 = (float) mAppBarLayout.getTotalScrollRange() + verticalOffset;
                            var4 = var10;
                            if (mIsMultiWindow) {
                                var4 = var10;
                                if (mBottomArea != null) {
                                    mBottomArea.setTranslationY(0.0F);
                                    var4 = var10 + (float)mBottomArea.getHeight();
                                }
                            }

                            var10 = var4;
                            if (!mIsMultiWindow) {
                                var10 = var4;
                                if (mBottomArea != null) {
                                    var10 = var4;
                                    if (mDecorViewInset != null) {
                                        if (isNavigationBarBottomPosition()) {
                                            mBottomArea.setTranslationY((float)(-mNavigationBarHeight));
                                        } else if (mNavigationBarBg != null && mNavigationBarBg.getTranslationY() != 0.0F) {
                                            mBottomArea.setTranslationY(0.0F);
                                        }

                                        var10 = var4 + (float)mBottomArea.getHeight() + (float)mNavigationBarHeight;
                                    }
                                }
                            }
                        }
                    } else {
                        if (mStatusBarBg != null) {
                            mStatusBarBg.setTranslationY(0.0F);
                        }

                        if (mNavigationBarBg != null) {
                            mNavigationBarBg.setTranslationY(0.0F);
                        }

                        var10 = var4;
                        if (mBottomArea != null) {
                            mBottomArea.setTranslationY(0.0F);
                            var10 = var4;
                        }
                    }

                    if (mAppBarLayout != null) {
                        mAppBarLayout.onImmOffsetChanged((int)var10);
                    }

                }
            }
        }
    };
    // kang

    private final WindowInsetsAnimationCompat.Callback mWindowAnimationCallback = new WindowInsetsAnimationCompat.Callback(WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
        @NonNull
        @Override
        public WindowInsetsCompat onProgress(@NonNull WindowInsetsCompat insets, @NonNull List<WindowInsetsAnimationCompat> runningAnimations) {
            return insets;
        }

        @Override
        public void onEnd(@NonNull WindowInsetsAnimationCompat animation) {
            super.onEnd(animation);
            if (mContentView != null && !mAppBarLayout.isDetachedState()) {
                mDecorViewInset = ViewCompat.getRootWindowInsets(mContentView);
                if (mDecorViewInset != null) {
                    ViewCompat.dispatchApplyWindowInsets(mContentView, mDecorViewInset);
                }
            }
        }
    };

    private WindowInsetsAnimationControlListenerCompat mWindowInsetsAnimationControlListener = new WindowInsetsAnimationControlListenerCompat() {
        @Override
        public void onReady(@NonNull WindowInsetsAnimationControllerCompat controller, int types) {
            if (mDecorView != null) {
                mCancellationSignal = null;
                mAnimationController = controller;
                mPendingRequestOnReady = null;
                setInsetsAndAlphaToDefault();
            }
        }

        @Override
        public void onFinished(@NonNull WindowInsetsAnimationControllerCompat controller) {
            resetWindowInsetsAnimationController();
        }

        @Override
        public void onCancelled(@Nullable WindowInsetsAnimationControllerCompat controller) {
            cancelWindowInsetsAnimationController();
        }
    };

    public SeslImmersiveScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        updateSystemBarsHeight();
        updateAppBarHeightProportion();
    }

    private boolean isDexEnabled() {
        if (mContext == null) {
            return false;
        }
        return SeslConfigurationReflector.isDexEnabled(mContext.getResources().getConfiguration());
    }

    @SuppressLint("LongLogTag")
    private boolean getCurrentNavbarCanMoveState() {
        try {
            return mContext.getApplicationContext().getResources().getBoolean(Resources.getSystem().getIdentifier("config_navBarCanMove", "bool", "android"));
        } catch (Exception e) {
            Log.e(TAG, "ERROR, e : " + e.getMessage());
            return true;
        }
    }

    private boolean isAccessibilityEnable() {
        if (mContext == null) {
            return false;
        }
        return ((AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).isTouchExplorationEnabled();
    }

    @SuppressLint("LongLogTag")
    private boolean canImmersiveScroll() {
        if (mAppBarLayout != null && !isDexEnabled() && !useCustomAnimationCallback) {
            if (mAppBarLayout.getIsMouse()) {
                prepareImmersiveScroll(false, false);
                return false;
            } else if (isAccessibilityEnable()) {
                Log.i(TAG, "Disable ImmersiveScroll due to accessibility enabled");
                updateOrientationState();
                prepareImmersiveScroll(false, true);
                return false;
            } else {
                if (mDecorView != null) {
                    mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
                    if (mDecorViewInset != null) {
                        updateOrientationState();
                        if (mDecorViewInset.isVisible(WindowInsetsCompat.Type.ime()) || (mDecorView.findFocus() instanceof EditText)) {
                            prepareImmersiveScroll(false, true);
                            return false;
                        }
                    }
                }
                if (mAppBarLayout.isActivatedImmsersiveScroll()) {
                    prepareImmersiveScroll(true, false);
                    if (mContext != null) {
                        Activity activity = getActivity(mContext);
                        if (activity == null && mAppBarLayout != null) {
                            mContext = mAppBarLayout.getContext();
                            activity = getActivity(mAppBarLayout.getContext());
                        }
                        if (activity != null) {
                            boolean isMultiWindow = isMultiWindow(activity);
                            if (mIsMultiWindow != isMultiWindow) {
                                forceRestoreWindowInset(true);
                                cancelWindowInsetsAnimationController();
                            }
                            mIsMultiWindow = isMultiWindow;
                            if (isMultiWindow) {
                                return false;
                            }
                        }
                    }
                    return !getCurrentNavbarCanMoveState() || updateOrientationState();
                }
                if (mAppBarLayout != null && mAppBarLayout.isImmersiveActivatedByUser()) {
                    cancelWindowInsetsAnimationController();
                }
                prepareImmersiveScroll(false, false);
            }
        }
        return false;
    }

    void setupDecorFitsSystemWindow(boolean fitsSystemWindows) {
        Activity activity = getActivity(mContext);
        if (activity != null && mAppBarLayout != null) {
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), fitsSystemWindows);
            if (mBottomArea != null) {
                mBottomArea.setTranslationY(0.0f);
            }
        }
        if (mStatusBarBg != null && mStatusBarBg.getTranslationY() != 0.0f) {
            mStatusBarBg.setTranslationY(0.0f);
        }
    }

    protected boolean dispatchImmersiveScrollEnable() {
        if (mAppBarLayout != null && !mAppBarLayout.isDetachedState()) {
            boolean canImmersiveScroll = canImmersiveScroll();
            setupDecorsFitSystemWindowState(canImmersiveScroll);
            updateAppBarHeightProportion();
            updateSystemBarsHeight();
            return canImmersiveScroll;

        }
        return false;
    }

    private void prepareImmersiveScroll(boolean allow, boolean force) {
        if (mCanImmersiveScroll != allow) {
            mCanImmersiveScroll = allow;
            forceRestoreWindowInset(force);
            setupDecorsFitSystemWindowState(allow);
            setAppBarScrolling(allow);
        }
    }

    private boolean isMultiWindow(Activity activity) {
        return WindowManagerSupport.isMultiWindowMode(activity);
    }

    @Override
    public boolean onMeasureChild(@NonNull SamsungCoordinatorLayout parent, @NonNull SamsungAppBarLayout child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        dispatchImmersiveScrollEnable();
        return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @SuppressLint("LongLogTag")
    private boolean updateOrientationState() {
        if (mAppBarLayout == null) {
            return false;
        }

        int currentOrientation = mAppBarLayout.getCurrentOrientation();
        if (mPrevOrientation != currentOrientation) {
            mPrevOrientation = currentOrientation;
            forceRestoreWindowInset(true);
            mCalledHideShowOnLayoutChlid = false;
        }
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        Log.e(TAG, "ERROR, e : AppbarLayout Configuration is wrong");
        return false;
    }

    private boolean isLandscape() {
        return mAppBarLayout != null && mAppBarLayout.getCurrentOrientation() == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isNavigationBarBottomPosition() {
        if (mDecorViewInset == null) {
            if (mDecorView == null) {
                mDecorView = mAppBarLayout.getRootView();
            }
            mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
        }

        return mDecorViewInset != null && mDecorViewInset.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom == 0;
    }

    private void setupDecorsFitSystemWindowState(boolean fitSystemWindow) {
        if (mDecorView != null && mAppBarLayout != null && !useCustomAnimationCallback) {
            if (mContext == null) {
                mContext = mAppBarLayout.getContext();
                if (mContext == null) {
                    return;
                }
            }

            Activity activity = getActivity(mContext);
            if (activity == null && mAppBarLayout != null) {
                mContext = mAppBarLayout.getContext();
                activity = getActivity(mAppBarLayout.getContext());
            }
            if (activity != null) {
                Window window = activity.getWindow();

                if (fitSystemWindow) {
                    if (mDecorViewInset.getDisplayCutout() == null) {
                        mAppBarLayout.setImmersiveTopInset(0);
                    } else {
                        mAppBarLayout.setImmersiveTopInset(mStatusBarHeight);
                    }
                    WindowCompat.setDecorFitsSystemWindows(window, false);
                    window.getDecorView().setFitsSystemWindows(false);

                    int topInset = mDecorViewInset.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    if (mDecorViewInset != null && topInset != 0 && topInset != mStatusBarHeight) {
                        mStatusBarHeight = topInset;
                        mAppBarLayout.setImmersiveTopInset(topInset);
                    }
                } else {
                    mAppBarLayout.setImmersiveTopInset(0);
                    WindowCompat.setDecorFitsSystemWindows(window, true);
                    window.getDecorView().setFitsSystemWindows(true);
                    if (!isNavigationBarBottomPosition() && isLandscape()) {
                        if (mWindowInsetsController == null) {
                            setWindowInsetsController();
                        }
                        mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
                        if (mWindowInsetsController != null && mDecorViewInset != null) {
                            if (mDecorViewInset.getInsets(WindowInsetsCompat.Type.statusBars()).top != 0) {
                                mWindowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
                            }
                        }
                    }
                }
            }
        }
    }

    void updatePunchHole(final boolean immersive) {
        if (mDecorViewInset == null) {
            if (mDecorView != null) {
                if (mContentView == null) {
                    mContentView = mDecorView.findViewById(android.R.id.content);
                }
                mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
            } else {
                return;
            }
        }

        final Insets cutoutInsets = mDecorViewInset != null ? mDecorViewInset.getInsets(WindowInsetsCompat.Type.displayCutout()) : null;
        if (cutoutInsets != null && mContentView != null) {
            mContentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    View child = ((ViewGroup) mDecorView).getChildAt(0);

                    boolean shouldSetPadding = true;
                    if (mAppBarLayout != null && mAppBarLayout.isImmersiveActivatedByUser()) {
                        shouldSetPadding = !immersive;
                    } else if (mDecorView != null && child != null) {
                        shouldSetPadding = child.getPaddingStart() != 0 || child.getPaddingEnd() != 0;
                    }

                    if (shouldSetPadding) {
                        mContentView.setPadding(cutoutInsets.left, 0, cutoutInsets.right, cutoutInsets.bottom);
                    } else {
                        mContentView.setPadding(0, 0, 0, 0);
                    }

                    return insets;
                }
            });
        }
    }

    protected void layoutChild(SamsungCoordinatorLayout parent, SamsungAppBarLayout child, int layoutDirection) {
        super.layoutChild(parent, child, layoutDirection);

        if (mWindowInsetsController != null) {
            mWindowInsetsController.addOnControllableInsetsChangedListener(new WindowInsetsControllerCompat.OnControllableInsetsChangedListener() {
                @Override
                public void onControllableInsetsChanged(@NonNull WindowInsetsControllerCompat controller, int typeMask) {
                    if (isLandscape() && !isNavigationBarBottomPosition() && !mCalledHideShowOnLayoutChlid) {
                        controller.hide(WindowInsetsCompat.Type.navigationBars());
                        controller.show(WindowInsetsCompat.Type.navigationBars());
                        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                        mCalledHideShowOnLayoutChlid = true;
                    }
                    if (typeMask == 8) {
                        mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
                        if (mDecorViewInset != null && mDecorViewInset.isVisible(WindowInsetsCompat.Type.statusBars()) && isAppBarHide()) {
                            seslRestoreTopAndBottom();
                        }
                    }
                }
            });
        }
        if (mAppBarLayout != null && child == mAppBarLayout) {
            initImmViews(parent, child);
        }
    }

    void initImmViews(SamsungCoordinatorLayout coordinatorLayout, SamsungAppBarLayout appBarLayout) {
        mAppBarLayout = appBarLayout;
        mCoordinatorLayout = coordinatorLayout;
        mAppBarLayout.addOnOffsetChangedListener(mOffsetChangedListener);

        if (!mAppBarLayout.isImmersiveActivatedByUser() && !isDexEnabled()) {
            mAppBarLayout.internalActivateImmersiveScroll(true, false);
        }

        mDecorView = mAppBarLayout.getRootView();
        mContentView = mDecorView.findViewById(android.R.id.content);
        if (useCustomAnimationCallback) {
            ViewCompat.setWindowInsetsAnimationCallback(mContentView, mCustomWindowInsetsAnimation);
        } else {
            ViewCompat.setWindowInsetsAnimationCallback(mContentView, mWindowAnimationCallback);
        }
        
        findSystemBarsBackground();
        dispatchImmersiveScrollEnable();
        
        for (int i = 0; i < appBarLayout.getChildCount(); i++) {
            View child = appBarLayout.getChildAt(i);
            
            if (mCollapsingToolbarLayout != null) {
                break;
            }
            if (child instanceof SamsungCollapsingToolbarLayout) {
                mCollapsingToolbarLayout = (SamsungCollapsingToolbarLayout) child;
            }
        }

        View bottomBarOverlay = mCoordinatorLayout.findViewById(R.id.bottom_bar_overlay);
        if (mBottomArea == null && bottomBarOverlay != null) {
            mBottomArea = bottomBarOverlay;
        }
    }

    void setWindowInsetsAnimationCallback(SamsungAppBarLayout appBarLayout, WindowInsetsAnimationCompat.Callback callback) {
        if (mContentView == null) {
            mDecorView = appBarLayout.getRootView();
            mContentView = mDecorView.findViewById(android.R.id.content);
        }

        if (callback == null) {
            useCustomAnimationCallback = false;
        } else {
            mCustomWindowInsetsAnimation = callback;
            useCustomAnimationCallback = true;
        }

        if (useCustomAnimationCallback) {
            ViewCompat.setWindowInsetsAnimationCallback(mContentView, mCustomWindowInsetsAnimation);
            prepareImmersiveScroll(false, false);
            if (mBottomArea != null) {
                mBottomArea.setTranslationY(0.0f);;
            }
        } else {
            mContentView.setPadding(0, 0, 0, 0);
            ViewCompat.setWindowInsetsAnimationCallback(mContentView, mWindowAnimationCallback);
        }
    }

    private void findSystemBarsBackground() {
        if (mDecorView != null && mContext != null) {
            mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
            mDecorView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mDecorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    mStatusBarBg = mDecorView.findViewById(android.R.id.statusBarBackground);
                    mNavigationBarBg = mDecorView.findViewById(android.R.id.navigationBarBackground);
                    return false;
                }
            });
            updateSystemBarsHeight();
        }
    }

    private void updateSystemBarsHeight() {
        if (mContext != null) {
            Resources res = mContext.getResources();

            int statusBarResId = res.getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResId > 0) {
                mStatusBarHeight = res.getDimensionPixelSize(statusBarResId);
            }

            mNavigationBarHeight = 0;

            if (mDecorView != null) {
                mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
                if (mDecorViewInset != null) {
                    mNavigationBarHeight = mDecorViewInset.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                }
            }

            if (mNavigationBarHeight == 0) {
                int navBarResId = res.getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResId > 0) {
                    mNavigationBarHeight = res.getDimensionPixelSize(navBarResId);
                }
            }
        }
    }

    private void updateAppBarHeightProportion() {
        if (mAppBarLayout != null) {
            if (mContext == null) {
                mContext = mAppBarLayout.getContext();
                if (mContext == null) {
                    return;
                }
            }

            mHeightProportion = ResourcesCompat.getFloat(mContext.getResources(), mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion);

            if (mCanImmersiveScroll) {
                mAppBarLayout.internalProportion(mHeightProportion != 0.0f ? mHeightProportion + getDifferImmHeightRatio(mContext.getResources()) : 0.0f);
            } else {
                mAppBarLayout.internalProportion(mHeightProportion);
            }
        }
    }

    private float getDifferImmHeightRatio(Resources res) {
        return ((float) mStatusBarHeight) / ((float) res.getDisplayMetrics().heightPixels);
    }

    private void setAppBarScrolling(boolean canScroll) {
        if (mAppBarLayout.getCanScroll() != canScroll) {
            mAppBarLayout.setCanScroll(canScroll);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull SamsungCoordinatorLayout parent, @NonNull SamsungAppBarLayout child, @NonNull View directTargetChild, View target, int nestedScrollAxes, int type) {
        mTargetView = target;
        if (dispatchImmersiveScrollEnable() && mAnimationController == null) {
            startAnimationControlRequest();
        }
        return super.onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onNestedScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull SamsungAppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
        mTargetView = target;
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
    }

    @Override
    public void onNestedPreScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull SamsungAppBarLayout child, View target, int dx, int dy, int[] consumed, int type) {
        mTargetView = target;
        if (mCancellationSignal != null) {
            consumed[0] = dx;
            consumed[1] = dy;
            return;
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onStopNestedScroll(SamsungCoordinatorLayout coordinatorLayout, @NonNull SamsungAppBarLayout abl, View target, int type) {
        mTargetView = target;
        super.onStopNestedScroll(coordinatorLayout, abl, target, type);
    }

    void cancelWindowInsetsAnimationController() {
        if (mDecorView != null) {
            mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
            if (mDecorViewInset != null) {
                mShownAtDown = mDecorViewInset.isVisible(WindowInsetsCompat.Type.statusBars()) || mDecorViewInset.isVisible(WindowInsetsCompat.Type.navigationBars());
            }
        }

        if (mAnimationController != null) {
            mAnimationController.finish(mShownAtDown);
        }

        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
        }
        resetWindowInsetsAnimationController();
    }

    void forceRestoreWindowInset(boolean force) {
        if (mWindowInsetsController != null) {
            mDecorViewInset = ViewCompat.getRootWindowInsets(mDecorView);
            showWindowInset(force);
        }
    }

    void showWindowInset(boolean force) {
        if (mWindowInsetsController != null && mDecorViewInset != null) {
            if (!mDecorViewInset.isVisible(WindowInsetsCompat.Type.statusBars()) || !mDecorViewInset.isVisible(WindowInsetsCompat.Type.navigationBars())) {
                if (isAppBarHide() || force) {
                    mWindowInsetsController.show(WindowInsetsCompat.Type.systemBars());
                }
            }
        }
    }

    private void finishWindowInsetsAnimationController() {
        if (mAppBarLayout != null) {
            if (mContentView == null) {
                mDecorView = mAppBarLayout.getRootView();
                mContentView = mDecorView.findViewById(android.R.id.content);
            }

            if (mAnimationController == null) {
                if (mCancellationSignal != null) {
                    mCancellationSignal.cancel();
                }
            } else {
                final int currentBottom = mAnimationController.getCurrentInsets().bottom;
                final int shownBottom = mAnimationController.getShownStateInsets().bottom;
                final int hiddenBottom = mAnimationController.getHiddenStateInsets().bottom;

                if (currentBottom == shownBottom) {
                    mAnimationController.finish(true);
                } else if (currentBottom == hiddenBottom) {
                    mAnimationController.finish(false);
                }
            }
        }
    }

    private void setWindowInsetsController() {
        if (mDecorView != null && mAnimationController == null && mWindowInsetsController == null) {
            mWindowInsetsController = ViewCompat.getWindowInsetsController(mDecorView);
        }
    }

    private void startAnimationControlRequest() {
        setWindowInsetsController();

        if (mCancellationSignal == null) {
            mCancellationSignal = new CancellationSignal();
        }
        if (mDecorViewInset.getDisplayCutout() != null) {
            mWindowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }
        mWindowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        mWindowInsetsController.controlWindowInsetsAnimation(WindowInsetsCompat.Type.systemBars(), -1, null, mCancellationSignal, mWindowInsetsAnimationControlListener);
    }

    // kang
    private void setInsetsAndAlphaToDefault() {
        int var2;
        int var4;
        label20: {
            boolean var1 = isPinEdgeEnabled(this.mContext);
            var2 = 0;
            if (var1) {
                Insets var3 = mDecorViewInset.getInsets(WindowInsetsCompat.Type.navigationBars());
                var4 = getPinnedEdgeWidth(this.mContext);
                int var5 = getEdgeArea(this.mContext);
                if (var4 == var3.left && var5 == 0) {
                    byte var8 = 0;
                    var2 = var4;
                    var4 = var8;
                    break label20;
                }

                if (var4 == var3.right && var5 == 1) {
                    break label20;
                }
            }

            var4 = 0;
        }

        float var6 = (float)this.mStatusBarHeight;
        float var7 = (float)this.mNavigationBarHeight;
        this.mAnimationController.setInsetsAndAlpha(Insets.of(var2, (int)var6, var4, (int)var7), 1.0F, 1.0F);
    }
    // kang

    private void resetWindowInsetsAnimationController() {
        mAnimationController = null;
        mCancellationSignal = null;
        mShownAtDown = false;
        mPendingRequestOnReady = null;
    }

    private boolean isMouseEvent(MotionEvent ev) {
        return ev.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull SamsungCoordinatorLayout parent, @NonNull SamsungAppBarLayout child, @NonNull MotionEvent ev) {
        final boolean isMouseEvent = isMouseEvent(ev);
        if (mToolIsMouse != isMouseEvent) {
            mToolIsMouse = isMouseEvent;
            child.seslSetIsMouse(isMouseEvent);
        }

        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    protected boolean dispatchGenericMotionEvent(MotionEvent event) {
        boolean isMouseEvent = isMouseEvent(event);
        if (mToolIsMouse != isMouseEvent) {
            mToolIsMouse = isMouseEvent;
            if (mAppBarLayout != null) {
                mAppBarLayout.seslSetIsMouse(isMouseEvent);
                dispatchImmersiveScrollEnable();
            }
        }

        return super.dispatchGenericMotionEvent(event);
    }

    boolean isAppBarHide() {
        return mAppBarLayout != null && ((float) (mAppBarLayout.getBottom() + mAppBarLayout.getPaddingBottom())) < mAppBarLayout.seslGetCollapsedHeight();
    }

    private boolean startRestoreAnimation() {
        if (!isAppBarHide()) {
            return false;
        }
        animateRestoreTopAndBottom(mCoordinatorLayout, mAppBarLayout, -mAppBarLayout.getUpNestedPreScrollRange());
        return true;
    }

    void seslRestoreTopAndBottom() {
        seslRestoreTopAndBottom(true);
    }

    @SuppressLint("LongLogTag")
    void seslRestoreTopAndBottom(boolean restore) {
        Log.i(TAG, " Restore top and bottom areas [Animate] " + restore);
        mNeedRestoreAnim = restore;
        restoreTopAndBottomInternal();
    }

    void seslSetBottomView(View view) {
        mBottomArea = view;
    }

    private void restoreTopAndBottomInternal() {
        if (mAppBarLayout != null && isAppBarHide()) {
            if (mAnimationHandler.hasMessages(MSG_APPEAR_ANIMATION)) {
                mAnimationHandler.removeMessages(MSG_APPEAR_ANIMATION);
            }
            mAnimationHandler.sendEmptyMessageDelayed(MSG_APPEAR_ANIMATION, 100);
        }
        if (mBottomArea != null && mNavigationBarBg != null && !mAnimationHandler.hasMessages(MSG_APPEAR_ANIMATION) && mAppBarLayout != null && !mAppBarLayout.isActivatedImmsersiveScroll()) {
            mBottomArea.setTranslationY(0.0f);
        }
    }

    private void animateRestoreTopAndBottom(SamsungCoordinatorLayout coordinatorLayout, SamsungAppBarLayout appBarLayout, int offset) {
        animateOffsetWithDuration(coordinatorLayout, appBarLayout, offset);
    }

    @SuppressLint("LongLogTag")
    private void animateOffsetWithDuration(final SamsungCoordinatorLayout coordinatorLayout, final SamsungAppBarLayout appBarLayout, int offset) {
        mPrevOffset = offset;

        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (mTargetView == null) {
                        Log.e(TAG, "mTargetView is null");
                        return;
                    }

                    int offset = (int) animation.getAnimatedValue();
                    mTargetView.scrollBy(0, -(mPrevOffset - offset));
                    setHeaderTopBottomOffset(coordinatorLayout, appBarLayout, offset);
                    mPrevOffset = offset;
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }

        mOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mNavigationBarBg != null) {
                    mNavigationBarBg.setTranslationY(0.0f);
                }
                if (mAnimationController != null) {
                    mAnimationController.finish(true);
                }
            }
        });

        mOffsetAnimator.setDuration(150L);
        mOffsetAnimator.setInterpolator(new PathInterpolator(0.17f, 0.17f, 0.2f, 1.0f));
        mOffsetAnimator.setStartDelay(0);

        float appBarHeightSum = ((float) (-mAppBarLayout.getHeight())) + mAppBarLayout.seslGetCollapsedHeight();
        int[] iArr = new int[2];
        iArr[0] = mNeedRestoreAnim ? -mAppBarLayout.getHeight() : (int) appBarHeightSum;
        iArr[1] = (int) appBarHeightSum;
        mOffsetAnimator.setIntValues(iArr);
        mOffsetAnimator.start();
    }

    private void forceHideRoundedCorner(int bottom) {
        if (mAnimationController != null && mDecorView != null) {
            boolean shouldHide = bottom != mAnimationController.getShownStateInsets().bottom;
            if (shouldHide != isRoundedCornerHide) {
                isRoundedCornerHide = shouldHide;
                SeslDecorViewReflector.semSetForceHideRoundedCorner(mDecorView, shouldHide);
            }
        }
    }

    /* kang from SeslContextUtils */
    public static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
    /* kang from SeslContextUtils */

    /* kang from SeslDisplayUtils */
    public static boolean isPinEdgeEnabled(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "panel_mode", 0) == 1;
        } catch (Exception e) {
            Log.w("SeslDisplayUtils", "Failed get panel mode " + e.toString());
            return false;
        }
    }

    public static int getPinnedEdgeWidth(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "pinned_edge_width");
        } catch (Settings.SettingNotFoundException e) {
            Log.w("SeslDisplayUtils", "Failed get EdgeWidth " + e.toString());
            return 0;
        }
    }

    public static int getEdgeArea(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "active_edge_area", 1);
    }
    /* kang from SeslDisplayUtils */
}
