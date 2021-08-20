package de.dlyt.yanndroid.oneui.dialog.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

import de.dlyt.yanndroid.oneui.R;

public class DialogTitle extends AppCompatTextView {
    public DialogTitle(Context context) {
        super(context);
    }

    public DialogTitle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public DialogTitle(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    @Override
    protected void onMeasure(int i, int i2) {
        int lineCount;
        super.onMeasure(i, i2);
        Layout layout = getLayout();
        if (layout != null && (lineCount = layout.getLineCount()) > 0 && layout.getEllipsisCount(lineCount - 1) > 0) {
            setSingleLine(false);
            setMaxLines(2);
            int dimensionPixelSize = getContext().getResources().getDimensionPixelSize(R.dimen.sesl_dialog_title_text_size);
            if (dimensionPixelSize != 0) {
                float f = getContext().getResources().getConfiguration().fontScale;
                float f2 = (float) dimensionPixelSize;
                if (f > 1.3f) {
                    f2 = (f2 / f) * 1.3f;
                }
                setTextSize(0, f2);
            }
            super.onMeasure(i, i2);
        }
    }
}