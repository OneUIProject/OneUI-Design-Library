package androidx.appcompat.animation;

import android.annotation.SuppressLint;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.RestrictTo;

public class SeslAnimationUtils {
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public static final Interpolator ELASTIC_40 = new SeslElasticInterpolator(1.0f, 0.8f);
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public static final Interpolator ELASTIC_50 = new SeslElasticInterpolator(1.0f, 0.7f);
    @SuppressLint({"NewApi"})
    public static final Interpolator SINE_IN_OUT_70 = new PathInterpolator(0.33f, 0.0f, 0.3f, 1.0f);
    @SuppressLint({"NewApi"})
    public static final Interpolator SINE_IN_OUT_80 = new PathInterpolator(0.33f, 0.0f, 0.2f, 1.0f);
    @SuppressLint({"NewApi"})
    public static final Interpolator SINE_IN_OUT_90 = new PathInterpolator(0.33f, 0.0f, 0.1f, 1.0f);
    @SuppressLint({"NewApi"})
    public static final Interpolator SINE_OUT_80 = new PathInterpolator(0.17f, 0.17f, 0.2f, 1.0f);
}
