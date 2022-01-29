package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SeslSpinner;

import de.dlyt.yanndroid.oneui.R;

public class DropDownPreference extends ListPreference {
    private final Context mContext;
    private final ArrayAdapter mAdapter;
    private SeslSpinner mSpinner;

    private final OnItemSelectedListener mItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            if (position >= 0) {
                String value = getEntryValues()[position].toString();
                if (!value.equals(getValue()) && callChangeListener(value)) {
                    setValue(value);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    public DropDownPreference(@NonNull Context context) {
        this(context, null);
    }

    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.dropdownPreferenceStyle);
    }

    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public DropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mAdapter = createAdapter();

        updateEntries();
    }

    @Override
    protected void onClick() {
        mSpinner.performClick();
    }

    @Override
    public void setEntries(@NonNull CharSequence[] entries) {
        super.setEntries(entries);
        updateEntries();
    }

    @NonNull
    protected ArrayAdapter createAdapter() {
        return new ArrayAdapter<>(mContext, R.layout.sesl_simple_spinner_dropdown_item);
    }

    @SuppressWarnings("unchecked")
    private void updateEntries() {
        mAdapter.clear();
        if (getEntries() != null) {
            for (CharSequence c : getEntries()) {
                mAdapter.add(c.toString());
            }
        }
    }

    @Override
    public void setValueIndex(int index) {
        setValue(getEntryValues()[index].toString());
    }

    @Override
    protected void notifyChanged() {
        super.notifyChanged();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mSpinner = holder.itemView.findViewById(R.id.spinner);
        mSpinner.setSoundEffectsEnabled(false);
        mSpinner.setDropDownHorizontalOffset(getContext().getResources().getDimensionPixelOffset(R.dimen.sesl_list_dropdown_item_start_padding));
        if (!mAdapter.equals(mSpinner.getAdapter())) {
            mSpinner.setAdapter((SpinnerAdapter) mAdapter);
        }
        mSpinner.setOnItemSelectedListener(mItemSelectedListener);
        mSpinner.setSelection(findSpinnerIndexOfValue(getValue()));
        super.onBindViewHolder(holder);
    }

    private int findSpinnerIndexOfValue(String value) {
        CharSequence[] entryValues = getEntryValues();
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(entryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return SeslSpinner.INVALID_POSITION;
    }

    public SeslSpinner seslGetSpinner() {
        return mSpinner;
    }
}

