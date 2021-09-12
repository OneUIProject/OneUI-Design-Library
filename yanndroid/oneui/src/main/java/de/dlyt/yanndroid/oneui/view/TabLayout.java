package de.dlyt.yanndroid.oneui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.tabs.SamsungBaseTabLayout;

public class TabLayout extends SamsungBaseTabLayout {

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.tabLayoutStyle);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDepthStyle = 2;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateWidget();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (int tabPosition = 0; tabPosition < getTabCount(); tabPosition++) {
            ViewGroup tabView = (ViewGroup) getTabView(tabPosition);
            if (tabView != null) {
                tabView.setEnabled(enabled);
                tabView.setAlpha(enabled ? 1.0f : 0.4f);
            }
        }
    }

    public void updateWidget() {
        Float[] tabCount = new Float[getTabCount()];
        float f = 0.0f;
        for (int i = 0; i < getTabCount(); i++) {
            TabLayout.Tab tab = getTabAt(i);
            if (tab != null) {
                tabCount[i] = getTabTextWidth(tab.seslGetTextView());
                f += tabCount[i];
            }
        }
        float tabLayoutPadding = (float) getResources().getDimensionPixelSize(R.dimen.tab_layout_padding);
        ((MarginLayoutParams) getLayoutParams()).setMargins((int) tabLayoutPadding, 0, (int) tabLayoutPadding, 0);
        setViewDimens(tabCount, f);
        post(new Runnable() {
            public final void run() {
                setSelectedTabScrollPosition();
            }
        });
    }

    private View getTabView(int position) {
        ViewGroup viewGroup = getTabViewGroup();
        if (viewGroup == null || viewGroup.getChildCount() <= position) {
            return null;
        }
        return viewGroup.getChildAt(position);
    }

    private ViewGroup getTabViewGroup() {
        if (getChildCount() <= 0) {
            return null;
        }
        View view = getChildAt(0);
        if (view == null || !(view instanceof ViewGroup)) {
            return null;
        }
        return (ViewGroup) view;
    }

    private void setSelectedTabScrollPosition() {
        setScrollPosition(getSelectedTabPosition(), 0.0f, true);
    }

    private float getTabTextWidth(TextView textView) {
        return textView.getPaint().measureText(textView.getText().toString());
    }

    // kang from com.samsung.android.messaging
    private void setViewDimens(Float[] fArr, float f) {
        int i;
        int tabCount = getTabCount();
        if (tabCount > 0) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.tablayout_start_end_margin);
            i = getDisplayWidth(getContext());
            int i2 = i - (dimensionPixelSize * 2);
            float dimensionPixelSize2 = (float) getResources().getDimensionPixelSize(R.dimen.tablayout_text_padding);
            float f2 = (float) i2;
            float f3 = f2 / ((float) tabCount);
            float f4 = dimensionPixelSize2 * 2.0f;
            float f5 = (0.75f * f2) - f4;
            float f6 = 0.0f;
            int i3 = 0;
            int i4 = 0;
            for (int i5 = 0; i5 < tabCount; i5++) {
                Log.d("TabLayout", "i : " + i5 + ", width : " + fArr[i5]);
                if (f5 < fArr[i5].floatValue()) {
                    i3 = (int) (((float) i3) + (fArr[i5].floatValue() - f5));
                    fArr[i5] = Float.valueOf(f5);
                    i4++;
                    f6 = f5;
                } else if (f6 < fArr[i5].floatValue()) {
                    f6 = fArr[i5].floatValue();
                }
            }
            float f7 = f - ((float) i3);
            setTabMode(0);
            Log.d("TabLayout", "[MODE_SCROLLABLE]");
            Log.d("TabLayout", "availableContentWidth : " + i2 + ", tabTextPaddingLeftRight : " + dimensionPixelSize2);
            ViewGroup viewGroup = (ViewGroup) getChildAt(0);
            int i6 = (tabCount - i4) * 2;
            int i7 = i6 > 0 ? ((int) ((f2 - f7) - ((((float) i4) * dimensionPixelSize2) * 2.0f))) / i6 : 0;
            int i8 = (int) dimensionPixelSize2;
            int i9 = -1;
            boolean z = true;
            if (i7 < i8) {
                i7 = i8;
            } else {
                float f8 = f6 + dimensionPixelSize2 + dimensionPixelSize2;
                if (f3 >= f8) {
                    setTabMode(1);
                    for (int i10 = 0; i10 < tabCount; i10++) {
                        ((ViewGroup) viewGroup.getChildAt(i10)).getChildAt(0).getLayoutParams().width = -1;
                        getTabAt(i10).seslGetTextView().setMaxWidth(i2);
                        getTabAt(i10).seslGetTextView().setMinimumWidth(0);
                        getTabAt(i10).seslGetTextView().setPadding(0, 0, 0, 0);
                    }
                    Log.d("TabLayout", "[MODE_FIXED] TabCount : " + tabCount + ", minNeededTabWidth : " + f3 + ", maxTabWidth : " + f8);
                    return;
                }
            }
            int i11 = 0;
            while (i11 < tabCount) {
                boolean z2 = fArr[i11].floatValue() >= f5 ? z : false;
                int floatValue = (int) (fArr[i11].floatValue() + ((z2 ? dimensionPixelSize2 : (float) i7) * 2.0f));
                ViewGroup.LayoutParams layoutParams = viewGroup.getChildAt(i11).getLayoutParams();
                layoutParams.width = floatValue;
                layoutParams.height = i9;
                viewGroup.getChildAt(i11).setMinimumWidth(floatValue);
                int i12 = z2 ? 0 : (int) (((float) i7) - dimensionPixelSize2);
                getTabAt(i11).seslGetTextView().setMaxWidth((int) f5);
                getTabAt(i11).seslGetTextView().setMinimumWidth(floatValue - ((int) f4));
                getTabAt(i11).seslGetTextView().setPadding(i12, 0, i12, 0);
                Log.d("TabLayout", "params.width : " + layoutParams.width + ", tabWidthList[" + i11 + "] : " + fArr[i11] + ", LeftRightPadding : " + (i7 * 2));
                i11++;
                i9 = -1;
                z = true;
            }
            requestLayout();
        }
    }

    private int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}