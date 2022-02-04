package de.dlyt.yanndroid.oneui.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;

public class PreferenceManager {
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private final Context mContext;
    private long mNextId = 0;
    @Nullable
    private SharedPreferences mSharedPreferences;
    @Nullable
    private PreferenceDataStore mPreferenceDataStore;
    @Nullable
    private SharedPreferences.Editor mEditor;
    private boolean mNoCommit;
    private String mSharedPreferencesName;
    private int mSharedPreferencesMode;
    private int mStorage = STORAGE_DEFAULT;
    private PreferenceScreen mPreferenceScreen;
    private PreferenceComparisonCallback mPreferenceComparisonCallback;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private OnDisplayPreferenceDialogListener mOnDisplayPreferenceDialogListener;
    private OnNavigateToScreenListener mOnNavigateToScreenListener;

    public PreferenceManager(@NonNull Context context) {
        mContext = context;

        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    public static SharedPreferences getDefaultSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    public static void setDefaultValues(@NonNull Context context, int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain);
    }

    public static void setDefaultValues(@NonNull Context context, String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain) {
        final SharedPreferences defaultValueSp = context.getSharedPreferences(KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);

        if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            final PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);

            defaultValueSp.edit().putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true).apply();
        }
    }

    @NonNull
    public PreferenceScreen inflateFromResource(@NonNull Context context, int resId, @Nullable PreferenceScreen rootPreferences) {
        setNoCommit(true);

        final PreferenceInflater inflater = new PreferenceInflater(context, this);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(this);

        setNoCommit(false);

        return rootPreferences;
    }

    @NonNull
    public PreferenceScreen createPreferenceScreen(@NonNull Context context) {
        final PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    long getNextId() {
        synchronized (this) {
            return mNextId++;
        }
    }

    public String getSharedPreferencesName() {
        return mSharedPreferencesName;
    }

    public void setSharedPreferencesName(String sharedPreferencesName) {
        mSharedPreferencesName = sharedPreferencesName;
        mSharedPreferences = null;
    }

    public int getSharedPreferencesMode() {
        return mSharedPreferencesMode;
    }

    public void setSharedPreferencesMode(int sharedPreferencesMode) {
        mSharedPreferencesMode = sharedPreferencesMode;
        mSharedPreferences = null;
    }

    public void setStorageDefault() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            mStorage = STORAGE_DEFAULT;
            mSharedPreferences = null;
        }
    }

    public void setStorageDeviceProtected() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            mStorage = STORAGE_DEVICE_PROTECTED;
            mSharedPreferences = null;
        }
    }

    public boolean isStorageDefault() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            return mStorage == STORAGE_DEFAULT;
        } else {
            return true;
        }
    }

    public boolean isStorageDeviceProtected() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            return mStorage == STORAGE_DEVICE_PROTECTED;
        } else {
            return false;
        }
    }

    public void setPreferenceDataStore(@Nullable PreferenceDataStore dataStore) {
        mPreferenceDataStore = dataStore;
    }

    @Nullable
    public PreferenceDataStore getPreferenceDataStore() {
        return mPreferenceDataStore;
    }

    @Nullable
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

    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Nullable
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        if (mPreferenceScreen == null) {
            return null;
        }

        return mPreferenceScreen.findPreference(key);
    }

    @Nullable
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

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @Nullable
    public PreferenceComparisonCallback getPreferenceComparisonCallback() {
        return mPreferenceComparisonCallback;
    }

    public void setPreferenceComparisonCallback(@Nullable PreferenceComparisonCallback preferenceComparisonCallback) {
        mPreferenceComparisonCallback = preferenceComparisonCallback;
    }

    @Nullable
    public OnDisplayPreferenceDialogListener getOnDisplayPreferenceDialogListener() {
        return mOnDisplayPreferenceDialogListener;
    }

    public void setOnDisplayPreferenceDialogListener(@Nullable OnDisplayPreferenceDialogListener onDisplayPreferenceDialogListener) {
        mOnDisplayPreferenceDialogListener = onDisplayPreferenceDialogListener;
    }

    public void showDialog(@NonNull Preference preference) {
        if (mOnDisplayPreferenceDialogListener != null) {
            mOnDisplayPreferenceDialogListener.onDisplayPreferenceDialog(preference);
        }
    }

    public void setOnPreferenceTreeClickListener(@Nullable OnPreferenceTreeClickListener listener) {
        mOnPreferenceTreeClickListener = listener;
    }

    @Nullable
    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return mOnPreferenceTreeClickListener;
    }

    public void setOnNavigateToScreenListener(@Nullable OnNavigateToScreenListener listener) {
        mOnNavigateToScreenListener = listener;
    }

    @Nullable
    public OnNavigateToScreenListener getOnNavigateToScreenListener() {
        return mOnNavigateToScreenListener;
    }

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(@NonNull Preference preference);
    }

    public interface OnDisplayPreferenceDialogListener {
        void onDisplayPreferenceDialog(@NonNull Preference preference);
    }

    public interface OnNavigateToScreenListener {
        void onNavigateToScreen(@NonNull PreferenceScreen preferenceScreen);
    }

    public static abstract class PreferenceComparisonCallback {
        public abstract boolean arePreferenceItemsTheSame(@NonNull Preference p1, @NonNull Preference p2);

        public abstract boolean arePreferenceContentsTheSame(@NonNull Preference p1, @NonNull Preference p2);
    }

    public static class SimplePreferenceComparisonCallback extends PreferenceComparisonCallback {
        @Override
        public boolean arePreferenceItemsTheSame(@NonNull Preference p1, @NonNull Preference p2) {
            return p1.getId() == p2.getId();
        }

        @Override
        public boolean arePreferenceContentsTheSame(@NonNull Preference p1, @NonNull Preference p2) {
            if (p1.getClass() != p2.getClass()) {
                return false;
            }
            if (p1 == p2 && p1.wasDetached()) {
                return false;
            }
            if (!TextUtils.equals(p1.getTitle(), p2.getTitle())) {
                return false;
            }
            if (!TextUtils.equals(p1.getSummary(), p2.getSummary())) {
                return false;
            }
            final Drawable p1Icon = p1.getIcon();
            final Drawable p2Icon = p2.getIcon();
            if (p1Icon != p2Icon && (p1Icon == null || !p1Icon.equals(p2Icon))) {
                return false;
            }
            if (p1.isEnabled() != p2.isEnabled()) {
                return false;
            }
            if (p1.isSelectable() != p2.isSelectable()) {
                return false;
            }
            if (p1 instanceof TwoStatePreference) {
                if (((TwoStatePreference) p1).isChecked() != ((TwoStatePreference) p2).isChecked()) {
                    return false;
                }
            }
            if (p1 instanceof DropDownPreference && p1 != p2) {
                return false;
            }

            return true;
        }
    }
}
