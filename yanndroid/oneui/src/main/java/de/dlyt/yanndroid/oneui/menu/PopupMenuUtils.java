package de.dlyt.yanndroid.oneui.menu;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.reflect.provider.SeslSystemReflector;
import androidx.reflect.view.SeslSemBlurInfoReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;

public class PopupMenuUtils {
    private static boolean isReduceTransparencySettingsEnabled(Context context) {
        String SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY = SeslSystemReflector.getField_SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY();
        if (!SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY.equals("not_supported")) {
            return Settings.System.getInt(context.getContentResolver(), SEM_ACCESSIBILITY_REDUCE_TRANSPARENCY, 0) == 1;
        } else {
            return false;
        }
    }

    public static void setupBlurEffect(Context context, boolean isOneUI4, PopupWindow popupWindow, ListView listView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (popupWindow != null && popupWindow.getContentView() != null) {
                Object blurBuilder = SeslSemBlurInfoReflector.semCreateBlurBuilder(0 /* SemBlurInfo.BLUR_MODE_WINDOW */);

                if (blurBuilder != null && !isReduceTransparencySettingsEnabled(context)) {
                    SeslSemBlurInfoReflector.semSetBuilderBlurRadius(blurBuilder, 120);
                    SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundColor(blurBuilder, context.getResources().getColor(R.color.sesl_popup_menu_blur_background, context.getTheme()));
                    SeslSemBlurInfoReflector.semSetBuilderBlurBackgroundCornerRadius(blurBuilder, context.getResources().getDimension(isOneUI4 ? R.dimen.sesl4_menu_popup_corner_radius : R.dimen.sesl_menu_popup_corner_radius));
                    SeslSemBlurInfoReflector.semBuildSetBlurInfo(blurBuilder, popupWindow.getContentView());

                    if (listView != null) {
                        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                    }
                }
            }
        }
    }

    public static void fixPopupRoundedCorners(PopupWindow popupWindow) {
        ((View) ReflectUtils.genericGetField(PopupWindow.class, popupWindow, "mBackgroundView")).setClipToOutline(true);
    }
}
