package de.dlyt.yanndroid.oneui;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dlyt.yanndroid.oneui.colorpicker.SeslColorPicker;
import de.dlyt.yanndroid.oneui.dialog.SamsungAlertDialog;

public class ClassicColorPickerDialog extends SamsungAlertDialog implements DialogInterface.OnClickListener {
    private static final String TAG = "SeslColorPickerDialog";
    private final SeslColorPicker mColorPicker;
    private Integer mCurrentColor;
    private final ClassicColorPickerDialog.ColorPickerChangedListener mColorPickerChangedListener;

    public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener) {
        super(context);
        this.mCurrentColor = null;
        Context var3 = this.getContext();
        View var4 = LayoutInflater.from(var3).inflate(R.layout.sesl_color_picker_dialog, (ViewGroup)null);
        this.setView(var4);
        this.setButton(-1, var3.getString(R.string.sesl_picker_done), this);
        this.setButton(-2, var3.getString(android.R.string.cancel), this);
        this.requestWindowFeature(1);
        this.getWindow().setSoftInputMode(16);
        this.mColorPickerChangedListener = listener;
        this.mColorPicker = (SeslColorPicker)var4.findViewById(R.id.sesl_color_picker_content_view);
    }

    public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int currentColor) throws Throwable {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().setCurrentColor(currentColor);
        this.mCurrentColor = currentColor;
        this.mColorPicker.updateRecentColorLayout();
    }

    public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int currentColor, int[] recentColors) throws Throwable {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().initRecentColorInfo(recentColors);
        this.mColorPicker.getRecentColorInfo().setCurrentColor(currentColor);
        this.mCurrentColor = currentColor;
        this.mColorPicker.updateRecentColorLayout();
    }

    public ClassicColorPickerDialog(Context context, ClassicColorPickerDialog.ColorPickerChangedListener listener, int[] recentColors) throws Throwable {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().initRecentColorInfo(recentColors);
        this.mColorPicker.updateRecentColorLayout();
    }

    public SeslColorPicker getColorPicker() {
        return this.mColorPicker;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onClick(DialogInterface var1, int var2) {
        if (var2 != -2 && var2 == -1) {
            this.mColorPicker.saveSelectedColor();
            if (this.mColorPickerChangedListener != null) {
                if (!this.mColorPicker.isUserInputValid()) {
                    Integer var3 = this.mCurrentColor;
                    if (var3 != null) {
                        this.mColorPickerChangedListener.onColorChanged(var3);
                        return;
                    }
                }

                this.mColorPickerChangedListener.onColorChanged(this.mColorPicker.getRecentColorInfo().getSelectedColor());
            }
        }

    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setNewColor(Integer var1) throws Throwable {
        this.mColorPicker.getRecentColorInfo().setNewColor(var1);
        this.mColorPicker.updateRecentColorLayout();
    }

    public void setTransparencyControlEnabled(boolean var1) {
        this.mColorPicker.setOpacityBarEnabled(var1);
    }

    public interface ColorPickerChangedListener {
        void onColorChanged(int var1);
    }
}
