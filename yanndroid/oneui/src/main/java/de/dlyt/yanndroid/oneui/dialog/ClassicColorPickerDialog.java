package de.dlyt.yanndroid.oneui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.colorpicker.classic.SeslColorPicker;

public class ClassicColorPickerDialog extends AlertDialog implements DialogInterface.OnClickListener {
    private static final String TAG = "SeslColorPickerDialog";
    private final SeslColorPicker mColorPicker;
    private Integer mCurrentColor;
    private final OnColorSetListener mOnColorSetListener;

    public ClassicColorPickerDialog(Context context, OnColorSetListener listener) {
        super(context, resolveDialogTheme(context));
        mCurrentColor = null;
        View inflate = LayoutInflater.from(context).inflate(R.layout.sesl_color_picker_dialog, (ViewGroup) null);
        setView(inflate);
        setButton(-1, context.getString(R.string.sesl_picker_done), this);
        setButton(-2, context.getString(R.string.sesl_picker_cancel), this);
        requestWindowFeature(1);
        getWindow().setSoftInputMode(16);
        mOnColorSetListener = listener;
        mColorPicker = (SeslColorPicker) inflate.findViewById(R.id.sesl_color_picker_content_view);
    }

    public ClassicColorPickerDialog(Context context, OnColorSetListener listener, int currentColor) {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().setCurrentColor(Integer.valueOf(currentColor));
        this.mCurrentColor = Integer.valueOf(currentColor);
        this.mColorPicker.updateRecentColorLayout();
    }

    public ClassicColorPickerDialog(Context context, OnColorSetListener listener, int[] recentColors) {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().initRecentColorInfo(recentColors);
        this.mColorPicker.updateRecentColorLayout();
    }

    public ClassicColorPickerDialog(Context context, OnColorSetListener listener, int currentColor, int[] recentColors) {
        this(context, listener);
        this.mColorPicker.getRecentColorInfo().initRecentColorInfo(recentColors);
        this.mColorPicker.getRecentColorInfo().setCurrentColor(Integer.valueOf(currentColor));
        this.mCurrentColor = Integer.valueOf(currentColor);
        this.mColorPicker.updateRecentColorLayout();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Integer num;
        if (i == -1) {
            this.mColorPicker.saveSelectedColor();
            if (this.mOnColorSetListener == null) {
                return;
            }
            if (this.mColorPicker.isUserInputValid() || (num = this.mCurrentColor) == null) {
                this.mOnColorSetListener.onColorSet(this.mColorPicker.getRecentColorInfo().getSelectedColor().intValue());
            } else {
                this.mOnColorSetListener.onColorSet(num.intValue());
            }
        }
    }

    public SeslColorPicker getColorPicker() {
        return mColorPicker;
    }

    public void setNewColor(int newColor) {
        mColorPicker.getRecentColorInfo().setNewColor(newColor);
        mColorPicker.updateRecentColorLayout();
    }

    public void setTransparencyControlEnabled(boolean enabled) {
        mColorPicker.setOpacityBarEnabled(enabled);
    }

    private static int resolveDialogTheme(Context context) {
        return context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false) ? R.style.OneUI4_DialogTheme : R.style.DialogTheme;
    }


    public interface OnColorSetListener {
        void onColorSet(int newColor);
    }
}
