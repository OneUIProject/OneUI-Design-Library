package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public class PreferenceCategory extends PreferenceGroup {
    private static final String TAG = "PreferenceCategory";
    private String mHeader;

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHeader = "Header";
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceCategoryStyle, android.R.attr.preferenceCategoryStyle));
        try {
            mHeader = context.getString(R.string.sesl_preferencecategory_added_title);
        } catch (Exception e) {
            Log.d(TAG, "Can not find the string. Please updates latest sesl-appcompat library, ", e);
        }
    }

    @Override
    protected boolean onPrepareAddPreference(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            throw new IllegalArgumentException("Cannot add a " + TAG + " directly to a " + TAG);
        }

        return super.onPrepareAddPreference(preference);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return !super.isEnabled();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        if (titleView != null) {
            titleView.setContentDescription(titleView.getText().toString() + ", " + mHeader);
        }
        if (mIsSolidRoundedCorner && titleView != null) {
            titleView.setBackgroundColor(mSubheaderColor);
        }
    }
}
