package de.dlyt.yanndroid.samsung;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.Styleable;
import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.appcompat.util.SeslMisc;
import androidx.appcompat.widget.SeslToggleSwitch;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.ArrayList;
import java.util.List;

public class SwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private static final int SWITCH_OFF_STRING_RESOURCE_ID = R.string.sesl_switchbar_off_text;
    private static final int SWITCH_ON_STRING_RESOURCE_ID = R.string.sesl_switchbar_on_text;
    private LinearLayout mBackground;
    @ColorInt
    private int mBackgroundActivatedColor;
    @ColorInt
    private int mBackgroundColor;
    private SwitchBarDelegate mDelegate;
    private String mLabel;
    @ColorInt
    private int mOffTextColor;
    @StringRes
    private int mOffTextId;
    @ColorInt
    private int mOnTextColor;
    @StringRes
    private int mOnTextId;
    private ProgressBar mProgressBar;
    private String mSessionDesc;
    private SeslToggleSwitch mSwitch;
    private final List<OnSwitchChangeListener> mSwitchChangeListeners;
    private TextView mTextView;

    public interface OnSwitchChangeListener {
        void onSwitchChanged(SwitchCompat switchCompat, boolean z);
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            /* class de.dlyt.yanndroid.samsung.SeslSwitchBar.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        boolean checked;
        boolean visible;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.checked = ((Boolean) parcel.readValue(null)).booleanValue();
            this.visible = ((Boolean) parcel.readValue(null)).booleanValue();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public String toString() {
            return "SeslSwitchBar.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checked=" + this.checked + " visible=" + this.visible + "}";
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeValue(Boolean.valueOf(this.checked));
            parcel.writeValue(Boolean.valueOf(this.visible));
        }
    }

    /* access modifiers changed from: private */
    public static class SwitchBarDelegate extends AccessibilityDelegateCompat {
        private String mSessionName = "";
        private SeslToggleSwitch mSwitch;
        private TextView mText;

        public SwitchBarDelegate(View view) {
            this.mText = (TextView) view.findViewById(R.id.sesl_switchbar_text);
            this.mSwitch = (SeslToggleSwitch) view.findViewById(R.id.sesl_switchbar_switch);
        }

        @Override // androidx.core.view.AccessibilityDelegateCompat
        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            String string = view.getContext().getResources().getString(this.mSwitch.isChecked() ? SwitchBar.SWITCH_ON_STRING_RESOURCE_ID : SwitchBar.SWITCH_OFF_STRING_RESOURCE_ID);
            StringBuilder sb = new StringBuilder();
            CharSequence text = this.mText.getText();
            if (!TextUtils.isEmpty(this.mSessionName)) {
                sb.append(this.mSessionName);
                sb.append(", ");
            }
            if (!TextUtils.equals(string, text) && !TextUtils.isEmpty(text)) {
                sb.append(text);
                sb.append(", ");
            }
            accessibilityNodeInfoCompat.setText(sb.toString());
        }

        public void setSessionName(String str) {
            this.mSessionName = str;
        }
    }

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.seslSwitchBarStyle);
    }

    public SwitchBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SwitchBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mSwitchChangeListeners = new ArrayList();
        this.mSessionDesc = null;
        LayoutInflater.from(context).inflate(R.layout.sesl_switchbar, this);
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, Styleable.styleable.SeslSwitchBar, i, i2);
        this.mBackgroundColor = obtainStyledAttributes.getColor(Styleable.styleable.SeslSwitchBar_seslSwitchBarBackgroundColor, resources.getColor(R.color.sesl_switchbar_off_background_color_light));
        this.mBackgroundActivatedColor = obtainStyledAttributes.getColor(Styleable.styleable.SeslSwitchBar_seslSwitchBarBackgroundActivatedColor, resources.getColor(R.color.sesl_switchbar_on_background_color_light));
        this.mOnTextColor = obtainStyledAttributes.getColor(Styleable.styleable.SeslSwitchBar_seslSwitchBarTextActivatedColor, resources.getColor(R.color.sesl_switchbar_text_color));
        this.mOffTextColor = obtainStyledAttributes.getColor(Styleable.styleable.SeslSwitchBar_seslSwitchBarTextColor, resources.getColor(R.color.sesl_switchbar_text_color));
        obtainStyledAttributes.recycle();
        this.mProgressBar = (ProgressBar) findViewById(R.id.sesl_switchbar_progress);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.sesl_switchbar_container);
        this.mBackground = linearLayout;
        linearLayout.setOnClickListener(new OnClickListener() {
            /* class de.dlyt.yanndroid.samsung.SeslSwitchBar.AnonymousClass1 */

            public void onClick(View view) {
                if (SwitchBar.this.mSwitch != null && SwitchBar.this.mSwitch.isEnabled()) {
                    SwitchBar.this.mSwitch.setChecked(!SwitchBar.this.mSwitch.isChecked());
                }
            }
        });
        this.mOnTextId = SWITCH_ON_STRING_RESOURCE_ID;
        this.mOffTextId = SWITCH_OFF_STRING_RESOURCE_ID;
        TextView textView = (TextView) findViewById(R.id.sesl_switchbar_text);
        this.mTextView = textView;
        ((MarginLayoutParams) textView.getLayoutParams()).setMarginStart((int) resources.getDimension(R.dimen.sesl_switchbar_margin_start));
        SeslToggleSwitch seslToggleSwitch = (SeslToggleSwitch) findViewById(R.id.sesl_switchbar_switch);
        this.mSwitch = seslToggleSwitch;
        seslToggleSwitch.setSaveEnabled(false);
        this.mSwitch.setFocusable(false);
        this.mSwitch.setClickable(false);
        this.mSwitch.setOnCheckedChangeListener(this);
        setSwitchBarText(this.mOnTextId, this.mOffTextId);
        addOnSwitchChangeListener(new OnSwitchChangeListener() {
            /* class de.dlyt.yanndroid.samsung.SeslSwitchBar.AnonymousClass2 */

            @Override // de.dlyt.yanndroid.samsung.SeslSwitchBar.OnSwitchChangeListener
            public void onSwitchChanged(SwitchCompat switchCompat, boolean z) {
                SwitchBar.this.setTextViewLabelAndBackground(z);
            }
        });
        ((MarginLayoutParams) this.mSwitch.getLayoutParams()).setMarginEnd((int) resources.getDimension(R.dimen.sesl_switchbar_margin_end));
        SwitchBarDelegate switchBarDelegate = new SwitchBarDelegate(this);
        this.mDelegate = switchBarDelegate;
        ViewCompat.setAccessibilityDelegate(this.mBackground, switchBarDelegate);
        setSessionDescription(getActivityTitle());
    }

    private String getActivityTitle() {
        for (Context context = getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                CharSequence title = ((Activity) context).getTitle();
                return title != null ? title.toString() : "";
            }
        }
        return "";
    }

    private void propagateChecked(boolean z) {
        int size = this.mSwitchChangeListeners.size();
        for (int i = 0; i < size; i++) {
            this.mSwitchChangeListeners.get(i).onSwitchChanged(this.mSwitch, z);
        }
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener onSwitchChangeListener) {
        if (!this.mSwitchChangeListeners.contains(onSwitchChangeListener)) {
            this.mSwitchChangeListeners.add(onSwitchChangeListener);
            return;
        }
        throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
    }

    public CharSequence getAccessibilityClassName() {
        return SwitchBar.class.getName();
    }

    public final SeslToggleSwitch getSwitch() {
        return this.mSwitch;
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(GONE);
            this.mSwitch.setOnCheckedChangeListener(null);
        }
        this.mDelegate.setSessionName(" ");
        this.mSessionDesc = null;
    }

    public boolean isChecked() {
        return this.mSwitch.isChecked();
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        propagateChecked(z);
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mSwitch.setCheckedInternal(savedState.checked);
        setTextViewLabelAndBackground(savedState.checked);
        setVisibility(savedState.visible ? VISIBLE : GONE);
        this.mSwitch.setOnCheckedChangeListener(savedState.visible ? this : null);
        requestLayout();
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.checked = this.mSwitch.isChecked();
        savedState.visible = isShowing();
        return savedState;
    }

    public boolean performClick() {
        return this.mSwitch.performClick();
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener onSwitchChangeListener) {
        if (this.mSwitchChangeListeners.contains(onSwitchChangeListener)) {
            this.mSwitchChangeListeners.remove(onSwitchChangeListener);
            return;
        }
        throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
    }

    public void setChecked(boolean z) {
        setTextViewLabelAndBackground(z);
        this.mSwitch.setChecked(z);
    }

    public void setCheckedInternal(boolean z) {
        setTextViewLabelAndBackground(z);
        this.mSwitch.setCheckedInternal(z);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mTextView.setEnabled(z);
        this.mSwitch.setEnabled(z);
        this.mBackground.setEnabled(z);
        setTextViewLabelAndBackground(isChecked());
    }

    public void setProgressBarVisible(boolean z) {
        try {
            this.mProgressBar.setVisibility(z ? VISIBLE : GONE);
        } catch (IndexOutOfBoundsException e) {
            Log.i("SetProgressBarVisible", "Invalid argument" + e);
        }
    }

    public void setSessionDescription(String str) {
        this.mSessionDesc = str;
        this.mDelegate.setSessionName(str);
    }

    public void setSwitchBarText(int i, int i2) {
        this.mOnTextId = i;
        this.mOffTextId = i2;
        setTextViewLabelAndBackground(isChecked());
    }

    public void setTextViewLabel(boolean z) {
        String string = getResources().getString(z ? this.mOnTextId : this.mOffTextId);
        this.mLabel = string;
        this.mTextView.setText(string);
    }

    public void setTextViewLabelAndBackground(boolean z) {
        TextView textView;
        float f;
        this.mLabel = getResources().getString(z ? this.mOnTextId : this.mOffTextId);
        DrawableCompat.setTintList(DrawableCompat.wrap(this.mBackground.getBackground()).mutate(), ColorStateList.valueOf(z ? this.mBackgroundActivatedColor : this.mBackgroundColor));
        this.mTextView.setTextColor(z ? this.mOnTextColor : this.mOffTextColor);
        if (isEnabled()) {
            textView = this.mTextView;
            f = 1.0f;
        } else if (!SeslMisc.isLightTheme(getContext()) || !z) {
            textView = this.mTextView;
            f = 0.4f;
        } else {
            textView = this.mTextView;
            f = 0.55f;
        }
        textView.setAlpha(f);
        String str = this.mLabel;
        if (str == null || !str.contentEquals(this.mTextView.getText())) {
            this.mTextView.setText(this.mLabel);
        }
    }

    public void show() {
        String str;
        SwitchBarDelegate switchBarDelegate;
        if (!isShowing()) {
            setVisibility(VISIBLE);
            this.mSwitch.setOnCheckedChangeListener(this);
        }
        if (TextUtils.isEmpty(this.mSessionDesc)) {
            switchBarDelegate = this.mDelegate;
            str = getActivityTitle();
        } else {
            switchBarDelegate = this.mDelegate;
            str = this.mSessionDesc;
        }
        switchBarDelegate.setSessionName(str);
    }

    public void updateHorizontalMargins() {
        Resources resources = getResources();
        TextView textView = this.mTextView;
        if (textView != null) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) textView.getLayoutParams();
            marginLayoutParams.setMarginStart((int) resources.getDimension(R.dimen.sesl_switchbar_margin_start));
            this.mTextView.setLayoutParams(marginLayoutParams);
        }
        SeslToggleSwitch seslToggleSwitch = this.mSwitch;
        if (seslToggleSwitch != null) {
            MarginLayoutParams marginLayoutParams2 = (MarginLayoutParams) seslToggleSwitch.getLayoutParams();
            marginLayoutParams2.setMarginEnd((int) resources.getDimension(R.dimen.sesl_switchbar_margin_end));
            this.mSwitch.setLayoutParams(marginLayoutParams2);
        }
    }
}
