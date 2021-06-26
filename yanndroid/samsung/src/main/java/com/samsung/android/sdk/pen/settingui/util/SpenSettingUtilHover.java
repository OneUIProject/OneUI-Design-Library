package com.samsung.android.sdk.pen.settingui.util;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

public class SpenSettingUtilHover {
    private static final String HOVER_TAG = "SupportTag";
    private static final boolean SUPPORT_TOOLTIP = (Build.VERSION.SDK_INT > 26);
    private static final String TAG = "SpenSettingUtilHover";

    public static void setHoverText(View view, CharSequence charSequence) {
        if (SUPPORT_TOOLTIP) {
            view.setTooltipText(charSequence);
        } else if (view instanceof Button) {
            ((Button) view).setContentDescription(charSequence);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            ImageButton findChildButton = findChildButton(viewGroup);
            if (charSequence != null) {
                if (findChildButton == null) {
                    findChildButton = new ImageButton(viewGroup.getContext());
                    findChildButton.setBackground(null);
                    findChildButton.setClickable(false);
                    findChildButton.setFocusable(false);
                    findChildButton.setTag(HOVER_TAG);
                    viewGroup.addView(findChildButton, new ViewGroup.LayoutParams(-1, -1));
                }
                findChildButton.setContentDescription(charSequence);
            } else if (findChildButton == null) {
            } else {
                if (findChildButton.getTag() == null || !findChildButton.getTag().equals(HOVER_TAG)) {
                    findChildButton.setContentDescription(charSequence);
                    return;
                }
                viewGroup.removeView(findChildButton);
                viewGroup.invalidate();
            }
        }
    }

    public static void setHoverText(View view, CharSequence charSequence, boolean z) {
        if (!z) {
            setHoverText(view, charSequence);
        } else if (SUPPORT_TOOLTIP) {
            view.setTooltipText(charSequence);
        }
    }

    public static CharSequence getHoverText(View view) {
        ImageButton findChildButton;
        if (SUPPORT_TOOLTIP) {
            return view.getTooltipText();
        }
        if (!(view instanceof ViewGroup) || (findChildButton = findChildButton((ViewGroup) view)) == null) {
            return null;
        }
        return findChildButton.getContentDescription();
    }

    private static ImageButton findChildButton(ViewGroup viewGroup) {
        ImageButton imageButton = null;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ImageButton) {
                if (imageButton != null) {
                    return null;
                }
                imageButton = (ImageButton) childAt;
            }
        }
        return imageButton;
    }
}
