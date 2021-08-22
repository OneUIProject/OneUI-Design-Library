package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public class MultiSelectListPreference extends DialogPreference {
    public CharSequence[] mEntries;
    public CharSequence[] mEntryValues;
    public Set<String> mValues;

    @SuppressLint({"RestrictedApi"})
    public MultiSelectListPreference(Context var1, AttributeSet var2) {
        this(var1, var2, TypedArrayUtils.getAttr(var1, R.attr.dialogPreferenceStyle, 16842897));
    }

    public MultiSelectListPreference(Context var1, AttributeSet var2, int var3) {
        this(var1, var2, var3, 0);
    }

    @SuppressLint({"RestrictedApi"})
    public MultiSelectListPreference(Context var1, AttributeSet var2, int var3, int var4) {
        super(var1, var2, var3, var4);
        this.mValues = new HashSet();
        TypedArray var5 = var1.obtainStyledAttributes(var2, R.styleable.MultiSelectListPreference, var3, var4);
        this.mEntries = TypedArrayUtils.getTextArray(var5, R.styleable.MultiSelectListPreference_entries, R.styleable.MultiSelectListPreference_android_entries);
        this.mEntryValues = TypedArrayUtils.getTextArray(var5, R.styleable.MultiSelectListPreference_entryValues, R.styleable.MultiSelectListPreference_android_entryValues);
        var5.recycle();
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public Set<String> getValues() {
        return this.mValues;
    }

    public Object onGetDefaultValue(TypedArray var1, int var2) {
        CharSequence[] var5 = var1.getTextArray(var2);
        HashSet var3 = new HashSet();
        int var4 = var5.length;

        for(var2 = 0; var2 < var4; ++var2) {
            var3.add(var5[var2].toString());
        }

        return var3;
    }

    public void onRestoreInstanceState(Parcelable var1) {
        if (var1 != null && var1.getClass().equals(de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState.class)) {
            de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState var2 = (de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState)var1;
            super.onRestoreInstanceState(var2.getSuperState());
            this.setValues(var2.mValues);
        } else {
            super.onRestoreInstanceState(var1);
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable var1 = super.onSaveInstanceState();
        if (this.isPersistent()) {
            return var1;
        } else {
            de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState var2 = new de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState(var1);
            var2.mValues = this.getValues();
            return var2;
        }
    }

    public void onSetInitialValue(Object var1) {
        this.setValues(this.getPersistedStringSet((Set<String>)var1));
    }

    public void setValues(Set<String> var1) {
        this.mValues.clear();
        this.mValues.addAll(var1);
        this.persistStringSet(var1);
    }

    private static class SavedState extends Preference.BaseSavedState {
        public static final Creator<de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState> CREATOR = new Creator<de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState>() {
            public de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState createFromParcel(Parcel var1) {
                return new de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState(var1);
            }

            public de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState[] newArray(int var1) {
                return new de.dlyt.yanndroid.oneui.preference.MultiSelectListPreference.SavedState[var1];
            }
        };
        public Set<String> mValues;

        public SavedState(Parcel var1) {
            super(var1);
            int var2 = var1.readInt();
            this.mValues = new HashSet();
            String[] var3 = new String[var2];
            var1.readStringArray(var3);
            Collections.addAll(this.mValues, var3);
        }

        public SavedState(Parcelable var1) {
            super(var1);
        }

        public void writeToParcel(Parcel var1, int var2) {
            super.writeToParcel(var1, var2);
            var1.writeInt(this.mValues.size());
            Set var3 = this.mValues;
            var1.writeStringArray((String[])var3.toArray(new String[var3.size()]));
        }
    }
}
