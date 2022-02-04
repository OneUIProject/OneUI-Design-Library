package de.dlyt.yanndroid.oneui.sesl.indexscroll;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.appcompat.animation.SeslAnimationUtils;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import androidx.reflect.view.SeslHapticFeedbackConstantsReflector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.LinearLayoutManager;
import de.dlyt.yanndroid.oneui.sesl.recyclerview.StaggeredGridLayoutManager;
import de.dlyt.yanndroid.oneui.view.RecyclerView;

public class SeslIndexScrollView extends FrameLayout {
    public static final int GRAVITY_INDEX_BAR_LEFT = 0;
    public static final int GRAVITY_INDEX_BAR_RIGHT = 1;
    private static final float OUT_OF_BOUNDARY = -9999.0f;
    float mA11yDownPosY = -1.0f;
    int mA11yTargetIndex = -1;
    private Context mContext;
    private String mCurrentIndex;
    private boolean mHasOverlayChild = false;
    private int mIndexBarGravity = 1;
    IndexScroll mIndexScroll;
    private IndexScrollPreview mIndexScrollPreview;
    private SeslAbsIndexer mIndexer;
    private final IndexerObserver mIndexerObserver = new IndexerObserver();
    private boolean mIsSimpleIndexScroll = false;
    boolean mNeedToHandleA11yEvent = false;
    private OnIndexBarEventListener mOnIndexBarEventListener = null;
    private final Runnable mPreviewDelayRunnable = new Runnable() {
        /* class androidx.indexscroll.widget.SeslIndexScrollView.AnonymousClass2 */

        public void run() {
            if (SeslIndexScrollView.this.mIndexScrollPreview != null) {
                SeslIndexScrollView.this.mIndexScrollPreview.fadeOutAnimation();
            }
        }
    };
    private boolean mRegisteredDataSetObserver = false;
    private Typeface mSECRobotoLightRegularFont;
    private long mStartTouchDown = 0;
    private IndexScrollTouchHelper mTouchHelper;
    private float mTouchY = OUT_OF_BOUNDARY;
    private ViewGroupOverlay mViewGroupOverlay;

    @Retention(RetentionPolicy.SOURCE)
    public @interface GravityIndexBar {
    }

    public interface OnIndexBarEventListener {
        void onIndexChanged(int i);

        void onPressed(float f);

        void onReleased(float f);
    }

    public SeslIndexScrollView(Context context) {
        super(context);
        this.mContext = context;
        this.mCurrentIndex = null;
        init();
    }

    public SeslIndexScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mIndexBarGravity = 1;
        init();
    }

    private void init() {
        this.mViewGroupOverlay = getOverlay();
        if (this.mIndexScrollPreview == null) {
            IndexScrollPreview indexScrollPreview = new IndexScrollPreview(this.mContext);
            this.mIndexScrollPreview = indexScrollPreview;
            indexScrollPreview.setLayout(0, 0, getWidth(), getHeight());
            this.mViewGroupOverlay.add(this.mIndexScrollPreview);
        }
        IndexScrollTouchHelper indexScrollTouchHelper = new IndexScrollTouchHelper(this);
        this.mTouchHelper = indexScrollTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, indexScrollTouchHelper);
        this.mHasOverlayChild = true;
        this.mIndexScroll = new IndexScroll(this.mContext, getHeight(), getWidth(), this.mIndexBarGravity);
    }

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (isTalkBackIsRunning() && this.mIndexScroll.mRecyclerView != null) {
            if (this.mTouchHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent)) {
                z = true;
            }
            this.mNeedToHandleA11yEvent = z;
            if (!z) {
                this.mA11yDownPosY = -1.0f;
                this.mA11yTargetIndex = -1;
            }
        }
        return z;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mTouchHelper.dispatchKeyEvent(keyEvent) || super.dispatchKeyEvent(keyEvent);
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        this.mTouchHelper.onFocusChanged(z, i, rect);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        IndexScrollPreview indexScrollPreview;
        super.dispatchDraw(canvas);
        IndexScroll indexScroll = this.mIndexScroll;
        if (indexScroll != null) {
            indexScroll.setDimensions(getWidth(), getHeight());
            String str = this.mCurrentIndex;
            if (!(str == null || str.length() == 0 || (indexScrollPreview = this.mIndexScrollPreview) == null)) {
                indexScrollPreview.setLayout(0, 0, getWidth(), getHeight());
                this.mIndexScrollPreview.invalidate();
            }
            IndexScroll indexScroll2 = this.mIndexScroll;
            if (indexScroll2 != null && indexScroll2.isAlphabetInit()) {
                this.mIndexScroll.draw(canvas);
            }
        }
    }

    public void setIndexer(SeslArrayIndexer seslArrayIndexer) {
        if (seslArrayIndexer != null) {
            setAbsIndexer(seslArrayIndexer);
            return;
        }
        throw new IllegalArgumentException("SeslIndexView.setIndexer(indexer) : indexer=null.");
    }

    public void setIndexer(SeslCursorIndexer seslCursorIndexer) {
        if (seslCursorIndexer == null) {
            throw new IllegalArgumentException("SeslIndexView.setIndexer(indexer) : indexer=null.");
        } else if (seslCursorIndexer.isInitialized()) {
            setAbsIndexer(seslCursorIndexer);
        } else {
            throw new IllegalArgumentException("The indexer was not initialized before setIndexer api call. It is necessary to check if the items being applied to the indexer is normal.");
        }
    }

    private void setAbsIndexer(SeslAbsIndexer seslAbsIndexer) {
        SeslAbsIndexer seslAbsIndexer2 = this.mIndexer;
        if (seslAbsIndexer2 != null && this.mRegisteredDataSetObserver) {
            this.mRegisteredDataSetObserver = false;
            seslAbsIndexer2.unregisterDataSetObserver(this.mIndexerObserver);
        }
        this.mIsSimpleIndexScroll = false;
        this.mIndexer = seslAbsIndexer;
        this.mRegisteredDataSetObserver = true;
        seslAbsIndexer.registerDataSetObserver(this.mIndexerObserver);
        if (this.mIndexScroll.mScrollThumbBgDrawable != null) {
            this.mIndexScroll.mScrollThumbBgDrawable.setColorFilter(this.mIndexScroll.mThumbColor, PorterDuff.Mode.MULTIPLY);
        }
        this.mIndexer.cacheIndexInfo();
        this.mIndexScroll.setAlphabetArray(this.mIndexer.getAlphabetArray());
    }

    public void setSimpleIndexScroll(String[] strArr, int i) {
        if (strArr != null) {
            this.mIsSimpleIndexScroll = true;
            setSimpleIndexWidth((int) this.mContext.getResources().getDimension(R.dimen.sesl_indexbar_simple_index_width));
            if (i != 0) {
                setSimpleIndexWidth(i);
            }
            if (this.mIndexScroll.mScrollThumbBgDrawable != null) {
                this.mIndexScroll.mScrollThumbBgDrawable.setColorFilter(this.mIndexScroll.mThumbColor, PorterDuff.Mode.MULTIPLY);
            }
            this.mIndexScroll.setAlphabetArray(strArr);
            return;
        }
        throw new IllegalArgumentException("SeslIndexView.setSimpleIndexScroll(indexBarChar) ");
    }

    private void setSimpleIndexWidth(int i) {
        IndexScroll indexScroll = this.mIndexScroll;
        if (indexScroll != null) {
            indexScroll.setSimpleIndexScrollWidth(i);
        }
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        if (this.mIndexScroll.mRecyclerView != recyclerView && recyclerView != null) {
            if (this.mIndexScroll.mRecyclerView != null) {
                this.mIndexScroll.mRecyclerView.removeOnScrollListener(this.mIndexScroll.mScrollListener);
            }
            this.mIndexScroll.mRecyclerView = recyclerView;
            IndexScroll indexScroll = this.mIndexScroll;
            indexScroll.mLayout = indexScroll.mRecyclerView.getLayoutManager();
            this.mIndexScroll.mRecyclerView.addOnScrollListener(this.mIndexScroll.mScrollListener);
            this.mIndexScroll.mCurItemPosition = -1;
            enableScrollThumb(true);
            this.mTouchHelper.updateId(recyclerView.getId());
        }
    }

    public void enableScrollThumb(boolean z) {
        IndexScroll indexScroll = this.mIndexScroll;
        if (indexScroll != null) {
            indexScroll.mEnableScrollThumb = z;
            if (!z) {
                this.mIndexScroll.changeThumbAlpha(0);
            }
        }
    }

    public void setIndexBarTextMode(boolean z) {
        IndexScroll indexScroll = this.mIndexScroll;
        if (indexScroll != null) {
            indexScroll.mEnableTextMode = z;
            if (z) {
                this.mIndexScroll.mBgDrawableDefault = getResources().getDrawable(R.drawable.sesl_index_bar_textmode_bg, this.mContext.getTheme());
                this.mIndexScroll.mBgRectWidth = (int) getResources().getDimension(R.dimen.sesl_indexbar_textmode_width);
                this.mIndexScroll.mScrollThumbBgDrawable = getResources().getDrawable(R.drawable.sesl_index_bar_textmode_thumb_shape, this.mContext.getTheme());
            } else {
                this.mIndexScroll.mBgDrawableDefault = getResources().getDrawable(R.drawable.sesl_index_bar_bg, this.mContext.getTheme());
                this.mIndexScroll.mBgRectWidth = (int) getResources().getDimension(R.dimen.sesl_indexbar_width);
                this.mIndexScroll.mScrollThumbBgDrawable = getResources().getDrawable(R.drawable.sesl_index_bar_thumb_shape, this.mContext.getTheme());
            }
            this.mIndexScroll.mScrollThumbBgDrawable.setColorFilter(this.mIndexScroll.mThumbColor, PorterDuff.Mode.MULTIPLY);
            this.mIndexScroll.mBgDrawableDefault.setColorFilter(this.mIndexScroll.mBgTintColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mHasOverlayChild) {
            this.mViewGroupOverlay.remove(this.mIndexScrollPreview);
            this.mHasOverlayChild = false;
        }
        SeslAbsIndexer seslAbsIndexer = this.mIndexer;
        if (seslAbsIndexer != null && this.mRegisteredDataSetObserver) {
            this.mRegisteredDataSetObserver = false;
            seslAbsIndexer.unregisterDataSetObserver(this.mIndexerObserver);
        }
        Runnable runnable = this.mPreviewDelayRunnable;
        if (runnable != null) {
            removeCallbacks(runnable);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!this.mHasOverlayChild) {
            this.mViewGroupOverlay.add(this.mIndexScrollPreview);
            this.mHasOverlayChild = true;
        }
        SeslAbsIndexer seslAbsIndexer = this.mIndexer;
        if (seslAbsIndexer != null && !this.mRegisteredDataSetObserver) {
            this.mRegisteredDataSetObserver = true;
            seslAbsIndexer.registerDataSetObserver(this.mIndexerObserver);
        }
    }

    public void setIndexBarBackgroundDrawable(Drawable drawable) {
        this.mIndexScroll.mBgDrawableDefault = drawable;
    }

    public void setIndexBarBackgroundColor(int i) {
        this.mIndexScroll.mBgDrawableDefault.setColorFilter(i, PorterDuff.Mode.MULTIPLY);
    }

    public void setIndexBarTextColor(int i) {
        this.mIndexScroll.mTextColorDimmed = i;
    }

    public void setIndexBarPressedTextColor(int i) {
        this.mIndexScroll.mScrollThumbBgDrawable.setColorFilter(i, PorterDuff.Mode.MULTIPLY);
        this.mIndexScroll.mThumbColor = i;
    }

    public void setEffectTextColor(int i) {
        this.mIndexScrollPreview.setTextColor(i);
    }

    public void setEffectBackgroundColor(int i) {
        this.mIndexScrollPreview.setBackgroundColor(this.mIndexScroll.getColorWithAlpha(i, 0.8f));
    }

    public void setIndexBarGravity(int i) {
        this.mIndexBarGravity = i;
        this.mIndexScroll.setPosition(i);
    }

    private int getListViewPosition(String str) {
        SeslAbsIndexer seslAbsIndexer;
        if (str == null || (seslAbsIndexer = this.mIndexer) == null) {
            return -1;
        }
        return seslAbsIndexer.getCachingValue(this.mIndexScroll.getSelectedIndex());
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        IndexScroll indexScroll = this.mIndexScroll;
        return (indexScroll != null && indexScroll.getIndexScrollThumb() == drawable) || super.verifyDrawable(drawable);
    }

    public void setIndexScrollMargin(int i, int i2) {
        IndexScroll indexScroll = this.mIndexScroll;
        if (indexScroll != null) {
            indexScroll.setIndexScrollBgMargin(i, i2);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        if (this.mNeedToHandleA11yEvent) {
            return handleA11yEvent(motionEvent);
        }
        return handleMotionEvent(motionEvent);
    }

    private boolean handleA11yEvent(MotionEvent motionEvent) {
        int i;
        int action = motionEvent.getAction();
        if (this.mIndexer == null) {
            return false;
        }
        if (action == 0) {
            this.mA11yDownPosY = motionEvent.getY();
        } else if (action == 1 || action == 3) {
            float y = motionEvent.getY();
            if (y != this.mA11yDownPosY) {
                if (this.mA11yTargetIndex == -1) {
                    this.mA11yTargetIndex = this.mIndexer.getIndexByPosition(this.mIndexScroll.findFirstChildPosition());
                }
                if (this.mA11yDownPosY - y > 0.0f && this.mA11yTargetIndex != this.mIndexScroll.mAlphabetSize - 1) {
                    this.mA11yTargetIndex++;
                } else if (this.mA11yDownPosY - y < 0.0f && (i = this.mA11yTargetIndex) != 0) {
                    this.mA11yTargetIndex = i - 1;
                }
                setContentDescription(this.mIndexScroll.mAlphabetArray[this.mA11yTargetIndex] + ", " + getResources().getString(R.string.sesl_index_selected));
                sendAccessibilityEvent(4);
                notifyIndexChange(this.mIndexer.getCachingValue(this.mA11yTargetIndex));
            }
        }
        return true;
    }

    private boolean handleMotionEvent(MotionEvent motionEvent) {
        int i;
        String str;
        int i2;
        String str2;
        int i3;
        int action = motionEvent.getAction();
        final float y = motionEvent.getY();
        float x = motionEvent.getX();
        if (action != MotionEvent.ACTION_DOWN) {
            if (action != MotionEvent.ACTION_UP) {
                if (action != MotionEvent.ACTION_MOVE) {
                    if (action != MotionEvent.ACTION_CANCEL) {
                        return false;
                    }
                } else if (this.mCurrentIndex == null || !this.mIndexScrollPreview.mIsOpen) {
                    return false;
                } else {
                    int i4 = (int) x;
                    int i5 = (int) y;
                    String indexByPosition = this.mIndexScroll.getIndexByPosition(i4, i5, false);
                    String str3 = this.mCurrentIndex;
                    if (str3 != null && indexByPosition == null && !this.mIsSimpleIndexScroll) {
                        String indexByPosition2 = this.mIndexScroll.getIndexByPosition(i4, i5, false);
                        this.mCurrentIndex = this.mIndexScroll.getIndexByPosition(i4, i5, false);
                        int listViewPosition = getListViewPosition(indexByPosition2);
                        if (listViewPosition != -1) {
                            notifyIndexChange(listViewPosition);
                        }
                    } else if (str3 == null || indexByPosition == null || indexByPosition.length() >= this.mCurrentIndex.length()) {
                        this.mCurrentIndex = this.mIndexScroll.getIndexByPosition(i4, i5, false);
                        if (!(!this.mIndexScroll.isAlphabetInit() || (str2 = this.mCurrentIndex) == null || str2.length() == 0)) {
                            this.mIndexScroll.setEffectText(this.mCurrentIndex);
                            this.mIndexScroll.drawEffect(y);
                            this.mTouchY = y;
                        }
                        if (!this.mIsSimpleIndexScroll) {
                            i2 = getListViewPosition(this.mCurrentIndex);
                        } else {
                            i2 = this.mIndexScroll.getSelectedIndex();
                        }
                        if (i2 != -1) {
                            notifyIndexChange(i2);
                        }
                    } else {
                        String indexByPosition3 = this.mIndexScroll.getIndexByPosition(i4, i5, false);
                        this.mCurrentIndex = indexByPosition3;
                        if (!this.mIsSimpleIndexScroll) {
                            i3 = getListViewPosition(indexByPosition3);
                        } else {
                            i3 = this.mIndexScroll.getSelectedIndex();
                        }
                        if (i3 != -1) {
                            notifyIndexChange(i3);
                        }
                    }
                }
            } else {
                postDelayed(() -> {
                    SeslIndexScrollView.this.mCurrentIndex = null;
                    SeslIndexScrollView.this.mIndexScroll.resetSelectedIndex();
                    SeslIndexScrollView.this.mIndexScrollPreview.close();
                    SeslIndexScrollView.this.mIndexScroll.changeThumbAlpha(0);
                    SeslIndexScrollView.this.invalidate();
                    if (SeslIndexScrollView.this.mOnIndexBarEventListener != null) {
                        SeslIndexScrollView.this.mOnIndexBarEventListener.onReleased(y);
                    }
                }, 30);
            }
        } else {
            this.mCurrentIndex = this.mIndexScroll.getIndexByPosition((int) x, (int) y, true);
            this.mStartTouchDown = System.currentTimeMillis();
            if (this.mCurrentIndex == null) {
                return false;
            }
            if (!(!this.mIndexScroll.isAlphabetInit() || (str = this.mCurrentIndex) == null || str.length() == 0)) {
                this.mIndexScroll.setEffectText(this.mCurrentIndex);
                this.mIndexScroll.drawEffect(y);
                this.mIndexScrollPreview.setLayout(0, 0, getWidth(), getHeight());
                this.mIndexScrollPreview.invalidate();
                this.mTouchY = y;
                this.mIndexScroll.changeThumbAlpha(255);
            }
            if (!this.mIsSimpleIndexScroll) {
                i = getListViewPosition(this.mCurrentIndex);
            } else {
                i = this.mIndexScroll.getSelectedIndex();
            }
            if (i != -1) {
                notifyIndexChange(i);
            }
        }
        invalidate();
        return true;
    }

    private boolean isTalkBackIsRunning() {
        String string;
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null || !accessibilityManager.isEnabled() || (string = Settings.Secure.getString(getContext().getContentResolver(), "enabled_accessibility_services")) == null) {
            return false;
        }
        return string.matches("(?i).*com.samsung.accessibility/com.samsung.android.app.talkback.TalkBackService.*") || string.matches("(?i).*com.samsung.android.accessibility.talkback/com.samsung.android.marvin.talkback.TalkBackService.*") || string.matches("(?i).*com.google.android.marvin.talkback.TalkBackService.*") || string.matches("(?i).*com.samsung.accessibility/com.samsung.accessibility.universalswitch.UniversalSwitchService.*");
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
    }

    private void notifyIndexChange(int i) {
        OnIndexBarEventListener onIndexBarEventListener = this.mOnIndexBarEventListener;
        if (onIndexBarEventListener != null) {
            onIndexBarEventListener.onIndexChanged(i);
        }
    }

    public void setOnIndexBarEventListener(OnIndexBarEventListener onIndexBarEventListener) {
        this.mOnIndexBarEventListener = onIndexBarEventListener;
    }

    /* access modifiers changed from: package-private */
    public class IndexerObserver extends DataSetObserver {
        private final long INDEX_UPDATE_DELAY = 200;
        boolean mDataInvalid = false;
        Runnable mUpdateIndex = new Runnable() {
            /* class androidx.indexscroll.widget.SeslIndexScrollView.IndexerObserver.AnonymousClass1 */

            public void run() {
                IndexerObserver.this.mDataInvalid = false;
            }
        };

        IndexerObserver() {
        }

        public void onChanged() {
            super.onChanged();
            notifyDataSetChange();
        }

        public void onInvalidated() {
            super.onInvalidated();
            notifyDataSetChange();
        }

        public boolean hasIndexerDataValid() {
            return !this.mDataInvalid;
        }

        private void notifyDataSetChange() {
            this.mDataInvalid = true;
            SeslIndexScrollView.this.removeCallbacks(this.mUpdateIndex);
            SeslIndexScrollView.this.postDelayed(this.mUpdateIndex, 200);
        }
    }

    /* access modifiers changed from: package-private */
    public class IndexScroll {
        public static final int GRAVITY_INDEX_BAR_LEFT = 0;
        public static final int GRAVITY_INDEX_BAR_RIGHT = 1;
        public static final int NO_SELECTED_INDEX = -1;
        private Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
        private int mAdditionalSpace;
        private String[] mAlphabetArray = null;
        private int mAlphabetSize;
        private Drawable mBgDrawableDefault = null;
        private Rect mBgRect;
        private boolean mBgRectParamsSet;
        private int mBgRectWidth;
        private int mBgTintColor;
        private String mBigText;
        private float mContentMinHeight;
        private int mContentPadding;
        private Context mContext;
        private int mCurItemPosition = -1;
        private int mCurThumbAlpha = 255;
        private float mDotRadius;
        private boolean mEnableScrollThumb;
        boolean mEnableTextMode;
        private final Runnable mFadeOutRunnable = new Runnable() {
            /* class androidx.indexscroll.widget.SeslIndexScrollView.IndexScroll.AnonymousClass1 */

            public void run() {
                IndexScroll.this.playThumbFadeAnimator(0);
            }
        };
        private int mHeight;
        IndexBarAttributeValues mIndexBarTextAttrs;
        private float mIndexScrollPreviewRadius;
        private boolean mIsAlphabetInit = false;
        private float mItemHeight;
        private int mItemWidth;
        private int mItemWidthGap;
        private RecyclerView.LayoutManager mLayout;
        private int mPosition = 0;
        private float mPreviewLimitY;
        private RecyclerView mRecyclerView;
        private int mScreenHeight;
        private int mScrollBottom;
        private int mScrollBottomMargin;
        private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
            /* class androidx.indexscroll.widget.SeslIndexScrollView.IndexScroll.AnonymousClass4 */

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                super.onScrollStateChanged(recyclerView, i);
                if (IndexScroll.this.mEnableScrollThumb && i == 0 && SeslIndexScrollView.this.mCurrentIndex == null) {
                    SeslIndexScrollView.this.removeCallbacks(IndexScroll.this.mFadeOutRunnable);
                    SeslIndexScrollView.this.postDelayed(IndexScroll.this.mFadeOutRunnable, 500);
                }
            }

            @Override // androidx.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (IndexScroll.this.mEnableScrollThumb) {
                    int findFirstChildPosition = IndexScroll.this.findFirstChildPosition();
                    if (SeslIndexScrollView.this.mCurrentIndex == null && IndexScroll.this.mCurItemPosition != findFirstChildPosition) {
                        if (!(IndexScroll.this.mTargetThumbAlpha == 255 || i2 == 0)) {
                            IndexScroll.this.playThumbFadeAnimator(255);
                        }
                        IndexScroll.this.mCurItemPosition = findFirstChildPosition;
                        if (SeslIndexScrollView.this.mIndexer != null) {
                            int indexByPosition = SeslIndexScrollView.this.mIndexer.getIndexByPosition(findFirstChildPosition);
                            if (findFirstChildPosition + IndexScroll.this.mRecyclerView.getChildCount() == IndexScroll.this.mRecyclerView.getAdapter().getItemCount()) {
                                indexByPosition = SeslIndexScrollView.this.mIndexer.getAlphabetArray().length - 1;
                            }
                            if (i2 != 0) {
                                IndexScroll indexScroll = IndexScroll.this;
                                indexScroll.playThumbPosAnimator(SeslIndexScrollView.this.mTouchY, ((float) (IndexScroll.this.mBgRect.top + IndexScroll.this.mScrollThumbBgRectVerticalPadding)) + ((((float) indexByPosition) / ((float) (SeslIndexScrollView.this.mIndexer.getAlphabetArray().length - 1))) * ((float) IndexScroll.this.mBgRect.height())));
                                return;
                            }
                            SeslIndexScrollView.this.mTouchY = ((float) (IndexScroll.this.mBgRect.top + IndexScroll.this.mScrollThumbBgRectVerticalPadding)) + ((((float) indexByPosition) / ((float) (SeslIndexScrollView.this.mIndexer.getAlphabetArray().length - 1))) * ((float) IndexScroll.this.mBgRect.height()));
                        }
                    }
                }
            }
        };
        private int mScrollThumbAdditionalHeight;
        private Drawable mScrollThumbBgDrawable = null;
        private Rect mScrollThumbBgRect;
        private int mScrollThumbBgRectHeight;
        private int mScrollThumbBgRectHorizontalPadding;
        private int mScrollThumbBgRectVerticalPadding;
        private int mScrollTop;
        private int mScrollTopMargin;
        private int mSelectedIndex = -1;
        private float mSeparatorHeight;
        private String mSmallText;
        private int mTargetThumbAlpha = 255;
        private Rect mTextBounds;
        private int mTextColorDimmed;
        private Paint mTextPaint;
        private int mTextSize;
        private int mThumbColor = 0;
        private ValueAnimator mThumbFadeAnimator;
        private ValueAnimator mThumbPosAnimator;
        private int mWidth;
        private int mWidthShift;

        /* access modifiers changed from: package-private */
        public class IndexBarAttributeValues {
            String[] alphabetArray;
            int count = 0;
            float height = 0.0f;
            float separatorHeight = 0.0f;

            public IndexBarAttributeValues() {
            }
        }

        public IndexScroll(Context context, int i, int i2) {
            this.mHeight = i;
            this.mWidth = i2;
            this.mWidthShift = 0;
            this.mScrollTop = 0;
            this.mTextBounds = new Rect();
            this.mBgRectParamsSet = false;
            this.mContext = context;
            init();
        }

        public IndexScroll(Context context, int i, int i2, int i3) {
            this.mHeight = i;
            this.mWidth = i2;
            this.mPosition = i3;
            this.mWidthShift = 0;
            this.mScrollTop = 0;
            this.mTextBounds = new Rect();
            this.mBgRectParamsSet = false;
            this.mContext = context;
            init();
        }

        public boolean isAlphabetInit() {
            return this.mIsAlphabetInit;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public int getSelectedIndex() {
            return this.mSelectedIndex;
        }

        public int getHeight() {
            return this.mHeight;
        }

        public void setSimpleIndexScrollWidth(int i) {
            if (i > 0) {
                this.mItemWidth = i;
                this.mBgRectWidth = i;
                allocateBgRectangle();
            }
        }

        public void setIndexScrollBgMargin(int i, int i2) {
            this.mScrollTopMargin = i;
            this.mScrollBottomMargin = i2;
            SeslIndexScrollView.this.invalidate();
        }

        public void setPosition(int i) {
            this.mPosition = i;
            setBgRectParams();
        }

        public void setDimensions(int i, int i2) {
            if (this.mIsAlphabetInit) {
                this.mWidth = i;
                int i3 = i2 - (((this.mScrollTop + this.mScrollBottom) + this.mScrollTopMargin) + this.mScrollBottomMargin);
                this.mHeight = i3;
                this.mScreenHeight = i2;
                float f = ((float) i3) / ((float) this.mAlphabetSize);
                this.mItemHeight = f;
                this.mSeparatorHeight = Math.max(f, this.mContentMinHeight);
                setBgRectParams();
                IndexBarAttributeValues indexBarAttributeValues = this.mIndexBarTextAttrs;
                if (indexBarAttributeValues != null) {
                    indexBarAttributeValues.separatorHeight = this.mContentMinHeight;
                    manageIndexScrollHeight();
                }
            }
        }

        private void init() {
            Resources resources = this.mContext.getResources();
            Paint paint = new Paint();
            this.mTextPaint = paint;
            paint.setAntiAlias(true);
            if (SeslIndexScrollView.this.mSECRobotoLightRegularFont == null) {
                SeslIndexScrollView.this.mSECRobotoLightRegularFont = Typeface.create(this.mContext.getString(R.string.sesl_font_family_regular), Typeface.NORMAL);
            }
            this.mTextPaint.setTypeface(SeslIndexScrollView.this.mSECRobotoLightRegularFont);
            this.mScrollTopMargin = 0;
            this.mScrollBottomMargin = 0;
            this.mItemWidth = 1;
            this.mItemWidthGap = 1;
            this.mBgRectWidth = (int) resources.getDimension(R.dimen.sesl_indexbar_width);
            this.mTextSize = (int) resources.getDimension(R.dimen.sesl_indexbar_text_size);
            this.mScrollTop = (int) resources.getDimension(R.dimen.sesl_indexbar_margin_top);
            this.mScrollBottom = (int) resources.getDimension(R.dimen.sesl_indexbar_margin_bottom);
            this.mWidthShift = (int) resources.getDimension(R.dimen.sesl_indexbar_margin_horizontal);
            this.mContentPadding = (int) resources.getDimension(R.dimen.sesl_indexbar_content_padding);
            this.mContentMinHeight = resources.getDimension(R.dimen.sesl_indexbar_content_min_height);
            this.mDotRadius = resources.getDimension(R.dimen.sesl_indexbar_dot_radius);
            this.mAdditionalSpace = (int) resources.getDimension(R.dimen.sesl_indexbar_additional_touch_boundary);
            this.mIndexScrollPreviewRadius = resources.getDimension(R.dimen.sesl_index_scroll_preview_radius);
            this.mPreviewLimitY = resources.getDimension(R.dimen.sesl_index_scroll_preview_ypos_limit);
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = this.mContext.getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.resourceId != 0 ? ResourcesCompat.getColor(resources, typedValue.resourceId, null) : typedValue.data;
            this.mIndexBarTextAttrs = new IndexBarAttributeValues();
            this.mScrollThumbBgRectVerticalPadding = (int) resources.getDimension(R.dimen.sesl_indexbar_thumb_vertical_padding);
            this.mScrollThumbBgRectHorizontalPadding = (int) resources.getDimension(R.dimen.sesl_indexbar_thumb_horizontal_padding);
            this.mScrollThumbAdditionalHeight = (int) resources.getDimension(R.dimen.sesl_indexbar_thumb_additional_height);
            Drawable drawable = resources.getDrawable(R.drawable.sesl_index_bar_thumb_shape, this.mContext.getTheme());
            this.mScrollThumbBgDrawable = drawable;
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            this.mThumbColor = color;
            this.mContext.getTheme().resolveAttribute(R.attr.isLightTheme, typedValue, true);
            if (typedValue.data != 0) {
                this.mTextColorDimmed = ResourcesCompat.getColor(resources, R.color.sesl_index_bar_text_color_light, theme);
                this.mBgTintColor = ResourcesCompat.getColor(resources, R.color.sesl_index_bar_background_tint_color_light, theme);
                SeslIndexScrollView.this.mIndexScrollPreview.setBackgroundColor(getColorWithAlpha(color, 0.8f));
            } else {
                this.mTextColorDimmed = ResourcesCompat.getColor(resources, R.color.sesl_index_bar_text_color_dark, theme);
                this.mBgTintColor = ResourcesCompat.getColor(resources, R.color.sesl_index_bar_background_tint_color_dark, theme);
                SeslIndexScrollView.this.mIndexScrollPreview.setBackgroundColor(getColorWithAlpha(color, 0.75f));
            }
            Drawable drawable2 = resources.getDrawable(R.drawable.sesl_index_bar_bg, theme);
            this.mBgDrawableDefault = drawable2;
            drawable2.setColorFilter(this.mBgTintColor, PorterDuff.Mode.MULTIPLY);
            this.mEnableTextMode = false;
            this.mEnableScrollThumb = false;
            setBgRectParams();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getColorWithAlpha(int i, float f) {
            return Color.argb(Math.round(((float) Color.alpha(i)) * f), Color.red(i), Color.green(i), Color.blue(i));
        }

        public void setAlphabetArray(String[] strArr) {
            if (strArr != null) {
                this.mAlphabetArray = strArr;
                int length = strArr.length;
                this.mAlphabetSize = length;
                float f = ((float) this.mHeight) / ((float) length);
                this.mItemHeight = f;
                this.mSeparatorHeight = Math.max(f, this.mContentMinHeight);
                this.mIsAlphabetInit = true;
            }
        }

        private void adjustSeparatorHeight() {
            IndexBarAttributeValues indexBarAttributeValues = this.mIndexBarTextAttrs;
            indexBarAttributeValues.separatorHeight = ((float) this.mHeight) / ((float) indexBarAttributeValues.count);
            float f = this.mIndexBarTextAttrs.separatorHeight;
            float f2 = this.mContentMinHeight;
            if (f < f2) {
                this.mIndexBarTextAttrs.separatorHeight = f2;
            }
            this.mIndexBarTextAttrs.height = (float) this.mHeight;
        }

        private void manageIndexScrollHeight() {
            if (this.mIsAlphabetInit) {
                this.mIndexBarTextAttrs.count = this.mAlphabetSize;
                IndexBarAttributeValues indexBarAttributeValues = this.mIndexBarTextAttrs;
                indexBarAttributeValues.alphabetArray = new String[indexBarAttributeValues.count];
                IndexBarAttributeValues indexBarAttributeValues2 = this.mIndexBarTextAttrs;
                indexBarAttributeValues2.height = ((float) indexBarAttributeValues2.count) * this.mContentMinHeight;
                setIndexBarTextOptimized(this.mIndexBarTextAttrs);
            }
        }

        private void setIndexBarTextOptimized(IndexBarAttributeValues indexBarAttributeValues) {
            adjustSeparatorHeight();
            int i = indexBarAttributeValues.count;
            int i2 = i;
            int i3 = 0;
            while (((float) this.mHeight) < indexBarAttributeValues.separatorHeight * ((float) i2)) {
                i2--;
                i3++;
            }
            if (this.mEnableTextMode) {
                float f = ((float) i) / (((float) i3) + 1.0f);
                int i4 = 0;
                for (int i5 = 0; i5 < i2; i5++) {
                    while (i5 != 0) {
                        int i6 = i4 + 1;
                        if (i5 + i4 != Math.round(((float) i6) * f)) {
                            break;
                        }
                        i4 = i6;
                    }
                    indexBarAttributeValues.alphabetArray[i5] = this.mAlphabetArray[i5 + i4];
                }
            }
            indexBarAttributeValues.count = i2;
            adjustSeparatorHeight();
        }

        public String getIndexByPosition(int i, int i2, boolean z) {
            int i3;
            int i4;
            Rect rect = this.mBgRect;
            if (rect == null || !this.mIsAlphabetInit) {
                return "";
            }
            if (z && ((this.mPosition == 0 && i < rect.left - this.mAdditionalSpace) || (this.mPosition == 1 && i > this.mBgRect.right + this.mAdditionalSpace))) {
                return "";
            }
            if (z && (i < this.mBgRect.left - this.mAdditionalSpace || i > this.mBgRect.right + this.mAdditionalSpace)) {
                int i5 = this.mPosition;
                if (i5 == 0 && i >= this.mWidthShift + this.mItemWidth + this.mItemWidthGap) {
                    return null;
                }
                if (i5 == 1 && i <= (this.mWidth - this.mWidthShift) - (this.mItemWidth + this.mItemWidthGap)) {
                    return null;
                }
                if (!isInSelectedIndexRect(i2)) {
                    return getIndexByY(i2);
                }
                String[] strArr = this.mAlphabetArray;
                if (strArr == null || (i4 = this.mSelectedIndex) < 0 || i4 >= this.mAlphabetSize) {
                    return "";
                }
                return strArr[i4];
            } else if (!isInSelectedIndexRect(i2)) {
                return getIndexByY(i2);
            } else {
                if (this.mAlphabetArray == null || (i3 = this.mSelectedIndex) < 0 || i3 >= this.mAlphabetSize) {
                    return "";
                }
                return getIndexByY(i2);
            }
        }

        private int getIndex(int i) {
            int i2;
            float f = (float) this.mAlphabetSize;
            if (((float) i) < ((float) (this.mScrollTop + this.mScrollTopMargin)) + this.mIndexBarTextAttrs.height) {
                i2 = (int) (((float) ((i - this.mScrollTop) - this.mScrollTopMargin)) / (this.mIndexBarTextAttrs.height / f));
            } else {
                i2 = this.mAlphabetSize - 1;
            }
            if (i2 < 0) {
                return 0;
            }
            int i3 = this.mAlphabetSize;
            return i2 >= i3 ? i3 - 1 : i2;
        }

        private String getIndexByY(int i) {
            int i2;
            if (i > this.mBgRect.top - this.mAdditionalSpace && i < this.mBgRect.bottom + this.mAdditionalSpace) {
                if (i < this.mBgRect.top) {
                    this.mSelectedIndex = 0;
                } else if (i > this.mBgRect.bottom) {
                    this.mSelectedIndex = this.mAlphabetSize - 1;
                } else {
                    int index = getIndex(i);
                    this.mSelectedIndex = index;
                    if (index == this.mAlphabetSize) {
                        this.mSelectedIndex = index - 1;
                    }
                }
                int i3 = this.mSelectedIndex;
                int i4 = this.mAlphabetSize;
                if (i3 == i4 || i3 == i4 + 1) {
                    this.mSelectedIndex = i4 - 1;
                }
                String[] strArr = this.mAlphabetArray;
                if (strArr != null && (i2 = this.mSelectedIndex) > -1 && i2 <= i4) {
                    return strArr[i2];
                }
            }
            return "";
        }

        private boolean isInSelectedIndexRect(int i) {
            int i2 = this.mSelectedIndex;
            if (i2 == -1 || i2 >= this.mAlphabetSize) {
                return false;
            }
            int i3 = this.mScrollTop;
            int i4 = this.mScrollTopMargin;
            float f = this.mSeparatorHeight;
            if (i < ((int) (((float) (i3 + i4)) + (((float) i2) * f))) || i > ((int) (((float) (i3 + i4)) + (f * ((float) (i2 + 1)))))) {
                return false;
            }
            return true;
        }

        public void resetSelectedIndex() {
            this.mSelectedIndex = -1;
        }

        public void draw(Canvas canvas) {
            if (this.mIsAlphabetInit) {
                drawScroll(canvas);
            }
        }

        public void drawScroll(Canvas canvas) {
            drawBgRectangle(canvas);
            drawAlphabetCharacters(canvas);
            int i = this.mSelectedIndex;
            if ((i < 0 || i >= this.mAlphabetSize) && SeslIndexScrollView.this.mIndexScrollPreview != null) {
                SeslIndexScrollView.this.mIndexScrollPreview.close();
            }
        }

        public Drawable getIndexScrollThumb() {
            return this.mScrollThumbBgDrawable;
        }

        public void setEffectText(String str) {
            this.mBigText = str;
        }

        public void drawEffect(float f) {
            float f2;
            float f3;
            int i = this.mSelectedIndex;
            if (i != -1) {
                String str = this.mAlphabetArray[i];
                this.mSmallText = str;
                this.mTextPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
                int i2 = this.mScreenHeight;
                float f4 = this.mIndexScrollPreviewRadius;
                float f5 = this.mPreviewLimitY;
                int i3 = this.mScrollTopMargin;
                float f6 = (2.0f * f4) + f5 + ((float) i3);
                int i4 = this.mScrollBottomMargin;
                if (((float) i2) <= f6 + ((float) i4)) {
                    f3 = ((float) (this.mScrollTop + i3)) + (this.mIndexBarTextAttrs.separatorHeight * 0.5f);
                    f2 = (((float) ((this.mScrollTop + this.mScrollTopMargin) - this.mScrollBottomMargin)) + this.mIndexBarTextAttrs.height) - (this.mIndexBarTextAttrs.separatorHeight * 0.5f);
                } else {
                    f2 = (((float) (i2 - i4)) - f5) - f4;
                    f3 = ((float) i3) + f5 + f4;
                }
                if (f <= f3 || f >= f2) {
                    f = f <= f3 ? f3 : f >= f2 ? f2 : -9999.0f;
                }
                if (f != SeslIndexScrollView.OUT_OF_BOUNDARY) {
                    SeslIndexScrollView.this.mIndexScrollPreview.open(f, this.mBigText);
                    if (SeslIndexScrollView.this.mOnIndexBarEventListener != null) {
                        SeslIndexScrollView.this.mOnIndexBarEventListener.onPressed(f);
                    }
                }
            }
        }

        private void allocateBgRectangle() {
            int i;
            int i2;
            if (this.mPosition == 1) {
                i2 = this.mWidth - this.mWidthShift;
                i = i2 - this.mBgRectWidth;
            } else {
                i = this.mWidthShift;
                i2 = this.mBgRectWidth + i;
            }
            Rect rect = this.mBgRect;
            if (rect == null) {
                int i3 = this.mScrollTop;
                int i4 = this.mScrollTopMargin;
                int i5 = this.mContentPadding;
                this.mBgRect = new Rect(i, (i3 + i4) - i5, i2, this.mHeight + i3 + i4 + i5);
            } else {
                int i6 = this.mScrollTop;
                int i7 = this.mScrollTopMargin;
                int i8 = this.mContentPadding;
                rect.set(i, (i6 + i7) - i8, i2, this.mHeight + i6 + i7 + i8);
            }
            if (this.mEnableTextMode) {
                this.mScrollThumbBgRectHeight = ((int) (this.mContentMinHeight * 3.0f)) + this.mScrollThumbAdditionalHeight;
                int i9 = this.mScrollThumbBgRectHorizontalPadding;
                i += i9;
                i2 -= i9;
            } else {
                this.mScrollThumbBgRectHeight = ((int) (this.mContentMinHeight * 2.0f)) + this.mScrollThumbAdditionalHeight;
            }
            int i10 = (int) (SeslIndexScrollView.this.mTouchY - ((float) (this.mScrollThumbBgRectHeight / 2)));
            int i11 = (int) (SeslIndexScrollView.this.mTouchY + ((float) (this.mScrollThumbBgRectHeight / 2)));
            if ((i10 < this.mBgRect.top + this.mScrollThumbBgRectVerticalPadding && i11 > this.mBgRect.bottom - this.mScrollThumbBgRectVerticalPadding) || this.mScrollThumbBgRectHeight >= (this.mBgRect.bottom - this.mBgRect.top) - (this.mScrollThumbBgRectVerticalPadding * 2)) {
                i10 = this.mBgRect.top + this.mScrollThumbBgRectVerticalPadding;
                i11 = this.mBgRect.bottom - this.mScrollThumbBgRectVerticalPadding;
            } else if (i10 < this.mBgRect.top + this.mScrollThumbBgRectVerticalPadding) {
                i10 = this.mBgRect.top + this.mScrollThumbBgRectVerticalPadding;
                i11 = this.mScrollThumbBgRectHeight + i10;
            } else if (i11 > this.mBgRect.bottom - this.mScrollThumbBgRectVerticalPadding) {
                i11 = this.mBgRect.bottom - this.mScrollThumbBgRectVerticalPadding;
                i10 = i11 - this.mScrollThumbBgRectHeight;
            }
            Rect rect2 = this.mScrollThumbBgRect;
            if (rect2 == null) {
                this.mScrollThumbBgRect = new Rect(i, i10, i2, i11);
            } else {
                rect2.set(i, i10, i2, i11);
            }
        }

        private void drawBgRectangle(Canvas canvas) {
            if (!this.mBgRectParamsSet) {
                setBgRectParams();
                this.mBgRectParamsSet = true;
            }
            this.mBgDrawableDefault.draw(canvas);
            if (SeslIndexScrollView.this.mTouchY != SeslIndexScrollView.OUT_OF_BOUNDARY) {
                this.mScrollThumbBgDrawable.draw(canvas);
            }
        }

        private void setBgRectParams() {
            allocateBgRectangle();
            this.mBgDrawableDefault.setBounds(this.mBgRect);
            this.mScrollThumbBgDrawable.setBounds(this.mScrollThumbBgRect);
        }

        private void drawAlphabetCharacters(Canvas canvas) {
            this.mTextPaint.setColor(this.mTextColorDimmed);
            this.mTextPaint.setTextSize((float) this.mTextSize);
            if (!(this.mAlphabetArray == null || this.mIndexBarTextAttrs.count == 0)) {
                int i = this.mIndexBarTextAttrs.count;
                for (int i2 = 0; i2 < i; i2++) {
                    if (this.mEnableTextMode) {
                        String str = this.mIndexBarTextAttrs.alphabetArray[i2];
                        this.mTextPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
                        canvas.drawText(str, ((float) this.mBgRect.centerX()) - (this.mTextPaint.measureText(str) * 0.5f), (this.mIndexBarTextAttrs.separatorHeight * ((float) i2)) + ((this.mIndexBarTextAttrs.separatorHeight * 0.5f) - (((float) this.mTextBounds.top) * 0.5f)) + ((float) this.mScrollTop) + ((float) this.mScrollTopMargin), this.mTextPaint);
                    } else {
                        canvas.drawCircle((float) this.mBgRect.centerX(), (this.mIndexBarTextAttrs.separatorHeight * ((float) i2)) + (this.mIndexBarTextAttrs.separatorHeight * 0.5f) + ((float) this.mScrollTop) + ((float) this.mScrollTopMargin), this.mDotRadius, this.mTextPaint);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void changeThumbAlpha(int i) {
            this.mCurThumbAlpha = i;
            this.mTargetThumbAlpha = i;
            SeslIndexScrollView.this.removeCallbacks(this.mFadeOutRunnable);
            ValueAnimator valueAnimator = this.mThumbFadeAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mScrollThumbBgDrawable.setAlpha(i);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void playThumbFadeAnimator(int i) {
            if (i != this.mCurThumbAlpha) {
                ValueAnimator valueAnimator = this.mThumbFadeAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                }
                this.mTargetThumbAlpha = i;
                ValueAnimator ofInt = ValueAnimator.ofInt(this.mCurThumbAlpha, i);
                this.mThumbFadeAnimator = ofInt;
                ofInt.setDuration(150L);
                this.mThumbFadeAnimator.setInterpolator(this.LINEAR_INTERPOLATOR);
                this.mThumbFadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class androidx.indexscroll.widget.SeslIndexScrollView.IndexScroll.AnonymousClass2 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        IndexScroll.this.mCurThumbAlpha = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                        IndexScroll.this.mScrollThumbBgDrawable.setAlpha(IndexScroll.this.mCurThumbAlpha);
                        SeslIndexScrollView.this.invalidate();
                    }
                });
                this.mThumbFadeAnimator.start();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void playThumbPosAnimator(float f, float f2) {
            ValueAnimator valueAnimator = this.mThumbPosAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
            this.mThumbPosAnimator = ofFloat;
            ofFloat.setDuration(300L);
            this.mThumbPosAnimator.setInterpolator(SeslAnimationUtils.SINE_OUT_70);
            this.mThumbPosAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class androidx.indexscroll.widget.SeslIndexScrollView.IndexScroll.AnonymousClass3 */

                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    SeslIndexScrollView.this.mTouchY = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    SeslIndexScrollView.this.invalidate();
                }
            });
            this.mThumbPosAnimator.start();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int findFirstChildPosition() {
            int i;
            RecyclerView.LayoutManager layoutManager = this.mLayout;
            if (layoutManager instanceof LinearLayoutManager) {
                i = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                i = ((StaggeredGridLayoutManager) this.mLayout).findFirstVisibleItemPositions(null)[layoutManager.getLayoutDirection() == 1 ? ((StaggeredGridLayoutManager) this.mLayout).getSpanCount() - 1 : 0];
            } else {
                i = 0;
            }
            if (i == -1) {
                return 0;
            }
            return i;
        }
    }

    /* access modifiers changed from: package-private */
    public class IndexScrollPreview extends View {
        private static final int FASTSCROLL_VIBRATE_INDEX = 26;
        private boolean mIsOpen = false;
        private float mPreviewCenterMargin;
        private float mPreviewCenterX;
        private float mPreviewCenterY;
        private float mPreviewRadius;
        private String mPreviewText;
        private Paint mShapePaint;
        private Rect mTextBounds;
        private Paint mTextPaint;
        private int mTextSize;
        private int mTextWidthLimit;
        private int mVibrateIndex;

        public IndexScrollPreview(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            Resources resources = context.getResources();
            Paint paint = new Paint();
            this.mShapePaint = paint;
            paint.setStyle(Paint.Style.FILL);
            this.mShapePaint.setAntiAlias(true);
            this.mTextSize = (int) resources.getDimension(R.dimen.sesl_index_scroll_preview_text_size);
            this.mTextWidthLimit = (int) resources.getDimension(R.dimen.sesl_index_scroll_preview_text_width_limit);
            Paint paint2 = new Paint();
            this.mTextPaint = paint2;
            paint2.setAntiAlias(true);
            this.mTextPaint.setTypeface(SeslIndexScrollView.this.mSECRobotoLightRegularFont);
            this.mTextPaint.setTextAlign(Paint.Align.CENTER);
            this.mTextPaint.setTextSize((float) this.mTextSize);
            this.mTextPaint.setColor(ResourcesCompat.getColor(resources, R.color.sesl_index_scroll_preview_text_color_light, null));
            this.mTextBounds = new Rect();
            this.mPreviewRadius = resources.getDimension(R.dimen.sesl_index_scroll_preview_radius);
            this.mPreviewCenterMargin = resources.getDimension(R.dimen.sesl_index_scroll_preview_margin_center);
            this.mIsOpen = false;
            this.mVibrateIndex = SeslHapticFeedbackConstantsReflector.semGetVibrationIndex(26);
        }

        public void setLayout(int i, int i2, int i3, int i4) {
            layout(i, i2, i3, i4);
            if (SeslIndexScrollView.this.mIndexBarGravity == 0) {
                this.mPreviewCenterX = this.mPreviewCenterMargin;
            } else {
                this.mPreviewCenterX = ((float) i3) - this.mPreviewCenterMargin;
            }
        }

        public void setBackgroundColor(int i) {
            this.mShapePaint.setColor(i);
        }

        public void setTextColor(int i) {
            this.mTextPaint.setColor(i);
        }

        public void open(float f, String str) {
            int i = this.mTextSize;
            this.mPreviewCenterY = f;
            if (!this.mIsOpen || !this.mPreviewText.equals(str)) {
                performHapticFeedback(this.mVibrateIndex);
            }
            this.mPreviewText = str;
            this.mTextPaint.setTextSize((float) i);
            while (this.mTextPaint.measureText(str) > ((float) this.mTextWidthLimit)) {
                i--;
                this.mTextPaint.setTextSize((float) i);
            }
            if (!this.mIsOpen) {
                startAnimation();
                this.mIsOpen = true;
            }
        }

        public void close() {
            long currentTimeMillis = System.currentTimeMillis() - SeslIndexScrollView.this.mStartTouchDown;
            removeCallbacks(SeslIndexScrollView.this.mPreviewDelayRunnable);
            if (currentTimeMillis <= 100) {
                postDelayed(SeslIndexScrollView.this.mPreviewDelayRunnable, 100);
            } else {
                fadeOutAnimation();
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void fadeOutAnimation() {
            if (this.mIsOpen) {
                startAnimation();
                this.mIsOpen = false;
            }
        }

        public void startAnimation() {
            ObjectAnimator objectAnimator;
            if (!this.mIsOpen) {
                objectAnimator = ObjectAnimator.ofFloat(SeslIndexScrollView.this.mIndexScrollPreview, "alpha", 0.0f, 1.0f);
            } else {
                objectAnimator = ObjectAnimator.ofFloat(SeslIndexScrollView.this.mIndexScrollPreview, "alpha", 1.0f, 0.0f);
            }
            objectAnimator.setDuration(167L);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(objectAnimator);
            animatorSet.start();
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.mIsOpen) {
                canvas.drawCircle(this.mPreviewCenterX, this.mPreviewCenterY, this.mPreviewRadius, this.mShapePaint);
                Paint paint = this.mTextPaint;
                String str = this.mPreviewText;
                paint.getTextBounds(str, 0, str.length() - 1, this.mTextBounds);
                canvas.drawText(this.mPreviewText, this.mPreviewCenterX, this.mPreviewCenterY - ((this.mTextPaint.descent() + this.mTextPaint.ascent()) / 2.0f), this.mTextPaint);
            }
        }
    }

    /* access modifiers changed from: private */
    public class IndexScrollTouchHelper extends ExploreByTouchHelper {
        private int mId = Integer.MIN_VALUE;

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            return false;
        }

        public IndexScrollTouchHelper(View view) {
            super(view);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateId(int i) {
            this.mId = i;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float f, float f2) {
            if (SeslIndexScrollView.this.mIndexScroll.mRecyclerView == null || !SeslIndexScrollView.this.mIndexScroll.mBgRect.contains((int) f, (int) f2)) {
                return Integer.MIN_VALUE;
            }
            return this.mId;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> list) {
            int i;
            if (SeslIndexScrollView.this.mIndexScroll.mRecyclerView != null && (i = this.mId) != Integer.MIN_VALUE) {
                list.add(Integer.valueOf(i));
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            if (this.mId == i) {
                Resources resources = SeslIndexScrollView.this.getResources();
                StringBuilder sb = new StringBuilder(resources.getString(R.string.sesl_index_section));
                sb.append(", ");
                sb.append(resources.getString(R.string.sesl_index_scrollbar));
                sb.append(", ");
                sb.append(resources.getString(R.string.sesl_index_assistant_text));
                accessibilityNodeInfoCompat.setContentDescription(sb);
                accessibilityNodeInfoCompat.setBoundsInParent(SeslIndexScrollView.this.mIndexScroll.mBgRect);
                accessibilityNodeInfoCompat.addAction(1);
            }
        }

        //@Override // androidx.core.view.AccessibilityDelegateCompat
        public void seslNotifyPerformAction(int i, int i2, Bundle bundle) {
            int i3 = this.mId;
            if (i3 == i && i3 != Integer.MIN_VALUE) {
                if (i2 == 64) {
                    SeslIndexScrollView.this.mNeedToHandleA11yEvent = true;
                } else if (i2 == 128) {
                    SeslIndexScrollView.this.mNeedToHandleA11yEvent = false;
                }
            }
        }
    }
}
