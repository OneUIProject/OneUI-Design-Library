package com.samsung.android.sdk.pen.settingui.colorpicker;

import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class SpenRGBCodeControl implements SpenColorViewInterface, SpenPickerColorEventListener {
    public static final String KEY_HASH_TAG_CHAR = "#";
    private static final int COLOR_CHANEL_MAX_VALUE = 255;
    private static final int COLOR_CHANEL_MIN_VALUE = 0;
    private static final int HEX_BLUE = 3;
    private static final int HEX_GREEN = 2;
    private static final int HEX_RED = 1;
    private static final int NEW_LINE_CHARECTER_ASCII = 10;
    private static final String RGB_HEX_CHARACTERS = "ABCDEFabcdef";
    private static final int RGB_HEX_MAX_LENGHT = 6;
    private static final String RGB_HEX_STRING_DEFAULT = "000000";
    private static final String TAG = "SpenHexColorControl";
    private EditText mBlue;
    private EditText mGreen;
    private EditText mRGBCode;
    private EditText mRed;
    private boolean mIsUpdating = false;
    private TextView.OnEditorActionListener mOnEditorActionListener;
    private final InputFilter mRGBCodeTextFilter = new InputFilter() {

        public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
            String str = spanned.toString().substring(0, i3) + spanned.toString().substring(i4);
            SpenRGBCodeControl.this.checkActionKey(charSequence);
            if ((str.substring(0, i3) + charSequence.toString() + str.substring(i3)).length() > 6) {
                return "";
            }
            while (i < i2) {
                if (!Character.isDigit(charSequence.charAt(i)) && SpenRGBCodeControl.RGB_HEX_CHARACTERS.indexOf(charSequence.charAt(i)) == -1) {
                    return "";
                }
                i++;
            }
            return null;
        }
    };
    private SpenPickerColor mPickerColor;
    private final TextWatcher mColorTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void afterTextChanged(Editable editable) {
            int i;
            if (SpenRGBCodeControl.this.mPickerColor != null) {
                String obj = editable.toString();
                int number = SpenRGBCodeControl.this.getNumber(editable.toString());
                if (SpenRGBCodeControl.this.mRed.getEditableText().toString().equals(obj)) {
                    SpenRGBCodeControl spenRGBCodeControl = SpenRGBCodeControl.this;
                    int selectionIndex = spenRGBCodeControl.getSelectionIndex(spenRGBCodeControl.mRed, obj, number);
                    SpenRGBCodeControl spenRGBCodeControl2 = SpenRGBCodeControl.this;
                    spenRGBCodeControl2.updateColor(spenRGBCodeControl2.mRed, number, selectionIndex);
                    i = 1;
                } else if (SpenRGBCodeControl.this.mGreen.getEditableText().toString().equals(obj)) {
                    SpenRGBCodeControl spenRGBCodeControl3 = SpenRGBCodeControl.this;
                    int selectionIndex2 = spenRGBCodeControl3.getSelectionIndex(spenRGBCodeControl3.mGreen, obj, number);
                    SpenRGBCodeControl spenRGBCodeControl4 = SpenRGBCodeControl.this;
                    spenRGBCodeControl4.updateColor(spenRGBCodeControl4.mGreen, number, selectionIndex2);
                    i = 2;
                } else if (SpenRGBCodeControl.this.mBlue.getEditableText().toString().equals(obj)) {
                    SpenRGBCodeControl spenRGBCodeControl5 = SpenRGBCodeControl.this;
                    int selectionIndex3 = spenRGBCodeControl5.getSelectionIndex(spenRGBCodeControl5.mBlue, obj, number);
                    SpenRGBCodeControl spenRGBCodeControl6 = SpenRGBCodeControl.this;
                    spenRGBCodeControl6.updateColor(spenRGBCodeControl6.mBlue, number, selectionIndex3);
                    i = 3;
                } else {
                    return;
                }
                if (!SpenRGBCodeControl.this.mIsUpdating) {
                    SpenRGBCodeControl.this.updateColorByUser(i, number);
                }
            }
        }
    };
    private final TextWatcher mRGBCodeTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void afterTextChanged(Editable editable) {
            if (SpenRGBCodeControl.this.mPickerColor != null) {
                String str = editable.toString().toUpperCase() + SpenRGBCodeControl.RGB_HEX_STRING_DEFAULT;
                int parseColor = Color.parseColor(KEY_HASH_TAG_CHAR + str.substring(0, 6));
                int red = Color.red(parseColor);
                int green = Color.green(parseColor);
                int blue = Color.blue(parseColor);
                SpenRGBCodeControl spenRGBCodeControl1 = SpenRGBCodeControl.this;
                spenRGBCodeControl1.updateColor(spenRGBCodeControl1.mRed, red, -1);
                SpenRGBCodeControl spenRGBCodeControl2 = SpenRGBCodeControl.this;
                spenRGBCodeControl2.updateColor(spenRGBCodeControl2.mGreen, green, -1);
                SpenRGBCodeControl spenRGBCodeControl3 = SpenRGBCodeControl.this;
                spenRGBCodeControl3.updateColor(spenRGBCodeControl3.mBlue, blue, -1);
                SpenRGBCodeControl.this.notifyColorChanged(red, green, blue);
            }
        }
    };

    SpenRGBCodeControl() {
    }

    @Override
    public void setPickerColor(SpenPickerColor spenPickerColor) {
        this.mPickerColor = spenPickerColor;
        SpenPickerColor spenPickerColor2 = this.mPickerColor;
        if (spenPickerColor2 != null) {
            int color = spenPickerColor2.getColor();
            updateView(Color.red(color), Color.green(color), Color.blue(color));
            this.mPickerColor.addEventListener(this);
        }
    }

    @Override
    public void release() {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.removeEventListener(this);
            this.mPickerColor = null;
        }
        this.mOnEditorActionListener = null;
        this.mRed = null;
        this.mGreen = null;
        this.mBlue = null;
    }

    public void setRGBView(EditText editText, EditText editText2, EditText editText3, EditText editText4) {
        if (editText2 != null && editText3 != null && editText4 != null) {
            this.mRGBCode = editText;
            this.mRGBCode.setFilters(new InputFilter[]{this.mRGBCodeTextFilter});
            this.mRGBCode.addTextChangedListener(this.mRGBCodeTextWatcher);
            this.mRed = editText2;
            this.mRed.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
            this.mRed.addTextChangedListener(this.mColorTextWatcher);
            this.mGreen = editText3;
            this.mGreen.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
            this.mGreen.addTextChangedListener(this.mColorTextWatcher);
            this.mBlue = editText4;
            this.mBlue.setFilters(new InputFilter[]{new InputFilterMinMax(0, 255)});
            this.mBlue.addTextChangedListener(this.mColorTextWatcher);
        }
    }

    public void setEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        this.mOnEditorActionListener = onEditorActionListener;
    }

    private int getSelectionIndex(EditText editText, String str, int i) {
        if (editText == null || TextUtils.isEmpty(str)) {
            return -1;
        }
        if (str.length() > String.valueOf(i).length()) {
            return editText.getSelectionStart() - (str.length() - String.valueOf(i).length());
        }
        return editText.getSelectionStart();
    }

    private int getNumber(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            return 0;
        }
    }

    private void updateColorByUser(int i, int number) {
        int r;
        int g;
        int b;
        if (i == 1) {
            r = number;
            g = Integer.parseInt(this.mGreen.getText().toString());
            b = Integer.parseInt(this.mBlue.getText().toString());
        } else if (i == 2) {
            r = Integer.parseInt(this.mRed.getText().toString());
            g = number;
            b = Integer.parseInt(this.mBlue.getText().toString());
        } else if (i == 3) {
            r = Integer.parseInt(this.mRed.getText().toString());
            g = Integer.parseInt(this.mGreen.getText().toString());
            b = number;
        } else {
            return;
        }
        updateCodeText(r, g, b);
        notifyColorChanged(r, g, b);
    }

    private void notifyColorChanged(int i, int i2, int i3) {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.setColor(TAG, Color.rgb(i, i2, i3));
        }
    }

    private void updateColor(EditText editText, int i, int i2) {
        if (editText != null && !this.mIsUpdating) {
            String valueOf = String.valueOf(i);
            if (!valueOf.equals(editText.getEditableText().toString())) {
                this.mIsUpdating = true;
                editText.setText(valueOf);
                if (i2 == -1) {
                    editText.setSelection(valueOf.length());
                } else {
                    editText.setSelection(i2);
                }
                this.mIsUpdating = false;
            }
        }
    }

    @Override
    public void update(String str, int i, float f, float f2, float f3) {
        if (!str.equals(TAG)) {
            updateView(Color.red(i), Color.green(i), Color.blue(i));
        }
    }

    private void updateView(int i, int i2, int i3) {
        EditText editText = this.mRed;
        if (!(editText == null || this.mGreen == null || this.mBlue == null)) {
            updateColor(editText, i, -1);
            updateColor(this.mGreen, i2, -1);
            updateColor(this.mBlue, i3, -1);
        }
        updateCodeText(i, i2, i3);
    }

    private void updateCodeText(int i, int i2, int i3) {
        if (this.mRGBCode != null) {
            this.mRGBCode.setText(String.format("%02X%02X%02X", Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)));
        }
    }

    private void checkActionKey(CharSequence charSequence) {
        TextView.OnEditorActionListener onEditorActionListener;
        if (charSequence != null) {
            if ((charSequence == null || charSequence.length() == 1) && charSequence.charAt(0) == '\n' && (onEditorActionListener = this.mOnEditorActionListener) != null) {
                onEditorActionListener.onEditorAction(null, 6, null);
            }
        }
    }

    public class InputFilterMinMax implements InputFilter {
        private final int max;
        private final int min;

        public InputFilterMinMax(int i, int i2) {
            this.min = i;
            this.max = i2;
        }

        private boolean isInRange(int i, int i2, int i3) {
            if (i2 > i) {
                return i3 >= i && i3 <= i2;
            } else return i3 >= i2 && i3 <= i;
        }

        public CharSequence filter(CharSequence charSequence, int i, int i2, Spanned spanned, int i3, int i4) {
            SpenRGBCodeControl.this.checkActionKey(charSequence);
            try {
                String str = spanned.toString().substring(0, i3) + spanned.toString().substring(i4);
                if (isInRange(this.min, this.max, Integer.parseInt(str.substring(0, i3) + charSequence.toString() + str.substring(i3)))) {
                    return null;
                }
                return "";
            } catch (NumberFormatException unused) {
                return "";
            }
        }
    }
}
