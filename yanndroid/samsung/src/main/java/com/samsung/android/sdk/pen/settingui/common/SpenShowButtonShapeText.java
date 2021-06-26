package com.samsung.android.sdk.pen.settingui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.Settings;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

import de.dlyt.yanndroid.samsung.R;

@SuppressLint("AppCompatCustomView")
public class SpenShowButtonShapeText extends TextView {
    private final String TAG = "SpenShowButtonShapeText";
    private int mButtonShapeBgColor;
    private Paint mButtonShapePaint = null;
    private RectF mButtonShapeRect = null;
    private boolean mButtonShapeSettingEnabled = false;
    private int mButtonShapeStrokeBottom;
    private int mButtonShapeStrokeHorizontal;
    private int mButtonShapeStrokeRadius;
    private int mButtonShapeStrokeTop;
    private int mButtonShapeTextColor;
    private boolean mIsButtonShapeTarget = false;
    private boolean mIsSetTextForButtonShape = false;

    public SpenShowButtonShapeText(Context context) {
        super(context);
        init(context);
    }

    public SpenShowButtonShapeText(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public SpenShowButtonShapeText(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public void setButtonShapeEnabled(boolean z) {
        setButtonShapeEnabled(z, z ? SpenSettingUtil.getColor(getContext(), R.color.setting_bg_color) : 0);
    }

    private void setButtonShapeEnabled(boolean z, int i) {
        Log.i("SpenShowButtonShapeText", "setButtonShapeEnabled() enabled=" + z + " textColor=" + getColor(i));
        this.mIsButtonShapeTarget = z;
        this.mButtonShapeTextColor = i;
        this.mButtonShapeBgColor = getCurrentTextColor();
        Paint paint = this.mButtonShapePaint;
        if (paint != null) {
            paint.setColor(this.mButtonShapeBgColor);
        }
        setButtonShapeSetting(this.mButtonShapeSettingEnabled);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Log.i("SpenShowButtonShape", "onDraw() + text=" + ((Object) getText()) + "(" + getText().length() + ")");
        if (this.mButtonShapeSettingEnabled && this.mIsButtonShapeTarget && this.mButtonShapePaint != null && this.mButtonShapeRect != null && !TextUtils.isEmpty(getText())) {
            int compoundPaddingLeft = getCompoundPaddingLeft();
            int extendedPaddingTop = getExtendedPaddingTop();
            if ((getGravity() & 112) != 48) {
                extendedPaddingTop += getVerticalOffset(false);
            }
            Layout layout = getLayout();
            int lineForOffset = layout.getLineForOffset(0);
            int lineForOffset2 = layout.getLineForOffset(getText().length());
            float lineLeft = layout.getLineLeft(lineForOffset);
            float lineRight = layout.getLineRight(lineForOffset);
            float f = lineLeft;
            for (int i = lineForOffset; i <= lineForOffset2; i++) {
                if (f > layout.getLineLeft(i)) {
                    f = layout.getLineLeft(i);
                }
                if (lineRight < layout.getLineRight(i)) {
                    lineRight = layout.getLineRight(i);
                }
            }
            float f2 = (float) extendedPaddingTop;
            this.mButtonShapeRect.top = (((float) layout.getLineTop(lineForOffset)) + f2) - ((float) this.mButtonShapeStrokeTop);
            this.mButtonShapeRect.bottom = ((float) layout.getLineBottom(lineForOffset2)) + f2 + ((float) this.mButtonShapeStrokeBottom);
            float f3 = (float) compoundPaddingLeft;
            this.mButtonShapeRect.left = (((float) Math.floor((double) f)) + f3) - ((float) this.mButtonShapeStrokeHorizontal);
            this.mButtonShapeRect.right = ((float) Math.ceil((double) lineRight)) + f3 + ((float) this.mButtonShapeStrokeHorizontal);
            RectF rectF = this.mButtonShapeRect;
            int i2 = this.mButtonShapeStrokeRadius;
            canvas.drawRoundRect(rectF, (float) i2, (float) i2, this.mButtonShapePaint);
        }
        super.onDraw(canvas);
    }

    /* access modifiers changed from: package-private */
    public int getVerticalOffset(boolean z) {
        int boxHeight;
        int height;
        int gravity = getGravity() & 112;
        Layout layout = getLayout();
        if (gravity == 48 || (height = layout.getHeight()) >= (boxHeight = getBoxHeight(layout))) {
            return 0;
        }
        return gravity == 80 ? boxHeight - height : (boxHeight - height) >> 1;
    }

    private int getBoxHeight(Layout layout) {
        return getMeasuredHeight() - (getExtendedPaddingTop() + getExtendedPaddingBottom());
    }

    @Override // android.widget.TextView
    public void setTextColor(@ColorInt int i) {
        super.setTextColor(i);
        Log.i("SpenShowButtonShapeText", "++++++++++++++ setTextColor() color=" + getColor(i));
        if (!this.mIsSetTextForButtonShape) {
            this.mButtonShapeBgColor = i;
            Paint paint = this.mButtonShapePaint;
            if (paint != null) {
                paint.setColor(i);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Log.i("SpenShowButtonShape", "onConfigurationChanged() mButtonShapeSettingEnabled=" + this.mButtonShapeSettingEnabled);
        boolean isButtonShapeSettingEnable = isButtonShapeSettingEnable();
        if (isButtonShapeSettingEnable != this.mButtonShapeSettingEnabled) {
            setButtonShapeSetting(isButtonShapeSettingEnable);
            this.mButtonShapeSettingEnabled = isButtonShapeSettingEnable;
        }
    }

    private void init(Context context) {
        Resources resources = context.getResources();
        this.mButtonShapeStrokeTop = resources.getDimensionPixelSize(R.dimen.setting_show_button_top);
        this.mButtonShapeStrokeBottom = resources.getDimensionPixelSize(R.dimen.setting_show_button_bottom);
        this.mButtonShapeStrokeHorizontal = resources.getDimensionPixelSize(R.dimen.setting_show_button_left_right);
        this.mButtonShapeStrokeRadius = resources.getDimensionPixelSize(R.dimen.setting_show_button_radius);
        if (this.mButtonShapePaint == null) {
            this.mButtonShapePaint = new Paint();
            this.mButtonShapePaint.setAntiAlias(true);
        }
        if (this.mButtonShapeRect == null) {
            this.mButtonShapeRect = new RectF();
        }
        this.mButtonShapeSettingEnabled = isButtonShapeSettingEnable();
        Log.i("SpenShowButtonShapeText", "init(), mButtonShapeSettingEnabled=" + this.mButtonShapeSettingEnabled);
    }

    private boolean isButtonShapeSettingEnable() {
        return Settings.System.getInt(getContext().getContentResolver(), "show_button_background", 0) == 1;
    }

    private void setButtonShapeSetting(boolean z) {
        this.mIsSetTextForButtonShape = true;
        if (z) {
            setTextColor(this.mButtonShapeTextColor);
        } else {
            setTextColor(this.mButtonShapeBgColor);
        }
        this.mIsSetTextForButtonShape = false;
    }

    private String getColor(int i) {
        return String.format(" #%08X", Integer.valueOf(i & -1));
    }
}
