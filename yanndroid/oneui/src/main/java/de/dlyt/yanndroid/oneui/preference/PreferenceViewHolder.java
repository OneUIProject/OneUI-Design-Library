package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class PreferenceViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mCachedViews = new SparseArray<>(4);
    private boolean mDividerAllowedAbove;
    private boolean mDividerAllowedBelow;
    private int mDrawCorners;
    private boolean mDrawBackground = false;
    private boolean mSubheaderRound = false;

    @SuppressLint("ResourceType")
    PreferenceViewHolder(@NonNull View itemView) {
        super(itemView);

        final TextView titleView = itemView.findViewById(android.R.id.title);

        mCachedViews.put(android.R.id.title, titleView);
        mCachedViews.put(android.R.id.summary, itemView.findViewById(android.R.id.summary));
        mCachedViews.put(android.R.id.icon, itemView.findViewById(android.R.id.icon));
        mCachedViews.put(R.id.icon_frame, itemView.findViewById(R.id.icon_frame));
        mCachedViews.put(16908350, itemView.findViewById(16908350));
    }

    @NonNull
    public static PreferenceViewHolder createInstanceForTests(@NonNull View itemView) {
        return new PreferenceViewHolder(itemView);
    }

    public View findViewById(@IdRes int id) {
        final View cachedView = mCachedViews.get(id);
        if (cachedView != null) {
            return cachedView;
        } else {
            final View v = itemView.findViewById(id);
            if (v != null) {
                mCachedViews.put(id, v);
            }
            return v;
        }
    }

    public boolean isDividerAllowedAbove() {
        return mDividerAllowedAbove;
    }

    public void setDividerAllowedAbove(boolean allowed) {
        mDividerAllowedAbove = allowed;
    }

    public boolean isDividerAllowedBelow() {
        return mDividerAllowedBelow;
    }

    public void setDividerAllowedBelow(boolean allowed) {
        mDividerAllowedBelow = allowed;
    }

    void setPreferenceBackgroundType(boolean drawBackground, int drawCorners, boolean subheaderRound) {
        mDrawBackground = drawBackground;
        mDrawCorners = drawCorners;
        mSubheaderRound = subheaderRound;
    }

    int getDrawCorners() {
        return mDrawCorners;
    }

    boolean isBackgroundDrawn() {
        return mDrawBackground;
    }

    boolean isDrawSubheaderRound() {
        return mSubheaderRound;
    }
}
