package de.dlyt.yanndroid.oneui.sesl.support;

import static android.os.Build.VERSION.SDK_INT;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EdgeEffect;

import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import de.dlyt.yanndroid.oneui.sesl.utils.SamsungEdgeEffect;

public final class EdgeEffectSupport {
    private final EdgeEffect mEdgeEffect;

    @Deprecated
    public EdgeEffectSupport(View view) {
        if (SDK_INT >= 31) {
            mEdgeEffect = new EdgeEffect(view.getContext());
        } else {
            mEdgeEffect = new SamsungEdgeEffect(view.getContext());
            ((SamsungEdgeEffect) mEdgeEffect).setHostView(view, false);
        }
    }

    @NonNull
    public static EdgeEffect create(@NonNull View view, @Nullable AttributeSet attrs) {
        if (SDK_INT >= 31) {
            return Api31Impl.create(view.getContext(), attrs);
        } else {
            SamsungEdgeEffect edgeEffect = new SamsungEdgeEffect(view.getContext());
            edgeEffect.setHostView(view, false);
            return edgeEffect;
        }
    }

    public static float getDistance(@NonNull EdgeEffect edgeEffect) {
        if (SDK_INT >= 31) {
            return Api31Impl.getDistance(edgeEffect);
        } else {
            return 0;
        }
    }

    @Deprecated
    public void setSize(int width, int height) {
        mEdgeEffect.setSize(width, height);
    }

    @Deprecated
    public boolean isFinished() {
        return mEdgeEffect.isFinished();
    }

    @Deprecated
    public void finish() {
        mEdgeEffect.finish();
    }

    @Deprecated
    public boolean onPull(float deltaDistance) {
        mEdgeEffect.onPull(deltaDistance);
        return true;
    }

    @Deprecated
    public boolean onPull(float deltaDistance, float displacement) {
        onPull(mEdgeEffect, deltaDistance, displacement);
        return true;
    }

    public static void onPull(@NonNull EdgeEffect edgeEffect, float deltaDistance, float displacement) {
        if (SDK_INT >= 21) {
            edgeEffect.onPull(deltaDistance, displacement);
        } else {
            edgeEffect.onPull(deltaDistance);
        }
    }

    public static float onPullDistance(@NonNull EdgeEffect edgeEffect, float deltaDistance, float displacement) {
        if (SDK_INT >= 31) {
            return Api31Impl.onPullDistance(edgeEffect, deltaDistance, displacement);
        } else {
            onPull(edgeEffect, deltaDistance, displacement);
            return deltaDistance;
        }
    }

    @Deprecated
    public boolean onRelease() {
        mEdgeEffect.onRelease();
        return mEdgeEffect.isFinished();
    }

    @Deprecated
    public boolean onAbsorb(int velocity) {
        mEdgeEffect.onAbsorb(velocity);
        return true;
    }

    @Deprecated
    public boolean draw(Canvas canvas) {
        return mEdgeEffect.draw(canvas);
    }

    @RequiresApi(31)
    private static class Api31Impl {
        private Api31Impl() {}

        @DoNotInline
        public static EdgeEffect create(Context context, AttributeSet attrs) {
            try {
                return new EdgeEffect(context, attrs);
            } catch (Throwable t) {
                return new EdgeEffect(context);
            }
        }

        @DoNotInline
        public static float onPullDistance(EdgeEffect edgeEffect, float deltaDistance, float displacement) {
            try {
                return edgeEffect.onPullDistance(deltaDistance, displacement);
            } catch (Throwable t) {
                edgeEffect.onPull(deltaDistance, displacement);
                return 0;
            }
        }

        @DoNotInline
        public static float getDistance(EdgeEffect edgeEffect) {
            try {
                return edgeEffect.getDistance();
            } catch (Throwable t) {
                return 0;
            }
        }
    }
}
