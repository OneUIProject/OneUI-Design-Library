package de.dlyt.yanndroid.oneui.sesl.viewpager2.adapter;

import android.os.Parcelable;

import androidx.annotation.NonNull;

public interface StatefulAdapter {
    @NonNull Parcelable saveState();

    void restoreState(@NonNull Parcelable savedState);
}
