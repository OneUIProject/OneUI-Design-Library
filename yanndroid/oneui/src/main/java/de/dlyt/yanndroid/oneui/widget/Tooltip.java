package de.dlyt.yanndroid.oneui.widget;

import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;

import android.content.Context;
import android.text.TextUtils;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.reflect.hardware.input.SeslInputManagerReflector;
import androidx.reflect.view.SeslViewReflector;

class Tooltip implements View.OnLongClickListener, View.OnHoverListener, View.OnAttachStateChangeListener {
    private static final String TAG = "Tooltip";

    private static final long LONG_CLICK_HIDE_TIMEOUT_MS = 2500;
    private static final long HOVER_HIDE_TIMEOUT_MS = 15000;
    private static final long HOVER_HIDE_TIMEOUT_SHORT_MS = 3000;

    private final View mAnchor;
    private final CharSequence mTooltipText;
    private final int mHoverSlop;

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            show(false /* not from touch*/);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private int mAnchorX;
    private int mAnchorY;

    private TooltipPopup mPopup;
    private boolean mFromTouch;

    private static Tooltip sPendingHandler;
    private static Tooltip sActiveHandler;

    // SeslTooltip
    private static boolean mIsForceActionBarX;
    private static boolean mIsForceBelow;
    private static boolean sIsCustomTooltipPosition;
    private static boolean sIsTooltipNull;
    private static int sLayoutDirection;
    private static int sPosX;
    private static int sPosY;
    private boolean mIsSPenPointChanged = false;
    private boolean mIsShowRunnablePostDelayed = false;
    // SeslTooltip

    public static void setTooltipText(View view, CharSequence tooltipText) {
        // SeslTooltip
        if (view == null) {
            Log.d(TAG, "view is null");
        } else {
        // SeslTooltip
            if (sPendingHandler != null && sPendingHandler.mAnchor == view) {
                setPendingHandler(null);
            }

            if (TextUtils.isEmpty(tooltipText)) {
                if (sActiveHandler != null && sActiveHandler.mAnchor == view) {
                    sActiveHandler.hide();
                }
                view.setOnLongClickListener(null);
                view.setLongClickable(false);
                view.setOnHoverListener(null);
            } else {
                // SeslTooltip
                if (sActiveHandler != null && sActiveHandler.mAnchor == view) {
                    sActiveHandler.hide();
                } else {
                    new Tooltip(view, tooltipText);
                }
                // SeslTooltip
            }
        }
    }

    private Tooltip(View anchor, CharSequence tooltipText) {
        mAnchor = anchor;
        mTooltipText = tooltipText;
        mHoverSlop = ViewConfigurationCompat.getScaledHoverSlop(
                ViewConfiguration.get(mAnchor.getContext()));
        clearAnchorPos();

        mAnchor.setOnLongClickListener(this);
        mAnchor.setOnHoverListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        mAnchorX = v.getWidth() / 2;
        mAnchorY = v.getHeight() / 2;
        show(true /* from touch */);
        return true;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (mPopup != null && mFromTouch) {
            return false;
        }

        // SeslTooltip
        if (mAnchor == null) {
            Log.d(TAG, "TooltipCompat Anchor view is null");
            return false;
        } else {
            if (event.isFromSource(0x4002) && !isSPenHoveringSettingsEnabled()) {
                if (mAnchor.isEnabled() && mPopup != null && v.getContext() != null) {
                    SeslViewReflector.semSetPointerIcon(v, 0x2 /* View.SEM_TOOLTIP */, PointerIcon.getSystemIcon(v.getContext(), 0x4e21 /* PointerIcon.HOVERING_SPENICON_DEFAULT */));
                }

                return false;
            } else {
        // SeslTooltip
                AccessibilityManager manager = (AccessibilityManager)
                        mAnchor.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
                if (manager.isEnabled() && manager.isTouchExplorationEnabled()) {
                    return false;
                }
                // SeslTooltip
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                        if (mAnchor.isEnabled() && mPopup == null) {
                            mAnchorX = (int) event.getX();
                            mAnchorY = (int) event.getY();
                            if (!mIsShowRunnablePostDelayed) {
                                setPendingHandler(this);
                                mIsShowRunnablePostDelayed = true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_ENTER:
                        if (mAnchor.isEnabled() && mPopup == null && v.getContext() != null) {
                            SeslViewReflector.semSetPointerIcon(v, 0x2 /* View.SEM_TOOLTIP */, PointerIcon.getSystemIcon(v.getContext(), 0x4e2a /* PointerIcon.HOVERING_SPENICON_MORE */));
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        Log.d(TAG, "MotionEvent.ACTION_HOVER_EXIT : hide TooltipPopup");
                        if (mAnchor.isEnabled() && mPopup != null && v.getContext() != null) {
                            SeslViewReflector.semSetPointerIcon(v, 0x2 /* View.SEM_TOOLTIP */, PointerIcon.getSystemIcon(v.getContext(), 0x4e21 /* PointerIcon.HOVERING_SPENICON_DEFAULT */));
                        }

                        hide();
                        break;
                }
                // SeslTooltip

                return false;
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        // no-op.
    }
    @Override
    public void onViewDetachedFromWindow(View v) {
        hide();
    }

    void show(boolean fromTouch) {
        if (!ViewCompat.isAttachedToWindow(mAnchor)) {
            return;
        }
        setPendingHandler(null);
        if (sActiveHandler != null) {
            sActiveHandler.hide();
        }
        sActiveHandler = this;

        mFromTouch = fromTouch;
        mPopup = new TooltipPopup(mAnchor.getContext());
        // SeslTooltip
        if (sIsCustomTooltipPosition) {
            mIsForceBelow = false;
            mIsForceActionBarX = false;
            if (sIsTooltipNull && !fromTouch) {
                return;
            }

            mPopup.showActionItemTooltip(sPosX, sPosY, sLayoutDirection, mTooltipText);
            sIsCustomTooltipPosition = false;
        } else {
            if (sIsTooltipNull) {
                return;
            }

            if (!mIsForceBelow && !mIsForceActionBarX) {
                mIsForceBelow = false;
                mIsForceActionBarX = false;
                mPopup.show(mAnchor, mAnchorX, mAnchorY, mFromTouch, mTooltipText);
            } else {
                mPopup.show(mAnchor, mAnchorX, mAnchorY, mFromTouch, mTooltipText, mIsForceBelow, mIsForceActionBarX);
            }
        }
        // SeslTooltip
        mAnchor.addOnAttachStateChangeListener(this);

        final long timeout;
        if (mFromTouch) {
            timeout = LONG_CLICK_HIDE_TIMEOUT_MS;
        } else if ((ViewCompat.getWindowSystemUiVisibility(mAnchor)
                & SYSTEM_UI_FLAG_LOW_PROFILE) == SYSTEM_UI_FLAG_LOW_PROFILE) {
            timeout = HOVER_HIDE_TIMEOUT_SHORT_MS - ViewConfiguration.getLongPressTimeout();
        } else {
            timeout = HOVER_HIDE_TIMEOUT_MS - ViewConfiguration.getLongPressTimeout();
        }

        mAnchor.removeCallbacks(mHideRunnable);
        mAnchor.postDelayed(mHideRunnable, timeout);
    }

    void hide() {
        if (sActiveHandler == this) {
            sActiveHandler = null;
            if (mPopup != null) {
                mPopup.hide();
                mPopup = null;
                clearAnchorPos();
                mAnchor.removeOnAttachStateChangeListener(this);
            } else {
                Log.e(TAG, "sActiveHandler.mPopup == null");
            }
        }
        // SeslTooltip
        mIsShowRunnablePostDelayed = false;
        // SeslTooltip
        if (sPendingHandler == this) {
            setPendingHandler(null);
        }
        mAnchor.removeCallbacks(this.mHideRunnable);
        // SeslTooltip
        sPosX = 0;
        sPosY = 0;
        sIsTooltipNull = false;
        sIsCustomTooltipPosition = false;
        // SeslTooltip
    }

    private static void setPendingHandler(Tooltip handler) {
        if (sPendingHandler != null) {
            sPendingHandler.cancelPendingShow();
        }
        sPendingHandler = handler;
        if (sPendingHandler != null) {
            sPendingHandler.scheduleShow();
        }
    }

    private void scheduleShow() {
        mAnchor.postDelayed(mShowRunnable, ViewConfiguration.getLongPressTimeout());
    }

    private void cancelPendingShow() {
        mAnchor.removeCallbacks(mShowRunnable);
    }

    private boolean updateAnchorPos(MotionEvent event) {
        final int newAnchorX = (int) event.getX();
        final int newAnchorY = (int) event.getY();
        if (Math.abs(newAnchorX - mAnchorX) <= mHoverSlop
                && Math.abs(newAnchorY - mAnchorY) <= mHoverSlop) {
            return false;
        }
        mAnchorX = newAnchorX;
        mAnchorY = newAnchorY;
        return true;
    }

    private void clearAnchorPos() {
        mAnchorX = Integer.MAX_VALUE;
        mAnchorY = Integer.MAX_VALUE;
    }

    // SeslTooltip
    public static void seslSetTooltipForceActionBarPosX(boolean z) {
        mIsForceActionBarX = z;
    }

    public static void seslSetTooltipForceBelow(boolean z) {
        mIsForceBelow = z;
    }

    public static void seslSetTooltipNull(boolean z) {
        sIsTooltipNull = z;
    }

    public static void seslSetTooltipPosition(int x, int y, int direction) {
        sLayoutDirection = direction;
        sPosX = x;
        sPosY = y;
        sIsCustomTooltipPosition = true;
    }

    public void showPenPointEffect(MotionEvent event, boolean more) {
        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
            if (more) {
                SeslInputManagerReflector.setPointerIconType(0x4e2a /* PointerIcon.SEM_TYPE_STYLUS_MORE */);
                mIsSPenPointChanged = true;
            } else if (mIsSPenPointChanged) {
                SeslInputManagerReflector.setPointerIconType(0x4e21 /* PointerIcon.SEM_TYPE_STYLUS_DEFAULT */);
                mIsSPenPointChanged = false;
            }
        }
    }

    public void update(CharSequence tooltipText) {
        mPopup.updateContent(tooltipText);
    }

    private boolean isSPenHoveringSettingsEnabled() {
        return System.getInt(mAnchor.getContext().getContentResolver(), "pen_hovering", 0) == 1;
    }
    // SeslTooltip
}
