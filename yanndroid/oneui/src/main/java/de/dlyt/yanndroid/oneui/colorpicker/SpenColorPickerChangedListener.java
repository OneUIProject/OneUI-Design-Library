package de.dlyt.yanndroid.oneui.colorpicker;

public interface SpenColorPickerChangedListener {
    int VIEW_MODE_GRADIENT = 1;
    int VIEW_MODE_SWATCH = 2;

    void onColorChanged(int i, float[] fArr);

    void onViewModeChanged(int i);
}
