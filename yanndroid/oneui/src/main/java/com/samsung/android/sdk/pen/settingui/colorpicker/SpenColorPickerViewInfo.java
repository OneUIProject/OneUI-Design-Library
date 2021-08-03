package com.samsung.android.sdk.pen.settingui.colorpicker;

public class SpenColorPickerViewInfo {
    protected static final int MODE_TYPE_BUTTON = 2;
    protected static final int MODE_TYPE_NONE = 0;
    protected static final int MODE_TYPE_TAB = 1;
    public int colorDisplayRadius;
    public int gradientCursorOutlineDimen;
    public int gradientCursorSizeDimen;
    public int gradientHeightDimen;
    public int gradientModeBtnSize;
    public int gradientModeBtnStartMargin;
    public int gradientSelectorRadiusDimen;
    public int itemLayoutId;
    public int layoutId;
    public int modeType;
    public int swatchBottomMarginDimen;
    public int swatchEndMarginDimen;
    public int swatchModeBtnSize;
    public int swatchModeBtnStartMargin;
    public int swatchStartMarginDimen;
    public int swatchTopMarginDimen;

    public SpenColorPickerViewInfo() {
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
    }
}
