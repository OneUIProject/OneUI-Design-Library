package de.dlyt.yanndroid.oneui.dialog;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.NumberFormat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.dialog.widget.DialogTitle;
import de.dlyt.yanndroid.oneui.widget.ProgressBar;

public class ProgressDialog extends AlertDialog {
    public static final int STYLE_SPINNER = 0;
    public static final int STYLE_HORIZONTAL = 1;
    public static final int STYLE_CIRCLE = 2;
    private Context mContext;
    private boolean mIsOneUI4;
    private View mContentView;
    private ProgressBar mProgress;
    private TextView mMessageView;
    private int mProgressStyle = STYLE_SPINNER;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    private int mMax;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    private boolean mIndeterminate;
    private boolean mHasStarted;
    private Handler mViewUpdateHandler;

    public ProgressDialog(Context context) {
        super(context);
        mContext = context;
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        initFormats();
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
        initFormats();
    }

    private void initFormats() {
        mProgressNumberFormat = "%1d/%1d";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message) {
        return show(context, title, message, false);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static ProgressDialog show(Context context, CharSequence title, CharSequence message, boolean indeterminate, boolean cancelable, OnCancelListener cancelListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @Override
    public void show() {
        super.show();

        DialogTitle dialogTitle = findViewById(R.id.alertTitle);
        if (dialogTitle != null && !dialogTitle.getText().toString().isEmpty()) {
            if (mContentView != null) {
                int topPadding = mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_title_padding_bottom : R.dimen.sesl_dialog_title_padding_bottom);
                mContentView.setPaddingRelative(mContentView.getPaddingStart(), topPadding, mContentView.getPaddingEnd(), mContentView.getPaddingBottom());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        TypedArray a = mContext.obtainStyledAttributes(null, R.styleable.SamsungAlertDialog, R.attr.alertDialogStyle, 0);
        if (mProgressStyle == STYLE_HORIZONTAL) {
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    int progress = mProgress.getProgress();
                    int max = mProgress.getMax();
                    if (mProgressNumberFormat != null) {
                        String format = mProgressNumberFormat;
                        if (mProgressNumber.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                            mProgressNumber.setText(String.format(format, max, progress));
                        } else {
                            mProgressNumber.setText(String.format(format, progress, max));
                        }
                    } else {
                        mProgressNumber.setText("");
                    }
                    if (mProgressPercentFormat != null) {
                        double percent = (double) progress / (double) max;
                        SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
                        tmp.setSpan(new StyleSpan(Typeface.NORMAL), 0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mProgressPercent.setText(tmp);
                    } else {
                        mProgressPercent.setText("");
                    }
                }
            };
            mContentView = inflater.inflate(a.getResourceId(R.styleable.SamsungAlertDialog_horizontalProgressLayout, R.layout.oui_progress_dialog_horizontal), null);
            mProgress = (ProgressBar) mContentView.findViewById(R.id.progress);
            mProgressNumber = (TextView) mContentView.findViewById(R.id.progress_number);
            mProgressPercent = (TextView) mContentView.findViewById(R.id.progress_percent);
            mMessageView = (TextView) mContentView.findViewById(R.id.message);
            setView(mContentView);
        } else if (mProgressStyle == STYLE_CIRCLE) {
            setTitle(null);
            getWindow().setBackgroundDrawableResource(mIsOneUI4 ? android.R.color.transparent : R.drawable.oui_progress_circle_dialog_bg);
            View view = inflater.inflate(R.layout.oui_progress_dialog_circle, null);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            mMessageView = (TextView) view.findViewById(R.id.message);
            setView(view);
        } else {
            View view = inflater.inflate(a.getResourceId(R.styleable.SamsungAlertDialog_progressLayout, R.layout.oui_progress_dialog_spinner), null);
            mProgress = (ProgressBar) view.findViewById(R.id.progress);
            mMessageView = (TextView) view.findViewById(R.id.message);
            if (mIsOneUI4) {
                mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimensionPixelSize(R.dimen.sesl4_dialog_body_text_size));
            }
            setView(view);
        }
        a.recycle();
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mSecondaryProgressVal > 0) {
            setSecondaryProgress(mSecondaryProgressVal);
        }
        if (mIncrementBy > 0) {
            incrementProgressBy(mIncrementBy);
        }
        if (mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(mIncrementSecondaryBy);
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        setIndeterminate(mIndeterminate);
        onProgressChanged();
        super.onCreate(savedInstanceState);

        if (mProgressStyle == STYLE_CIRCLE) {
            int size = (int) ((70 * mContext.getResources().getDisplayMetrics().density));
            getWindow().setGravity(Gravity.CENTER);
            getWindow().setLayout(size, size);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    public int getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }

    public int getSecondaryProgress() {
        if (mProgress != null) {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else {
            mSecondaryProgressVal = secondaryProgress;
        }
    }

    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    public void incrementProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementBy += diff;
        }
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementSecondaryBy += diff;
        }
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }

    public boolean isIndeterminate() {
        if (mProgress != null) {
            return mProgress.isIndeterminate();
        }
        return mIndeterminate;
    }

    public void setIndeterminate(boolean indeterminate) {
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (mContentView != null) {
            int topPaddingWithoutTitle = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_dialog_padding_vertical);
            int topPaddingWithTitle = mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_title_padding_bottom : R.dimen.sesl_dialog_title_padding_bottom);
            int paddingTop = title.toString().isEmpty() ? topPaddingWithoutTitle : topPaddingWithTitle;
            mContentView.setPaddingRelative(mContentView.getPaddingStart(), paddingTop, mContentView.getPaddingEnd(), mContentView.getPaddingBottom());
        }
    }

    @Override
    public void setMessage(CharSequence message) {
        if (mProgress != null) {
            if (mProgressStyle == STYLE_HORIZONTAL) {
                if (mMessageView != null) {
                    mMessageView.setText(message);
                    mMessageView.setVisibility(message != "" ? View.VISIBLE : View.GONE);
                } else {
                    super.setMessage(message);
                }
            } else if (mProgressStyle == STYLE_CIRCLE) {
                mMessageView.setText(message);
                mMessageView.setVisibility(message != "" ? View.VISIBLE : View.GONE);
            } else {
                mMessageView.setText(message);
            }
        } else {
            mMessage = message;
        }
    }

    public void setProgressStyle(int style) {
        mProgressStyle = style;
    }

    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        onProgressChanged();
    }

    public void setProgressPercentFormat(NumberFormat format) {
        mProgressPercentFormat = format;
        onProgressChanged();
    }

    private void onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
                mViewUpdateHandler.sendEmptyMessage(0);
            }
        }
    }

    public int getCurrentProgressStyle() {
        return mProgressStyle;
    }
}
