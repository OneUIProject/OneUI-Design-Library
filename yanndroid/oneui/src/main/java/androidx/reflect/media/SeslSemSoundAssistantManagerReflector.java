package androidx.reflect.media;

import android.content.Context;
import android.util.Log;

import androidx.reflect.SeslBaseReflector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SeslSemSoundAssistantManagerReflector {
    public static String mClassName = "com.samsung.android.media.SemSoundAssistantManager";

    public static Object getInstance(Context context) {
        Constructor<?> constructor = SeslBaseReflector.getConstructor(mClassName, Context.class);
        if (constructor == null) {
            return null;
        }
        try {
            return constructor.newInstance(context);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException unused) {
            Log.e("SeslSemSoundAssistantManagerReflector", "Failed to instantiate class");
            return null;
        }
    }

    public static void setFastAudioOpenMode(Context context, boolean z) {
        Method declaredMethod = SeslBaseReflector.getDeclaredMethod(mClassName, "setFastAudioOpenMode", Boolean.TYPE);
        Object instance = getInstance(context);
        if (declaredMethod != null && instance != null) {
            SeslBaseReflector.invoke(instance, declaredMethod, Boolean.valueOf(z));
        }
    }
}
