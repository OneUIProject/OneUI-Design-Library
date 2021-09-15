package de.dlyt.yanndroid.oneui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.sesl.widget.PopupListView;

public class PopupMenu {

    private Context context;
    private View anchor;
    private NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    public static final int N_BADGE = -1;
    private OnMenuItemClickListener onMenuItemClickListener = item -> {
    };

    private Menu menu;
    private PopupWindow popupWindow;
    private PopupMenuAdapter popupMenuAdapter;
    private HashMap<MenuItem, Integer> overflowBadges = new HashMap<>();

    public PopupMenu(View anchor) {
        this.context = anchor.getContext();
        this.anchor = anchor;
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(MenuItem item);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        onMenuItemClickListener = listener;
    }

    public Menu getMenu() {
        return menu;
    }

    @SuppressLint("RestrictedApi")
    public void inflate(@MenuRes int menuRes) {
        menu = new MenuBuilder(context);
        MenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(menuRes, menu);

        ArrayList<MenuItem> menuItems = new ArrayList<>();

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            overflowBadges.put(item, 0);
            menuItems.add(item);
        }
        inflate(menuItems);
    }

    public void inflate(ArrayList<MenuItem> menuItems) {
        if (menuItems.isEmpty()) return;

        if (popupWindow != null) {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
            popupWindow = null;
        }
        PopupListView listView = new PopupListView(context);
        popupMenuAdapter = new PopupMenuAdapter(getActivity(), menuItems);
        listView.setAdapter(popupMenuAdapter);
        listView.setMaxHeight(context.getResources().getDimensionPixelSize(R.dimen.sesl_menu_popup_max_height));
        listView.setDivider(null);
        listView.setSelector(context.getResources().getDrawable(R.drawable.sesl_list_selector, context.getTheme()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemClickListener.onMenuItemClick((MenuItem) popupMenuAdapter.getItem(position));
            }
        });

        listView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = listView.getMeasuredHeight() + context.getResources().getDimensionPixelSize(R.dimen.sesl_popup_menu_item_bottom_padding) - 5;

        popupWindow = new PopupWindow(listView);
        popupWindow.setWidth(getPopupMenuWidth());
        popupWindow.setHeight(height);
        popupWindow.setAnimationStyle(R.style.MenuPopupAnimStyle);
        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.sesl_menu_popup_background, context.getTheme()));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 12, context.getResources().getDisplayMetrics()));
        popupWindow.setFocusable(true);
        if (popupWindow.isClippingEnabled()) {
            popupWindow.setClippingEnabled(false);
        }
        popupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_MENU || keyEvent.getAction() != KeyEvent.ACTION_UP || !popupWindow.isShowing()) {
                    return false;
                }
                popupWindow.dismiss();
                return true;
            }
        });
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_OUTSIDE) {
                    return false;
                }
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void show() {
        show(0, 0);
    }

    public void show(int xoff, int yoff) {
        popupWindow.showAsDropDown(anchor, xoff, yoff);
        ((View) ReflectUtils.genericGetField(popupWindow, "mBackgroundView")).setClipToOutline(true);
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }


    public void setMenuItemBadge(MenuItem item, Integer badge) {
        overflowBadges.put(item, badge);
        popupMenuAdapter.notifyDataSetChanged();
        popupWindow.setWidth(getPopupMenuWidth());
        if (popupWindow.isShowing()) popupWindow.dismiss();
    }

    public Integer getOverflowMenuBadge(MenuItem item) {
        return overflowBadges.get(item);
    }

    private int getPopupMenuWidth() {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = 0;

        for (int i = 0; i < popupMenuAdapter.getCount(); i++) {
            View view = popupMenuAdapter.getView(i, null, new LinearLayout(context));
            view.measure(makeMeasureSpec, makeMeasureSpec);
            int measuredWidth = view.getMeasuredWidth();
            if (measuredWidth > popupWidth) {
                popupWidth = measuredWidth;
            }
        }
        return popupWidth;
    }

    private AppCompatActivity getActivity() {
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    private class PopupMenuAdapter extends ArrayAdapter {
        Activity activity;
        ArrayList<MenuItem> overflowItems;

        public PopupMenuAdapter(Activity instance, ArrayList<MenuItem> overflowItems) {
            super(instance, 0);
            this.activity = instance;
            this.overflowItems = overflowItems;
        }

        @Override
        public int getCount() {
            return overflowItems.size();
        }

        @Override
        public Object getItem(int position) {
            return overflowItems.get(position);
        }

        @Override
        public View getView(int index, View view, ViewGroup parent) {
            TextView titleText;
            TextView badgeIcon;

            view = ((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.menu_popup_item_layout, parent, false);
            titleText = view.findViewById(R.id.more_menu_popup_title_text);
            titleText.setText(overflowItems.get(index).getTitle());

            badgeIcon = view.findViewById(R.id.more_menu_popup_badge);
            Integer badgeCount = overflowBadges.get(overflowItems.get(index));

            if (badgeCount == null) return view;

            if (badgeCount > 0) {
                int count = badgeCount;
                if (count > 99) {
                    count = 99;
                }
                String countString = numberFormat.format((long) count);
                badgeIcon.setText(countString);
                int width = (int) (context.getResources().getDimension(R.dimen.sesl_badge_default_width) + (float) countString.length() * context.getResources().getDimension(R.dimen.sesl_badge_additional_width));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) badgeIcon.getLayoutParams();
                lp.width = width;
                badgeIcon.setLayoutParams(lp);
                badgeIcon.setVisibility(View.VISIBLE);
            } else if (badgeCount == N_BADGE) {
                badgeIcon.setText("N");
                badgeIcon.setVisibility(View.VISIBLE);
            } else {
                badgeIcon.setVisibility(View.GONE);
            }

            return view;
        }
    }

}
