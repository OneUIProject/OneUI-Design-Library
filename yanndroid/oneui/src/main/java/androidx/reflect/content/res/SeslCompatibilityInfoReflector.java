package androidx.reflect.content.res;

import android.content.res.Resources;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Field;

public class SeslCompatibilityInfoReflector {
    public static float getField_applicationScale(Resources resources) {
        Field field;
        Object compatibilityInfo = SeslResourcesReflector.getCompatibilityInfo(resources);
        if (compatibilityInfo == null || (field = SeslBaseReflector.getField("android.content.res.CompatibilityInfo", "applicationScale")) == null) {
            return 1.0f;
        }
        Object obj = SeslBaseReflector.get(compatibilityInfo, field);
        if (obj instanceof Integer) {
            return (float) ((Integer) obj).intValue();
        }
        return 1.0f;
    }
}
