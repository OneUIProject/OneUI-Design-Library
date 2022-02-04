package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import android.graphics.Canvas;
import android.view.View;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public interface ItemTouchUIUtil {
    void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive);

    void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive);

    void clearView(View view);

    void onSelected(View view);
}

