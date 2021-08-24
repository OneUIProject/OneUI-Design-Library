package de.dlyt.yanndroid.oneui.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.utils.ReflectUtils;

class SeslRecyclerViewFastScroller {
    public static final int EFFECT_STATE_CLOSE = 0;
    public static final int EFFECT_STATE_OPEN = 1;
    private static final int DURATION_CROSS_FADE = 0;
    private static final int DURATION_FADE_IN = 167;
    private static final int DURATION_FADE_OUT = 167;
    private static final int DURATION_RESIZE = 100;
    private static final long FADE_TIMEOUT = 2500;
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
    private static final long TAP_TIMEOUT = ((long) ViewConfiguration.getTapTimeout());
    private static final int THUMB_POSITION_INSIDE = 1;
    private static final int THUMB_POSITION_MIDPOINT = 0;
    private static Property<View, Integer> BOTTOM = new IntProperty<View>("bottom") {
        public void setValue(View object, int value) {
            object.setBottom(value);
        }

        public Integer get(View object) {
            return object.getBottom();
        }
    };
    private static Property<View, Integer> LEFT = new IntProperty<View>("left") {
        public void setValue(View object, int value) {
            object.setLeft(value);
        }

        public Integer get(View object) {
            return object.getLeft();
        }
    };
    private static Property<View, Integer> RIGHT = new IntProperty<View>("right") {
        public void setValue(View object, int value) {
            object.setRight(value);
        }

        public Integer get(View object) {
            return object.getRight();
        }
    };
    private static Property<View, Integer> TOP = new IntProperty<View>("top") {
        public void setValue(View object, int value) {
            object.setTop(value);
        }

        public Integer get(View object) {
            return object.getTop();
        }
    };
    private final Rect mContainerRect = new Rect();
    private final ViewGroupOverlay mOverlay;
    private final View mPreviewImage;
    private final int[] mPreviewResId = new int[2];
    private final TextView mPrimaryText;
    private final SeslRecyclerView mRecyclerView;
    private final TextView mSecondaryText;
    private final Rect mTempBounds = new Rect();
    private final Rect mTempMargins = new Rect();
    private final ImageView mThumbImage;
    private final ImageView mTrackImage;
    private int mAdditionalBottomPadding;
    private float mAdditionalTouchArea = 0.0f;
    private boolean mAlwaysShow;
    private int mColorPrimary = -1;
    private Context mContext;
    private int mCurrentSection = -1;
    private AnimatorSet mDecorAnimation;
    private int mEffectState = 0;
    private boolean mEnabled;
    private int mFirstVisibleItem;
    private int mHeaderCount;
    private int mImmersiveBottomPadding;
    private float mInitialTouchY;
    private boolean mLayoutFromRight;
    private SeslRecyclerView.Adapter mListAdapter;
    private boolean mLongList;
    private boolean mMatchDragPosition;
    private int mOldChildCount;
    private int mOldItemCount;
    private float mOldThumbPosition = -1.0f;
    private int mOrientation;
    private int mOverlayPosition;
    private long mPendingDrag = -1;
    private AnimatorSet mPreviewAnimation;
    private int mPreviewMarginEnd;
    private int mPreviewMinHeight;
    private int mPreviewMinWidth;
    private int mPreviewPadding;
    private int mScaledTouchSlop;
    private int mScrollBarStyle;
    private boolean mScrollCompleted;
    private float mScrollY = 0.0f;
    private int mScrollbarPosition = -1;
    private SectionIndexer mSectionIndexer;
    private Object[] mSections;
    private boolean mShowingPreview;
    private boolean mShowingPrimary;
    private final Animator.AnimatorListener mSwitchPrimaryListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            mShowingPrimary = !mShowingPrimary;
        }
    };
    private int mState;
    private final Runnable mDeferHide = new Runnable() {
        public void run() {
            setState(EFFECT_STATE_CLOSE);
        }
    };
    private int mTextAppearance;
    private ColorStateList mTextColor;
    private float mTextSize;
    private Drawable mThumbDrawable;
    private int mThumbMarginEnd;
    private int mThumbMinHeight;
    private int mThumbMinWidth;
    private float mThumbOffset;
    private int mThumbPosition;
    private float mThumbRange;
    private Drawable mTrackDrawable;
    private int mTrackPadding;
    private boolean mUpdatingLayout;
    private int mWidth;


    public SeslRecyclerViewFastScroller(SeslRecyclerView recyclerView, int styleResId) {
        mRecyclerView = recyclerView;
        mOldItemCount = recyclerView.getAdapter().getItemCount();
        mOldChildCount = recyclerView.getChildCount();
        mContext = recyclerView.getContext();
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mScrollBarStyle = recyclerView.getScrollBarStyle();
        mScrollCompleted = true;
        mState = 1;
        mMatchDragPosition = mContext.getApplicationInfo().targetSdkVersion >= 11;
        mTrackImage = new ImageView(mContext);
        mTrackImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mThumbImage = new ImageView(mContext);
        mThumbImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mPreviewImage = new View(mContext);
        mPreviewImage.setAlpha(0.0f);
        mPrimaryText = createPreviewTextView(mContext);
        mSecondaryText = createPreviewTextView(mContext);
        setStyle(styleResId);
        ViewGroupOverlay overlay = recyclerView.getOverlay();
        mOverlay = overlay;
        overlay.add(mTrackImage);
        overlay.add(mThumbImage);
        overlay.add(mPreviewImage);
        overlay.add(mPrimaryText);
        overlay.add(mSecondaryText);
        mPreviewMarginEnd = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroll_preview_margin_end);
        mThumbMarginEnd = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroll_thumb_margin_end);
        mAdditionalTouchArea = mContext.getResources().getDimension(R.dimen.sesl_fast_scroll_additional_touch_area);
        mTrackPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroller_track_padding);
        mAdditionalBottomPadding = mContext.getResources().getDimensionPixelOffset(R.dimen.sesl_fast_scroller_additional_bottom_padding);
        mImmersiveBottomPadding = 0;
        mPrimaryText.setPadding(mPreviewPadding, 0, mPreviewPadding, 0);
        mSecondaryText.setPadding(mPreviewPadding, 0, mPreviewPadding, 0);
        getSectionsFromIndexer();
        updateLongList(mOldChildCount, mOldItemCount);
        setScrollbarPosition(recyclerView.getVerticalScrollbarPosition());
        postAutoHide();
    }

    private static Animator groupAnimatorOfFloat(Property<View, Float> property, float value, View... views) {
        AnimatorSet animSet = new AnimatorSet();
        AnimatorSet.Builder builder = null;
        for (int i = views.length - 1; i >= 0; i--) {
            Animator anim = ObjectAnimator.ofFloat(views[i], property, new float[]{value});
            if (builder == null) {
                builder = animSet.play(anim);
            } else {
                builder.with(anim);
            }
        }
        return animSet;
    }

    private static Animator animateScaleX(View v, float target) {
        return ObjectAnimator.ofFloat(v, View.SCALE_X, new float[]{target});
    }

    private static Animator animateAlpha(View v, float alpha) {
        return ObjectAnimator.ofFloat(v, View.ALPHA, new float[]{alpha});
    }

    private static Animator animateBounds(View v, Rect bounds) {
        return ObjectAnimator.ofPropertyValuesHolder(v, new PropertyValuesHolder[]{PropertyValuesHolder.ofInt(LEFT, new int[]{bounds.left}), PropertyValuesHolder.ofInt(TOP, new int[]{bounds.top}), PropertyValuesHolder.ofInt(RIGHT, new int[]{bounds.right}), PropertyValuesHolder.ofInt(BOTTOM, new int[]{bounds.bottom})});
    }

    /*kang from MathUtils.smali*/
    public static int constrain(int amount, int low, int high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    public static float constrain(float amount, float low, float high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    /*kang from SeslViewReflector$SeslMeasureSpecReflector.smali*/
    public static int makeSafeMeasureSpec(int size, int mode) {
        boolean useZeroUnspecifiedMeasureSpec = Build.VERSION.SDK_INT < 23;
        if (!useZeroUnspecifiedMeasureSpec || mode != 0) {
            return View.MeasureSpec.makeMeasureSpec(size, mode);
        }
        return 0;
    }

    private void updateAppearance() {
        int width = 0;

        TypedValue outValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorPrimary, outValue, true);
        mColorPrimary = mContext.getResources().getColor(outValue.resourceId, mContext.getTheme());

        mTrackImage.setImageDrawable(mTrackDrawable);
        if (mTrackDrawable != null) {
            width = Math.max(0, mTrackDrawable.getIntrinsicWidth());
        }

        if (mThumbDrawable != null) {
            mThumbDrawable.setTint(mColorPrimary);
        }
        mThumbImage.setImageDrawable(mThumbDrawable);
        mThumbImage.setMinimumWidth(mThumbMinWidth);
        mThumbImage.setMinimumHeight(mThumbMinHeight);
        if (mThumbDrawable != null) {
            width = Math.max(width, mThumbDrawable.getIntrinsicWidth());
        }

        mWidth = Math.max(width, mThumbMinWidth);
        mPreviewImage.setMinimumWidth(mPreviewMinWidth);
        mPreviewImage.setMinimumHeight(mPreviewMinHeight);

        if (mTextAppearance != 0) {
            mPrimaryText.setTextAppearance(mTextAppearance);
            mSecondaryText.setTextAppearance(mTextAppearance);
        }

        if (mTextColor != null) {
            mPrimaryText.setTextColor(mTextColor);
            mSecondaryText.setTextColor(mTextColor);
        }
        if (mTextSize > 0.0f) {
            mPrimaryText.setTextSize(0, mTextSize);
            mSecondaryText.setTextSize(0, mTextSize);
        }
        int textMinSize = Math.max(0, mPreviewMinHeight);
        mPrimaryText.setMinimumWidth(mPreviewMinWidth);
        mPrimaryText.setMinimumHeight(textMinSize);
        mPrimaryText.setIncludeFontPadding(false);
        mSecondaryText.setMinimumWidth(mPreviewMinWidth);
        mSecondaryText.setMinimumHeight(textMinSize);
        mSecondaryText.setIncludeFontPadding(false);

        mOrientation = mContext.getResources().getConfiguration().orientation;
        refreshDrawablePressedState();
    }

    public void setStyle(int resId) {
        TypedArray ta = mContext.obtainStyledAttributes(null, R.styleable.SeslRecyclerViewFastScroller, android.R.attr.fastScrollStyle, resId);
        int N = ta.getIndexCount();
        for (int i = 0; i < N; i++) {
            int index = ta.getIndex(i);
            if (index == R.styleable.SeslRecyclerViewFastScroller_position) {
                mOverlayPosition = ta.getInt(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_backgroundLeft) {
                mPreviewResId[0] = ta.getResourceId(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_backgroundRight) {
                mPreviewResId[1] = ta.getResourceId(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_thumbDrawable) {
                mThumbDrawable = ta.getDrawable(index);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_trackDrawable) {
                mTrackDrawable = ta.getDrawable(index);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_textAppearance) {
                mTextAppearance = ta.getResourceId(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_textColor) {
                mTextColor = ta.getColorStateList(index);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_textSize) {
                mTextSize = (float) ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_minWidth) {
                mPreviewMinWidth = ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_minHeight) {
                mPreviewMinHeight = ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_thumbMinWidth) {
                mThumbMinWidth = ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_thumbMinHeight) {
                mThumbMinHeight = ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_android_padding) {
                mPreviewPadding = ta.getDimensionPixelSize(index, 0);
            } else if (index == R.styleable.SeslRecyclerViewFastScroller_thumbPosition) {
                mThumbPosition = ta.getInt(index, 0);
            }
        }
        ta.recycle();
        updateAppearance();
    }

    public void remove() {
        mOverlay.remove(mTrackImage);
        mOverlay.remove(mThumbImage);
        mOverlay.remove(mPreviewImage);
        mOverlay.remove(mPrimaryText);
        mOverlay.remove(mSecondaryText);
    }

    public boolean isEnabled() {
        return mEnabled && (mLongList || mAlwaysShow);
    }

    public void setEnabled(boolean enabled) {
        Log.d(TAG, "setEnabled() enabled = " + enabled);
        if (mEnabled != enabled) {
            mEnabled = enabled;
            onStateDependencyChanged(true);
        }
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
        if (!isEnabled()) {
            stop();
        } else if (isAlwaysShowEnabled()) {
            setState(EFFECT_STATE_OPEN);
        } else if (mState == EFFECT_STATE_OPEN) {
            postAutoHide();
        } else if (peekIfEnabled) {
            setState(EFFECT_STATE_OPEN);
            postAutoHide();
        }
        resolvePadding(mRecyclerView);
    }

    public void setScrollBarStyle(int style) {
        if (mScrollBarStyle != style) {
            mScrollBarStyle = style;
            updateLayout();
        }
    }

    public void stop() {
        setState(EFFECT_STATE_CLOSE);
    }

    public void setImmersiveBottomPadding(int var1) {
        this.mImmersiveBottomPadding = var1;
        this.updateOffsetAndRange();
    }

    public void setScrollbarPosition(int position) {
        if (position == 0) {
            position = mRecyclerView.mLayout.getLayoutDirection() == 1 ? 1 : 2;
        }
        if (mScrollbarPosition != position) {
            mScrollbarPosition = position;
            mLayoutFromRight = position != 1;
            int[] iArr = mPreviewResId;
            mPreviewImage.setBackgroundResource(iArr[mLayoutFromRight ? PREVIEW_RIGHT : PREVIEW_LEFT]);
            mPreviewImage.getBackground().setTintMode(PorterDuff.Mode.MULTIPLY);
            mPreviewImage.getBackground().setTint(mColorPrimary);
            updateLayout();
        }
    }

    public int getWidth() {
        return mWidth;
    }

    int getEffectState() {
        return mEffectState;
    }

    float getScrollY() {
        return mScrollY;
    }

    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateLayout();
    }

    public void onItemCountChanged(int childCount, int itemCount) {
        if (mOldItemCount != itemCount || mOldChildCount != childCount) {
            mOldItemCount = itemCount;
            mOldChildCount = childCount;
            if ((itemCount - childCount > 0) && mState != 2) {
                setThumbPos(getPosFromItemCount(mRecyclerView.findFirstVisibleItemPosition(), childCount, itemCount));
            }
            updateLongList(childCount, itemCount);
        }
    }

    private void updateLongList(int childCount, int itemCount) {
        boolean longList = childCount > 0 && (canScrollList(1) || canScrollList(-1));
        if (mLongList != longList) {
            mLongList = longList;
            onStateDependencyChanged(true);
        }
    }

    private TextView createPreviewTextView(Context context) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView textView = new TextView(context);
        textView.setLayoutParams(params);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        textView.setGravity(17);
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
        Rect bounds = mTempBounds;
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
        view.setPivotX(mLayoutFromRight ? (float) (bounds.right - bounds.left) : 0.0f);
    }

    private void measurePreview(View v, Rect out) {
        Rect margins = mTempMargins;
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
        int marginLeft, marginRight;
        int maxWidth;
        int left;
        int right;

        if (mLayoutFromRight) {
            if (adjacent == null) {
                marginRight = mThumbMarginEnd;
                marginLeft = 0;
            } else {
                marginRight = mPreviewMarginEnd;
                marginLeft = 0;
            }
        } else if (adjacent == null) {
            marginLeft = mThumbMarginEnd;
            marginRight = 0;
        } else {
            marginLeft = mPreviewMarginEnd;
            marginRight = 0;
        }

        Rect container = mContainerRect;
        int containerWidth = container.width();
        if (adjacent == null) {
            maxWidth = containerWidth;
        } else if (mLayoutFromRight) {
            maxWidth = adjacent.getLeft();
        } else {
            maxWidth = containerWidth - adjacent.getRight();
        }

        int adjMaxHeight = Math.max(0, container.height());
        int adjMaxWidth = Math.max(0, (maxWidth - marginLeft) - marginRight);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(adjMaxWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = makeSafeMeasureSpec(View.MeasureSpec.getSize(adjMaxHeight), 0);
        view.measure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(adjMaxWidth, view.getMeasuredWidth());
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
        int marginLeft, marginTop, marginRight;

        if (margins == null) {
            marginLeft = 0;
            marginTop = 0;
            marginRight = 0;
        } else {
            marginLeft = margins.left;
            marginTop = margins.top;
            marginRight = margins.right;
        }

        Rect container = mContainerRect;
        int containerWidth = container.width();
        View view = preview;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, (containerWidth - marginLeft) - marginRight), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = makeSafeMeasureSpec(View.MeasureSpec.getSize(Math.max(0, container.height())), 0);
        view.measure(widthMeasureSpec, heightMeasureSpec);

        int containerHeight = container.height();
        int width = preview.getMeasuredWidth();
        int top = (containerHeight / 10) + marginTop + container.top;
        int left = ((containerWidth - width) / 2) + container.left;
        Rect rect = out;
        rect.set(left, top, left + width, top + preview.getMeasuredHeight());
    }

    private void updateContainerRect() {
        SeslRecyclerView list = mRecyclerView;
        resolvePadding(mRecyclerView);
        Rect container = mContainerRect;

        container.left = 0;
        container.top = 0;
        container.right = list.getWidth();
        container.bottom = list.getHeight();

        int scrollbarStyle = mScrollBarStyle;
        if (scrollbarStyle == 0x1000000 || scrollbarStyle == 0) {
            container.left += list.getPaddingLeft();
            container.top += list.getPaddingTop();
            container.right -= list.getPaddingRight();
            container.bottom -= list.getPaddingBottom();
            if (scrollbarStyle == 0x1000000) {
                int width = getWidth();
                if (mScrollbarPosition == 2) {
                    container.right += width;
                } else {
                    container.left -= width;
                }
            }
        }
    }

    private void layoutThumb() {
        Rect bounds = mTempBounds;
        measureViewToSide(mThumbImage, null, null, bounds);
        applyLayout(mThumbImage, bounds);
    }

    private void layoutTrack() {
        int top;
        int bottom;
        View track = mTrackImage;
        View thumb = mThumbImage;
        Rect container = mContainerRect;

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(0, container.width()), View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = makeSafeMeasureSpec(View.MeasureSpec.getSize(Math.max(0, container.height())), 0);
        track.measure(widthMeasureSpec, heightMeasureSpec);

        if (mThumbPosition == 1) {
            top = container.top + mTrackPadding;
            bottom = (container.bottom - mTrackPadding) - mAdditionalBottomPadding;
        } else {
            int thumbHalfHeight = thumb.getHeight() / 2;
            top = container.top + thumbHalfHeight + mTrackPadding;
            bottom = ((container.bottom - thumbHalfHeight) - mTrackPadding) - mAdditionalBottomPadding;
        }
        int trackWidth = track.getMeasuredWidth();
        int left = thumb.getLeft() + ((thumb.getWidth() - trackWidth) / 2);
        track.layout(left, top, left + trackWidth, bottom);
    }

    private void updateOffsetAndRange() {
        float min;
        float max;
        View trackImage = mTrackImage;
        View thumbImage = mThumbImage;
        if (mThumbPosition == 1) {
            float halfThumbHeight = ((float) thumbImage.getHeight()) / 2.0f;
            min = ((float) trackImage.getTop()) + halfThumbHeight;
            max = ((float) trackImage.getBottom()) - halfThumbHeight;
        } else {
            min = (float) trackImage.getTop();
            max = (float) trackImage.getBottom();
        }
        mThumbOffset = min;
        mThumbRange = max - min - (float) mImmersiveBottomPadding;
        if (mThumbRange < 0.0F) {
            mThumbRange = 0.0F;
        }
    }

    private void setState(int state) {
        mRecyclerView.removeCallbacks(mDeferHide);
        if (mAlwaysShow && state == STATE_NONE) {
            state = STATE_VISIBLE;
        }
        if (state != mState) {
            switch (state) {
                case STATE_NONE:
                    transitionToHidden();
                    break;
                case STATE_VISIBLE:
                    transitionToVisible();
                    break;
                case STATE_DRAGGING:
                    transitionPreviewLayout(mCurrentSection);
                    break;
            }
            mState = state;
            refreshDrawablePressedState();
        }
    }

    private void refreshDrawablePressedState() {
        boolean isPressed = mState == STATE_DRAGGING;
        mThumbImage.setPressed(isPressed);
        mTrackImage.setPressed(isPressed);
    }

    private void transitionToHidden() {
        Log.d(TAG, "transitionToHidden() mState = " + mState);
        int duration = DURATION_CROSS_FADE;
        mShowingPreview = false;
        mCurrentSection = -1;
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
            duration = DURATION_FADE_OUT;
        }
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, mThumbImage, mTrackImage, mPreviewImage, mPrimaryText, mSecondaryText).setDuration((long) duration);
        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(new Animator[]{fadeOut});
        mDecorAnimation.setInterpolator(new PathInterpolator(0.33f, 0.0f, 0.3f, 1.0f));
        mDecorAnimation.start();
    }

    private void transitionToVisible() {
        Log.d(TAG, "transitionToVisible()");
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }
        Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1.0f, mThumbImage, mTrackImage).setDuration(DURATION_FADE_IN);
        Animator fadeOut = groupAnimatorOfFloat(View.ALPHA, 0.0f, mPreviewImage, mPrimaryText, mSecondaryText).setDuration(DURATION_FADE_IN);
        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(new Animator[]{fadeIn, fadeOut});
        mDecorAnimation.setInterpolator(new PathInterpolator(0.33f, 0.0f, 0.3f, 1.0f));
        mShowingPreview = false;
        mDecorAnimation.start();
    }

    private void transitionToDragging() {
        Log.d(TAG, "transitionToDragging()");
        if (mDecorAnimation != null) {
            mDecorAnimation.cancel();
        }
        Animator fadeIn = groupAnimatorOfFloat(View.ALPHA, 1.0f, mThumbImage, mTrackImage, mPreviewImage).setDuration(DURATION_FADE_IN);
        mDecorAnimation = new AnimatorSet();
        mDecorAnimation.playTogether(new Animator[]{fadeIn});
        mDecorAnimation.setInterpolator(new PathInterpolator(0.33f, 0.0f, 0.3f, 1.0f));
        mDecorAnimation.start();
        mShowingPreview = true;
    }

    private void postAutoHide() {
        mRecyclerView.removeCallbacks(mDeferHide);
        mRecyclerView.postDelayed(mDeferHide, FADE_TIMEOUT);
    }

    public boolean canScrollList(int direction) {
        int childCount = mRecyclerView.getChildCount();
        if (childCount == 0) {
            return false;
        }

        int firstPosition = mRecyclerView.findFirstVisibleItemPosition();
        Rect listPadding = mRecyclerView.mListPadding;

        if (direction > 0) {
            int lastBottom = mRecyclerView.getChildAt(childCount - 1).getBottom();
            if (firstPosition + childCount < mRecyclerView.getAdapter().getItemCount() || lastBottom > mRecyclerView.getHeight() - listPadding.bottom) {
                return true;
            }
            return false;
        }

        int firstTop = mRecyclerView.getChildAt(0).getTop();
        if (firstPosition > 0 || firstTop < listPadding.top) {
            return true;
        }

        return false;
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!isEnabled()) {
            setState(STATE_NONE);
            return;
        }
        if ((canScrollList(1) || canScrollList(-1)) && mState != STATE_DRAGGING) {
            if (mOldThumbPosition != -1.0f) {
                setThumbPos(mOldThumbPosition);
                mOldThumbPosition = -1.0f;
            } else {
                setThumbPos(getPosFromItemCount(firstVisibleItem, visibleItemCount, totalItemCount));
            }
        }
        mScrollCompleted = true;
        if (mFirstVisibleItem != firstVisibleItem) {
            mFirstVisibleItem = firstVisibleItem;
            if (mState != STATE_DRAGGING) {
                setState(STATE_VISIBLE);
                postAutoHide();
            }
        }
    }

    private void getSectionsFromIndexer() {
        mSectionIndexer = null;
        SeslRecyclerView.Adapter adapter = mRecyclerView.getAdapter();
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
        int sectionCount, targetIndex, sectionIndex;

        mScrollCompleted = false;
        int count = mRecyclerView.getAdapter().getItemCount();
        Object[] sections = mSections;
        if (sections == null) {
            sectionCount = 0;
        } else {
            sectionCount = sections.length;
        }
        if (sections == null || sectionCount <= 0) {
            targetIndex = constrain((int) (((float) count) * position), 0, count - 1);
            sectionIndex = -1;
        } else {
            int exactSection = constrain((int) (((float) sectionCount) * position), 0, sectionCount - 1);
            int targetSection = exactSection;
            targetIndex = mSectionIndexer.getPositionForSection(targetSection);
            sectionIndex = targetSection;
            int nextIndex = count;
            int prevIndex = targetIndex;
            int prevSection = targetSection;
            int nextSection = targetSection + 1;
            if (targetSection < sectionCount - 1) {
                nextIndex = mSectionIndexer.getPositionForSection(targetSection + 1);
            }
            if (nextIndex == targetIndex) {
                while (true) {
                    if (targetSection <= 0) {
                        break;
                    }
                    targetSection--;
                    prevIndex = mSectionIndexer.getPositionForSection(targetSection);
                    if (prevIndex == targetIndex) {
                        if (targetSection == 0) {
                            sectionIndex = 0;
                            break;
                        }
                    } else {
                        prevSection = targetSection;
                        sectionIndex = targetSection;
                        break;
                    }
                }
            }
            int nextNextSection = nextSection + 1;
            while (nextNextSection < sectionCount && mSectionIndexer.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }
            float prevPosition = ((float) prevSection) / ((float) sectionCount);
            float nextPosition = ((float) nextSection) / ((float) sectionCount);
            float snapThreshold = count == 0 ? Float.MAX_VALUE : 0.125f / ((float) count);
            if (prevSection != exactSection || position - prevPosition >= snapThreshold) {
                targetIndex = prevIndex + ((int) ((((float) (nextIndex - prevIndex)) * (position - prevPosition)) / (nextPosition - prevPosition)));
            } else {
                targetIndex = prevIndex;
            }
            targetIndex = constrain(targetIndex, 0, count - 1);
        }

        if (mRecyclerView.mLayout instanceof SeslLinearLayoutManager) {
            ((SeslLinearLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(mHeaderCount + targetIndex, 0);
        } else {
            ((StaggeredGridLayoutManager) mRecyclerView.mLayout).scrollToPositionWithOffset(mHeaderCount + targetIndex, 0);
        }
        onScroll(mRecyclerView.findFirstVisibleItemPosition(), mRecyclerView.getChildCount(), mRecyclerView.getAdapter().getItemCount());
        mCurrentSection = sectionIndex;
        boolean hasPreview = transitionPreviewLayout(sectionIndex);
        Log.d(TAG, "scrollTo() called transitionPreviewLayout() sectionIndex =" + sectionIndex + ", position = " + position);
        if (!mShowingPreview && hasPreview) {
            transitionToDragging();
        } else if (mShowingPreview && !hasPreview) {
            transitionToVisible();
        }
    }

    private boolean transitionPreviewLayout(int sectionIndex) {
        Object[] sections = mSections;
        String text = null;
        if (sections != null && sectionIndex >= 0 && sectionIndex < sections.length) {
            Object section = sections[sectionIndex];
            if (section != null) {
                text = section.toString();
            }
        }

        Rect bounds = mTempBounds;
        View preview = mPreviewImage;
        TextView showing;
        TextView target;
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
        } else if (mState == STATE_DRAGGING && target.getText() == showing.getText()) {
            return !TextUtils.isEmpty(text);
        }

        if (mPreviewAnimation != null) {
            mPreviewAnimation.cancel();
        }
        Animator showTarget = animateAlpha(target, 1.0f).setDuration(TAP_TIMEOUT);
        Animator hideShowing = animateAlpha(showing, 0.0f).setDuration(TAP_TIMEOUT);
        hideShowing.addListener(mSwitchPrimaryListener);
        bounds.left -= preview.getPaddingLeft();
        bounds.top -= preview.getPaddingTop();
        bounds.right += preview.getPaddingRight();
        bounds.bottom += preview.getPaddingBottom();
        Animator resizePreview = animateBounds(preview, bounds);
        resizePreview.setDuration(DURATION_RESIZE);
        mPreviewAnimation = new AnimatorSet();
        AnimatorSet.Builder builder = mPreviewAnimation.play(hideShowing).with(showTarget);
        builder.with(resizePreview);

        int previewWidth = (preview.getWidth() - preview.getPaddingLeft()) - preview.getPaddingRight();
        int targetWidth = target.getWidth();
        if (targetWidth > previewWidth) {
            target.setScaleX(((float) previewWidth) / ((float) targetWidth));
            builder.with(animateScaleX(target, 1.0f).setDuration(DURATION_RESIZE));
        } else {
            target.setScaleX(1.0f);
        }
        int showingWidth = showing.getWidth();
        if (showingWidth > targetWidth) {
            builder.with(animateScaleX(showing, ((float) targetWidth) / ((float) showingWidth)).setDuration(DURATION_RESIZE));
        }
        mPreviewAnimation.setInterpolator(new PathInterpolator(0.33f, 0.0f, 0.3f, 1.0f));
        mPreviewAnimation.start();

        return !TextUtils.isEmpty(text);
    }

    private void setThumbPos(float position) {
        Rect container = mContainerRect;
        int top = container.top;
        int bottom = container.bottom;
        if (position > 1.0f) {
            position = 1.0f;
        } else if (position < 0.0f) {
            position = 0.0f;
        }
        float thumbMiddle = (mThumbRange * position) + mThumbOffset;
        mThumbImage.setTranslationY(thumbMiddle - (((float) mThumbImage.getHeight()) / 2.0f));
        View previewImage = mPreviewImage;
        float previewHalfHeight = ((float) previewImage.getHeight()) / 2.0f;
        float previewTop = constrain(thumbMiddle, ((float) top) + previewHalfHeight, ((float) bottom) - previewHalfHeight) - previewHalfHeight;
        previewImage.setTranslationY(previewTop);
        mPrimaryText.setTranslationY(previewTop);
        mSecondaryText.setTranslationY(previewTop);
    }

    private float getPosFromMotionEvent(float y) {
        if (mThumbRange <= 0.0f) {
            return 0.0f;
        }
        return constrain((y - mThumbOffset) / mThumbRange, 0.0f, 1.0f);
    }

    private float getPosFromItemCount(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        float incrementalPos, posWithinSection;
        int positionsInSection, maxSize, currentVisibleSize, nextSectionPos;

        SectionIndexer sectionIndexer = mSectionIndexer;
        if (sectionIndexer == null || mListAdapter == null) {
            getSectionsFromIndexer();
        }

        if (visibleItemCount == 0 || totalItemCount == 0) {
            return 0.0f;
        }

        if ((sectionIndexer != null && mSections != null && mSections.length > 0) && mMatchDragPosition) {
            firstVisibleItem = firstVisibleItem - mHeaderCount;
            if (firstVisibleItem < 0) {
                return 0.0f;
            }

            totalItemCount = totalItemCount - mHeaderCount;
            View child = mRecyclerView.getChildAt(0);
            if (child == null || child.getHeight() == 0) {
                incrementalPos = 0.0f;
            } else {
                incrementalPos = ((float) (mRecyclerView.getPaddingTop() - child.getTop())) / ((float) child.getHeight());
            }

            int section = sectionIndexer.getSectionForPosition(firstVisibleItem);
            int sectionPos = sectionIndexer.getPositionForSection(section);
            int sectionCount = mSections.length;
            if (section < sectionCount - 1) {
                if (section + 1 < sectionCount) {
                    nextSectionPos = sectionIndexer.getPositionForSection(section + 1);
                } else {
                    nextSectionPos = totalItemCount - 1;
                }
                positionsInSection = nextSectionPos - sectionPos;
            } else {
                positionsInSection = totalItemCount - sectionPos;
            }
            if (positionsInSection == 0) {
                posWithinSection = 0.0f;
            } else {
                posWithinSection = ((((float) firstVisibleItem) + incrementalPos) - ((float) sectionPos)) / ((float) positionsInSection);
            }

            float result = (((float) section) + posWithinSection) / ((float) sectionCount);
            if (firstVisibleItem <= 0 || firstVisibleItem + visibleItemCount != totalItemCount) {
                return result;
            }

            View lastChild = mRecyclerView.getChildAt(visibleItemCount - 1);
            int bottomPadding = mRecyclerView.getPaddingBottom();
            if (mRecyclerView.getClipToPadding()) {
                maxSize = lastChild.getHeight();
                currentVisibleSize = (mRecyclerView.getHeight() - bottomPadding) - lastChild.getTop();
            } else {
                maxSize = lastChild.getHeight() + bottomPadding;
                currentVisibleSize = mRecyclerView.getHeight() - lastChild.getTop();
            }
            if (currentVisibleSize <= 0 || maxSize <= 0) {
                return result;
            }

            return result + ((1.0f - result) * (((float) currentVisibleSize) / ((float) maxSize)));
        } else if (visibleItemCount != totalItemCount) {
            return ((float) firstVisibleItem) / ((float) (totalItemCount - visibleItemCount));
        } else {
            if ((mRecyclerView.mLayout instanceof StaggeredGridLayoutManager) && firstVisibleItem != 0) {
                View view = mRecyclerView.getChildAt(0);
                if (view != null && ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).isFullSpan()) {
                    return 1.0f;
                }
            }
            return 0.0f;
        }
    }

    private void cancelFling() {
        MotionEvent cancelFling = MotionEvent.obtain(TAP_TIMEOUT, TAP_TIMEOUT, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
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
        Log.d(TAG, "beginDrag() !!!");
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
                Log.d(TAG, "onInterceptTouchEvent() ACTION_DOWN ev.getY() = " + ev.getY());
                if (!isPointInside(ev.getX(), ev.getY())) {
                    return false;
                }
                if (!mRecyclerView.isInScrollingContainer()) {
                    return true;
                }
                mInitialTouchY = ev.getY();
                startPendingDrag();
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelPendingDrag();
                return false;
            case MotionEvent.ACTION_MOVE:
                if (!isPointInside(ev.getX(), ev.getY())) {
                    cancelPendingDrag();
                    return false;
                } else if (mPendingDrag < TAP_TIMEOUT || mPendingDrag > SystemClock.uptimeMillis()) {
                    return false;
                } else {
                    beginDrag();
                    float pos = getPosFromMotionEvent(mInitialTouchY);
                    mOldThumbPosition = pos;
                    scrollTo(pos);
                    Log.d(TAG, "onInterceptTouchEvent() ACTION_MOVE pendingdrag open()");
                    return onTouchEvent(ev);
                }
            default:
                return false;
        }
    }

    public boolean onInterceptHoverEvent(MotionEvent ev) {
        if (isEnabled()) {
            int actionMasked = ev.getActionMasked();
            if ((actionMasked == MotionEvent.ACTION_HOVER_ENTER || actionMasked == MotionEvent.ACTION_HOVER_MOVE) && mState == STATE_NONE && isPointInside(ev.getX(), ev.getY())) {
                setState(STATE_VISIBLE);
                postAutoHide();
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent me) {
        Rect container = mContainerRect;
        int top = container.top;
        int bottom = container.bottom;
        View trackImage = mTrackImage;
        float min = (float) trackImage.getTop();
        float max = (float) trackImage.getBottom();
        mScrollY = me.getY();

        if (!isEnabled()) {
            return false;
        }

        switch (me.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isPointInside(me.getX(), me.getY()) && !mRecyclerView.isInScrollingContainer()) {
                    beginDrag();
                    mEffectState = EFFECT_STATE_OPEN;
                    Log.d(TAG, "onTouchEvent() ACTION_DOWN.. open() called with posY " + me.getY());
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (mPendingDrag >= TAP_TIMEOUT) {
                    beginDrag();
                    float pos = getPosFromMotionEvent(me.getY());
                    mOldThumbPosition = pos;
                    setThumbPos(pos);
                    scrollTo(pos);
                    mEffectState = EFFECT_STATE_OPEN;
                    Log.d(TAG, "onTouchEvent() ACTION_UP.. open() called with posY " + me.getY());
                }
                if (mState == STATE_DRAGGING) {
                    mRecyclerView.requestDisallowInterceptTouchEvent(false);
                    setState(STATE_NONE);
                    postAutoHide();
                    mEffectState = EFFECT_STATE_CLOSE;
                    mScrollY = 0.0f;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent() ACTION_MOVE.. mState= " + mState + ", mInitialTouchY=" + mInitialTouchY);
                if (mPendingDrag >= TAP_TIMEOUT && Math.abs(me.getY() - mInitialTouchY) > ((float) mScaledTouchSlop)) {
                    beginDrag();
                    if (mScrollY > ((float) top) && mScrollY < ((float) bottom)) {
                        Log.d(TAG, "onTouchEvent() ACTION_MOVE 1 mScrollY=" + mScrollY + ", min=" + min + ", max=" + max);
                        if (mScrollY < ((float) top) + min) {
                            mScrollY = ((float) top) + min;
                        } else if (mScrollY > max) {
                            mScrollY = max;
                        }
                        mEffectState = EFFECT_STATE_OPEN;
                    }
                }
                if (mState == STATE_DRAGGING) {
                    float pos = getPosFromMotionEvent(me.getY());
                    mOldThumbPosition = pos;
                    setThumbPos(pos);
                    if (mScrollCompleted) {
                        scrollTo(pos);
                    }
                    if (mScrollY > ((float) top) && mScrollY < ((float) bottom)) {
                        Log.d(TAG, "onTouchEvent() ACTION_MOVE 2 mScrollY=" + mScrollY + ", min=" + min + ", max=" + max);
                        if (mScrollY < ((float) top) + min) {
                            mScrollY = ((float) top) + min;
                        } else if (mScrollY > max) {
                            mScrollY = max;
                        }
                        mEffectState = EFFECT_STATE_OPEN;
                    }
                    return true;
                }
                break;
            case 3:
                cancelPendingDrag();
                if (mState == STATE_DRAGGING) {
                    setState(STATE_NONE);
                }
                mEffectState = EFFECT_STATE_CLOSE;
                mScrollY = 0.0f;
                break;
        }

        return false;
    }

    private boolean isPointInside(float x, float y) {
        return isPointInsideX(x) && isPointInsideY(y) && mState != STATE_NONE;
    }

    private boolean isPointInsideX(float x) {
        if (mLayoutFromRight) {
            if (x >= ((float) mThumbImage.getLeft()) - mAdditionalTouchArea) {
                return true;
            }
            return false;
        } else if (x > ((float) mThumbImage.getRight()) + mAdditionalTouchArea) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isPointInsideY(float y) {
        float offset = mThumbImage.getTranslationY();
        return y >= ((float) mThumbImage.getTop()) + offset && y <= ((float) mThumbImage.getBottom()) + offset;
    }

    private void resolvePadding(ViewGroup viewGroup) {
        ReflectUtils.genericInvokeMethod(viewGroup, "resolvePadding");
    }
}
