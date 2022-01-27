package androidx.appcompat.animation;

import android.view.animation.Interpolator;

import androidx.annotation.RestrictTo;

public class SeslElasticInterpolator implements Interpolator {
    private float mAmplitude;
    private float mPeriod;

    public SeslElasticInterpolator(float f, float f2) {
        this.mAmplitude = f;
        this.mPeriod = f2;
    }

    private float out(float f, float f2, float f3) {
        float f4;
        if (f == 0.0f) {
            return 0.0f;
        }
        if (f >= 1.0f) {
            return 1.0f;
        }
        if (f3 == 0.0f) {
            f3 = 0.3f;
        }
        if (f2 == 0.0f || f2 < 1.0f) {
            f4 = f3 / 4.0f;
            f2 = 1.0f;
        } else {
            f4 = (float) ((((double) f3) / 6.283185307179586d) * Math.asin((double) (1.0f / f2)));
        }
        return (float) ((((double) f2) * Math.pow(2.0d, (double) (-10.0f * f)) * Math.sin((((double) (f - f4)) * 6.283185307179586d) / ((double) f3))) + 1.0d);
    }

    public float getAmplitude() {
        return this.mAmplitude;
    }

    public void setAmplitude(float f) {
        this.mAmplitude = f;
    }

    public float getInterpolation(float f) {
        return out(f, this.mAmplitude, this.mPeriod);
    }

    public float getPeriod() {
        return this.mPeriod;
    }

    public void setPeriod(float f) {
        this.mPeriod = f;
    }
}
