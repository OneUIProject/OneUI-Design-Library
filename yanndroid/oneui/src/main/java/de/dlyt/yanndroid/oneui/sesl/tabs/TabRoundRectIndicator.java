package de.dlyt.yanndroid.oneui.sesl.tabs;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
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

public class TabRoundRectIndicator extends AbsIndicatorView {
    private boolean mIsOneUI4;
    private AnimationSet mPressAnimationSet;

    public TabRoundRectIndicator(Context context) {
        this(context, null);
    }

    public TabRoundRectIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabRoundRectIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TabRoundRectIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        ViewCompat.setBackground(this, ContextCompat.getDrawable(context, mIsOneUI4 ? R.drawable.sesl4_tablayout_subtab_indicator_background : R.drawable.sesl_tablayout_subtab_indicator_background));
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != 0 && !isSelected()) {
            onHide();
        }
    }

    @Override
    void onHide() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 0.0f);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);
        startAnimation(alphaAnimation);
        setAlpha(0.0f);
    }

    @Override
    void onShow() {
        setAlpha(1.0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 1.0f);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);
        startAnimation(alphaAnimation);
    }

    @Override
    void startPressEffect() {
        setAlpha(1.0f);
        mPressAnimationSet = new AnimationSet(false);
        mPressAnimationSet.setStartOffset(50);
        mPressAnimationSet.setFillAfter(true);
        mPressAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                mPressAnimationSet = null;
            }
        });

        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.95f, 1.0f, 0.95f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(50);
        scaleAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
        scaleAnimation.setFillAfter(true);
        mPressAnimationSet.addAnimation(scaleAnimation);

        if (!isSelected()) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(50);
            alphaAnimation.setFillAfter(true);
            alphaAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
            mPressAnimationSet.addAnimation(alphaAnimation);
        }

        startAnimation(mPressAnimationSet);
    }

    @Override
    void startReleaseEffect() {
        setAlpha(1.0f);
        AnimationSet animationSet = new AnimationSet(false);
        animationSet.setFillAfter(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.95f, 1.0f, 0.95f, 1.0f, 1, 0.5f, 1, 0.5f);
        scaleAnimation.setDuration(350);
        scaleAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_80);
        scaleAnimation.setFillAfter(true);
        animationSet.addAnimation(scaleAnimation);
        startAnimation(animationSet);
    }

    @Override
    void onSetSelectedIndicatorColor(int color) {
        if (!(getBackground() instanceof NinePatchDrawable)) {
            getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            if (!isSelected()) {
                setHide();
            }
        }
    }
}
