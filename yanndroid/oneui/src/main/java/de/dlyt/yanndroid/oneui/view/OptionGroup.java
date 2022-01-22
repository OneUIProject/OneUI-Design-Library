package de.dlyt.yanndroid.oneui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.HashMap;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.OptionButton;

public class OptionGroup extends LinearLayout {

    private HashMap<Integer, Boolean> checkState = new HashMap<>();
    private HashMap<Integer, Integer> idPosition = new HashMap<>();
    private Integer selectedId = -1;

    private OnOptionButtonClickListener mOnOptionButtonClickListener;

    public OptionGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OptionGroup, 0, 0);
        selectedId = attr.getResourceId(R.styleable.OptionGroup_selectedOptionButton, -1);
        attr.recycle();
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof OptionButton) {
            final OptionButton optionButton = (OptionButton) child;

            int id = child.getId();
            if (id == View.NO_ID) {
                id = View.generateViewId();
                child.setId(id);
            }

            checkState.put(id, id == selectedId);
            idPosition.put(id, idPosition.size());

            optionButton.setButtonSelected(id == selectedId);

            optionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedId != -1) checkState.put(selectedId, false);
                    selectedId = v.getId();
                    checkState.put(selectedId, true);
                    updateCheckState();
                    if (mOnOptionButtonClickListener != null) {
                        mOnOptionButtonClickListener.onOptionButtonClick(optionButton, selectedId, idPosition.get(selectedId));
                    }
                }
            });

        }
        super.addView(child, index, params);
    }

    private void updateCheckState() {
        for (Integer id : checkState.keySet()) {
            OptionButton optionButton = findViewById(id);
            optionButton.setButtonSelected(checkState.get(id));
        }
    }

    public OptionButton getSelectedOptionButton() {
        return findViewById(selectedId);
    }

    public void setSelectedOptionButton(OptionButton optionButton) {
        if (selectedId != -1) checkState.put(selectedId, false);
        selectedId = optionButton.getId();
        checkState.put(selectedId, true);
        updateCheckState();
    }

    public void setSelectedOptionButton(int position) {
        for (Integer id : idPosition.keySet()) {
            if (idPosition.get(id) == position) {
                if (selectedId != -1) checkState.put(selectedId, false);
                selectedId = idPosition.get(id);
                checkState.put(selectedId, true);
                updateCheckState();
                break;
            }
        }
    }

    public void setSelectedOptionButton(Integer id) {
        if (selectedId != -1) checkState.put(selectedId, false);
        selectedId = id;
        checkState.put(selectedId, true);
        updateCheckState();
    }

    public void setOnOptionButtonClickListener(OnOptionButtonClickListener listener) {
        mOnOptionButtonClickListener = listener;
    }

    public interface OnOptionButtonClickListener {
        public void onOptionButtonClick(OptionButton view, int checkedId, int position);
    }

}