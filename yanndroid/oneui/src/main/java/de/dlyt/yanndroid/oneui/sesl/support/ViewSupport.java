package de.dlyt.yanndroid.oneui.sesl.support;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.dlyt.yanndroid.oneui.sesl.coordinatorlayout.SamsungCoordinatorLayout;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;

public class ViewSupport {
    public static void semSetRoundedCorners(View view, int roundMode) {
        ReflectUtils.genericInvokeMethod(view, "semSetRoundedCorners", roundMode);
    }

    public static void setHorizontalMargin(ViewGroup viewGroup, int margin) {
        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layoutParams;
            lp.setMargins(margin, 0, margin, 0);
            viewGroup.setLayoutParams(lp);
        } else if (layoutParams instanceof SamsungCoordinatorLayout.LayoutParams) {
            SamsungCoordinatorLayout.LayoutParams lp = (SamsungCoordinatorLayout.LayoutParams) layoutParams;
            lp.setMargins(margin, 0, margin, 0);
            viewGroup.setLayoutParams(lp);
        }
    }

    public static void setPointerIcon(View view, int type) {
        if (view != null && Build.VERSION.SDK_INT >= 24) {
            view.setPointerIcon(PointerIcon.getSystemIcon(view.getContext(), type));
        }
    }

    public static void updateListBothSideMargin(final Activity activity, final ViewGroup viewGroup) {
        if (viewGroup != null && activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
            activity.findViewById(android.R.id.content).post(new Runnable() {
                public void run() {
                    int width = activity.findViewById(android.R.id.content).getWidth();
                    Configuration configuration = activity.getResources().getConfiguration();
                    if (configuration.screenHeightDp <= 411 || configuration.screenWidthDp < 512) {
                        setHorizontalMargin(viewGroup, 0);
                        return;
                    }
                    viewGroup.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int screenWidthDp = configuration.screenWidthDp;
                    if (screenWidthDp < 685 || screenWidthDp > 959) {
                        if (screenWidthDp >= 960 && screenWidthDp <= 1919) {
                            int i = (int) (((float) width) * 0.125f);
                            setHorizontalMargin(viewGroup, i);
                        } else if (configuration.screenWidthDp >= 1920) {
                            int i = (int) (((float) width) * 0.25f);
                            setHorizontalMargin(viewGroup, i);
                        } else {
                            setHorizontalMargin(viewGroup, 0);
                        }
                    } else {
                        int i = (int) (((float) width) * 0.05f);
                        setHorizontalMargin(viewGroup, i);
                    }
                }
            });
        }
    }
}
