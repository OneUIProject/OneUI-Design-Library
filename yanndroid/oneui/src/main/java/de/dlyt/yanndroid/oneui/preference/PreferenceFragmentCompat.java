package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.XmlRes;
import androidx.appcompat.util.SeslRoundedCorner;
import androidx.appcompat.util.SeslSubheaderRoundedCorner;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public abstract class PreferenceFragmentCompat extends Fragment implements
        PreferenceManager.OnPreferenceTreeClickListener,
        PreferenceManager.OnDisplayPreferenceDialogListener,
        PreferenceManager.OnNavigateToScreenListener,
        DialogPreference.TargetFragment {
    public static final String ARG_PREFERENCE_ROOT = "de.dlyt.yanndroid.oneui.preference.PreferenceFragmentCompat.PREFERENCE_ROOT";
    private static final String DIALOG_FRAGMENT_TAG = "de.dlyt.yanndroid.oneui.preference.PreferenceFragment.DIALOG";
    private static final float FONT_SCALE_LARGE = 1.3f;
    private static final float FONT_SCALE_MEDIUM = 1.1f;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final String PREFERENCES_TAG = "android:preferences";
    static final int SWITCH_PREFERENCE_LAYOUT = 2;
    static final int SWITCH_PREFERENCE_LAYOUT_LARGE = 1;
    private static final String TAG = "SeslPreferenceFragmentC";
    private boolean mHavePrefs;
    private boolean mInitDone;
    private int mIsLargeLayout;
    private boolean mIsReducedMargin;
    @SuppressWarnings("WeakerAccess")
    RecyclerView mList;
    private SeslRoundedCorner mListRoundedCorner;
    private PreferenceManager mPreferenceManager;
    private SeslRoundedCorner mRoundedCorner;
    private Runnable mSelectPreferenceRunnable;
    private int mSubheaderColor;
    private SeslSubheaderRoundedCorner mSubheaderRoundedCorner;
    private final DividerDecoration mDividerDecoration = new DividerDecoration();
    private int mLayoutResId = R.layout.oui_preference_list_fragment;
    private boolean mIsRoundedCorner = true;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
            }
        }
    };

    final private Runnable mRequestFocus = new Runnable() {
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
        mIsLargeLayout = ((config.screenWidthDp > 320 || config.fontScale < FONT_SCALE_MEDIUM) && (config.screenWidthDp >= 411 || config.fontScale < FONT_SCALE_LARGE)) ? SWITCH_PREFERENCE_LAYOUT : SWITCH_PREFERENCE_LAYOUT_LARGE;
        mIsReducedMargin = config.screenWidthDp <= 250;
        int theme = tv.resourceId;
        if (theme == 0) {
            theme = R.style.PreferenceThemeStyle;
        }
        getActivity().getTheme().applyStyle(theme, false);

        mPreferenceManager = new PreferenceManager(requireContext());
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

    public abstract void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey);

    @SuppressLint("ResourceType")
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.PreferenceFragmentCompat, R.attr.preferenceFragmentCompatStyle, 0);

        mLayoutResId = a.getResourceId(R.styleable.PreferenceFragmentCompat_android_layout, mLayoutResId);

        final Drawable divider = a.getDrawable(R.styleable.PreferenceFragmentCompat_android_divider);
        final int dividerHeight = a.getDimensionPixelSize(R.styleable.PreferenceFragmentCompat_android_dividerHeight, -1);
        final boolean allowDividerAfterLastItem = a.getBoolean(R.styleable.PreferenceFragmentCompat_allowDividerAfterLastItem, true);

        a.recycle();

        TypedArray a2 = getContext().obtainStyledAttributes(null, R.styleable.View, android.R.attr.listSeparatorTextViewStyle, 0);
        Drawable background = a2.getDrawable(R.styleable.View_android_background);
        if (background instanceof ColorDrawable) {
            mSubheaderColor = ((ColorDrawable) background).getColor();
        }
        a2.recycle();

        final LayoutInflater themedInflater = inflater.cloneInContext(getContext());

        final View view = themedInflater.inflate(mLayoutResId, container, false);

        final View rawListContainer = view.findViewById(16908351);
        if (!(rawListContainer instanceof ViewGroup)) {
            throw new IllegalStateException("Content has view with id attribute 'android.R.id.list_container' that is not a ViewGroup class");
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
        mRoundedCorner = new SeslRoundedCorner(getContext());
        mSubheaderRoundedCorner = new SeslSubheaderRoundedCorner(getContext());
        if (mIsRoundedCorner) {
            mList.seslSetFillBottomEnabled(true);
            mList.seslSetFillBottomColor(this.mSubheaderColor);
            mListRoundedCorner = new SeslRoundedCorner(getContext(), true);
            mListRoundedCorner.setRoundedCorners(SeslRoundedCorner.ROUNDED_CORNER_TOP_LEFT | SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT);
        }

        if (mList.getParent() == null) {
            listContainer.addView(mList);
        }
        mHandler.post(mRequestFocus);

        return view;
    }

    public void setDivider(@Nullable Drawable divider) {
        mDividerDecoration.setDivider(divider);
    }

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

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceManager.getPreferenceScreen();
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            onUnbindPreferences();
            mHavePrefs = true;
            if (mInitDone) {
                postBindPreferences();
            }
        }
    }

    public void addPreferencesFromResource(@XmlRes int preferencesResId) {
        requirePreferenceManager();

        setPreferenceScreen(mPreferenceManager.inflateFromResource(requireContext(), preferencesResId, getPreferenceScreen()));
    }

    public void setPreferencesFromResource(@XmlRes int preferencesResId, @Nullable String key) {
        requirePreferenceManager();

        final PreferenceScreen xmlRoot = mPreferenceManager.inflateFromResource(requireContext(), preferencesResId, null);

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

    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        if (preference.getFragment() != null) {
            boolean handled = false;
            if (getCallbackFragment() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getCallbackFragment()).onPreferenceStartFragment(this, preference);
            }
            Fragment callbackFragment = this;
            while (!handled && callbackFragment != null) {
                if (callbackFragment instanceof OnPreferenceStartFragmentCallback) {
                    handled = ((OnPreferenceStartFragmentCallback) callbackFragment).onPreferenceStartFragment(this, preference);
                }
                callbackFragment = callbackFragment.getParentFragment();
            }
            if (!handled && getContext() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getContext()).onPreferenceStartFragment(this, preference);
            }
            if (!handled && getActivity() instanceof OnPreferenceStartFragmentCallback) {
                handled = ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
            }
            if (!handled) {
                Log.w(TAG, "onPreferenceStartFragment is not implemented in the parent activity - attempting to use a fallback implementation. You should implement this method so that you can configure the new fragment that will be displayed, and set a transition between the fragments.");
                final FragmentManager fragmentManager = getParentFragmentManager();
                final Bundle args = preference.getExtras();
                final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(requireActivity().getClassLoader(), preference.getFragment());
                fragment.setArguments(args);
                fragment.setTargetFragment(this, 0);
                fragmentManager.beginTransaction().replace(((View) requireView().getParent()).getId(), fragment).addToBackStack(null).commit();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onNavigateToScreen(@NonNull PreferenceScreen preferenceScreen) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceStartScreenCallback) {
            handled = ((OnPreferenceStartScreenCallback) getCallbackFragment()).onPreferenceStartScreen(this, preferenceScreen);
        }
        Fragment callbackFragment = this;
        while (!handled && callbackFragment != null) {
            if (callbackFragment instanceof OnPreferenceStartScreenCallback) {
                handled = ((OnPreferenceStartScreenCallback) callbackFragment).onPreferenceStartScreen(this, preferenceScreen);
            }
            callbackFragment = callbackFragment.getParentFragment();
        }
        if (!handled && getContext() instanceof OnPreferenceStartScreenCallback) {
            handled = ((OnPreferenceStartScreenCallback) getContext()).onPreferenceStartScreen(this, preferenceScreen);
        }
        if (!handled && getActivity() instanceof OnPreferenceStartScreenCallback) {
            ((OnPreferenceStartScreenCallback) getActivity()).onPreferenceStartScreen(this, preferenceScreen);
        }
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Nullable
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

    @SuppressWarnings("WeakerAccess")
    void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            getListView().setAdapter(onCreateAdapter(preferenceScreen));
            preferenceScreen.onAttached();
        }
        onBindPreferences();
    }

    private void unbindPreferences() {
        getListView().setAdapter(null);
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.onDetached();
        }
        onUnbindPreferences();
    }

    protected void onBindPreferences() {}

    protected void onUnbindPreferences() {}

    public final RecyclerView getListView() {
        return mList;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    public RecyclerView onCreateRecyclerView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @Nullable Bundle savedInstanceState) {
        if (requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
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

    @NonNull
    public RecyclerView.LayoutManager onCreateLayoutManager() {
        return new LinearLayoutManager(requireContext());
    }

    @NonNull
    protected RecyclerView.Adapter onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
        return new PreferenceGroupAdapter(preferenceScreen);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        boolean handled = false;
        if (getCallbackFragment() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getCallbackFragment()).onPreferenceDisplayDialog(this, preference);
        }
        Fragment callbackFragment = this;
        while (!handled && callbackFragment != null) {
            if (callbackFragment instanceof OnPreferenceDisplayDialogCallback) {
                handled = ((OnPreferenceDisplayDialogCallback) callbackFragment).onPreferenceDisplayDialog(this, preference);
            }
            callbackFragment = callbackFragment.getParentFragment();
        }
        if (!handled && getContext() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getContext()).onPreferenceDisplayDialog(this, preference);
        }
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity()).onPreferenceDisplayDialog(this, preference);
        }

        if (handled) {
            return;
        }

        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final DialogFragment f;
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else {
            throw new IllegalArgumentException(
                    "Cannot display dialog for an unknown Preference type: " + preference.getClass().getSimpleName() + ". Make sure to implement onPreferenceDisplayDialog() to handle displaying a custom dialog for this Preference.");
        }
        f.setTargetFragment(this, 0);
        f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public Fragment getCallbackFragment() {
        return null;
    }

    public void scrollToPreference(@NonNull String key) {
        scrollToPreferenceInternal(null, key);
    }

    public void scrollToPreference(@NonNull Preference preference) {
        scrollToPreferenceInternal(preference, null);
    }

    private void scrollToPreferenceInternal(@Nullable final Preference preference, @Nullable final String key) {
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
    @Override
    public void onConfigurationChanged(Configuration configuration) {
        if (getListView() != null) {
            RecyclerView.Adapter adapter = getListView().getAdapter();
            boolean z = true;
            int i = ((configuration.screenWidthDp > 320 || configuration.fontScale < FONT_SCALE_MEDIUM) && (configuration.screenWidthDp >= 411 || configuration.fontScale < FONT_SCALE_LARGE)) ? SWITCH_PREFERENCE_LAYOUT : SWITCH_PREFERENCE_LAYOUT_LARGE;
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
                    if (item instanceof SwitchPreferenceCompat) {
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
            if (configuration.screenWidthDp > 250) {
                z = false;
            }
            if (z != this.mIsReducedMargin && z2) {
                this.mIsReducedMargin = z;
                setDivider(getContext().obtainStyledAttributes(null, R.styleable.PreferenceFragmentCompat, R.attr.preferenceFragmentCompatStyle, 0).getDrawable(R.styleable.PreferenceFragment_android_divider));
                Parcelable onSaveInstanceState = getListView().getLayoutManager().onSaveInstanceState();
                getListView().setAdapter(getListView().getAdapter());
                getListView().getLayoutManager().onRestoreInstanceState(onSaveInstanceState);
            }
        }
        super.onConfigurationChanged(configuration);
    }
    // kang

    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref);
    }

    public interface OnPreferenceStartScreenCallback {
        boolean onPreferenceStartScreen(@NonNull PreferenceFragmentCompat caller, @NonNull PreferenceScreen pref);
    }

    public interface OnPreferenceDisplayDialogCallback {
        boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref);
    }

    private static class ScrollToPreferenceObserver extends RecyclerView.AdapterDataObserver {
        private final RecyclerView.Adapter<?> mAdapter;
        private final RecyclerView mList;
        private final Preference mPreference;
        private final String mKey;

        ScrollToPreferenceObserver(RecyclerView.Adapter<?> adapter, RecyclerView list, Preference preference, String key) {
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

        private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
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

        public void setDivider(Drawable divider) {
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
