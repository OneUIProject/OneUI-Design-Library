package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;

public class SeslTabRoundRectIndicator extends SeslAbsIndicatorView {
    private boolean mIsOneUI4;
    private static final int DURATION_PRESS = 50;
    private static final int DURATION_RELEASE = 350;
    private static final float SCALE_MINOR = 0.95f;
    private AnimationSet mPressAnimationSet;

    public SeslTabRoundRectIndicator(Context context) {
        this(context, null);
    }

    public SeslTabRoundRectIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeslTabRoundRectIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeslTabRoundRectIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        ViewCompat.setBackground(this, ContextCompat.getDrawable(context, mIsOneUI4 ? R.drawable.sesl4_tablayout_subtab_indicator_background : R.drawable.sesl_tablayout_subtab_indicator_background));
        onSetSelectedIndicatorColor(getResources().getColor(mIsOneUI4 ? R.color.sesl4_tablayout_subtab_background_stroke_color : R.color.sesl_tablayout_subtab_background_stroke_color));
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != VISIBLE && !isSelected()) {
            onHide();
        }
    }

    @Override
    void onHide() {
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 0.0f);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        startAnimation(alpha);
        setAlpha(0.0f);
    }

    @Override
    void onShow() {
        setAlpha(1.0f);
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 1.0f);
        alpha.setDuration(0);
        alpha.setFillAfter(true);
        startAnimation(alpha);
    }

    @Override
    void startPressEffect() {
        setAlpha(1.0f);

        mPressAnimationSet = new AnimationSet(false);
        mPressAnimationSet.setStartOffset(DURATION_PRESS);
        mPressAnimationSet.setFillAfter(true);
        mPressAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPressAnimationSet = null;
            }
        });

        ScaleAnimation scale = new ScaleAnimation(1.0f, SCALE_MINOR, 1.0f, SCALE_MINOR, 1, 0.5f, 1, 0.5f);
        scale.setDuration(DURATION_PRESS);
        scale.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
        scale.setFillAfter(true);
        mPressAnimationSet.addAnimation(scale);

        if (!isSelected()) {
            AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(DURATION_PRESS);
            alpha.setFillAfter(true);
            alpha.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            mPressAnimationSet.addAnimation(alpha);
        }

        startAnimation(mPressAnimationSet);
    }

    @Override
    void startReleaseEffect() {
        setAlpha(1.0f);

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(true);

        ScaleAnimation scale = new ScaleAnimation(SCALE_MINOR, 1.0f, SCALE_MINOR, 1.0f, 1, 0.5f, 1, 0.5f);
        scale.setDuration(DURATION_RELEASE);
        scale.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
        scale.setFillAfter(true);
        set.addAnimation(scale);

        startAnimation(set);
    }

    @Override
    void onSetSelectedIndicatorColor(int color) {
        if (!(getBackground() instanceof NinePatchDrawable)) {
            if (Build.VERSION.SDK_INT >= 22) {
                getBackground().setTint(color);
            } else {
                getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            if (!isSelected()) {
                setHide();
            }
        }
    }
}
