package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.AbsSavedState;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.dlyt.yanndroid.oneui.R;

public class Preference implements Comparable<Preference> {
    private static final String CLIPBOARD_ID = "Preference";
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    protected static final float FONT_SCALE_LARGE = 1.3f;
    protected static final float FONT_SCALE_MEDIUM = 1.1f;
    private static final String TAG = "SeslPreference";
    private boolean mAllowDividerAbove = true;
    private boolean mAllowDividerBelow = true;
    private boolean mBaseMethodCalled;
    private boolean mChangedSummaryColor = false;
    private boolean mChangedSummaryColorStateList = false;
    @NonNull
    private Context mContext;
    private boolean mCopyingEnabled;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet = true;
    private List<Preference> mDependents;
    private boolean mEnabled = true;
    private Bundle mExtras;
    private String mFragment;
    private boolean mHasId;
    private boolean mHasSingleLineTitleAttr;
    private Drawable mIcon;
    private int mIconResId;
    private boolean mIconSpaceReserved;
    private long mId;
    private Intent mIntent;
    private boolean mIsPreferenceRoundedBg = false;
    boolean mIsRoundChanged = false;
    private View mItemView;
    private String mKey;
    private int mLayoutResId = R.layout.sesl_preference;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private OnPreferenceCopyListener mOnCopyListener;
    private int mOrder = DEFAULT_ORDER;
    private boolean mParentDependencyMet = true;
    private PreferenceGroup mParentGroup;
    private boolean mPersistent = true;
    @Nullable
    private PreferenceDataStore mPreferenceDataStore;
    @Nullable
    private PreferenceManager mPreferenceManager;
    private boolean mRequiresKey;
    private boolean mSelectable = true;
    private boolean mShouldDisableView = true;
    private boolean mSingleLineTitle = true;
    int mSubheaderColor;
    private boolean mSubheaderRound = false;
    private CharSequence mSummary;
    private int mSummaryColor;
    private ColorStateList mSummaryColorStateList;
    private SummaryProvider mSummaryProvider;
    private ColorStateList mTextColorSecondary;
    private CharSequence mTitle;
    private int mViewId = 0;
    private boolean mVisible = true;
    private boolean mWasDetached;
    private int mWhere = 0;
    private int mWidgetLayoutResId;

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            performClick(v);
        }
    };

    @SuppressLint("RestrictedApi")
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        mIconResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_icon, R.styleable.Preference_android_icon, 0);
        mKey = TypedArrayUtils.getString(a, R.styleable.Preference_key, R.styleable.Preference_android_key);
        mTitle = TypedArrayUtils.getText(a, R.styleable.Preference_title, R.styleable.Preference_android_title);
        mSummary = TypedArrayUtils.getText(a, R.styleable.Preference_summary, R.styleable.Preference_android_summary);
        mOrder = TypedArrayUtils.getInt(a, R.styleable.Preference_order, R.styleable.Preference_android_order, DEFAULT_ORDER);
        mFragment = TypedArrayUtils.getString(a, R.styleable.Preference_fragment, R.styleable.Preference_android_fragment);
        mLayoutResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_layout, R.styleable.Preference_android_layout, R.layout.sesl_preference);
        mWidgetLayoutResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_widgetLayout, R.styleable.Preference_android_widgetLayout, 0);
        mEnabled = TypedArrayUtils.getBoolean(a, R.styleable.Preference_enabled, R.styleable.Preference_android_enabled, true);
        mSelectable = TypedArrayUtils.getBoolean(a, R.styleable.Preference_selectable, R.styleable.Preference_android_selectable, true);
        mPersistent = TypedArrayUtils.getBoolean(a, R.styleable.Preference_persistent, R.styleable.Preference_android_persistent, true);
        mDependencyKey = TypedArrayUtils.getString(a, R.styleable.Preference_dependency, R.styleable.Preference_android_dependency);
        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove, R.styleable.Preference_allowDividerAbove, mSelectable);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow, R.styleable.Preference_allowDividerBelow, mSelectable);

        if (a.hasValue(R.styleable.Preference_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_defaultValue);
        } else if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }

        mShouldDisableView = TypedArrayUtils.getBoolean(a, R.styleable.Preference_shouldDisableView, R.styleable.Preference_android_shouldDisableView, true);

        mHasSingleLineTitleAttr = a.hasValue(R.styleable.Preference_singleLineTitle);
        if (mHasSingleLineTitleAttr) {
            mSingleLineTitle = TypedArrayUtils.getBoolean(a, R.styleable.Preference_singleLineTitle, R.styleable.Preference_android_singleLineTitle, true);
        }

        mIconSpaceReserved = TypedArrayUtils.getBoolean(a, R.styleable.Preference_iconSpaceReserved, R.styleable.Preference_android_iconSpaceReserved, false);
        mVisible = TypedArrayUtils.getBoolean(a, R.styleable.Preference_isPreferenceVisible, R.styleable.Preference_isPreferenceVisible, true);
        mCopyingEnabled = TypedArrayUtils.getBoolean(a, R.styleable.Preference_enableCopying, R.styleable.Preference_enableCopying, false);

        a.recycle();

        TypedValue textColorSecondary = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, textColorSecondary, true);
        if (textColorSecondary.resourceId > 0) {
            mTextColorSecondary = context.getResources().getColorStateList(textColorSecondary.resourceId);
        }
    }

    public Preference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle, android.R.attr.preferenceStyle));
    }

    public Preference(@NonNull Context context) {
        this(context, null);
    }

    @Nullable
    protected Object onGetDefaultValue(@NonNull TypedArray a, int index) {
        return null;
    }

    public void setIntent(@Nullable Intent intent) {
        mIntent = intent;
    }

    @Nullable
    public Intent getIntent() {
        return mIntent;
    }

    public void setFragment(@Nullable String fragment) {
        mFragment = fragment;
    }

    @Nullable
    public String getFragment() {
        return mFragment;
    }

    public void setPreferenceDataStore(@Nullable PreferenceDataStore dataStore) {
        mPreferenceDataStore = dataStore;
    }

    @Nullable
    public PreferenceDataStore getPreferenceDataStore() {
        if (mPreferenceDataStore != null) {
            return mPreferenceDataStore;
        } else if (mPreferenceManager != null) {
            return mPreferenceManager.getPreferenceDataStore();
        }

        return null;
    }

    @NonNull
    public Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    @SuppressWarnings("NullableCollection")
    @Nullable
    public Bundle peekExtras() {
        return mExtras;
    }

    public void setLayoutResource(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public final int getLayoutResource() {
        return mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        mWidgetLayoutResId = widgetLayoutResId;
    }

    public final int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }

    // kang
    @SuppressLint("ResourceType")
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        View var2;
        byte var4;
        CharSequence var5;
        Integer var11;
        label113: {
            var2 = holder.itemView;
            var2.setOnClickListener(this.mClickListener);
            var2.setId(this.mViewId);
            TextView var3 = (TextView)holder.findViewById(16908304);
            var4 = View.GONE;
            if (var3 != null) {
                var5 = this.getSummary();
                if (!TextUtils.isEmpty(var5)) {
                    var3.setText(var5);
                    if (this.mChangedSummaryColor) {
                        var3.setTextColor(this.mSummaryColor);
                        Log.d("SeslPreference", "set Summary Color : " + this.mSummaryColor);
                    } else if (this.mChangedSummaryColorStateList) {
                        var3.setTextColor(this.mSummaryColorStateList);
                        Log.d("SeslPreference", "set Summary ColorStateList : " + this.mSummaryColorStateList);
                    } else {
                        ColorStateList var14 = this.mTextColorSecondary;
                        if (var14 != null) {
                            var3.setTextColor(var14);
                        }
                    }

                    var3.setVisibility(View.VISIBLE);
                    var11 = var3.getCurrentTextColor();
                    break label113;
                }

                var3.setVisibility(View.GONE);
            }

            var11 = null;
        }

        holder.setPreferenceBackgroundType(this.mIsPreferenceRoundedBg, this.mWhere, this.mSubheaderRound);
        TextView var6 = (TextView)holder.findViewById(16908310);
        if (var6 != null) {
            var5 = this.getTitle();
            if (!TextUtils.isEmpty(var5)) {
                var6.setText(var5);
                var6.setVisibility(View.VISIBLE);
                if (this.mHasSingleLineTitleAttr) {
                    var6.setSingleLine(this.mSingleLineTitle);
                }

                if (!this.isSelectable() && this.isEnabled() && var11 != null) {
                    var6.setTextColor(var11);
                }
            } else if (TextUtils.isEmpty(var5) && this instanceof PreferenceCategory) {
                var6.setVisibility(View.VISIBLE);
                if (this.mHasSingleLineTitleAttr) {
                    var6.setSingleLine(this.mSingleLineTitle);
                }
            } else {
                var6.setVisibility(View.GONE);
            }
        }

        ImageView var15 = (ImageView)holder.findViewById(16908294);
        byte var17;
        if (var15 != null) {
            int var7 = this.mIconResId;
            if (var7 != 0 || this.mIcon != null) {
                if (this.mIcon == null) {
                    this.mIcon = AppCompatResources.getDrawable(this.mContext, var7);
                }

                Drawable var12 = this.mIcon;
                if (var12 != null) {
                    var15.setImageDrawable(var12);
                }
            }

            if (this.mIcon != null) {
                var15.setVisibility(View.VISIBLE);
            } else {
                if (this.mIconSpaceReserved) {
                    var17 = View.INVISIBLE;
                } else {
                    var17 = View.GONE;
                }

                var15.setVisibility(var17);
            }
        }

        View var16 = holder.findViewById(R.id.icon_frame);
        View var13 = var16;
        if (var16 == null) {
            var13 = holder.findViewById(16908350);
        }

        if (var13 != null) {
            if (this.mIcon != null) {
                var13.setVisibility(View.VISIBLE);
            } else {
                var17 = var4;
                if (this.mIconSpaceReserved) {
                    var17 = View.INVISIBLE;
                }

                var13.setVisibility(var17);
            }
        }

        if (this.mShouldDisableView) {
            this.setEnabledStateOnViews(var2, this.isEnabled());
        } else {
            this.setEnabledStateOnViews(var2, true);
        }

        boolean var8 = this.isSelectable();
        var2.setFocusable(var8);
        var2.setClickable(var8);
        holder.setDividerAllowedAbove(this.mAllowDividerAbove);
        holder.setDividerAllowedBelow(this.mAllowDividerBelow);
        boolean var9 = this.isCopyingEnabled();
        if (var9 && this.mOnCopyListener == null) {
            this.mOnCopyListener = new Preference.OnPreferenceCopyListener(this);
        }

        Preference.OnPreferenceCopyListener var10;
        if (var9) {
            var10 = this.mOnCopyListener;
        } else {
            var10 = null;
        }

        var2.setOnCreateContextMenuListener(var10);
        var2.setLongClickable(var9);
        if (var9 && !var8) {
            ViewCompat.setBackground(var2, (Drawable)null);
        }

        this.mItemView = var2;
    }
    // kang

    public void seslGetPreferenceBounds(Rect bounds) {
        if (mItemView != null) {
            mItemView.getGlobalVisibleRect(bounds);
        }
    }

    private void setEnabledStateOnViews(@NonNull View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public void setOrder(int order) {
        if (order != mOrder) {
            mOrder = order;

            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return mOrder;
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    public void setTitle(@Nullable CharSequence title) {
        if (title == null && mTitle != null || title != null && !title.equals(mTitle)) {
            mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(mContext.getString(titleResId));
    }

    @Nullable
    public CharSequence getTitle() {
        return mTitle;
    }

    public void setIcon(@Nullable Drawable icon) {
        if (mIcon != icon) {
            mIcon = icon;
            mIconResId = 0;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        setIcon(AppCompatResources.getDrawable(mContext, iconResId));
        mIconResId = iconResId;
    }

    @Nullable
    public Drawable getIcon() {
        if (mIcon == null && mIconResId != 0) {
            mIcon = AppCompatResources.getDrawable(mContext, mIconResId);
        }
        return mIcon;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public CharSequence getSummary() {
        if (getSummaryProvider() != null) {
            return getSummaryProvider().provideSummary(this);
        }
        return mSummary;
    }

    public void setSummary(@Nullable CharSequence summary) {
        if (getSummaryProvider() != null) {
            throw new IllegalStateException("Preference already has a SummaryProvider set.");
        }
        if (!TextUtils.equals(mSummary, summary)) {
            mSummary = summary;
            notifyChanged();
        }
    }

    public void setSummary(int summaryResId) {
        setSummary(mContext.getString(summaryResId));
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;

            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return mEnabled && mDependencyMet && mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (mSelectable != selectable) {
            mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        if (mShouldDisableView != shouldDisableView) {
            mShouldDisableView = shouldDisableView;
            notifyChanged();
        }
    }

    public boolean getShouldDisableView() {
        return mShouldDisableView;
    }

    public final void setVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            if (mListener != null) {
                mListener.onPreferenceVisibilityChange(this);
            }
        }
    }

    public final boolean isVisible() {
        return mVisible;
    }

    public final boolean isShown() {
        if (!isVisible()) {
            return false;
        }

        if (getPreferenceManager() == null) {
            return false;
        }

        if (this == getPreferenceManager().getPreferenceScreen()) {
            return true;
        }

        PreferenceGroup parent = getParent();
        if (parent == null) {
            return false;
        }

        return parent.isShown();
    }

    long getId() {
        return mId;
    }

    protected void onClick() {}

    public void setKey(String key) {
        mKey = key;

        if (mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    public String getKey() {
        return mKey;
    }

    void requireKey() {
        if (TextUtils.isEmpty(mKey)) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }

        mRequiresKey = true;
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(mKey);
    }

    public boolean isPersistent() {
        return mPersistent;
    }

    protected boolean shouldPersist() {
        return mPreferenceManager != null && isPersistent() && hasKey();
    }

    public void setPersistent(boolean persistent) {
        mPersistent = persistent;
    }

    public void setSingleLineTitle(boolean singleLineTitle) {
        mHasSingleLineTitleAttr = true;
        mSingleLineTitle = singleLineTitle;
    }

    public boolean isSingleLineTitle() {
        return mSingleLineTitle;
    }

    public void setIconSpaceReserved(boolean iconSpaceReserved) {
        if (mIconSpaceReserved != iconSpaceReserved) {
            mIconSpaceReserved = iconSpaceReserved;
            notifyChanged();
        }
    }

    public boolean isIconSpaceReserved() {
        return mIconSpaceReserved;
    }

    public void setCopyingEnabled(boolean enabled) {
        if (mCopyingEnabled != enabled) {
            mCopyingEnabled = enabled;
            notifyChanged();
        }
    }

    public boolean isCopyingEnabled() {
        return mCopyingEnabled;
    }

    public final void setSummaryProvider(@Nullable SummaryProvider summaryProvider) {
        mSummaryProvider = summaryProvider;
        notifyChanged();
    }

    @Nullable
    public final SummaryProvider getSummaryProvider() {
        return mSummaryProvider;
    }

    public boolean callChangeListener(Object newValue) {
        return mOnChangeListener == null || mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public void setOnPreferenceChangeListener(@Nullable OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnChangeListener = onPreferenceChangeListener;
    }

    @Nullable
    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return mOnChangeListener;
    }

    public void setOnPreferenceClickListener(@Nullable OnPreferenceClickListener onPreferenceClickListener) {
        mOnClickListener = onPreferenceClickListener;
    }

    @Nullable
    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return mOnClickListener;
    }

    protected void performClick(@NonNull View view) {
        performClick();
    }

    public void performClick() {
        if (!isEnabled() || !isSelectable()) {
            return;
        }

        onClick();

        if (mOnClickListener != null && mOnClickListener.onPreferenceClick(this)) {
            return;
        }

        PreferenceManager preferenceManager = getPreferenceManager();
        if (preferenceManager != null) {
            PreferenceManager.OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
            if (listener != null && listener.onPreferenceTreeClick(this)) {
                return;
            }
        }

        if (mIntent != null) {
            Context context = getContext();
            context.startActivity(mIntent);
        }
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @Nullable
    public SharedPreferences getSharedPreferences() {
        if (mPreferenceManager == null || getPreferenceDataStore() != null) {
            return null;
        }

        return mPreferenceManager.getSharedPreferences();
    }

    @Override
    public int compareTo(@NonNull Preference another) {
        if (mOrder != another.mOrder) {
            return mOrder - another.mOrder;
        } else if (mTitle == another.mTitle) {
            return 0;
        } else if (mTitle == null) {
            return 1;
        } else if (another.mTitle == null) {
            return -1;
        } else {
            return mTitle.toString().compareToIgnoreCase(another.mTitle.toString());
        }
    }

    final void setOnPreferenceChangeInternalListener(@Nullable OnPreferenceChangeInternalListener listener) {
        mListener = listener;
    }

    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }

    protected void notifyHierarchyChanged() {
        if (mListener != null) {
            mListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;

        if (!mHasId) {
            mId = preferenceManager.getNextId();
        }

        dispatchSetInitialValue();
    }

    protected void onAttachedToHierarchy(@NonNull PreferenceManager preferenceManager, long id) {
        mId = id;
        mHasId = true;
        try {
            onAttachedToHierarchy(preferenceManager);
        } finally {
            mHasId = false;
        }
    }

    void assignParent(@Nullable PreferenceGroup parentGroup) {
        if (parentGroup != null && mParentGroup != null) {
            throw new IllegalStateException("This preference already has a parent. You must remove the existing parent before assigning a new one.");
        }
        mParentGroup = parentGroup;
    }

    public void onAttached() {
        registerDependency();
    }

    public void onDetached() {
        unregisterDependency();
        mWasDetached = true;
    }

    final boolean wasDetached() {
        return mWasDetached;
    }

    final void clearWasDetached() {
        mWasDetached = false;
    }

    private void registerDependency() {
        if (TextUtils.isEmpty(mDependencyKey)) return;

        Preference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null) {
            preference.registerDependent(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey + "\" not found for preference \"" + mKey + "\" (title: \"" + mTitle + "\"");
        }
    }

    private void unregisterDependency() {
        if (mDependencyKey != null) {
            final Preference oldDependency = findPreferenceInHierarchy(mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Nullable
    protected <T extends Preference> T findPreferenceInHierarchy(@NonNull String key) {
        if (mPreferenceManager == null) {
            return null;
        }

        return mPreferenceManager.findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (mDependents == null) {
            mDependents = new ArrayList<>();
        }

        mDependents.add(dependent);

        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (mDependents != null) {
            mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        final List<Preference> dependents = mDependents;

        if (dependents == null) {
            return;
        }

        final int dependentsCount = dependents.size();
        for (int i = 0; i < dependentsCount; i++) {
            dependents.get(i).onDependencyChanged(this, disableDependents);
        }
    }

    public void onDependencyChanged(@NonNull Preference dependency, boolean disableDependent) {
        if (mDependencyMet == disableDependent) {
            mDependencyMet = !disableDependent;

            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public void onParentChanged(@NonNull Preference parent, boolean disableChild) {
        if (mParentDependencyMet == disableChild) {
            mParentDependencyMet = !disableChild;

            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public void setDependency(@Nullable String dependencyKey) {
        unregisterDependency();

        mDependencyKey = dependencyKey;
        registerDependency();
    }

    @Nullable
    public String getDependency() {
        return mDependencyKey;
    }

    @Nullable
    public PreferenceGroup getParent() {
        return mParentGroup;
    }

    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (getPreferenceDataStore() != null) {
            onSetInitialValue(true, mDefaultValue);
            return;
        }

        final boolean shouldPersist = shouldPersist();
        if (!shouldPersist || !getSharedPreferences().contains(mKey)) {
            if (mDefaultValue != null) {
                onSetInitialValue(false, mDefaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    @Deprecated
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        onSetInitialValue(defaultValue);
    }

    protected void onSetInitialValue(@Nullable Object defaultValue) {}

    private void tryCommit(@NonNull SharedPreferences.Editor editor) {
        if (mPreferenceManager.shouldCommit()) {
            editor.apply();
        }
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }

        if (TextUtils.equals(value, getPersistedString(null))) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putString(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putString(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getString(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getString(mKey, defaultReturnValue);
    }

    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }

        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putStringSet(mKey, values);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putStringSet(mKey, values);
            tryCommit(editor);
        }
        return true;
    }

    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getStringSet(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getStringSet(mKey, defaultReturnValue);
    }

    protected boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedInt(~value)) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putInt(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putInt(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getInt(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getInt(mKey, defaultReturnValue);
    }

    protected boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putFloat(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putFloat(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected float getPersistedFloat(float defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getFloat(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getFloat(mKey, defaultReturnValue);
    }

    protected boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedLong(~value)) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putLong(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putLong(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected long getPersistedLong(long defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getLong(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getLong(mKey, defaultReturnValue);
    }

    protected boolean persistBoolean(boolean value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedBoolean(!value)) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putBoolean(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putBoolean(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getBoolean(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getBoolean(mKey, defaultReturnValue);
    }

    @NonNull
    @Override
    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    @NonNull
    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(@NonNull Bundle container) {
        dispatchSaveInstanceState(container);
    }

    void dispatchSaveInstanceState(@NonNull Bundle container) {
        if (hasKey()) {
            mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.putParcelable(mKey, state);
            }
        }
    }

    @Nullable
    protected Parcelable onSaveInstanceState() {
        mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(@NonNull Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    void dispatchRestoreInstanceState(@NonNull Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(mKey);
            if (state != null) {
                mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!mBaseMethodCalled) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }

    void callClickListener() {
        if (mOnClickListener != null) {
            mOnClickListener.onPreferenceClick(this);
        }
    }

    boolean isTalkBackIsRunning() {
        AccessibilityManager accessibility = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        String enabledServices = Settings.Secure.getString(getContext().getContentResolver(), "enabled_accessibility_services");

        if (accessibility != null && accessibility.isEnabled() && enabledServices != null) {
            return enabledServices.matches("(?i).*com.samsung.accessibility/com.samsung.android.app.talkback.TalkBackService.*")
                    || enabledServices.matches("(?i).*com.samsung.android.accessibility.talkback/com.samsung.android.marvin.talkback.TalkBackService.*")
                    || enabledServices.matches("(?i).*com.google.android.marvin.talkback.TalkBackService.*")
                    || enabledServices.matches("(?i).*com.samsung.accessibility/com.samsung.accessibility.universalswitch.UniversalSwitchService.*");
        }
        return false;
    }

    public void seslSetSummaryColor(int color) {
        mSummaryColor = color;
        mChangedSummaryColor = true;
        mChangedSummaryColorStateList = false;
    }

    public void seslSetSummaryColor(ColorStateList color) {
        mSummaryColorStateList = color;
        mChangedSummaryColorStateList = true;
        mChangedSummaryColor = false;
    }

    public void seslSetRoundedBg(int where) {
        mIsPreferenceRoundedBg = true;
        mWhere = where;
        mSubheaderRound = false;
        mIsRoundChanged = true;
    }

    public void seslSetSubheaderRoundedBackground(int where) {
        mIsPreferenceRoundedBg = true;
        mWhere = where;
        mSubheaderRound = true;
        mIsRoundChanged = true;
    }

    public void seslSetSubheaderColor(int color) {
        mSubheaderColor = color;
    }

    @CallSuper
    @Deprecated
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {}

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(@NonNull Preference preference, Object newValue);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(@NonNull Preference preference);
    }

    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(@NonNull Preference preference);

        void onPreferenceHierarchyChange(@NonNull Preference preference);

        void onPreferenceVisibilityChange(@NonNull Preference preference);
    }

    public interface SummaryProvider<T extends Preference> {
        @Nullable
        CharSequence provideSummary(@NonNull T preference);
    }

    public static class BaseSavedState extends AbsSavedState {
        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        @NonNull
        public static final Parcelable.Creator<BaseSavedState> CREATOR = new Parcelable.Creator<BaseSavedState>() {
            @Override
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            @Override
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }

    private static class OnPreferenceCopyListener implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private final Preference mPreference;

        OnPreferenceCopyListener(@NonNull Preference preference) {
            mPreference = preference;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            CharSequence summary = mPreference.getSummary();
            if (!mPreference.isCopyingEnabled() || TextUtils.isEmpty(summary)) {
                return;
            }
            menu.setHeaderTitle(summary);
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.copy).setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            ClipboardManager clipboard = (ClipboardManager) mPreference.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            CharSequence summary = mPreference.getSummary();
            ClipData clip = ClipData.newPlainText(CLIPBOARD_ID, summary);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mPreference.getContext(), mPreference.getContext().getString(R.string.preference_copied, summary), Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
