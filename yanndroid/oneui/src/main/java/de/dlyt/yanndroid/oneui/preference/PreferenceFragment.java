package de.dlyt.yanndroid.oneui.preference;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.util.SeslRoundedCorner;
import androidx.appcompat.util.SeslSubheaderRoundedCorner;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.SeslLinearLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public abstract class PreferenceFragment extends Fragment implements PreferenceManager.OnPreferenceTreeClickListener, PreferenceManager.OnDisplayPreferenceDialogListener, PreferenceManager.OnNavigateToScreenListener, DialogPreference.TargetFragment {
    private final PreferenceFragment.DividerDecoration mDividerDecoration = new PreferenceFragment.DividerDecoration();
    private boolean mHavePrefs;
    private boolean mInitDone;
    private int mIsLargeLayout;
    private int mLayoutResId = R.layout.sesl_preference_list_fragment;
    private RecyclerView mList;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            RecyclerView var1 = PreferenceFragment.this.mList;
            var1.focusableViewAvailable(var1);
        }
    };
    private PreferenceManager mPreferenceManager;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message var1) {
            if (var1.what == 1) {
                PreferenceFragment.this.bindPreferences();
            }

        }
    };
    private Runnable mSelectPreferenceRunnable;
    private boolean mIsRoundedCorner = true;
    private SeslRoundedCorner mSeslListRoundedCorner;
    private SeslRoundedCorner mSeslRoundedCorner;
    private SeslSubheaderRoundedCorner mSeslSubheaderRoundedCorner;
    private int mSubheaderColor;

    public PreferenceFragment() {
    }

    public void bindPreferences() {
        PreferenceScreen var1 = this.getPreferenceScreen();
        if (var1 != null) {
            this.getListView().setAdapter(this.onCreateAdapter(var1));
            var1.onAttached();
        }

        this.onBindPreferences();
    }

    public Preference findPreference(CharSequence var1) {
        PreferenceManager var2 = this.mPreferenceManager;
        return var2 == null ? null : var2.findPreference(var1);
    }

    public Fragment getCallbackFragment() {
        return null;
    }

    public final RecyclerView getListView() {
        return this.mList;
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceManager.getPreferenceScreen();
    }

    public void setPreferenceScreen(PreferenceScreen var1) {
        if (this.mPreferenceManager.setPreferences(var1) && var1 != null) {
            this.onUnbindPreferences();
            this.mHavePrefs = true;
            if (this.mInitDone) {
                this.postBindPreferences();
            }
        }

    }

    public void onBindPreferences() {
    }

    public void onConfigurationChanged(Configuration var1) {
        if (this.getListView() != null) {
            RecyclerView.Adapter var2 = this.getListView().getAdapter();
            byte var3;
            if (var1.smallestScreenWidthDp <= 320) {
                var3 = 1;
            } else {
                var3 = 2;
            }

            if (var2 instanceof PreferenceGroupAdapter && var3 != this.mIsLargeLayout) {
                this.mIsLargeLayout = var3;
                int var6 = 0;
                boolean var4 = false;

                while (true) {
                    PreferenceGroupAdapter var5 = (PreferenceGroupAdapter) var2;
                    if (var6 >= var5.getItemCount()) {
                        if (var4) {
                            var2.notifyDataSetChanged();
                        }
                        break;
                    }

                    Preference var8 = var5.getItem(var6);
                    if (var8 instanceof SwitchPreference) {
                        int var7;
                        if (var8 instanceof SwitchPreferenceScreen) {
                            if (this.mIsLargeLayout == 1) {
                                var7 = R.layout.sesl_switch_preference_screen_large;
                            } else {
                                var7 = R.layout.sesl_switch_preference_screen;
                            }

                            var8.setLayoutResource(var7);
                        } else {
                            if (this.mIsLargeLayout == 1) {
                                var7 = R.layout.sesl_preference_switch_large;
                            } else {
                                var7 = R.layout.sesl_preference;
                            }

                            var8.setLayoutResource(var7);
                        }

                        var4 = true;
                    }

                    ++var6;
                }
            }
        }

        super.onConfigurationChanged(var1);
    }

    public void onCreate(Bundle var1) {
        super.onCreate(var1);
        TypedValue var2 = new TypedValue();
        this.getActivity().getTheme().resolveAttribute(R.attr.preferenceTheme, var2, true);
        int var3 = var2.resourceId;
        int var4 = var3;
        if (var3 == 0) {
            var4 = R.style.PreferenceThemeStyle;
        }

        this.getActivity().getTheme().applyStyle(var4, false);
        this.mPreferenceManager = new PreferenceManager(this.getContext());
        this.mPreferenceManager.setOnNavigateToScreenListener(this);
        String var5;
        if (this.getArguments() != null) {
            var5 = this.getArguments().getString("de.dlyt.yanndroid.oneui.preference.PreferenceFragment.PREFERENCE_ROOT");
        } else {
            var5 = null;
        }

        this.onCreatePreferences(var1, var5);
    }

    public RecyclerView.Adapter onCreateAdapter(PreferenceScreen var1) {
        return new PreferenceGroupAdapter(var1);
    }

    public RecyclerView.LayoutManager onCreateLayoutManager() {
        return new SeslLinearLayoutManager(this.getContext());
    }

    public abstract void onCreatePreferences(Bundle var1, String var2);

    public RecyclerView onCreateRecyclerView(LayoutInflater var1, ViewGroup var2, Bundle var3) {
        if (this.getContext().getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
            RecyclerView var5 = (RecyclerView) var2.findViewById(R.id.recycler_view);
            if (var5 != null) {
                return var5;
            }
        }

        RecyclerView var4 = (RecyclerView) var1.inflate(R.layout.sesl_preference_recyclerview, var2, false);
        var4.setLayoutManager(this.onCreateLayoutManager());
        var4.setAccessibilityDelegateCompat(new PreferenceRecyclerViewAccessibilityDelegate(var4));
        return var4;
    }

    public View onCreateView(LayoutInflater var1, ViewGroup var2, Bundle var3) {
        TypedArray var4 = this.getContext().obtainStyledAttributes((AttributeSet) null, R.styleable.SeslPreferenceFragmentCompat, R.attr.preferenceFragmentCompatStyle, 0);
        this.mLayoutResId = var4.getResourceId(R.styleable.SeslPreferenceFragmentCompat_android_layout, this.mLayoutResId);
        Drawable var5 = var4.getDrawable(R.styleable.SeslPreferenceFragmentCompat_android_divider);
        int var6 = var4.getDimensionPixelSize(R.styleable.SeslPreferenceFragmentCompat_android_dividerHeight, -1);
        boolean var7 = var4.getBoolean(R.styleable.SeslPreferenceFragmentCompat_allowDividerAfterLastItem, true);
        var4.recycle();
        Resources var13 = this.getActivity().getResources();
        TypedArray var8 = this.getContext().obtainStyledAttributes((AttributeSet) null, R.styleable.View, android.R.attr.listSeparatorTextViewStyle, 0);
        Drawable var9 = var8.getDrawable(R.styleable.View_android_background);
        if (var9 instanceof ColorDrawable) {
            this.mSubheaderColor = ((ColorDrawable) var9).getColor();
        }

        StringBuilder var15 = new StringBuilder();
        var15.append(" sub header color = ");
        var15.append(this.mSubheaderColor);
        Log.d("SeslPreferenceFragmentC", var15.toString());
        var8.recycle();
        LayoutInflater var14 = var1.cloneInContext(this.getContext());
        View var10 = var14.inflate(this.mLayoutResId, var2, false);
        View var11 = var10.findViewById(android.R.id.list_container);
        if (var11 instanceof ViewGroup) {
            var2 = (ViewGroup) var11;
            RecyclerView var12 = this.onCreateRecyclerView(var14, var2, var3);
            if (var12 != null) {
                this.mList = var12;
                var12.addItemDecoration(this.mDividerDecoration);
                this.setDivider(var5);
                if (var6 != -1) {
                    this.setDividerHeight(var6);
                }

                this.mDividerDecoration.setAllowDividerAfterLastItem(var7);
                this.mList.setItemAnimator(null);

                this.mSeslRoundedCorner = new SeslRoundedCorner(this.getContext());
                this.mSeslSubheaderRoundedCorner = new SeslSubheaderRoundedCorner(this.getContext());
                if (this.mIsRoundedCorner) {
                    var12.seslSetFillBottomEnabled(true);
                    var12.seslSetFillBottomColor(this.mSubheaderColor);
                    SeslRoundedCorner seslRoundedCorner = new SeslRoundedCorner(this.getContext(), true);
                    this.mSeslListRoundedCorner = seslRoundedCorner;
                    seslRoundedCorner.setRoundedCorners(3);
                }

                if (this.mList.getParent() == null) {
                    var2.addView(this.mList);
                }

                this.mHandler.post(this.mRequestFocus);
                return var10;
            } else {
                throw new RuntimeException("Could not create RecyclerView");
            }
        } else {
            throw new RuntimeException("Content has view with id attribute 'android.R.id.list_container' that is not a ViewGroup class");
        }
    }

    public void onDestroyView() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mHandler.removeMessages(1);
        if (this.mHavePrefs) {
            this.unbindPreferences();
        }

        this.mList = null;
        super.onDestroyView();
    }

    public void onDisplayPreferenceDialog(Preference var1) {
        boolean var2;
        if (this.getCallbackFragment() instanceof PreferenceFragment.OnPreferenceDisplayDialogCallback) {
            var2 = ((PreferenceFragment.OnPreferenceDisplayDialogCallback) this.getCallbackFragment()).onPreferenceDisplayDialog(this, var1);
        } else {
            var2 = false;
        }

        boolean var3 = var2;
        if (!var2) {
            var3 = var2;
            if (this.getActivity() instanceof PreferenceFragment.OnPreferenceDisplayDialogCallback) {
                var3 = ((PreferenceFragment.OnPreferenceDisplayDialogCallback) this.getActivity()).onPreferenceDisplayDialog(this, var1);
            }
        }

        if (!var3) {
            if (this.getFragmentManager().findFragmentByTag("de.dlyt.yanndroid.oneui.preference.PreferenceFragment.DIALOG") == null) {
                Object var4;
                if (var1 instanceof EditTextPreference) {
                    var4 = EditTextPreferenceDialogFragmentCompat.newInstance(var1.getKey());
                } else if (var1 instanceof ListPreference) {
                    var4 = ListPreferenceDialogFragmentCompat.newInstance(var1.getKey());
                } else {
                    if (!(var1 instanceof MultiSelectListPreference)) {
                        throw new IllegalArgumentException("Tried to display dialog for unknown preference type. Did you forget to override onDisplayPreferenceDialog()?");
                    }

                    var4 = MultiSelectListPreferenceDialogFragmentCompat.newInstance(var1.getKey());
                }

                ((Fragment) var4).setTargetFragment(this, 0);
                ((DialogFragment) var4).show(this.getFragmentManager(), "de.dlyt.yanndroid.oneui.preference.PreferenceFragment.DIALOG");
            }
        }
    }

    public void onNavigateToScreen(PreferenceScreen var1) {
        boolean var2;
        if (this.getCallbackFragment() instanceof PreferenceFragment.OnPreferenceStartScreenCallback) {
            var2 = ((PreferenceFragment.OnPreferenceStartScreenCallback) this.getCallbackFragment()).onPreferenceStartScreen(this, var1);
        } else {
            var2 = false;
        }

        if (!var2 && this.getActivity() instanceof PreferenceFragment.OnPreferenceStartScreenCallback) {
            ((PreferenceFragment.OnPreferenceStartScreenCallback) this.getActivity()).onPreferenceStartScreen(this, var1);
        }

    }

    public boolean onPreferenceTreeClick(Preference var1) {
        if (var1.getFragment() != null) {
            boolean var2;
            if (this.getCallbackFragment() instanceof PreferenceFragment.OnPreferenceStartFragmentCallback) {
                var2 = ((PreferenceFragment.OnPreferenceStartFragmentCallback) this.getCallbackFragment()).onPreferenceStartFragment(this, var1);
            } else {
                var2 = false;
            }

            boolean var3 = var2;
            if (!var2) {
                var3 = var2;
                if (this.getActivity() instanceof PreferenceFragment.OnPreferenceStartFragmentCallback) {
                    var3 = ((PreferenceFragment.OnPreferenceStartFragmentCallback) this.getActivity()).onPreferenceStartFragment(this, var1);
                }
            }

            if (!var3) {
                Log.w("SeslPreferenceFragmentC", "onPreferenceStartFragment is not implemented in the parent activity - attempting to use a fallback implementation. You should implement this method so that you can configure the new fragment that will be displayed, and set a transition between the fragments.");
                FragmentManager var4 = this.requireActivity().getSupportFragmentManager();
                Bundle var5 = var1.getExtras();
                Fragment var6 = var4.getFragmentFactory().instantiate(this.requireActivity().getClassLoader(), var1.getFragment());
                var6.setArguments(var5);
                var6.setTargetFragment(this, 0);
                FragmentTransaction var7 = var4.beginTransaction();
                var7.replace(((View) this.getView().getParent()).getId(), var6);
                var7.addToBackStack(null);
                var7.commit();
            }

            return true;
        } else {
            return false;
        }
    }

    public void onSaveInstanceState(Bundle var1) {
        super.onSaveInstanceState(var1);
        PreferenceScreen var2 = this.getPreferenceScreen();
        if (var2 != null) {
            Bundle var3 = new Bundle();
            var2.saveHierarchyState(var3);
            var1.putBundle("android:preferences", var3);
        }

    }

    public void onStart() {
        super.onStart();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
        this.mPreferenceManager.setOnDisplayPreferenceDialogListener(this);
    }

    public void onStop() {
        super.onStop();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(null);
        this.mPreferenceManager.setOnDisplayPreferenceDialogListener(null);
    }

    public void onUnbindPreferences() {
    }

    public void onViewCreated(View var1, Bundle var2) {
        super.onViewCreated(var1, var2);
        if (var2 != null) {
            Bundle var3 = var2.getBundle("android:preferences");
            if (var3 != null) {
                PreferenceScreen var5 = this.getPreferenceScreen();
                if (var5 != null) {
                    var5.restoreHierarchyState(var3);
                }
            }
        }

        if (this.mHavePrefs) {
            this.bindPreferences();
            Runnable var4 = this.mSelectPreferenceRunnable;
            if (var4 != null) {
                var4.run();
                this.mSelectPreferenceRunnable = null;
            }
        }

        this.mInitDone = true;
    }

    public final void postBindPreferences() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    public final void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    public void setDivider(Drawable var1) {
        this.mDividerDecoration.setDivider(var1);
    }

    public void setDividerHeight(int var1) {
        this.mDividerDecoration.setDividerHeight(var1);
    }

    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(mPreferenceManager.inflateFromResource(getContext(), preferencesResId, getPreferenceScreen()));
    }

    public void setPreferencesFromResource(int var1, String var2) {
        this.requirePreferenceManager();
        PreferenceScreen var3 = this.mPreferenceManager.inflateFromResource(this.getContext(), var1, (PreferenceScreen) null);
        Object var4 = var3;
        if (var2 != null) {
            var4 = var3.findPreference(var2);
            if (!(var4 instanceof PreferenceScreen)) {
                StringBuilder var5 = new StringBuilder();
                var5.append("Preference object with key ");
                var5.append(var2);
                var5.append(" is not a PreferenceScreen");
                throw new IllegalArgumentException(var5.toString());
            }
        }

        this.setPreferenceScreen((PreferenceScreen) var4);
    }

    public final void unbindPreferences() {
        PreferenceScreen var1 = this.getPreferenceScreen();
        if (var1 != null) {
            var1.onDetached();
        }

        this.onUnbindPreferences();
    }

    public void seslSetRoundedCorner(boolean z) {
        this.mIsRoundedCorner = z;
    }

    public interface OnPreferenceDisplayDialogCallback {
        boolean onPreferenceDisplayDialog(PreferenceFragment var1, Preference var2);
    }

    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(PreferenceFragment var1, Preference var2);
    }

    public interface OnPreferenceStartScreenCallback {
        boolean onPreferenceStartScreen(PreferenceFragment var1, PreferenceScreen var2);
    }

    private class DividerDecoration extends RecyclerView.ItemDecoration {
        public boolean mAllowDividerAfterLastItem = true;
        public Drawable mDivider;
        public int mDividerHeight;

        public DividerDecoration() {
        }

        public final boolean canScrollUp(RecyclerView var1) {
            RecyclerView.LayoutManager var2 = var1.getLayoutManager();
            boolean var3 = var2 instanceof SeslLinearLayoutManager;
            boolean var4 = false;
            boolean var5 = var4;
            if (var3) {
                if (((SeslLinearLayoutManager) var2).findFirstVisibleItemPosition() > 0) {
                    var5 = true;
                } else {
                    var5 = false;
                }

                if (!var5) {
                    View var6 = var1.getChildAt(0);
                    if (var6 != null) {
                        var5 = var4;
                        if (var6.getTop() < var1.getPaddingTop()) {
                            var5 = true;
                        }
                    }
                }
            }

            return var5;
        }

        public void seslOnDispatchDraw(Canvas var1, RecyclerView var2, RecyclerView.State var3) {
            super.seslOnDispatchDraw(var1, var2, var3);
            int var4 = var2.getChildCount();
            int var5 = var2.getWidth();
            var2.getAdapter();
            int var6 = 0;
            PreferenceViewHolder var7 = null;

            PreferenceViewHolder var8;
            PreferenceViewHolder var10;
            for (var8 = var7; var6 < var4; var7 = var10) {
                View var9 = var2.getChildAt(var6);
                RecyclerView.ViewHolder var12 = var2.getChildViewHolder(var9);
                PreferenceViewHolder var13;
                if (var12 instanceof PreferenceViewHolder) {
                    var13 = (PreferenceViewHolder) var12;
                } else {
                    var13 = null;
                }

                if (var6 == 0) {
                    var10 = var13;
                } else {
                    var10 = var7;
                    if (var6 == 1) {
                        var8 = var13;
                        var10 = var7;
                    }
                }

                int var11 = (int) var9.getY() + var9.getHeight();
                if (this.mDivider != null && this.shouldDrawDividerBelow(var9, var2)) {
                    this.mDivider.setBounds(0, var11, var5, this.mDividerHeight + var11);
                    this.mDivider.draw(var1);
                }

                if (PreferenceFragment.this.mIsRoundedCorner && var13 != null && var13.seslIsBackgroundDrawn()) {
                    if (var13.seslIsDrawSubheaderRound()) {
                        PreferenceFragment.this.mSeslSubheaderRoundedCorner.setRoundedCorners(var13.seslGetDrawCorners());
                        PreferenceFragment.this.mSeslSubheaderRoundedCorner.drawRoundedCorner(var9, var1);
                    } else {
                        PreferenceFragment.this.mSeslRoundedCorner.setRoundedCorners(var13.seslGetDrawCorners());
                        PreferenceFragment.this.mSeslRoundedCorner.drawRoundedCorner(var9, var1);
                    }
                }

                ++var6;
            }

            if (PreferenceFragment.this.mIsRoundedCorner) {
                PreferenceFragment.this.mSeslListRoundedCorner.drawRoundedCorner(var1);
            }

        }

        public void setAllowDividerAfterLastItem(boolean var1) {
            this.mAllowDividerAfterLastItem = var1;
        }

        public void setDivider(Drawable var1) {
            if (var1 != null) {
                this.mDividerHeight = var1.getIntrinsicHeight();
            } else {
                this.mDividerHeight = 0;
            }

            this.mDivider = var1;
            PreferenceFragment.this.mList.invalidateItemDecorations();
        }

        public void setDividerHeight(int var1) {
            this.mDividerHeight = var1;
            PreferenceFragment.this.mList.invalidateItemDecorations();
        }

        public final boolean shouldDrawDividerBelow(View var1, RecyclerView var2) {
            RecyclerView.ViewHolder var3 = var2.getChildViewHolder(var1);
            boolean var4;
            if (var3 instanceof PreferenceViewHolder && ((PreferenceViewHolder) var3).isDividerAllowedBelow()) {
                var4 = true;
            } else {
                var4 = false;
            }

            if (!var4) {
                return false;
            } else {
                boolean var5 = this.mAllowDividerAfterLastItem;
                int var7 = var2.indexOfChild(var1);
                if (var7 < var2.getChildCount() - 1) {
                    RecyclerView.ViewHolder var6 = var2.getChildViewHolder(var2.getChildAt(var7 + 1));
                    if (var6 instanceof PreferenceViewHolder && ((PreferenceViewHolder) var6).isDividerAllowedAbove()) {
                        var5 = true;
                    } else {
                        var5 = false;
                    }
                }

                return var5;
            }
        }
    }
}
