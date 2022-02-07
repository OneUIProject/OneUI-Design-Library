package de.dlyt.yanndroid.oneui.utils;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import de.dlyt.yanndroid.oneui.R;

public class ThemeDynamicDrawable extends Drawable {
    private Drawable mDrawable;
    private int drawable3, drawable4;

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws IOException, XmlPullParserException {
        super.inflate(r, parser, attrs, theme);
        TypedArray attr = r.obtainAttributes(attrs, R.styleable.ThemeDynamicDrawable);
        drawable3 = attr.getResourceId(R.styleable.ThemeDynamicDrawable_drawable3, 0);
        drawable4 = attr.getResourceId(R.styleable.ThemeDynamicDrawable_drawable4, 0);
        attr.recycle();

        //render fix: theme is null in app, but not in render for some reason -> only works for render
        if (theme != null) {
            mDrawable = theme.getDrawable(theme.obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false) ? drawable4 : drawable3).mutate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme t) {
        mDrawable = t.getDrawable(t.obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false) ? drawable4 : drawable3).mutate();
        //mDrawable.applyTheme(t);
    }

    @Override
    public boolean canApplyTheme() {
        return mDrawable == null || mDrawable.canApplyTheme();
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @Override
    public void setColorFilter(int color, @NonNull PorterDuff.Mode mode) {
        mDrawable.setColorFilter(color, mode);
    }

    @Override
    public ColorFilter getColorFilter() {
        return mDrawable.getColorFilter();
    }

    @Override
    public void clearColorFilter() {
        mDrawable.clearColorFilter();
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(@NonNull Rect bounds) {
        mDrawable.setBounds(bounds);
    }

    @Override
    public void setTint(int tintColor) {
        mDrawable.setTint(tintColor);
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        mDrawable.setTintList(tint);
    }

    @Override
    public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
        mDrawable.setTintMode(tintMode);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void setTintBlendMode(@Nullable BlendMode blendMode) {
        mDrawable.setTintBlendMode(blendMode);
    }

    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mDrawable.getPadding(padding);
    }

    @Override
    public int getMinimumWidth() {
        return mDrawable.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mDrawable.getMinimumHeight();
    }


    //works without those, but some drawable features might be missing (untested)

    /*
    @Override
    public int getChangingConfigurations() {
        return mDrawable.getChangingConfigurations();
    }

    @Override
    public int getAlpha() {
        return mDrawable.getAlpha();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return mDrawable.setVisible(visible, restart);
    }

    @Override
    public boolean isStateful() {
        return mDrawable.isStateful();
    }

    @NonNull
    @Override
    public Rect getDirtyBounds() {
        return mDrawable.getDirtyBounds();
    }

    @Override
    public void setChangingConfigurations(int configs) {
        mDrawable.setChangingConfigurations(configs);
    }

    @Override
    public void setDither(boolean dither) {
        mDrawable.setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mDrawable.setFilterBitmap(filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean isFilterBitmap() {
        return mDrawable.isFilterBitmap();
    }

    @Nullable
    @Override
    public Callback getCallback() {
        return mDrawable.getCallback();
    }

    @Override
    public void invalidateSelf() {
        mDrawable.invalidateSelf();
    }

    @Override
    public void scheduleSelf(@NonNull Runnable what, long when) {
        mDrawable.scheduleSelf(what, when);
    }

    @Override
    public void unscheduleSelf(@NonNull Runnable what) {
        mDrawable.unscheduleSelf(what);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int getLayoutDirection() {
        return mDrawable.getLayoutDirection();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        return mDrawable.onLayoutDirectionChanged(layoutDirection);
    }

    @Override
    public void setHotspot(float x, float y) {
        mDrawable.setHotspot(x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        mDrawable.setHotspotBounds(left, top, right, bottom);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void getHotspotBounds(@NonNull Rect outRect) {
        mDrawable.getHotspotBounds(outRect);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public boolean isProjected() {
        return mDrawable.isProjected();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public boolean hasFocusStateSpecified() {
        return mDrawable.hasFocusStateSpecified();
    }

    @Override
    public boolean setState(@NonNull int[] stateSet) {
        return mDrawable.setState(stateSet);
    }

    @NonNull
    @Override
    public int[] getState() {
        return mDrawable.getState();
    }

    @Override
    public void jumpToCurrentState() {
        mDrawable.jumpToCurrentState();
    }

    @NonNull
    @Override
    public Drawable getCurrent() {
        return mDrawable.getCurrent();
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        mDrawable.setAutoMirrored(mirrored);
    }

    @Override
    public boolean isAutoMirrored() {
        return mDrawable.isAutoMirrored();
    }

    @Nullable
    @Override
    public Region getTransparentRegion() {
        return mDrawable.getTransparentRegion();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @NonNull
    @Override
    public Insets getOpticalInsets() {
        return mDrawable.getOpticalInsets();
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        mDrawable.getOutline(outline);
    }

    @Nullable
    @Override
    public ConstantState getConstantState() {
        return mDrawable.getConstantState();
    }
    */
}