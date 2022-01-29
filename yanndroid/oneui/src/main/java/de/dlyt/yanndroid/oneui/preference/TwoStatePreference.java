package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class TwoStatePreference extends Preference {
    protected boolean mChecked;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mCheckedSet;
    private boolean mDisableDependentsState;

    public TwoStatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TwoStatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TwoStatePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoStatePreference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        super.onClick();

        final boolean newValue = !isChecked();
        if (callChangeListener(newValue)) {
            setChecked(newValue);
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        final boolean changed = mChecked != checked;
        if (changed || !mCheckedSet) {
            mChecked = checked;
            mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = mDisableDependentsState ? mChecked : !mChecked;
        return shouldDisable || super.shouldDisableDependents();
    }

    public void setSummaryOn(@Nullable CharSequence summary) {
        mSummaryOn = summary;
        if (isChecked()) {
            notifyChanged();
        }
    }

    @Nullable
    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOn(int summaryResId) {
        setSummaryOn(getContext().getString(summaryResId));
    }

    public void setSummaryOff(@Nullable CharSequence summary) {
        mSummaryOff = summary;
        if (!isChecked()) {
            notifyChanged();
        }
    }

    @Nullable
    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }

    public void setSummaryOff(int summaryResId) {
        setSummaryOff(getContext().getString(summaryResId));
    }

    public boolean getDisableDependentsState() {
        return mDisableDependentsState;
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        mDisableDependentsState = disableDependentsState;
    }

    @Override
    protected @Nullable Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = false;
        }
        setChecked(getPersistedBoolean((Boolean) defaultValue));
    }

    protected void syncSummaryView(@NonNull PreferenceViewHolder holder) {
        View view = holder.findViewById(android.R.id.summary);
        syncSummaryView(view);
    }

    protected void syncSummaryView(View view) {
        if (!(view instanceof TextView)) {
            return;
        }
        TextView summaryView = (TextView) view;
        boolean useDefaultSummary = true;
        if (mChecked && !TextUtils.isEmpty(mSummaryOn)) {
            summaryView.setText(mSummaryOn);
            useDefaultSummary = false;
        } else if (!mChecked && !TextUtils.isEmpty(mSummaryOff)) {
            summaryView.setText(mSummaryOff);
            useDefaultSummary = false;
        }
        if (useDefaultSummary) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                useDefaultSummary = false;
            }
        }
        int newVisibility = View.GONE;
        if (!useDefaultSummary) {
            newVisibility = View.VISIBLE;
        }
        if (newVisibility != summaryView.getVisibility()) {
            summaryView.setVisibility(newVisibility);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.mChecked = isChecked();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.mChecked);
    }

    static class SavedState extends BaseSavedState {
        boolean mChecked;

        SavedState(Parcel source) {
            super(source);
            mChecked = source.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mChecked ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
