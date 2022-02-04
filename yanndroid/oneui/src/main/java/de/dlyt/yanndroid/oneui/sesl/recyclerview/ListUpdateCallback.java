package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import androidx.annotation.Nullable;

public interface ListUpdateCallback {
    void onInserted(int position, int count);

    void onRemoved(int position, int count);

    void onMoved(int fromPosition, int toPosition);

    void onChanged(int position, int count, @Nullable Object payload);
}
