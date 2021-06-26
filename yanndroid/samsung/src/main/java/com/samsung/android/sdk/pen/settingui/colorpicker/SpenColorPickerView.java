package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilDrawable;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilHover;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilSIP;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilText;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.R;

public class SpenColorPickerView extends LinearLayout implements SpenPickerColorEventListener, View.OnFocusChangeListener {
    private static final int DEFAULT_COLOR = -1644826;
    private static int RECENT_COLOR_BUTTON_MAX = 6;
    private static final String TAG = "SpenColorPickerView";
    public static final int VIEW_MODE_GRADIENT = 1;
    public static final int VIEW_MODE_SWATCH = 2;
    private EditText mBlueInputView;
    private ColorListener mColorListener;
    private Context mContext;
    private String mCurrentColorString;
    private View mCurrentColorView;
    private SpenColorPickerEyedropperListener mEyedropperClickListener;
    private int mFocusViewID = 0;
    private EditText mGreenInputView;
    private EditText mHexInputView;
    private boolean mIsSupportEyedropper;
    private boolean mIsSupportRGBCode;
    private int mMode;
    private ImageButton mModeButton;
    private final OnClickListener mModeButtonClickListener = new OnClickListener() {

        public void onClick(View view) {
            if (SpenColorPickerView.this.mContext == null) {
                return;
            }
            SpenColorPickerView.this.toggleMode();
            if (SpenColorPickerView.this.mModeChangeListener != null) {
                SpenColorPickerView.this.mModeChangeListener.onModeChanged(SpenColorPickerView.this.mMode);
            }
        }
    };
    private OnModeChangeListener mModeChangeListener;
    private String mNewColorString;
    private View mNewColorView;
    private float[] mOldHsv;
    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            View findViewById;
            if (i != 6) {
                return false;
            }
            if (SpenColorPickerView.this.mFocusViewID != 0 && (findViewById = SpenColorPickerView.this.mPickerContainer.getRootView().findViewById(SpenColorPickerView.this.mFocusViewID)) != null && (findViewById instanceof EditText) && findViewById.isFocused()) {
                findViewById.clearFocus();
                SpenSettingUtilSIP.hideSoftInput(SpenColorPickerView.this.getContext(), findViewById, 0);
            }
            return true;
        }
    };
    private int mOutlineColor;
    private int mOutlineSize;
    private SpenPickerColor mPickerColor;
    private LinearLayout mPickerContainer;
    private SpenPickerTabGroup mPickerTabGroup;
    private SpenColorViewInterface mPickerView;
    private SpenColorPickerViewInfo mPickerViewInfo;
    private SpenRGBCodeControl mRGBCodeControl;
    private OnClickListener mRecentColorClickListener = new OnClickListener() {

        public void onClick(View view) {
            if (SpenColorPickerView.this.isReleaseResource()) {
                return;
            }
            float[] fArr = new float[3];
            ((SpenHSVColor) SpenColorPickerView.this.mRecentColors.get(((Integer) view.getTag()).intValue())).getHSV(fArr);
            SpenColorPickerView.this.setCurrentColor(fArr);
            SpenColorPickerView.this.notifyColorSelected(3);
        }
    };
    private List<SpenHSVColor> mRecentColors;
    private ViewGroup mRecentParent;
    private EditText mRedInputView;
    private SpenColorValueSeekBar mValueSeekBar;

    public interface ColorListener {
        public static final int TYPE_COLOR = 1;
        public static final int TYPE_RECENT = 3;
        public static final int TYPE_SEEKBAR = 2;

        void onColorSelected(float f, float f2, float f3, int i);
    }

    public interface OnModeChangeListener {
        void onModeChanged(int i);
    }

    SpenColorPickerView(Context context, int i, float[] fArr, SpenColorPickerViewInfo spenColorPickerViewInfo, boolean z, boolean z2) {
        super(context);
        this.mIsSupportRGBCode = z;
        this.mIsSupportEyedropper = z2;
        this.mContext = context;
        this.mPickerColor = new SpenPickerColor();
        this.mPickerColor.setColor(TAG, 255, fArr[0], fArr[1], fArr[2]);
        this.mOldHsv = new float[]{fArr[0], fArr[1], fArr[2]};
        this.mMode = i;
        this.mPickerViewInfo = new SpenColorPickerViewInfo(spenColorPickerViewInfo);
        Resources resources = context.getResources();
        this.mCurrentColorString = resources.getString(R.string.pen_string_current_any, resources.getString(R.string.pen_string_color));
        this.mNewColorString = resources.getString(R.string.pen_string_new_any, resources.getString(R.string.pen_string_color));
        construct(context);
        changeMode(i);
        setDisplayColor(this.mCurrentColorView, this.mOldHsv);
        setColorAccessibility(this.mCurrentColorView, this.mOldHsv, this.mCurrentColorString);
        setDisplayColor(this.mNewColorView, fArr);
        setColorAccessibility(this.mNewColorView, fArr, this.mNewColorString);
        this.mPickerColor.addEventListener(this);
    }

    public void close() {
        if (this.mContext != null) {
            List<SpenHSVColor> list = this.mRecentColors;
            if (list != null) {
                list.clear();
                this.mRecentColors = null;
            }
            this.mOldHsv = null;
            SpenColorViewInterface spenColorViewInterface = this.mPickerView;
            if (spenColorViewInterface != null) {
                spenColorViewInterface.release();
                this.mPickerView = null;
            }
            SpenColorValueSeekBar spenColorValueSeekBar = this.mValueSeekBar;
            if (spenColorValueSeekBar != null) {
                spenColorValueSeekBar.release();
                this.mValueSeekBar = null;
            }
            SpenRGBCodeControl spenRGBCodeControl = this.mRGBCodeControl;
            if (spenRGBCodeControl != null) {
                spenRGBCodeControl.release();
                this.mRGBCodeControl = null;
            }
            this.mModeChangeListener = null;
            SpenPickerColor spenPickerColor = this.mPickerColor;
            if (spenPickerColor != null) {
                spenPickerColor.removeEventListener(this);
                this.mPickerColor.close();
                this.mPickerColor = null;
            }
            this.mContext = null;
            this.mPickerViewInfo = null;
            this.mRecentParent = null;
            this.mHexInputView = null;
            this.mRedInputView = null;
            this.mGreenInputView = null;
            this.mBlueInputView = null;
            this.mFocusViewID = 0;
        }
    }

    public void setColorListener(ColorListener colorListener) {
        this.mColorListener = colorListener;
    }

    public void setModeChangeListener(OnModeChangeListener onModeChangeListener) {
        if (this.mPickerViewInfo.modeType == 1 || this.mPickerViewInfo.modeType == 2) {
            this.mModeChangeListener = onModeChangeListener;
        }
    }

    public void setEyedropperClickListener(SpenColorPickerEyedropperListener spenColorPickerEyedropperListener) {
        if (this.mIsSupportEyedropper) {
            this.mEyedropperClickListener = spenColorPickerEyedropperListener;
        }
    }

    public void setColor(float[] fArr, float[] fArr2) {
        if (this.mContext == null || fArr == null || fArr2 == null) {
            return;
        }
        System.arraycopy(fArr, 0, this.mOldHsv, 0, 3);
        setDisplayColor(this.mCurrentColorView, this.mOldHsv);
        setColorAccessibility(this.mCurrentColorView, this.mOldHsv, this.mCurrentColorString);
        setCurrentColor(fArr2);
    }

    public boolean getCurrentColor(float[] fArr) {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            return spenPickerColor.getColor(fArr);
        }
        return false;
    }

    public void setCurrentColor(float[] fArr) {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.setColor(TAG, 255, fArr[0], fArr[1], fArr[2]);
        }
    }

    @Override
    public void update(String str, int i, float f, float f2, float f3) {
        StringBuilder sb = new StringBuilder();
        sb.append("update() who=");
        sb.append(str);
        sb.append(" color=");
        int i2 = 1;
        sb.append(String.format("%X", Integer.valueOf(i)));
        sb.append(" [");
        sb.append(f);
        sb.append(", ");
        sb.append(f2);
        sb.append(", ");
        sb.append(f3);
        sb.append("]");
        updateNewColor();
        if (!str.equals(TAG)) {
            if (str.equals("SpenColorValueSeekBar")) {
                i2 = 2;
            }
            notifyColorSelected(i2);
        }
    }

    public boolean setMode(int i) {
        boolean z = this.mMode != i;
        if (!z || isSupportModeChange()) {
            if (z) {
                toggleMode();
            }
            return z;
        }
        return false;
    }

    public void setRecentColors(float[] fArr, int i) {
        if (i <= 0 || fArr.length >= i * 3) {
            if (this.mRecentColors == null) {
                this.mRecentColors = new ArrayList();
            }
            this.mRecentColors.clear();
            for (int i2 = 0; i2 < i; i2++) {
                int i3 = i2 * 3;
                this.mRecentColors.add(new SpenHSVColor(fArr[i3], fArr[i3 + 1], fArr[i3 + 2]));
            }
            int i4 = this.mIsSupportEyedropper ? RECENT_COLOR_BUTTON_MAX - 1 : RECENT_COLOR_BUTTON_MAX;
            for (int i5 = 0; i5 < i4; i5++) {
                View childAt = this.mRecentParent.getChildAt(i5);
                GradientDrawable gradientDrawable = (GradientDrawable) ((RippleDrawable) childAt.getBackground()).getDrawable(0);
                if (i5 < this.mRecentColors.size()) {
                    gradientDrawable.setColor(this.mRecentColors.get(i5).getRGB());
                    childAt.setTag(Integer.valueOf(i5));
                    childAt.setOnClickListener(this.mRecentColorClickListener);
                    childAt.setEnabled(true);
                    childAt.setFocusable(true);
                    childAt.setImportantForAccessibility(1);
                    childAt.setContentDescription(getContext().getResources().getString(R.string.pen_palette_color_custom) + ", " + getContext().getResources().getString(R.string.pen_string_button));
                } else {
                    gradientDrawable.setColor(-1644826);
                    childAt.setEnabled(false);
                    childAt.setFocusable(false);
                    childAt.setImportantForAccessibility(2);
                }
            }
            return;
        }
    }

    public void onFocusChange(View view, boolean z) {
        if (this.mHexInputView != null && this.mRedInputView != null && this.mGreenInputView != null && this.mBlueInputView != null) {
            if (z) {
                int id = view.getId();
                if (id == this.mHexInputView.getId() || id == this.mRedInputView.getId() || id == this.mGreenInputView.getId() || id == this.mBlueInputView.getId()) {
                    this.mFocusViewID = id;
                }
            } else if (this.mFocusViewID == view.getId()) {
                this.mFocusViewID = 0;
            }
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        LinearLayout linearLayout;
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        int x = ((int) motionEvent.getX()) + iArr[0];
        boolean z = true;
        int y = ((int) motionEvent.getY()) + iArr[1];
        if (!(motionEvent.getAction() != 0 || this.mHexInputView == null || this.mRedInputView == null || this.mGreenInputView == null || this.mBlueInputView == null || (linearLayout = this.mPickerContainer) == null || this.mFocusViewID == 0)) {
            View findViewById = linearLayout.getRootView().findViewById(this.mFocusViewID);
            if (findViewById instanceof EditText) {
                if (!checkViewConstainsPoint(this.mHexInputView, x, y) && !checkViewConstainsPoint(this.mRedInputView, x, y) && !checkViewConstainsPoint(this.mGreenInputView, x, y) && !checkViewConstainsPoint(this.mBlueInputView, x, y)) {
                    z = false;
                }
                if (!z) {
                    if (!checkViewConstainsPoint(findViewById, x, y)) {
                        findViewById.clearFocus();
                    }
                    ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(findViewById.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public int getFocusID() {
        return this.mFocusViewID;
    }

    private void construct(Context context) {
        View inflate = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(this.mPickerViewInfo.layoutId, (ViewGroup) this, false);
        addView(inflate);
        this.mOutlineSize = context.getResources().getDimensionPixelOffset(R.dimen.setting_color_picker_select_outline);
        this.mOutlineColor = SpenSettingUtil.getColor(context, R.color.setting_color_picker_adaptive_outline);
        this.mCurrentColorView = inflate.findViewById(R.id.display_current_view);
        this.mNewColorView = inflate.findViewById(R.id.display_new_view);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(this.mPickerViewInfo.colorDisplayRadius);
        if (context.getResources().getConfiguration().getLayoutDirection() == 1) {
            float f = (float) dimensionPixelSize;
            this.mCurrentColorView.setBackground(SpenSettingUtilDrawable.getRoundedRectDrawable(0.0f, f, 0.0f, f, this.mOutlineSize, this.mOutlineColor));
            this.mNewColorView.setBackground(SpenSettingUtilDrawable.getRoundedRectDrawable(f, 0.0f, f, 0.0f, this.mOutlineSize, this.mOutlineColor));
        } else {
            float f2 = (float) dimensionPixelSize;
            this.mCurrentColorView.setBackground(SpenSettingUtilDrawable.getRoundedRectDrawable(f2, 0.0f, f2, 0.0f, this.mOutlineSize, this.mOutlineColor));
            this.mNewColorView.setBackground(SpenSettingUtilDrawable.getRoundedRectDrawable(0.0f, f2, 0.0f, f2, this.mOutlineSize, this.mOutlineColor));
        }
        initTabGroup(inflate, this.mMode);
        this.mPickerContainer = (LinearLayout) inflate.findViewById(R.id.color_pick_area);
        this.mValueSeekBar = (SpenColorValueSeekBar) inflate.findViewById(R.id.color_picker_seek_bar);
        setDisplayColor(this.mCurrentColorView, this.mOldHsv);
        setColorAccessibility(this.mCurrentColorView, this.mOldHsv, this.mCurrentColorString);
        SpenColorValueSeekBar spenColorValueSeekBar = this.mValueSeekBar;
        if (spenColorValueSeekBar != null) {
            spenColorValueSeekBar.setPickerColor(this.mPickerColor);
        }
        TextView textView = (TextView) inflate.findViewById(R.id.seek_bar_title);
        if (textView != null) {
            SpenSettingUtilText.applyUpToLargeLevel(context, 14.0f, textView);
        }
        if (this.mIsSupportRGBCode) {
            this.mRGBCodeControl = new SpenRGBCodeControl();
            this.mHexInputView = (EditText) inflate.findViewById(R.id.rgb_code);
            this.mRedInputView = (EditText) inflate.findViewById(R.id.red_code);
            this.mGreenInputView = (EditText) inflate.findViewById(R.id.green_code);
            this.mBlueInputView = (EditText) inflate.findViewById(R.id.blue_code);
            SpenSettingUtilText.applyUpToLargeLevel(context, 12.0f, (TextView) inflate.findViewById(R.id.rgb_title), (TextView) inflate.findViewById(R.id.red_title), (TextView) inflate.findViewById(R.id.green_title), (TextView) inflate.findViewById(R.id.blue_title));
            SpenSettingUtilText.applyUpToLargeLevel(context, 14.0f, this.mHexInputView, this.mRedInputView, this.mGreenInputView, this.mBlueInputView);
            this.mHexInputView.setOnFocusChangeListener(this);
            this.mRedInputView.setOnFocusChangeListener(this);
            this.mGreenInputView.setOnFocusChangeListener(this);
            this.mBlueInputView.setOnFocusChangeListener(this);
            this.mHexInputView.setOnEditorActionListener(this.mOnEditorActionListener);
            this.mRedInputView.setOnEditorActionListener(this.mOnEditorActionListener);
            this.mGreenInputView.setOnEditorActionListener(this.mOnEditorActionListener);
            this.mBlueInputView.setOnEditorActionListener(this.mOnEditorActionListener);
            this.mRGBCodeControl.setRGBView(this.mHexInputView, this.mRedInputView, this.mGreenInputView, this.mBlueInputView);
            this.mRGBCodeControl.setPickerColor(this.mPickerColor);
            this.mRGBCodeControl.setEditorActionListener(this.mOnEditorActionListener);
        }
        this.mRecentParent = (ViewGroup) inflate.findViewById(R.id.color_picker_recent_color_button_layout);
        setRecentColors(null, 0);
        ViewGroup viewGroup = this.mRecentParent;
        initEyedropperButton(viewGroup.getChildAt(viewGroup.getChildCount() - 1));
    }

    private void initTabGroup(View view, int i) {
        Button button = (Button) view.findViewById(R.id.tab_swatch);
        Button button2 = (Button) view.findViewById(R.id.tab_spectrum);
        if (button == null || button2 == null) {
            return;
        }
        this.mPickerTabGroup = new SpenPickerTabGroup();
        this.mPickerTabGroup.addTab(button);
        this.mPickerTabGroup.addTab(button2);
        this.mPickerTabGroup.select(i == 1 ? R.id.tab_spectrum : R.id.tab_swatch);
        this.mPickerTabGroup.setOnTabSelectedListener(new SpenPickerTabGroup.OnTabSelectedListener() {
            /* class com.samsung.android.sdk.pen.settingui.colorpicker.SpenColorPickerView.AnonymousClass1 */

            @Override // com.samsung.android.sdk.pen.settingui.colorpicker.SpenPickerTabGroup.OnTabSelectedListener
            public void onTabUnselected(View view) {
            }

            @Override // com.samsung.android.sdk.pen.settingui.colorpicker.SpenPickerTabGroup.OnTabSelectedListener
            public void onTabSelected(View view) {
                SpenColorPickerView.this.toggleMode();
                if (SpenColorPickerView.this.mModeChangeListener != null) {
                    SpenColorPickerView.this.mModeChangeListener.onModeChanged(SpenColorPickerView.this.mMode);
                }
            }

            @Override // com.samsung.android.sdk.pen.settingui.colorpicker.SpenPickerTabGroup.OnTabSelectedListener
            public void onTabReselected(View view) {
            }
        });
    }

    private void updateNewColor() {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            float[] fArr = {0.0f, 0.0f, 0.0f};
            spenPickerColor.getColor(fArr);
            setDisplayColor(this.mNewColorView, fArr);
            setColorAccessibility(this.mNewColorView, fArr, this.mNewColorString);
        }
    }

    private void setDisplayColor(View view, float[] fArr) {
        int HSVToColor = SpenSettingUtil.HSVToColor(fArr);
        GradientDrawable gradientDrawable = (GradientDrawable) view.getBackground();
        gradientDrawable.setStroke(this.mOutlineSize, HSVToColor);
        gradientDrawable.setColor(HSVToColor);
    }

    private void setColorAccessibility(View view, float[] fArr, String str) {
        view.setContentDescription(str);
    }

    private void toggleMode() {
        int i = 1;
        if (this.mMode == 1) {
            i = 2;
        }
        changeMode(i);
        this.mMode = i;
    }

    private void changeMode(int i) {
        SpenColorViewInterface spenColorViewInterface;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        Resources resources = this.mContext.getResources();
        if (i == 1) {
            spenColorViewInterface = new SpenColorGradientView(this.mContext, this.mPickerViewInfo.gradientCursorSizeDimen, this.mPickerViewInfo.gradientCursorOutlineDimen);
            i7 = R.drawable.note_pensettings_picker_01;
            i6 = R.string.pen_string_swatch_mode;
            i5 = resources.getDimensionPixelSize(this.mPickerViewInfo.gradientHeightDimen);
            View view = (View) spenColorViewInterface;
            view.setSoundEffectsEnabled(true);
            view.setClickable(true);
            view.setFocusable(false);
            view.setContentDescription(this.mContext.getResources().getString(R.string.pen_string_select_color) + " " + this.mContext.getResources().getString(R.string.pen_string_color_double_tap_to_apply));
            i4 = resources.getDimensionPixelSize(this.mPickerViewInfo.gradientModeBtnSize);
            i3 = resources.getDimensionPixelSize(this.mPickerViewInfo.gradientModeBtnStartMargin);
            i2 = 0;
        } else {
            int dimensionPixelSize = resources.getDimensionPixelSize(this.mPickerViewInfo.swatchTopMarginDimen);
            spenColorViewInterface = new SpenColorSwatchView(this.mContext, this.mPickerViewInfo.itemLayoutId, resources.getDimensionPixelSize(this.mPickerViewInfo.swatchStartMarginDimen), dimensionPixelSize, resources.getDimensionPixelSize(this.mPickerViewInfo.swatchEndMarginDimen), resources.getDimensionPixelSize(this.mPickerViewInfo.swatchBottomMarginDimen));
            i7 = R.drawable.note_pensettings_picker_02;
            i6 = R.string.pen_string_spectrum_mode;
            i4 = resources.getDimensionPixelSize(this.mPickerViewInfo.swatchModeBtnSize);
            i3 = resources.getDimensionPixelSize(this.mPickerViewInfo.swatchModeBtnStartMargin);
            i5 = -2;
            i2 = 8;
        }
        spenColorViewInterface.setPickerColor(this.mPickerColor);
        this.mPickerContainer.addView((View) spenColorViewInterface, 0, new LayoutParams(-1, i5));
        SpenColorViewInterface spenColorViewInterface2 = this.mPickerView;
        if (spenColorViewInterface2 != null) {
            this.mPickerContainer.removeView((View) spenColorViewInterface2);
            this.mPickerView.release();
        }
        this.mPickerView = spenColorViewInterface;
        ImageButton imageButton = this.mModeButton;
        if (imageButton != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageButton.getLayoutParams();
            layoutParams.width = i4;
            layoutParams.height = i4;
            layoutParams.setMarginStart(i3);
            this.mModeButton.setLayoutParams(layoutParams);
            this.mModeButton.setBackgroundResource(i7);
            this.mModeButton.setContentDescription(resources.getString(i6));
            SpenSettingUtilHover.setHoverText(this.mModeButton, resources.getString(i6), true);
        }
        SpenColorValueSeekBar spenColorValueSeekBar = this.mValueSeekBar;
        if (spenColorValueSeekBar != null) {
            spenColorValueSeekBar.setVisibility(i2);
        }
    }

    private void notifyColorSelected(int i) {
        float[] fArr = {0.0f, 0.0f, 0.0f};
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor == null || !spenPickerColor.getColor(fArr)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" notifyColorSelected() type");
        sb.append(i);
        sb.append(" Color[");
        sb.append(fArr[0]);
        sb.append(", ");
        sb.append(fArr[1]);
        sb.append(", ");
        sb.append(fArr[2]);
        sb.append("] mColorListener is ");
        sb.append(this.mColorListener != null ? "NOT NULL" : "NULL");
        ColorListener colorListener = this.mColorListener;
        if (colorListener != null) {
            colorListener.onColorSelected(fArr[0], fArr[1], fArr[2], i);
        }
    }

    private boolean isReleaseResource() {
        return this.mContext == null;
    }

    private void initEyedropperButton(View view) {
        if (this.mIsSupportEyedropper) {
            view.setBackgroundResource(this.mPickerViewInfo.eyedropperBgResourceId);
            setForegroundDrawable(this.mContext, view, R.drawable.spen_round_ripple);
            Resources resources = this.mContext.getResources();
            SpenSettingUtilHover.setHoverText(view, resources.getString(R.string.pen_string_color_spuit));
            view.setContentDescription(resources.getString(R.string.pen_string_color_spuit));
            view.setOnClickListener(new OnClickListener() {

                public void onClick(View view) {
                    if (SpenColorPickerView.this.mEyedropperClickListener != null) {
                        SpenColorPickerView.this.mEyedropperClickListener.onEyedropperButtonClicked();
                    }
                }
            });
        }
    }

    public static void setForegroundDrawable(Context context, View view, int i) {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        if (i == 0) {
            view.setForeground(null);
        } else {
            view.setForeground(context.getResources().getDrawable(i, null));
        }
    }

    private boolean isSupportModeChange() {
        SpenColorPickerViewInfo spenColorPickerViewInfo = this.mPickerViewInfo;
        return spenColorPickerViewInfo != null && (spenColorPickerViewInfo.modeType == 2 || this.mPickerViewInfo.modeType == 1);
    }

    private boolean checkViewConstainsPoint(View view, int i, int i2) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        return new Rect(iArr[0], iArr[1], iArr[0] + view.getWidth(), iArr[1] + view.getHeight()).contains(i, i2);
    }
}
