package de.dlyt.yanndroid.oneui.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import de.dlyt.yanndroid.oneui.dialog.AlertDialog;

public abstract class PreferenceDialogFragmentCompat extends DialogFragment implements DialogInterface.OnClickListener {
    protected static final String ARG_KEY = "key";
    private static final String SAVE_STATE_TITLE = "PreferenceDialogFragment.title";
    private static final String SAVE_STATE_POSITIVE_TEXT = "PreferenceDialogFragment.positiveText";
    private static final String SAVE_STATE_NEGATIVE_TEXT = "PreferenceDialogFragment.negativeText";
    private static final String SAVE_STATE_MESSAGE = "PreferenceDialogFragment.message";
    private static final String SAVE_STATE_LAYOUT = "PreferenceDialogFragment.layout";
    private static final String SAVE_STATE_ICON = "PreferenceDialogFragment.icon";
    private DialogPreference mPreference;
    private CharSequence mDialogTitle;
    private CharSequence mPositiveButtonText;
    private CharSequence mNegativeButtonText;
    private CharSequence mDialogMessage;
    private int mDialogLayoutRes;
    private BitmapDrawable mDialogIcon;
    private int mWhichButtonClicked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Fragment rawFragment = getTargetFragment();
        if (!(rawFragment instanceof DialogPreference.TargetFragment)) {
            throw new IllegalStateException("Target fragment must implement TargetFragment" + " interface");
        }

        final DialogPreference.TargetFragment fragment = (DialogPreference.TargetFragment) rawFragment;

        final String key = getArguments().getString(ARG_KEY);
        if (savedInstanceState == null) {
            mPreference = (DialogPreference) fragment.findPreference(key);
            mDialogTitle = mPreference.getDialogTitle();
            mPositiveButtonText = mPreference.getPositiveButtonText();
            mNegativeButtonText = mPreference.getNegativeButtonText();
            mDialogMessage = mPreference.getDialogMessage();
            mDialogLayoutRes = mPreference.getDialogLayoutResource();

            final Drawable icon = mPreference.getDialogIcon();
            if (icon == null || icon instanceof BitmapDrawable) {
                mDialogIcon = (BitmapDrawable) icon;
            } else {
                final Bitmap bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
                mDialogIcon = new BitmapDrawable(getResources(), bitmap);
            }
        } else {
            mDialogTitle = savedInstanceState.getCharSequence(SAVE_STATE_TITLE);
            mPositiveButtonText = savedInstanceState.getCharSequence(SAVE_STATE_POSITIVE_TEXT);
            mNegativeButtonText = savedInstanceState.getCharSequence(SAVE_STATE_NEGATIVE_TEXT);
            mDialogMessage = savedInstanceState.getCharSequence(SAVE_STATE_MESSAGE);
            mDialogLayoutRes = savedInstanceState.getInt(SAVE_STATE_LAYOUT, 0);
            final Bitmap bitmap = savedInstanceState.getParcelable(SAVE_STATE_ICON);
            if (bitmap != null) {
                mDialogIcon = new BitmapDrawable(getResources(), bitmap);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharSequence(SAVE_STATE_TITLE, mDialogTitle);
        outState.putCharSequence(SAVE_STATE_POSITIVE_TEXT, mPositiveButtonText);
        outState.putCharSequence(SAVE_STATE_NEGATIVE_TEXT, mNegativeButtonText);
        outState.putCharSequence(SAVE_STATE_MESSAGE, mDialogMessage);
        outState.putInt(SAVE_STATE_LAYOUT, mDialogLayoutRes);
        if (mDialogIcon != null) {
            outState.putParcelable(SAVE_STATE_ICON, mDialogIcon.getBitmap());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;

        final AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(mDialogTitle).setIcon(mDialogIcon).setPositiveButton(mPositiveButtonText, this).setNegativeButton(mNegativeButtonText, this);

        View contentView = onCreateDialogView(context);
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.setView(contentView);
        } else {
            builder.setMessage(mDialogMessage);
        }

        onPrepareDialogBuilder(builder);

        final Dialog dialog = builder.create();
        if (needInputMethod()) {
            requestInputMethod(dialog);
        }

        return dialog;
    }

    public DialogPreference getPreference() {
        if (mPreference == null) {
            final String key = getArguments().getString(ARG_KEY);
            final DialogPreference.TargetFragment fragment = (DialogPreference.TargetFragment) getTargetFragment();
            mPreference = (DialogPreference) fragment.findPreference(key);
        }
        return mPreference;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    }

    protected boolean needInputMethod() {
        return false;
    }

    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    protected View onCreateDialogView(Context context) {
        final int resId = mDialogLayoutRes;
        if (resId == 0) {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(resId, null);
    }

    protected void onBindDialogView(View view) {
        View dialogMessageView = view.findViewById(android.R.id.message);

        if (dialogMessageView != null) {
            final CharSequence message = mDialogMessage;
            int newVisibility = View.GONE;

            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView instanceof TextView) {
                    ((TextView) dialogMessageView).setText(message);
                }

                newVisibility = View.VISIBLE;
            }

            if (dialogMessageView.getVisibility() != newVisibility) {
                dialogMessageView.setVisibility(newVisibility);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mWhichButtonClicked = which;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    }

    public abstract void onDialogClosed(boolean positiveResult);
}
