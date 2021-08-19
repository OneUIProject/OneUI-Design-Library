package de.dlyt.yanndroid.oneui.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import de.dlyt.yanndroid.oneui.R;

class TooltipPopup {
    private static final String TAG = "TooltipPopup";

    private final Context mContext;
    private final View mContentView;
    private final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams();
    private final TextView mMessageView;

    private boolean mIsForceActionBarX = false;
    private boolean mIsForceBelow = false;

    private int mNavigationBarHeight = 0;
    private final int[] mTmpAnchorPos = new int[2];
    private final int[] mTmpAppPos = new int[2];
    private final Rect mTmpDisplayFrame = new Rect();

    public TooltipPopup(Context context) {
        TypedValue obtainStyledAttributes = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.popupTheme, obtainStyledAttributes, false);
        if (obtainStyledAttributes.data != 0) {
            mContext = new ContextThemeWrapper(context, obtainStyledAttributes.data);
        } else {
            mContext = context;
        }

        mContentView = LayoutInflater.from(mContext).inflate(R.layout.samsung_tooltip, null);
        mMessageView = (TextView) mContentView.findViewById(R.id.message);
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hide();
                        return true;
                    case MotionEvent.ACTION_OUTSIDE:
                        hide();
                        return false;
                    default:
                        return false;
                }
            }
        });

        mLayoutParams.setTitle(TooltipPopup.class.getSimpleName());
        mLayoutParams.packageName = mContext.getPackageName();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.format = -0x3;
        mLayoutParams.windowAnimations = R.style.TooltipAnimation;
        mLayoutParams.flags = 0x40008;
    }

    private int AdjustTooltipPosition(View var1, int var2, int var3, int var4) {
        int var5 = ((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int var6;
        if (this.checkNaviBarForLandscape()) {
            if (var5 == 1) {
                var3 = (this.mTmpDisplayFrame.width() - var3 - this.getNavigationBarHeight()) / 2 - var4;
                var6 = var2;
                if (var2 <= var3) {
                    return var6;
                }

                var2 = var3;
            } else {
                var6 = var2;
                if (var5 != 3) {
                    return var6;
                }

                if (var2 <= 0) {
                    var3 = (var3 - this.mTmpDisplayFrame.width()) / 2 + var4;
                    var6 = var2;
                    if (var2 <= var3) {
                        var6 = var3 + var4;
                    }

                    return var6;
                }

                var3 = (this.mTmpDisplayFrame.width() - var3) / 2 + var4;
                var6 = var2;
                if (var2 <= var3) {
                    return var6;
                }

                var2 = var3;
            }
        } else {
            if (var5 != 1) {
                var6 = var2;
                if (var5 != 3) {
                    return var6;
                }
            }

            if (var2 <= 0) {
                var3 = (var3 - this.mTmpDisplayFrame.width()) / 2 + var4;
                var6 = var2;
                if (var2 < var3) {
                    var6 = var3 + var4;
                }

                return var6;
            }

            var3 = (this.mTmpDisplayFrame.width() - var3) / 2 + var4;
            var6 = var2;
            if (var2 <= var3) {
                return var6;
            }

            var2 = var3;
        }

        var6 = var2 - var4;
        return var6;
    }

    private boolean checkNaviBarForLandscape() {
        Context var1 = this.mContext;
        Resources var2 = var1.getResources();
        Rect var3 = this.mTmpDisplayFrame;
        Point var4 = new Point();
        Display var9 = ((WindowManager)var1.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        var9.getRealSize(var4);
        int var5 = var9.getRotation();
        int var6 = (int)var2.getDimension(R.dimen.sesl_navigation_bar_height);
        if (var5 == 1) {
            int var7 = var3.right;
            int var8 = var4.x;
            if (var7 + var6 >= var8) {
                this.setNavigationBarHeight(var8 - var7);
                return true;
            }
        }

        if (var5 == 3) {
            var5 = var3.left;
            if (var5 <= var6) {
                this.setNavigationBarHeight(var5);
                return true;
            }
        }

        return false;
    }

    @SuppressLint("WrongConstant")
    private void computePosition(View var1, int var2, int var3, boolean var4, WindowManager.LayoutParams var5, boolean var6, boolean var7) {
        var5.token = var1.getApplicationWindowToken();
        var3 = var1.getWidth() / 2;
        var5.gravity = 49;
        View var8 = getAppRootView(var1);
        if (var8 == null) {
            Log.e(TAG, "Cannot find app view");
        } else {
            var8.getWindowVisibleDisplayFrame(this.mTmpDisplayFrame);
            Rect var9 = this.mTmpDisplayFrame;
            if (var9.left < 0 && var9.top < 0) {
                Resources var21 = this.mContext.getResources();
                var2 = var21.getIdentifier("status_bar_height", "dimen", "android");
                if (var2 != 0) {
                    var2 = var21.getDimensionPixelSize(var2);
                } else {
                    var2 = 0;
                }

                DisplayMetrics var22 = var21.getDisplayMetrics();
                this.mTmpDisplayFrame.set(0, var2, var22.widthPixels, var22.heightPixels);
            }

            int[] var23 = new int[2];
            var8.getLocationOnScreen(var23);
            Rect var10 = new Rect(var23[0], var23[1], var23[0] + var8.getWidth(), var23[1] + var8.getHeight());
            var9 = this.mTmpDisplayFrame;
            var9.left = var10.left;
            var9.right = var10.right;
            var8.getLocationOnScreen(this.mTmpAppPos);
            var1.getLocationOnScreen(this.mTmpAnchorPos);
            int[] var20 = this.mTmpAnchorPos;
            var2 = var20[0];
            var23 = this.mTmpAppPos;
            var20[0] = var2 - var23[0];
            var20[1] -= var23[1];
            var5.x = var20[0] + var3 - this.mTmpDisplayFrame.width() / 2;
            var2 = View.MeasureSpec.makeMeasureSpec(0, 0);
            this.mContentView.measure(var2, var2);
            int var11 = this.mContentView.getMeasuredHeight();
            int var12 = this.mContentView.getMeasuredWidth();
            var2 = this.mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_hover_tooltip_popup_right_margin);
            int var13 = this.mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_hover_tooltip_popup_area_margin);
            var20 = this.mTmpAnchorPos;
            int var14 = var20[1] - var11;
            int var15 = var20[1] + var1.getHeight();
            int var16;
            int var17;
            int var18;
            if (var4) {
                if (var1.getLayoutDirection() == 0) {
                    var16 = this.mTmpAnchorPos[0];
                    var17 = var1.getWidth();
                    var18 = this.mTmpDisplayFrame.width() / 2;
                    int var19 = var12 / 2;
                    var5.x = var16 + var17 - var18 - var19 - var2;
                    if (var5.x < -this.mTmpDisplayFrame.width() / 2 + var19) {
                        var5.x = -this.mTmpDisplayFrame.width() / 2 + var19 + var2;
                    }

                    var5.x = this.AdjustTooltipPosition(var1, var5.x, var12, var2);
                } else {
                    var5.x = this.mTmpAnchorPos[0] + var3 - this.mTmpDisplayFrame.width() / 2 + var12 / 2 + var2;
                    var5.x = this.AdjustTooltipPosition(var1, var5.x, var12, var2);
                }

                if (var15 + var11 > this.mTmpDisplayFrame.height()) {
                    var5.y = var14;
                } else {
                    var5.y = var15;
                }
            } else {
                var5.x = this.mTmpAnchorPos[0] + var3 - this.mTmpDisplayFrame.width() / 2;
                var16 = var5.x;
                var18 = -this.mTmpDisplayFrame.width() / 2;
                var17 = var12 / 2;
                if (var16 < var18 + var17) {
                    var5.x = -this.mTmpDisplayFrame.width() / 2 + var17 + var13;
                }

                var5.x = this.AdjustTooltipPosition(var1, var5.x, var12, var2);
                if (var14 >= 0) {
                    var5.y = var14;
                } else {
                    var5.y = var15;
                }
            }

            if (var6) {
                var5.y = this.mTmpAnchorPos[1] + var1.getHeight();
            }

            if (var7) {
                if (var1.getLayoutDirection() == 0) {
                    var17 = this.mTmpAnchorPos[0];
                    var3 = var1.getWidth();
                    var18 = this.mTmpDisplayFrame.width() / 2;
                    var16 = var12 / 2;
                    var5.x = var17 + var3 - var18 - var16 - var2;
                    if (var5.x < -this.mTmpDisplayFrame.width() / 2 + var16) {
                        var5.x = -this.mTmpDisplayFrame.width() / 2 + var16 + var13;
                    }

                    var5.x = this.AdjustTooltipPosition(var1, var5.x, var12, var2);
                } else {
                    var5.x = this.mTmpAnchorPos[0] + var3 - this.mTmpDisplayFrame.width() / 2 + var12 / 2 - var2;
                    var5.x = this.AdjustTooltipPosition(var1, var5.x, var12, var2);
                }

                if (var11 + var15 > this.mTmpDisplayFrame.height()) {
                    var5.y = var14;
                } else {
                    var5.y = var15;
                }
            }

        }
    }

    public static View getAppRootView(View var0) {
        View var1 = var0.getRootView();
        android.view.ViewGroup.LayoutParams var2 = var1.getLayoutParams();
        if (var2 instanceof WindowManager.LayoutParams && ((WindowManager.LayoutParams)var2).type == 2) {
            return var1;
        } else {
            for(Context var3 = var0.getContext(); var3 instanceof ContextWrapper; var3 = ((ContextWrapper)var3).getBaseContext()) {
                if (var3 instanceof Activity) {
                    return ((Activity)var3).getWindow().getDecorView();
                }
            }

            return var1;
        }
    }

    private int getNavigationBarHeight() {
        return this.mNavigationBarHeight;
    }

    private void setNavigationBarHeight(int var1) {
        this.mNavigationBarHeight = var1;
    }

    public void hide() {
        this.mIsForceBelow = false;
        this.mIsForceActionBarX = false;
        if (this.isShowing()) {
            ((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE)).removeView(this.mContentView);
        }
    }

    public boolean isShowing() {
        boolean var1;
        if (this.mContentView.getParent() != null) {
            var1 = true;
        } else {
            var1 = false;
        }

        return var1;
    }

    public void show(View var1, int var2, int var3, boolean var4, CharSequence var5) {
        if (this.isShowing()) {
            this.hide();
        }

        this.mMessageView.setText(var5);
        this.computePosition(var1, var2, var3, var4, this.mLayoutParams, false, false);
        ((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE)).addView(this.mContentView, this.mLayoutParams);
    }

    public void show(View var1, int var2, int var3, boolean var4, CharSequence var5, boolean var6, boolean var7) {
        this.mIsForceBelow = var6;
        this.mIsForceActionBarX = var7;
        if (this.isShowing()) {
            this.hide();
        }

        this.mMessageView.setText(var5);
        this.computePosition(var1, var2, var3, var4, this.mLayoutParams, this.mIsForceBelow, this.mIsForceActionBarX);
        ((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE)).addView(this.mContentView, this.mLayoutParams);
    }

    public void showActionItemTooltip(int var1, int var2, int var3, CharSequence var4) {
        if (this.isShowing()) {
            this.hide();
        }

        this.mMessageView.setText(var4);
        WindowManager.LayoutParams var5 = this.mLayoutParams;
        var5.x = var1;
        var5.y = var2;
        if (var3 == 0) {
            var5.gravity = 8388661;
        } else {
            var5.gravity = 8388659;
        }

        ((WindowManager)this.mContext.getSystemService(Context.WINDOW_SERVICE)).addView(this.mContentView, this.mLayoutParams);
    }

    public void updateContent(CharSequence var1) {
        this.mMessageView.setText(var1);
    }
}
