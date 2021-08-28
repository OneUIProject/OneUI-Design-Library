package de.dlyt.yanndroid.oneui.preference.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.SamsungPreferenceFragment;

public class PreferencesRelatedCard extends LinearLayout {
    private static final String TAG = "PreferencesRelatedCard";
    private Context mContext;
    private SamsungPreferenceFragment mTargetFragment;

    private View mParentView;
    private TextView mCardTitleText;
    private LinearLayout mCardContainer;


    public PreferencesRelatedCard(Context context) {
        this(context, null, 0);
        mContext = context;
        init();
    }

    public PreferencesRelatedCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferencesRelatedCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mParentView = LayoutInflater.from(mContext).inflate(R.layout.samsung_related_card_preference, this);
        mParentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mCardTitleText = mParentView.findViewById(R.id.related_card_title);
        mCardContainer = mParentView.findViewById(R.id.related_card_container);
    }

    public void setTitleText(int resid) {
        mCardTitleText.setText(resid);
    }

    public void setTitleText(CharSequence text) {
        mCardTitleText.setText(text);
    }

    public PreferencesRelatedCard addButton(CharSequence titleText, View.OnClickListener ocl) {
        TextView textView = new TextView(new ContextThemeWrapper(mContext, R.style.RelatedButtonStyle));
        textView.setFocusable(true);
        textView.setBackgroundResource(R.drawable.related_button_ripple_background);
        textView.setText(titleText);
        textView.setOnClickListener(ocl);
        mCardContainer.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return this;
    }

    public void resetCardData() {
        mCardContainer.removeAllViews();
    }

    public View getRelatedCardView() {
        return mParentView;
    }

    public boolean isValid() {
        if (mCardContainer == null || mCardContainer.getChildCount() > 0) {
            return true;
        }
        Log.d("RelativeLinkView", "The current screen doesn't have any content.");
        return false;
    }

    public void show(Object obj) {
        if (mCardContainer != null && mCardContainer.getChildCount() <= 0) {
            Log.d(TAG, "The current screen doesn't have any content.");
        } else {
            if (obj instanceof SamsungPreferenceFragment) {
                SamsungPreferenceFragment fragment = (SamsungPreferenceFragment) obj;
                fragment.setFooterView(mParentView, true);
                mTargetFragment = fragment;
                return;
            }
            Log.d(TAG, "Failed to attach RelatedCard. " + obj.getClass());
        }
    }

    public void setTargetFragment(Object obj) {
        if (obj instanceof SamsungPreferenceFragment) {
            mTargetFragment = (SamsungPreferenceFragment) obj;
        }
    }
}
