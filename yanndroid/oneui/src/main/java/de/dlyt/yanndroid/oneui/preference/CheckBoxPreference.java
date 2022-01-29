package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public class CheckBoxPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();

    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs,int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.CheckBoxPreference, defStyleAttr, defStyleRes);

        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.CheckBoxPreference_summaryOn,R.styleable.CheckBoxPreference_android_summaryOn));

        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.CheckBoxPreference_summaryOff,R.styleable.CheckBoxPreference_android_summaryOff));

        setDisableDependentsState(TypedArrayUtils.getBoolean(a,R.styleable.CheckBoxPreference_disableDependentsState,R.styleable.CheckBoxPreference_android_disableDependentsState, false));

        a.recycle();
    }
    public CheckBoxPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.checkBoxPreferenceStyle, android.R.attr.checkBoxPreferenceStyle));
    }

    public CheckBoxPreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        syncCheckboxView(holder.findViewById(android.R.id.checkbox));

        syncSummaryView(holder);
    }

    @Override
    protected void performClick(@NonNull View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    private void syncViewIfAccessibilityEnabled(@NonNull View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }

        View checkboxView = view.findViewById(android.R.id.checkbox);
        syncCheckboxView(checkboxView);

        if (!isTalkBackIsRunning()) {
            View summaryView = view.findViewById(android.R.id.summary);
            syncSummaryView(summaryView);
        }
    }

    private void syncCheckboxView(View view) {
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof CompoundButton) {
            ((CompoundButton) view).setOnCheckedChangeListener(mListener);
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        Listener() {}

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }
            CheckBoxPreference.this.setChecked(isChecked);
        }
    }
}
