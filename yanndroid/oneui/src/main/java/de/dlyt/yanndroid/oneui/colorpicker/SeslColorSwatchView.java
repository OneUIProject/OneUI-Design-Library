package de.dlyt.yanndroid.oneui.colorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;

import de.dlyt.yanndroid.oneui.R;

class SeslColorSwatchView extends View {
    private static final int MAX_SWATCH_VIEW_ID = 110;
    private static final int SWATCH_ITEM_COLUMN = 11;
    private static final int SWATCH_ITEM_ROW = 10;
    private static final float SWATCH_ITEM_SIZE_ROUNDING_VALUE = 0.5F;
    private static String TAG;
    private int[][] mColorBrightness;
    private int[][] mColorSwatch;
    private StringBuilder[][] mColorSwatchDescription;
    private Context mContext;
    private GradientDrawable mCursorDrawable;
    private Point mCursorIndex;
    private Rect mCursorRect;
    private boolean mFromUser;
    private boolean mIsColorInSwatch;
    private SeslColorSwatchView.OnColorSwatchChangedListener mListener;
    private Resources mResources;
    private int mSelectedVirtualViewId;
    private float mSwatchItemHeight;
    private float mSwatchItemWidth;
    private SeslColorSwatchView.SeslColorSwatchViewTouchHelper mTouchHelper;

    public SeslColorSwatchView(Context var1) {
        this(var1, (AttributeSet) null);
    }

    public SeslColorSwatchView(Context var1, AttributeSet var2) {
        this(var1, var2, 0);
    }

    public SeslColorSwatchView(Context var1, AttributeSet var2, int var3) {
        this(var1, var2, var3, 0);
    }

    public SeslColorSwatchView(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
        this.mSelectedVirtualViewId = -1;
        this.mFromUser = false;
        this.mIsColorInSwatch = true;
        int[] var12 = new int[]{-22360, -38037, -49859, -60396, -65536, -393216, -2424832, -5767168, -10747904, -13434880};
        int[] var5 = new int[]{-5701685, -10027101, -13041784, -15728785, -16711834, -16714398, -16721064, -16735423, -16753627, -16764140};
        int[] var6 = new int[]{-5712641, -9718273, -13067009, -15430913, -16744193, -16744966, -16748837, -16755544, -16764575, -16770509};
        int[] var7 = new int[]{-3430145, -5870593, -7849729, -9498625, -10092289, -10223366, -11009829, -12386136, -14352292, -15466445};
        this.mColorSwatch = new int[][]{{-1, -3355444, -5000269, -6710887, -8224126, -10066330, -11711155, -13421773, -15066598, -16777216}, var12, {-11096, -19093, -25544, -30705, -32768, -361216, -2396672, -5745664, -10736128, -13428224}, {-88, -154, -200, -256, -256, -329216, -2368768, -6053120, -10724352, -13421824}, {-5701720, -10027162, -13041864, -16056566, -16711936, -16713216, -16721152, -16735488, -16753664, -16764160}, var5, {-5701633, -10027009, -12713985, -16056321, -16711681, -16714251, -16720933, -16735325, -16753572, -16764109}, var6, {-5723905, -9737217, -13092609, -16119041, -16776961, -16776966, -16776997, -16777048, -16777119, -16777165}, var7, {-22273, -39169, -50945, -61441, -65281, -392966, -2424613, -5767000, -10420127, -13434829}};
        var12 = new int[]{100, 80, 70, 60, 51, 40, 30, 20, 10, 0};
        var5 = new int[]{83, 71, 62, 54, 50, 49, 43, 33, 18, 10};
        var6 = new int[]{83, 70, 61, 52, 50, 49, 43, 32, 18, 10};
        var7 = new int[]{83, 70, 61, 53, 50, 48, 43, 32, 18, 10};
        int[] var8 = new int[]{83, 70, 62, 52, 50, 48, 43, 32, 18, 10};
        int[] var9 = new int[]{83, 71, 61, 54, 50, 49, 43, 33, 19, 10};
        int[] var10 = new int[]{83, 71, 61, 53, 50, 49, 43, 33, 18, 10};
        int[] var11 = new int[]{83, 70, 61, 53, 50, 49, 43, 33, 19, 10};
        this.mColorBrightness = new int[][]{var12, var5, {83, 71, 61, 53, 50, 49, 43, 33, 18, 10}, {83, 70, 61, 50, 50, 49, 43, 32, 18, 10}, var6, var7, var8, var9, {83, 71, 61, 52, 50, 49, 43, 33, 19, 10}, var10, var11};
        this.mColorSwatchDescription = new StringBuilder[11][10];
        this.mContext = var1;
        this.mResources = this.mContext.getResources();
        this.initCursorDrawable();
        this.initAccessibility();
        this.mSwatchItemHeight = this.mResources.getDimension(R.dimen.sesl_color_picker_color_swatch_view_height) / 10.0F;
        this.mSwatchItemWidth = this.mResources.getDimension(R.dimen.sesl_color_picker_color_swatch_view_width) / 11.0F;
        this.mCursorIndex = new Point(-1, -1);
    }

    private void initAccessibility() {
        this.mTouchHelper = new SeslColorSwatchView.SeslColorSwatchViewTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, this.mTouchHelper);
        this.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private void initCursorDrawable() {
        this.mCursorDrawable = (GradientDrawable) this.mResources.getDrawable(R.drawable.sesl_color_swatch_view_cursor, null);
        this.mCursorRect = new Rect();
    }

    private void setCursorIndexAt(int var1) {
        Point var2 = this.getCursorIndexAt(var1);
        if (this.mFromUser) {
            this.mCursorIndex.set(var2.x, var2.y);
        }

    }

    private boolean setCursorIndexAt(float var1, float var2) {
        float var3 = this.mSwatchItemWidth * 11.0F;
        float var4 = this.mSwatchItemHeight * 10.0F;
        if (var1 >= var3) {
            --var3;
        } else {
            var3 = var1;
            if (var1 < 0.0F) {
                var3 = 0.0F;
            }
        }

        if (var2 >= var4) {
            var1 = var4 - 1.0F;
        } else {
            var1 = var2;
            if (var2 < 0.0F) {
                var1 = 0.0F;
            }
        }

        Point var5 = new Point(this.mCursorIndex.x, this.mCursorIndex.y);
        this.mCursorIndex.set((int) (var3 / this.mSwatchItemWidth), (int) (var1 / this.mSwatchItemHeight));
        return var5.equals(this.mCursorIndex) ^ true;
    }

    private void setCursorRect(Rect var1) {
        var1.set((int) ((float) this.mCursorIndex.x * this.mSwatchItemWidth + 0.5F), (int) ((float) this.mCursorIndex.y * this.mSwatchItemHeight + 0.5F), (int) ((float) (this.mCursorIndex.x + 1) * this.mSwatchItemWidth + 0.5F), (int) ((float) (this.mCursorIndex.y + 1) * this.mSwatchItemHeight + 0.5F));
    }

    private void setSelectedVirtualViewId() {
        this.mSelectedVirtualViewId = this.mCursorIndex.y * 11 + this.mCursorIndex.x;
    }

    protected boolean dispatchHoverEvent(MotionEvent var1) {
        boolean var2;
        if (!this.mTouchHelper.dispatchHoverEvent(var1) && !super.dispatchHoverEvent(var1)) {
            var2 = false;
        } else {
            var2 = true;
        }

        return var2;
    }

    StringBuilder getColorSwatchDescriptionAt(int var1) {
        Point var2 = this.getCursorIndexAt(var1);
        if (this.mFromUser) {
            return this.mColorSwatchDescription[var2.x][var2.y] == null ? this.mTouchHelper.getItemDescription(var2.x + var2.y * 11) : this.mColorSwatchDescription[var2.x][var2.y];
        } else {
            return null;
        }
    }

    Point getCursorIndexAt(int var1) {
        int var2 = Color.argb(255, var1 >> 16 & 255, var1 >> 8 & 255, var1 & 255);
        Point var3 = new Point(-1, -1);
        this.mFromUser = false;

        for (var1 = 0; var1 < 11; ++var1) {
            for (int var4 = 0; var4 < 10; ++var4) {
                if (this.mColorSwatch[var1][var4] == var2) {
                    var3.set(var1, var4);
                    this.mFromUser = true;
                }
            }
        }

        this.mIsColorInSwatch = true;
        if (!this.mFromUser && !this.mCursorIndex.equals(-1, -1)) {
            this.mIsColorInSwatch = false;
            this.invalidate();
        }

        return var3;
    }

    protected void onDraw(Canvas var1) {
        Paint var2 = new Paint();

        for (int var3 = 0; var3 < 11; ++var3) {
            int var4 = 0;

            while (var4 < 10) {
                var2.setColor(this.mColorSwatch[var3][var4]);
                float var5 = this.mSwatchItemWidth;
                float var6 = (float) ((int) ((float) var3 * var5 + 0.5F));
                float var7 = this.mSwatchItemHeight;
                float var8 = (float) ((int) ((float) var4 * var7 + 0.5F));
                var5 = (float) ((int) (var5 * (float) (var3 + 1) + 0.5F));
                ++var4;
                var1.drawRect(var6, var8, var5, (float) ((int) (var7 * (float) var4 + 0.5F)), var2);
            }
        }

        if (this.mIsColorInSwatch) {
            this.mCursorDrawable.setBounds(this.mCursorRect);
            this.mCursorDrawable.draw(var1);
        }

    }

    public boolean onTouchEvent(MotionEvent var1) {
        int var2 = var1.getAction();
        if (var2 != 0 && var2 != 1 && var2 == 2 && this.getParent() != null) {
            this.getParent().requestDisallowInterceptTouchEvent(true);
        }

        if (this.setCursorIndexAt(var1.getX(), var1.getY()) || !this.mIsColorInSwatch) {
            this.setCursorRect(this.mCursorRect);
            this.setSelectedVirtualViewId();
            this.invalidate();
            SeslColorSwatchView.OnColorSwatchChangedListener var3 = this.mListener;
            if (var3 != null) {
                var3.onColorSwatchChanged(this.mColorSwatch[this.mCursorIndex.x][this.mCursorIndex.y]);
            }
        }

        return true;
    }

    void setOnColorSwatchChangedListener(SeslColorSwatchView.OnColorSwatchChangedListener var1) {
        this.mListener = var1;
    }

    public void updateCursorPosition(int var1) {
        this.setCursorIndexAt(var1);
        if (this.mFromUser) {
            this.setCursorRect(this.mCursorRect);
            this.invalidate();
            this.setSelectedVirtualViewId();
        } else {
            this.mSelectedVirtualViewId = -1;
        }

    }

    interface OnColorSwatchChangedListener {
        void onColorSwatchChanged(int var1);
    }

    private class SeslColorSwatchViewTouchHelper extends ExploreByTouchHelper {
        private final Rect mVirtualViewRect;
        private String[][] mColorDescription;
        private int mVirtualCursorIndexX;
        private int mVirtualCursorIndexY;

        public SeslColorSwatchViewTouchHelper(@NonNull View var2) {
            super(var2);
            String var3 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_white);
            String var4 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_light_gray);
            String var5 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_gray);
            String var6 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_dark_gray);
            String var7 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_black);
            String var8 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_red);
            String var9 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_red);
            String var10 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_red);
            String var11 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_orange);
            String var12 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_orange);
            String var13 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_orange);
            String var14 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_light_yellow);
            String var15 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_yellow);
            String var16 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_yellow);
            String[] var17 = new String[]{SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_light_green), SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_green), SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_dark_green)};
            String var18 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_spring_green);
            String var19 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_spring_green);
            String var20 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_spring_green);
            String var35 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_cyan);
            String var21 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_cyan);
            String var22 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_cyan);
            String var23 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_azure);
            String var24 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_azure);
            String var25 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_azure);
            String var26 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_blue);
            String var27 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_blue);
            String var28 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_blue);
            String var29 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_violet);
            String var30 = SeslColorSwatchView.this.mResources.getString(R.string.pen_palette_color_violet);
            String var31 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_violet);
            String var34 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_light_magenta);
            String var32 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_magenta);
            String var33 = SeslColorSwatchView.this.mResources.getString(R.string.pen_swatch_color_dark_magenta);
            this.mColorDescription = new String[][]{{var3, var4, var5, var6, var7}, {var8, var9, var10}, {var11, var12, var13}, {var14, var15, var16}, var17, {var18, var19, var20}, {var35, var21, var22}, {var23, var24, var25}, {var26, var27, var28}, {var29, var30, var31}, {var34, var32, var33}};
            this.mVirtualViewRect = new Rect();
        }

        private int getFocusedVirtualViewId() {
            return this.mVirtualCursorIndexX + this.mVirtualCursorIndexY * 11;
        }

        private StringBuilder getItemDescription(int var1) {
            this.setVirtualCursorIndexAt(var1);
            if (SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY] == null) {
                StringBuilder var2 = new StringBuilder();
                var1 = this.mVirtualCursorIndexX;
                int var3;
                if (var1 == 0) {
                    var3 = this.mVirtualCursorIndexY;
                    if (var3 == 0) {
                        var2.append(this.mColorDescription[var1][0]);
                    } else if (var3 < 3) {
                        var2.append(this.mColorDescription[var1][1]);
                    } else if (var3 < 6) {
                        var2.append(this.mColorDescription[var1][2]);
                    } else if (var3 < 9) {
                        var2.append(this.mColorDescription[var1][3]);
                    } else {
                        var2.append(this.mColorDescription[var1][4]);
                    }
                } else {
                    var3 = this.mVirtualCursorIndexY;
                    if (var3 < 3) {
                        var2.append(this.mColorDescription[var1][0]);
                    } else if (var3 < 6) {
                        var2.append(this.mColorDescription[var1][1]);
                    } else {
                        var2.append(this.mColorDescription[var1][2]);
                    }
                }

                var2.append(", ");
                var2.append(SeslColorSwatchView.this.mColorBrightness[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY]);
                SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY] = var2;
            }

            return SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY];
        }

        private void onVirtualViewClick(int var1) {
            if (SeslColorSwatchView.this.mListener != null) {
                SeslColorSwatchView.this.mListener.onColorSwatchChanged(var1);
            }

            SeslColorSwatchView.this.mTouchHelper.sendEventForVirtualView(SeslColorSwatchView.this.mSelectedVirtualViewId, 1);
        }

        private void setVirtualCursorIndexAt(float var1, float var2) {
            float var3 = SeslColorSwatchView.this.mSwatchItemWidth * 11.0F;
            float var4 = SeslColorSwatchView.this.mSwatchItemHeight * 10.0F;
            if (var1 >= var3) {
                --var3;
            } else {
                var3 = var1;
                if (var1 < 0.0F) {
                    var3 = 0.0F;
                }
            }

            if (var2 >= var4) {
                var1 = var4 - 1.0F;
            } else {
                var1 = var2;
                if (var2 < 0.0F) {
                    var1 = 0.0F;
                }
            }

            this.mVirtualCursorIndexX = (int) (var3 / SeslColorSwatchView.this.mSwatchItemWidth);
            this.mVirtualCursorIndexY = (int) (var1 / SeslColorSwatchView.this.mSwatchItemHeight);
        }

        private void setVirtualCursorIndexAt(int var1) {
            this.mVirtualCursorIndexX = var1 % 11;
            this.mVirtualCursorIndexY = var1 / 11;
        }

        private void setVirtualCursorRect(Rect var1) {
            var1.set((int) ((float) this.mVirtualCursorIndexX * SeslColorSwatchView.this.mSwatchItemWidth + 0.5F), (int) ((float) this.mVirtualCursorIndexY * SeslColorSwatchView.this.mSwatchItemHeight + 0.5F), (int) ((float) (this.mVirtualCursorIndexX + 1) * SeslColorSwatchView.this.mSwatchItemWidth + 0.5F), (int) ((float) (this.mVirtualCursorIndexY + 1) * SeslColorSwatchView.this.mSwatchItemHeight + 0.5F));
        }

        protected int getVirtualViewAt(float var1, float var2) {
            this.setVirtualCursorIndexAt(var1, var2);
            return this.getFocusedVirtualViewId();
        }

        protected void getVisibleVirtualViews(List<Integer> var1) {
            for (int var2 = 0; var2 < 110; ++var2) {
                var1.add(var2);
            }

        }

        protected boolean onPerformActionForVirtualView(int var1, int var2, @Nullable Bundle var3) {
            if (var2 == 16) {
                this.setVirtualCursorIndexAt(var1);
                this.onVirtualViewClick(SeslColorSwatchView.this.mColorSwatch[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY]);
            }

            return false;
        }

        protected void onPopulateEventForVirtualView(int var1, @NonNull AccessibilityEvent var2) {
            var2.setContentDescription(this.getItemDescription(var1));
        }

        protected void onPopulateNodeForVirtualView(int var1, @NonNull AccessibilityNodeInfoCompat var2) {
            this.setVirtualCursorIndexAt(var1);
            this.setVirtualCursorRect(this.mVirtualViewRect);
            var2.setContentDescription(this.getItemDescription(var1));
            var2.setBoundsInParent(this.mVirtualViewRect);
            var2.addAction(16);
            var2.setClassName(Button.class.getName());
            if (SeslColorSwatchView.this.mSelectedVirtualViewId != -1 && var1 == SeslColorSwatchView.this.mSelectedVirtualViewId) {
                var2.addAction(4);
                var2.setClickable(true);
                var2.setCheckable(true);
                var2.setChecked(true);
            }

        }
    }
}
