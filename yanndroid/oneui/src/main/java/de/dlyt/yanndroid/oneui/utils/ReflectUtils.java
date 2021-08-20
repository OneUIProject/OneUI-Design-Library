package de.dlyt.yanndroid.oneui.utils;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static Object genericGetField(Object obj, String fieldName) {
        Field field;
        Object requiredObj = null;
        try {
            field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            requiredObj = field.get(obj);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.e("ReflectUtils.genericGetField", e.toString());
        }

        return requiredObj;
    }

    public static Object genericGetField(Class<?> cl, Object obj, String fieldName) {
        Field field;
        Object requiredObj = null;
        try {
            field = cl.getDeclaredField(fieldName);
            field.setAccessible(true);
            requiredObj = field.get(obj);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.e("ReflectUtils.genericGetField", e.toString());
        }

        return requiredObj;
    }

    public static void genericSetField(Object obj, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, fieldValue);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.e("ReflectUtils.genericSetField", e.toString());
        }
    }

    public static void genericSetField(Class<?> cl, Object obj, String fieldName, Object fieldValue) {
        Field field;
        try {
            field = cl.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, fieldValue);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            Log.e("ReflectUtils.genericSetField", e.toString());
        }
    }

    public static Object genericInvokeMethod(String className, String methodName, Object... params) {
        int paramCount = params.length;
        Method method;
        // FIX (SeslOverScroller)
        Object requiredObj = className.contains("SemPerfManager") ? false : null;
        // FIX (SeslOverScroller)
        Class<?> cl;
        Class<?>[] classArray = new Class<?>[paramCount];

        try {
            cl = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
            return requiredObj;
        }

        for (int i = 0; i < paramCount; i++) {
            // FIX
            if (params[i].getClass() == Boolean.class)
                classArray[i] = boolean.class;
            else if (params[i].getClass() == Integer.class)
                classArray[i] = int.class;
            else
                classArray[i] = params[i].getClass();
        }
        try {
            method = cl.getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(null, params);
            // FIX (SeslOverScroller)
            return className.contains("SemPerfManager") ? true : requiredObj;
            // FIX (SeslOverScroller)
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
        }

        return requiredObj;
    }

    public static Object genericInvokeMethod(Object obj, String methodName, Object... params) {
        int paramCount = params.length;
        Method method;
        Object requiredObj = null;
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            // FIX
            if (params[i].getClass() == Boolean.class)
                classArray[i] = boolean.class;
            else if (params[i].getClass() == Integer.class)
                classArray[i] = int.class;
            else
                classArray[i] = params[i].getClass();
        }
        try {
            method = obj.getClass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
        }

        return requiredObj;
    }

    public static Object genericInvokeMethod(Class<?> cl, String methodName, Object... params) {
        int paramCount = params.length;
        Method method;
        Object requiredObj = null;
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            // FIX
            if (params[i].getClass() == Boolean.class)
                classArray[i] = boolean.class;
            else if (params[i].getClass() == Integer.class)
                classArray[i] = int.class;
            else
                classArray[i] = params[i].getClass();
        }
        try {
            method = cl.getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(null, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
        }

        return requiredObj;
    }

    public static Object genericInvokeMethod(String className, Object obj, String methodName, Object... params) {
        int paramCount = params.length;
        Method method;
        Object requiredObj = null;
        Class<?> cl;
        Class<?>[] classArray = new Class<?>[paramCount];

        try {
            cl = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
            return requiredObj;
        }

        for (int i = 0; i < paramCount; i++) {
            // FIX
            if (params[i].getClass() == Boolean.class)
                classArray[i] = boolean.class;
            else if (params[i].getClass() == Integer.class)
                classArray[i] = int.class;
            else
                classArray[i] = params[i].getClass();
        }
        try {
            method = cl.getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
        }

        return requiredObj;
    }

    public static Object genericInvokeMethod(Class<?> cl, Object obj, String methodName, Object... params) {
        int paramCount = params.length;
        Method method;
        Object requiredObj = null;
        Class<?>[] classArray = new Class<?>[paramCount];
        for (int i = 0; i < paramCount; i++) {
            // FIX
            if (params[i].getClass() == Boolean.class)
                classArray[i] = boolean.class;
            else if (params[i].getClass() == Integer.class)
                classArray[i] = int.class;
            else
                classArray[i] = params[i].getClass();
        }
        try {
            method = cl.getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            requiredObj = method.invoke(obj, params);
        } catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericInvokeMethod", e.toString());
        }

        return requiredObj;
    }

    public static Object genericNewInstance(String className, Class<?> conCl, Object obj) {
        Object requiredObj = null;
        Class<?> cl;
        Constructor<?> ctor = null;
        try {
            cl = Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.e("ReflectUtils.genericNewInstance", e.toString());
            return requiredObj;
        }

        try {
            ctor = cl.getConstructor(cl, conCl);
        } catch (NoSuchMethodException e) {
            Log.e("ReflectUtils.genericNewInstance", e.toString());
        }

        try {
            requiredObj = ctor.newInstance(cl.newInstance(), obj);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e("ReflectUtils.genericNewInstance", e.toString());
        }

        return requiredObj;
    }
}
