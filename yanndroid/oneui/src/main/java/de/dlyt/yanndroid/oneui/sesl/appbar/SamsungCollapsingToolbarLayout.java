package de.dlyt.yanndroid.oneui.sesl.appbar;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.ViewStubCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class SamsungCollapsingToolbarLayout extends FrameLayout {
    private boolean mIsOneUI4;
    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;
    private static final int DEF_STYLE_RES = R.style.OneUI4_CollapsingToolbarLayoutStyle;
    private static final float LAND_HEIGHT_PERCENT = 0.3f;
    private static final float MAX_FONT_SCALE = 1.0f;
    protected static final String TAG = "Sesl_CTL";
    public static final int TITLE_COLLAPSE_MODE_FADE = 1;
    public static final int TITLE_COLLAPSE_MODE_SCALE = 0;
    @IntDef(value = {TITLE_COLLAPSE_MODE_SCALE, TITLE_COLLAPSE_MODE_FADE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TitleCollapseMode {}
    final CollapsingTextHelper collapsingTextHelper;
    private boolean collapsingTitleEnabled;
    @Nullable private Drawable contentScrim;
    int currentOffset;
    private boolean drawCollapsingTitle;
    private View dummyView;
    @NonNull final ElevationOverlayProvider elevationOverlayProvider;
    private int expandedMarginBottom;
    private int expandedMarginEnd;
    private int expandedMarginStart;
    private int expandedMarginTop;
    @Nullable WindowInsetsCompat lastInsets;
    private Context mContext;
    private View mCustomSubTitleView = null;
    private float mDefaultHeight;
    private int mExtendSubTitleAppearance;
    private int mExtendTitleAppearance;
    private TextView mExtendedSubTitle;
    private TextView mExtendedTitle;
    private boolean mFadeToolbarTitle = true;
    private float mHeightProportion;
    private boolean mIsCollapsingToolbarTitleCustom;
    private boolean mIsCustomAccessibility = false;
    private boolean mSubTitleEnabled;
    private boolean mTitleEnabled;
    private LinearLayout mTitleLayout;
    private LinearLayout mTitleLayoutParent;
    private ViewStubCompat mViewStubCompat;
    private SamsungAppBarLayout.OnOffsetChangedListener onOffsetChangedListener;
    private boolean refreshToolbar = true;
    private int scrimAlpha;
    private long scrimAnimationDuration;
    private ValueAnimator scrimAnimator;
    private int scrimVisibleHeightTrigger = -1;
    private boolean scrimsAreShown;
    @Nullable Drawable statusBarScrim;
    @TitleCollapseMode private int titleCollapseMode;
    private final Rect tmpRect = new Rect();
    private ViewGroup toolbar;
    @Nullable private View toolbarDirectChild;
    @Nullable private int toolbarId;

    public SamsungCollapsingToolbarLayout(@NonNull Context context) {
        this(context, null);
    }

    public SamsungCollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.collapsingToolbarLayoutStyle);
    }

    @SuppressLint("RestrictedApi")
    public SamsungCollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);

        mContext = getContext();
        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        TypedArray a = ThemeEnforcement.obtainStyledAttributes(context, attrs, R.styleable.SamsungCollapsingToolbarLayout, defStyleAttr, DEF_STYLE_RES);

        collapsingTitleEnabled = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_titleEnabled, false);
        mTitleEnabled = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_extendedTitleEnabled, true);
        if (collapsingTitleEnabled == mTitleEnabled && collapsingTitleEnabled) {
            collapsingTitleEnabled = false;
        }

        if (collapsingTitleEnabled) {
            collapsingTextHelper = new CollapsingTextHelper(this);
            collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
            collapsingTextHelper.setRtlTextDirectionHeuristicsEnabled(false);

            collapsingTextHelper.setExpandedTextGravity(a.getInt(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleGravity, GravityCompat.START | Gravity.BOTTOM));
            collapsingTextHelper.setCollapsedTextGravity(a.getInt(R.styleable.SamsungCollapsingToolbarLayout_collapsedTitleGravity, GravityCompat.START | Gravity.CENTER_VERTICAL));

            expandedMarginStart = expandedMarginTop = expandedMarginEnd = expandedMarginBottom = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMargin, 0);
        } else {
            collapsingTextHelper = null;
        }

        elevationOverlayProvider = new ElevationOverlayProvider(context);

        mExtendTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_extendedTitleTextAppearance, 0);
        mExtendSubTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_extendedSubtitleTextAppearance, 0);
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            mExtendTitleAppearance = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance, 0);
        }
        mSubTitleEnabled = mTitleEnabled && !TextUtils.isEmpty(a.getText(R.styleable.SamsungCollapsingToolbarLayout_subtitle));

        mTitleLayoutParent = new LinearLayout(context, attrs, defStyleAttr);
        //mTitleLayoutParent.setId(R.id.collapsing_appbar_title_layout_parent);
        mTitleLayoutParent.setBackgroundColor(0);
        if (mTitleLayoutParent != null) {
            addView(mTitleLayoutParent, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }

        mTitleLayout = new LinearLayout(context, attrs, defStyleAttr);
        //mTitleLayout.setId(R.id.collapsing_appbar_title_layout);
        mTitleLayout.setBackgroundColor(0);
        if (mTitleLayout != null) {
            mTitleLayout.setOrientation(LinearLayout.VERTICAL);
            final int statusBarHeight = getStatusbarHeight();
            if (statusBarHeight > 0) {
                mTitleLayout.setPadding(0, 0, 0, statusBarHeight / 2);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 16.0f);
            lp.gravity = Gravity.CENTER_VERTICAL;
            mTitleLayoutParent.addView(mTitleLayout, lp);
        }

        if (mTitleEnabled) {
            mExtendedTitle = new TextView(context);
            //mExtendedTitle.setId(R.id.collapsing_appbar_extended_title);
            if (VERSION.SDK_INT >= 29) {
                mExtendedTitle.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL);
            }
            mTitleLayout.addView(mExtendedTitle);

            mExtendedTitle.setEllipsize(TextUtils.TruncateAt.END);
            mExtendedTitle.setGravity(Gravity.CENTER);
            mExtendedTitle.setTextAppearance(getContext(), mExtendTitleAppearance);

            int padding = (int) getResources().getDimension(R.dimen.sesl_appbar_extended_title_padding);
            mExtendedTitle.setPadding(padding, 0, padding, 0);
        }
        if (mSubTitleEnabled) {
            seslSetSubtitle(a.getText(R.styleable.SamsungCollapsingToolbarLayout_subtitle));
        }
        updateDefaultHeight();
        updateTitleLayout();

        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginStart)) {
            expandedMarginStart = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginStart, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            expandedMarginEnd = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginEnd, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginTop)) {
            expandedMarginTop = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            expandedMarginBottom = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleMarginBottom, 0);
        }

        setTitle(a.getText(R.styleable.SamsungCollapsingToolbarLayout_title));

        if (collapsingTitleEnabled) {
            collapsingTextHelper.setExpandedTextAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
            collapsingTextHelper.setCollapsedTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
            if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance)) {
                collapsingTextHelper.setExpandedTextAppearance(a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_expandedTitleTextAppearance, 0));
            }
            if (a.hasValue(R.styleable.SamsungCollapsingToolbarLayout_collapsedTitleTextAppearance)) {
                collapsingTextHelper.setCollapsedTextAppearance(a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_collapsedTitleTextAppearance, 0));
            }
        }

        scrimVisibleHeightTrigger = a.getDimensionPixelSize(R.styleable.SamsungCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

        if (a.hasValue(R.styleable.CollapsingToolbarLayout_maxLines)) {
            collapsingTextHelper.setMaxLines(a.getInt(R.styleable.SamsungCollapsingToolbarLayout_maxLines, 1));
        }

        scrimAnimationDuration = a.getInt(R.styleable.SamsungCollapsingToolbarLayout_scrimAnimationDuration, DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a.getDrawable(R.styleable.SamsungCollapsingToolbarLayout_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.SamsungCollapsingToolbarLayout_statusBarScrim));

        toolbarId = a.getResourceId(R.styleable.SamsungCollapsingToolbarLayout_toolbarId, -1);

        a.recycle();

        TypedArray a2 = getContext().obtainStyledAttributes(R.styleable.AppCompatTheme);
        if (!a2.getBoolean(R.styleable.AppCompatTheme_windowActionModeOverlay, false)) {
            LayoutInflater.from(context).inflate(R.layout.sesl_material_action_mode_view_stub, this, true);
            mViewStubCompat = (ViewStubCompat) findViewById(R.id.action_mode_bar_stub);
        }
        a2.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this, new androidx.core.view.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, @NonNull WindowInsetsCompat insets) {
                return onWindowInsetChanged(insets);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewParent parent = getParent();
        if (parent instanceof SamsungAppBarLayout) {
            SamsungAppBarLayout appBarLayout = (SamsungAppBarLayout) parent;

            disableLiftOnScrollIfNeeded(appBarLayout);

            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows(appBarLayout));

            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = new OffsetUpdateListener();
            }
            appBarLayout.addOnOffsetChangedListener(onOffsetChangedListener);

            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        final ViewParent parent = getParent();
        if (onOffsetChangedListener != null && parent instanceof SamsungAppBarLayout) {
            ((SamsungAppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    WindowInsetsCompat onWindowInsetChanged(@NonNull final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            newInsets = insets;
        }

        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets;
            requestLayout();
        }

        return insets.consumeSystemWindowInsets();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        ensureToolbar();
        if (toolbar == null && contentScrim != null && scrimAlpha > 0) {
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
        }

        if (collapsingTitleEnabled && drawCollapsingTitle) {
            if (toolbar != null && contentScrim != null && scrimAlpha > 0 && isTitleCollapseFadeMode() && collapsingTextHelper.getExpansionFraction() < collapsingTextHelper.getFadeModeThresholdFraction()) {
                int save = canvas.save();
                canvas.clipRect(contentScrim.getBounds(), Op.DIFFERENCE);
                collapsingTextHelper.draw(canvas);
                canvas.restoreToCount(save);
            } else {
                collapsingTextHelper.draw(canvas);
            }
        }

        if (statusBarScrim != null && scrimAlpha > 0) {
            final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
                statusBarScrim.mutate().setAlpha(scrimAlpha);
                statusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mHeightProportion = ResourcesCompat.getFloat(getResources(), mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion);
        updateDefaultHeight();
        updateTitleLayout();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean invalidated = false;
        if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
            updateContentScrimBounds(contentScrim, child, getWidth(), getHeight());
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (contentScrim != null) {
            updateContentScrimBounds(contentScrim, w, h);
        }
    }

    private boolean isTitleCollapseFadeMode() {
        return titleCollapseMode == TITLE_COLLAPSE_MODE_FADE;
    }

    private void disableLiftOnScrollIfNeeded(SamsungAppBarLayout appBarLayout) {
        if (isTitleCollapseFadeMode()) {
            appBarLayout.setLiftOnScroll(false);
        }
    }

    private void updateContentScrimBounds(@NonNull Drawable contentScrim, int width, int height) {
        updateContentScrimBounds(contentScrim, this.toolbar, width, height);
    }

    private void updateContentScrimBounds(@NonNull Drawable contentScrim, @Nullable View toolbar, int width, int height) {
        int bottom = isTitleCollapseFadeMode() && toolbar != null && collapsingTitleEnabled ? toolbar.getBottom() : height;
        contentScrim.setBounds(0, 0, width, bottom);
    }

    private void ensureToolbar() {
        if (!refreshToolbar) {
            return;
        }

        this.toolbar = null;
        toolbarDirectChild = null;

        if (toolbarId != -1) {
            this.toolbar = findViewById(toolbarId);
            if (this.toolbar != null) {
                toolbarDirectChild = findDirectChild(this.toolbar);
            }
        }

        if (this.toolbar == null) {
            ViewGroup toolbar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (isToolbar(child)) {
                    toolbar = (ViewGroup) child;
                    break;
                }
            }
            this.toolbar = toolbar;

            if (mViewStubCompat != null) {
                mViewStubCompat.bringToFront();
                mViewStubCompat.invalidate();
            }
        }

        updateDummyView();
        refreshToolbar = false;
    }

    private static boolean isToolbar(View view) {
        return view instanceof androidx.appcompat.widget.Toolbar || (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && view instanceof android.widget.Toolbar);
    }

    private boolean isToolbarChild(View child) {
        return (toolbarDirectChild == null || toolbarDirectChild == this) ? child == toolbar : child == toolbarDirectChild;
    }

    @NonNull
    private View findDirectChild(@NonNull final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    private void updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            final ViewParent parent = dummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(dummyView);
            }
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = new View(getContext());
            }
            if (dummyView.getParent() == null) {
                toolbar.addView(dummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = MeasureSpec.getMode(heightMeasureSpec);
        final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            int newHeight = getMeasuredHeight() + topInset;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        if (toolbar != null) {
            if (toolbarDirectChild == null || toolbarDirectChild == this) {
                setMinimumHeight(getHeightWithMargins(toolbar));
            } else {
                setMinimumHeight(getHeightWithMargins(toolbarDirectChild));
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (lastInsets != null) {
            final int insetTop = lastInsets.getSystemWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.getTop() < insetTop) {
                        ViewCompat.offsetTopAndBottom(child, insetTop);
                    }
                }
            }
        }

        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        if (collapsingTitleEnabled && dummyView != null) {
            drawCollapsingTitle = ViewCompat.isAttachedToWindow(dummyView) && dummyView.getVisibility() == VISIBLE;

            if (drawCollapsingTitle) {
                final boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
                updateCollapsedBounds(isRtl);
                collapsingTextHelper.setExpandedBounds(isRtl ? expandedMarginEnd : expandedMarginStart, tmpRect.top + expandedMarginTop, right - left - (isRtl ? expandedMarginStart : expandedMarginEnd), bottom - top - expandedMarginBottom);
                collapsingTextHelper.recalculate();
            }
        }

        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.getText())) {
                setTitle(getToolbarTitle(toolbar));
            }
        }

        updateScrimVisibility();

        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).applyOffsets();
        }
    }

    @SuppressLint("RestrictedApi")
    private void updateCollapsedBounds(boolean isRtl) {
        final int maxOffset = getMaxOffsetForPinChild(toolbarDirectChild != null ? toolbarDirectChild : toolbar);
        DescendantOffsetUtils.getDescendantRect(this, dummyView, tmpRect);
        final int titleMarginStart;
        final int titleMarginEnd;
        final int titleMarginTop;
        final int titleMarginBottom;
        if (toolbar instanceof androidx.appcompat.widget.Toolbar) {
            androidx.appcompat.widget.Toolbar compatToolbar = (androidx.appcompat.widget.Toolbar) toolbar;
            titleMarginStart = compatToolbar.getTitleMarginStart();
            titleMarginEnd = compatToolbar.getTitleMarginEnd();
            titleMarginTop = compatToolbar.getTitleMarginTop();
            titleMarginBottom = compatToolbar.getTitleMarginBottom();
        } else if (VERSION.SDK_INT >= VERSION_CODES.N && toolbar instanceof android.widget.Toolbar) {
            android.widget.Toolbar frameworkToolbar = (android.widget.Toolbar) toolbar;
            titleMarginStart = frameworkToolbar.getTitleMarginStart();
            titleMarginEnd = frameworkToolbar.getTitleMarginEnd();
            titleMarginTop = frameworkToolbar.getTitleMarginTop();
            titleMarginBottom = frameworkToolbar.getTitleMarginBottom();
        } else {
            titleMarginStart = 0;
            titleMarginEnd = 0;
            titleMarginTop = 0;
            titleMarginBottom = 0;
        }
        collapsingTextHelper.setCollapsedBounds(tmpRect.left + (isRtl ? titleMarginEnd : titleMarginStart), tmpRect.top + maxOffset + titleMarginTop, tmpRect.right - (isRtl ? titleMarginStart : titleMarginEnd), tmpRect.bottom + maxOffset - titleMarginBottom);
    }

    private static CharSequence getToolbarTitle(View view) {
        if (view instanceof androidx.appcompat.widget.Toolbar) {
            return ((androidx.appcompat.widget.Toolbar) view).getTitle();
        } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && view instanceof android.widget.Toolbar) {
            return ((android.widget.Toolbar) view).getTitle();
        } else {
            return null;
        }
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getMeasuredHeight();
    }

    @NonNull
    static ViewOffsetHelper getViewOffsetHelper(@NonNull View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    @SuppressLint("RestrictedApi")
    public void setTitle(@Nullable CharSequence title) {
        if (collapsingTitleEnabled) {
            collapsingTextHelper.setText(title);
            updateContentDescriptionFromTitle();
        } else {
            if (mExtendedTitle != null) {
                mExtendedTitle.setText(title);
            }
        }
        updateTitleLayout();
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    public CharSequence getTitle() {
        return collapsingTitleEnabled ? collapsingTextHelper.getText() : mExtendedTitle.getText();
    }

    @SuppressLint("RestrictedApi")
    public void setTitleCollapseMode(@TitleCollapseMode int titleCollapseMode) {
        this.titleCollapseMode = titleCollapseMode;

        boolean fadeModeEnabled = isTitleCollapseFadeMode();
        collapsingTextHelper.setFadeModeEnabled(fadeModeEnabled);

        ViewParent parent = getParent();
        if (parent instanceof SamsungAppBarLayout) {
            disableLiftOnScrollIfNeeded((SamsungAppBarLayout) parent);
        }

        if (fadeModeEnabled && contentScrim == null) {
            float appBarElevation = getResources().getDimension(R.dimen.sesl_appbar_elevation);
            int scrimColor = elevationOverlayProvider.compositeOverlayWithThemeSurfaceColorIfNeeded(appBarElevation);
            setContentScrimColor(scrimColor);
        }
    }

    @TitleCollapseMode
    public int getTitleCollapseMode() {
        return titleCollapseMode;
    }

    public void setTitleEnabled(boolean enabled) {
        if (enabled) {
            if (mExtendedTitle != null) {
                mTitleEnabled = true;
                mTitleEnabled = false;
            } else if (collapsingTextHelper != null) {
                mTitleEnabled = true;
                mTitleEnabled = false;
            } else {
                mTitleEnabled = false;
                mTitleEnabled = false;
            }
        } else {
            mTitleEnabled = false;
            collapsingTitleEnabled = false;
        }

        if (!enabled && !mTitleEnabled) {
            if (mExtendedTitle != null) {
                mExtendedTitle.setVisibility(INVISIBLE);
            }
        }

        if (enabled && collapsingTitleEnabled) {
            updateDummyView();
            requestLayout();
        }
    }

    public boolean isTitleEnabled() {
        return mTitleEnabled;
    }

    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    public void setScrimsShown(boolean shown, boolean animate) {
        if (scrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            scrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (scrimAnimator == null) {
            scrimAnimator = new ValueAnimator();
            scrimAnimator.setDuration(scrimAnimationDuration);
            scrimAnimator.setInterpolator(targetAlpha > scrimAlpha ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            scrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                    setScrimAlpha((int) animator.getAnimatedValue());
                }
            });
        } else if (scrimAnimator.isRunning()) {
            scrimAnimator.cancel();
        }

        scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
        scrimAnimator.start();
    }

    void setScrimAlpha(int alpha) {
        if (alpha != scrimAlpha) {
            final Drawable contentScrim = this.contentScrim;
            if (contentScrim != null && toolbar != null) {
                ViewCompat.postInvalidateOnAnimation(toolbar);
            }
            scrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(SamsungCollapsingToolbarLayout.this);
        }
    }

    int getScrimAlpha() {
        return scrimAlpha;
    }

    public void setContentScrim(@Nullable Drawable drawable) {
        if (contentScrim != drawable) {
            if (contentScrim != null) {
                contentScrim.setCallback(null);
            }
            contentScrim = drawable != null ? drawable.mutate() : null;
            if (contentScrim != null) {
                updateContentScrimBounds(contentScrim, getWidth(), getHeight());
                contentScrim.setCallback(this);
                contentScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
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
        return contentScrim;
    }

    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (statusBarScrim != drawable) {
            if (statusBarScrim != null) {
                statusBarScrim.setCallback(null);
            }
            statusBarScrim = drawable != null ? drawable.mutate() : null;
            if (statusBarScrim != null) {
                if (statusBarScrim.isStateful()) {
                    statusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
                statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                statusBarScrim.setCallback(this);
                statusBarScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = statusBarScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        d = contentScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        if (collapsingTextHelper != null) {
            changed |= collapsingTextHelper.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
            statusBarScrim.setVisible(visible, false);
        }
        if (contentScrim != null && contentScrim.isVisible() != visible) {
            contentScrim.setVisible(visible, false);
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
        return statusBarScrim;
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        if (collapsingTitleEnabled) {
            collapsingTextHelper.setCollapsedTextAppearance(resId);
        }
    }

    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        if (collapsingTitleEnabled) {
            collapsingTextHelper.setCollapsedTextColor(colors);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setCollapsedTitleGravity(int gravity) {
        if (collapsingTitleEnabled) {
            collapsingTextHelper.setCollapsedTextGravity(gravity);
        }
    }

    @SuppressLint("RestrictedApi")
    public int getCollapsedTitleGravity() {
        return collapsingTitleEnabled ? collapsingTextHelper.getCollapsedTextGravity() : 0;
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        if (mTitleEnabled) {
            mExtendedTitle.setTextAppearance(getContext(), resId);
        } else if (collapsingTitleEnabled) {
            collapsingTextHelper.setExpandedTextAppearance(resId);
        }
    }

    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        if (mTitleEnabled) {
            mExtendedTitle.setTextColor(colors);
        } else if (collapsingTitleEnabled) {
            collapsingTextHelper.setExpandedTextColor(colors);
        }
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleGravity(int gravity) {
        if (mTitleEnabled) {
            mExtendedTitle.setGravity(gravity);
        } else if (collapsingTitleEnabled) {
            collapsingTextHelper.setExpandedTextGravity(gravity);
        }
    }

    @SuppressLint("RestrictedApi")
    public int getExpandedTitleGravity() {
        if (mTitleEnabled) {
            return mExtendedTitle.getGravity();
        }
        if (collapsingTitleEnabled) {
            return collapsingTextHelper.getExpandedTextGravity();
        }
        return 0;
    }

    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        if (collapsingTitleEnabled) {
            collapsingTextHelper.setCollapsedTypeface(typeface);
        }
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return collapsingTitleEnabled ? collapsingTextHelper.getCollapsedTypeface() : Typeface.DEFAULT;
    }

    @SuppressLint("RestrictedApi")
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        if (mTitleEnabled) {
            mExtendedTitle.setTypeface(typeface);
        } else if (collapsingTitleEnabled) {
            collapsingTextHelper.setExpandedTypeface(typeface);
        }
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    public Typeface getExpandedTitleTypeface() {
        if (mTitleEnabled) {
            return mExtendedTitle.getTypeface();
        }
        if (collapsingTitleEnabled) {
            return collapsingTextHelper.getExpandedTypeface();
        }
        return Typeface.DEFAULT;
    }

    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        expandedMarginStart = start;
        expandedMarginTop = top;
        expandedMarginEnd = end;
        expandedMarginBottom = bottom;
        requestLayout();
    }

    public int getExpandedTitleMarginStart() {
        return expandedMarginStart;
    }

    public void setExpandedTitleMarginStart(int margin) {
        expandedMarginStart = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginTop() {
        return expandedMarginTop;
    }

    public void setExpandedTitleMarginTop(int margin) {
        expandedMarginTop = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginEnd() {
        return expandedMarginEnd;
    }

    public void setExpandedTitleMarginEnd(int margin) {
        expandedMarginEnd = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginBottom() {
        return expandedMarginBottom;
    }

    public void setExpandedTitleMarginBottom(int margin) {
        expandedMarginBottom = margin;
        requestLayout();
    }

    @SuppressLint("RestrictedApi")
    public void setMaxLines(int maxLines) {
        collapsingTextHelper.setMaxLines(maxLines);
    }

    @SuppressLint("RestrictedApi")
    public int getMaxLines() {
        return collapsingTextHelper.getMaxLines();
    }

    @SuppressLint("RestrictedApi")
    public int getLineCount() {
        return collapsingTextHelper.getLineCount();
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public void setLineSpacingAdd(float spacingAdd) {
        collapsingTextHelper.setLineSpacingAdd(spacingAdd);
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public float getLineSpacingAdd() {
        return collapsingTextHelper.getLineSpacingAdd();
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public void setLineSpacingMultiplier(@FloatRange(from = 0.0) float spacingMultiplier) {
        collapsingTextHelper.setLineSpacingMultiplier(spacingMultiplier);
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public float getLineSpacingMultiplier() {
        return collapsingTextHelper.getLineSpacingMultiplier();
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public void setHyphenationFrequency(int hyphenationFrequency) {
        collapsingTextHelper.setHyphenationFrequency(hyphenationFrequency);
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(VERSION_CODES.M)
    public int getHyphenationFrequency() {
        return collapsingTextHelper.getHyphenationFrequency();
    }

    @SuppressLint("RestrictedApi")
    public void setRtlTextDirectionHeuristicsEnabled(boolean rtlTextDirectionHeuristicsEnabled) {
        collapsingTextHelper.setRtlTextDirectionHeuristicsEnabled(rtlTextDirectionHeuristicsEnabled);
    }

    @SuppressLint("RestrictedApi")
    public boolean isRtlTextDirectionHeuristicsEnabled() {
        return collapsingTextHelper.isRtlTextDirectionHeuristicsEnabled();
    }

    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (scrimVisibleHeightTrigger != height) {
            scrimVisibleHeightTrigger = height;
            updateScrimVisibility();
        }
    }

    public int getScrimVisibleHeightTrigger() {
        if (scrimVisibleHeightTrigger >= 0) {
            return scrimVisibleHeightTrigger;
        }

        final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0) {
            return Math.min((minHeight * 2) + insetTop, getHeight());
        }

        return getHeight() / 3;
    }

    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        scrimAnimationDuration = duration;
    }

    public long getScrimAnimationDuration() {
        return scrimAnimationDuration;
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

    public static class LayoutParams extends FrameLayout.LayoutParams {
        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;
        @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX})
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {}
        public static final int COLLAPSE_MODE_OFF = 0;
        public static final int COLLAPSE_MODE_PIN = 1;
        public static final int COLLAPSE_MODE_PARALLAX = 2;
        int collapseMode = COLLAPSE_MODE_OFF;
        private boolean isTitleCustom;
        float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SamsungCollapsingToolbarLayout_Layout);
            collapseMode = a.getInt(R.styleable.SamsungCollapsingToolbarLayout_Layout_layout_collapseMode, COLLAPSE_MODE_OFF);
            setParallaxMultiplier(a.getFloat(R.styleable.SamsungCollapsingToolbarLayout_Layout_layout_collapseParallaxMultiplier, DEFAULT_PARALLAX_MULTIPLIER));
            isTitleCustom = a.getBoolean(R.styleable.SamsungCollapsingToolbarLayout_Layout_isCustomTitle, false);
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

        public void seslSetIsTitleCustom(boolean custom) {
            isTitleCustom = custom;
        }

        public boolean seslIsTitleCustom() {
            return isTitleCustom;
        }
    }

    final void updateScrimVisibility() {
        if (contentScrim != null || statusBarScrim != null) {
            setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
        }
    }

    final int getMaxOffsetForPinChild(@NonNull View child) {
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
    }

    private void updateContentDescriptionFromTitle() {
        setContentDescription(getTitle());
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);

        if (mTitleEnabled) {
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (layoutParams != null) {
                mIsCollapsingToolbarTitleCustom = layoutParams.seslIsTitleCustom();
                if (mIsCollapsingToolbarTitleCustom) {
                    if (mExtendedTitle != null && mExtendedTitle.getParent() == mTitleLayout) {
                        mTitleLayout.removeView(mExtendedTitle);
                    }
                    if (mExtendedSubTitle != null && mExtendedSubTitle.getParent() == mTitleLayout) {
                        mTitleLayout.removeView(mExtendedSubTitle);
                    }
                    if (child.getParent() != null) {
                        ((ViewGroup) child.getParent()).removeView(child);
                    }
                    mTitleLayout.addView(child, params);
                }
            }
        }
    }

    public void seslSetCustomTitleView(View v, LayoutParams lp) {
        mIsCollapsingToolbarTitleCustom = lp.seslIsTitleCustom();

        if (mIsCollapsingToolbarTitleCustom) {
            if (mExtendedTitle != null && mExtendedTitle.getParent() == mTitleLayout) {
                mTitleLayout.removeView(mExtendedTitle);
            }
            if (mExtendedSubTitle != null && mExtendedSubTitle.getParent() == mTitleLayout) {
                mTitleLayout.removeView(mExtendedSubTitle);
            }
            mTitleLayout.addView(v, lp);
        } else {
            super.addView(v, lp);
        }
    }

    private void updateTitleLayout() {
        mHeightProportion = ResourcesCompat.getFloat(getResources(), mIsOneUI4 ? R.dimen.sesl4_appbar_height_proportion : R.dimen.sesl_appbar_height_proportion);

        if (this.mTitleEnabled) {
            TypedArray a = getContext().obtainStyledAttributes(mExtendTitleAppearance, R.styleable.TextAppearance);

            TypedValue peekValue = a.peekValue(R.styleable.TextAppearance_android_textSize);
            if (peekValue == null) {
                Log.i(TAG, "ExtendTitleAppearance value is null");
                return;
            }

            float textSize = TypedValue.complexToFloat(peekValue.data);
            float fontScale = getContext().getResources().getConfiguration().fontScale;
            if (mIsOneUI4) {
                fontScale = Math.min(fontScale, 1.0f);
            } else if (fontScale > 1.1f) {
                fontScale = 1.1f;
            }

            Log.i(TAG, "updateTitleLayout : context : " + getContext() + ", textSize : " + textSize + ", fontScale : " + fontScale + ", mSubTitleEnabled : " + mSubTitleEnabled);

            if (!mSubTitleEnabled) {
                mExtendedTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize * fontScale);
            } else {
                mExtendedTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_appbar_extended_title_text_size_with_subtitle : R.dimen.sesl_appbar_extended_title_text_size_with_subtitle));
                if (mExtendedSubTitle != null) {
                    mExtendedSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.sesl_appbar_extended_subtitle_text_size));
                }
            }

            if (mHeightProportion == LAND_HEIGHT_PERCENT) {
                if (mSubTitleEnabled) {
                    mExtendedTitle.setSingleLine(true);
                    mExtendedTitle.setMaxLines(1);
                } else {
                    mExtendedTitle.setSingleLine(false);
                    mExtendedTitle.setMaxLines(2);
                }
            } else {
                mExtendedTitle.setSingleLine(false);
                mExtendedTitle.setMaxLines(2);
            }

            if (mExtendedTitle.getMaxLines() > 1) {
                if (mIsOneUI4) {
                    int statusBarHeight = getStatusbarHeight();
                    if (mSubTitleEnabled && statusBarHeight > 0) {
                        mTitleLayout.setPadding(0, 0, 0, (statusBarHeight / 2) + getResources().getDimensionPixelSize(R.dimen.sesl4_action_bar_top_padding));
                    } else if (statusBarHeight > 0) {
                        mTitleLayout.setPadding(0, 0, 0, statusBarHeight / 2);
                    }
                } else {
                    //mExtendedTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.sesl_appbar_extended_title_text_view_height)));
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(mExtendedTitle, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    TextViewCompat.setAutoSizeTextTypeUniformWithPresetSizes(mExtendedTitle, new int[]{38, 32, 26}, TypedValue.COMPLEX_UNIT_DIP);
                }
            } else {
                mExtendedTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TextViewCompat.setAutoSizeTextTypeWithDefaults(mExtendedTitle, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
                mExtendedTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.sesl4_appbar_extended_title_text_size_with_subtitle));
            }

            a.recycle();
        }
    }

    private void updateDefaultHeight() {
        if (getParent() instanceof SamsungAppBarLayout) {
            SamsungAppBarLayout appBarLayout = (SamsungAppBarLayout) getParent();
            if (appBarLayout.useCollapsedHeight()) {
                mDefaultHeight = appBarLayout.seslGetCollapsedHeight();
            } else {
                mDefaultHeight = getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding);
            }
        } else {
            mDefaultHeight = getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_action_bar_height_with_padding : R.dimen.sesl_action_bar_height_with_padding);
        }
    }

    public void seslSetSubtitle(@StringRes int resId) {
        seslSetSubtitle(getContext().getText(resId));
    }

    public void seslSetSubtitle(@Nullable CharSequence subtitle) {
        if (!mTitleEnabled || TextUtils.isEmpty(subtitle)) {
            mSubTitleEnabled = false;
            if (mExtendedSubTitle != null) {
                ((ViewGroup) mExtendedSubTitle.getParent()).removeView(mExtendedSubTitle);
                mExtendedSubTitle = null;
            }
        } else {
            mSubTitleEnabled = true;
            if (mExtendedSubTitle == null) {
                mExtendedSubTitle = new TextView(getContext());
                //mExtendedSubTitle.setId(R.id.collapsing_appbar_extended_subtitle);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                mExtendedSubTitle.setText(subtitle);
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                mTitleLayout.addView(mExtendedSubTitle, lp);
                mExtendedSubTitle.setSingleLine(false);
                mExtendedSubTitle.setMaxLines(1);
                mExtendedSubTitle.setEllipsize(TextUtils.TruncateAt.END);
                mExtendedSubTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                mExtendedSubTitle.setTextAppearance(getContext(), mExtendSubTitleAppearance);
                mExtendedSubTitle.setPadding(getResources().getDimensionPixelSize(R.dimen.sesl_appbar_extended_title_padding), 0, getResources().getDimensionPixelSize(R.dimen.sesl_appbar_extended_title_padding), 0);
            } else {
                mExtendedSubTitle.setText(subtitle);
            }
            if (mExtendedTitle != null) {
                mExtendedTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(mIsOneUI4 ? R.dimen.sesl4_appbar_extended_title_text_size_with_subtitle : R.dimen.sesl_appbar_extended_title_text_size_with_subtitle));
            }
        }

        updateTitleLayout();
        requestLayout();
    }

    public void seslSetCustomSubtitle(View v) {
        if (v != null) {
            mSubTitleEnabled = true;
            mCustomSubTitleView = v;
            if (mTitleEnabled) {
                mTitleLayout.addView(v);
            }
        } else {
            mSubTitleEnabled = false;
            if (mCustomSubTitleView != null) {
                ((ViewGroup) mCustomSubTitleView.getParent()).removeView(mCustomSubTitleView);
                mCustomSubTitleView = null;
            }
        }

        updateTitleLayout();
        requestLayout();
    }

    public View seslGetCustomSubtitle() {
        return this.mCustomSubTitleView;
    }

    public CharSequence getSubTitle() {
        return mExtendedSubTitle != null ? mExtendedSubTitle.getText() : null;
    }

    private int getStatusbarHeight() {
        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            return getResources().getDimensionPixelOffset(resId);
        } else {
            return 0;
        }
    }

    // kang
    public int seslGetMinimumHeightWithoutMargin() {
        ViewGroup var1 = this.toolbar;
        int var4;
        if (var1 != null) {
            View var2 = this.toolbarDirectChild;
            Object var3 = var1;
            if (var2 != null) {
                if (var2 == this) {
                    var3 = var1;
                } else {
                    var3 = var2;
                }
            }

            android.view.ViewGroup.LayoutParams var5 = ((View)var3).getLayoutParams();
            if (var5 instanceof MarginLayoutParams) {
                MarginLayoutParams var6 = (MarginLayoutParams)var5;
                var4 = var6.topMargin + var6.bottomMargin;
                return ViewCompat.getMinimumHeight(this) - var4;
            }
        }

        var4 = 0;
        return ViewCompat.getMinimumHeight(this) - var4;
    }
    // kang

    public void seslSetUseCustomAccessibility(boolean custom) {
        mIsCustomAccessibility = custom;
    }

    public boolean seslIsCustomAccessibility() {
        return mIsCustomAccessibility;
    }


    private class OffsetUpdateListener implements SamsungAppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {}

        @SuppressLint("RestrictedApi")
        @Override
        public void onOffsetChanged(SamsungAppBarLayout layout, int verticalOffset) {
            currentOffset = verticalOffset;

            mTitleLayout.setTranslationY((float) ((-currentOffset) / 3));

            final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.collapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
                        break;
                    default:
                        break;
                }
            }

            updateScrimVisibility();

            if (statusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(SamsungCollapsingToolbarLayout.this);
            }


            if (mTitleEnabled) {
                int layoutPosition = Math.abs(layout.getTop());
                float alphaRange = ((float) getHeight()) * 0.17999999f;

                float titleAlpha = 255.0f - ((100.0f / alphaRange) * (((float) layoutPosition) - 0.0f));

                if (titleAlpha < 0.0f) {
                    titleAlpha = 0.0f;
                } else if (titleAlpha > 255.0f) {
                    titleAlpha = 255.0f;
                }

                if (layout.getBottom() > mDefaultHeight && !layout.seslIsCollapsed()) {
                    mTitleLayout.setAlpha(titleAlpha / 255.0f);
                } else {
                    mTitleLayout.setAlpha(0.0f);
                }

                // moved inside ToolbarLayout.java

            } else if (collapsingTitleEnabled) {
                int height = getHeight();
                final int expandRange = height - ViewCompat.getMinimumHeight(SamsungCollapsingToolbarLayout.this) - insetTop;
                collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);
            }

        }
    }

    public void seslEnableFadeToolbarTitle(boolean fade) {
        mFadeToolbarTitle = fade;
    }
}
