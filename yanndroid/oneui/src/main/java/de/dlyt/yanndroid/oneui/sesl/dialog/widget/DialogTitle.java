package de.dlyt.yanndroid.oneui.sesl.dialog.widget;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import de.dlyt.yanndroid.oneui.R;

public class DialogTitle extends AppCompatTextView {
    private boolean mIsOneUI4;

    public DialogTitle(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
    }

    public DialogTitle(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
    }

    public DialogTitle(@NonNull Context context) {
        super(context);
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (layout != null) {
            final int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                final int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                if (ellipsisCount > 0) {
                    setSingleLine(false);
                    setMaxLines(2);

                    float textSize = getContext().getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_dialog_title_text_size : R.dimen.sesl_dialog_title_text_size);
                    if (textSize != 0) {
                        float fontScale = getContext().getResources().getConfiguration().fontScale;
                        if (fontScale > 1.3f) {
                            textSize = (textSize / fontScale) * 1.3f;
                        }
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    }

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }
    }
}