package de.dlyt.yanndroid.oneui.colorpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.SeekBar;

public class SeslColorPicker extends LinearLayout {
    private static final int CURRENT_COLOR_VIEW = 0;
    private static final int NEW_COLOR_VIEW = 1;
    static final int RECENT_COLOR_SLOT_COUNT = 6;
    private static final int RIPPLE_EFFECT_OPACITY = 61;
    private static final float SCALE_LARGE = 1.2F;
    private String[] mColorDescription;
    private SeslColorSwatchView mColorSwatchView;
    private final Context mContext;
    private GradientDrawable mCurrentColorBackground;
    private View mCurrentColorContainer;
    private ImageView mCurrentColorView;
    private float mCurrentFontScale;
    private final OnClickListener mImageButtonClickListener;
    private boolean mIsInputFromUser;
    private boolean mIsLightTheme;
    private boolean mIsOpacityBarEnabled;
    private SeslColorPicker.OnColorChangedListener mOnColorChangedListener;
    private SeslOpacitySeekBar mOpacitySeekBar;
    private FrameLayout mOpacitySeekBarContainer;
    private SeslColorPicker.PickedColor mPickedColor;
    private View mPickedColorContainer;
    private ImageView mPickedColorView;
    private final SeslRecentColorInfo mRecentColorInfo;
    private LinearLayout mRecentColorListLayout;
    private final ArrayList<Integer> mRecentColorValues;
    private View mRecentlyDivider;
    private TextView mRecentlyText;
    private final Resources mResources;
    private GradientDrawable mSelectedColorBackground;
    private final int[] mSmallestWidthDp = new int[]{320, 360, 411};

    public SeslColorPicker(Context var1, AttributeSet var2) {
        super(var1, var2);
        boolean var3 = false;
        this.mIsInputFromUser = false;
        this.mIsOpacityBarEnabled = false;
        this.mColorDescription = null;
        this.mImageButtonClickListener = new OnClickListener() {
            public void onClick(View var1) {
                int var2 = SeslColorPicker.this.mRecentColorValues.size();

                for(int var3 = 0; var3 < var2 && var3 < 6; ++var3) {
                    if (SeslColorPicker.this.mRecentColorListLayout.getChildAt(var3).equals(var1)) {
                        SeslColorPicker.this.mIsInputFromUser = true;
                        int var4 = (Integer)SeslColorPicker.this.mRecentColorValues.get(var3);
                        SeslColorPicker.this.mPickedColor.setColor(var4);
                        try {
                            SeslColorPicker.this.mapColorOnColorWheel(var4);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        if (SeslColorPicker.this.mOnColorChangedListener != null) {
                            SeslColorPicker.this.mOnColorChangedListener.onColorChanged(var4);
                        }
                    }
                }

            }
        };
        this.mContext = var1;
        this.mResources = this.getResources();
        TypedValue var4 = new TypedValue();
        this.mContext.getTheme().resolveAttribute(R.attr.isLightTheme, var4, true);
        if (var4.data != 0) {
            var3 = true;
        }

        this.mIsLightTheme = var3;
        this.mCurrentFontScale = this.mResources.getConfiguration().fontScale;
        LayoutInflater.from(var1).inflate(R.layout.sesl_color_picker_layout, this);
        this.mRecentColorInfo = new SeslRecentColorInfo();
        this.mRecentColorValues = this.mRecentColorInfo.getRecentColorInfo();
        this.mPickedColor = new SeslColorPicker.PickedColor();
        this.initDialogPadding();
        this.initCurrentColorView();
        this.initColorSwatchView();
        this.initOpacitySeekBar();
        this.initRecentColorLayout();
        this.updateCurrentColor();
        this.setInitialColors();
    }

    private void initColorSwatchView() {
        this.mColorSwatchView = (SeslColorSwatchView)this.findViewById(R.id.sesl_color_picker_color_swatch_view);
        this.mColorSwatchView.setOnColorSwatchChangedListener(new SeslColorSwatchView.OnColorSwatchChangedListener() {
            public void onColorSwatchChanged(int var1) {
                SeslColorPicker.this.mIsInputFromUser = true;
                SeslColorPicker.this.mPickedColor.setColor(var1);
                try {
                    SeslColorPicker.this.updateCurrentColor();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void initCurrentColorView() {
        this.mCurrentColorView = (ImageView)this.findViewById(R.id.sesl_color_picker_current_color_view);
        this.mPickedColorView = (ImageView)this.findViewById(R.id.sesl_color_picker_picked_color_view);
        Resources var1 = this.mResources;
        int var2;
        var2 = var1.getColor(R.color.sesl_color_picker_selected_color_item_text_color, null);
        TextView var5 = (TextView)this.findViewById(R.id.sesl_color_picker_current_color_text);
        var5.setTextColor(var2);
        TextView var3 = (TextView)this.findViewById(R.id.sesl_color_picker_picked_color_text);
        var3.setTextColor(var2);
        if (this.mCurrentFontScale > 1.2F) {
            float var4 = (float)this.mResources.getDimensionPixelOffset(R.dimen.sesl_color_picker_selected_color_text_size);
            var5.setTextSize(0, (float)Math.floor(Math.ceil((double)(var4 / this.mCurrentFontScale)) * 1.2000000476837158D));
            var3.setTextSize(0, (float)Math.floor(Math.ceil((double)(var4 / this.mCurrentFontScale)) * 1.2000000476837158D));
        }

        this.mCurrentColorContainer = this.findViewById(R.id.sesl_color_picker_current_color_focus);
        this.mPickedColorContainer = this.findViewById(R.id.sesl_color_picker_picked_color_focus);
        this.mSelectedColorBackground = (GradientDrawable)this.mPickedColorView.getBackground();
        Integer var6 = this.mPickedColor.getColor();
        if (var6 != null) {
            this.mSelectedColorBackground.setColor(var6);
        }

        this.mCurrentColorBackground = (GradientDrawable)this.mCurrentColorView.getBackground();
    }

    private void initDialogPadding() {
        if (this.mResources.getConfiguration().orientation == 1) {
            DisplayMetrics var1 = this.mResources.getDisplayMetrics();
            float var2 = var1.density;
            if (var2 % 1.0F != 0.0F) {
                float var3 = (float)var1.widthPixels;
                if (this.isContains((int)(var3 / var2))) {
                    int var4 = this.mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_seekbar_width);
                    if (var3 < (float)(this.mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_dialog_padding_left) * 2 + var4)) {
                        int var5 = (int)((var3 - (float)var4) / 2.0F);
                        int var6 = this.mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_dialog_padding_top);
                        var4 = this.mResources.getDimensionPixelSize(R.dimen.sesl_color_picker_dialog_padding_bottom);
                        ((LinearLayout)this.findViewById(R.id.sesl_color_picker_main_content_container)).setPadding(var5, var6, var5, var4);
                    }
                }
            }
        }

    }

    private void initOpacitySeekBar() {
        this.mOpacitySeekBar = (SeslOpacitySeekBar)this.findViewById(R.id.sesl_color_picker_opacity_seekbar);
        this.mOpacitySeekBarContainer = (FrameLayout)this.findViewById(R.id.sesl_color_picker_opacity_seekbar_container);
        if (!this.mIsOpacityBarEnabled) {
            this.mOpacitySeekBar.setVisibility(View.GONE);
            this.mOpacitySeekBarContainer.setVisibility(View.GONE);
        }

        this.mOpacitySeekBar.init(this.mPickedColor.getColor());
        this.mOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar var1, int var2, boolean var3) {
                if (var3) {
                    SeslColorPicker.this.mIsInputFromUser = true;
                }

                SeslColorPicker.this.mPickedColor.setAlpha(var2);
                Integer var4 = SeslColorPicker.this.mPickedColor.getColor();
                if (var4 != null) {
                    if (SeslColorPicker.this.mSelectedColorBackground != null) {
                        SeslColorPicker.this.mSelectedColorBackground.setColor(var4);
                    }

                    if (SeslColorPicker.this.mOnColorChangedListener != null) {
                        SeslColorPicker.this.mOnColorChangedListener.onColorChanged(var4);
                    }
                }

            }

            public void onStartTrackingTouch(SeekBar var1) {
            }

            public void onStopTrackingTouch(SeekBar var1) {
            }
        });
        this.mOpacitySeekBar.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View var1, MotionEvent var2) {
                int var3 = var2.getAction();
                if (var3 != 0) {
                    if (var3 != 1 && var3 != 3) {
                    }

                    return false;
                } else {
                    return true;
                }
            }
        });
        FrameLayout var1 = this.mOpacitySeekBarContainer;
        StringBuilder var2 = new StringBuilder();
        var2.append(this.mResources.getString(R.string.pen_string_opacity));
        var2.append(", ");
        var2.append(this.mResources.getString(R.string.pen_string_slider));
        var2.append(", ");
        var2.append(this.mResources.getString(R.string.sesl_color_picker_double_tap_to_select));
        var1.setContentDescription(var2.toString());
    }

    private void initRecentColorLayout() {
        this.mRecentColorListLayout = (LinearLayout)this.findViewById(R.id.sesl_color_picker_used_color_item_list_layout);
        this.mRecentlyText = (TextView)this.findViewById(R.id.sesl_color_picker_used_color_divider_text);
        this.mRecentlyDivider = this.findViewById(R.id.sesl_color_picker_recently_divider);
        this.mColorDescription = new String[]{this.mResources.getString(R.string.sesl_color_picker_color_one), this.mResources.getString(R.string.sesl_color_picker_color_two), this.mResources.getString(R.string.sesl_color_picker_color_three), this.mResources.getString(R.string.sesl_color_picker_color_four), this.mResources.getString(R.string.sesl_color_picker_color_five), this.mResources.getString(R.string.sesl_color_picker_color_six)};
        Context var1 = this.mContext;
        int var2;
        int var3 = ContextCompat.getColor(var1, R.color.sesl_color_picker_used_color_item_empty_slot_color);

        for(var2 = 0; var2 < 6; ++var2) {
            View var4 = this.mRecentColorListLayout.getChildAt(var2);
            this.setImageColor(var4, var3);
            var4.setFocusable(false);
            var4.setClickable(false);
        }

        if (this.mCurrentFontScale > 1.2F) {
            var2 = this.mResources.getDimensionPixelOffset(R.dimen.sesl_color_picker_selected_color_text_size);
            this.mRecentlyText.setTextSize(0, (float)Math.floor(Math.ceil((double)((float)var2 / this.mCurrentFontScale)) * 1.2000000476837158D));
        }

        var1 = this.mContext;

        var2 = ContextCompat.getColor(var1, R.color.sesl_color_picker_used_color_text_color);
        this.mRecentlyText.setTextColor(var2);
        this.mRecentlyDivider.getBackground().setTint(var2);
    }

    private boolean isContains(int var1) {
        int[] var2 = this.mSmallestWidthDp;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            if (var2[var4] == var1) {
                return true;
            }
        }

        return false;
    }

    private void mapColorOnColorWheel(int var1) {
        this.mPickedColor.setColor(var1);
        SeslColorSwatchView var2 = this.mColorSwatchView;
        if (var2 != null) {
            var2.updateCursorPosition(var1);
        }

        SeslOpacitySeekBar var3 = this.mOpacitySeekBar;
        if (var3 != null) {
            var3.restoreColor(var1);
        }

        GradientDrawable var4 = this.mSelectedColorBackground;
        if (var4 != null) {
            var4.setColor(var1);
            this.setCurrentColorViewDescription(var1, 1);
        }

    }

    private void setCurrentColorViewDescription(int var1, int var2) {
        StringBuilder var3 = new StringBuilder();
        StringBuilder var4 = this.mColorSwatchView.getColorSwatchDescriptionAt(var1);
        if (var4 != null) {
            var3.append(", ");
            var3.append(var4);
        }

        if (var2 != 0) {
            if (var2 == 1) {
                var3.insert(0, this.mResources.getString(R.string.pen_string_color_new));
                this.mPickedColorContainer.setContentDescription(var3);
            }
        } else {
            var3.insert(0, this.mResources.getString(R.string.pen_string_color_current));
            this.mCurrentColorContainer.setContentDescription(var3);
        }

    }

    private void setImageColor(View var1, Integer var2) {
        Context var3 = this.mContext;
        int var4;
        GradientDrawable var5 = (GradientDrawable)var3.getDrawable(R.drawable.sesl_color_picker_used_color_item_slot);
        if (var2 != null) {
            var5.setColor(var2);
        }

        var4 = Color.argb(61, 0, 0, 0);
        var1.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{var4}), var5, (Drawable)null));
        var1.setOnClickListener(this.mImageButtonClickListener);
    }

    private void setInitialColors() {
        Integer var1 = this.mPickedColor.getColor();
        if (var1 != null) {
            this.mapColorOnColorWheel(var1);
        }

    }

    private void updateCurrentColor() {
        Integer var1 = this.mPickedColor.getColor();
        if (var1 != null) {
            SeslOpacitySeekBar var2 = this.mOpacitySeekBar;
            if (var2 != null) {
                var2.changeColorBase(var1);
            }

            GradientDrawable var3 = this.mSelectedColorBackground;
            if (var3 != null) {
                var3.setColor(var1);
                this.setCurrentColorViewDescription(var1, 1);
            }

            SeslColorPicker.OnColorChangedListener var4 = this.mOnColorChangedListener;
            if (var4 != null) {
                var4.onColorChanged(var1);
            }
        }

    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public SeslRecentColorInfo getRecentColorInfo() {
        return this.mRecentColorInfo;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public boolean isUserInputValid() {
        return this.mIsInputFromUser;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void saveSelectedColor() {
        Integer var1 = this.mPickedColor.getColor();
        if (var1 != null) {
            this.mRecentColorInfo.saveSelectedColor(var1);
        }

    }

    public void setOnColorChangedListener(SeslColorPicker.OnColorChangedListener var1) {
        this.mOnColorChangedListener = var1;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void setOpacityBarEnabled(boolean var1) {
        this.mIsOpacityBarEnabled = var1;
        if (this.mIsOpacityBarEnabled) {
            this.mOpacitySeekBar.setVisibility(View.VISIBLE);
            this.mOpacitySeekBarContainer.setVisibility(View.VISIBLE);
        }

    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void updateRecentColorLayout() {
        ArrayList var1 = this.mRecentColorValues;
        int var2;
        if (var1 != null) {
            var2 = var1.size();
        } else {
            var2 = 0;
        }

        StringBuilder var8 = new StringBuilder();
        var8.append(", ");
        var8.append(this.mResources.getString(R.string.sesl_color_picker_option));
        String var3 = var8.toString();

        for(int var4 = 0; var4 < 6; ++var4) {
            View var5 = this.mRecentColorListLayout.getChildAt(var4);
            if (var4 < var2) {
                int var6 = (Integer)this.mRecentColorValues.get(var4);
                this.setImageColor(var5, var6);
                StringBuilder var7 = new StringBuilder();
                var7.append(this.mColorSwatchView.getColorSwatchDescriptionAt(var6));
                var8 = new StringBuilder();
                var8.append(this.mColorDescription[var4]);
                var8.append(var3);
                var8.append(", ");
                var7.insert(0, var8.toString());
                var5.setContentDescription(var7);
                var5.setFocusable(true);
                var5.setClickable(true);
            }
        }

        if (this.mRecentColorInfo.getCurrentColor() != null) {
            var2 = this.mRecentColorInfo.getCurrentColor();
            this.mCurrentColorBackground.setColor(var2);
            this.setCurrentColorViewDescription(var2, 0);
            this.mSelectedColorBackground.setColor(var2);
            this.mapColorOnColorWheel(var2);
        } else if (var2 != 0) {
            var2 = (Integer)this.mRecentColorValues.get(0);
            this.mCurrentColorBackground.setColor(var2);
            this.setCurrentColorViewDescription(var2, 0);
            this.mSelectedColorBackground.setColor(var2);
            this.mapColorOnColorWheel(var2);
        }

        if (this.mRecentColorInfo.getNewColor() != null) {
            var2 = this.mRecentColorInfo.getNewColor();
            this.mSelectedColorBackground.setColor(var2);
            this.mapColorOnColorWheel(var2);
        }

    }

    public interface OnColorChangedListener {
        void onColorChanged(int var1);
    }

    private static class PickedColor {
        private int mAlpha = 255;
        private Integer mColor = null;
        private float[] mHsv = new float[3];

        public PickedColor() {
        }

        public int getAlpha() {
            return this.mAlpha;
        }

        public Integer getColor() {
            return this.mColor;
        }

        public float getV() {
            return this.mHsv[2];
        }

        public void setAlpha(int var1) {
            this.mAlpha = var1;
            this.mColor = Color.HSVToColor(this.mAlpha, this.mHsv);
        }

        public void setColor(int var1) {
            this.mColor = var1;
            this.mAlpha = Color.alpha(this.mColor);
            Color.colorToHSV(this.mColor, this.mHsv);
        }

        public void setHS(float var1, float var2) {
            float[] var3 = this.mHsv;
            var3[0] = var1;
            var3[1] = var2;
            var3[2] = 1.0F;
            this.mColor = Color.HSVToColor(this.mAlpha, var3);
        }

        public void setV(float var1) {
            float[] var2 = this.mHsv;
            var2[2] = var1;
            this.mColor = Color.HSVToColor(this.mAlpha, var2);
        }
    }
}
