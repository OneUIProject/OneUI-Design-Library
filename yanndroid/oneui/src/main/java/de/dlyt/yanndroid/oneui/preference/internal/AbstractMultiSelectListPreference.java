package de.dlyt.yanndroid.oneui.preference.internal;

import android.content.Context;
import android.util.AttributeSet;
import java.util.Set;

import de.dlyt.yanndroid.oneui.preference.DialogPreference;

public abstract class AbstractMultiSelectListPreference extends DialogPreference {
    public abstract CharSequence[] getEntries();

    public abstract CharSequence[] getEntryValues();

    public abstract Set<String> getValues();

    public abstract void setValues(Set<String> set);

    public AbstractMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
