package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.Switch;

public class SwitchPreferenceCompat extends TwoStatePreference {
    private final DummyClickListener mClickListener = new DummyClickListener();
    private final Listener mListener = new Listener();
    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    @SuppressLint("RestrictedApi")
    public SwitchPreferenceCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Configuration config = context.getResources().getConfiguration();
        if ((config.screenWidthDp <= 320 && config.fontScale >= FONT_SCALE_MEDIUM) || (config.screenWidthDp < 411 && config.fontScale >= FONT_SCALE_LARGE)) {
            setLayoutResource(R.layout.sesl_preference_switch_large);
        }

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchPreferenceCompat, defStyleAttr, defStyleRes);
        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreferenceCompat_summaryOn, R.styleable.SwitchPreferenceCompat_android_summaryOn));
        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreferenceCompat_summaryOff, R.styleable.SwitchPreferenceCompat_android_summaryOff));
        setSwitchTextOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreferenceCompat_switchTextOn, R.styleable.SwitchPreferenceCompat_android_switchTextOn));
        setSwitchTextOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreferenceCompat_switchTextOff, R.styleable.SwitchPreferenceCompat_android_switchTextOff));
        setDisableDependentsState(TypedArrayUtils.getBoolean(a, R.styleable.SwitchPreferenceCompat_disableDependentsState, R.styleable.SwitchPreferenceCompat_android_disableDependentsState, false));
        a.recycle();
    }

    public SwitchPreferenceCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreferenceCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceCompatStyle);
    }

    public SwitchPreferenceCompat(@NonNull Context context) {
        this(context, null);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View switchView = holder.findViewById(16908352);
        syncSwitchView(switchView);
        syncSummaryView(holder);
    }

    public void setSwitchTextOn(@Nullable CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOff(@Nullable CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    @Nullable
    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    @Nullable
    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }

    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    @Override
    protected void performClick(@NonNull View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    @SuppressLint("ResourceType")
    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }

        View switchView = view.findViewById(16908352);
        syncSwitchView(switchView);

        if (!isTalkBackIsRunning()) {
            View summaryView = view.findViewById(android.R.id.summary);
            syncSummaryView(summaryView);
        }
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
                ViewCompat.setBackground(switchView, null);
                switchView.setClickable(false);
            }
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

            SwitchPreferenceCompat.this.setChecked(isChecked);
        }
    }

    private class DummyClickListener implements View.OnClickListener {
        private DummyClickListener() {
        }

        @Override
        public void onClick(View v) {
            callClickListener();
        }
    }
}
