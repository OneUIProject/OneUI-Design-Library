package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.lang.reflect.Array;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;

class SeslColorSwatchView extends View {
    private static final int MAX_SWATCH_VIEW_ID = 110;
    private static final int ROUNDED_CORNER_RADIUS = 4;
    private static final float STROKE_WIDTH = 0.25f;
    private static final int SWATCH_ITEM_COLUMN = 11;
    private static final int SWATCH_ITEM_ROW = 10;
    private static final float SWATCH_ITEM_SIZE_ROUNDING_VALUE = 4.5f;
    private static String TAG = "SeslColorSwatchView";
    private int ROUNDED_CORNER_RADIUS_IN_Px;
    private float[] corners;
    private int currentCursorColor;
    Paint mBackgroundPaint;
    private int[][] mColorBrightness;
    private int[][] mColorSwatch;
    private StringBuilder[][] mColorSwatchDescription;
    private GradientDrawable mCursorDrawable;
    private Point mCursorIndex;
    private Rect mCursorRect;
    private boolean mFromUser;
    private boolean mIsColorInSwatch;
    private OnColorSwatchChangedListener mListener;
    private Resources mResources;
    private int mSelectedVirtualViewId;
    private Rect mShadowRect;
    Paint mStrokePaint;
    private float mSwatchItemHeight;
    private float mSwatchItemWidth;
    private RectF mSwatchRect;
    private RectF mSwatchRectBackground;
    private SeslColorSwatchViewTouchHelper mTouchHelper;
    Paint shadow;

    interface OnColorSwatchChangedListener {
        void onColorSwatchChanged(int i);
    }

    void setOnColorSwatchChangedListener(OnColorSwatchChangedListener onColorSwatchChangedListener) {
        mListener = onColorSwatchChangedListener;
    }

    public SeslColorSwatchView(Context context) {
        this(context, null);
    }

    public SeslColorSwatchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeslColorSwatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeslColorSwatchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mSelectedVirtualViewId = -1;
        mFromUser = false;
        mIsColorInSwatch = true;

        mColorSwatch = new int[][]{new int[]{-1, -3355444, -5000269, -6710887, -8224126, -10066330, -11711155, -13421773, -15066598, -16777216}, new int[]{-22360, -38037, -49859, -60396, -65536, -393216, -2424832, -5767168, -10747904, -13434880}, new int[]{-11096, -19093, -25544, -30705, -32768, -361216, -2396672, -5745664, -10736128, -13428224}, new int[]{-88, -154, -200, -256, -328704, -329216, -2368768, -6053120, -10724352, -13421824}, new int[]{-5701720, -10027162, -13041864, -16056566, -16711936, -16713216, -16721152, -16735488, -16753664, -16764160}, new int[]{-5701685, -10027101, -13041784, -15728785, -16711834, -16714398, -16721064, -16735423, -16753627, -16764140}, new int[]{-5701633, -10027009, -12713985, -16056321, -16711681, -16714251, -16720933, -16735325, -16753572, -16764109}, new int[]{-5712641, -9718273, -13067009, -15430913, -16744193, -16744966, -16748837, -16755544, -16764575, -16770509}, new int[]{-5723905, -9737217, -13092609, -16119041, -16776961, -16776966, -16776997, -16777048, -16777119, -16777165}, new int[]{-3430145, -5870593, -7849729, -9498625, -10092289, -10223366, -11009829, -12386136, -14352292, -15466445}, new int[]{-22273, -39169, -50945, -61441, -65281, -392966, -2424613, -5767000, -10420127, -13434829}};
        mColorBrightness = new int[][]{new int[]{100, 80, 70, 60, 51, 40, 30, 20, 10, 0}, new int[]{83, 71, 62, 54, 50, 49, 43, 33, 18, 10}, new int[]{83, 71, 61, 53, 50, 49, 43, 33, 18, 10}, new int[]{83, 70, 61, 50, 51, 49, 43, 32, 18, 10}, new int[]{83, 70, 61, 52, 50, 49, 43, 32, 18, 10}, new int[]{83, 70, 61, 53, 50, 48, 43, 32, 18, 10}, new int[]{83, 70, 62, 52, 50, 48, 43, 32, 18, 10}, new int[]{83, 71, 61, 54, 50, 49, 43, 33, 19, 10}, new int[]{83, 71, 61, 52, 50, 49, 43, 33, 19, 10}, new int[]{83, 71, 61, 53, 50, 49, 43, 33, 18, 10}, new int[]{83, 70, 61, 53, 50, 49, 43, 33, 19, 10}};
        mColorSwatchDescription = (StringBuilder[][]) Array.newInstance(StringBuilder.class, 11, 10);

        mResources = context.getResources();

        initCursorDrawable();
        initAccessibility();

        mSwatchItemHeight = mResources.getDimension(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_height) / 10.0f;
        mSwatchItemWidth = mResources.getDimension(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_width) / 11.0f;
        mSwatchRect = new RectF(SWATCH_ITEM_SIZE_ROUNDING_VALUE, SWATCH_ITEM_SIZE_ROUNDING_VALUE, ((float) mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_width)) + SWATCH_ITEM_SIZE_ROUNDING_VALUE, ((float) mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_height)) + SWATCH_ITEM_SIZE_ROUNDING_VALUE);
        mSwatchRectBackground = new RectF(0.0f, 0.0f, (float) mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_width_background), (float) mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_color_swatch_view_height_background));

        mCursorIndex = new Point(-1, -1);

        ROUNDED_CORNER_RADIUS_IN_Px = dpToPx(ROUNDED_CORNER_RADIUS);

        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(mResources.getColor(R.color.sesl_color_picker_stroke_color_swatchview));
        mStrokePaint.setStrokeWidth(STROKE_WIDTH);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mResources.getColor(R.color.sesl_color_picker_transparent));
    }

    private void initAccessibility() {
        mTouchHelper = new SeslColorSwatchViewTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        setImportantForAccessibility(1);
    }

    private void initCursorDrawable() {
        mCursorDrawable = (GradientDrawable) mResources.getDrawable(R.drawable.sesl_color_swatch_view_cursor);
        mCursorRect = new Rect();
        mShadowRect = new Rect();
        shadow = new Paint();
        shadow.setStyle(Paint.Style.FILL);
        shadow.setColor(this.mResources.getColor(R.color.sesl_color_picker_shadow));
        shadow.setMaskFilter(new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        canvas.drawRoundRect(mSwatchRectBackground, (float) ROUNDED_CORNER_RADIUS_IN_Px, (float) ROUNDED_CORNER_RADIUS_IN_Px, mBackgroundPaint);
        int i2 = 0;
        while (true) {
            int i3 = 8;
            if (i2 >= 11) {
                break;
            }
            int i4 = 0;
            while (i4 < 10) {
                paint.setColor(this.mColorSwatch[i2][i4]);
                if (i2 == 0 && i4 == 0) {
                    float[] fArr = new float[i3];
                    int i5 = ROUNDED_CORNER_RADIUS_IN_Px;
                    fArr[0] = (float) i5;
                    fArr[1] = (float) i5;
                    fArr[2] = 0.0f;
                    fArr[3] = 0.0f;
                    fArr[4] = 0.0f;
                    fArr[5] = 0.0f;
                    fArr[6] = 0.0f;
                    fArr[7] = 0.0f;
                    this.corners = fArr;
                    Path path = new Path();
                    path.addRoundRect((float) ((int) ((((float) i2) * mSwatchItemWidth) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((((float) i4) * mSwatchItemHeight) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((mSwatchItemWidth * ((float) (i2 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((mSwatchItemHeight * ((float) (i4 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), corners, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else if (i2 == 0 && i4 == 9) {
                    int i6 = this.ROUNDED_CORNER_RADIUS_IN_Px;
                    this.corners = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, (float) i6, (float) i6};
                    Path path2 = new Path();
                    float f3 = this.mSwatchItemWidth;
                    float f4 = this.mSwatchItemHeight;
                    path2.addRoundRect((float) ((int) ((((float) i2) * f3) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((((float) i4) * f4) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f3 * ((float) (i2 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f4 * ((float) (i4 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), this.corners, Path.Direction.CW);
                    canvas.drawPath(path2, paint);
                } else if (i2 == 10 && i4 == 0) {
                    int i7 = this.ROUNDED_CORNER_RADIUS_IN_Px;
                    this.corners = new float[]{0.0f, 0.0f, (float) i7, (float) i7, 0.0f, 0.0f, 0.0f, 0.0f};
                    Path path3 = new Path();
                    float f5 = this.mSwatchItemWidth;
                    float f6 = this.mSwatchItemHeight;
                    path3.addRoundRect((float) ((int) ((((float) i2) * f5) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((((float) i4) * f6) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f5 * ((float) (i2 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f6 * ((float) (i4 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), this.corners, Path.Direction.CW);
                    canvas.drawPath(path3, paint);
                } else if (i2 == 10 && i4 == 9) {
                    int i8 = this.ROUNDED_CORNER_RADIUS_IN_Px;
                    this.corners = new float[]{0.0f, 0.0f, 0.0f, 0.0f, (float) i8, (float) i8, 0.0f, 0.0f};
                    Path path4 = new Path();
                    float f7 = this.mSwatchItemWidth;
                    float f8 = this.mSwatchItemHeight;
                    path4.addRoundRect((float) ((int) ((((float) i2) * f7) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((((float) i4) * f8) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f7 * ((float) (i2 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f8 * ((float) (i4 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), this.corners, Path.Direction.CW);
                    canvas.drawPath(path4, paint);
                } else {
                    float f9 = this.mSwatchItemWidth;
                    float f10 = this.mSwatchItemHeight;
                    canvas.drawRect((float) ((int) ((((float) i2) * f9) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((((float) i4) * f10) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f9 * ((float) (i2 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), (float) ((int) ((f10 * ((float) (i4 + 1))) + SWATCH_ITEM_SIZE_ROUNDING_VALUE)), paint);
                }
                i4++;
                i3 = 8;
            }
            i2++;
        }
        RectF rectF2 = this.mSwatchRect;
        int i9 = this.ROUNDED_CORNER_RADIUS_IN_Px;
        canvas.drawRoundRect(rectF2, (float) i9, (float) i9, this.mStrokePaint);
        if (this.mIsColorInSwatch) {
            canvas.drawRect(this.mShadowRect, this.shadow);
            if (this.mCursorIndex.y == 8 || this.mCursorIndex.y == 9) {
                this.mCursorDrawable = (GradientDrawable) this.mResources.getDrawable(R.drawable.sesl_color_swatch_view_cursor);
            } else {
                this.mCursorDrawable = (GradientDrawable) this.mResources.getDrawable(R.drawable.sesl_color_swatch_view_cursor_gray);
            }
            this.mCursorDrawable.setColor(this.currentCursorColor);
            this.mCursorDrawable.setBounds(this.mCursorRect);
            this.mCursorDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 2 && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        if (setCursorIndexAt(motionEvent.getX(), motionEvent.getY()) || !this.mIsColorInSwatch) {
            int i = this.mColorSwatch[this.mCursorIndex.x][this.mCursorIndex.y];
            this.currentCursorColor = i;
            this.currentCursorColor = ColorUtils.setAlphaComponent(i, 255);
            setShadowRect(this.mShadowRect);
            setCursorRect(this.mCursorRect);
            setSelectedVirtualViewId();
            invalidate();
            OnColorSwatchChangedListener onColorSwatchChangedListener = this.mListener;
            if (onColorSwatchChangedListener != null) {
                onColorSwatchChangedListener.onColorSwatchChanged(this.mColorSwatch[this.mCursorIndex.x][this.mCursorIndex.y]);
            }
        }
        return true;
    }

    Point getCursorIndexAt(int i) {
        int argb = Color.argb(255, (i >> 16) & 255, (i >> 8) & 255, i & 255);
        Point point = new Point(-1, -1);
        this.mFromUser = false;
        for (int i2 = 0; i2 < 11; i2++) {
            for (int i3 = 0; i3 < 10; i3++) {
                if (this.mColorSwatch[i2][i3] == argb) {
                    point.set(i2, i3);
                    this.mFromUser = true;
                }
            }
        }
        this.mIsColorInSwatch = true;
        if (!this.mFromUser && !this.mCursorIndex.equals(-1, -1)) {
            this.mIsColorInSwatch = false;
            invalidate();
        }
        return point;
    }

    StringBuilder getColorSwatchDescriptionAt(int i) {
        Point cursorIndexAt = getCursorIndexAt(i);
        if (!this.mFromUser) {
            return null;
        }
        if (this.mColorSwatchDescription[cursorIndexAt.x][cursorIndexAt.y] == null) {
            return this.mTouchHelper.getItemDescription(cursorIndexAt.x + (cursorIndexAt.y * 11));
        }
        return this.mColorSwatchDescription[cursorIndexAt.x][cursorIndexAt.y];
    }

    private boolean setCursorIndexAt(float f, float f2) {
        float f3 = this.mSwatchItemWidth * 11.0f;
        float f4 = this.mSwatchItemHeight * 10.0f;
        if (f >= f3) {
            f = f3 - 1.0f;
        } else if (f < 0.0f) {
            f = 0.0f;
        }
        if (f2 >= f4) {
            f2 = f4 - 1.0f;
        } else if (f2 < 0.0f) {
            f2 = 0.0f;
        }
        Point point = new Point(this.mCursorIndex.x, this.mCursorIndex.y);
        this.mCursorIndex.set((int) (f / this.mSwatchItemWidth), (int) (f2 / this.mSwatchItemHeight));
        return !point.equals(this.mCursorIndex);
    }

    private void setCursorIndexAt(int i) {
        Point cursorIndexAt = getCursorIndexAt(i);
        if (this.mFromUser) {
            this.mCursorIndex.set(cursorIndexAt.x, cursorIndexAt.y);
        }
    }

    private void setSelectedVirtualViewId() {
        this.mSelectedVirtualViewId = (this.mCursorIndex.y * 11) + this.mCursorIndex.x;
    }

    public void updateCursorPosition(int i) {
        setCursorIndexAt(i);
        if (this.mFromUser) {
            this.currentCursorColor = ColorUtils.setAlphaComponent(i, 255);
            setShadowRect(this.mShadowRect);
            setCursorRect(this.mCursorRect);
            invalidate();
            setSelectedVirtualViewId();
            return;
        }
        this.mSelectedVirtualViewId = -1;
    }

    private void setCursorRect(Rect rect) {
        rect.set((int) (((((double) this.mCursorIndex.x) - 0.05d) * ((double) this.mSwatchItemWidth)) + 4.5d), (int) (((((double) this.mCursorIndex.y) - 0.05d) * ((double) this.mSwatchItemHeight)) + 4.5d), (int) (((((double) (this.mCursorIndex.x + 1)) + 0.05d) * ((double) this.mSwatchItemWidth)) + 4.5d), (int) (((((double) (this.mCursorIndex.y + 1)) + 0.05d) * ((double) this.mSwatchItemHeight)) + 4.5d));
    }

    private void setShadowRect(Rect rect) {
        rect.set((int) ((((float) this.mCursorIndex.x) * this.mSwatchItemWidth) + SWATCH_ITEM_SIZE_ROUNDING_VALUE), (int) ((((float) this.mCursorIndex.y) * this.mSwatchItemHeight) + SWATCH_ITEM_SIZE_ROUNDING_VALUE), (int) (((((double) (this.mCursorIndex.x + 1)) + 0.05d) * ((double) this.mSwatchItemWidth)) + 4.5d), (int) (((((double) (this.mCursorIndex.y + 1)) + 0.1d) * ((double) this.mSwatchItemHeight)) + 4.5d));
    }


    private class SeslColorSwatchViewTouchHelper extends ExploreByTouchHelper {
        private String[][] mColorDescription;
        private int mVirtualCursorIndexX;
        private int mVirtualCursorIndexY;
        private final Rect mVirtualViewRect = new Rect();

        SeslColorSwatchViewTouchHelper(View view) {
            super(view);
            this.mColorDescription = new String[][]{new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_white), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_gray), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_gray), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_gray), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_black)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_red), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_red), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_red)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_orange), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_orange), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_orange)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_yellow), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_yellow), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_yellow)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_green), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_green), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_green)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_spring_green), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_spring_green), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_spring_green)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_cyan), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_cyan), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_cyan)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_azure), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_azure), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_azure)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_blue), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_blue), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_blue)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_violet), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_violet), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_violet)}, new String[]{SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_light_magenta), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_magenta), SeslColorSwatchView.this.mResources.getString(R.string.sesl_color_picker_dark_magenta)}};
        }

        @Override
        protected int getVirtualViewAt(float f, float f2) {
            setVirtualCursorIndexAt(f, f2);
            return getFocusedVirtualViewId();
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> list) {
            for (int i = 0; i < MAX_SWATCH_VIEW_ID; i++) {
                list.add(Integer.valueOf(i));
            }
        }

        @Override
        protected void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setContentDescription(getItemDescription(i));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            setVirtualCursorIndexAt(i);
            setVirtualCursorRect(this.mVirtualViewRect);
            accessibilityNodeInfoCompat.setContentDescription(getItemDescription(i));
            accessibilityNodeInfoCompat.setBoundsInParent(this.mVirtualViewRect);
            accessibilityNodeInfoCompat.addAction(16);
            accessibilityNodeInfoCompat.setClassName(Button.class.getName());
            if (SeslColorSwatchView.this.mSelectedVirtualViewId != -1 && i == SeslColorSwatchView.this.mSelectedVirtualViewId) {
                accessibilityNodeInfoCompat.addAction(4);
                accessibilityNodeInfoCompat.setClickable(true);
                accessibilityNodeInfoCompat.setCheckable(true);
                accessibilityNodeInfoCompat.setChecked(true);
            }
        }

        @Override
        protected boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            setVirtualCursorIndexAt(i);
            onVirtualViewClick(SeslColorSwatchView.this.mColorSwatch[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY]);
            return false;
        }

        private int getFocusedVirtualViewId() {
            return this.mVirtualCursorIndexX + (this.mVirtualCursorIndexY * 11);
        }

        private void setVirtualCursorIndexAt(float f, float f2) {
            float f3 = SeslColorSwatchView.this.mSwatchItemWidth * 11.0f;
            float f4 = SeslColorSwatchView.this.mSwatchItemHeight * 10.0f;
            if (f >= f3) {
                f = f3 - 1.0f;
            } else if (f < 0.0f) {
                f = 0.0f;
            }
            if (f2 >= f4) {
                f2 = f4 - 1.0f;
            } else if (f2 < 0.0f) {
                f2 = 0.0f;
            }
            this.mVirtualCursorIndexX = (int) (f / SeslColorSwatchView.this.mSwatchItemWidth);
            this.mVirtualCursorIndexY = (int) (f2 / SeslColorSwatchView.this.mSwatchItemHeight);
        }

        private void setVirtualCursorIndexAt(int i) {
            this.mVirtualCursorIndexX = i % 11;
            this.mVirtualCursorIndexY = i / 11;
        }

        private void setVirtualCursorRect(Rect rect) {
            rect.set((int) ((((float) this.mVirtualCursorIndexX) * SeslColorSwatchView.this.mSwatchItemWidth) + SeslColorSwatchView.SWATCH_ITEM_SIZE_ROUNDING_VALUE), (int) ((((float) this.mVirtualCursorIndexY) * SeslColorSwatchView.this.mSwatchItemHeight) + SeslColorSwatchView.SWATCH_ITEM_SIZE_ROUNDING_VALUE), (int) ((((float) (this.mVirtualCursorIndexX + 1)) * SeslColorSwatchView.this.mSwatchItemWidth) + SeslColorSwatchView.SWATCH_ITEM_SIZE_ROUNDING_VALUE), (int) ((((float) (this.mVirtualCursorIndexY + 1)) * SeslColorSwatchView.this.mSwatchItemHeight) + SeslColorSwatchView.SWATCH_ITEM_SIZE_ROUNDING_VALUE));
        }

        private StringBuilder getItemDescription(int i) {
            setVirtualCursorIndexAt(i);
            if (SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY] == null) {
                StringBuilder sb = new StringBuilder();
                int i2 = this.mVirtualCursorIndexX;
                if (i2 == 0) {
                    int i3 = this.mVirtualCursorIndexY;
                    if (i3 == 0) {
                        sb.append(this.mColorDescription[i2][0]);
                    } else if (i3 < 3) {
                        sb.append(this.mColorDescription[i2][1]);
                    } else if (i3 < 6) {
                        sb.append(this.mColorDescription[i2][2]);
                    } else if (i3 < 9) {
                        sb.append(this.mColorDescription[i2][3]);
                    } else {
                        sb.append(this.mColorDescription[i2][4]);
                    }
                } else {
                    int i4 = this.mVirtualCursorIndexY;
                    if (i4 < 3) {
                        sb.append(this.mColorDescription[i2][0]);
                    } else if (i4 < 6) {
                        sb.append(this.mColorDescription[i2][1]);
                    } else {
                        sb.append(this.mColorDescription[i2][2]);
                    }
                }
                int i5 = this.mVirtualCursorIndexX;
                if (!(i5 == 3 && this.mVirtualCursorIndexY == 3)) {
                    if (i5 == 0 && this.mVirtualCursorIndexY == 4) {
                        sb.append(", ").append(SeslColorSwatchView.this.mColorBrightness[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY]);
                    } else if (this.mVirtualCursorIndexY != 4) {
                        sb.append(", ").append(SeslColorSwatchView.this.mColorBrightness[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY]);
                    }
                }
                SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY] = sb;
            }
            return SeslColorSwatchView.this.mColorSwatchDescription[this.mVirtualCursorIndexX][this.mVirtualCursorIndexY];
        }

        private void onVirtualViewClick(int i) {
            if (SeslColorSwatchView.this.mListener != null) {
                SeslColorSwatchView.this.mListener.onColorSwatchChanged(i);
            }
            SeslColorSwatchView.this.mTouchHelper.sendEventForVirtualView(SeslColorSwatchView.this.mSelectedVirtualViewId, 1);
        }
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mTouchHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    private static int dpToPx(int i) {
        return (int) (((float) i) * Resources.getSystem().getDisplayMetrics().density);
    }
}
