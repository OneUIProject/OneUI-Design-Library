package androidx.reflect.view;

import android.os.Build;
import android.view.View;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Method;

public class SeslDecorViewReflector {
    private SeslDecorViewReflector() {
    }

    public static void semSetForceHideRoundedCorner(View view, boolean z) {
        Method declaredMethod;
        if (Build.VERSION.SDK_INT >= 30 && (declaredMethod = SeslBaseReflector.getDeclaredMethod(view.getClass(), "hidden_semSetForceHideRoundedCorner", Boolean.TYPE)) != null) {
            SeslBaseReflector.invoke(view, declaredMethod, Boolean.valueOf(z));
        }
    }
}
