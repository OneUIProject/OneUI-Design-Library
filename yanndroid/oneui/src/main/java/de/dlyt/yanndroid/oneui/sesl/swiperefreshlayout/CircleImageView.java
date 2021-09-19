package de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
public class CircleImageView extends ImageView {
    private static final int DEFAULT_BACKGROUND_COLOR = -197380;
    private static final int DEFAULT_BACKGROUND_COLOR_DARK = -12763843;
    private static final int FILL_SHADOW_COLOR = 1023410176;
    private static final int KEY_SHADOW_COLOR = 503316480;
    private static final int SHADOW_ELEVATION = 8;
    private static final float SHADOW_RADIUS = 3.5f;
    private static final float X_OFFSET = 0.0f;
    private static final float Y_OFFSET = 1.75f;
    private int mBackgroundColor;
    private Animation.AnimationListener mListener;
    private int mShadowRadius;

    public CircleImageView(Context context) {
        super(context);
        ShapeDrawable shapeDrawable;
        float f = getContext().getResources().getDisplayMetrics().density;
        int i = (int) (Y_OFFSET * f);
        int i2 = (int) (0.0f * f);
        this.mShadowRadius = (int) (SHADOW_RADIUS * f);
        TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(R.styleable.SwipeRefreshLayout);
        this.mBackgroundColor = obtainStyledAttributes.getColor(R.styleable.SwipeRefreshLayout_swipeRefreshLayoutProgressSpinnerBackgroundColor, isLightTheme(context) ? DEFAULT_BACKGROUND_COLOR : DEFAULT_BACKGROUND_COLOR_DARK);
        obtainStyledAttributes.recycle();
        if (elevationSupported()) {
            shapeDrawable = new ShapeDrawable(new OvalShape());
            ViewCompat.setElevation(this, f * 8.0f);
        } else {
            shapeDrawable = new ShapeDrawable(new OvalShadow(this, this.mShadowRadius));
            setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.getPaint());
            shapeDrawable.getPaint().setShadowLayer((float) this.mShadowRadius, (float) i2, (float) i, KEY_SHADOW_COLOR);
            int i3 = this.mShadowRadius;
            setPadding(i3, i3, i3, i3);
        }
        shapeDrawable.getPaint().setColor(this.mBackgroundColor);
        ViewCompat.setBackground(this, shapeDrawable);
    }

    private boolean isLightTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(16844176, typedValue, true) || typedValue.data != 0) {
            return true;
        }
        return false;
    }

    private boolean elevationSupported() {
        return Build.VERSION.SDK_INT >= 21;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (!elevationSupported()) {
            setMeasuredDimension(getMeasuredWidth() + (this.mShadowRadius * 2), getMeasuredHeight() + (this.mShadowRadius * 2));
        }
    }

    public void setAnimationListener(Animation.AnimationListener animationListener) {
        this.mListener = animationListener;
    }

    public void onAnimationStart() {
        super.onAnimationStart();
        Animation.AnimationListener animationListener = this.mListener;
        if (animationListener != null) {
            animationListener.onAnimationStart(getAnimation());
        }
    }

    public void onAnimationEnd() {
        super.onAnimationEnd();
        Animation.AnimationListener animationListener = this.mListener;
        if (animationListener != null) {
            animationListener.onAnimationEnd(getAnimation());
        }
    }

    public void setBackgroundColor(int i) {
        if (getBackground() instanceof ShapeDrawable) {
            ((ShapeDrawable) getBackground()).getPaint().setColor(i);
            this.mBackgroundColor = i;
        }
    }

    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    private static class OvalShadow extends OvalShape {
        private CircleImageView mCircleImageView;
        private Paint mShadowPaint = new Paint();
        private int mShadowRadius;

        OvalShadow(CircleImageView circleImageView, int i) {
            this.mCircleImageView = circleImageView;
            this.mShadowRadius = i;
            updateRadialGradient((int) rect().width());
        }

        /* access modifiers changed from: protected */
        public void onResize(float f, float f2) {
            super.onResize(f, f2);
            updateRadialGradient((int) f);
        }

        public void draw(Canvas canvas, Paint paint) {
            int width = this.mCircleImageView.getWidth() / 2;
            float f = (float) width;
            float height = (float) (this.mCircleImageView.getHeight() / 2);
            canvas.drawCircle(f, height, f, this.mShadowPaint);
            canvas.drawCircle(f, height, (float) (width - this.mShadowRadius), paint);
        }

        private void updateRadialGradient(int i) {
            float f = (float) (i / 2);
            this.mShadowPaint.setShader(new RadialGradient(f, f, (float) this.mShadowRadius, new int[]{CircleImageView.FILL_SHADOW_COLOR, 0}, (float[]) null, Shader.TileMode.CLAMP));
        }
    }
}
