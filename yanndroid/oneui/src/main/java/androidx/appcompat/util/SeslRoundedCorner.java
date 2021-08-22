package androidx.appcompat.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.RestrictTo;

import de.dlyt.yanndroid.oneui.R;

public class SeslRoundedCorner {
    private static final int RADIUS = 26;
    public static final int ROUNDED_CORNER_ALL = 15;
    public static final int ROUNDED_CORNER_BOTTOM_LEFT = 4;
    public static final int ROUNDED_CORNER_BOTTOM_RIGHT = 8;
    public static final int ROUNDED_CORNER_NONE = 0;
    public static final int ROUNDED_CORNER_TOP_LEFT = 1;
    public static final int ROUNDED_CORNER_TOP_RIGHT = 2;
    static final String TAG = "SeslRoundedCorner";
    Drawable mBottomLeftRound;
    @ColorInt
    private int mBottomLeftRoundColor;
    Drawable mBottomRightRound;
    @ColorInt
    private int mBottomRightRoundColor;
    private Context mContext;
    private boolean mIsMutate = false;
    private Resources mRes;
    int mRoundRadius = -1;
    Rect mRoundedCornerBounds = new Rect();
    int mRoundedCornerMode;
    Drawable mTopLeftRound;
    @ColorInt
    private int mTopLeftRoundColor;
    Drawable mTopRightRound;
    @ColorInt
    private int mTopRightRoundColor;
    int mX;
    int mY;

    public SeslRoundedCorner(Context context) {
        this.mContext = context;
        this.mRes = context.getResources();
        initRoundedCorner();
    }

    public SeslRoundedCorner(Context context, boolean z) {
        this.mContext = context;
        this.mRes = context.getResources();
        this.mIsMutate = z;
        initRoundedCorner();
    }

    private void drawRoundedCornerInternal(Canvas canvas) {
        Rect rect = this.mRoundedCornerBounds;
        int i = rect.left;
        int i2 = rect.right;
        int i3 = rect.top;
        int i4 = rect.bottom;
        if ((this.mRoundedCornerMode & 1) != 0) {
            Drawable drawable = this.mTopLeftRound;
            int i5 = this.mRoundRadius;
            drawable.setBounds(i, i3, i + i5, i5 + i3);
            this.mTopLeftRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 2) != 0) {
            Drawable drawable2 = this.mTopRightRound;
            int i6 = this.mRoundRadius;
            drawable2.setBounds(i2 - i6, i3, i2, i6 + i3);
            this.mTopRightRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 4) != 0) {
            Drawable drawable3 = this.mBottomLeftRound;
            int i7 = this.mRoundRadius;
            drawable3.setBounds(i, i4 - i7, i7 + i, i4);
            this.mBottomLeftRound.draw(canvas);
        }
        if ((this.mRoundedCornerMode & 8) != 0) {
            Drawable drawable4 = this.mBottomRightRound;
            int i8 = this.mRoundRadius;
            drawable4.setBounds(i2 - i8, i4 - i8, i2, i4);
            this.mBottomRightRound.draw(canvas);
        }
    }

    private void initRoundedCorner() {
        this.mRoundRadius = (int) TypedValue.applyDimension(1, 26.0f, this.mRes.getDisplayMetrics());
        Resources.Theme theme = this.mContext.getTheme();
        if (this.mIsMutate) {
            this.mTopLeftRound = this.mRes.getDrawable(R.drawable.sesl_top_left_round, theme).mutate();
            this.mTopRightRound = this.mRes.getDrawable(R.drawable.sesl_top_right_round, theme).mutate();
            this.mBottomLeftRound = this.mRes.getDrawable(R.drawable.sesl_bottom_left_round, theme).mutate();
            this.mBottomRightRound = this.mRes.getDrawable(R.drawable.sesl_bottom_right_round, theme).mutate();
        } else {
            this.mTopLeftRound = this.mRes.getDrawable(R.drawable.sesl_top_left_round, theme);
            this.mTopRightRound = this.mRes.getDrawable(R.drawable.sesl_top_right_round, theme);
            this.mBottomLeftRound = this.mRes.getDrawable(R.drawable.sesl_bottom_left_round, theme);
            this.mBottomRightRound = this.mRes.getDrawable(R.drawable.sesl_bottom_right_round, theme);
        }
        int color = getColor(mContext, android.R.attr.windowBackground);
        this.mBottomRightRoundColor = color;
        this.mBottomLeftRoundColor = color;
        this.mTopRightRoundColor = color;
        this.mTopLeftRoundColor = color;

        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(this.mTopLeftRoundColor, PorterDuff.Mode.SRC_IN);
        this.mTopLeftRound.setColorFilter(porterDuffColorFilter);
        this.mTopRightRound.setColorFilter(porterDuffColorFilter);
        this.mBottomLeftRound.setColorFilter(porterDuffColorFilter);
        this.mBottomRightRound.setColorFilter(porterDuffColorFilter);
    }

    private void removeRoundedCorner(int i) {
        if ((i & 1) != 0) {
            this.mTopLeftRound = null;
        }
        if ((i & 2) != 0) {
            this.mTopRightRound = null;
        }
        if ((i & 4) != 0) {
            this.mBottomLeftRound = null;
        }
        if ((i & 8) != 0) {
            this.mBottomRightRound = null;
        }
    }

    public void drawRoundedCorner(Canvas canvas) {
        canvas.getClipBounds(this.mRoundedCornerBounds);
        drawRoundedCornerInternal(canvas);
    }

    public void drawRoundedCorner(View view, Canvas canvas) {
        if (view.getTranslationY() != 0.0f) {
            this.mX = Math.round(view.getX());
            this.mY = Math.round(view.getY());
            canvas.translate((view.getX() - ((float) this.mX)) + 0.5f, (view.getY() - ((float) this.mY)) + 0.5f);
        } else {
            this.mX = view.getLeft();
            this.mY = view.getTop();
        }
        Rect rect = this.mRoundedCornerBounds;
        int i = this.mX;
        rect.set(i, this.mY, view.getWidth() + i, this.mY + view.getHeight());
        drawRoundedCornerInternal(canvas);
    }

    @ColorInt
    public int getRoundedCornerColor(int i) {
        if (i == 0) {
            throw new IllegalArgumentException("There is no rounded corner on = " + this);
        } else if (i == 1 || i == 2 || i == 4 || i == 8) {
            return (i & 1) != 0 ? this.mTopLeftRoundColor : (i & 2) != 0 ? this.mTopRightRoundColor : (i & 4) != 0 ? this.mBottomLeftRoundColor : this.mBottomRightRoundColor;
        } else {
            throw new IllegalArgumentException("Use multiple rounded corner as param on = " + this);
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public int getRoundedCornerRadius() {
        return this.mRoundRadius;
    }

    public int getRoundedCorners() {
        return this.mRoundedCornerMode;
    }

    public void setRoundedCornerColor(int i, @ColorInt int i2) {
        if (i == 0) {
            throw new IllegalArgumentException("There is no rounded corner on = " + this);
        } else if ((i & -16) == 0) {
            if (this.mTopLeftRound == null || this.mTopRightRound == null || this.mBottomLeftRound == null || this.mBottomRightRound == null) {
                initRoundedCorner();
            }
            PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(i2, PorterDuff.Mode.SRC_IN);
            if ((i & 1) != 0) {
                this.mTopLeftRoundColor = i2;
                this.mTopLeftRound.setColorFilter(porterDuffColorFilter);
            }
            if ((i & 2) != 0) {
                this.mTopRightRoundColor = i2;
                this.mTopRightRound.setColorFilter(porterDuffColorFilter);
            }
            if ((i & 4) != 0) {
                this.mBottomLeftRoundColor = i2;
                this.mBottomLeftRound.setColorFilter(porterDuffColorFilter);
            }
            if ((i & 8) != 0) {
                this.mBottomRightRoundColor = i2;
                this.mBottomRightRound.setColorFilter(porterDuffColorFilter);
            }
        } else {
            throw new IllegalArgumentException("Use wrong rounded corners to the param, corners = " + i);
        }
    }

    public void setRoundedCorners(int i) {
        if ((i & -16) == 0) {
            this.mRoundedCornerMode = i;
            if (this.mTopLeftRound == null || this.mTopRightRound == null || this.mBottomLeftRound == null || this.mBottomRightRound == null) {
                initRoundedCorner();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Use wrong rounded corners to the param, corners = " + i);
    }

    private int getColor(Context context, int colorResId) {
        TypedValue typedValue = new TypedValue();
        TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[]{colorResId});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }
}