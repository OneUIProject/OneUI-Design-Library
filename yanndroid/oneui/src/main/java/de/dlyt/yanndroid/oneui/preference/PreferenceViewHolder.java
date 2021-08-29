package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.view.View;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class PreferenceViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mCachedViews = new SparseArray<>(4);
    boolean mDrawBackground = false;
    int mDrawCorners;
    boolean mSubheaderRound = false;
    private boolean mDividerAllowedAbove;
    private boolean mDividerAllowedBelow;

    @SuppressLint("ResourceType")
    PreferenceViewHolder(View itemView) {
        super(itemView);

        mCachedViews.put(android.R.id.title, itemView.findViewById(android.R.id.title));
        mCachedViews.put(android.R.id.summary, itemView.findViewById(android.R.id.summary));
        mCachedViews.put(android.R.id.icon, itemView.findViewById(android.R.id.icon));
        mCachedViews.put(R.id.icon_frame, itemView.findViewById(R.id.icon_frame));
        mCachedViews.put(0x102003e /*AndroidResources.ANDROID_R_ICON_FRAME*/, itemView.findViewById(0x102003e /*AndroidResources.ANDROID_R_ICON_FRAME*/));
    }

    public View findViewById(int id) {
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

    void seslSetPreferenceBackgroundType(boolean draw, int corners, boolean subheaderRound) {
        mDrawBackground = draw;
        mDrawCorners = corners;
        mSubheaderRound = subheaderRound;
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

    public int seslGetDrawCorners() {
        return mDrawCorners;
    }

    public boolean seslIsBackgroundDrawn() {
        return this.mDrawBackground;
    }

    public boolean seslIsDrawSubheaderRound() {
        return mSubheaderRound;
    }
}
