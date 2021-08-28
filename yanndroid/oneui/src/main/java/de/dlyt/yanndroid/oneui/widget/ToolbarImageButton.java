package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;

import de.dlyt.yanndroid.oneui.R;

public class ToolbarImageButton extends AppCompatImageButton {
    private boolean mIcon;
    private CharSequence mToolTipText;

    public ToolbarImageButton(Context context) {
        super(context);
    }

    public ToolbarImageButton(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ToolbarImageButton(Context context, AttributeSet attr, int defStyleAttr) {
        super(context, attr, defStyleAttr);
        mIcon = getDrawable() != null;

        TypedArray attrs = context.getTheme().obtainStyledAttributes(attr, R.styleable.ToolbarImageButton, defStyleAttr, 0);
        mToolTipText = attrs.getString(R.styleable.ToolbarImageButton_seslTooltipText);
        if (mToolTipText != null && !((String) mToolTipText).isEmpty())
            setTooltipText(mToolTipText);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Tooltip.seslSetTooltipNull(false);
        if (mIcon) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_MOVE:
                case MotionEvent.ACTION_HOVER_ENTER:
                    Tooltip.setTooltipText(this, mToolTipText);
                    Tooltip.seslSetTooltipForceBelow(true);
                    Tooltip.seslSetTooltipForceActionBarPosX(true);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    Tooltip.seslSetTooltipNull(true);
                    Tooltip.seslSetTooltipForceBelow(false);
                    Tooltip.seslSetTooltipForceActionBarPosX(false);
                    break;
            }
        }
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        Tooltip.seslSetTooltipNull(!hovered);
        super.onHoverChanged(hovered);
    }

    @Override
    public boolean performLongClick() {
        if (mIcon) {
            return super.performLongClick();
        } else {
            Tooltip.seslSetTooltipNull(true);
            return true;
        }
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mIcon = true;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        mIcon = true;
    }

    @Override
    public void setImageTintList(@Nullable ColorStateList tint) {
        super.setImageTintList(tint);
    }

    @Override
    public void setTooltipText(CharSequence text) {
        Tooltip.setTooltipText(this, text);
        mToolTipText = text;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        return PointerIcon.getSystemIcon(getContext(), PointerIcon.TYPE_ARROW);
    }

}
