package de.dlyt.yanndroid.oneui.sesl.colorpicker;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.colorpicker.util.SpenSettingUtil;
import de.dlyt.yanndroid.oneui.sesl.colorpicker.util.SpenSettingUtilText;

public class SpenColorValueSeekBar extends RelativeLayout implements SpenColorViewInterface, SpenPickerColorEventListener {
    private static final String TAG = "SpenColorValueSeekBar";
    private final float[] mHsv = {0.0f, 0.0f, 0.0f};
    private final OnTouchListener mSeekBarOnTouchListener = new OnTouchListener() {

        public boolean onTouch(View view, MotionEvent motionEvent) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            return motionEvent.getAction() == 0;
        }
    };
    int[] mColors = {-16777216, -1};
    private SpenPickerColor mPickerColor;
    private GradientDrawable mProgressDrawable;
    private SeekBar mSeekBar;
    private TextView mSeekBarText;
    private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            if (z) {
                SpenColorValueSeekBar.this.mHsv[2] = ((float) i) / 255.0f;
                if (SpenColorValueSeekBar.this.mPickerColor != null) {
                    SpenColorValueSeekBar.this.mPickerColor.setColor(SpenColorValueSeekBar.TAG, 255, SpenColorValueSeekBar.this.mHsv[0], SpenColorValueSeekBar.this.mHsv[1], SpenColorValueSeekBar.this.mHsv[2]);
                }
            }
            SpenColorValueSeekBar spenColorValueSeekBar = SpenColorValueSeekBar.this;
            spenColorValueSeekBar.updateSeekBarText(spenColorValueSeekBar.mHsv[2]);
        }
    };

    public SpenColorValueSeekBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    @Override
    public void setPickerColor(SpenPickerColor spenPickerColor) {
        this.mPickerColor = spenPickerColor;
        this.mPickerColor.getColor(this.mHsv);
        float[] fArr = this.mHsv;
        updateSeekBar(fArr[0], fArr[1], fArr[2]);
        this.mPickerColor.addEventListener(this);
    }

    @Override
    public void release() {
        SpenPickerColor spenPickerColor = this.mPickerColor;
        if (spenPickerColor != null) {
            spenPickerColor.removeEventListener(this);
        }
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null) {
            seekBar.setOnTouchListener(null);
            this.mSeekBar.setOnSeekBarChangeListener(null);
            this.mSeekBar = null;
        }
        this.mProgressDrawable = null;
    }

    @Override
    public void update(String str, int i, float f, float f2, float f3) {
        float[] fArr = this.mHsv;
        fArr[0] = f;
        fArr[1] = f2;
        fArr[2] = f3;
        updateSeekBar(fArr[0], fArr[1], fArr[2]);
    }

    private void init() {
        View findViewById = findViewById(R.id.seekbar_accessibility_view);
        findViewById.setContentDescription(getContext().getResources().getString(R.string.pen_string_select_color) + " " + getContext().getResources().getString(R.string.pen_string_color_double_tap_to_apply));
        this.mSeekBar = findViewById(R.id.color_value_seek_bar);
        if (this.mSeekBar != null) {
            this.mColors = new int[]{-16777216, -1};
            this.mProgressDrawable = initProgressDrawable();
            this.mSeekBar.setProgressDrawable(this.mProgressDrawable);
            this.mSeekBar.setPadding(0, 0, 0, 0);
            this.mSeekBar.setOnTouchListener(this.mSeekBarOnTouchListener);
            this.mSeekBar.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        }
        this.mSeekBarText = findViewById(R.id.color_value_seek_bar_text);
        if (this.mSeekBarText != null) {
            SpenSettingUtilText.setTypeFace(getContext(), SpenSettingUtilText.STYLE_REGULAR, this.mSeekBarText);
            SpenSettingUtilText.applyUpToLargeLevel(getContext(), 14.0f, this.mSeekBarText);
        }
    }

    private GradientDrawable initProgressDrawable() {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, this.mColors);
        int dimensionPixelSize = getContext().getResources().getDimensionPixelSize(R.dimen.color_picker_popup_seekbar_track_height);
        gradientDrawable.setCornerRadius((float) dimensionPixelSize);
        gradientDrawable.setSize(0, dimensionPixelSize * 2);
        return gradientDrawable;
    }

    private void updateSeekBar(float f, float f2, float f3) {
        int i = (int) (f3 * 255.0f);
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null) {
            seekBar.setProgress(i);
            this.mColors[1] = SpenSettingUtil.HSVToColor(new float[]{f, f2, 1.0f});
            this.mProgressDrawable.mutate();
            this.mProgressDrawable.setColors(this.mColors);
            this.mSeekBar.setProgressDrawable(this.mProgressDrawable);
        }
    }

    private void updateSeekBarText(float f) {
        TextView textView = this.mSeekBarText;
        if (textView != null) {
            textView.setText(String.format("%d%%", Integer.valueOf((int) (f * 100.0f))));
        }
    }
}
