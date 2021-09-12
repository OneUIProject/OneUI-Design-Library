package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ViewStubCompat;
import androidx.coordinatorlayout.widget.ViewGroupUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.CollapsingTextHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class SamsungCollapsingToolbarLayout extends FrameLayout {
    static final Interpolator SINE_OUT_80_INTERPOLATOR = new PathInterpolator(0.17f, 0.17f, 0.2f, 1.0f);
    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;
    private final Rect mTmpRect = new Rect();
    /*final*/ CollapsingTextHelper mCollapsingTextHelper;
    int mCurrentOffset;
    WindowInsetsCompat mLastInsets;
    Drawable mStatusBarScrim;
    private boolean mCollapsingTitleEnabled;
    private LinearLayout mCollapsingTitleLayout;
    private LinearLayout mCollapsingTitleLayoutParent;
    private TextView mCollapsingToolbarExtendedSubTitle;
    private TextView mCollapsingToolbarExtendedTitle;
    private boolean mCollapsingToolbarLayoutSubTitleEnabled;
    private boolean mCollapsingToolbarLayoutTitleEnabled;
    private Drawable mContentScrim;
    private float mDefaultHeightDp;
    private boolean mDrawCollapsingTitle;
    private View mDummyView;
    private int mExpandedMarginBottom;
    private int mExpandedMarginEnd;
    private int mExpandedMarginStart;
    private int mExpandedMarginTop;
    private int mExtendSubTitleAppearance;
    private int mExtendTitleAppearance;
    private float mHeightPercent = 0.0f;
    private boolean mIsCollapsingToolbarTitleCustom;
    private ViewStubCompat mViewStubCompat;
    private SamsungAppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
    private boolean mRefreshToolbar = true;
    private int mScrimAlpha;
    private long mScrimAnimationDuration;
    private ValueAnimator mScrimAnimator;
    private int mScrimVisibleHeightTrigger = -1;
    private boolean mScrimsAreShown;
    private int mStatsusBarHeight = 0;
    private Toolbar mToolbar;
    private View mToolbarDirectChild;
    private int mToolbarId;

    public SamsungCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public SamsungCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public SamsungCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SamsungCollapsingToolbarLayout, defStyleAttr, 0);

        mCollapsingTitleLayout = new LinearLayout(context, attrs, defStyleAttr);
        mCollapsingTitleLayout.setBackgroundColor(0);
        mCollapsingTitleLayoutParent = new LinearLayout(context, attrs, defStyleAttr);
        mCollapsingTitleLayoutParent.setBackgroundColor(0);

        mCollapsingTitleEnabled = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_titleEnabled, false);
        mCollapsingToolbarLayoutTitleEnabled = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_extendTitleEnabled, true);
        if (mCollapsingTitleEnabled == mCollapsingToolbarLayoutTitleEnabled && mCollapsingTitleEnabled) {
            mCollapsingTitleEnabled = !mCollapsingToolbarLayoutTitleEnabled;
        }
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper = new CollapsingTextHelper(this);
            mCollapsingTextHelper.setTextSizeInterpolator(SINE_OUT_80_INTERPOLATOR);
            mCollapsingTextHelper.setExpandedTextGravity(a.getInt(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleGravity, GravityCompat.START | Gravity.BOTTOM));
            mCollapsingTextHelper.setCollapsedTextGravity(a.getInt(R.styleable.SamsungCollapsingToolbarLayout_collapsedTitleGravity, GravityCompat.START | Gravity.CENTER_VERTICAL));
        } else {
            mCollapsingTextHelper = null;
        }

        mExtendTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_extendTitleTextAppearance, 0);
        mExtendSubTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_extendSubTitleTextAppearance, 0);
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mExtendTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance, 0);
        }
        CharSequence subtitle = a.getText(R.styleable.SamsungCollapsingToolbarLayout_subtitle);
        if (!mCollapsingToolbarLayoutTitleEnabled || TextUtils.isEmpty(subtitle)) {
            mCollapsingToolbarLayoutSubTitleEnabled = false;
        } else {
            mCollapsingToolbarLayoutSubTitleEnabled = true;
        }

        if (mCollapsingTitleLayoutParent != null) {
            addView(mCollapsingTitleLayoutParent, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
        if (mCollapsingTitleLayout != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 16.0f);
            params.gravity = 16;
            mCollapsingTitleLayout.setOrientation(LinearLayout.VERTICAL);
            mStatsusBarHeight = getStatusbarHeight();
            if (mStatsusBarHeight > 0) {
                mCollapsingTitleLayout.setPadding(0, 0, 0, mStatsusBarHeight / 2);
            }
            mCollapsingTitleLayoutParent.addView(mCollapsingTitleLayout, params);
        }
        if (mCollapsingToolbarLayoutTitleEnabled) {
            mCollapsingToolbarExtendedTitle = new TextView(context);
            if (Build.VERSION.SDK_INT >= 29) {
                mCollapsingToolbarExtendedTitle.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL);
            }
            mCollapsingTitleLayout.addView(mCollapsingToolbarExtendedTitle);
            mCollapsingToolbarExtendedTitle.setEllipsize(TextUtils.TruncateAt.END);
            mCollapsingToolbarExtendedTitle.setGravity(Gravity.CENTER);
            mCollapsingToolbarExtendedTitle.setTextAppearance(mExtendTitleAppearance);
            int extendedTitlePadding = (int) getResources().getDimension(R.dimen.sesl_appbar_extended_title_padding);
            mCollapsingToolbarExtendedTitle.setPadding(extendedTitlePadding, 0, extendedTitlePadding, 0);
        }
        if (mCollapsingToolbarLayoutSubTitleEnabled) {
            setSubtitle(subtitle);
        }
        updateDefaultHeightDP();
        updateTitleLayout();

        int dimensionPixelSize = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMargin, 0);
        mExpandedMarginBottom = mExpandedMarginEnd = mExpandedMarginTop = mExpandedMarginStart = dimensionPixelSize;
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginStart)) {
            mExpandedMarginStart = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginStart, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            mExpandedMarginEnd = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginEnd, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginBottom, 0);
        }

        setTitle(a.getText(R.styleable.SamsungCollapsingToolbarLayout_title));

        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setExpandedTextAppearance(R.style.CollapsingToolbarExpandTitleStyle);
            mCollapsingTextHelper.setCollapsedTextAppearance(R.style.CollapsedActionBarTitleTextStyle);
            if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance)) {
                mCollapsingTextHelper.setExpandedTextAppearance(a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance, 0));
            }
        }

        mScrimVisibleHeightTrigger = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);
        mScrimAnimationDuration = a.getInt(R.styleable.SamsungCollapsingToolbarLayout_scrimAnimationDuration, DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a.getDrawable(R.styleable.SamsungCollapsingToolbarLayout_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.SamsungCollapsingToolbarLayout_statusBarScrim));

        mToolbarId = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_toolbarId, -1);

        a.recycle();

        a = getContext().obtainStyledAttributes(R.styleable.AppCompatTheme);
        if (!a.getBoolean(R.styleable.AppCompatTheme_windowActionModeOverlay, false)) {
            LayoutInflater.from(context).inflate(R.layout.sesl_material_action_mode_view_stub, (ViewGroup) this, true);
            mViewStubCompat = (ViewStubCompat) findViewById(R.id.action_mode_bar_stub);
        }
        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                return onWindowInsetChanged(insets);
            }
        });
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getHeight();
    }

    static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);

        if (mCollapsingToolbarLayoutTitleEnabled) {
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (layoutParams != null) {
                mIsCollapsingToolbarTitleCustom = layoutParams.seslIsTitleCustom();
                if (mIsCollapsingToolbarTitleCustom) {
                    if (mCollapsingToolbarExtendedTitle != null && mCollapsingToolbarExtendedTitle.getParent() == mCollapsingTitleLayout) {
                        mCollapsingTitleLayout.removeView(mCollapsingToolbarExtendedTitle);
                    }
                    if (mCollapsingToolbarExtendedSubTitle != null && mCollapsingToolbarExtendedSubTitle.getParent() == mCollapsingTitleLayout) {
                        mCollapsingTitleLayout.removeView(mCollapsingToolbarExtendedSubTitle);
                    }
                    if (child.getParent() != null) {
                        ((ViewGroup) child.getParent()).removeView(child);
                    }
                    mCollapsingTitleLayout.addView(child, params);
                }
            }
        }
    }

    private void updateTitleLayout() {
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.sesl_appbar_height_proportion, typedValue, true);
        mHeightPercent = typedValue.getFloat();
        if (mCollapsingToolbarLayoutTitleEnabled) {
            TypedArray appearance = getContext().obtainStyledAttributes(mExtendTitleAppearance, R.styleable.TextAppearance);
            float textSize = TypedValue.complexToFloat(appearance.peekValue(R.styleable.TextAppearance_android_textSize).data);
            float fontScale = getContext().getResources().getConfiguration().fontScale;
            if (fontScale > 1.1f) {
                fontScale = 1.1f;
            }
            Log.d("Sesl_CTL", "updateTitleLayout: context:" + getContext() + ", orientation:" + getContext().getResources().getConfiguration().orientation + " density:" + getContext().getResources().getConfiguration().densityDpi + " ,testSize : " + textSize + "fontScale : " + fontScale + ", mCollapsingToolbarLayoutSubTitleEnabled :" + mCollapsingToolbarLayoutSubTitleEnabled);
            if (!mCollapsingToolbarLayoutSubTitleEnabled) {
                mCollapsingToolbarExtendedTitle.setTextSize(1, textSize * fontScale);
            } else {
                mCollapsingToolbarExtendedTitle.setTextSize(0, (float) getContext().getResources().getDimensionPixelSize(R.dimen.sesl_appbar_extended_title_text_size_with_subtitle));
                mCollapsingToolbarExtendedSubTitle.setTextSize(0, (float) getContext().getResources().getDimensionPixelSize(R.dimen.sesl_toolbar_subtitle_text_size));
            }
            if (mHeightPercent != 0.3f) {
                mCollapsingToolbarExtendedTitle.setSingleLine(false);
                mCollapsingToolbarExtendedTitle.setMaxLines(2);
            } else if (mCollapsingToolbarLayoutSubTitleEnabled) {
                mCollapsingToolbarExtendedTitle.setSingleLine(true);
                mCollapsingToolbarExtendedTitle.setMaxLines(1);
            } else {
                mCollapsingToolbarExtendedTitle.setSingleLine(false);
                mCollapsingToolbarExtendedTitle.setMaxLines(2);
            }
            appearance.recycle();
        }
    }

    private void updateDefaultHeightDP() {
        if (getParent() instanceof SamsungAppBarLayout) {
            SamsungAppBarLayout abl = (SamsungAppBarLayout) getParent();
            if (abl.getPaddingBottom() > 0) {
                mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
            } else {
                mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_default_height);
            }
        } else {
            mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewParent parent = getParent();
        if (parent instanceof SamsungAppBarLayout) {
            setFitsSystemWindows(ViewCompat.getFitsSystemWindows((View) parent));

            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((SamsungAppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);

            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof SamsungAppBarLayout) {
            ((SamsungAppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets;
        }

        if (!ObjectsCompat.equals(mLastInsets, newInsets)) {
            mLastInsets = newInsets;
            requestLayout();
        }

        return insets.consumeSystemWindowInsets();
    }

    @Override
    @SuppressLint("RestrictedApi")
    public void draw(Canvas canvas) {
        super.draw(canvas);

        ensureToolbar();
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper.draw(canvas);
        }

        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(), topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean invalidated = false;
        if (mContentScrim != null && mScrimAlpha > 0 && isToolbarChild(child)) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mContentScrim != null) {
            mContentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!mRefreshToolbar) {
            return;
        }

        mToolbar = null;
        mToolbarDirectChild = null;

        if (mToolbarId != -1) {
            mToolbar = findViewById(mToolbarId);
            if (mToolbar != null) {
                mToolbarDirectChild = findDirectChild(mToolbar);
            }
        }

        if (mToolbar == null) {
            Toolbar toolbar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof Toolbar) {
                    toolbar = (Toolbar) child;
                    break;
                }
            }
            mToolbar = toolbar;

            if (mViewStubCompat != null) {
                mViewStubCompat.bringToFront();
                mViewStubCompat.invalidate();
            }
        }

        updateDummyView();
        mRefreshToolbar = false;
    }

    private boolean isToolbarChild(View child) {
        return (mToolbarDirectChild == null || mToolbarDirectChild == this) ? child == mToolbar : child == mToolbarDirectChild;
    }

    private View findDirectChild(final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    private void updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            final ViewParent parent = mDummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mDummyView);
            }
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null) {
                mDummyView = new View(getContext());
            }
            if (mDummyView.getParent() == null) {
                mToolbar.addView(mDummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = MeasureSpec.getMode(heightMeasureSpec);
        final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (mLastInsets != null) {
            final int insetTop = mLastInsets.getSystemWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.getTop() < insetTop) {
                        ViewCompat.offsetTopAndBottom(child, insetTop);
                    }
                }
            }
        }

        if (mCollapsingTitleEnabled && mDummyView != null) {
            mDrawCollapsingTitle = ViewCompat.isAttachedToWindow(mDummyView) && mDummyView.getVisibility() == VISIBLE;

            if (mDrawCollapsingTitle) {
                final boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

                final int maxOffset = getMaxOffsetForPinChild(mToolbarDirectChild != null ? mToolbarDirectChild : mToolbar);
                ViewGroupUtils.getDescendantRect(this, mDummyView, mTmpRect);
                mCollapsingTextHelper.setCollapsedBounds(mTmpRect.left + (isRtl ? mToolbar.getTitleMarginEnd() : mToolbar.getTitleMarginStart()), mTmpRect.top + maxOffset + mToolbar.getTitleMarginTop(), mTmpRect.right + (isRtl ? mToolbar.getTitleMarginStart() : mToolbar.getTitleMarginEnd()), mTmpRect.bottom + maxOffset - mToolbar.getTitleMarginBottom());

                mCollapsingTextHelper.setExpandedBounds(isRtl ? mExpandedMarginEnd : mExpandedMarginStart, mTmpRect.top + mExpandedMarginTop, right - left - (isRtl ? mExpandedMarginStart : mExpandedMarginEnd), bottom - top - mExpandedMarginBottom);
                mCollapsingTextHelper.recalculate();
            }
        }

        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        if (mToolbar != null) {
            final int toolbar_height;

            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.getText())) {
                mCollapsingTextHelper.setText(mToolbar.getTitle());
            }
            if (mToolbarDirectChild == null || mToolbarDirectChild == this) {
                toolbar_height = getHeightWithMargins(mToolbar);
            } else {
                toolbar_height = getHeightWithMargins(mToolbarDirectChild);
            }
            if (getMinimumHeight() != toolbar_height) {
                post(new Runnable() {
                    public void run() {
                        setMinimumHeight(toolbar_height);
                    }
                });
            }
        }

        updateScrimVisibility();
    }

    private int getStatusbarHeight() {
        int identifier = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return getResources().getDimensionPixelOffset(identifier);
        }
        return 0;
    }

    @Nullable
    @SuppressLint("RestrictedApi")
    public CharSequence getTitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getText() : mCollapsingToolbarExtendedTitle.getText();
    }

    @SuppressLint("RestrictedApi")
    public void setTitle(@Nullable CharSequence title) {
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setText(title);
        } else if (mCollapsingToolbarExtendedTitle != null) {
            mCollapsingToolbarExtendedTitle.setText(title);
        }
        updateTitleLayout();
    }

    public boolean isTitleEnabled() {
        return mCollapsingToolbarLayoutTitleEnabled;
    }

    public void setTitleEnabled(boolean enabled) {
        if (!enabled) {
            mCollapsingToolbarLayoutTitleEnabled = false;
            mCollapsingTitleEnabled = false;
        } else if (mCollapsingToolbarExtendedTitle != null) {
            mCollapsingToolbarLayoutTitleEnabled = true;
            mCollapsingTitleEnabled = false;
        } else if (mCollapsingTextHelper != null) {
            mCollapsingTitleEnabled = true;
            mCollapsingToolbarLayoutTitleEnabled = false;
        } else {
            mCollapsingToolbarLayoutTitleEnabled = false;
            mCollapsingTitleEnabled = false;
        }
        if (!enabled && !mCollapsingToolbarLayoutTitleEnabled && mCollapsingToolbarExtendedTitle != null) {
            mCollapsingToolbarExtendedTitle.setVisibility(View.INVISIBLE);
        }
        if (enabled && mCollapsingTitleEnabled) {
            updateDummyView();
            requestLayout();
        }
    }

    public void seslSetCustomTitleView(View v, LayoutParams lp) {
        mIsCollapsingToolbarTitleCustom = lp.seslIsTitleCustom();

        if (mIsCollapsingToolbarTitleCustom) {
            if (mCollapsingToolbarExtendedTitle != null && mCollapsingToolbarExtendedTitle.getParent() == mCollapsingTitleLayout) {
                mCollapsingTitleLayout.removeView(mCollapsingToolbarExtendedTitle);
            }
            if (mCollapsingToolbarExtendedSubTitle != null && mCollapsingToolbarExtendedSubTitle.getParent() == mCollapsingTitleLayout) {
                mCollapsingTitleLayout.removeView(mCollapsingToolbarExtendedSubTitle);
            }
            mCollapsingTitleLayout.addView(v, lp);
        } else
            super.addView(v, lp);
    }

    public void setSubtitle(int resId) {
        setSubtitle(getContext().getText(resId));
    }

    public void setSubtitle(CharSequence subtitle) {
        if (!mCollapsingToolbarLayoutTitleEnabled || TextUtils.isEmpty(subtitle)) {
            mCollapsingToolbarLayoutSubTitleEnabled = false;
            if (mCollapsingToolbarExtendedSubTitle != null) {
                ((ViewGroup) mCollapsingToolbarExtendedSubTitle.getParent()).removeView(mCollapsingToolbarExtendedSubTitle);
                mCollapsingToolbarExtendedSubTitle = null;
            }
        } else {
            mCollapsingToolbarLayoutSubTitleEnabled = true;
            if (mCollapsingToolbarExtendedSubTitle == null) {
                mCollapsingToolbarExtendedSubTitle = new TextView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mCollapsingToolbarExtendedSubTitle.setText(subtitle);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                mCollapsingTitleLayout.addView(mCollapsingToolbarExtendedSubTitle, params);
                mCollapsingToolbarExtendedSubTitle.setSingleLine(false);
                mCollapsingToolbarExtendedSubTitle.setMaxLines(1);
                mCollapsingToolbarExtendedSubTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                mCollapsingToolbarExtendedSubTitle.setTextAppearance(mExtendSubTitleAppearance);
            } else {
                mCollapsingToolbarExtendedSubTitle.setText(subtitle);
            }
            if (mCollapsingToolbarExtendedTitle != null) {
                mCollapsingToolbarExtendedTitle.setTextSize(0, (float) getContext().getResources().getDimensionPixelSize(R.dimen.sesl_appbar_extended_title_text_size_with_subtitle));
            }
        }
        requestLayout();
        updateTitleLayout();
    }

    public CharSequence getSubTitle() {
        if (mCollapsingToolbarExtendedSubTitle != null) {
            return mCollapsingToolbarExtendedSubTitle.getText();
        } else {
            return null;
        }
    }

    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    public void setScrimsShown(boolean shown, boolean animate) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            mScrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (mScrimAnimator == null) {
            mScrimAnimator = new ValueAnimator();
            mScrimAnimator.setDuration(mScrimAnimationDuration);
            mScrimAnimator.setInterpolator(targetAlpha > mScrimAlpha ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    setScrimAlpha((int) animator.getAnimatedValue());
                }
            });
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    int getScrimAlpha() {
        return mScrimAlpha;
    }

    void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
            final Drawable contentScrim = mContentScrim;
            if (contentScrim != null && mToolbar != null) {
                ViewCompat.postInvalidateOnAnimation(mToolbar);
            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(SamsungCollapsingToolbarLayout.this);
        }
    }

    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));

    }

    @Nullable
    public Drawable getContentScrim() {
        return mContentScrim;
    }

    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null) {
                mContentScrim.setCallback(null);
            }
            mContentScrim = drawable != null ? drawable.mutate() : null;
            if (mContentScrim != null) {
                mContentScrim.setBounds(0, 0, getWidth(), getHeight());
                mContentScrim.setCallback(this);
                mContentScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    @SuppressLint("RestrictedApi")
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = mStatusBarScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        d = mContentScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        if (mCollapsingTextHelper != null) {
            changed |= mCollapsingTextHelper.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mContentScrim || who == mStatusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (mStatusBarScrim != null && mStatusBarScrim.isVisible() != visible) {
            mStatusBarScrim.setVisible(visible, false);
        }
        if (mContentScrim != null && mContentScrim.isVisible() != visible) {
            mContentScrim.setVisible(visible, false);
        }
    }

    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    @Nullable
    public Drawable getStatusBarScrim() {
        return mStatusBarScrim;
    }

    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null) {
                mStatusBarScrim.setCallback(null);
            }
            mStatusBarScrim = drawable != null ? drawable.mutate() : null;
            if (mStatusBarScrim != null) {
                if (mStatusBarScrim.isStateful()) {
                    mStatusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(mStatusBarScrim, ViewCompat.getLayoutDirection(this));
                mStatusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                mStatusBarScrim.setCallback(this);
                mStatusBarScrim.setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setCollapsedTextAppearance(resId);
        }
    }

    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setCollapsedTextColor(colors);
        }
    }

    @SuppressLint("RestrictedApi")
    public int getCollapsedTitleGravity() {
        if (mCollapsingTitleEnabled) {
            return mCollapsingTextHelper.getCollapsedTextGravity();
        } else {
            return -1;
        }
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleGravity(int gravity) {
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setCollapsedTextGravity(gravity);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            mCollapsingToolbarExtendedTitle.setTextAppearance(resId);
        } else if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setExpandedTextAppearance(resId);
        }
    }

    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            mCollapsingToolbarExtendedTitle.setTextColor(colors);
        } else if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setExpandedTextColor(colors);
        }
    }

    @SuppressLint("RestrictedApi")
    public int getExpandedTitleGravity() {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            return mCollapsingToolbarExtendedTitle.getGravity();
        }
        if (mCollapsingTitleEnabled) {
            return mCollapsingTextHelper.getExpandedTextGravity();
        }
        return -1;
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleGravity(int gravity) {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            mCollapsingToolbarExtendedTitle.setGravity(gravity);
        } else if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setExpandedTextGravity(gravity);
        }
    }

    @SuppressLint("RestrictedApi")
    public Typeface getCollapsedTitleTypeface() {
        if (mCollapsingTitleEnabled) {
            return mCollapsingTextHelper.getCollapsedTypeface();
        } else {
            return null;
        }
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setCollapsedTypeface(typeface);
        }
    }

    @SuppressLint("RestrictedApi")
    public Typeface getExpandedTitleTypeface() {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            return mCollapsingToolbarExtendedTitle.getTypeface();
        }
        if (mCollapsingTitleEnabled) {
            return mCollapsingTextHelper.getExpandedTypeface();
        }
        return null;
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        if (mCollapsingToolbarLayoutTitleEnabled) {
            mCollapsingToolbarExtendedTitle.setTypeface(typeface);
        } else if (mCollapsingTitleEnabled) {
            mCollapsingTextHelper.setExpandedTypeface(typeface);
        }
    }

    public int getExpandedTitleMarginStart() {
        return mExpandedMarginStart;
    }

    public void setExpandedTitleMarginStart(int margin) {
        mExpandedMarginStart = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginTop() {
        return mExpandedMarginTop;
    }

    public void setExpandedTitleMarginTop(int margin) {
        mExpandedMarginTop = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginEnd() {
        return mExpandedMarginEnd;
    }

    public void setExpandedTitleMarginEnd(int margin) {
        mExpandedMarginEnd = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginBottom() {
        return mExpandedMarginBottom;
    }

    public void setExpandedTitleMarginBottom(int margin) {
        mExpandedMarginBottom = margin;
        requestLayout();
    }

    public int getScrimVisibleHeightTrigger() {
        if (mScrimVisibleHeightTrigger >= 0) {
            return mScrimVisibleHeightTrigger;
        }

        final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;

        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0) {
            return Math.min((minHeight * 2) + insetTop, getHeight());
        }

        return getHeight() / 3;
    }

    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (mScrimVisibleHeightTrigger != height) {
            mScrimVisibleHeightTrigger = height;
            updateScrimVisibility();
        }
    }

    public long getScrimAnimationDuration() {
        return mScrimAnimationDuration;
    }

    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        mScrimAnimationDuration = duration;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.sesl_appbar_height_proportion, typedValue, true);
        mHeightPercent = typedValue.getFloat();
        updateDefaultHeightDP();
        updateTitleLayout();
    }

    final void updateScrimVisibility() {
        if (mContentScrim != null || mStatusBarScrim != null) {
            setScrimsShown(getHeight() + mCurrentOffset < getScrimVisibleHeightTrigger());
        }
    }

    final int getMaxOffsetForPinChild(View child) {
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;
        @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {}
        public static final int COLLAPSE_MODE_OFF = 0;
        public static final int COLLAPSE_MODE_PIN = 1;
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        int collapseMode = COLLAPSE_MODE_OFF;
        float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;
        private boolean isTitleCustom;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SamsungCollapsingToolbarLayout_Layout);
            collapseMode = a.getInt(R.styleable.SamsungCollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF);
            setParallaxMultiplier(a.getFloat(R.styleable.SamsungCollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier, DEFAULT_PARALLAX_MULTIPLIER));
            isTitleCustom = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_Layout_layout_isTitleCustom, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            super(source);
        }

        public void setCollapseMode(@CollapseMode int collapseMode) {
            this.collapseMode = collapseMode;
        }

        @CollapseMode
        public int getCollapseMode() {
            return collapseMode;
        }

        public void setParallaxMultiplier(float multiplier) {
            parallaxMult = multiplier;
        }

        public float getParallaxMultiplier() {
            return parallaxMult;
        }

        public boolean seslIsTitleCustom() {
            return isTitleCustom;
        }

        public void seslSetIsTitleCustom(boolean z) {
            isTitleCustom = z;
        }
    }

    private class OffsetUpdateListener implements SamsungAppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
            if (getParent() instanceof SamsungAppBarLayout) {
                SamsungAppBarLayout abl = (SamsungAppBarLayout) getParent();
                if (abl.getPaddingBottom() > 0) {
                    mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
                } else {
                    mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_default_height);
                }
            } else {
                mDefaultHeightDp = (float) getResources().getDimensionPixelSize(R.dimen.sesl_action_bar_height_with_padding);
            }
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onOffsetChanged(SamsungAppBarLayout layout, int verticalOffset) {
            layout.getWindowVisibleDisplayFrame(new Rect());
            int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;

            mCurrentOffset = verticalOffset;
            mCollapsingTitleLayout.setTranslationY((float) ((-mCurrentOffset) / 3));

            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.collapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(Math.round(((float) (-verticalOffset)) * lp.parallaxMult));
                        break;
                }
            }

            updateScrimVisibility();

            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(SamsungCollapsingToolbarLayout.this);
            }

            if (mCollapsingToolbarLayoutTitleEnabled) {
                int layoutPosition = Math.abs(layout.getTop());
                float alphaRange = ((float) getHeight()) * 0.17999999f;

                float titleAlpha = 255.0f - ((100.0f / alphaRange) * (((float) layoutPosition) - 0.0f));

                if (titleAlpha < 0.0f) {
                    titleAlpha = 0.0f;
                } else if (titleAlpha > 255.0f) {
                    titleAlpha = 255.0f;
                }

                mCollapsingTitleLayout.setAlpha(titleAlpha / 255.0f);

                if (layout.getHeight() <= ((int) mDefaultHeightDp)) {
                    mCollapsingTitleLayout.setAlpha(0.0f);
                }

                // moved inside ToolbarLayout.java

            } else if (mCollapsingTitleEnabled) {
                int expandRange = (getHeight() - getMinimumHeight()) - insetTop;
                mCollapsingTextHelper.setExpansionFraction(((float) Math.abs(verticalOffset)) / ((float) expandRange));
            }
        }
    }
}
