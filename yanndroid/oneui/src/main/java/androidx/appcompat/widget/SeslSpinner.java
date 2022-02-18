package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.view.menu.ShowableListMenu;
import androidx.core.view.TintableBackgroundView;
import androidx.core.view.ViewCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.menu.PopupMenuUtils;
import de.dlyt.yanndroid.oneui.sesl.utils.ReflectUtils;

@SuppressLint("AppCompatCustomView")
public class SeslSpinner extends Spinner implements TintableBackgroundView {
    private boolean mIsOneUI4;
    protected boolean mBlurEffectEnabled;
    private static final int[] ATTRS_ANDROID_SPINNERMODE = {android.R.attr.spinnerMode};
    private static final int MAX_ITEMS_MEASURED = 15;
    private static final String TAG = "SeslSpinner";
    private static final int MODE_DIALOG = 0;
    private static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private final AppCompatBackgroundHelper mBackgroundTintHelper;
    private final Context mPopupContext;
    private ForwardingListener mForwardingListener;
    private SpinnerAdapter mTempAdapter;
    private final boolean mPopupSet;
    private SpinnerPopup mPopup;
    int mDropDownWidth;
    final Rect mTempRect = new Rect();

    public SeslSpinner(@NonNull Context context) {
        this(context, null);
    }

    public SeslSpinner(@NonNull Context context, int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    public SeslSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    public SeslSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, MODE_THEME);
    }

    public SeslSpinner(@NonNull Context context, @Nullable  AttributeSet attrs, int defStyleAttr, int mode) {
        this(context, attrs, defStyleAttr, mode, null);
    }

    @SuppressLint("RestrictedApi")
    public SeslSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        mBlurEffectEnabled = mIsOneUI4;

        ThemeUtils.checkAppCompatTheme(this, getContext());

        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.Spinner, defStyleAttr, 0);

        mBackgroundTintHelper = new AppCompatBackgroundHelper(this);

        if (popupTheme != null) {
            mPopupContext = new ContextThemeWrapper(context, popupTheme);
        } else {
            final int popupThemeResId = a.getResourceId(R.styleable.Spinner_popupTheme, 0);
            if (popupThemeResId != 0) {
                mPopupContext = new ContextThemeWrapper(context, popupThemeResId);
            } else {
                mPopupContext = context;
            }
        }

        if (mode == MODE_THEME) {
            TypedArray aa = null;
            try {
                aa = context.obtainStyledAttributes(attrs, ATTRS_ANDROID_SPINNERMODE, defStyleAttr, 0);
                if (aa.hasValue(0)) {
                    mode = aa.getInt(0, MODE_DIALOG);
                }
            } catch (Exception e) {
                Log.i(TAG, "Could not read android:spinnerMode", e);
            } finally {
                if (aa != null) {
                    aa.recycle();
                }
            }
        }

        switch (mode) {
            case MODE_DIALOG: {
                mPopup = new SeslSpinner.DialogPopup();
                mPopup.setPromptText(a.getString(R.styleable.Spinner_android_prompt));
                break;
            }
            case MODE_DROPDOWN: {
                final DropdownPopup popup = new DropdownPopup(mPopupContext, attrs, defStyleAttr);
                final TintTypedArray pa = TintTypedArray.obtainStyledAttributes(mPopupContext, attrs, R.styleable.Spinner, defStyleAttr, 0);
                mDropDownWidth = pa.getLayoutDimension(R.styleable.Spinner_android_dropDownWidth, LayoutParams.WRAP_CONTENT);
                popup.setBackgroundDrawable(pa.getDrawable(R.styleable.Spinner_android_popupBackground));
                popup.setPromptText(a.getString(R.styleable.Spinner_android_prompt));
                pa.recycle();
                mPopup = popup;
                mForwardingListener = new ForwardingListener(this) {
                    @Override
                    public ShowableListMenu getPopup() {
                        return popup;
                    }
                    @Override
                    @SuppressLint("SyntheticAccessor")
                    public boolean onForwardingStarted() {
                        if (!getInternalPopup().isShowing()) {
                            showPopup();
                        }
                        return true;
                    }
                };
            }
        }

        final CharSequence[] entries = a.getTextArray(R.styleable.Spinner_android_entries);
        if (entries != null) {
            final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, entries);
            adapter.setDropDownViewResource(R.layout.sesl_simple_spinner_dropdown_item);
            setAdapter(adapter);
        }

        a.recycle();

        mPopupSet = true;

        if (mTempAdapter != null) {
            setAdapter(mTempAdapter);
            mTempAdapter = null;
        }

        mBackgroundTintHelper.loadFromAttributes(attrs, defStyleAttr);
    }

    @Override
    public Context getPopupContext() {
        return mPopupContext;
    }

    @Override
    public void setPopupBackgroundDrawable(Drawable background) {
        if (mPopup != null) {
            mPopup.setBackgroundDrawable(background);
        } else {
            super.setPopupBackgroundDrawable(background);
        }
    }

    @Override
    public void setPopupBackgroundResource(@DrawableRes int resId) {
        setPopupBackgroundDrawable(AppCompatResources.getDrawable(getPopupContext(), resId));
    }

    @Override
    public Drawable getPopupBackground() {
        if (mPopup != null) {
            return mPopup.getBackground();
        } else {
            return super.getPopupBackground();
        }
    }

    @Override
    public void setDropDownVerticalOffset(int pixels) {
        if (mPopup != null) {
            mPopup.setVerticalOffset(pixels);
        } else {
            super.setDropDownVerticalOffset(pixels);
        }
    }

    @Override
    public int getDropDownVerticalOffset() {
        if (mPopup != null) {
            return mPopup.getVerticalOffset();
        } else {
            return super.getDropDownVerticalOffset();
        }
    }

    @Override
    public void setDropDownHorizontalOffset(int pixels) {
        if (mPopup != null) {
            mPopup.setHorizontalOriginalOffset(pixels);
            mPopup.setHorizontalOffset(pixels);
        } else {
            super.setDropDownHorizontalOffset(pixels);
        }
    }

    @Override
    public int getDropDownHorizontalOffset() {
        if (mPopup != null) {
            return mPopup.getHorizontalOffset();
        } else {
            return super.getDropDownHorizontalOffset();
        }
    }

    @Override
    public void setDropDownWidth(int pixels) {
        if (mPopup != null) {
            mDropDownWidth = pixels;
        } else {
            super.setDropDownWidth(pixels);
        }
    }

    @Override
    public int getDropDownWidth() {
        if (mPopup != null) {
            return mDropDownWidth;
        } else {
            return super.getDropDownWidth();
        }
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (!mPopupSet) {
            mTempAdapter = adapter;
            return;
        }

        super.setAdapter(adapter);

        if (mPopup != null) {
            final Context popupContext = mPopupContext == null ? getContext() : mPopupContext;
            mPopup.setAdapter(new DropDownAdapter(adapter, popupContext.getTheme()));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPopup != null && mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mForwardingListener != null && mForwardingListener.onTouch(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mPopup != null && MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
            final int measuredWidth = getMeasuredWidth();
            setMeasuredDimension(Math.min(Math.max(measuredWidth, compatMeasureContentWidth(getAdapter(), getBackground())), MeasureSpec.getSize(widthMeasureSpec)), getMeasuredHeight());
        }
    }

    @Override
    public boolean performClick() {
        if (mPopup != null) {
            if (!mPopup.isShowing()) {
                showPopup();
            }
            return true;
        }
        return super.performClick();
    }

    @Override
    public void setPrompt(CharSequence prompt) {
        if (mPopup != null) {
            mPopup.setPromptText(prompt);
        } else {
            super.setPrompt(prompt);
        }
    }

    @Override
    public CharSequence getPrompt() {
        return mPopup != null ? mPopup.getHintText() : super.getPrompt();
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        super.setBackgroundResource(resId);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundResource(resId);
        }
    }

    @Override
    public void setBackgroundDrawable(@Nullable Drawable background) {
        super.setBackgroundDrawable(background);
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.onSetBackgroundDrawable(background);
        }
    }

    @Override
    public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintList(tint);
        }
    }

    @Override
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        return mBackgroundTintHelper != null ? mBackgroundTintHelper.getSupportBackgroundTintList() : null;
    }

    @Override
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.setSupportBackgroundTintMode(tintMode);
        }
    }

    @Override
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        return mBackgroundTintHelper != null ? mBackgroundTintHelper.getSupportBackgroundTintMode() : null;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper.applySupportBackgroundTint();
        }
    }

    int compatMeasureContentWidth(SpinnerAdapter adapter, Drawable background) {
        if (adapter == null) {
            return 0;
        }
        int width = 0;
        View itemView = null;
        int itemType = 0;
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
        int start = Math.max(0, getSelectedItemPosition());
        final int end = Math.min(adapter.getCount(), start + MAX_ITEMS_MEASURED);
        final int count = end - start;
        start = Math.max(0, start - (MAX_ITEMS_MEASURED - count));
        for (int i = start; i < end; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, itemView.getMeasuredWidth());
        }

        if (background != null) {
            background.getPadding(mTempRect);
            width += mTempRect.left + mTempRect.right;
        }
        return width;
    }

    final SpinnerPopup getInternalPopup() {
        return mPopup;
    }

    void showPopup() {
        mPopup.show(getTextDirection(), getTextAlignment());
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final SeslSpinner.SavedState ss = new SeslSpinner.SavedState(super.onSaveInstanceState());
        ss.mShowDropdown = mPopup != null && mPopup.isShowing();
        return ss;
    }
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SeslSpinner.SavedState ss = (SeslSpinner.SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.mShowDropdown) {
            ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                final ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!getInternalPopup().isShowing()) {
                            showPopup();
                        }
                        final ViewTreeObserver vto = getViewTreeObserver();
                        if (vto != null) {
                            vto.removeOnGlobalLayoutListener(this);
                        }
                    }
                };
                vto.addOnGlobalLayoutListener(listener);
            }
        }
    }


    static class SavedState extends BaseSavedState {
        boolean mShowDropdown;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel in) {
            super(in);
            mShowDropdown = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (mShowDropdown ? 1 : 0));
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(@Nullable SpinnerAdapter adapter, @Nullable Resources.Theme dropDownTheme) {
            mAdapter = adapter;

            if (adapter instanceof ListAdapter) {
                mListAdapter = (ListAdapter) adapter;
            }

            if (dropDownTheme != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && adapter instanceof android.widget.ThemedSpinnerAdapter) {
                    final android.widget.ThemedSpinnerAdapter themedAdapter = (android.widget.ThemedSpinnerAdapter) adapter;
                    if (themedAdapter.getDropDownViewTheme() != dropDownTheme) {
                        themedAdapter.setDropDownViewTheme(dropDownTheme);
                    }
                } else if (adapter instanceof ThemedSpinnerAdapter) {
                    final ThemedSpinnerAdapter themedAdapter = (ThemedSpinnerAdapter) adapter;
                    if (themedAdapter.getDropDownViewTheme() == null) {
                        themedAdapter.setDropDownViewTheme(dropDownTheme);
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return mAdapter == null ? 0 : mAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return mAdapter == null ? null : mAdapter.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return mAdapter == null ? -1 : mAdapter.getItemId(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return (mAdapter == null) ? null : mAdapter.getDropDownView(position, convertView, parent);
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter != null && mAdapter.hasStableIds();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (mAdapter != null) {
                mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            } else {
                return true;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            final ListAdapter adapter = mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            } else {
                return true;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    interface SpinnerPopup {
        void setAdapter(ListAdapter adapter);

        void show(int textDirection, int textAlignment);

        void dismiss();

        boolean isShowing();

        void setPromptText(CharSequence hintText);
        CharSequence getHintText();

        void setBackgroundDrawable(Drawable bg);
        void setVerticalOffset(int px);
        void setHorizontalOffset(int px);
        void setHorizontalOriginalOffset(int px);
        int getHorizontalOriginalOffset();
        Drawable getBackground();
        int getVerticalOffset();
        int getHorizontalOffset();
    }

    class DialogPopup implements SpinnerPopup, DialogInterface.OnClickListener {
        AlertDialog mPopup;
        private ListAdapter mListAdapter;
        private CharSequence mPrompt;

        @Override
        public void dismiss() {
            if (mPopup != null) {
                mPopup.dismiss();
                mPopup = null;
            }
        }

        @Override
        public boolean isShowing() {
            return mPopup != null ? mPopup.isShowing() : false;
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            mListAdapter = adapter;
        }

        @Override
        public void setPromptText(CharSequence hintText) {
            mPrompt = hintText;
        }

        @Override
        public CharSequence getHintText() {
            return mPrompt;
        }

        @Override
        public void show(int textDirection, int textAlignment) {
            if (mListAdapter == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getPopupContext());
            if (mPrompt != null) {
                builder.setTitle(mPrompt);
            }
            mPopup = builder.setSingleChoiceItems(mListAdapter, getSelectedItemPosition(), this).create();
            final ListView listView = mPopup.getListView();
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            mPopup.show();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            setSelection(which);
            if (getOnItemClickListener() != null) {
                performItemClick(null, which, mListAdapter.getItemId(which));
            }
            dismiss();
        }

        @Override
        public void setBackgroundDrawable(Drawable bg) {
            Log.e(TAG, "Cannot set popup background for MODE_DIALOG, ignoring");
        }

        @Override
        public void setVerticalOffset(int px) {
            Log.e(TAG, "Cannot set vertical offset for MODE_DIALOG, ignoring");
        }

        @Override
        public void setHorizontalOffset(int px) {
            Log.e(TAG, "Cannot set horizontal offset for MODE_DIALOG, ignoring");
        }

        @Override
        public Drawable getBackground() {
            return null;
        }

        @Override
        public int getVerticalOffset() {
            return 0;
        }

        @Override
        public int getHorizontalOffset() {
            return 0;
        }

        @Override
        public void setHorizontalOriginalOffset(int px) {
            Log.e(TAG, "Cannot set horizontal (original) offset for MODE_DIALOG, ignoring");
        }

        @Override
        public int getHorizontalOriginalOffset() {
            return 0;
        }
    }

    class DropdownPopup extends ListPopupWindow implements SpinnerPopup {
        private CharSequence mHintText;
        ListAdapter mAdapter;
        private final Rect mVisibleRect = new Rect();
        private int mOriginalHorizontalOffset;

        public DropdownPopup(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            setAnchorView(SeslSpinner.this);
            setModal(true);
            setPromptPosition(POSITION_PROMPT_ABOVE);

            setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    SeslSpinner.this.setSelection(position);
                    if (getOnItemClickListener() != null) {
                        SeslSpinner.this.performItemClick(v, position, mAdapter.getItemId(position));
                    }
                    dismiss();
                }
            });
        }

        @Override
        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            mAdapter = adapter;
        }

        @Override
        public CharSequence getHintText() {
            return mHintText;
        }

        @Override
        public void setPromptText(CharSequence hintText) {
            mHintText = hintText;
        }

        @SuppressLint("RestrictedApi")
        void computeContentWidth() {
            final Drawable background = getBackground();
            int hOffset = 0;
            if (background != null) {
                background.getPadding(mTempRect);
                hOffset = ViewUtils.isLayoutRtl(SeslSpinner.this) ? mTempRect.right : -mTempRect.left;
            } else {
                mTempRect.left = mTempRect.right = 0;
            }
            final int spinnerPaddingLeft = SeslSpinner.this.getPaddingLeft();
            final int spinnerPaddingRight = SeslSpinner.this.getPaddingRight();
            final int spinnerWidth = SeslSpinner.this.getWidth();
            if (mDropDownWidth == WRAP_CONTENT) {
                int contentWidth = compatMeasureContentWidth((SpinnerAdapter) mAdapter, getBackground());
                final int contentWidthLimit = getContext().getResources().getDisplayMetrics().widthPixels - mTempRect.left - mTempRect.right;
                if (contentWidth > contentWidthLimit) {
                    contentWidth = contentWidthLimit;
                }
                setContentWidth(Math.max(contentWidth, spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight));
            } else if (mDropDownWidth == MATCH_PARENT) {
                setContentWidth(spinnerWidth - spinnerPaddingLeft - spinnerPaddingRight);
            } else {
                setContentWidth(mDropDownWidth);
            }
            if (ViewUtils.isLayoutRtl(SeslSpinner.this)) {
                hOffset += spinnerWidth - spinnerPaddingRight - getWidth() - getHorizontalOriginalOffset();
            } else {
                hOffset += spinnerPaddingLeft + getHorizontalOriginalOffset();
            }
            setHorizontalOffset(hOffset);
        }

        @Override
        public void show(int textDirection, int textAlignment) {
            final boolean wasShowing = isShowing();

            computeContentWidth();

            setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
            super.show();

            PopupWindow popup = ((PopupWindow) ReflectUtils.genericGetField(ListPopupWindow.class, this, "mPopup"));
            if (mBlurEffectEnabled) {
                PopupMenuUtils.setupBlurEffect(mPopupContext, mIsOneUI4, popup, getListView());
            }
            PopupMenuUtils.fixPopupRoundedCorners(popup);

            final ListView listView = getListView();
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setTextDirection(textDirection);
            listView.setTextAlignment(textAlignment);
            setSelection(SeslSpinner.this.getSelectedItemPosition());

            if (wasShowing) {
                return;
            }

            final ViewTreeObserver vto = getViewTreeObserver();
            if (vto != null) {
                final ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        computeContentWidth();
                        DropdownPopup.super.show();
                    }
                };
                vto.addOnGlobalLayoutListener(layoutListener);
                setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        final ViewTreeObserver vto = getViewTreeObserver();
                        if (vto != null) {
                            vto.removeGlobalOnLayoutListener(layoutListener);
                        }
                    }
                });
            }
        }

        boolean isVisibleToUser(View view) {
            return ViewCompat.isAttachedToWindow(view) && view.getGlobalVisibleRect(mVisibleRect);
        }

        @Override
        public void setHorizontalOriginalOffset(int px) {
            mOriginalHorizontalOffset = px;
        }

        @Override
        public int getHorizontalOriginalOffset() {
            return mOriginalHorizontalOffset;
        }
    }
}