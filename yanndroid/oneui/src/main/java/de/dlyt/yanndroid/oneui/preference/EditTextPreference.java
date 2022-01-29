package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public class EditTextPreference extends DialogPreference {
    private String mText;
    @Nullable
    private OnBindEditTextListener mOnBindEditTextListener;

    @SuppressLint("RestrictedApi")
    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);

        if (TypedArrayUtils.getBoolean(a, R.styleable.EditTextPreference_useSimpleSummaryProvider, R.styleable.EditTextPreference_useSimpleSummaryProvider, false)) {
            setSummaryProvider(SimpleSummaryProvider.getInstance());
        }

        a.recycle();
    }

    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public EditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.editTextPreferenceStyle, android.R.attr.editTextPreferenceStyle));
    }

    public EditTextPreference(@NonNull Context context) {
        this(context, null);
    }

    public void setText(@Nullable String text) {
        final boolean wasBlocking = shouldDisableDependents();

        mText = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    @Nullable
    public String getText() {
        return mText;
    }

    @Override
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setText(getPersistedString((String) defaultValue));
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.mText = getText();
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
        setText(myState.mText);
    }

    public void setOnBindEditTextListener(@Nullable OnBindEditTextListener onBindEditTextListener) {
        mOnBindEditTextListener = onBindEditTextListener;
    }

    @Nullable OnBindEditTextListener getOnBindEditTextListener() {
        return mOnBindEditTextListener;
    }

    public interface OnBindEditTextListener {
        void onBindEditText(@NonNull EditText editText);
    }

    private static class SavedState extends BaseSavedState {
        String mText;

        SavedState(Parcel source) {
            super(source);
            mText = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mText);
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

    public static final class SimpleSummaryProvider implements SummaryProvider<EditTextPreference> {
        private static SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

        @NonNull
        public static SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }

        @Nullable
        @Override
        public CharSequence provideSummary(@NonNull EditTextPreference preference) {
            if (TextUtils.isEmpty(preference.getText())) {
                return (preference.getContext().getString(R.string.not_set));
            } else {
                return preference.getText();
            }
        }
    }

}
