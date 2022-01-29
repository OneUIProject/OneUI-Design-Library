package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.dlyt.yanndroid.oneui.R;

public class MultiSelectListPreference extends DialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mValues = new HashSet<>();

    @SuppressLint("RestrictedApi")
    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiSelectListPreference, defStyleAttr, defStyleRes);

        mEntries = TypedArrayUtils.getTextArray(a, R.styleable.MultiSelectListPreference_entries, R.styleable.MultiSelectListPreference_android_entries);

        mEntryValues = TypedArrayUtils.getTextArray(a, R.styleable.MultiSelectListPreference_entryValues, R.styleable.MultiSelectListPreference_android_entryValues);

        a.recycle();
    }

    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public MultiSelectListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle, android.R.attr.dialogPreferenceStyle));
    }

    public MultiSelectListPreference(@NonNull Context context) {
        this(context, null);
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    public void setEntries(@ArrayRes int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    public void setEntryValues(@ArrayRes int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public void setValues(Set<String> values) {
        mValues.clear();
        mValues.addAll(values);

        persistStringSet(values);
        notifyChanged();
    }

    public Set<String> getValues() {
        return mValues;
    }

    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(mEntryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected boolean[] getSelectedItems() {
        final CharSequence[] entries = mEntryValues;
        final int entryCount = entries.length;
        final Set<String> values = mValues;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }

        return result;
    }

    @Override
    protected @Nullable Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        final CharSequence[] defaultValues = a.getTextArray(index);
        final Set<String> result = new HashSet<>();

        for (final CharSequence defaultValue : defaultValues) {
            result.add(defaultValue.toString());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValues(getPersistedStringSet((Set<String>) defaultValue));
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.mValues = getValues();
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
        setValues(myState.mValues);
    }

    private static class SavedState extends BaseSavedState {
        Set<String> mValues;

        SavedState(Parcel source) {
            super(source);
            final int size = source.readInt();
            mValues = new HashSet<>();
            String[] strings = new String[size];
            source.readStringArray(strings);

            Collections.addAll(mValues, strings);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mValues.size());
            dest.writeStringArray(mValues.toArray(new String[mValues.size()]));
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
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
