package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

public class PreferenceManager {
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    private long mNextId = 0;
    private boolean mNoCommit;
    private OnDisplayPreferenceDialogListener mOnDisplayPreferenceDialogListener;
    private OnNavigateToScreenListener mOnNavigateToScreenListener;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private PreferenceComparisonCallback mPreferenceComparisonCallback;
    private PreferenceDataStore mPreferenceDataStore;
    private PreferenceScreen mPreferenceScreen;
    private SharedPreferences mSharedPreferences;
    private int mSharedPreferencesMode;
    private String mSharedPreferencesName;
    private int mStorage = 0;

    public PreferenceManager(Context context) {
        mContext = context;
        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    public static void setDefaultValues(Context context, int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain);
    }

    public static void setDefaultValues(Context context, String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain) {
        final SharedPreferences defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            final PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);
            defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        }
    }

    public PreferenceScreen inflateFromResource(Context context, int resId, PreferenceScreen rootPreferences) {
        setNoCommit(true);

        final PreferenceInflater inflater = new PreferenceInflater(context, this);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(this);

        setNoCommit(false);

        return rootPreferences;
    }

    long getNextId() {
        synchronized (this) {
            return mNextId++;
        }
    }

    public void setSharedPreferencesName(String sharedPreferencesName) {
        mSharedPreferencesName = sharedPreferencesName;
        mSharedPreferences = null;
    }

    public void setSharedPreferencesMode(int sharedPreferencesMode) {
        mSharedPreferencesMode = sharedPreferencesMode;
        mSharedPreferences = null;
    }

    public PreferenceDataStore getPreferenceDataStore() {
        return mPreferenceDataStore;
    }

    public SharedPreferences getSharedPreferences() {
        if (getPreferenceDataStore() != null) {
            return null;
        }

        if (mSharedPreferences == null) {
            final Context storageContext;
            switch (mStorage) {
                case STORAGE_DEVICE_PROTECTED:
                    storageContext = ContextCompat.createDeviceProtectedStorageContext(mContext);
                    break;
                default:
                    storageContext = mContext;
                    break;
            }

            mSharedPreferences = storageContext.getSharedPreferences(mSharedPreferencesName, mSharedPreferencesMode);
        }

        return mSharedPreferences;
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }

    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != mPreferenceScreen) {
            if (mPreferenceScreen != null) {
                mPreferenceScreen.onDetached();
            }
            mPreferenceScreen = preferenceScreen;
            return true;
        }

        return false;
    }

    public Preference findPreference(CharSequence key) {
        if (mPreferenceScreen == null) {
            return null;
        }

        return mPreferenceScreen.findPreference(key);
    }

    SharedPreferences.Editor getEditor() {
        if (mPreferenceDataStore != null) {
            return null;
        }

        if (mNoCommit) {
            if (mEditor == null) {
                mEditor = getSharedPreferences().edit();
            }

            return mEditor;
        } else {
            return getSharedPreferences().edit();
        }
    }

    boolean shouldCommit() {
        return !mNoCommit;
    }

    private void setNoCommit(boolean noCommit) {
        if (!noCommit && mEditor != null) {
            mEditor.apply();
        }
        mNoCommit = noCommit;
    }

    public PreferenceComparisonCallback getPreferenceComparisonCallback() {
        return mPreferenceComparisonCallback;
    }

    public void setOnDisplayPreferenceDialogListener(OnDisplayPreferenceDialogListener onDisplayPreferenceDialogListener) {
        mOnDisplayPreferenceDialogListener = onDisplayPreferenceDialogListener;
    }

    public void showDialog(Preference preference) {
        if (mOnDisplayPreferenceDialogListener != null) {
            mOnDisplayPreferenceDialogListener.onDisplayPreferenceDialog(preference);
        }
    }

    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        mOnPreferenceTreeClickListener = listener;
    }

    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return mOnPreferenceTreeClickListener;
    }

    public void setOnNavigateToScreenListener(OnNavigateToScreenListener listener) {
        mOnNavigateToScreenListener = listener;
    }

    public OnNavigateToScreenListener getOnNavigateToScreenListener() {
        return mOnNavigateToScreenListener;
    }


    public interface OnDisplayPreferenceDialogListener {
        void onDisplayPreferenceDialog(Preference preference);
    }

    public interface OnNavigateToScreenListener {
        void onNavigateToScreen(PreferenceScreen preferenceScreen);
    }

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(Preference preference);
    }

    public static abstract class PreferenceComparisonCallback {
        public abstract boolean arePreferenceContentsTheSame(Preference p1, Preference p2);

        public abstract boolean arePreferenceItemsTheSame(Preference p1, Preference p2);
    }
}
