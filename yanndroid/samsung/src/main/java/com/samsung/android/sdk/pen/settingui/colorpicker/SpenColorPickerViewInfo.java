package com.samsung.android.sdk.pen.settingui.colorpicker;

/* access modifiers changed from: package-private */
public class SpenColorPickerViewInfo {
    protected static final int MODE_TYPE_BUTTON = 2;
    protected static final int MODE_TYPE_NONE = 0;
    protected static final int MODE_TYPE_TAB = 1;
    protected int colorDisplayRadius;
    protected int eyedropperBgResourceId;
    protected int gradientCursorOutlineDimen;
    protected int gradientCursorSizeDimen;
    protected int gradientHeightDimen;
    protected int gradientModeBtnSize;
    protected int gradientModeBtnStartMargin;
    protected int gradientSelectorRadiusDimen;
    protected int itemLayoutId;
    protected int layoutId;
    protected int modeType;
    protected int swatchBottomMarginDimen;
    protected int swatchEndMarginDimen;
    protected int swatchModeBtnSize;
    protected int swatchModeBtnStartMargin;
    protected int swatchStartMarginDimen;
    protected int swatchTopMarginDimen;

    SpenColorPickerViewInfo() {
        this.modeType = 0;
    }

    SpenColorPickerViewInfo(SpenColorPickerViewInfo spenColorPickerViewInfo) {
        this.layoutId = spenColorPickerViewInfo.layoutId;
        this.itemLayoutId = spenColorPickerViewInfo.itemLayoutId;
        this.gradientCursorSizeDimen = spenColorPickerViewInfo.gradientCursorSizeDimen;
        this.gradientCursorOutlineDimen = spenColorPickerViewInfo.gradientCursorOutlineDimen;
        this.gradientSelectorRadiusDimen = spenColorPickerViewInfo.gradientSelectorRadiusDimen;
        this.gradientHeightDimen = spenColorPickerViewInfo.gradientHeightDimen;
        this.swatchTopMarginDimen = spenColorPickerViewInfo.swatchTopMarginDimen;
        this.swatchStartMarginDimen = spenColorPickerViewInfo.swatchStartMarginDimen;
        this.swatchEndMarginDimen = spenColorPickerViewInfo.swatchEndMarginDimen;
        this.swatchBottomMarginDimen = spenColorPickerViewInfo.swatchBottomMarginDimen;
        this.gradientModeBtnSize = spenColorPickerViewInfo.gradientModeBtnSize;
        this.gradientModeBtnStartMargin = spenColorPickerViewInfo.gradientModeBtnStartMargin;
        this.swatchModeBtnSize = spenColorPickerViewInfo.swatchModeBtnSize;
        this.swatchModeBtnStartMargin = spenColorPickerViewInfo.swatchModeBtnStartMargin;
        this.colorDisplayRadius = spenColorPickerViewInfo.colorDisplayRadius;
        this.modeType = spenColorPickerViewInfo.modeType;
        this.eyedropperBgResourceId = spenColorPickerViewInfo.eyedropperBgResourceId;
    }
}
