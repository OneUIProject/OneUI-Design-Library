package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.internal.ThemeEnforcement;

import de.dlyt.yanndroid.oneui.R;

@RequiresApi(21)
class ViewUtilsLollipop {
    private static final int[] STATE_LIST_ANIM_ATTRS = new int[] {android.R.attr.stateListAnimator};

    static void setBoundsViewOutlineProvider(@NonNull View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

    @SuppressLint("RestrictedApi")
    static void setStateListAnimatorFromAttrs(@NonNull View view, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Context context = view.getContext();
        final TypedArray a = ThemeEnforcement.obtainStyledAttributes(context, attrs, STATE_LIST_ANIM_ATTRS, defStyleAttr, defStyleRes);
        try {
            if (a.hasValue(0)) {
                StateListAnimator sla = AnimatorInflater.loadStateListAnimator(context, a.getResourceId(0, 0));
                view.setStateListAnimator(sla);
            }
        } finally {
            a.recycle();
        }
    }

    static void setDefaultAppBarLayoutStateListAnimator(@NonNull final View view, final float elevation) {
        final int dur = view.getResources().getInteger(R.integer.app_bar_elevation_anim_duration);

        final StateListAnimator sla = new StateListAnimator();

        sla.addState(new int[] {android.R.attr.state_enabled, R.attr.state_liftable, -R.attr.state_lifted}, ObjectAnimator.ofFloat(view, "elevation", 0f).setDuration(dur));

        sla.addState(new int[] {android.R.attr.state_enabled}, ObjectAnimator.ofFloat(view, "elevation", elevation).setDuration(dur));

        sla.addState(new int[0], ObjectAnimator.ofFloat(view, "elevation", 0).setDuration(0));

        view.setStateListAnimator(sla);
    }
}
