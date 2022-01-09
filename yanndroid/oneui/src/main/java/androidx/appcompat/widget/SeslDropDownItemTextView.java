package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import de.dlyt.yanndroid.oneui.R;

public class SeslDropDownItemTextView extends SeslCheckedTextView {
    private static final String TAG = SeslDropDownItemTextView.class.getSimpleName();

    public SeslDropDownItemTextView(Context context) {
        this(context, null);
    }

    public SeslDropDownItemTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16842884);
    }

    public SeslDropDownItemTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void setChecked(boolean z) {
        Context context;
        super.setChecked(z);
        setTypeface(Typeface.create("sec-roboto-light", z ? 1 : 0));
        if (z && (context = getContext()) != null && getCurrentTextColor() == -65281) {
            Log.w(TAG, "text color reload!");
            ColorStateList colorStateList = ResourcesCompat.getColorStateList(context.getResources(), R.color.sesl_spinner_dropdown_text_color, context.getTheme());
            if (colorStateList != null) {
                setTextColor(colorStateList);
            } else {
                Log.w(TAG, "Didn't set SeslDropDownItemTextView text color!!");
            }
        }
    }
}