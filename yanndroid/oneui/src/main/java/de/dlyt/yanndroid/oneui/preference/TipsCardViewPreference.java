package de.dlyt.yanndroid.oneui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.Tooltip;

public class TipsCardViewPreference extends Preference {
    private Context mContext;
    private TipsCardListener mTipsCardListener;
    private int mTextColor;

    public TipsCardViewPreference(Context context) {
        this(context, null);
    }

    public TipsCardViewPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TipsCardViewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setSelectable(false);
        setLayoutResource(R.layout.oui_tips_card_view_preference);
        mTextColor = ContextCompat.getColor(context, R.color.tips_card_view_item_color);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);

        ((TextView) preferenceViewHolder.itemView.findViewById(android.R.id.title)).setTextColor(mTextColor);
        ((TextView) preferenceViewHolder.itemView.findViewById(android.R.id.summary)).setTextColor(mTextColor);

        Tooltip.setTooltipText(preferenceViewHolder.itemView.findViewById(R.id.tips_cancel_button), mContext.getString(R.string.sesl_cancel));
        preferenceViewHolder.itemView.findViewById(R.id.tips_cancel_button).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                mTipsCardListener.onCancelClicked(view);
            }
        });
    }

    public void setTipsCardListener(TipsCardListener listener) {
        mTipsCardListener = listener;
    }

    public interface TipsCardListener {
        void onCancelClicked(View view);
    }

}