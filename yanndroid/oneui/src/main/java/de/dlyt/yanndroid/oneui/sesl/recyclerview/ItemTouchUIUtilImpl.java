package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.graphics.Canvas;
import android.os.Build;
import android.view.View;

import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

class ItemTouchUIUtilImpl implements ItemTouchUIUtil {
    static final ItemTouchUIUtil INSTANCE =  new ItemTouchUIUtilImpl();

    @Override
    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (isCurrentlyActive) {
                Object originalElevation = view.getTag(R.id.item_touch_helper_previous_elevation);
                if (originalElevation == null) {
                    originalElevation = ViewCompat.getElevation(view);
                    float newElevation = 1f + findMaxElevation(recyclerView, view);
                    ViewCompat.setElevation(view, newElevation);
                    view.setTag(R.id.item_touch_helper_previous_elevation, originalElevation);
                }
            }
        }

        view.setTranslationX(dX);
        view.setTranslationY(dY);
    }

    private static float findMaxElevation(RecyclerView recyclerView, View itemView) {
        final int childCount = recyclerView.getChildCount();
        float max = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = recyclerView.getChildAt(i);
            if (child == itemView) {
                continue;
            }
            final float elevation = ViewCompat.getElevation(child);
            if (elevation > max) {
                max = elevation;
            }
        }
        return max;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    }

    @Override
    public void clearView(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Object tag = view.getTag(R.id.item_touch_helper_previous_elevation);
            if (tag instanceof Float) {
                ViewCompat.setElevation(view, (Float) tag);
            }
            view.setTag(R.id.item_touch_helper_previous_elevation, null);
        }

        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }

    @Override
    public void onSelected(View view) {
    }
}
