package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.Switch;

public class SwitchPreference extends TwoStatePreference {
    private final DummyClickListener mClickListener = new DummyClickListener();
    private final Listener mListener = new Listener();
    private CharSequence mSwitchOff;
    private CharSequence mSwitchOn;

    @SuppressLint("RestrictedApi")
    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOn, R.styleable.SwitchPreference_summaryOn));
        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOff, R.styleable.SwitchPreference_summaryOff));
        setSwitchTextOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_switchTextOn, R.styleable.SwitchPreference_switchTextOn));
        setSwitchTextOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_switchTextOff, R.styleable.SwitchPreference_switchTextOff));
        setDisableDependentsState(TypedArrayUtils.getBoolean(a, R.styleable.SwitchPreference_disableDependentsState, R.styleable.SwitchPreference_disableDependentsState, false));
        a.recycle();
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceStyle);
    }

    public SwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(android.R.id.switch_widget);
        syncSwitchView(switchView);
        syncSummaryView(holder);
    }

    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    @Override
    protected void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }

        View switchView = view.findViewById(android.R.id.switch_widget);
        syncSwitchView(switchView);

        View summaryView = view.findViewById(android.R.id.summary);
        syncSummaryView(summaryView);
    }

    private void syncSwitchView(View view) {
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setTextOn(mSwitchOn);
            switchView.setTextOff(mSwitchOff);
            switchView.setOnCheckedChangeListener(mListener);
            if (switchView.isClickable()) {
                switchView.setOnClickListener(mClickListener);
            }
            if (isTalkBackIsRunning() && !(this instanceof SwitchPreferenceScreen)) {
                switchView.setBackground(null);
                switchView.setClickable(false);
            }
        }
    }


    private class DummyClickListener implements OnClickListener {
        public void onClick(View v) {
            callClickListener();
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                buttonView.setChecked(!isChecked);
                return;
            }

            de.dlyt.yanndroid.oneui.preference.SwitchPreference.this.setChecked(isChecked);
        }
    }
}
