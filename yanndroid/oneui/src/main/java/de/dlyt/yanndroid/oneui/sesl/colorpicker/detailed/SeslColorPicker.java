package de.dlyt.yanndroid.oneui.sesl.colorpicker.detailed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.widget.SeekBar;

public class SeslColorPicker extends LinearLayout implements View.OnClickListener {
    private static final int CURRENT_COLOR_VIEW = 0;
    private static final int NEW_COLOR_VIEW = 1;
    static int RECENT_COLOR_SLOT_COUNT = 6;
    private static final int RIPPLE_EFFECT_OPACITY = 61;

    private boolean mIsOneUI4;
    private String beforeValue;
    private EditText mColorPickerBlueEditText;
    private EditText mColorPickerGreenEditText;
    private EditText mColorPickerHexEditText;
    private EditText mColorPickerOpacityEditText;
    private EditText mColorPickerRedEditText;
    private EditText mColorPickerSaturationEditText;
    private TextView mColorPickerTabSpectrumText;
    private TextView mColorPickerTabSwatchesText;
    private SeslColorSpectrumView mColorSpectrumView;
    private SeslColorSwatchView mColorSwatchView;
    private final Context mContext;
    private GradientDrawable mCurrentColorBackground;
    private ImageView mCurrentColorView;
    private boolean mFlagVar;
    private SeslGradientColorSeekBar mGradientColorSeekBar;
    private LinearLayout mGradientSeekBarContainer;
    private OnColorChangedListener mOnColorChangedListener;
    private LinearLayout mOpacityLayout;
    private SeslOpacitySeekBar mOpacitySeekBar;
    private FrameLayout mOpacitySeekBarContainer;
    private PickedColor mPickedColor;
    private ImageView mPickedColorView;
    private final SeslRecentColorInfo mRecentColorInfo;
    private LinearLayout mRecentColorListLayout;
    private final ArrayList<Integer> mRecentColorValues;
    private GradientDrawable mSelectedColorBackground;
    private boolean mShowOpacitySeekbar;
    private FrameLayout mSpectrumViewContainer;
    private FrameLayout mSwatchViewContainer;
    private LinearLayout mTabLayoutContainer;
    private final int[] mSmallestWidthDp = {320, 360, 411};
    private boolean mIsInputFromUser = false;
    private boolean mIsOpacityBarEnabled = false;
    boolean mIsSpectrumSelected = false;
    ArrayList<EditText> editTexts = new ArrayList<>();
    private String[] mColorDescription = null;
    private boolean mfromEditText = false;
    private boolean mfromSaturationSeekbar = false;
    private boolean mfromSpectrumTouch = false;
    private boolean mfromRGB = false;
    private boolean mTextFromRGB = false;
    private final Resources mResources = getResources();

    private final View.OnClickListener mImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mRecentColorValues.size() && i < SeslColorPicker.RECENT_COLOR_SLOT_COUNT; i++) {
                if (mRecentColorListLayout.getChildAt(i).equals(v)) {
                    mIsInputFromUser = true;

                    int intValue = mRecentColorValues.get(i);
                    mPickedColor.setColor(intValue);
                    mapColorOnColorWheel(intValue);
                    updateHexAndRGBValues(intValue);

                    if (mGradientColorSeekBar != null) {
                        int progress = mGradientColorSeekBar.getProgress();
                        mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", Integer.valueOf(progress)));
                        mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                    }

                    if (mOnColorChangedListener != null) {
                        mOnColorChangedListener.onColorChanged(intValue);
                    }
                }
            }
        }
    };

    public interface OnColorChangedListener {
        void onColorChanged(int newColor);
    }

    public SeslColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        LayoutInflater.from(context).inflate(R.layout.sesl_color_picker_oneui_3_layout, this);

        mRecentColorInfo = new SeslRecentColorInfo();
        mRecentColorValues = mRecentColorInfo.getRecentColorInfo();
        mTabLayoutContainer = (LinearLayout) findViewById(R.id.sesl_color_picker_tab_layout);
        mPickedColor = new PickedColor();

        initDialogPadding();
        initCurrentColorView();
        initColorSwatchView();
        initGradientColorSeekBar();
        initColorSpectrumView();
        initOpacitySeekBar(mShowOpacitySeekbar);
        initRecentColorLayout();
        updateCurrentColor();
        setInitialColors();
        initCurrentColorValuesLayout();
    }

    public void setOnlySpectrumMode() {
        mTabLayoutContainer.setVisibility(View.GONE);

        initColorSpectrumView();
        if (!mIsSpectrumSelected) {
            mIsSpectrumSelected = true;
        }
        mSwatchViewContainer.setVisibility(View.GONE);
        mSpectrumViewContainer.setVisibility(View.VISIBLE);
        mColorPickerHexEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerRedEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerBlueEditText.setInputType(InputType.TYPE_NULL);
        mColorPickerGreenEditText.setInputType(InputType.TYPE_NULL);
    }

    private void initDialogPadding() {
        if (mResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DisplayMetrics metrics = mResources.getDisplayMetrics();
            float density = metrics.density;

            if (density % 1.0f != 0.0f) {
                float width = (float) metrics.widthPixels;
                if (isContains((int) (width / density))) {
                    int seekBarWidth = mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_seekbar_width);
                    if (width < ((float) ((mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_dialog_padding_left) * 2) + seekBarWidth))) {
                        int padding = (int) ((width - ((float) seekBarWidth)) / 2.0f);
                        ((LinearLayout) findViewById(R.id.sesl_color_picker_main_content_container)).setPadding(padding, mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_dialog_padding_top), padding, mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_oneui_3_dialog_padding_bottom));
                    }
                }
            }
        }
    }

    private boolean isContains(int dp) {
        for (int i : mSmallestWidthDp) {
            if (dp == i) {
                return true;
            }
        }
        return false;
    }

    private void initCurrentColorView() {
        mCurrentColorView = (ImageView) findViewById(R.id.sesl_color_picker_current_color_view);
        mPickedColorView = (ImageView) findViewById(R.id.sesl_color_picker_picked_color_view);

        mColorPickerTabSwatchesText = (TextView) findViewById(R.id.sesl_color_picker_swatches_text_view);
        mColorPickerTabSpectrumText = (TextView) findViewById(R.id.sesl_color_picker_spectrum_text_view);
        mColorPickerOpacityEditText = (EditText) findViewById(R.id.sesl_color_seek_bar_opacity_value_edit_view);
        mColorPickerSaturationEditText = (EditText) findViewById(R.id.sesl_color_seek_bar_saturation_value_edit_view);
        mColorPickerOpacityEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerSaturationEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerTabSwatchesText.setBackgroundResource(mIsOneUI4 ? R.drawable.sesl4_color_picker_tab_selector_bg : R.drawable.sesl_color_picker_tab_selector_bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mColorPickerTabSwatchesText.setTextAppearance(R.style.TabTextSelected);
        }
        mColorPickerTabSwatchesText.setTextColor(getResources().getColor(R.color.sesl_dialog_body_text_color));
        mColorPickerTabSpectrumText.setTextColor(getResources().getColor(R.color.sesl_secondary_text_color));
        mColorPickerOpacityEditText.setTag(1);
        mFlagVar = true;

        mSelectedColorBackground = (GradientDrawable) this.mPickedColorView.getBackground();
        if (mPickedColor.getColor() != null) {
            mSelectedColorBackground.setColor(mPickedColor.getColor());
        }
        mCurrentColorBackground = (GradientDrawable) mCurrentColorView.getBackground();

        mColorPickerTabSwatchesText.setOnClickListener(this);
        mColorPickerTabSpectrumText.setOnClickListener(this);

        mColorPickerOpacityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int i = Integer.parseInt(s.toString());
                if (mOpacitySeekBar != null && s.toString().trim().length() > 0 && i <= 100) {
                    mColorPickerOpacityEditText.setTag(0);
                    mOpacitySeekBar.setProgress((i * 255) / 100);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (Integer.parseInt(s.toString()) > 100) {
                        mColorPickerOpacityEditText.setText("" + String.format(Locale.getDefault(), "%d", 100));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                mColorPickerOpacityEditText.setSelection(mColorPickerOpacityEditText.getText().length());
            }
        });
        mColorPickerOpacityEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!mColorPickerOpacityEditText.hasFocus() && mColorPickerOpacityEditText.getText().toString().isEmpty()) {
                    mColorPickerOpacityEditText.setText("" + String.format(Locale.getDefault(), "%d", 0));
                }
            }
        });
        mColorPickerOpacityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    mColorPickerHexEditText.requestFocus();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sesl_color_picker_swatches_text_view) {
            mColorPickerTabSwatchesText.setSelected(true);
            mColorPickerTabSwatchesText.setBackgroundResource(mIsOneUI4 ? R.drawable.sesl4_color_picker_tab_selector_bg : R.drawable.sesl_color_picker_tab_selector_bg);
            mColorPickerTabSpectrumText.setSelected(false);
            mColorPickerTabSpectrumText.setBackgroundResource(0);
            mColorPickerTabSwatchesText.setTextColor(getResources().getColor(R.color.sesl_dialog_body_text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mColorPickerTabSwatchesText.setTextAppearance(R.style.TabTextSelected);
            }
            mColorPickerTabSpectrumText.setTextColor(getResources().getColor(R.color.sesl_secondary_text_color));
            mColorPickerTabSpectrumText.setTypeface(Typeface.DEFAULT);

            mSwatchViewContainer.setVisibility(View.VISIBLE);
            mSpectrumViewContainer.setVisibility(View.GONE);

            if (mResources.getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || isTablet(mContext)) {
                mGradientSeekBarContainer.setVisibility(View.GONE);
            } else {
                mGradientSeekBarContainer.setVisibility(View.INVISIBLE);
            }
        } else if (v.getId() == R.id.sesl_color_picker_spectrum_text_view) {
            mColorPickerTabSwatchesText.setSelected(false);
            mColorPickerTabSpectrumText.setSelected(true);
            mColorPickerTabSpectrumText.setBackgroundResource(mIsOneUI4 ? R.drawable.sesl4_color_picker_tab_selector_bg : R.drawable.sesl_color_picker_tab_selector_bg);
            mColorPickerTabSwatchesText.setBackgroundResource(0);
            initColorSpectrumView();
            mColorPickerTabSpectrumText.setTextColor(getResources().getColor(R.color.sesl_dialog_body_text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mColorPickerTabSpectrumText.setTextAppearance(R.style.TabTextSelected);
            }
            mColorPickerTabSwatchesText.setTextColor(getResources().getColor(R.color.sesl_secondary_text_color));
            mColorPickerTabSwatchesText.setTypeface(Typeface.DEFAULT);

            mSwatchViewContainer.setVisibility(View.GONE);
            mSpectrumViewContainer.setVisibility(View.VISIBLE);

            mGradientSeekBarContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initColorSwatchView() {
        mColorSwatchView = (SeslColorSwatchView) findViewById(R.id.sesl_color_picker_color_swatch_view);
        mSwatchViewContainer = (FrameLayout) findViewById(R.id.sesl_color_picker_color_swatch_view_container);
        mColorSwatchView.setOnColorSwatchChangedListener(new SeslColorSwatchView.OnColorSwatchChangedListener() {
            @Override
            public void onColorSwatchChanged(int newColor) {
                mIsInputFromUser = true;
                try {
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPickedColor.setColorWithAlpha(newColor, mOpacitySeekBar.getProgress());
                updateCurrentColor();
                updateHexAndRGBValues(newColor);
            }
        });
    }

    private void initColorSpectrumView() {
        mColorSpectrumView = (SeslColorSpectrumView) findViewById(R.id.sesl_color_picker_color_spectrum_view);
        mSpectrumViewContainer = (FrameLayout) findViewById(R.id.sesl_color_picker_color_spectrum_view_container);
        mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", mGradientColorSeekBar.getProgress()));
        mColorSpectrumView.setOnSpectrumColorChangedListener(new SeslColorSpectrumView.SpectrumColorChangedListener() {
            @Override
            public void onSpectrumColorChanged(float newHue, float newSaturation) {
                mIsInputFromUser = true;
                try {
                    ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mPickedColor.setHS(newHue, newSaturation, mOpacitySeekBar.getProgress());
                updateCurrentColor();
                updateHexAndRGBValues(mPickedColor.getColor());
            }
        });
        mColorPickerSaturationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mTextFromRGB) {
                    try {
                        if (mGradientColorSeekBar != null && s.toString().trim().length() > 0) {
                            int i = Integer.parseInt(s.toString());
                            mfromEditText = true;
                            mFlagVar = false;
                            if (i <= 100) {
                                mColorPickerSaturationEditText.setTag(0);
                                mGradientColorSeekBar.setProgress(i);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mTextFromRGB) {
                    try {
                        if (Integer.parseInt(s.toString()) > 100) {
                            mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", 100));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    mColorPickerSaturationEditText.setSelection(mColorPickerSaturationEditText.getText().length());
                }
            }
        });
        mColorPickerSaturationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!mColorPickerSaturationEditText.hasFocus() && mColorPickerSaturationEditText.getText().toString().isEmpty()) {
                    mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", 0));
                }
            }
        });
    }

    private void initGradientColorSeekBar() {
        mGradientSeekBarContainer = (LinearLayout) findViewById(R.id.sesl_color_picker_saturation_layout);
        mGradientColorSeekBar = (SeslGradientColorSeekBar) findViewById(R.id.sesl_color_picker_saturation_seekbar);
        mGradientColorSeekBar.init(mPickedColor.getColor());
        mGradientColorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsInputFromUser = true;
                    mfromSaturationSeekbar = true;
                }
                float f = ((float) seekBar.getProgress()) / ((float) seekBar.getMax());
                if (progress >= 0 && mFlagVar) {
                    mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", Integer.valueOf(progress)));
                    mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                }
                if (mfromRGB) {
                    mTextFromRGB = true;
                    mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", Integer.valueOf(progress)));
                    mColorPickerSaturationEditText.setSelection(String.valueOf(progress).length());
                    mTextFromRGB = false;
                }
                mPickedColor.setV(f);
                int i = mPickedColor.getColor();
                if (mfromEditText) {
                    updateHexAndRGBValues(i);
                    mfromEditText = false;
                }
                if (mSelectedColorBackground != null) {
                    mSelectedColorBackground.setColor(i);
                }
                if (mOpacitySeekBar != null) {
                    mOpacitySeekBar.changeColorBase(i, mPickedColor.getAlpha());
                }
                if (mOnColorChangedListener != null) {
                    mOnColorChangedListener.onColorChanged(i);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mfromSaturationSeekbar = false;
            }
        });
        mGradientColorSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mFlagVar = true;

                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mGradientColorSeekBar.setSelected(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mGradientColorSeekBar.setSelected(false);
                        return false;
                    default:
                        return false;
                }
            }
        });
        ((FrameLayout) findViewById(R.id.sesl_color_picker_saturation_seekbar_container)).setContentDescription(mResources.getString(R.string.sesl_color_picker_hue_and_saturation) + ", " + mResources.getString(R.string.sesl_color_picker_slider) + ", " + mResources.getString(R.string.sesl_color_picker_double_tap_to_select));
    }

    public void initOpacitySeekBar(boolean enable) {
        mOpacitySeekBar = (SeslOpacitySeekBar) findViewById(R.id.sesl_color_picker_opacity_seekbar);
        mOpacitySeekBarContainer = (FrameLayout) findViewById(R.id.sesl_color_picker_opacity_seekbar_container);
        mOpacityLayout = (LinearLayout) findViewById(R.id.sesl_color_picker_opacity_layout);
        mOpacityLayout.setVisibility(enable ? View.VISIBLE : View.GONE);
        if (!mIsOpacityBarEnabled) {
            mOpacitySeekBar.setVisibility(View.GONE);
            mOpacitySeekBarContainer.setVisibility(View.GONE);
        }
        mOpacitySeekBar.init(mPickedColor.getColor());
        mOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mIsInputFromUser = true;
                }
                mPickedColor.setAlpha(progress);
                if (progress >= 0 && Integer.parseInt(mColorPickerOpacityEditText.getTag().toString()) == 1) {
                    mColorPickerOpacityEditText.setText("" + String.format(Locale.getDefault(), "%d", (int) Math.ceil((double) (((float) (progress * 100)) / 255.0f))));
                }
                if (mPickedColor.getColor() != null) {
                    if (mSelectedColorBackground != null) {
                        mSelectedColorBackground.setColor(mPickedColor.getColor());
                    }
                    if (mOnColorChangedListener != null) {
                        mOnColorChangedListener.onColorChanged(mPickedColor.getColor());
                    }
                }
            }
        });
        mOpacitySeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mColorPickerOpacityEditText.setTag(1);
                return event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });
        mOpacitySeekBarContainer.setContentDescription(mResources.getString(R.string.sesl_color_picker_opacity) + ", " + mResources.getString(R.string.sesl_color_picker_slider) + ", " + mResources.getString(R.string.sesl_color_picker_double_tap_to_select));
    }

    private void initCurrentColorValuesLayout() {
        mColorPickerHexEditText = (EditText) findViewById(R.id.sesl_color_hex_edit_text);
        mColorPickerRedEditText = (EditText) findViewById(R.id.sesl_color_red_edit_text);
        mColorPickerBlueEditText = (EditText) findViewById(R.id.sesl_color_blue_edit_text);
        mColorPickerGreenEditText = (EditText) findViewById(R.id.sesl_color_green_edit_text);
        mColorPickerRedEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerBlueEditText.setPrivateImeOptions("disableDirectWriting=true;");
        mColorPickerGreenEditText.setPrivateImeOptions("disableDirectWriting=true;");
        editTexts.add(mColorPickerRedEditText);
        editTexts.add(mColorPickerGreenEditText);
        editTexts.add(mColorPickerBlueEditText);
        setTextWatcher();
        mColorPickerBlueEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mColorPickerBlueEditText.clearFocus();
                }
                return false;
            }
        });
    }

    private void setTextWatcher() {
        mColorPickerHexEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.toString().trim().length();
                if (length == 6) {
                    int color = Color.parseColor("#" + s.toString());
                    if (!mColorPickerRedEditText.getText().toString().trim().equalsIgnoreCase("" + Color.red(color))) {
                        mColorPickerRedEditText.setText("" + Color.red(color));
                    }
                    if (!mColorPickerGreenEditText.getText().toString().trim().equalsIgnoreCase("" + Color.green(color))) {
                        mColorPickerGreenEditText.setText("" + Color.green(color));
                    }
                    if (!mColorPickerBlueEditText.getText().toString().trim().equalsIgnoreCase("" + Color.blue(color))) {
                        mColorPickerBlueEditText.setText("" + Color.blue(color));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                mIsInputFromUser = true;
            }
        });

        beforeValue = "";

        for (EditText editText : editTexts) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    beforeValue = s.toString().trim();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!s.toString().equalsIgnoreCase(beforeValue) && s.toString().trim().length() > 0) {
                        updateHexData();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (Integer.parseInt(s.toString()) > 255) {
                            if (editText == editTexts.get(0)) {
                                mColorPickerRedEditText.setText("255");
                            }
                            if (editText == SeslColorPicker.this.editTexts.get(1)) {
                                mColorPickerGreenEditText.setText("255");
                            }
                            if (editText == editTexts.get(2)) {
                                mColorPickerBlueEditText.setText("255");
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        if (editText == editTexts.get(0)) {
                            mColorPickerRedEditText.setText("0");
                        }
                        if (editText == editTexts.get(1)) {
                            mColorPickerGreenEditText.setText("0");
                        }
                        if (editText == editTexts.get(2)) {
                            mColorPickerBlueEditText.setText("0");
                        }
                    }
                    mIsInputFromUser = true;
                    mfromRGB = true;
                    mColorPickerRedEditText.setSelection(mColorPickerRedEditText.getText().length());
                    mColorPickerGreenEditText.setSelection(mColorPickerGreenEditText.getText().length());
                    mColorPickerBlueEditText.setSelection(mColorPickerBlueEditText.getText().length());
                }
            });
        }
    }

    private void updateHexData() {
        int red = Integer.parseInt(mColorPickerRedEditText.getText().toString().trim().length() > 0 ?
                mColorPickerRedEditText.getText().toString().trim() : "0");
        int green = Integer.parseInt(mColorPickerGreenEditText.getText().toString().trim().length() > 0 ?
                mColorPickerGreenEditText.getText().toString().trim() : "0");
        int blue = Integer.parseInt(mColorPickerBlueEditText.getText().toString().trim().length() > 0 ?
                mColorPickerBlueEditText.getText().toString().trim() : "0");

        int color = ((red & 255) << 16) | ((mOpacitySeekBar.getProgress() & 255) << 24) | ((green & 255) << 8) | (blue & 255);
        String colorStr = String.format("%08x", color);

        mColorPickerHexEditText.setText("" + colorStr.substring(2, colorStr.length()));
        mColorPickerHexEditText.setSelection(mColorPickerHexEditText.getText().length());
        if (!mfromSaturationSeekbar && !mfromSpectrumTouch) {
            mapColorOnColorWheel(color);
        }

        if (mOnColorChangedListener != null) {
            mOnColorChangedListener.onColorChanged(color);
        }
    }

    private void updateHexAndRGBValues(int i) {
        if (i != 0) {
            String format = String.format("%08x", i);
            String substring = format.substring(2, format.length());
            mColorPickerHexEditText.setText("" + substring);
            mColorPickerHexEditText.setSelection(mColorPickerHexEditText.getText().length());

            int color = Color.parseColor("#" + substring);
            mColorPickerRedEditText.setText("" + Color.red(color));
            mColorPickerBlueEditText.setText("" + Color.blue(color));
            mColorPickerGreenEditText.setText("" + Color.green(color));
        }
    }

    private void initRecentColorLayout() {
        mRecentColorListLayout = (LinearLayout) findViewById(R.id.sesl_color_picker_used_color_item_list_layout);
        mColorDescription = new String[]{mResources.getString(R.string.sesl_color_picker_color_one), mResources.getString(R.string.sesl_color_picker_color_two), mResources.getString(R.string.sesl_color_picker_color_three), mResources.getString(R.string.sesl_color_picker_color_four), mResources.getString(R.string.sesl_color_picker_color_five), mResources.getString(R.string.sesl_color_picker_color_six), mResources.getString(R.string.sesl_color_picker_color_seven)};
        if (mResources.getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE || isTablet(mContext)) {
            RECENT_COLOR_SLOT_COUNT = 6;
        } else {
            RECENT_COLOR_SLOT_COUNT = 7;
        }
        for (int i = 0; i < RECENT_COLOR_SLOT_COUNT; i++) {
            View child = mRecentColorListLayout.getChildAt(i);
            setImageColor(child, ContextCompat.getColor(mContext, R.color.sesl_color_picker_used_color_item_empty_slot_color));
            child.setFocusable(false);
            child.setClickable(false);
        }
    }

    public void updateRecentColorLayout() {
        int recentColorValues = mRecentColorValues != null ? mRecentColorValues.size() : 0;
        String description = ", " + mResources.getString(R.string.sesl_color_picker_option);
        if (mResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RECENT_COLOR_SLOT_COUNT = 7;
        } else {
            RECENT_COLOR_SLOT_COUNT = 6;
        }
        for (int i = 0; i < RECENT_COLOR_SLOT_COUNT; i++) {
            View child = mRecentColorListLayout.getChildAt(i);
            if (i < recentColorValues) {
                setImageColor(child, mRecentColorValues.get(i));

                StringBuilder sb = new StringBuilder();
                sb.append((CharSequence) mColorSwatchView.getColorSwatchDescriptionAt(mRecentColorValues.get(i)));
                sb.insert(0, mColorDescription[i] + description + ", ");
                child.setContentDescription(sb);

                child.setFocusable(true);
                child.setClickable(true);
            }
        }
        if (mRecentColorInfo.getCurrentColor() != null) {
            mCurrentColorBackground.setColor(mRecentColorInfo.getCurrentColor().intValue());
            setCurrentColorViewDescription(mRecentColorInfo.getCurrentColor().intValue(), 0);
            mSelectedColorBackground.setColor(mRecentColorInfo.getCurrentColor().intValue());
            mapColorOnColorWheel(mRecentColorInfo.getCurrentColor().intValue());
            updateHexAndRGBValues(mCurrentColorBackground.getColor().getDefaultColor());
        } else if (recentColorValues != 0) {
            mCurrentColorBackground.setColor(mRecentColorValues.get(0));
            setCurrentColorViewDescription(mRecentColorValues.get(0), 0);
            mSelectedColorBackground.setColor(mRecentColorValues.get(0));
            mapColorOnColorWheel(mRecentColorValues.get(0));
            updateHexAndRGBValues(mCurrentColorBackground.getColor().getDefaultColor());
        }
        if (mRecentColorInfo.getNewColor() != null) {
            mSelectedColorBackground.setColor(mRecentColorInfo.getNewColor().intValue());
            mapColorOnColorWheel(mRecentColorInfo.getNewColor().intValue());
            updateHexAndRGBValues(mSelectedColorBackground.getColor().getDefaultColor());
        }
    }

    public void setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        mOnColorChangedListener = onColorChangedListener;
    }

    private void setInitialColors() {
        if (mPickedColor.getColor() != null) {
            mapColorOnColorWheel(mPickedColor.getColor());
        }
    }

    private void updateCurrentColor() {
        Integer color = mPickedColor.getColor();
        if (color != null) {
            if (mOpacitySeekBar != null) {
                mOpacitySeekBar.changeColorBase(color, mPickedColor.getAlpha());
                mColorPickerOpacityEditText.setText("" + String.format(Locale.getDefault(), "%d", Integer.valueOf(mOpacitySeekBar.getProgress())));
                mColorPickerOpacityEditText.setSelection(String.valueOf(mOpacitySeekBar.getProgress()).length());
            }

            if (mSelectedColorBackground != null) {
                mSelectedColorBackground.setColor(color);
                setCurrentColorViewDescription(color, 1);
            }

            if (mOnColorChangedListener != null) {
                mOnColorChangedListener.onColorChanged(color);
            }

            if (mColorSpectrumView != null) {
                mColorSpectrumView.updateCursorColor(color);
                mColorSpectrumView.setColor(color);
            }

            if (mGradientColorSeekBar != null) {
                mGradientColorSeekBar.changeColorBase(color);
                mfromSpectrumTouch = true;
                mColorPickerSaturationEditText.setText("" + String.format(Locale.getDefault(), "%d", Integer.valueOf(mGradientColorSeekBar.getProgress())));
                mColorPickerSaturationEditText.setSelection(String.valueOf(mGradientColorSeekBar.getProgress()).length());
                mfromSpectrumTouch = false;
            }
        }
    }

    private void setImageColor(View view, Integer num) {
        GradientDrawable gradientDrawable = (GradientDrawable) mContext.getDrawable(R.drawable.sesl_color_picker_used_color_item_slot);
        if (num != null) {
            gradientDrawable.setColor(num);
        }
        view.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.argb(61, 0, 0, 0)}), gradientDrawable, null));
        view.setOnClickListener(mImageButtonClickListener);
    }

    private void mapColorOnColorWheel(int i) {
        mPickedColor.setColor(i);

        if (mColorSwatchView != null) {
            mColorSwatchView.updateCursorPosition(i);
        }

        if (mColorSpectrumView != null) {
            mColorSpectrumView.setColor(i);
        }

        if (mGradientColorSeekBar != null) {
            mGradientColorSeekBar.restoreColor(i);
        }

        if (mOpacitySeekBar != null) {
            mOpacitySeekBar.restoreColor(i);
        }

        if (mSelectedColorBackground != null) {
            mSelectedColorBackground.setColor(i);
            setCurrentColorViewDescription(i, 1);
        }

        if (mColorSpectrumView != null) {
            float v = mPickedColor.getV();
            int alpha = mPickedColor.getAlpha();
            mPickedColor.setV(1.0f);
            mPickedColor.setAlpha(255);
            mColorSpectrumView.updateCursorColor(mPickedColor.getColor());
            mPickedColor.setV(v);
            mPickedColor.setAlpha(alpha);
        }

        if (mOpacitySeekBar != null) {
            int ceil = (int) Math.ceil((double) (((float) (mOpacitySeekBar.getProgress() * 100)) / 255.0f));
            mColorPickerOpacityEditText.setText("" + String.format(Locale.getDefault(), "%d", ceil));
            mColorPickerOpacityEditText.setSelection(String.valueOf(ceil).length());
        }
    }

    private void setCurrentColorViewDescription(int i, int i2) {
        StringBuilder sb = new StringBuilder();
        StringBuilder colorSwatchDescriptionAt = mColorSwatchView.getColorSwatchDescriptionAt(i);
        if (colorSwatchDescriptionAt != null) {
            sb.append(", ").append((CharSequence) colorSwatchDescriptionAt);
        }
        if (i2 == 0) {
            sb.insert(0, mResources.getString(R.string.sesl_color_picker_current));
        } else if (i2 == 1) {
            sb.insert(0, mResources.getString(R.string.sesl_color_picker_new));
        }
    }

    public void saveSelectedColor() {
        if (mPickedColor.getColor() != null) {
            mRecentColorInfo.saveSelectedColor(mPickedColor.getColor());
        }
    }

    public SeslRecentColorInfo getRecentColorInfo() {
        return mRecentColorInfo;
    }

    public boolean isUserInputValid() {
        return mIsInputFromUser;
    }

    public void setOpacityBarEnabled(boolean z) {
        mIsOpacityBarEnabled = z;
        if (z) {
            mOpacitySeekBar.setVisibility(View.VISIBLE);
            mOpacitySeekBarContainer.setVisibility(View.VISIBLE);
        }
    }


    private static class PickedColor {
        private Integer mColor = null;
        private int mAlpha = 255;
        private float[] mHsv = new float[3];

        public void setColor(int i) {
            mColor = i;
            mAlpha = Color.alpha(i);
            Color.colorToHSV(mColor, mHsv);
        }

        public void setColorWithAlpha(int i, int i2) {
            mColor = i;
            mAlpha = (int) Math.ceil((double) (((float) (i2 * 100)) / 255.0f));
            Color.colorToHSV(mColor, mHsv);
        }

        public Integer getColor() {
            return mColor;
        }

        public void setHS(float f, float f2, int i) {
            float[] fArr = mHsv;
            fArr[0] = f;
            fArr[1] = f2;
            fArr[2] = 1.0f;
            mColor = Color.HSVToColor(mAlpha, fArr);
            mAlpha = (int) Math.ceil((double) (((float) (i * 100)) / 255.0f));
        }

        public void setV(float f) {
            float[] fArr = mHsv;
            fArr[2] = f;
            mColor = Color.HSVToColor(mAlpha, fArr);
        }

        public void setAlpha(int i) {
            mAlpha = i;
            mColor = Color.HSVToColor(i, mHsv);
        }

        public float getV() {
            return mHsv[2];
        }

        public int getAlpha() {
            return mAlpha;
        }
    }

    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 3;
    }
}
