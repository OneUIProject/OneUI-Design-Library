package de.dlyt.yanndroid.oneui.sesl.swiperefreshlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;

@SuppressLint("AppCompatCustomView")
public class CircleImageView extends ImageView {
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

        final float density = getContext().getResources().getDisplayMetrics().density;
        final int shadowYOffset = (int) (density * Y_OFFSET);
        final int shadowXOffset = (int) (density * X_OFFSET);

        mShadowRadius = (int) (density * SHADOW_RADIUS);

        @SuppressLint("CustomViewStyleable")
        TypedArray colorArray = getContext().obtainStyledAttributes(R.styleable.SwipeRefreshLayout);
        mBackgroundColor = colorArray.getColor(R.styleable.SwipeRefreshLayout_swipeRefreshLayoutProgressSpinnerBackgroundColor, getResources().getColor(R.color.sesl_swipe_refresh_background));
        colorArray.recycle();

        ShapeDrawable circle;
        if (elevationSupported()) {
            circle = new ShapeDrawable(new OvalShape());
            ViewCompat.setElevation(this, SHADOW_ELEVATION * density);
        } else {
            circle = new ShapeDrawable(new OvalShadow(this, mShadowRadius));
            setLayerType(View.LAYER_TYPE_SOFTWARE, circle.getPaint());
            circle.getPaint().setShadowLayer(mShadowRadius, shadowXOffset, shadowYOffset, KEY_SHADOW_COLOR);
            final int padding = mShadowRadius;
            setPadding(padding, padding, padding, padding);
        }
        circle.getPaint().setColor(mBackgroundColor);
        ViewCompat.setBackground(this, circle);
    }

    private boolean isLightTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        if (!context.getTheme().resolveAttribute(16844176, typedValue, true) || typedValue.data != 0) {
            return true;
        }
        return false;
    }

    private boolean elevationSupported() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!elevationSupported()) {
            setMeasuredDimension(getMeasuredWidth() + mShadowRadius * 2, getMeasuredHeight() + mShadowRadius * 2);
        }
    }

    public void setAnimationListener(Animation.AnimationListener listener) {
        mListener = listener;
    }

    @Override
    public void onAnimationStart() {
        super.onAnimationStart();
        if (mListener != null) {
            mListener.onAnimationStart(getAnimation());
        }
    }

    @Override
    public void onAnimationEnd() {
        super.onAnimationEnd();
        if (mListener != null) {
            mListener.onAnimationEnd(getAnimation());
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        if (getBackground() instanceof ShapeDrawable) {
            ((ShapeDrawable) getBackground()).getPaint().setColor(color);
            mBackgroundColor = color;
        }
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    private static class OvalShadow extends OvalShape {
        private Paint mShadowPaint;
        private int mShadowRadius;
        private CircleImageView mCircleImageView;

        OvalShadow(CircleImageView circleImageView, int shadowRadius) {
            super();
            mCircleImageView = circleImageView;
            mShadowPaint = new Paint();
            mShadowRadius = shadowRadius;
            updateRadialGradient((int) rect().width());
        }

        @Override
        protected void onResize(float width, float height) {
            super.onResize(width, height);
            updateRadialGradient((int) width);
        }

        @Override
        public void draw(Canvas canvas, Paint paint) {
            final int x = mCircleImageView.getWidth() / 2;
            final int y = mCircleImageView.getHeight() / 2;
            canvas.drawCircle(x, y, x, mShadowPaint);
            canvas.drawCircle(x, y, x - mShadowRadius, paint);
        }

        private void updateRadialGradient(int diameter) {
            mShadowPaint.setShader(new RadialGradient(diameter / 2, diameter / 2, mShadowRadius, new int[]{FILL_SHADOW_COLOR, Color.TRANSPARENT}, null, Shader.TileMode.CLAMP));
        }
    }
}
