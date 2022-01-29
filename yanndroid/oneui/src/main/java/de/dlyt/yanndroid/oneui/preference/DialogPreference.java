package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public abstract class DialogPreference extends Preference {
    private CharSequence mDialogTitle;
    private CharSequence mDialogMessage;
    private Drawable mDialogIcon;
    private CharSequence mPositiveButtonText;
    private CharSequence mNegativeButtonText;
    private int mDialogLayoutResId;

    @SuppressLint("RestrictedApi")
    public DialogPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);

        mDialogTitle = TypedArrayUtils.getString(a, R.styleable.DialogPreference_dialogTitle, R.styleable.DialogPreference_android_dialogTitle);
        if (mDialogTitle == null) {
            mDialogTitle = getTitle();
        }

        mDialogMessage = TypedArrayUtils.getString(a, R.styleable.DialogPreference_dialogMessage, R.styleable.DialogPreference_android_dialogMessage);

        mDialogIcon = TypedArrayUtils.getDrawable(a, R.styleable.DialogPreference_dialogIcon, R.styleable.DialogPreference_android_dialogIcon);

        mPositiveButtonText = TypedArrayUtils.getString(a, R.styleable.DialogPreference_positiveButtonText, R.styleable.DialogPreference_android_positiveButtonText);

        mNegativeButtonText = TypedArrayUtils.getString(a, R.styleable.DialogPreference_negativeButtonText, R.styleable.DialogPreference_android_negativeButtonText);

        mDialogLayoutResId = TypedArrayUtils.getResourceId(a, R.styleable.DialogPreference_dialogLayout, R.styleable.DialogPreference_android_dialogLayout, 0);

        a.recycle();
    }

    public DialogPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public DialogPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle, android.R.attr.dialogPreferenceStyle));
    }

    public DialogPreference(@NonNull Context context) {
        this(context, null);
    }

    public void setDialogTitle(@Nullable CharSequence dialogTitle) {
        mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(getContext().getString(dialogTitleResId));
    }

    @Nullable
    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }

    public void setDialogMessage(@Nullable CharSequence dialogMessage) {
        mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage(getContext().getString(dialogMessageResId));
    }

    @Nullable
    public CharSequence getDialogMessage() {
        return mDialogMessage;
    }

    public void setDialogIcon(@Nullable Drawable dialogIcon) {
        mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(int dialogIconRes) {
        mDialogIcon = AppCompatResources.getDrawable(getContext(), dialogIconRes);
    }

    @Nullable
    public Drawable getDialogIcon() {
        return mDialogIcon;
    }

    public void setPositiveButtonText(@Nullable CharSequence positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText(getContext().getString(positiveButtonTextResId));
    }

    @Nullable
    public CharSequence getPositiveButtonText() {
        return mPositiveButtonText;
    }

    public void setNegativeButtonText(@Nullable CharSequence negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText(getContext().getString(negativeButtonTextResId));
    }

    @Nullable
    public CharSequence getNegativeButtonText() {
        return mNegativeButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    @Override
    protected void onClick() {
        getPreferenceManager().showDialog(this);
    }

    public interface TargetFragment {
        @SuppressWarnings("TypeParameterUnusedInFormals")
        @Nullable
        <T extends Preference> T findPreference(@NonNull CharSequence key);
    }
}
