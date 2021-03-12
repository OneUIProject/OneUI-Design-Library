package androidx.core.graphics.drawable;

import android.graphics.drawable.Drawable;

public interface WrappedDrawable {
    Drawable getWrappedDrawable();

    void setWrappedDrawable(Drawable drawable);
}
