package de.dlyt.yanndroid.oneui.sesl.recyclerview;

import static android.view.View.SCROLLBAR_POSITION_DEFAULT;
import static android.view.View.SCROLLBAR_POSITION_LEFT;
import static android.view.View.SCROLLBAR_POSITION_RIGHT;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.IntProperty;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class SeslRecyclerViewFastScroller {
    private static final int DURATION_CROSS_FADE = 0;
    private static final int DURATION_FADE_IN = 167;
    private static final int DURATION_FADE_OUT = 150;
    private static final int DURATION_RESIZE = 100;
    public static final int EFFECT_STATE_CLOSE = 0;
    public static final int EFFECT_STATE_OPEN = 1;
    private static final long FADE_TIMEOUT = 1500;
    private static final int FASTSCROLL_VIBRATE_INDEX = 26;
    private static final int MIN_PAGES = 1;
    private static final int OVERLAY_ABOVE_THUMB = 2;
    private static final int OVERLAY_AT_THUMB = 1;
    private static final int OVERLAY_FLOATING = 0;
    private static final int PREVIEW_LEFT = 0;
    private static final int PREVIEW_RIGHT = 1;
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_NONE = 0;
    private static final int STATE_VISIBLE = 1;
    private static final String TAG = "SeslFastScroller";
    private static final int THUMB_POSITION_INSIDE = 1;
    private static final int THUMB_POSITION_MIDPOINT = 0;
    private int mAdditionalBottomPadding;
    private float mAdditionalTouchArea = 0.0f;
    private boolean mAlwaysShow;
    private Context mContext;
    private AnimatorSet mDecorAnimation;
    private boolean mEnabled;
    private int mImmersiveBottomPadding;
    private float mInitialTouchY;
    private boolean mIsDexMode;
    private boolean mLayoutFromRight;
    private RecyclerView.Adapter mListAdapter;
    private boolean mLongList;
    private boolean mMatchDragPosition;
    private int mOldChildCount;
    private int mOldItemCount;
    private int mOrientation;
    private final ViewGroupOverlay mOverlay;
    private int mOverlayPosition;
    private AnimatorSet mPreviewAnimation;
    private final View mPreviewImage;
    private int mPreviewMarginEnd;
    private int mPreviewMinHeight;
    private int mPreviewMinWidth;
    private int mPreviewPadding;
    private final int[] mPreviewResId = new int[2];
    private final TextView mPrimaryText;
    private final RecyclerView mRecyclerView;
    private int mScaledTouchSlop;
    private int mScrollBarStyle;
    private final TextView mSecondaryText;
    private SectionIndexer mSectionIndexer;
    private Object[] mSections;
    private boolean mShowingPreview;
    private boolean mShowingPrimary;
    private int mTextAppearance;
    private ColorStateList mTextColor;
    private float mTextSize;
    private Drawable mThumbDrawable;
    private final ImageView mThumbImage;
    private int mThumbMarginEnd;
    private int mThumbMinHeight;
    private int mThumbMinWidth;
    private float mThumbOffset;
    private int mThumbPosition;
    private float mThumbRange;
    private Drawable mTrackDrawable;
    private final ImageView mTrackImage;
    private int mTrackPadding;
    private boolean mUpdatingLayout;
    private int mVibrateIndex;
    private int mWidth;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final long TAP_TIMEOUT = (long) ViewConfiguration.getTapTimeout();
    private final Rect mTempBounds = new Rect();
    private final Rect mTempMargins = new Rect();
    private final Rect mContainerRect = new Rect();
    private int mCurrentSection = -1;
    private int mScrollbarPosition = -1;
    private long mPendingDrag = -1;
    private int mColorPrimary = -1;
    private int mThumbBackgroundColor = -1;
    private float mScrollY = 0.0f;
    private int mEffectState = 0;
    private float mOldThumbPosition = -1.0f;
    private float mThreshold = 0.0f;
    private float mLastDraggingY = 0.0f;
    private boolean mScrollCompleted = true;
    private int mState = STATE_VISIBLE;

    private static Property<View, Integer> LEFT = new IntProperty<View>("left") {
        @Override
        public void setValue(View object, int value) {
            object.setLeft(value);
        }

        @Override
        public Integer get(View object) {
            return object.getLeft();
        }
    };

    private static Property<View, Integer> TOP = new IntProperty<View>("top") {
        @Override
        public void setValue(View object, int value) {
            object.setTop(value);
        }

        @Override
        public Integer get(View object) {
            return object.getTop();
        }
    };

    private static Property<View, Integer> RIGHT = new IntProperty<View>("right") {
        @Override
        public void setValue(View object, int value) {
            object.setRight(value);
        }

        @Override
        public Integer get(View object) {
            return object.getRight();
        }
    };

    private static Property<View, Integer> BOTTOM = new IntProperty<View>("bottom") {
        @Override
        public void setValue(View object, int value) {
            object.setBottom(value);
        }

        @Override
        public Integer get(View object) {
            return object.getBottom();
        }
    };

    private final Runnable mDeferHide = new Runnable() {
        @Override
        public void run() {
            setState(STATE_NONE);
        }
    };

    private final Animator.AnimatorListener mSwitchPrimaryListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            mShowingPrimary = !mShowingPrimary;
        }
    };

    public SeslRecyclerViewFastScroller(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mOldItemCount = recyclerView.getAdapter().getItemCount();
        mOldChildCount = recyclerView.getChildCount();

        mContext = recyclerView.getContext();
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mScrollBarStyle = recyclerView.getScrollBarStyle();

        mMatchDragPosition = mContext.getApplicationInfo().targetSdkVersion >= 11;

        mTrackImage = new ImageView(mContext);
        mTrackImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mThumbImage = new ImageView(mContext);
        mThumbImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mPreviewImage = new View(mContext);
        mPreviewImage.setAlpha(0.0f);

        mPrimaryText = createPreviewTextView(mContext);
        mSecondaryText = createPreviewTextView(mContext);

        TypedArray a = mContext.getTheme().obtainStyledAttributes(null, R.styleable.SeslRecyclerViewFastScroller, 0, R.style.RecyclerViewFastScrollStyle);
        mOverlayPosition = a.getInt(R.styleable.SeslRecyclerViewFastScroller_position, 0);
        mPreviewResId[PREVIEW_LEFT] = a.getResourceId(R.styleable.SeslRecyclerViewFastScroller_backgroundLeft, 0);
        mPreviewResId[PREVIEW_RIGHT] = a.getResourceId(R.styleable.SeslRecyclerViewFastScroller_backgroundRight, 0);
        mThumbDrawable = a.getDrawable(R.styleable.SeslRecyclerViewFastScroller_thumbDrawable);
        mTrackDrawable = a.getDrawable(R.styleable.SeslRecyclerViewFastScroller_trackDrawable);
        mTextAppearance = a.getResourceId(R.styleable.SeslRecyclerViewFastScroller_android_textAppearance, 0);
        mTextColor = a.getColorStateList(R.styleable.SeslRecyclerViewFastScroller_android_textColor);
        mTextSize = a.getDimension(R.styleable.SeslRecyclerViewFastScroller_android_textSize, 0);
        mPreviewMinWidth = a.getDimensionPixelSize(R.styleable.SeslRecyclerViewFastScroller_android_minWidth, 0);
        mPreviewMinHeight = a.getDimensionPixelSize(R.styleable.SeslRecyclerViewFastScroller_android_minHeight, 0);
        mThumbMinWidth = a.getDimensionPixelSize(R.styleable.SeslRecyclerViewFastScroller_thumbMinWidth, 0);
        mThumbMinHeight = a.getDimensionPixelSize(R.styleable.SeslRecyclerViewFastScroller_thumbMinHeight, 0);
        mPreviewPadding = a.getDimensionPixelSize(R.styleable.SeslRecyclerViewFastScroller_android_padding, 0);
        mThumbPosition = a.getInt(R.styleable.SeslRecyclerViewFastScroller_thumbPosition, 0);
        a.recycle();
        
        updateAppearance();
        
        mOverlay = recyclerView.getOverlay();
        mOverlay.add(mTrackImage);
        mOverlay.add(mThumbImage);
        mOverlay.add(mPreviewImage);
        mOverlay.add(mPrimaryText);
        mOverlay.add(mSecondaryText);

        mPreviewMarginEnd = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroll_preview_margin_end);
        mThumbMarginEnd = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroll_thumb_margin_end);
        mAdditionalTouchArea = mContext.getResources().getDimension(R.dimen.sesl_fast_scroll_additional_touch_area);
        mTrackPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroller_track_padding);
        mAdditionalBottomPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroller_additional_bottom_padding);
        mImmersiveBottomPadding = 0;

        mIsDexMode = SeslConfigurationReflector.isDexEnabled(mContext.getResources().getConfiguration());

        mPrimaryText.setPadding(mPreviewPadding, 0, mPreviewPadding, 0);
        mSecondaryText.setPadding(mPreviewPadding, 0, mPreviewPadding, 0);

        getSectionsFromIndexer();
        updateLongList(mOldChildCount, mOldItemCount);
        setScrollbarPosition(recyclerView.getVerticalScrollbarPosition());
        postAutoHide();

        mVibrateIndex = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(FASTSCROLL_VIBRATE_INDEX);
    }

    private void updateAppearance() {
        TypedValue colorPrimary = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        mColorPrimary = getColorWithAlpha(colorPrimary.data, 0.9f);

        mThumbBackgroundColor = mContext.getResources().getColor(R.color.sesl_fast_scrollbar_bg_color);
        mTrackImage.setImageDrawable(mTrackDrawable);
        if (mThumbDrawable != null) {
            DrawableCompat.setTint(mThumbDrawable, mThumbBackgroundColor);
        }
        mThumbImage.setImageDrawable(mThumbDrawable);
        mThumbImage.setMinimumWidth(mThumbMinWidth);
        mThumbImage.setMinimumHeight(mThumbMinHeight);

        int width = 0;
        if (mTrackDrawable != null) {
            width = Math.max(0, mTrackDrawable.getIntrinsicWidth());
        }
        if (mThumbDrawable != null) {
            width = Math.max(width, mThumbDrawable.getIntrinsicWidth());
        }
        mWidth = Math.max(width, mThumbMinWidth);

        mPreviewImage.setMinimumWidth(mPreviewMinWidth);
        mPreviewImage.setMinimumHeight(mPreviewMinHeight);

        if (mTextAppearance != 0) {
            mPrimaryText.setTextAppearance(mContext, mTextAppearance);
            mSecondaryText.setTextAppearance(mContext, mTextAppearance);
        }

        if (mTextColor != null) {
            mPrimaryText.setTextColor(mTextColor);
            mSecondaryText.setTextColor(mTextColor);
        }

        if (mTextSize > 0.0f) {
            mPrimaryText.setTextSize(0, mTextSize);
            mSecondaryText.setTextSize(0, this.mTextSize);
        }

        mPrimaryText.setMinimumWidth(mPreviewMinWidth);
        mPrimaryText.setMinimumHeight(Math.max(0, mPreviewMinHeight));
        mPrimaryText.setIncludeFontPadding(false);
        mSecondaryText.setMinimumWidth(mPreviewMinWidth);
        mSecondaryText.setMinimumHeight(Math.max(0, mPreviewMinHeight));
        mSecondaryText.setIncludeFontPadding(false);

        mOrientation = mContext.getResources().getConfiguration().orientation;

        refreshDrawablePressedState();
    }

    public void remove() {
        mOverlay.remove(mTrackImage);
        mOverlay.remove(mThumbImage);
        mOverlay.remove(mPreviewImage);
        mOverlay.remove(mPrimaryText);
        mOverlay.remove(mSecondaryText);
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;

            onStateDependencyChanged(true);
        }
    }

    public boolean isEnabled() {
        if (mEnabled && !mLongList) {
            mLongList = canScrollList(1) || canScrollList(-1);
        }
        return mEnabled && (mLongList || mAlwaysShow);
    }

    public void setAlwaysShow(boolean alwaysShow) {
        if (mAlwaysShow != alwaysShow) {
            mAlwaysShow = alwaysShow;

            onStateDependencyChanged(false);
        }
    }

    public boolean isAlwaysShowEnabled() {
        return mAlwaysShow;
    }

    private void onStateDependencyChanged(boolean peekIfEnabled) {
        if (isEnabled()) {
            if (isAlwaysShowEnabled()) {
                setState(STATE_VISIBLE);
            } else if (mState == STATE_VISIBLE) {
                postAutoHide();
            } else if (peekIfEnabled) {
                setState(STATE_VISIBLE);
                postAutoHide();
            }
        } else {
            stop();
        }
    }

    public void setScrollBarStyle(int style) {
        if (mScrollBarStyle != style) {
            mScrollBarStyle = style;

            updateLayout();
        }
    }

    public void stop() {
        setState(STATE_NONE);
    }

    public void setScrollbarPosition(int position) {
        if (position == SCROLLBAR_POSITION_DEFAULT) {
            position = mRecyclerView.mLayout.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? SCROLLBAR_POSITION_LEFT : SCROLLBAR_POSITION_RIGHT;
        }
        if (mScrollbarPosition != position) {
            mScrollbarPosition = position;
            mLayoutFromRight = position != SCROLLBAR_POSITION_LEFT;

            final int previewResId = mPreviewResId[mLayoutFromRight ? PREVIEW_RIGHT : PREVIEW_LEFT];
            mPreviewImage.setBackgroundResource(previewResId);
            mPreviewImage.getBackground().setTintMode(PorterDuff.Mode.MULTIPLY);
            mPreviewImage.getBackground().setTint(mColorPrimary);

            updateLayout();
        }
    }

    private int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getEffectState() {
        return mEffectState;
    }

    public float getScrollY() {
        return mScrollY;
    }

    public void setThreshold(float threshold) {
        Log.d(TAG, "FastScroller setThreshold called = " + threshold);
        mThreshold = threshold;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        mLongList = canScrollList(1) || canScrollList(-1);
        updateLayout();
    }

    public void onItemCountChanged(int childCount, int itemCount) {
        if (mOldChildCount == 0) {
            mOldChildCount = mRecyclerView.getChildCount();
        }
        if (mOldItemCount != itemCount || mOldChildCount != childCount) {
            mOldItemCount = itemCount;
            mOldChildCount = childCount;

            final boolean hasMoreItems = itemCount - childCount > 0;
            if (hasMoreItems && mState != STATE_DRAGGING) {
                setThumbPos(getPosFromItemCount(mRecyclerView.findFirstVisibleItemPosition(), childCount, itemCount));
            }

            updateLongList(childCount, itemCount);
        }
    }

    private void updateLongList(int childCount, int itemCount) {
        final boolean longList = childCount > 0 && (canScrollList(1) || canScrollList(-1));
        if (mLongList != longList) {
            mLongList = longList;

            onStateDependencyChanged(true);
        }
    }

    private TextView createPreviewTextView(Context context) {
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        textView.setGravity(Gravity.CENTER);
        textView.setAlpha(0.0f);

        textView.setLayoutDirection(mRecyclerView.getLayoutDirection());

        return textView;
    }

    public void updateLayout() {
        if (mUpdatingLayout) {
            return;
        }

        mUpdatingLayout = true;

        updateContainerRect();

        layoutThumb();
        layoutTrack();

        updateOffsetAndRange();

        mUpdatingLayout = false;

        final Rect bounds = mTempBounds;
        measurePreview(mPrimaryText, bounds);
        applyLayout(mPrimaryText, bounds);
        measurePreview(mSecondaryText, bounds);
        applyLayout(mSecondaryText, bounds);

        bounds.left -= mPreviewImage.getPaddingLeft();
        bounds.top -= mPreviewImage.getPaddingTop();
        bounds.right += mPreviewImage.getPaddingRight();
        bounds.bottom += mPreviewImage.getPaddingBottom();
        applyLayout(mPreviewImage, bounds);
    }

    private void applyLayout(View view, Rect bounds) {
        view.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
        view.setPivotX(mLayoutFromRight ? bounds.right - bounds.left : 0.0f);
    }

    private void measurePreview(View v, Rect out) {
        final Rect margins = mTempMargins;
        margins.left = mPreviewImage.getPaddingLeft();
        margins.top = mPreviewImage.getPaddingTop();
        margins.right = mPreviewImage.getPaddingRight();
        margins.bottom = mPreviewImage.getPaddingBottom();

        if (mOverlayPosition == OVERLAY_FLOATING) {
            measureFloating(v, margins, out);
        } else {
            measureViewToSide(v, mThumbImage, margins, out);
        }
    }

    private void measureViewToSide(View view, View adjacent, Rect margins, Rect out) {
        final int marginLeft;
        final int marginRight;
        if (mLayoutFromRight) {
            marginRight = adjacent == null ? mThumbMarginEnd : mPreviewMarginEnd;
            marginLeft = 0;
        } else {
            marginLeft = adjacent == null ? mThumbMarginEnd : mPreviewMarginEnd;
            marginRight = 0;
        }

        final Rect container = mContainerRect;
        final int containerWidth = container.width();
        final int maxWidth;
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (mLayoutFromRight) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }

        final int adjMaxHeight = Math.max(0, container.height());
        final int adjMaxWidth = Math.max(0, maxWidth - marginLeft - marginRight);
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(adjMaxWidth, MeasureSpec.AT_MOST);
        final int heightMeasureSpec = makeSafeMeasureSpec(adjMaxHeight, MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);

        final int width = Math.min(adjMaxWidth, view.getMeasuredWidth());
        final int left;
        final int right;
        if (mLayoutFromRight) {
            right = (adjacent == null ? container.right : adjacent.getLeft()) - marginRight;
            left = right - width;
        } else {
            left = (adjacent == null ? container.left : adjacent.getRight()) + marginLeft;
            right = left + width;
        }

        out.set(left, 0, right, view.getMeasuredHeight());
    }

    private void measureFloating(View preview, Rect margins, Rect out) {
        final int marginLeft;
        final int marginTop;
        final int marginRight;
        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }

        final Rect container = mContainerRect;
        final int containerWidth = container.width();
        final int adjMaxHeight = Math.max(0, container.height());
        final int adjMaxWidth = Math.max(0, containerWidth - marginLeft - marginRight);
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(adjMaxWidth, MeasureSpec.AT_MOST);
        final int heightMeasureSpec = makeSafeMeasureSpec(adjMaxHeight, MeasureSpec.UNSPECIFIED);
        preview.measure(widthMeasureSpec, heightMeasureSpec);

        final int containerHeight = container.height();
        final int width = preview.getMeasuredWidth();
        final int top = containerHeight / 10 + marginTop + container.top;
        final int bottom = top + preview.getMeasuredHeight();
        final int left = (containerWidth - width) / 2 + container.left;
        final int right = left + width;
        out.set(left, top, right, bottom);
    }

    private void updateContainerRect() {
        final RecyclerView recyclerView = mRecyclerView;

        final Rect container = mContainerRect;
        container.left = 0;
        container.top = 0;
        container.right = recyclerView.getWidth();
        container.bottom = recyclerView.getHeight();

        final int scrollbarStyle = mScrollBarStyle;
        if (scrollbarStyle == View.SCROLLBARS_INSIDE_INSET || scrollbarStyle == View.SCROLLBARS_INSIDE_OVERLAY) {
            container.left += recyclerView.getPaddingLeft();
            container.top += recyclerView.getPaddingTop();
            container.right -= recyclerView.getPaddingRight();
            container.bottom -= recyclerView.getPaddingBottom();

            if (scrollbarStyle == View.SCROLLBARS_INSIDE_INSET) {
                final int width = getWidth();
                if (mScrollbarPosition == View.SCROLLBAR_POSITION_RIGHT) {
                    container.right += width;
                } else {
                    container.left -= width;
                }
            }
        }
    }

    public void setImmersiveBottomPadding(int bottom) {
        mImmersiveBottomPadding = bottom;

        updateOffsetAndRange();
    }

    private void layoutThumb() {
        final Rect bounds = mTempBounds;
        measureViewToSide(mThumbImage, null, null, bounds);
        applyLayout(mThumbImage, bounds);
    }

    private void layoutTrack() {
        final View track = mTrackImage;
        final View thumb = mThumbImage;
        final Rect container = mContainerRect;
        final int maxWidth = Math.max(0, container.width());
        final int maxHeight = Math.max(0, container.height());
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        final int heightMeasureSpec = makeSafeMeasureSpec(MeasureSpec.getSize(maxHeight), MeasureSpec.UNSPECIFIED);
        track.measure(widthMeasureSpec, heightMeasureSpec);

        int top;
        int bottom;
        if (mThumbPosition == THUMB_POSITION_INSIDE) {
            top = container.top + mTrackPadding;
            bottom = container.bottom - mTrackPadding - mAdditionalBottomPadding;
        } else {
            final int thumbHalfHeight = thumb.getHeight() / 2;
            bottom = container.bottom - thumbHalfHeight - mTrackPadding - mAdditionalBottomPadding;
            top = container.top + thumbHalfHeight + mTrackPadding;
        }

        if (bottom < top) {
            Log.e(TAG, "Error occured during layoutTrack() because bottom[" + bottom + "] is less than top[" + top + "].");
            bottom = top;
        }
        final int trackWidth = track.getMeasuredWidth();
        final int left = thumb.getLeft() + (thumb.getWidth() - trackWidth) / 2;
        final int right = left + trackWidth;
        track.layout(left, top, right, bottom);
    }

    private void updateOffsetAndRange() {
        final View trackImage = mTrackImage;
        final View thumbImage = mThumbImage;
        final float min;
        final float max;
        if (mThumbPosition == THUMB_POSITION_INSIDE) {
            final float halfThumbHeight = thumbImage.getHeight() / 2f;
            min = trackImage.getTop() + halfThumbHeight;
            max = trackImage.getBottom() - halfThumbHeight;
        } else{
            min = trackImage.getTop();
            max = trackImage.getBottom();
        }

        mThumbOffset = min;
        mThumbRange = max - min - mImmersiveBottomPadding;
        if (mThumbRange < 0.0f) {
            mThumbRange = 0.0f;
        }
    }

    private void setState(int state) {
        mRecyclerView.removeCallbacks(mDeferHide);

        if (mAlwaysShow && state == STATE_NONE) {
            state = STATE_VISIBLE;
        }

        if (state == mState) {
            return;
        }

        switch (state) {
            case STATE_NONE:
                transitionToHidden();
                break;
            case STATE_VISIBLE:
                if (mThumbDrawable != null) {
                    DrawableCompat.setTint(mThumbDrawable, mThumbBackgroundColor);
                }
                transitionToVisible();
                break;
            case STATE_DRAGGING:
                if (mThumbDrawable != null) {
                    DrawableCompat.setTint(mThumbDrawable, mColorPrimary);
                }
                transitionPreviewLayout(mCurrentSection);
                break;
        }

        mState = state;

        refreshDrawablePressedState();
    }

    private void refreshDrawablePressedState() {
        boolean isPressed = mState == STATE_DRAGGING;
        mThumbImage.setPressed(isPressed);
        mTrackImage.setPressed(isPressed);
    }

    private void transitionToHidden() {
        mShowingPreview = false;
        mCurrentSection = -1;

        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }

        final Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0f, mThumbImage, mTrackImage, mPreviewImage, mPrimaryText, mSecondaryText)
                .setDuration(mDecorAnimation != null ? DURATION_FADE_OUT : 0);

        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(fadeOut);
        mDecorAnimation.setInterpolator(LINEAR_INTERPOLATOR);
        mDecorAnimation.start();
    }

    private void transitionToVisible() {
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }

        final Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1f, mThumbImage, mTrackImage)
                .setDuration(DURATION_FADE_IN);
        final Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0f, mPreviewImage, mPrimaryText, mSecondaryText)
                .setDuration(DURATION_FADE_OUT);

        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(fadeIn, fadeOut);
        mDecorAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);

        mShowingPreview = false;

        mDecorAnimation.start();
    }

    private void transitionToDragging() {
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }

        final Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1f, mThumbImage, mTrackImage, mPreviewImage)
                .setDuration(DURATION_FADE_IN);

        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(fadeIn);
        mDecorAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);
        mDecorAnimation.start();

        mShowingPreview = true;
    }

    private void postAutoHide() {
        mRecyclerView.removeCallbacks(mDeferHide);
        mRecyclerView.postDelayed(mDeferHide, FADE_TIMEOUT);
    }

    public boolean canScrollList(int direction) {
        final int childCount = mRecyclerView.getChildCount();
        if (childCount == 0) {
            return false;
        }
        final int firstPosition = mRecyclerView.findFirstVisibleItemPosition();
        final Rect listPadding = mRecyclerView.mListPadding;
        if (direction > 0) {
            final int lastBottom = mRecyclerView.getChildAt(childCount - 1).getBottom();
            final int lastPosition = firstPosition + childCount;
            return lastPosition < mRecyclerView.getAdapter().getItemCount() || lastBottom > mRecyclerView.getHeight() - listPadding.bottom;
        } else {
            final int firstTop = mRecyclerView.getChildAt(0).getTop();
            return firstPosition > 0 || firstTop < listPadding.top;
        }
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!isEnabled()) {
            setState(STATE_NONE);
            return;
        }

        final boolean canScrollList = (canScrollList(1) || canScrollList(-1));
        if (canScrollList && mState != STATE_DRAGGING) {
            if (mOldThumbPosition != -1.0f) {
                setThumbPos(mOldThumbPosition);
                mOldThumbPosition = -1.0f;
            } else {
                setThumbPos(getPosFromItemCount(firstVisibleItem, visibleItemCount, totalItemCount));
            }
        }

        mScrollCompleted = true;

        if (mState != STATE_DRAGGING) {
            setState(STATE_VISIBLE);
            postAutoHide();
        }
    }

    private void getSectionsFromIndexer() {
        mSectionIndexer = null;

        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter instanceof SectionIndexer) {
            mListAdapter = adapter;
            mSectionIndexer = (SectionIndexer) adapter;
            mSections = mSectionIndexer.getSections();
        } else {
            mListAdapter = adapter;
            mSections = null;
        }
    }

    public void onSectionsChanged() {
        mListAdapter = null;
    }

    private void scrollTo(float position) {
        mScrollCompleted = false;

        final int count = mRecyclerView.getAdapter().getItemCount();
        final Object[] sections = mSections;
        final int sectionCount = sections == null ? 0 : sections.length;
        int sectionIndex;
        if (sections != null && sectionCount > 1) {
            final int exactSection = constrain((int) (position * sectionCount), 0, sectionCount - 1);
            int targetSection = exactSection;
            int targetIndex = mSectionIndexer.getPositionForSection(targetSection);
            sectionIndex = targetSection;

            int nextIndex = count;
            int prevIndex = targetIndex;
            int prevSection = targetSection;
            int nextSection = targetSection + 1;

            if (targetSection < sectionCount - 1) {
                nextIndex = mSectionIndexer.getPositionForSection(targetSection + 1);
            }

            if (nextIndex == targetIndex) {
                while (targetSection > 0) {
                    targetSection--;
                    prevIndex = mSectionIndexer.getPositionForSection(targetSection);
                    if (prevIndex != targetIndex) {
                        prevSection = targetSection;
                        sectionIndex = targetSection;
                        break;
                    } else if (targetSection == 0) {
                        sectionIndex = 0;
                        break;
                    }
                }
            }

            int nextNextSection = nextSection + 1;
            while (nextNextSection < sectionCount && mSectionIndexer.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }

            final float prevPosition = (float) prevSection / sectionCount;
            final float nextPosition = (float) nextSection / sectionCount;
            final float snapThreshold = (count == 0) ? Float.MAX_VALUE : .125f / count;
            if (prevSection == exactSection && position - prevPosition < snapThreshold) {
                targetIndex = prevIndex;
            } else {
                targetIndex = prevIndex + (int) ((nextIndex - prevIndex) * (position - prevPosition) / (nextPosition - prevPosition));
            }

            targetIndex = constrain(targetIndex, 0, count - 1);

            if (mRecyclerView.mLayout instanceof LinearLayoutManager) {
                ((LinearLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(targetIndex, 0);
            } else {
                ((StaggeredGridLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(targetIndex, 0, true);
            }
        } else {
            final int index = constrain((int) (position * count), 0, count - 1);

            sectionIndex = -1;

            if (mRecyclerView.mLayout instanceof LinearLayoutManager) {
                ((LinearLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(index, 0);
            } else {
                ((StaggeredGridLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(index, 0, true);
            }
        }

        onScroll(mRecyclerView.findFirstVisibleItemPosition(), mRecyclerView.getChildCount(), mRecyclerView.getAdapter().getItemCount());

        mCurrentSection = sectionIndex;

        final boolean hasPreview = transitionPreviewLayout(sectionIndex);
        Log.d(TAG, "scrollTo() called transitionPreviewLayout() sectionIndex =" + sectionIndex + ", position = " + position);
        if (!mShowingPreview && hasPreview) {
            transitionToDragging();
        } else if (mShowingPreview && !hasPreview) {
            transitionToVisible();
        }
    }

    private boolean transitionPreviewLayout(int sectionIndex) {
        final Object[] sections = mSections;
        String text = null;
        if (sections != null && sectionIndex >= 0 && sectionIndex < sections.length) {
            final Object section = sections[sectionIndex];
            if (section != null) {
                text = section.toString();
            }
        }

        final Rect bounds = mTempBounds;
        final View preview = mPreviewImage;
        final TextView showing;
        final TextView target;
        if (mShowingPrimary) {
            showing = mPrimaryText;
            target = mSecondaryText;
        } else {
            showing = mSecondaryText;
            target = mPrimaryText;
        }

        target.setText(text);
        measurePreview(target, bounds);
        applyLayout(target, bounds);

        if (mState == STATE_VISIBLE) {
            showing.setText("");
        } else if (mState == STATE_DRAGGING && showing.getText().equals(text)) {
            return !TextUtils.isEmpty(text);
        }

        if (mPreviewAnimation != null) {
            mPreviewAnimation.cancel();
        }

        if (!showing.getText().equals("")) {
            mRecyclerView.performHapticFeedback(mVibrateIndex);
        }

        final Animator showTarget = animateAlpha(target, 1f).setDuration(DURATION_CROSS_FADE);
        final Animator hideShowing = animateAlpha(showing, 0f).setDuration(DURATION_CROSS_FADE);
        hideShowing.addListener(mSwitchPrimaryListener);

        bounds.left -= preview.getPaddingLeft();
        bounds.top -= preview.getPaddingTop();
        bounds.right += preview.getPaddingRight();
        bounds.bottom += preview.getPaddingBottom();
        final Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(DURATION_RESIZE);

        mPreviewAnimation = new AnimatorSet();
        final AnimatorSet.Builder builder = mPreviewAnimation.play(hideShowing).with(showTarget);
        builder.with(resizePreview);

        final int previewWidth = preview.getWidth() - preview.getPaddingLeft() - preview.getPaddingRight();

        final int targetWidth = target.getWidth();
        if (targetWidth > previewWidth) {
            target.setScaleX((float) previewWidth / targetWidth);
            final Animator scaleAnim = animateScaleX(target, 1f).setDuration(DURATION_RESIZE);
            builder.with(scaleAnim);
        } else {
            target.setScaleX(1f);
        }

        final int showingWidth = showing.getWidth();
        if (showingWidth > targetWidth) {
            final float scale = (float) targetWidth / showingWidth;
            final Animator scaleAnim = animateScaleX(showing, scale).setDuration(DURATION_RESIZE);
            builder.with(scaleAnim);
        }

        mPreviewAnimation.setInterpolator(SeslAnimationUtils.SINE_IN_OUT_70);
        mPreviewAnimation.start();

        return !TextUtils.isEmpty(text);
    }

    private void setThumbPos(float position) {
        final Rect container = mContainerRect;
        final int top = container.top;
        final int bottom = container.bottom;

        if (position > 1.0f) {
            position = 1.0f;
        } else if (position < 0.0f) {
            position = 0.0f;
        }

        final float thumbMiddle = position * mThumbRange + mThumbOffset;
        mThumbImage.setTranslationY(thumbMiddle - mThumbImage.getHeight() / 2f);

        final View previewImage = mPreviewImage;
        final float previewHalfHeight = previewImage.getHeight() / 2f;
        final float minP = top + previewHalfHeight;
        final float maxP = bottom - previewHalfHeight;
        final float previewMiddle = constrain(thumbMiddle, minP, maxP);
        final float previewTop = previewMiddle - previewHalfHeight;
        previewImage.setTranslationY(previewTop);

        mPrimaryText.setTranslationY(previewTop);
        mSecondaryText.setTranslationY(previewTop);
    }

    private float getPosFromMotionEvent(float y) {
        if (mThumbRange <= 0) {
            return 0f;
        }

        return constrain((y - mThumbOffset) / mThumbRange, 0f, 1f);
    }

    // kang
    private float getPosFromItemCount(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        float f;
        float f2;
        float f3;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;

        if (mSectionIndexer == null || mListAdapter == null) {
            getSectionsFromIndexer();
        }

        float f4 = 0.0f;
        if (visibleItemCount == 0 || totalItemCount == 0) {
            return 0.0f;
        }
        SectionIndexer sectionIndexer = mSectionIndexer;
        int paddingTop = mRecyclerView.getPaddingTop();
        if (paddingTop > 0 && (mRecyclerView.mLayout instanceof LinearLayoutManager)) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.mLayout;
            while (firstVisibleItem > 0) {
                int i6 = firstVisibleItem - 1;
                if (linearLayoutManager.findViewByPosition(i6) == null) {
                    break;
                }
                firstVisibleItem = i6;
            }
        }
        boolean z = false;
        View childAt = mRecyclerView.getChildAt(0);
        if (childAt == null || childAt.getHeight() == 0) {
            f = 0.0f;
        } else {
            f = firstVisibleItem == 0 ? ((float) (paddingTop - childAt.getTop())) / ((float) (childAt.getHeight() + paddingTop)) : ((float) (-childAt.getTop())) / ((float) childAt.getHeight());
        }
        if (!(sectionIndexer == null || mSections == null || mSections.length <= 0)) {
            z = true;
        }
        if (!z || !mMatchDragPosition) {
            if (visibleItemCount != totalItemCount) {
                if (mRecyclerView.mLayout instanceof GridLayoutManager) {
                    i3 = ((GridLayoutManager) mRecyclerView.mLayout).getSpanCount() / ((GridLayoutManager) mRecyclerView.mLayout).getSpanSizeLookup().getSpanSize(firstVisibleItem);
                } else {
                    i3 = mRecyclerView.mLayout instanceof StaggeredGridLayoutManager ? ((StaggeredGridLayoutManager) mRecyclerView.mLayout).getSpanCount() : 1;
                }
                f2 = ((float) firstVisibleItem) + (f * ((float) i3));
                f3 = (float) totalItemCount;
            } else if (!(mRecyclerView.mLayout instanceof StaggeredGridLayoutManager) || firstVisibleItem == 0 || childAt == null || !((StaggeredGridLayoutManager.LayoutParams) childAt.getLayoutParams()).isFullSpan()) {
                return 0.0f;
            } else {
                return 1.0f;
            }
        } else if (firstVisibleItem < 0) {
            return 0.0f;
        } else {
            int sectionForPosition = sectionIndexer.getSectionForPosition(firstVisibleItem);
            int positionForSection = sectionIndexer.getPositionForSection(sectionForPosition);
            int length = mSections.length;
            if (sectionForPosition < length - 1) {
                int i10 = sectionForPosition + 1;
                if (i10 < length) {
                    i5 = sectionIndexer.getPositionForSection(i10);
                } else {
                    i5 = totalItemCount - 1;
                }
                i4 = i5 - positionForSection;
            } else {
                i4 = totalItemCount - positionForSection;
            }
            if (i4 != 0) {
                f4 = ((((float) firstVisibleItem) + f) - ((float) positionForSection)) / ((float) i4);
            }
            f2 = ((float) sectionForPosition) + f4;
            f3 = (float) length;
        }
        float f5 = f2 / f3;
        if (firstVisibleItem <= 0 || firstVisibleItem + visibleItemCount != totalItemCount) {
            return f5;
        }
        View childAt2 = mRecyclerView.getChildAt(visibleItemCount - 1);
        int paddingBottom = mRecyclerView.getPaddingBottom();
        if (mRecyclerView.getClipToPadding()) {
            i = childAt2.getHeight();
            i2 = (mRecyclerView.getHeight() - paddingBottom) - childAt2.getTop();
        } else {
            i = childAt2.getHeight() + paddingBottom;
            i2 = mRecyclerView.getHeight() - childAt2.getTop();
        }
        return (i2 <= 0 || i <= 0) ? f5 : f5 + ((1.0f - f5) * (((float) i2) / ((float) i)));
    }
    // kang

    private void cancelFling() {
        final MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mRecyclerView.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    private void cancelPendingDrag() {
        mPendingDrag = -1;
    }

    private void startPendingDrag() {
        mPendingDrag = SystemClock.uptimeMillis() + TAP_TIMEOUT;
    }

    private void beginDrag() {
        mPendingDrag = -1;

        if (mListAdapter == null) {
            getSectionsFromIndexer();
        }

        mRecyclerView.requestDisallowInterceptTouchEvent(true);

        cancelFling();

        setState(STATE_DRAGGING);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isPointInside(ev.getX(), ev.getY())) {
                    mRecyclerView.performHapticFeedback(mVibrateIndex);
                    if (!mRecyclerView.isInScrollingContainer() || mIsDexMode) {
                        return true;
                    }

                    mInitialTouchY = ev.getY();
                    startPendingDrag();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isPointInside(ev.getX(), ev.getY())) {
                    cancelPendingDrag();
                } else if (mPendingDrag >= 0 && mPendingDrag <= SystemClock.uptimeMillis()) {
                    beginDrag();

                    final float pos = getPosFromMotionEvent(mInitialTouchY);
                    mOldThumbPosition = pos;
                    scrollTo(pos);

                    return onTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelPendingDrag();
                break;
        }

        return false;
    }

    public boolean onInterceptHoverEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }

        final int actionMasked = ev.getActionMasked();
        if ((actionMasked == MotionEvent.ACTION_HOVER_ENTER || actionMasked == MotionEvent.ACTION_HOVER_MOVE) && mState == STATE_NONE && isPointInside(ev.getX(), ev.getY())) {
            setState(STATE_VISIBLE);
            postAutoHide();
        }

        return false;
    }

    public boolean onTouchEvent(MotionEvent me) {
        mScrollY = me.getY();

        if (!isEnabled()) {
            return false;
        }

        switch (me.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (isPointInside(me.getX(), me.getY())) {
                    if (!mRecyclerView.isInScrollingContainer() || mIsDexMode) {
                        beginDrag();
                        mEffectState = EFFECT_STATE_OPEN;
                        return true;
                    }
                }
            } break;

            case MotionEvent.ACTION_UP: {
                if (mPendingDrag >= 0) {
                    beginDrag();

                    final float pos = getPosFromMotionEvent(me.getY());
                    mOldThumbPosition = pos;
                    setThumbPos(pos);
                    scrollTo(pos);
                    mEffectState = EFFECT_STATE_OPEN;
                }

                if (mState == STATE_DRAGGING) {
                    mRecyclerView.requestDisallowInterceptTouchEvent(false);

                    setState(STATE_VISIBLE);
                    postAutoHide();

                    mEffectState = EFFECT_STATE_CLOSE;
                    mScrollY = 0.0f;
                    return true;
                }
            } break;

            case MotionEvent.ACTION_MOVE: {
                final Rect container = mContainerRect;
                final int containerTop = container.top;
                final int containerBottom = container.bottom;
                final View trackImage = mTrackImage;
                final int trackTop = trackImage.getTop();
                final int trackBottom = trackImage.getBottom();

                if (mPendingDrag >= 0 && Math.abs(me.getY() - mInitialTouchY) > mScaledTouchSlop) {
                    beginDrag();

                    if (mScrollY > containerTop && mScrollY < containerBottom) {
                        float top = containerTop + trackTop;
                        if (mScrollY < top) {
                            mScrollY = top;
                        } else if (mScrollY > trackBottom) {
                            mScrollY = trackBottom;
                        }
                        mEffectState = EFFECT_STATE_OPEN;
                    }
                }

                if (mState == STATE_DRAGGING) {
                    final float pos = getPosFromMotionEvent(me.getY());
                    mOldThumbPosition = pos;
                    setThumbPos(pos);

                    if (mThreshold != 0.0f && Math.abs(mLastDraggingY - mScrollY) <= mThreshold) {
                        return true;
                    }
                    mLastDraggingY = mScrollY;

                    if (mScrollCompleted) {
                        scrollTo(pos);
                    }

                    if (mScrollY > containerTop && mScrollY < containerBottom) {
                        float top = containerTop + trackTop;
                        if (mScrollY < top) {
                            mScrollY = top;
                        } else if (mScrollY > trackBottom) {
                            mScrollY = trackBottom;
                        }
                        mEffectState = EFFECT_STATE_OPEN;
                    }

                    return true;
                }
            } break;

            case MotionEvent.ACTION_CANCEL: {
                cancelPendingDrag();

                if (mState == STATE_DRAGGING) {
                    setState(STATE_NONE);
                }
                mEffectState = EFFECT_STATE_CLOSE;
                mScrollY = 0.0f;
            } break;
        }

        return false;
    }

    private boolean isPointInside(float x, float y) {
        return isPointInsideX(x) && isPointInsideY(y) && mState != STATE_NONE;
    }

    private boolean isPointInsideX(float x) {
        if (mLayoutFromRight) {
            return x >= mThumbImage.getLeft() - mAdditionalTouchArea;
        } else {
            return x <= mThumbImage.getRight() + mAdditionalTouchArea;
        }
    }

    private boolean isPointInsideY(float y) {
        final float offset = mThumbImage.getTranslationY();
        final float top = mThumbImage.getTop() + offset;
        final float bottom = mThumbImage.getBottom() + offset;

        return y >= top && y <= bottom;
    }

    private static Animator groupAnimatorOfFloat(Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        AnimatorSet.Builder builder = null;

        for (int i = views.length - 1; i >= 0; i--) {
            final Animator anim = ObjectAnimator.ofFloat(views[i], property, value);
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }

        return animSet;
    }

    private static Animator animateScaleX(View v, float target) {
        return ObjectAnimator.ofFloat(v, View.SCALE_X, target);
    }

    private static Animator animateAlpha(View v, float alpha) {
        return ObjectAnimator.ofFloat(v, View.ALPHA, alpha);
    }

    private static Animator animateBounds(View v, Rect bounds) {
        final PropertyValuesHolder left = PropertyValuesHolder.ofInt(LEFT, bounds.left);
        final PropertyValuesHolder top = PropertyValuesHolder.ofInt(TOP, bounds.top);
        final PropertyValuesHolder right = PropertyValuesHolder.ofInt(RIGHT, bounds.right);
        final PropertyValuesHolder bottom = PropertyValuesHolder.ofInt(BOTTOM, bounds.bottom);
        return ObjectAnimator.ofPropertyValuesHolder(v, left, top, right, bottom);
    }

    /*kang from MathUtils.smali*/
    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    private float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    /*kang from SeslViewReflector$SeslMeasureSpecReflector.smali*/
    private int makeSafeMeasureSpec(int size, int mode) {
        boolean useZeroUnspecifiedMeasureSpec = Build.VERSION.SDK_INT < 23;
        if (!useZeroUnspecifiedMeasureSpec || mode != 0) {
            return MeasureSpec.makeMeasureSpec(size, mode);
        }
        return 0;
    }
}
