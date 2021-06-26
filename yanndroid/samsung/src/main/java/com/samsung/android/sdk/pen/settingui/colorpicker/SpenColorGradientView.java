package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;

class SpenColorGradientView extends View implements SpenColorViewInterface, SpenPickerColorEventListener {
    private static final int BORDER_COLOR = Color.parseColor("#e2e2e2");
    private static final int BORDER_SIZE = 1;
    private static final int CURSOR_COLOR = -1;
    private static final String TAG = "SpenColorGradientView";
    private static final int TRANSPARENT_WHITE = 16777215;
    private float mCurX;
    private float mCurY;
    private Paint mCursorColorPaint;
    private Paint mCursorPaint;
    private int mCursorSize;
    private int mCursorStrokeSize;
    private Paint mGradientPaint;
    private Rect mGradientSize;
    private float[] mHsv = {0.0f, 0.0f, 0.0f};
    private SpenPickerColor mPickerColor;

    public interface ActionListener {
        void onColorSelected(float f, float f2);
    }

    public SpenColorGradientView(Context context, int i, int i2) {
        super(context);
        construct(context, i, i2);
    }

    private void construct(Context context, int i, int i2) {
        initCursor(context, i, i2);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void setPickerColor(SpenPickerColor spenPickerColor) {
        this.mPickerColor = spenPickerColor;
        float[] fArr = {0.0f, 0.0f, 0.0f};
        this.mPickerColor.getColor(fArr);
        setColor(fArr);
        this.mPickerColor.addEventListener(this);
    }

    @Override
    public void release() {
        this.mGradientSize = null;
        this.mCursorPaint = null;
        this.mCursorColorPaint = null;
        Paint paint = this.mGradientPaint;
        if (paint != null) {
            paint.setShader(null);
            this.mGradientPaint = null;
        }
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.removeEventListener(this);
            this.mPickerColor = null;
        }
    }

    @Override
    public void update(String str, int i, float f, float f2, float f3) {
        if (!str.equals(TAG)) {
            float[] fArr = this.mHsv;
            if (fArr[0] != f || fArr[1] != f2 || fArr[2] != f3) {
                setColor(new float[]{f, f2, f3});
            }
        }
    }

    private void setColor(float[] fArr) {
        float[] fArr2 = this.mHsv;
        fArr2[0] = fArr[0];
        fArr2[1] = fArr[1];
        fArr2[2] = fArr[2];
        updateCursorColor();
        updateCursorPosition();
        updateGradient();
        invalidate();
    }

    private void initCursor(Context context, int i, int i2) {
        Resources resources = context.getResources();
        this.mGradientSize = new Rect();
        this.mCursorSize = resources.getDimensionPixelSize(i);
        this.mCursorStrokeSize = resources.getDimensionPixelSize(i2);
        this.mCursorPaint = new Paint();
        this.mCursorPaint.setStyle(Paint.Style.STROKE);
        this.mCursorPaint.setAntiAlias(true);
        this.mCursorColorPaint = new Paint();
        this.mCursorColorPaint.setAntiAlias(true);
        this.mCursorColorPaint.setDither(true);
    }

    private void updateCursorPosition() {
        if (isInitComplete()) {
            this.mCurX = ((float) this.mGradientSize.left) + (((float) this.mGradientSize.width()) * (this.mHsv[0] / 359.0f));
            this.mCurY = ((float) this.mGradientSize.top) + (((float) this.mGradientSize.height()) * this.mHsv[1]);
        }
    }

    private boolean updatePickedColor() {
        boolean z;
        if (isInitComplete()) {
            float width = ((this.mCurX - ((float) this.mGradientSize.left)) / ((float) this.mGradientSize.width())) * 359.0f;
            float height = (this.mCurY - ((float) this.mGradientSize.top)) / ((float) this.mGradientSize.height());
            if (width < 0.0f) {
                width = 0.0f;
            }
            float[] fArr = this.mHsv;
            if (!(fArr[0] == width && fArr[1] == height)) {
                float[] fArr2 = this.mHsv;
                fArr2[0] = width;
                fArr2[1] = height;
                z = true;
                return z;
            }
        }
        z = false;
        return z;
    }

    private void updateGradient() {
        if (isInitComplete()) {
            Paint paint = this.mGradientPaint;
            if (paint == null) {
                this.mGradientPaint = new Paint();
                this.mGradientPaint.setAntiAlias(true);
            } else {
                paint.setShader(null);
            }
            float[] fArr = {0.0f, 60.0f, 120.0f, 180.0f, 240.0f, 300.0f, 359.0f};
            int[] iArr = new int[7];
            for (int i = 0; i < iArr.length; i++) {
                iArr[i] = SpenSettingUtil.HSVToColor(new float[]{fArr[i], 1.0f, 1.0f});
            }
            this.mGradientPaint.setShader(new ComposeShader(new LinearGradient((float) this.mGradientSize.left, (float) this.mGradientSize.top, (float) this.mGradientSize.right, (float) this.mGradientSize.top, iArr, (float[]) null, Shader.TileMode.CLAMP), new LinearGradient((float) this.mGradientSize.left, (float) this.mGradientSize.top, (float) this.mGradientSize.left, (float) this.mGradientSize.bottom, 16777215, -1, Shader.TileMode.CLAMP), PorterDuff.Mode.MULTIPLY));
        }
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        Rect rect = this.mGradientSize;
        if (rect != null) {
            int i5 = (this.mCursorSize + this.mCursorStrokeSize) / 2;
            rect.set(i5, i5, i - i5, i2 - i5);
            setColor(this.mHsv);
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInitComplete()) {
            Paint paint = this.mCursorPaint;
            if (paint != null) {
                paint.setStyle(Paint.Style.FILL);
                this.mCursorPaint.setColor(-1);
                canvas.drawRect(this.mGradientSize, this.mCursorPaint);
                this.mCursorPaint.setStyle(Paint.Style.STROKE);
            }
            Paint paint2 = this.mGradientPaint;
            if (paint2 != null) {
                canvas.drawRect(this.mGradientSize, paint2);
            }
            Paint paint3 = this.mCursorPaint;
            if (paint3 != null) {
                paint3.setStrokeWidth(1.0f);
                this.mCursorPaint.setColor(BORDER_COLOR);
                canvas.drawRect(this.mGradientSize, this.mCursorPaint);
            }
            Paint paint4 = this.mCursorColorPaint;
            if (paint4 != null) {
                canvas.drawCircle(this.mCurX, this.mCurY, ((float) this.mCursorSize) / 2.0f, paint4);
            }
            Paint paint5 = this.mCursorPaint;
            if (paint5 != null) {
                paint5.setStrokeWidth((float) this.mCursorStrokeSize);
                this.mCursorPaint.setColor(-1);
                canvas.drawCircle(this.mCurX, this.mCurY, ((float) this.mCursorSize) / 2.0f, this.mCursorPaint);
            }
        }
    }

    private boolean isCursorArea(float f, float f2) {
        return ((float) this.mCursorSize) / 2.0f >= ((float) Math.sqrt(Math.pow((double) (this.mCurX - f), 2.0d) + Math.pow((double) (this.mCurY - f2), 2.0d)));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!isInitComplete()) {
            return false;
        }
        boolean contains = this.mGradientSize.contains((int) motionEvent.getX(), (int) motionEvent.getY());
        if (motionEvent.getAction() == 0 && !contains && !isCursorArea(motionEvent.getX(), motionEvent.getY())) {
            return false;
        }
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        this.mCurX = motionEvent.getX();
        this.mCurY = motionEvent.getY();
        if (!contains) {
            if (this.mCurX < ((float) this.mGradientSize.left)) {
                this.mCurX = (float) this.mGradientSize.left;
            } else if (this.mCurX > ((float) this.mGradientSize.right)) {
                this.mCurX = (float) this.mGradientSize.right;
            }
            if (this.mCurY < ((float) this.mGradientSize.top)) {
                this.mCurY = (float) this.mGradientSize.top;
            } else if (this.mCurY > ((float) this.mGradientSize.bottom)) {
                this.mCurY = (float) this.mGradientSize.bottom;
            }
        }
        if (motionEvent.getAction() == 0 && getParent() != null) {
            ((View) getParent()).playSoundEffect(0);
        }
        if (updatePickedColor()) {
            updateCursorColor();
            SpenPickerColor spenPickerColor = this.mPickerColor;
            if (spenPickerColor != null) {
                float[] fArr = this.mHsv;
                spenPickerColor.setColor(TAG, 255, fArr[0], fArr[1], fArr[2]);
            }
        }
        invalidate();
        return true;
    }

    private void updateCursorColor() {
        Paint paint = this.mCursorColorPaint;
        if (paint != null) {
            float[] fArr = this.mHsv;
            paint.setColor(SpenSettingUtil.HSVToColor(new float[]{fArr[0], fArr[1], 1.0f}));
        }
    }

    private boolean isInitComplete() {
        Rect rect = this.mGradientSize;
        return rect != null && !rect.isEmpty();
    }
}
