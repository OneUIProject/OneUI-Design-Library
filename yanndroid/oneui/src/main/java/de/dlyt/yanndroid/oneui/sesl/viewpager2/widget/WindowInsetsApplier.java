package de.dlyt.yanndroid.oneui.sesl.viewpager2.widget;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.dlyt.yanndroid.oneui.view.RecyclerView;

public final class WindowInsetsApplier implements OnApplyWindowInsetsListener {
    private WindowInsetsApplier() {
    }

    public static boolean install(@NonNull SeslViewPager2 viewPager) {
        ApplicationInfo appInfo = viewPager.getContext().getApplicationInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && appInfo.targetSdkVersion >= Build.VERSION_CODES.R) {
            return false;
        }
        ViewCompat.setOnApplyWindowInsetsListener(viewPager, new WindowInsetsApplier());
        return true;
    }

    @NonNull
    @Override
    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
        SeslViewPager2 viewPager = (SeslViewPager2) v;

        final WindowInsetsCompat applied = ViewCompat.onApplyWindowInsets(viewPager, insets);

        if (applied.isConsumed()) {
            return applied;
        }

        final RecyclerView rv = viewPager.mRecyclerView;
        for (int i = 0, count = rv.getChildCount(); i < count; i++) {
            ViewCompat.dispatchApplyWindowInsets(rv.getChildAt(i), new WindowInsetsCompat(applied));
        }

        return consumeAllInsets(applied);
    }

    private WindowInsetsCompat consumeAllInsets(@NonNull WindowInsetsCompat insets) {
        if (Build.VERSION.SDK_INT >= 21) {
            if (WindowInsetsCompat.CONSUMED.toWindowInsets() != null) {
                return WindowInsetsCompat.CONSUMED;
            }
            return insets.consumeSystemWindowInsets().consumeStableInsets();
        }
        return insets;
    }
}
