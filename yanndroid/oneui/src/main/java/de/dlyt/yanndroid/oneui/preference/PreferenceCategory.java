package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import de.dlyt.yanndroid.oneui.R;

public class PreferenceCategory extends PreferenceGroup {
    private static final String TAG = "SeslPreferenceCategory";
    private String mHeader;

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHeader = "Header";
    }

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceCategoryStyle, android.R.attr.preferenceCategoryStyle));

        if (Build.VERSION.SDK_INT < 30) {
            try {
                mHeader = context.getString(R.string.sesl_preferencecategory_added_title);
            } catch (Exception | NoSuchFieldError e) {
                Log.d(TAG, "Can not find the string. Please updates latest sesl-appcompat library, ", e);
            }
        }
    }

    public PreferenceCategory(@NonNull Context context) {
        this(context, null);
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
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (Build.VERSION.SDK_INT >= VERSION_CODES.P) {
            holder.itemView.setAccessibilityHeading(true);
        } else if (Build.VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            final TypedValue value = new TypedValue();
            if (!getContext().getTheme().resolveAttribute(R.attr.colorAccent, value, true)) {
                return;
            }
            final TextView titleView = (TextView) holder.findViewById(android.R.id.title);
            if (titleView == null) {
                return;
            }
            final int fallbackColor = ContextCompat.getColor(getContext(), R.color.preference_fallback_accent_color);
            if (titleView.getCurrentTextColor() != fallbackColor) {
                return;
            }
            titleView.setTextColor(value.data);
        }
    }

    @Override
    @Deprecated
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(info);

        if (Build.VERSION.SDK_INT < 28) {
            AccessibilityNodeInfoCompat.CollectionItemInfoCompat collectionItemInfo = info.getCollectionItemInfo();

            if (collectionItemInfo == null) {
                return;
            }

            info.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(collectionItemInfo.getRowIndex(), collectionItemInfo.getRowSpan(), collectionItemInfo.getColumnIndex(), collectionItemInfo.getColumnSpan(), true, collectionItemInfo.isSelected()));
        }

        if (Build.VERSION.SDK_INT < 30) {
            info.setRoleDescription(mHeader);
        }
    }
}
