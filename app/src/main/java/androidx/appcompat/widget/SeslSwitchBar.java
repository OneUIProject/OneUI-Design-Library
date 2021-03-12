package androidx.appcompat.widget;

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
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.R;
import de.dlyt.yanndroid.samsung.RdotStyleable;


public class SeslSwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private static final int SWITCH_OFF_STRING_RESOURCE_ID = R.string.sesl_switch_off;
    private static final int SWITCH_ON_STRING_RESOURCE_ID = R.string.sesl_switch_on;
    private LinearLayout mBackground;
    private int mBackgroundActivatedColor;
    private int mBackgroundColor;
    private SwitchBarDelegate mDelegate;
    private String mLabel;
    private int mOffTextColor;
    private int mOffTextId;
    private int mOnTextColor;
    private int mOnTextId;
    private SeslProgressBar mProgressBar;
    private String mSessionDesc;
    private SeslToggleSwitch mSwitch;
    private final List<OnSwitchChangeListener> mSwitchChangeListeners;
    private TextView mTextView;

    public interface OnSwitchChangeListener {
        void onSwitchChanged(SwitchCompat switchCompat, boolean z);
    }

    public SeslSwitchBar(Context context) {
        this(context, null);
    }

    public SeslSwitchBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.seslSwitchBarStyle);
    }

    public SeslSwitchBar(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SeslSwitchBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mSwitchChangeListeners = new ArrayList();
        this.mSessionDesc = null;
        LayoutInflater.from(context).inflate(R.layout.sesl_switchbar, this);
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, RdotStyleable.styleable.SeslSwitchBar, i, i2);
        this.mBackgroundColor = obtainStyledAttributes.getColor(RdotStyleable.styleable.SeslSwitchBar_seslSwitchBarBackgroundColor, resources.getColor(R.color.sesl_switchbar_off_background_color));
        this.mBackgroundActivatedColor = obtainStyledAttributes.getColor(RdotStyleable.styleable.SeslSwitchBar_seslSwitchBarBackgroundActivatedColor, resources.getColor(R.color.sesl_switchbar_on_background_color));
        this.mOnTextColor = obtainStyledAttributes.getColor(RdotStyleable.styleable.SeslSwitchBar_seslSwitchBarTextActivatedColor, resources.getColor(R.color.sesl_switchbar_on_text_color));
        this.mOffTextColor = obtainStyledAttributes.getColor(RdotStyleable.styleable.SeslSwitchBar_seslSwitchBarTextColor, resources.getColor(R.color.sesl_switchbar_off_text_color));
        obtainStyledAttributes.recycle();
        this.mProgressBar = (SeslProgressBar) findViewById(R.id.sesl_switchbar_progress);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.sesl_switchbar_container);
        this.mBackground = linearLayout;
        linearLayout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (SeslSwitchBar.this.mSwitch != null && SeslSwitchBar.this.mSwitch.isEnabled()) {
                    SeslSwitchBar seslSwitchBar = SeslSwitchBar.this;
                    seslSwitchBar.setChecked(!seslSwitchBar.mSwitch.isChecked());
                }
            }
        });
        this.mOnTextId = SWITCH_ON_STRING_RESOURCE_ID;
        this.mOffTextId = SWITCH_OFF_STRING_RESOURCE_ID;
        TextView textView = (TextView) findViewById(R.id.sesl_switchbar_text);
        this.mTextView = textView;
        ((ViewGroup.MarginLayoutParams) textView.getLayoutParams()).setMarginStart((int) resources.getDimension(R.dimen.sesl_switchbar_margin_start));
        SeslToggleSwitch seslToggleSwitch = (SeslToggleSwitch) findViewById(R.id.sesl_switchbar_switch);
        this.mSwitch = seslToggleSwitch;
        seslToggleSwitch.setSaveEnabled(false);
        this.mSwitch.setOnCheckedChangeListener(this);
        setSwitchBarText(R.string.sesl_switch_on, R.string.sesl_switch_off);
        addOnSwitchChangeListener(new OnSwitchChangeListener() {

            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean z) {
                SeslSwitchBar.this.setTextViewLabelAndBackground(z);
            }
        });
        ((ViewGroup.MarginLayoutParams) this.mSwitch.getLayoutParams()).setMarginEnd((int) resources.getDimension(R.dimen.sesl_switchbar_margin_end));
        SwitchBarDelegate switchBarDelegate = new SwitchBarDelegate(this);
        this.mDelegate = switchBarDelegate;
        ViewCompat.setAccessibilityDelegate(this.mBackground, switchBarDelegate);
        ViewCompat.setAccessibilityDelegate(this.mSwitch, this.mDelegate);
        setSessionDescription(getActivityTitle());
    }

    public boolean performClick() {
        return this.mSwitch.performClick();
    }

    public void setProgressBarVisible(boolean z) {
        try {
            this.mProgressBar.setVisibility(z ? VISIBLE : GONE);
        } catch (IndexOutOfBoundsException e) {
            Log.i("SetProgressBarVisible", "Invalid argument" + e);
        }
    }

    public void setTextViewLabelAndBackground(boolean z) {
        this.mLabel = getResources().getString(z ? this.mOnTextId : this.mOffTextId);
        DrawableCompat.setTintList(DrawableCompat.wrap(this.mBackground.getBackground()).mutate(), ColorStateList.valueOf(z ? this.mBackgroundActivatedColor : this.mBackgroundColor));
        this.mTextView.setTextColor(z ? this.mOnTextColor : this.mOffTextColor);
        if (isEnabled()) {
            this.mTextView.setAlpha(1.0f);
            this.mBackground.getBackground().setAlpha(255);
        } else {
            this.mTextView.setAlpha(0.4f);
            this.mBackground.getBackground().setAlpha(102);
        }
        String str = this.mLabel;
        if (str == null || !str.contentEquals(this.mTextView.getText())) {
            this.mTextView.setText(this.mLabel);
        }
    }

    public void setTextViewLabel(boolean z) {
        String string = getResources().getString(z ? R.string.sesl_switch_on : R.string.sesl_switch_off);
        this.mLabel = string;
        this.mTextView.setText(string);
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

    public void setChecked(boolean z) {
        setTextViewLabelAndBackground(z);
        this.mSwitch.setChecked(z);
    }

    public void setCheckedInternal(boolean z) {
        setTextViewLabelAndBackground(z);
        this.mSwitch.setCheckedInternal(z);
    }

    public boolean isChecked() {
        return this.mSwitch.isChecked();
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mTextView.setEnabled(z);
        this.mSwitch.setEnabled(z);
        setTextViewLabelAndBackground(isChecked());
    }

    public final SeslToggleSwitch getSwitch() {
        return this.mSwitch;
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(VISIBLE);
            this.mSwitch.setOnCheckedChangeListener(this);
        }
        if (TextUtils.isEmpty(this.mSessionDesc)) {
            this.mDelegate.setSessionName(getActivityTitle());
        } else {
            this.mDelegate.setSessionName(this.mSessionDesc);
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(GONE);
            this.mSwitch.setOnCheckedChangeListener(null);
        }
        this.mDelegate.setSessionName(" ");
        this.mSessionDesc = null;
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    private void propagateChecked(boolean z) {
        int size = this.mSwitchChangeListeners.size();
        for (int i = 0; i < size; i++) {
            this.mSwitchChangeListeners.get(i).onSwitchChanged(this.mSwitch, z);
        }
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        propagateChecked(z);
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener onSwitchChangeListener) {
        if (!this.mSwitchChangeListeners.contains(onSwitchChangeListener)) {
            this.mSwitchChangeListeners.add(onSwitchChangeListener);
            return;
        }
        throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener onSwitchChangeListener) {
        if (this.mSwitchChangeListeners.contains(onSwitchChangeListener)) {
            this.mSwitchChangeListeners.remove(onSwitchChangeListener);
            return;
        }
        throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class androidx.appcompat.widget.SeslSwitchBar.SavedState.AnonymousClass1 */

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

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.checked = ((Boolean) parcel.readValue(null)).booleanValue();
            this.visible = ((Boolean) parcel.readValue(null)).booleanValue();
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeValue(Boolean.valueOf(this.checked));
            parcel.writeValue(Boolean.valueOf(this.visible));
        }

        public String toString() {
            return "SeslSwitchBar.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checked=" + this.checked + " visible=" + this.visible + "}";
        }
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.checked = this.mSwitch.isChecked();
        savedState.visible = isShowing();
        return savedState;
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

    private String getActivityTitle() {
        for (Context context = getContext(); context instanceof ContextWrapper; context = ((ContextWrapper) context).getBaseContext()) {
            if (context instanceof Activity) {
                CharSequence title = ((Activity) context).getTitle();
                if (title != null) {
                    return title.toString();
                }
                return "";
            }
        }
        return "";
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

        public void setSessionName(String str) {
            this.mSessionName = str;
        }

        @Override // androidx.core.view.AccessibilityDelegateCompat
        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            String string = view.getContext().getResources().getString(this.mSwitch.isChecked() ? SeslSwitchBar.SWITCH_ON_STRING_RESOURCE_ID : SeslSwitchBar.SWITCH_OFF_STRING_RESOURCE_ID);
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
            if (view instanceof SeslToggleSwitch) {
                accessibilityNodeInfoCompat.setText(sb.toString());
            } else if (view instanceof LinearLayout) {
                String string2 = view.getContext().getResources().getString(R.string.sesl_switch);
                sb.append(string);
                sb.append(", ");
                sb.append(string2);
                view.setContentDescription(sb.toString());
            }
        }
    }
}
