package de.dlyt.yanndroid.oneui.colorpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import de.dlyt.yanndroid.oneui.R;

public class SpenColorSwatchAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final int mItemResourceId;
    private final List<SpenColorSwatchItem> mSwatchItemList;
    private final String mSelectedString;
    int mSelectedPosition = -1;

    SpenColorSwatchAdapter(Context context, List<SpenColorSwatchItem> list, int i) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mSwatchItemList = list;
        this.mItemResourceId = i;
        this.mSelectedString = context.getResources().getString(R.string.pen_string_selected);
    }

    public long getItemId(int i) {
        return i;
    }

    private SpenColorSwatchItem getSwatchItem(int i) {
        return this.mSwatchItemList.get(i);
    }

    private int getSelectorColor(int i) {
        SpenColorSwatchItem swatchItem = getSwatchItem(i);
        if (swatchItem != null) {
            return swatchItem.getSelectorColor();
        }
        return -16777216;
    }

    public int getCount() {
        return this.mSwatchItemList.size();
    }

    public Object getItem(int i) {
        SpenColorSwatchItem swatchItem = getSwatchItem(i);
        if (swatchItem != null) {
            return Integer.valueOf(swatchItem.getColor());
        }
        return null;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        boolean z = false;
        if (view == null) {
            view = this.mInflater.inflate(this.mItemResourceId, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.mItemView = view.findViewById(R.id.swatch_item);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.mItemView.setSelectorColor(getSelectorColor(i));
        viewHolder.mItemView.setSelected(i == this.mSelectedPosition);
        viewHolder.mItemView.setBackgroundColor(((Integer) getItem(i)).intValue());
        return view;
    }

    public int getSelectedPosition() {
        return this.mSelectedPosition;
    }

    public void setSelectedPosition(int i) {
        int i2 = this.mSelectedPosition;
        this.mSelectedPosition = i;
        notifyDataSetChanged();
    }

    class ViewHolder {
        SpenColorSwatchItemView mItemView;

        ViewHolder() {
        }
    }
}
