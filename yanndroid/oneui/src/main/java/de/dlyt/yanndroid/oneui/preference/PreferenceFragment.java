package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslRoundedCorner;
import de.dlyt.yanndroid.oneui.sesl.utils.SeslSubheaderRoundedCorner;
import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
@Deprecated
public abstract class PreferenceFragment extends android.app.Fragment implements
        PreferenceManager.OnPreferenceTreeClickListener,
        PreferenceManager.OnDisplayPreferenceDialogListener,
        PreferenceManager.OnNavigateToScreenListener,
        DialogPreference.TargetFragment {
    @Deprecated
    public static final String ARG_PREFERENCE_ROOT = "de.dlyt.yanndroid.oneui.preference.PreferenceFragmentCompat.PREFERENCE_ROOT";
    private static final String DIALOG_FRAGMENT_TAG = "de.dlyt.yanndroid.oneui.preference.PreferenceFragment.DIALOG";
    private static final float FONT_SCALE_LARGE = 1.3f;
    private static final float FONT_SCALE_MEDIUM = 1.1f;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final String PREFERENCES_TAG = "android:preferences";
    static final String TAG = "SeslPreferenceFragment";
    private boolean mHavePrefs;
    private boolean mInitDone;
    private int mIsLargeLayout;
    private boolean mIsReducedMargin;
    RecyclerView mList;
    private SeslRoundedCorner mListRoundedCorner;
    private PreferenceManager mPreferenceManager;
    private SeslRoundedCorner mRoundedCorner;
    private Runnable mSelectPreferenceRunnable;
    private Context mStyledContext;
    private int mSubheaderColor;
    private SeslSubheaderRoundedCorner mSubheaderRoundedCorner;
    private final DividerDecoration mDividerDecoration = new DividerDecoration();
    private int mLayoutResId = R.layout.oui_preference_list_fragment;
    private boolean mIsRoundedCorner = true;

    @SuppressWarnings("deprecation")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
            }
        }
    };

    private final Runnable mRequestFocus = new Runnable() {
        @Override
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TypedValue tv = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        Configuration config = getResources().getConfiguration();
        mIsLargeLayout = ((config.screenWidthDp > 320 || config.fontScale < FONT_SCALE_MEDIUM) && (config.screenWidthDp >= 411 || config.fontScale < FONT_SCALE_LARGE)) ? 2 : 1;
        mIsReducedMargin = config.screenWidthDp <= 250;
        int theme = tv.resourceId;
        if (theme == 0) {
            theme = R.style.PreferenceThemeStyle;
        }
        mStyledContext = new ContextThemeWrapper(getActivity(), theme);

        mPreferenceManager = new PreferenceManager(mStyledContext);
        mPreferenceManager.setOnNavigateToScreenListener(this);
        final Bundle args = getArguments();
        final String rootKey;
        if (args != null) {
            rootKey = getArguments().getString(ARG_PREFERENCE_ROOT);
        } else {
            rootKey = null;
        }
        onCreatePreferences(savedInstanceState, rootKey);
    }

    @Deprecated
    public abstract void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey);

    @SuppressLint({"RestrictedApi", "ResourceType"})
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TypedArray a = mStyledContext.obtainStyledAttributes(null, R.styleable.PreferenceFragment, TypedArrayUtils.getAttr(mStyledContext, R.attr.preferenceFragmentStyle, 16844038), 0);

        mLayoutResId = a.getResourceId(R.styleable.PreferenceFragment_android_layout, mLayoutResId);

        final Drawable divider = a.getDrawable(R.styleable.PreferenceFragment_android_divider);
        final int dividerHeight = a.getDimensionPixelSize(R.styleable.PreferenceFragment_android_dividerHeight, -1);
        final boolean allowDividerAfterLastItem = a.getBoolean(R.styleable.PreferenceFragment_allowDividerAfterLastItem, true);
        a.recycle();

        TypedArray a2 = mStyledContext.obtainStyledAttributes(null, R.styleable.View, android.R.attr.listSeparatorTextViewStyle, 0);
        Drawable background = a2.getDrawable(R.styleable.View_android_background);
        if (background instanceof ColorDrawable) {
            mSubheaderColor = ((ColorDrawable) background).getColor();
        }
        Log.d(TAG, " sub header color = " + mSubheaderColor);
        a2.recycle();

        final LayoutInflater themedInflater = inflater.cloneInContext(mStyledContext);

        final View view = themedInflater.inflate(mLayoutResId, container, false);

        final View rawListContainer = view.findViewById(16908351);
        if (!(rawListContainer instanceof ViewGroup)) {
            throw new RuntimeException("Content has view with id attribute 'android.R.id.list_container' that is not a ViewGroup class");
        }

        final ViewGroup listContainer = (ViewGroup) rawListContainer;

        final RecyclerView listView = onCreateRecyclerView(themedInflater, listContainer, savedInstanceState);
        if (listView == null) {
            throw new RuntimeException("Could not create RecyclerView");
        }

        mList = listView;

        listView.addItemDecoration(mDividerDecoration);
        setDivider(divider);
        if (dividerHeight != -1) {
            setDividerHeight(dividerHeight);
        }
        mDividerDecoration.setAllowDividerAfterLastItem(allowDividerAfterLastItem);

        mList.setItemAnimator(null);
        mRoundedCorner = new SeslRoundedCorner(mStyledContext);
        mSubheaderRoundedCorner = new SeslSubheaderRoundedCorner(this.mStyledContext);
        if (mIsRoundedCorner) {
            mList.seslSetFillBottomEnabled(true);
            mList.seslSetFillBottomColor(mSubheaderColor);
            mListRoundedCorner = new SeslRoundedCorner(mStyledContext, true);
            mListRoundedCorner.setRoundedCorners(SeslRoundedCorner.ROUNDED_CORNER_TOP_LEFT | SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT);
        }

        if (mList.getParent() == null) {
            listContainer.addView(mList);
        }
        mHandler.post(mRequestFocus);

        return view;
    }

    @Deprecated
    public void setDivider(@Nullable Drawable divider) {
        mDividerDecoration.setDivider(divider);
    }

    @Deprecated
    public void setDividerHeight(int height) {
        mDividerDecoration.setDividerHeight(height);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                final PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }

        if (mHavePrefs) {
            bindPreferences();
            if (mSelectPreferenceRunnable != null) {
                mSelectPreferenceRunnable.run();
                mSelectPreferenceRunnable = null;
            }
        }

        mInitDone = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreferenceManager.setOnPreferenceTreeClickListener(this);
        mPreferenceManager.setOnDisplayPreferenceDialogListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPreferenceManager.setOnPreferenceTreeClickListener(null);
        mPreferenceManager.setOnDisplayPreferenceDialogListener(null);
    }

    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mHandler.removeMessages(MSG_BIND_PREFERENCES);
        if (mHavePrefs) {
            unbindPreferences();
        }
        mList = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    @Deprecated
    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    @Deprecated
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            onUnbindPreferences();
            mHavePrefs = true;
            if (mInitDone) {
                postBindPreferences();
            }
        }
    }

    @Deprecated
    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceManager.getPreferenceScreen();
    }

    @Deprecated
    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        requirePreferenceManager();

        setPreferenceScreen(mPreferenceManager.inflateFromResource(mStyledContext, preferencesResId, getPreferenceScreen()));
    }

    @Deprecated
    public void setPreferencesFromResource(@XmlRes int preferencesResId, @Nullable String key) {
        requirePreferenceManager();

        final PreferenceScreen xmlRoot = mPreferenceManager.inflateFromResource(mStyledContext, preferencesResId, null);

        final Preference root;
        if (key != null) {
            root = xmlRoot.findPreference(key);
            if (!(root instanceof PreferenceScreen)) {
                throw new IllegalArgumentException("Preference object with key " + key + " is not a PreferenceScreen");
            }
        } else {
            root = xmlRoot;
        }

        setPreferenceScreen((PreferenceScreen) root);
    }

    @Deprecated
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        if (preference.getFragment() != null) {
            boolean handled = false;
            if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getCallbackFragment()).onPreferenceStartFragment(this, preference);
            }
            if (!handled && getActivity() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
            }
            return handled;
        }
        return false;
    }

    @Deprecated
    @Override
    public void onNavigateToScreen(@NonNull PreferenceScreen preferenceScreen) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceStartScreenCallback) {
            handled = ((OnPreferenceStartScreenCallback) getCallbackFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
        if (!handled && getActivity() instanceof OnPreferenceStartScreenCallback) {
            ((OnPreferenceStartScreenCallback) getActivity()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }

    @Deprecated
    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) return;
        mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
    }

    void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            getListView().setAdapter(onCreateAdapter(preferenceScreen));
            preferenceScreen.onAttached();
        }
        onBindPreferences();
    }

    private void unbindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.onDetached();
        }
        onUnbindPreferences();
    }

    protected void onBindPreferences() {}

    protected void onUnbindPreferences() {}

    @Deprecated
    public final RecyclerView getListView() {
        return mList;
    }

    @Deprecated
    @NonNull
    public RecyclerView onCreateRecyclerView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        if (mStyledContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            RecyclerView recyclerView = parent.findViewById(R.id.recycler_view);
            if (recyclerView != null) {
                return recyclerView;
            }
        }
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.sesl_preference_recyclerview, parent, false);

        recyclerView.setLayoutManager(onCreateLayoutManager());
        recyclerView.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(recyclerView));

        return recyclerView;
    }

    @Deprecated
    @NonNull
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @Deprecated
    @NonNull
    protected RecyclerView.Adapter onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen);
    }

    @Deprecated
    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity()).onPreferenceDisplayDialog(this, preference);
        }

        if (handled) {
            return;
        }

        if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final android.app.DialogFragment f;
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            throw new IllegalArgumentException("Tried to display dialog for unknown preference type. Did you forget to override onDisplayPreferenceDialog()?");
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public android.app.Fragment getCallbackFragment() {
        return null;
    }

    @Deprecated
    public void scrollToPreference(@NonNull String key) {
        scrollToPreferenceInternal(null, key);
    }

    @Deprecated
    public void scrollToPreference(@NonNull Preference preference) {
        scrollToPreferenceInternal(preference, null);
    }

    private void scrollToPreferenceInternal(final Preference preference, final String key) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final RecyclerView.Adapter<?> adapter = mList.getAdapter();
                if (!(adapter instanceof PreferenceGroup.PreferencePositionCallback)) {
                    if (adapter != null) {
                        throw new IllegalStateException("Adapter must implement PreferencePositionCallback");
                    } else {
                        return;
                    }
                }
                final int position;
                if (preference != null) {
                    position = ((PreferenceGroup.PreferencePositionCallback) adapter).getPreferenceAdapterPosition(preference);
                } else {
                    position = ((PreferenceGroup.PreferencePositionCallback) adapter).getPreferenceAdapterPosition(key);
                }
                if (position != RecyclerView.NO_POSITION) {
                    mList.scrollToPosition(position);
                } else {
                    adapter.registerAdapterDataObserver(new ScrollToPreferenceObserver(adapter, mList, preference, key));
                }
            }
        };
        if (mList == null) {
            mSelectPreferenceRunnable = r;
        } else {
            r.run();
        }
    }

    public void seslSetRoundedCorner(boolean rounded) {
        mIsRoundedCorner = rounded;
    }

    // kang
    @SuppressLint("RestrictedApi")
    public void onConfigurationChanged(Configuration newConfig) {
        if (getListView() != null) {
            RecyclerView.Adapter adapter = getListView().getAdapter();
            boolean z = true;
            int i = ((newConfig.screenWidthDp > 320 || newConfig.fontScale < FONT_SCALE_MEDIUM) && (newConfig.screenWidthDp >= 411 || newConfig.fontScale < FONT_SCALE_LARGE)) ? 2 : 1;
            boolean z2 = adapter instanceof PreferenceGroupAdapter;
            if (z2 && i != this.mIsLargeLayout) {
                this.mIsLargeLayout = i;
                int i2 = 0;
                boolean z3 = false;
                while (true) {
                    PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) adapter;
                    if (i2 >= preferenceGroupAdapter.getItemCount()) {
                        break;
                    }
                    Preference item = preferenceGroupAdapter.getItem(i2);
                    int layoutResource = item.getLayoutResource();
                    if ((item instanceof SwitchPreferenceCompat) || (item instanceof SwitchPreference)) {
                        if (layoutResource == R.layout.sesl_switch_preference_screen_large) {
                            item.setLayoutResource(R.layout.sesl_switch_preference_screen);
                        } else if (layoutResource == R.layout.sesl_switch_preference_screen) {
                            item.setLayoutResource(R.layout.sesl_switch_preference_screen_large);
                        } else if (layoutResource == R.layout.sesl_preference_switch_large) {
                            item.setLayoutResource(R.layout.sesl_preference);
                        } else if (layoutResource == R.layout.sesl_preference) {
                            item.setLayoutResource(R.layout.sesl_preference_switch_large);
                        }
                        z3 = true;
                    }
                    i2++;
                }
                if (z3) {
                    adapter.notifyDataSetChanged();
                }
            }
            if (newConfig.screenWidthDp > 250) {
                z = false;
            }
            if (z != this.mIsReducedMargin && z2) {
                this.mIsReducedMargin = z;
                setDivider(this.mStyledContext.obtainStyledAttributes(null, R.styleable.PreferenceFragment, TypedArrayUtils.getAttr(this.mStyledContext, R.attr.preferenceFragmentStyle, 16844038), 0).getDrawable(R.styleable.PreferenceFragment_android_divider));
                Parcelable onSaveInstanceState = getListView().getLayoutManager().onSaveInstanceState();
                getListView().setAdapter(getListView().getAdapter());
                getListView().getLayoutManager().onRestoreInstanceState(onSaveInstanceState);
            }
        }
        super.onConfigurationChanged(newConfig);
    }
    // kang

    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(@NonNull PreferenceFragment caller, @NonNull Preference pref);
    }

    public interface OnPreferenceStartScreenCallback {
        boolean onPreferenceStartScreen(@NonNull PreferenceFragment caller, @NonNull PreferenceScreen pref);
    }

    public interface OnPreferenceDisplayDialogCallback {
        boolean onPreferenceDisplayDialog(@NonNull PreferenceFragment caller, @NonNull Preference pref);
    }

    private static class ScrollToPreferenceObserver extends RecyclerView.AdapterDataObserver {
        private final RecyclerView.Adapter<?> mAdapter;
        private final RecyclerView mList;
        private final Preference mPreference;
        private final String mKey;

        ScrollToPreferenceObserver(@NonNull RecyclerView.Adapter<?> adapter, @NonNull RecyclerView list, Preference preference, String key) {
            mAdapter = adapter;
            mList = list;
            mPreference = preference;
            mKey = key;
        }

        private void scrollToPreference() {
            mAdapter.unregisterAdapterDataObserver(this);
            final int position;
            if (mPreference != null) {
                position = ((PreferenceGroup.PreferencePositionCallback) mAdapter).getPreferenceAdapterPosition(mPreference);
            } else {
                position = ((PreferenceGroup.PreferencePositionCallback) mAdapter).getPreferenceAdapterPosition(mKey);
            }
            if (position != RecyclerView.NO_POSITION) {
                mList.scrollToPosition(position);
            }
        }

        @Override
        public void onChanged() {
            scrollToPreference();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            scrollToPreference();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            scrollToPreference();
        }
    }

    private class DividerDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int mDividerHeight;
        private boolean mAllowDividerAfterLastItem = true;

        DividerDecoration() {}

        @Override
        public void seslOnDispatchDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.seslOnDispatchDraw(c, parent, state);

            for (int i = 0; i < parent.getChildCount(); i++) {
                final View child = parent.getChildAt(i);
                final PreferenceViewHolder holder = parent.getChildViewHolder(child) instanceof PreferenceViewHolder ?
                        (PreferenceViewHolder) parent.getChildViewHolder(child) : null;

                int y = ((int) child.getY()) + child.getHeight();
                if (mDivider != null && shouldDrawDividerBelow(child, parent)) {
                    mDivider.setBounds(0, y, parent.getWidth(), mDividerHeight + y);
                    mDivider.draw(c);
                }

                if (mIsRoundedCorner && holder != null && holder.isBackgroundDrawn()) {
                    if (holder.isDrawSubheaderRound()) {
                        mSubheaderRoundedCorner.setRoundedCorners(holder.getDrawCorners());
                        mSubheaderRoundedCorner.drawRoundedCorner(child, c);
                    } else {
                        mRoundedCorner.setRoundedCorners(holder.getDrawCorners());
                        mRoundedCorner.drawRoundedCorner(child, c);
                    }
                }
            }

            if (mIsRoundedCorner) {
                mListRoundedCorner.drawRoundedCorner(c);
            }
        }

        private boolean shouldDrawDividerBelow(@NonNull View view, @NonNull RecyclerView parent) {
            final RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
            final boolean dividerAllowedBelow = holder instanceof PreferenceViewHolder && ((PreferenceViewHolder) holder).isDividerAllowedBelow();
            if (!dividerAllowedBelow) {
                return false;
            }
            boolean nextAllowed = mAllowDividerAfterLastItem;
            int index = parent.indexOfChild(view);
            if (index < parent.getChildCount() - 1) {
                final View nextView = parent.getChildAt(index + 1);
                final RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(nextView);
                nextAllowed = nextHolder instanceof PreferenceViewHolder && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove();
            }
            return nextAllowed;
        }

        public void setDivider(@Nullable Drawable divider) {
            if (divider != null) {
                mDividerHeight = divider.getIntrinsicHeight();
            } else {
                mDividerHeight = 0;
            }
            mDivider = divider;
            mList.invalidateItemDecorations();
        }

        public void setDividerHeight(int dividerHeight) {
            mDividerHeight = dividerHeight;
            mList.invalidateItemDecorations();
        }

        public void setAllowDividerAfterLastItem(boolean allowDividerAfterLastItem) {
            mAllowDividerAfterLastItem = allowDividerAfterLastItem;
        }
    }
}
