package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.R;
import androidx.core.view.ViewCompat;

public class SeslAppCompatBackgroundHelper {

    private final View mView;
    private final AppCompatDrawableManager mDrawableManager;

    private int mBackgroundResId = -1;

    private TintInfo mInternalBackgroundTint;
    private TintInfo mBackgroundTint;
    private TintInfo mTmpInfo;

    @SuppressLint("RestrictedApi")
    public SeslAppCompatBackgroundHelper(View view) {
        mView = view;
        mDrawableManager = AppCompatDrawableManager.get();
    }

    @SuppressLint("RestrictedApi")
    public void loadFromAttributes(AttributeSet attrs, int defStyleAttr) {
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(mView.getContext(), attrs,
                R.styleable.ViewBackgroundHelper, defStyleAttr, 0);
        try {
            if (a.hasValue(R.styleable.ViewBackgroundHelper_android_background)) {
                mBackgroundResId = a.getResourceId(
                        R.styleable.ViewBackgroundHelper_android_background, -1);
                ColorStateList tint = mDrawableManager
                        .getTintList(mView.getContext(), mBackgroundResId);
                if (tint != null) {
                    setInternalBackgroundTint(tint);
                }
            }
            if (a.hasValue(R.styleable.ViewBackgroundHelper_backgroundTint)) {
                ViewCompat.setBackgroundTintList(mView,
                        a.getColorStateList(R.styleable.ViewBackgroundHelper_backgroundTint));
            }
            if (a.hasValue(R.styleable.ViewBackgroundHelper_backgroundTintMode)) {
                ViewCompat.setBackgroundTintMode(mView,
                        DrawableUtils.parseTintMode(
                                a.getInt(R.styleable.ViewBackgroundHelper_backgroundTintMode, -1),
                                null));
            }
        } finally {
            a.recycle();
        }
    }

    @SuppressLint("RestrictedApi")
    public void onSetBackgroundResource(int resId) {
        mBackgroundResId = resId;
        // Update the default background tint
        setInternalBackgroundTint(mDrawableManager != null
                ? mDrawableManager.getTintList(mView.getContext(), resId)
                : null);
        applySupportBackgroundTint();
    }

    public void onSetBackgroundDrawable(Drawable background) {
        mBackgroundResId = -1;
        // We don't know that this drawable is, so we need to clear the default background tint
        setInternalBackgroundTint(null);
        applySupportBackgroundTint();
    }

    public ColorStateList getSupportBackgroundTintList() {
        return mBackgroundTint != null ? mBackgroundTint.mTintList : null;
    }

    @SuppressLint("RestrictedApi")
    public void setSupportBackgroundTintList(ColorStateList tint) {
        if (mBackgroundTint == null) {
            mBackgroundTint = new TintInfo();
        }
        mBackgroundTint.mTintList = tint;
        mBackgroundTint.mHasTintList = true;
        applySupportBackgroundTint();
    }

    public PorterDuff.Mode getSupportBackgroundTintMode() {
        return mBackgroundTint != null ? mBackgroundTint.mTintMode : null;
    }

    @SuppressLint("RestrictedApi")
    public void setSupportBackgroundTintMode(PorterDuff.Mode tintMode) {
        if (mBackgroundTint == null) {
            mBackgroundTint = new TintInfo();
        }
        mBackgroundTint.mTintMode = tintMode;
        mBackgroundTint.mHasTintMode = true;

        applySupportBackgroundTint();
    }

    @SuppressLint("RestrictedApi")
    public void applySupportBackgroundTint() {
        final Drawable background = mView.getBackground();
        if (background != null) {
            if (shouldApplyFrameworkTintUsingColorFilter()
                    && applyFrameworkTintUsingColorFilter(background)) {
                // This needs to be called before the internal tints below so it takes
                // effect on any widgets using the compat tint on API 21 (EditText)
                return;
            }

            if (mBackgroundTint != null) {
                AppCompatDrawableManager.tintDrawable(background, mBackgroundTint,
                        mView.getDrawableState());
            } else if (mInternalBackgroundTint != null) {
                AppCompatDrawableManager.tintDrawable(background, mInternalBackgroundTint,
                        mView.getDrawableState());
            }
        }
    }

    @SuppressLint("RestrictedApi")
    public void setInternalBackgroundTint(ColorStateList tint) {
        if (tint != null) {
            if (mInternalBackgroundTint == null) {
                mInternalBackgroundTint = new TintInfo();
            }
            mInternalBackgroundTint.mTintList = tint;
            mInternalBackgroundTint.mHasTintList = true;
        } else {
            mInternalBackgroundTint = null;
        }
        applySupportBackgroundTint();
    }

    private boolean shouldApplyFrameworkTintUsingColorFilter() {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk > 21) {
            // On API 22+, if we're using an internal compat background tint, we're also
            // responsible for applying any custom tint set via the framework impl
            return mInternalBackgroundTint != null;
        } else if (sdk == 21) {
            // GradientDrawable doesn't implement setTintList on API 21, and since there is
            // no nice way to unwrap DrawableContainers we have to blanket apply this
            // on API 21
            return true;
        } else {
            // API 19 and below doesn't have framework tint
            return false;
        }
    }

    /**
     * Applies the framework background tint to a view, but using the compat method (ColorFilter)
     *
     * @return true if a tint was applied
     */
    @SuppressLint("RestrictedApi")
    private boolean applyFrameworkTintUsingColorFilter(@NonNull Drawable background) {
        if (mTmpInfo == null) {
            mTmpInfo = new TintInfo();
        }
        final TintInfo info = mTmpInfo;
        info.clear();

        final ColorStateList tintList = ViewCompat.getBackgroundTintList(mView);
        if (tintList != null) {
            info.mHasTintList = true;
            info.mTintList = tintList;
        }
        final PorterDuff.Mode mode = ViewCompat.getBackgroundTintMode(mView);
        if (mode != null) {
            info.mHasTintMode = true;
            info.mTintMode = mode;
        }

        if (info.mHasTintList || info.mHasTintMode) {
            AppCompatDrawableManager.tintDrawable(background, info, mView.getDrawableState());
            return true;
        }

        return false;
    }
}
