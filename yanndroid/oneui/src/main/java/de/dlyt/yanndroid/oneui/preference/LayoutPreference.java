package de.dlyt.yanndroid.oneui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.res.TypedArrayUtils;

import de.dlyt.yanndroid.oneui.R;

public class LayoutPreference extends Preference {
    private final View.OnClickListener mClickListener;
    View mRootView;
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private int mDescendantFocusability;
    private boolean mIsRelatedCardView;

    public LayoutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mClickListener = new View.OnClickListener() {
            public final void onClick(View view) {
                performClick(view);
            }
        };
        mIsRelatedCardView = false;
        mDescendantFocusability = -1;

        init(context, attrs, 0);
    }

    public LayoutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mClickListener = new View.OnClickListener() {
            public final void onClick(View view) {
                performClick(view);
            }
        };
        mIsRelatedCardView = false;
        mDescendantFocusability = -1;

        init(context, attrs, defStyleAttr);
    }

    public LayoutPreference(Context context, int resource) {
        this(context, LayoutInflater.from(context).inflate(resource, null, false));
    }

    public LayoutPreference(Context context, View view, boolean isRelatedCardView) {
        super(context, null);

        mClickListener = new View.OnClickListener() {
            public final void onClick(View view) {
                performClick(view);
            }
        };
        mIsRelatedCardView = isRelatedCardView;
        mDescendantFocusability = -1;

        setView(view);
    }

    public LayoutPreference(Context context, View view, int descendantFocusability) {
        super(context, null);

        mClickListener = new View.OnClickListener() {
            public final void onClick(View view) {
                performClick(view);
            }
        };
        mIsRelatedCardView = false;
        mDescendantFocusability = -1;

        setView(view);
        mDescendantFocusability = descendantFocusability;
    }

    public LayoutPreference(Context context, View view) {
        super(context, null);

        mClickListener = new View.OnClickListener() {
            public final void onClick(View view) {
                performClick(view);
            }
        };

        mIsRelatedCardView = false;
        mDescendantFocusability = -1;
        setView(view);
    }

    @SuppressLint("RestrictedApi")
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Preference);
        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove, R.styleable.Preference_allowDividerAbove, false);
        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow, R.styleable.Preference_allowDividerBelow, false);
        a.recycle();

        a = context.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, 0);
        int layoutResource = a.getResourceId(R.styleable.Preference_android_layout, 0);

        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }

        a.recycle();
        setView(LayoutInflater.from(getContext()).inflate(layoutResource, null, false));
    }

    private void setView(View view) {
        setLayoutResource(R.layout.oui_layout_preference);

        ViewGroup allDetails = view.findViewById(R.id.all_details);
        if (allDetails != null) {
            forceCustomPadding(allDetails, true);
        }

        mRootView = view;
        setShouldDisableView(false);
    }

    private void forceCustomPadding(View view, boolean additive) {
        Resources res = view.getResources();
        int paddingSide = res.getDimensionPixelSize(R.dimen.sesl_preference_item_padding_vertical);
        view.setPaddingRelative((additive ? view.getPaddingStart() : 0) + paddingSide, 0, (additive ? view.getPaddingEnd() : 0) + paddingSide, res.getDimensionPixelSize(R.dimen.layout_preference_padding_bottom));
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (mIsRelatedCardView) {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setFocusable(false);
            holder.itemView.setClickable(false);
        } else {
            holder.itemView.setOnClickListener(mClickListener);
            boolean selectable = isSelectable();
            holder.itemView.setFocusable(selectable);
            holder.itemView.setClickable(selectable);
            holder.setDividerAllowedAbove(mAllowDividerAbove);
            holder.setDividerAllowedBelow(mAllowDividerBelow);
        }

        FrameLayout layout = (FrameLayout) holder.itemView;
        layout.removeAllViews();

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        layout.addView(mRootView);
        if (mDescendantFocusability != -1) {
            layout.setDescendantFocusability(mDescendantFocusability);
        }
    }

    public <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

    public void setDescendantFocusability(int descendantFocusability) {
        mDescendantFocusability = descendantFocusability;
    }

    public boolean isRelatedCardView() {
        return mIsRelatedCardView;
    }
}
