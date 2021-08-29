package de.dlyt.yanndroid.oneui.sesl.colorpicker;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class SpenPickerTabGroup {
    private final int mDefaultId = 0;
    private View mSelectView = null;
    private OnTabSelectedListener mSelectedListener;
    private final View.OnClickListener mChildClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            SpenPickerTabGroup.this.select(view, true);
        }
    };
    private List<View> mTabs = new ArrayList();

    public void close() {
        this.mTabs = null;
        this.mSelectView = null;
        this.mSelectedListener = null;
    }

    public void addTab(View view) {
        if (view != null) {
            addTab(view, false);
        }
    }

    public void addTab(View view, boolean z) {
        List<View> list = this.mTabs;
        if (list != null) {
            addTab(view, list.size(), z);
        }
    }

    public void addTab(View view, int i) {
        addTab(view, i, false);
    }

    public void addTab(View view, int i, boolean z) {
        List<View> list = this.mTabs;
        if (list != null && view != null) {
            list.add(i, view);
            if (z) {
                View view2 = this.mSelectView;
                if (view2 != null) {
                    view2.setSelected(false);
                }
                this.mSelectView = view;
            }
            view.setSelected(z);
            view.setOnClickListener(this.mChildClickListener);
        }
    }

    public void select(int i) {
        View child = getChild(i);
        if (child != null) {
            select(child, false);
        }
    }

    private void select(View view, boolean z) {
        OnTabSelectedListener onTabSelectedListener;
        OnTabSelectedListener onTabSelectedListener2;
        OnTabSelectedListener onTabSelectedListener3;
        if (view != null) {
            View view2 = this.mSelectView;
            if (view2 != null) {
                if (view2 == view) {
                    if (z && (onTabSelectedListener3 = this.mSelectedListener) != null) {
                        onTabSelectedListener3.onTabReselected(this.mSelectView);
                        return;
                    }
                    return;
                }
                view2.setSelected(false);
                if (z && (onTabSelectedListener2 = this.mSelectedListener) != null) {
                    onTabSelectedListener2.onTabUnselected(this.mSelectView);
                }
            }
            view.setSelected(true);
            if (z && (onTabSelectedListener = this.mSelectedListener) != null) {
                onTabSelectedListener.onTabSelected(view);
            }
            this.mSelectView = view;
        }
    }

    public int getSelectId() {
        View view = this.mSelectView;
        if (view != null) {
            return view.getId();
        }
        return 0;
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.mSelectedListener = onTabSelectedListener;
    }

    private View getChild(int i) {
        List<View> list = this.mTabs;
        if (list == null) {
            return null;
        }
        for (View view : list) {
            if (view.getId() == i) {
                return view;
            }
        }
        return null;
    }

    public interface OnTabSelectedListener {
        void onTabReselected(View view);

        void onTabSelected(View view);

        void onTabUnselected(View view);
    }
}
