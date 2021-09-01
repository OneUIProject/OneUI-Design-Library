package de.dlyt.yanndroid.oneui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import de.dlyt.yanndroid.oneui.R;

public class PopupMenu {

    private Context context;
    private View anchor;

    private PopupWindow popupWindow;
    private ListView listView;
    private PopupMenuAdapter popupMenuAdapter;

    public PopupMenu(View anchor) {
        this.context = anchor.getContext();
        this.anchor = anchor;
    }

    public void inflate(ArrayList<String> menu) {
        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindow = null;
        }

        listView = new ListView(context);
        popupMenuAdapter = new PopupMenuAdapter(menu);
        listView.setAdapter(popupMenuAdapter);
        listView.setDivider(null);
        listView.setSelector(R.drawable.menu_popup_list_selector);

        popupWindow = new PopupWindow(listView);
        popupWindow.setWidth(getPopupMenuWidth());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setAnimationStyle(R.style.MenuPopupAnimStyle);
        popupWindow.setBackgroundDrawable(context.getDrawable(R.drawable.sesl_menu_popup_background));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(30);
        popupWindow.setFocusable(true);
        if (popupWindow.isClippingEnabled()) {
            popupWindow.setClippingEnabled(false);
        }
    }

    public void setOnMenuItemClickListener(AdapterView.OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void show() {
        popupWindow.showAsDropDown(anchor);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }


    private class PopupMenuAdapter extends ArrayAdapter {
        ArrayList<String> itemTitle;

        public PopupMenuAdapter(ArrayList<String> itemTitle) {
            super(context, 0);
            this.itemTitle = itemTitle;
        }

        @Override
        public int getCount() {
            return itemTitle.size();
        }

        @Override
        public Object getItem(int position) {
            return itemTitle.get(position);
        }

        @Override
        public View getView(int index, View view, ViewGroup parent) {
            final TextView titleText;

            if (view == null) {
                view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_popup_item_layout, parent, false);
                titleText = view.findViewById(R.id.more_menu_popup_title_text);
                view.setTag(titleText);
            } else {
                titleText = (TextView) view.getTag();
            }

            titleText.setText(itemTitle.get(index));

            if (getCount() <= 1) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_all_round);
            } else if (index == 0) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_top_round);
            } else if (index == getCount() - 1) {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_bottom_round);
            } else {
                view.setBackgroundResource(R.drawable.menu_popup_item_bg_no_round);
            }

            return view;
        }
    }

    private int getPopupMenuWidth() {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        View view = null;
        ViewGroup viewGroup = null;
        int measuredWidth = 0;

        int i = 0;
        int count = popupMenuAdapter.getCount();

        while (i < count) {
            ViewGroup linearLayout;
            int itemViewType = popupMenuAdapter.getItemViewType(i);

            if (itemViewType != 0)
                view = null;

            if (viewGroup == null)
                linearLayout = new LinearLayout(context);
            else
                linearLayout = viewGroup;

            view = popupMenuAdapter.getView(i, view, linearLayout);
            view.measure(makeMeasureSpec, makeMeasureSpec);
            measuredWidth = view.getMeasuredWidth();
            if (measuredWidth <= 0) {
                measuredWidth = 0;
            }
            i++;
            viewGroup = linearLayout;
        }
        return measuredWidth;
    }

}
