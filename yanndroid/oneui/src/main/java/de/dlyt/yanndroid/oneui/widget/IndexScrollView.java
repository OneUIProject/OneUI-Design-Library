package de.dlyt.yanndroid.oneui.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.List;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.sesl.indexscroll.SeslArrayIndexer;
import de.dlyt.yanndroid.oneui.sesl.indexscroll.SeslIndexScrollView;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.GridLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class IndexScrollView extends SeslIndexScrollView {
    public IndexScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void syncWithRecyclerView(RecyclerView recyclerView, List<String> itemNames, boolean useLetters) {
        recyclerView.seslSetFastScrollerEnabled(false);
        recyclerView.setVerticalScrollBarEnabled(false);
        attachToRecyclerView(recyclerView);
        setIndexBarTextMode(useLetters);
        setOnIndexBarEventListener(new SeslIndexScrollView.OnIndexBarEventListener() {
            @Override
            public void onIndexChanged(int i) {
                recyclerView.stopScroll();
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager != null) {
                    if (manager instanceof LinearLayoutManager)
                        ((LinearLayoutManager) manager).scrollToPositionWithOffset(i, 0);
                    if (manager instanceof GridLayoutManager)
                        ((GridLayoutManager) manager).scrollToPositionWithOffset(i, 0);
                    if (manager instanceof StaggeredGridLayoutManager)
                        ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(i, 0);
                }
            }

            @Override
            public void onPressed(float f) {
            }

            @Override
            public void onReleased(float f) {

            }
        });

        StringBuilder sections = new StringBuilder();
        for (String itemName : itemNames) {
            char sectionChar = itemName.toUpperCase(Locale.ROOT).charAt(0);
            if (sections.length() == 0 || sectionChar != sections.charAt(sections.length() - 1))
                sections.append(sectionChar);
        }
        setIndexer(new SeslArrayIndexer(itemNames, sections.toString()));
    }
}
