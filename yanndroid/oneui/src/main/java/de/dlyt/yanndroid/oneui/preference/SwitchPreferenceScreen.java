package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.reflect.view.SeslViewReflector;
import androidx.reflect.widget.SeslHoverPopupWindowReflector;

import de.dlyt.yanndroid.oneui.R;

public class SwitchPreferenceScreen extends SwitchPreference {
    private View.OnKeyListener mSwitchKeyListener;

    public SwitchPreferenceScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mSwitchKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean handled = false;
                switch (event.getKeyCode()) {
                    case 21:
                        if (isChecked()) {
                            if (callChangeListener(false)) {
                                setChecked(false);
                            }
                            handled = true;
                        }
                        break;
                    case 22:
                        if (!isChecked()) {
                            if (callChangeListener(true)) {
                                setChecked(true);
                            }
                            handled = true;
                        }
                        break;
                }
                return handled;
            }
        };

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);
        String fragment = a.getString(R.styleable.Preference_android_fragment);
        if (fragment == null || fragment.equals("")) {
            Log.w("SwitchPreferenceScreen", "SwitchPreferenceScreen should get fragment property. Fragment property does not exsit in SwitchPreferenceScreen");
        }
        Configuration conf = context.getResources().getConfiguration();
        if ((conf.screenWidthDp > 320 || conf.fontScale < 1.1F) && (conf.screenWidthDp >= 411 || conf.fontScale < 1.3F)) {
            setLayoutResource(R.layout.sesl_switch_preference_screen);
        } else {
            setLayoutResource(R.layout.sesl_switch_preference_screen_large);
        }
        setWidgetLayoutResource(R.layout.sesl_switch_preference_screen_widget_divider);
        a.recycle();
    }

    public SwitchPreferenceScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreferenceScreen(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceStyle);
    }

    @Override
    protected void onClick() { }

    @Override
    protected void callClickListener() { }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setOnKeyListener(this.mSwitchKeyListener);
        TextView textView = (TextView) holder.findViewById(16908310);
        View findViewById = holder.findViewById(16908352);
        if (!(textView == null || findViewById == null)) {
            SeslViewReflector.semSetHoverPopupType(findViewById, SeslHoverPopupWindowReflector.getField_TYPE_NONE());
            findViewById.setContentDescription(textView.getText().toString());
        }
        if (findViewById != null) {
            Configuration configuration = getContext().getResources().getConfiguration();
            ViewGroup.LayoutParams layoutParams = findViewById.getLayoutParams();
            if ((configuration.screenWidthDp > 320 || configuration.fontScale < 1.1f) && (configuration.screenWidthDp >= 411 || configuration.fontScale < 1.3f)) {
                layoutParams.height = -1;
            } else {
                layoutParams.height = -2;
            }
            findViewById.setLayoutParams(layoutParams);
        }
    }
}
