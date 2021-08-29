package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.SeekBar;

public class SeekBarPreference extends Preference {
    private static final String TAG = "SeekBarPreference";
    @SuppressWarnings("WeakerAccess")
    int mSeekBarValue;
    @SuppressWarnings("WeakerAccess")
    String mUnits;
    @SuppressWarnings("WeakerAccess")
    int mMin;
    @SuppressWarnings("WeakerAccess")
    boolean mTrackingTouch;
    @SuppressWarnings("WeakerAccess")
    SeekBar mSeekBar;
    @SuppressWarnings("WeakerAccess")
    boolean mAdjustable;
    @SuppressWarnings("WeakerAccess")
    boolean mUpdatesContinuously;
    private int mSeekBarMode;
    private boolean mSeekBarSeamless;
    private int mMax;
    private int mOverlapPoint;
    private int mSeekBarIncrement;
    private TextView mSeekBarValueTextView;
    private boolean mShowSeekBarValue;
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser && (mUpdatesContinuously || !mTrackingTouch)) {
                syncValueInternal(seekBar);
            } else {
                updateLabelValue(progress + mMin);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = false;
            if (seekBar.getProgress() + mMin != mSeekBarValue) {
                syncValueInternal(seekBar);
            }
        }
    };
    private View.OnKeyListener mSeekBarKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (!mAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                return false;
            }
            if (mSeekBar == null) {
                Log.e(TAG, "SeekBar view is null and hence cannot be adjusted.");
                return false;
            }
            return mSeekBar.onKeyDown(keyCode, event);
        }
    };


    public SeekBarPreference(Context context) {
        this(context, null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        mMin = a.getInt(R.styleable.SeekBarPreference_min, 0);
        setMax(a.getInt(R.styleable.SeekBarPreference_max, 100));
        mOverlapPoint = a.getInt(R.styleable.SeekBarPreference_overlap, -1);
        setSeekBarIncrement(a.getInt(R.styleable.SeekBarPreference_seekBarIncrement, 0));
        mAdjustable = a.getBoolean(R.styleable.SeekBarPreference_adjustable, true);
        mShowSeekBarValue = a.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, false);
        mSeekBarMode = a.getInt(R.styleable.SeekBarPreference_seekBarMode, 0);
        mSeekBarSeamless = a.getBoolean(R.styleable.SeekBarPreference_seekBarSeamless, false);

        mUnits = a.getString(R.styleable.SeekBarPreference_units);
        if (mUnits == null)
            mUnits = "";

        mUpdatesContinuously = a.getBoolean(R.styleable.SeekBarPreference_updatesContinuously, false);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(mSeekBarKeyListener);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBarValueTextView = (TextView) view.findViewById(R.id.seekbar_value);
        if (mShowSeekBarValue) {
            mSeekBarValueTextView.setVisibility(View.VISIBLE);
        } else {
            mSeekBarValueTextView.setVisibility(View.GONE);
            mSeekBarValueTextView = null;
        }
        if (mSeekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }

        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.setMode(mSeekBarMode);
        mSeekBar.setMax(mMax - mMin);
        mSeekBar.setOverlapPointForDualColor(mOverlapPoint);
        mSeekBar.setSeamless(mSeekBarSeamless);

        if (mSeekBarIncrement != 0) {
            mSeekBar.setKeyProgressIncrement(mSeekBarIncrement);
        } else {
            mSeekBarIncrement = mSeekBar.getKeyProgressIncrement();
        }
        mSeekBar.setProgress(mSeekBarValue - mMin);
        updateLabelValue(mSeekBarValue);
        mSeekBar.setEnabled(isEnabled());
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = 0;
        }
        setValue(getPersistedInt((Integer) defaultValue));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public int getMin() {
        return mMin;
    }

    public void setMin(int min) {
        if (min > mMax) {
            min = mMax;
        }
        if (min != mMin) {
            mMin = min;
            notifyChanged();
        }
    }

    public final int getSeekBarIncrement() {
        return mSeekBarIncrement;
    }

    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != mSeekBarIncrement) {
            mSeekBarIncrement = Math.min(mMax - mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public int getMax() {
        return mMax;
    }

    public final void setMax(int max) {
        if (max < mMin) {
            max = mMin;
        }
        if (max != mMax) {
            mMax = max;
            notifyChanged();
        }
    }

    public boolean isAdjustable() {
        return mAdjustable;
    }

    public void setAdjustable(boolean adjustable) {
        mAdjustable = adjustable;
    }

    public boolean getUpdatesContinuously() {
        return mUpdatesContinuously;
    }

    public void setUpdatesContinuously(boolean updatesContinuously) {
        mUpdatesContinuously = updatesContinuously;
    }

    public boolean getShowSeekBarValue() {
        return mShowSeekBarValue;
    }

    public void setShowSeekBarValue(boolean showSeekBarValue) {
        mShowSeekBarValue = showSeekBarValue;
        notifyChanged();
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < mMin) {
            seekBarValue = mMin;
        }
        if (seekBarValue > mMax) {
            seekBarValue = mMax;
        }
        if (seekBarValue != mSeekBarValue) {
            mSeekBarValue = seekBarValue;
            updateLabelValue(mSeekBarValue);
            persistInt(seekBarValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getValue() {
        return mSeekBarValue;
    }

    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue, true);
    }

    @SuppressWarnings("WeakerAccess")
    void syncValueInternal(SeekBar seekBar) {
        int seekBarValue = mMin + seekBar.getProgress();
        if (seekBarValue != mSeekBarValue) {
            if (callChangeListener(seekBarValue)) {
                setValueInternal(seekBarValue, false);
            } else {
                seekBar.setProgress(mSeekBarValue - mMin);
                updateLabelValue(mSeekBarValue);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    void updateLabelValue(int value) {
        if (mSeekBarValueTextView != null) {
            mSeekBarValueTextView.setText(mUnits.isEmpty() ? String.valueOf(value) : value + " " + mUnits);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.mSeekBarValue = mSeekBarValue;
        myState.mMin = mMin;
        myState.mMax = mMax;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mSeekBarValue = myState.mSeekBarValue;
        mMin = myState.mMin;
        mMax = myState.mMax;
        notifyChanged();
    }


    private static class SavedState extends BaseSavedState {
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
        int mSeekBarValue;
        int mMin;
        int mMax;

        SavedState(Parcel source) {
            super(source);
            mSeekBarValue = source.readInt();
            mMin = source.readInt();
            mMax = source.readInt();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mSeekBarValue);
            dest.writeInt(mMin);
            dest.writeInt(mMax);
        }
    }
}