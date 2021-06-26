package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.samsung.android.sdk.pen.settingui.common.SpenShowButtonShapeText;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtil;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilSIP;
import com.samsung.android.sdk.pen.settingui.util.SpenSettingUtilText;

import de.dlyt.yanndroid.samsung.R;

@TargetApi(17)
public class SpenColorPickerPopup extends Dialog {
    private static final int TYPE_CUSTOMIZE = 0;
    public static final int VIEW_MODE_GRADIENT = 1;
    public static final int VIEW_MODE_SWATCH = 2;
    private SpenShowButtonShapeText cancelTextView;
    private SpenShowButtonShapeText doneTextView;
    private final View.OnClickListener mCancelButtonClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            SpenColorPickerPopup.this.dismiss();
        }
    };
    private ColorPickerChangedListener mColorPickerChangedListener;
    private ColorPickerListener mColorPickerListener = null;
    private SpenColorPickerTheme mColorTheme;
    private Context mContext;
    private int mCurrentOrientation = 1;
    private SpenColorPickerDataManager mDataManager;
    private final View.OnClickListener mDoneButtonClickListener = new View.OnClickListener() {

        public void onClick(View view) {
            SpenColorPickerPopup.this.doneAction();
        }
    };
    private final SpenColorPickerEyedropperListener mEyedropperButtonClickListener = new SpenColorPickerEyedropperListener() {

        @Override
        public void onEyedropperButtonClicked() {
            if (SpenColorPickerPopup.this.mEyedropperButtonListener != null) {
                SpenColorPickerPopup.this.mEyedropperButtonListener.onEyedropperButtonClicked();
            }
        }
    };
    private PickerEyedropperButtonListener mEyedropperButtonListener;
    private boolean mIsKeyboardShowing = false;
    private boolean mIsSupportEyedropper;
    private boolean mIsSupportRGBCode;
    private RelativeLayout mParentLayout;
    private final SpenColorPickerActionListener mPickerActionListener = new SpenColorPickerActionListener() {

        @Override
        public void onRecentColorSelected() {
            if (SpenColorPickerPopup.this.mColorPickerListener != null) {
                SpenColorPickerPopup.this.mColorPickerListener.onRecentColorSelected();
            }
        }

        @Override
        public void onColorSeekBarChanged() {
            if (SpenColorPickerPopup.this.mColorPickerListener != null) {
                SpenColorPickerPopup.this.mColorPickerListener.onColorSeekBarPressed();
            }
        }

        @Override
        public void onColorPickerChanged(int i) {
            if (SpenColorPickerPopup.this.mColorPickerListener != null) {
                SpenColorPickerPopup.this.mColorPickerListener.onColorCirclePressed();
            }
        }
    };
    private final SpenColorPickerChangedListener mPickerChangedListener = new SpenColorPickerChangedListener() {

        @Override
        public void onColorChanged(int i, float[] fArr) {
        }

        @Override
        public void onViewModeChanged(int i) {
            if (SpenColorPickerPopup.this.mColorPickerChangedListener != null) {
                SpenColorPickerPopup.this.mColorPickerChangedListener.onViewModeChanged(i);
            }
        }
    };
    private SpenColorPickerControl mPickerControl;
    private SpenColorPickerView mPickerLayout;

    public interface ColorPickerChangedListener extends SpenColorPickerChangedListener {
        @Override
        void onColorChanged(int i, float[] fArr);

        @Override
        void onViewModeChanged(int i);
    }

    public interface ColorPickerListener {
        void onColorCirclePressed();

        void onColorPickerUsage(int i);

        void onColorSeekBarPressed();

        void onRecentColorSelected();
    }

    public interface PickerEyedropperButtonListener extends SpenColorPickerEyedropperListener {
    }

    public SpenColorPickerPopup(Context context, int i, float[] fArr, boolean z) {
        super(context, R.style.ColorPickerPopupDialog);
        SpenSettingUtil.initDialogWindow(this, 5376, -1);
        getWindow().setFlags(256, 256);
        getWindow().setSoftInputMode(33);
        this.mIsSupportRGBCode = true;
        construct(context, i, fArr, z);
    }

    public SpenColorPickerPopup(Context context, float[] fArr) {
        super(context, R.style.ColorPickerPopupDialog);
        SpenSettingUtil.initDialogWindow(this, 4096, -1);
        this.mIsSupportRGBCode = false;
        construct(context, 2, fArr, false);
    }

    private void construct(Context context, int i, float[] fArr, boolean z) {
        this.mContext = context;
        this.mColorTheme = new SpenColorPickerTheme(context, fArr);
        this.mPickerControl = new SpenColorPickerControl(i, fArr);
        this.mIsSupportEyedropper = z;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCurrentOrientation = this.mContext.getResources().getConfiguration().orientation;
        this.mDataManager = new SpenColorPickerDataManager(this.mContext);
        init();
    }

    public void setColorTheme(int i) {
        SpenColorPickerTheme spenColorPickerTheme = this.mColorTheme;
        if (spenColorPickerTheme != null) {
            boolean z = spenColorPickerTheme.getTheme() != i;
            if (z) {
                float[] fArr = new float[3];
                float[] fArr2 = new float[3];
                float[] fArr3 = new float[3];
                getCurrentColor(fArr2);
                this.mColorTheme.setTheme(i);
                this.mColorTheme.getOldVisibleColor(fArr);
                this.mColorTheme.getColor(fArr2, fArr3);
                this.mPickerControl.setColor(fArr, fArr3);
                initColor();
            }
        }
    }

    public boolean getCurrentColor(float[] fArr) {
        SpenColorPickerControl spenColorPickerControl = this.mPickerControl;
        if (spenColorPickerControl != null) {
            float[] fArr2 = new float[3];
            if (spenColorPickerControl.getCurrentColor(fArr2)) {
                return this.mColorTheme.getContentColor(fArr2, fArr);
            }
        }
        return false;
    }

    public void setCurrentColor(float[] fArr) {
        if (fArr == null || this.mPickerLayout == null) {
            return;
        }
        float[] fArr2 = new float[3];
        this.mColorTheme.getColor(fArr, fArr2);
        this.mPickerControl.setCurrentColor(fArr2);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        dismiss();
        return super.onTouchEvent(motionEvent);
    }

    @SuppressLint({"InlinedApi"})
    public void show(View view) {
        show();
    }

    public void show() {
        if (this.mIsSupportRGBCode && !this.mIsKeyboardShowing) {
            SpenSettingUtilSIP.forceHideSoftInput(getContext(), getCurrentFocus());
        }
        super.show();
    }

    public void onWindowFocusChanged(boolean z) {
        View findViewById;
        super.onWindowFocusChanged(z);
        if (this.mContext != null && this.mIsSupportRGBCode) {
            if (!z) {
                updateSIPState();
                SpenSettingUtilSIP.forceHideSoftInput(this.mContext, getCurrentFocus());
                return;
            }
            SpenColorPickerView spenColorPickerView = this.mPickerLayout;
            if (spenColorPickerView != null && this.mParentLayout != null && (findViewById = this.mParentLayout.findViewById(spenColorPickerView.getFocusID())) != null && (findViewById instanceof EditText)) {
                findViewById.setFocusable(true);
                findViewById.setFocusableInTouchMode(true);
                findViewById.requestFocus();
                if (this.mIsKeyboardShowing) {
                    SpenSettingUtilSIP.showSoftInput(this.mContext, findViewById, 0);
                }
            }
        }
    }

    public void close() {
        this.mContext = null;
        this.mColorPickerChangedListener = null;
        this.mColorPickerListener = null;
        this.mEyedropperButtonListener = null;
        SpenColorPickerView spenColorPickerView = this.mPickerLayout;
        if (spenColorPickerView != null) {
            spenColorPickerView.close();
            this.mPickerLayout = null;
        }
        SpenColorPickerControl spenColorPickerControl = this.mPickerControl;
        if (spenColorPickerControl != null) {
            spenColorPickerControl.close();
            this.mPickerControl = null;
        }
        SpenColorPickerTheme spenColorPickerTheme = this.mColorTheme;
        if (spenColorPickerTheme != null) {
            spenColorPickerTheme.close();
            this.mColorTheme = null;
        }
        this.cancelTextView = null;
        this.doneTextView = null;
        this.mParentLayout = null;
        SpenColorPickerDataManager spenColorPickerDataManager = this.mDataManager;
        if (spenColorPickerDataManager != null) {
            spenColorPickerDataManager.close();
            this.mDataManager = null;
        }
    }

    public void setColorPickerEyedropperButtonListener(PickerEyedropperButtonListener pickerEyedropperButtonListener) {
        StringBuilder sb = new StringBuilder();
        sb.append("setEyedropperButtonListener() mIsSupportEyedropper=");
        sb.append(this.mIsSupportEyedropper ? "SUPPORT" : "NOT SUPPORT");
        if (this.mIsSupportEyedropper) {
            this.mEyedropperButtonListener = pickerEyedropperButtonListener;
        }
    }

    public void setColorPickerListener(ColorPickerListener colorPickerListener) {
        this.mColorPickerListener = colorPickerListener;
    }

    public void setColorPickerChangeListener(ColorPickerChangedListener colorPickerChangedListener) {
        this.mColorPickerChangedListener = colorPickerChangedListener;
    }

    public void dismiss() {
        if (this.mIsSupportRGBCode) {
            SpenSettingUtilSIP.forceHideSoftInput(getContext(), this.mParentLayout);
        }
        super.dismiss();
        close();
    }

    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    public void setOrientationMode(int i) {
        if (i == 1 || i == 2 || this.mCurrentOrientation != i) {
            this.mCurrentOrientation = i;
            if (this.mParentLayout != null) {
                reInitView();
                return;
            }
            return;
        }
    }

    public void apply() {
        doneAction();
    }

    public int getViewMode() {
        return this.mPickerControl.getViewMode();
    }

    public void setViewMode(int i) {
        this.mPickerControl.setViewMode(i);
    }

    private void doneAction() {
        float[] fArr = new float[3];
        if (this.mPickerControl != null) {
            getCurrentColor(fArr);
            if (this.mColorPickerChangedListener != null) {
                if (fArr[1] > 1.0f) {
                    fArr[1] = 1.0f;
                }
                this.mColorPickerChangedListener.onColorChanged(SpenSettingUtil.HSVToColor(fArr), fArr);
            }
            ColorPickerListener colorPickerListener = this.mColorPickerListener;
            if (colorPickerListener != null) {
                colorPickerListener.onColorPickerUsage(0);
            }
            this.mDataManager.saveRecentColors(fArr);
        }
        dismiss();
    }

    private void init() {
        SpenColorPickerView spenColorPickerView = this.mPickerLayout;
        if (spenColorPickerView != null) {
            spenColorPickerView.close();
            this.mPickerLayout = null;
        }
        RelativeLayout relativeLayout = this.mParentLayout;
        if (relativeLayout != null) {
            relativeLayout.removeAllViews();
            this.mParentLayout = null;
        }
        initView();
        if (this.mCurrentOrientation == 1) {
            int statusBarHeight = SpenSettingUtil.getStatusBarHeight(this.mContext);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
            layoutParams.topMargin = statusBarHeight;
            layoutParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.setting_color_picker_popup_margin_bottom);
            setContentView(this.mParentLayout, layoutParams);
        } else {
            setContentView(this.mParentLayout);
        }
        setListener();
        initColor();
    }

    private void initColor() {
        SpenColorPickerDataManager spenColorPickerDataManager = this.mDataManager;
        if (spenColorPickerDataManager != null) {
            if (!spenColorPickerDataManager.isLoadComplete()) {
                this.mDataManager.loadRecentColors();
            }
            int recentColorCount = this.mDataManager.getRecentColorCount();
            float[] fArr = new float[(recentColorCount * 3)];
            for (int i = 0; i < recentColorCount; i++) {
                float[] fArr2 = {0.0f, 0.0f, 0.0f};
                float[] fArr3 = {0.0f, 0.0f, 0.0f};
                this.mDataManager.getRecentColor(i, fArr2);
                this.mColorTheme.getColor(fArr2, fArr3);
                for (int i2 = 0; i2 < 3; i2++) {
                    fArr[(i * 3) + i2] = fArr3[i2];
                }
            }
            this.mPickerLayout.setRecentColors(fArr, recentColorCount);
        }
    }

    private void setListener() {
        SpenColorPickerControl spenColorPickerControl = this.mPickerControl;
        if (spenColorPickerControl != null) {
            spenColorPickerControl.setPickerView(this.mPickerLayout);
            this.mPickerControl.setColorPickerChangeListener(this.mPickerChangedListener);
            this.mPickerControl.setColorPickerActionListener(this.mPickerActionListener);
        }
        SpenShowButtonShapeText spenShowButtonShapeText = this.cancelTextView;
        if (spenShowButtonShapeText != null) {
            spenShowButtonShapeText.setOnClickListener(this.mCancelButtonClickListener);
        }
        SpenShowButtonShapeText spenShowButtonShapeText2 = this.doneTextView;
        if (spenShowButtonShapeText2 != null) {
            spenShowButtonShapeText2.setOnClickListener(this.mDoneButtonClickListener);
        }
    }

    @TargetApi(17)
    private void initView() {
        SpenColorPickerViewInfo spenColorPickerViewInfo = new SpenColorPickerViewInfo();

        spenColorPickerViewInfo.layoutId = R.layout.setting_color_picker_view_oneui30;
        spenColorPickerViewInfo.modeType = 1;

        spenColorPickerViewInfo.itemLayoutId = R.layout.setting_color_swatch_item;
        spenColorPickerViewInfo.gradientCursorSizeDimen = R.dimen.color_picker_popup_content_point_size;
        spenColorPickerViewInfo.gradientCursorOutlineDimen = R.dimen.color_picker_popup_content_point_outline;
        spenColorPickerViewInfo.gradientSelectorRadiusDimen = R.dimen.setting_color_picker_color_swatch_margin_start;
        spenColorPickerViewInfo.gradientHeightDimen = R.dimen.setting_color_picker_color_gradient_height;
        spenColorPickerViewInfo.swatchTopMarginDimen = R.dimen.setting_color_picker_color_swatch_margin_top;
        spenColorPickerViewInfo.swatchStartMarginDimen = R.dimen.setting_color_picker_color_swatch_margin_start;
        spenColorPickerViewInfo.swatchEndMarginDimen = R.dimen.setting_color_picker_color_swatch_margin_end;
        spenColorPickerViewInfo.swatchBottomMarginDimen = R.dimen.setting_color_picker_color_swatch_margin_bottom;
        spenColorPickerViewInfo.gradientModeBtnSize = R.dimen.setting_color_picker_popup_gradient_mode_btn_size;
        spenColorPickerViewInfo.gradientModeBtnStartMargin = R.dimen.setting_color_picker_popup_gradient_mode_btn_margin_start;
        spenColorPickerViewInfo.swatchModeBtnSize = R.dimen.setting_color_picker_popup_swatch_mode_btn_size;
        spenColorPickerViewInfo.swatchModeBtnStartMargin = R.dimen.setting_color_picker_popup_swatch_mode_btn_margin_start;
        spenColorPickerViewInfo.colorDisplayRadius = R.dimen.setting_color_picker_popup_color_display_radius;
        spenColorPickerViewInfo.eyedropperBgResourceId = R.drawable.color_picker_recent_eyedropper;

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources mSdkResources = mContext.getResources();
        this.mParentLayout = (RelativeLayout) mInflater.inflate(mSdkResources.getLayout(R.layout.setting_color_picker_layout), null);

        FrameLayout frameLayout = (FrameLayout) this.mParentLayout.findViewById(R.id.popup_content_view);
        float[] fArr = new float[3];
        this.mPickerControl.getOldColor(fArr);
        this.mPickerLayout = new SpenColorPickerView(this.mContext, this.mPickerControl.getViewMode(), fArr, spenColorPickerViewInfo, this.mIsSupportRGBCode, this.mIsSupportEyedropper);
        if (this.mIsSupportEyedropper) {
            this.mPickerLayout.setEyedropperClickListener(this.mEyedropperButtonClickListener);
        }
        frameLayout.addView(this.mPickerLayout, new ViewGroup.LayoutParams(getPickerLayoutWidth(), -2));
        this.cancelTextView = (SpenShowButtonShapeText) this.mParentLayout.findViewById(R.id.color_picker_button_cancel);
        this.doneTextView = (SpenShowButtonShapeText) this.mParentLayout.findViewById(R.id.color_picker_button_done);
        SpenSettingUtilText.setTypeFace(this.mContext, SpenSettingUtilText.STYLE_MEDIUM, this.cancelTextView, this.doneTextView);
        SpenSettingUtilText.applyUpToLargeLevel(this.mContext, 16.0f, this.cancelTextView, this.doneTextView);
        SpenShowButtonShapeText spenShowButtonShapeText = this.cancelTextView;
        spenShowButtonShapeText.setContentDescription(this.mContext.getResources().getString(R.string.pen_string_cancel) + " " + this.mContext.getResources().getString(R.string.pen_string_button));
        SpenShowButtonShapeText spenShowButtonShapeText2 = this.doneTextView;
        spenShowButtonShapeText2.setContentDescription(this.mContext.getResources().getString(R.string.pen_string_done) + " " + this.mContext.getResources().getString(R.string.pen_string_button));
        this.cancelTextView.setButtonShapeEnabled(true);
        this.doneTextView.setButtonShapeEnabled(true);
        if (Build.VERSION.SDK_INT >= 26) {
            this.mParentLayout.findViewById(R.id.content_main).setFocusable(View.NOT_FOCUSABLE);
        }
    }

    private void reInitView() {
        int selectionEnd;
        float[] fArr;
        SpenShowButtonShapeText spenShowButtonShapeText = this.doneTextView;
        int i = TYPE_CUSTOMIZE;
        boolean isFocused = spenShowButtonShapeText != null ? spenShowButtonShapeText.isFocused() : false;
        if (this.mIsSupportRGBCode) {
            updateSIPState();
            i = this.mPickerLayout.getFocusID();
            View findViewById = this.mParentLayout.findViewById(i);
            if (findViewById != null && (findViewById instanceof EditText)) {
                EditText editText = (EditText) findViewById;
                selectionEnd = editText.getSelectionStart() == editText.getSelectionEnd() ? editText.getSelectionEnd() : -1;
                if (this.mIsKeyboardShowing) {
                    SpenSettingUtilSIP.showSoftInput(this.mContext, findViewById, VIEW_MODE_SWATCH);
                }
                fArr = new float[3];
                this.mPickerControl.getCurrentColor(fArr);
                init();
                this.mPickerControl.setCurrentColor(fArr);
                if (isFocused) {
                    this.doneTextView.requestFocus();
                }
                if (this.mIsSupportRGBCode && i != 0) {
                    View findViewById2 = this.mParentLayout.findViewById(i);
                    if (findViewById2 == null || !(findViewById2 instanceof EditText)) {
                        SpenSettingUtilSIP.forceHideSoftInput(this.mContext, findViewById2);
                        updateSIPState();
                        return;
                    }
                    findViewById2.setFocusable(true);
                    findViewById2.setFocusableInTouchMode(true);
                    findViewById2.requestFocus();
                    if (selectionEnd != -1) {
                        ((EditText) findViewById2).setSelection(selectionEnd);
                    }
                    if (this.mIsKeyboardShowing && !SpenSettingUtilSIP.isSIPShowing(this.mContext)) {
                        SpenSettingUtilSIP.showSoftInput(this.mContext, findViewById2, VIEW_MODE_GRADIENT);
                        return;
                    }
                    return;
                }
            }
        }
        selectionEnd = -1;
        fArr = new float[3];
        this.mPickerControl.getCurrentColor(fArr);
        init();
        this.mPickerControl.setCurrentColor(fArr);
        if (isFocused) {
            this.doneTextView.requestFocus();
        }
        if (this.mIsSupportRGBCode) {
        }
    }


    private void updateSIPState() {
        Context context = this.mContext;
        if (context != null) {
            this.mIsKeyboardShowing = SpenSettingUtilSIP.isSIPShowing(context);
        }
    }

    private int getPickerLayoutWidth() {
        boolean z = true;
        if (this.mCurrentOrientation == 1) {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.setting_color_picker_color_area_min_width);
            int i = this.mContext.getResources().getDisplayMetrics().widthPixels;
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
                z = false;
            }
            if (i < dimensionPixelSize) {
                return dimensionPixelSize;
            }
            if (z) {
                float f = ((float) i) / this.mContext.getResources().getDisplayMetrics().density;
                getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    public void onGlobalLayout() {
                        SpenColorPickerPopup.this.getWindow().getDecorView().getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int width = SpenColorPickerPopup.this.getWindow().getDecorView().getRootView().getWidth();
                        ViewGroup.LayoutParams layoutParams = SpenColorPickerPopup.this.mPickerLayout.getLayoutParams();
                        layoutParams.width = width + -1;
                        SpenColorPickerPopup.this.mPickerLayout.setLayoutParams(layoutParams);
                    }
                });
                return dimensionPixelSize;
            }
        }
        return -1;
    }
}
