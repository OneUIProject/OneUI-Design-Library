package de.dlyt.yanndroid.samsung.drawer;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class OptionGroup extends LinearLayout {

    private HashMap<Integer, Boolean> checkState = new HashMap<>();
    private HashMap<Integer, Integer> idPosition = new HashMap<>();
    private Integer selectedId = -1;

    private OnOptionButtonClickListener mOnOptionButtonClickListener;

    public OptionGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
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
            checkState.put(id, false);
            idPosition.put(id, idPosition.size());

            optionButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedId != -1) {
                        checkState.put(selectedId, false);
                    }
                    selectedId = v.getId();
                    checkState.put(selectedId, true);
                    updateCheckState();
                    if (mOnOptionButtonClickListener != null){
                        mOnOptionButtonClickListener.onOptionButtonClick(selectedId, idPosition.get(selectedId));
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

    public interface OnOptionButtonClickListener {
        public void onOptionButtonClick(int checkedId, int position);
    }

    public void setOnOptionButtonClickListener(OnOptionButtonClickListener listener) {
        mOnOptionButtonClickListener = listener;
    }

}