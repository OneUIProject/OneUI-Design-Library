package de.dlyt.yanndroid.oneui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.animation.SeslElasticInterpolator;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.reflect.content.res.SeslConfigurationReflector;
import androidx.reflect.widget.SeslTextViewReflector;

import de.dlyt.yanndroid.oneui.R;

public class TipPopup {
    private static final String TAG = "TipPopup";
    private static final int ANIMATION_DURATION_BOUNCE_SCALE1 = 167;
    private static final int ANIMATION_DURATION_BOUNCE_SCALE2 = 250;
    private static final int ANIMATION_DURATION_DISMISS_ALPHA = 166;
    private static final int ANIMATION_DURATION_DISMISS_SCALE = 166;
    private static final int ANIMATION_DURATION_EXPAND_ALPHA = 83;
    private static final int ANIMATION_DURATION_EXPAND_SCALE = 500;
    private static final int ANIMATION_DURATION_EXPAND_TEXT = 167;
    private static final int ANIMATION_DURATION_SHOW_SCALE = 500;
    private static final int ANIMATION_OFFSET_BOUNCE_SCALE = 3000;
    private static final int ANIMATION_OFFSET_EXPAND_TEXT = 100;
    public static final int DIRECTION_BOTTOM_LEFT = 2;
    public static final int DIRECTION_BOTTOM_RIGHT = 3;
    public static final int DIRECTION_DEFAULT = -1;
    public static final int DIRECTION_TOP_LEFT = 0;
    public static final int DIRECTION_TOP_RIGHT = 1;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_TRANSLUCENT = 1;
    private static final int MSG_DISMISS = 1;
    private static final int MSG_SCALE_UP = 2;
    private static final int MSG_TIMEOUT = 0;
    public static final int STATE_DISMISSED = 0;
    public static final int STATE_EXPANDED = 2;
    public static final int STATE_HINT = 1;
    private static final int TIMEOUT_DURATION_MS = 7100;
    private static final int TYPE_BALLOON_ACTION = 1;
    private static final int TYPE_BALLOON_CUSTOM = 2;
    private static final int TYPE_BALLOON_SIMPLE = 0;
    private static Interpolator INTERPOLATOR_SINE_IN_OUT_33;
    private static Interpolator INTERPOLATOR_SINE_IN_OUT_70;
    private static Interpolator INTERPOLATOR_ELASTIC_50;
    private static Interpolator INTERPOLATOR_ELASTIC_CUSTOM;
    private static final boolean localLOGD = true;
    private static Handler mHandler;
    private View.OnClickListener mActionClickListener = null;
    private CharSequence mActionText = null;
    private Integer mActionTextColor = null;
    private final Button mActionView;
    private int mArrowDirection = DIRECTION_DEFAULT;
    private final int mArrowHeight;
    private int mArrowPositionX = -1;
    private int mArrowPositionY = -1;
    private final int mArrowWidth;
    private int mBackgroundColor;
    private ImageView mBalloonBg1;
    private ImageView mBalloonBg2;
    private FrameLayout mBalloonBubble;
    private ImageView mBalloonBubbleHint;
    private ImageView mBalloonBubbleIcon;
    private FrameLayout mBalloonContent;
    private int mBalloonHeight;
    private FrameLayout mBalloonPanel;
    private TipWindow mBalloonPopup;
    private int mBalloonPopupX;
    private int mBalloonPopupY;
    private final View mBalloonView;
    private int mBalloonWidth;
    private int mBalloonX = -1;
    private int mBalloonY;
    private Integer mBorderColor = null;
    private ImageView mBubbleBackground;
    private int mBubbleHeight;
    private ImageView mBubbleIcon;
    private TipWindow mBubblePopup;
    private int mBubblePopupX;
    private int mBubblePopupY;
    private final View mBubbleView;
    private int mBubbleWidth;
    private int mBubbleX;
    private int mBubbleY;
    private final Context mContext;
    private final Rect mDisplayFrame;
    private DisplayMetrics mDisplayMetrics;
    private boolean mForceRealDisplay = false;
    private CharSequence mHintDescription = null;
    private final int mHorizontalTextMargin;
    private int mInitialmMessageViewWidth = 0;
    private boolean mIsDefaultPosition = true;
    private boolean mIsMessageViewMeasured = false;
    private CharSequence mMessageText = null;
    private Integer mMessageTextColor = null;
    private final TextView mMessageView;
    private final int mMode;
    private boolean mNeedToCallParentViewsOnClick = false;
    private OnDismissListener mOnDismissListener;
    private OnStateChangeListener mOnStateChangeListener;
    private final View mParentView;
    private final Resources mResources;
    private int mScaleMargin;
    private int mSideMargin;
    private int mState = STATE_HINT;
    private int mType = TYPE_BALLOON_SIMPLE;
    private final int mVerticalTextMargin;
    private final WindowManager mWindowManager;

    public interface OnDismissListener {
        void onDismiss();
    }

    public interface OnStateChangeListener {
        void onStateChanged(int state);
    }

    public void setOnStateChangeListener(OnStateChangeListener changeListener) {
        mOnStateChangeListener = changeListener;
    }

    public TipPopup(View parentView) {
        this(parentView, MODE_NORMAL);
    }

    public TipPopup(View parentView, int mode) {
        mContext = parentView.getContext();
        mResources = mContext.getResources();
        mParentView = parentView;
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplayMetrics = mResources.getDisplayMetrics();
        debugLog("mDisplayMetrics = " + mDisplayMetrics);
        mMode = mode;

        TypedValue colorPrimary = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        mBackgroundColor = colorPrimary.data;

        initInterpolator();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mBubbleView = inflater.inflate(R.layout.oui_tip_popup_bubble, null);
        mBalloonView = inflater.inflate(R.layout.oui_tip_popup_balloon, null);
        initBubblePopup(mode);
        initBalloonPopup(mode);
        mMessageView = mBalloonView.findViewById(R.id.tip_popup_message);
        mActionView = mBalloonView.findViewById(R.id.tip_popup_action);
        mMessageView.setVisibility(View.GONE);
        mActionView.setVisibility(View.GONE);

        if (mode == MODE_TRANSLUCENT) {
            mMessageView.setTextColor(mResources.getColor(R.color.oui_tip_popup_text_color_translucent));
            mActionView.setTextColor(mResources.getColor(R.color.oui_tip_popup_text_color_translucent));
        }

        mScaleMargin = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_scale_margin);
        mSideMargin = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_side_margin);
        mArrowHeight = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_balloon_arrow_height);
        mArrowWidth = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_balloon_arrow_width);
        mHorizontalTextMargin = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_balloon_message_margin_horizontal);
        mVerticalTextMargin = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_balloon_message_margin_vertical);

        mDisplayFrame = new Rect();

        mBubblePopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mState == STATE_HINT) {
                    mState = STATE_DISMISSED;
                    if (mOnStateChangeListener != null) {
                        mOnStateChangeListener.onStateChanged(mState);
                        debugLog("mIsShowing : " + isShowing());
                    }

                    if (mHandler != null) {
                        mHandler.removeCallbacksAndMessages(null);
                        mHandler = null;
                    }

                    debugLog("onDismiss - BubblePopup");
                }
            }
        });
        mBalloonPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mState = STATE_DISMISSED;
                if (mOnStateChangeListener != null) {
                    mOnStateChangeListener.onStateChanged(mState);
                    debugLog("mIsShowing : " + isShowing());
                }

                debugLog("onDismiss - BalloonPopup");
                dismissBubble(false);
                if (mHandler != null) {
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler = null;
                }
            }
        });
    }

    private void initInterpolator() {
        if (INTERPOLATOR_SINE_IN_OUT_33 == null) {
            INTERPOLATOR_SINE_IN_OUT_33 = AnimationUtils.loadInterpolator(mContext, R.interpolator.sine_in_out_33);
        }
        if (INTERPOLATOR_SINE_IN_OUT_70 == null) {
            INTERPOLATOR_SINE_IN_OUT_70 = AnimationUtils.loadInterpolator(mContext, R.interpolator.sine_in_out_70);
        }
        if (INTERPOLATOR_ELASTIC_50 == null) {
            INTERPOLATOR_ELASTIC_50 = new SeslElasticInterpolator(1.0f, 0.7f);
        }
        if (INTERPOLATOR_ELASTIC_CUSTOM == null) {
            INTERPOLATOR_ELASTIC_CUSTOM = new SeslElasticInterpolator(1.0f, 1.3f);
        }
    }

    private void initBubblePopup(int mode) {
        mBubbleBackground = mBubbleView.findViewById(R.id.tip_popup_bubble_bg);
        mBubbleIcon = mBubbleView.findViewById(R.id.tip_popup_bubble_icon);

        if (mode == MODE_TRANSLUCENT) {
            mBubbleBackground.setImageResource(R.drawable.oui_tip_popup_hint_background_translucent);
            mBubbleBackground.setImageTintList(null);

            mBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_translucent_rtl : R.drawable.oui_tip_popup_hint_icon_translucent);
            mBubbleIcon.setImageTintList(null);

            mBubbleWidth = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_width_translucent);
            mBubbleHeight = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_height_translucent);
        } else {
            mBubbleWidth = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_width);
            mBubbleHeight = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_height);
        }

        mBubblePopup = new TipWindow(mBubbleView, mBubbleWidth, mBubbleHeight, false);
        mBubblePopup.setTouchable(true);
        mBubblePopup.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mBubblePopup.setAttachedInDecor(false);
        }
    }

    private void initBalloonPopup(int mode) {
        mBalloonBubble = mBalloonView.findViewById(R.id.tip_popup_balloon_bubble);
        mBalloonBubbleHint = mBalloonView.findViewById(R.id.tip_popup_balloon_bubble_hint);
        mBalloonBubbleIcon = mBalloonView.findViewById(R.id.tip_popup_balloon_bubble_icon);
        mBalloonPanel = mBalloonView.findViewById(R.id.tip_popup_balloon_panel);
        mBalloonContent = mBalloonView.findViewById(R.id.tip_popup_balloon_content);
        mBalloonBg1 = mBalloonView.findViewById(R.id.tip_popup_balloon_bg_01);
        mBalloonBg2 = mBalloonView.findViewById(R.id.tip_popup_balloon_bg_02);
        
        if (mode == MODE_TRANSLUCENT) {
            mBalloonBg1.setBackgroundResource(R.drawable.oui_tip_popup_balloon_background_left_translucent);
            mBalloonBg1.setBackgroundTintList(null);
            mBalloonBg2.setBackgroundResource(R.drawable.oui_tip_popup_balloon_background_right_translucent);
            mBalloonBg2.setBackgroundTintList(null);
        }
        
        mBalloonBubble.setVisibility(View.VISIBLE);
        mBalloonPanel.setVisibility(View.GONE);
        
        mBalloonPopup = new TipWindow(mBalloonView, mBalloonWidth, mBalloonHeight, true);
        mBalloonPopup.setFocusable(true);
        mBalloonPopup.setTouchable(true);
        mBalloonPopup.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mBalloonPopup.setAttachedInDecor(false);
        }
        mBalloonPopup.setTouchInterceptor(new View.OnTouchListener() {
            @Override 
            public boolean onTouch(View v, MotionEvent event) {
                if (mNeedToCallParentViewsOnClick && mParentView.hasOnClickListeners() && (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_OUTSIDE)) {
                    Rect parentViewBounds = new Rect();
                    int[] outLocation = new int[2];

                    mParentView.getLocationOnScreen(outLocation);
                    parentViewBounds.set(outLocation[0], outLocation[1], outLocation[0] + mParentView.getWidth(), outLocation[1] + mParentView.getHeight());

                    if (parentViewBounds.contains((int) event.getRawX(), (int) event.getRawY())) {
                        debugLog("callOnClick for parent view");
                        mParentView.callOnClick();
                    }
                }
                return false;
            }
        });
    }

    public void show(int direction) {
        setInternal();

        if (mArrowPositionX == -1 || mArrowPositionY == -1) {
            calculateArrowPosition();
        }
        if (direction == DIRECTION_DEFAULT) {
            calculateArrowDirection(mArrowPositionX, mArrowPositionY);
        } else {
            mArrowDirection = direction;
        }

        calculatePopupSize();
        calculatePopupPosition();
        setBubblePanel();
        setBalloonPanel();
        showInternal();
    }

    public void setMessage(CharSequence message) {
        mMessageText = message;
    }

    public void setAction(CharSequence actionText, View.OnClickListener listener) {
        mActionText = actionText;
        mActionClickListener = listener;
    }

    public void semCallParentViewsOnClick(boolean needToCall) {
        mNeedToCallParentViewsOnClick = needToCall;
    }

    public boolean isShowing() {
        boolean isBubbleShowing = false;
        boolean isBalloonShowing = false;

        if (mBubblePopup != null) {
            isBubbleShowing = mBubblePopup.isShowing();
        }
        if (mBalloonPopup != null) {
            isBalloonShowing = mBalloonPopup.isShowing();
        }

        return isBubbleShowing || isBalloonShowing;
    }

    public void dismiss(boolean withAnimation) {
        if (mBubblePopup != null) {
            mBubblePopup.setUseDismissAnimation(withAnimation);
            debugLog("mBubblePopup.mIsDismissing = " + mBubblePopup.mIsDismissing);
            mBubblePopup.dismiss();
        }
        if (mBalloonPopup != null) {
            mBalloonPopup.setUseDismissAnimation(withAnimation);
            debugLog("mBalloonPopup.mIsDismissing = " + mBalloonPopup.mIsDismissing);
            mBalloonPopup.dismiss();
        }

        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void setExpanded(boolean expanded) {
        if (expanded) {
            mState = STATE_EXPANDED;
            mScaleMargin = 0;
        } else {
            mScaleMargin = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_scale_margin);
        }
    }

    public void setTargetPosition(int x, int y) {
        if (x >= 0 && y >= 0) {
            mIsDefaultPosition = false;
            mArrowPositionX = x;
            mArrowPositionY = y;
        }
    }

    public void setHintDescription(CharSequence hintDescription) {
        mHintDescription = hintDescription;
    }

    public void update() {
        update(mArrowDirection, false);
    }

    public void update(int direction, boolean resetHintTimer) {
        if (isShowing() && mParentView != null) {
            setInternal();

            mBalloonX = -1;
            mBalloonY = -1;

            if (mIsDefaultPosition) {
                debugLog("update - default position");
                calculateArrowPosition();
            }
            if (direction == DIRECTION_DEFAULT) {
                calculateArrowDirection(mArrowPositionX, mArrowPositionY);
            } else {
                mArrowDirection = direction;
            }

            calculatePopupSize();
            calculatePopupPosition();
            setBubblePanel();
            setBalloonPanel();

            if (mState == STATE_HINT && mBubblePopup != null) {
                mBubblePopup.update(mBubblePopupX, mBubblePopupY, mBubblePopup.getWidth(), mBubblePopup.getHeight());
                if (resetHintTimer) {
                    debugLog("Timer Reset!");
                    scheduleTimeout();
                }
            } else if (mState == STATE_EXPANDED && mBalloonPopup != null) {
                mBalloonPopup.update(mBalloonPopupX, mBalloonPopupY, mBalloonPopup.getWidth(), mBalloonPopup.getHeight());
            }
        }
    }

    public void setMessageTextColor(int color) {
        mMessageTextColor = color | Color.BLACK;
    }

    public void setActionTextColor(int color) {
        mActionTextColor = color | Color.BLACK;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color | Color.BLACK;
    }

    public void setBackgroundColorWithAlpha(int color) {
        mBackgroundColor = color;
    }

    public void setBorderColor(int color) {
        mBorderColor = color | Color.BLACK;
    }

    public void setOutsideTouchEnabled(boolean enabled) {
        mBubblePopup.setFocusable(enabled);
        mBubblePopup.setOutsideTouchable(enabled);
        mBalloonPopup.setFocusable(enabled);
        mBalloonPopup.setOutsideTouchable(enabled);
        debugLog("outside enabled : " + enabled);
    }

    public void setPopupWindowClippingEnabled(boolean enabled) {
        mBubblePopup.setClippingEnabled(enabled);
        mBalloonPopup.setClippingEnabled(enabled);
        mForceRealDisplay = !enabled;
        mSideMargin = enabled ? mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_side_margin) : 0;
        debugLog("clipping enabled : " + enabled);
    }

    private void setInternal() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_TIMEOUT:
                            dismissBubble(true);
                            break;
                        case MSG_DISMISS:
                            dismissBubble(false);
                            break;
                        case MSG_SCALE_UP:
                            animateScaleUp();
                            break;
                    }
                }
            };
        }

        if (mMessageView != null && mActionView != null) {
            float currentFontScale = mResources.getConfiguration().fontScale;
            int messageTextSize = mResources.getDimensionPixelOffset(R.dimen.oui_tip_popup_balloon_message_text_size);
            int actionTextSize = mResources.getDimensionPixelOffset(R.dimen.oui_tip_popup_balloon_action_text_size);
            if (currentFontScale > 1.2f) {
                mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) Math.floor(Math.ceil((double) (((float) messageTextSize) / currentFontScale)) * ((double) 1.2f)));
                mActionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) Math.floor(Math.ceil((double) (((float) actionTextSize) / currentFontScale)) * ((double) 1.2f)));
            }
            mMessageView.setText(mMessageText);

            if (TextUtils.isEmpty(mActionText) || mActionClickListener == null) {
                mActionView.setVisibility(View.GONE);
                mActionView.setOnClickListener(null);
                mType = TYPE_BALLOON_SIMPLE;
            } else {
                mActionView.setVisibility(View.VISIBLE);
                SeslTextViewReflector.semSetButtonShapeEnabled(mActionView, true, mBackgroundColor);
                mActionView.setText(mActionText);
                mActionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mActionClickListener != null) {
                            mActionClickListener.onClick(v);
                        }
                        dismiss(true);
                    }
                });
                mType = TYPE_BALLOON_ACTION;
            }

            if (mBubbleIcon != null && mHintDescription != null) {
                mBubbleIcon.setContentDescription(mHintDescription);
            }

            if (mMode != MODE_TRANSLUCENT && mBubbleIcon != null && mBubbleBackground != null && mBalloonBubble != null && mBalloonBg1 != null && mBalloonBg2 != null) {
                if (mMessageTextColor != null) {
                    mMessageView.setTextColor(mMessageTextColor);
                }
                if (mActionTextColor != null) {
                    mActionView.setTextColor(mActionTextColor);
                }
                mBubbleBackground.setColorFilter(mBackgroundColor);
                mBalloonBubbleHint.setColorFilter(mBackgroundColor);
                mBalloonBg1.setBackgroundTintList(ColorStateList.valueOf(mBackgroundColor));
                mBalloonBg2.setBackgroundTintList(ColorStateList.valueOf(mBackgroundColor));
                if (mBorderColor != null) {
                    mBubbleIcon.setColorFilter(mBorderColor);
                    mBalloonBubbleIcon.setColorFilter(mBorderColor);
                }
            }
        }
    }

    private void showInternal() {
        if (mState != STATE_EXPANDED) {
            mState = STATE_HINT;

            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.onStateChanged(STATE_HINT);
                debugLog("mIsShowing : " + isShowing());
            }

            if (mBubblePopup != null) {
                mBubblePopup.showAtLocation(mParentView, 0, mBubblePopupX, mBubblePopupY);
                animateViewIn();
            }

            mBubbleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mState = STATE_EXPANDED;

                    if (mOnStateChangeListener != null) {
                        mOnStateChangeListener.onStateChanged(mState);
                    }

                    if (mBalloonPopup != null) {
                        mBalloonPopup.showAtLocation(mParentView, 0, mBalloonPopupX, mBalloonPopupY);
                    }

                    if (mHandler != null) {
                        mHandler.removeMessages(0);
                        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_DISMISS), 10);
                        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_SCALE_UP), 20);
                    }
                    return false;
                }
            });
        } else {
            mBalloonBubble.setVisibility(View.GONE);
            mBalloonPanel.setVisibility(View.VISIBLE);
            mMessageView.setVisibility(View.VISIBLE);

            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.onStateChanged(mState);
            }

            if (mBalloonPopup != null) {
                mBalloonPopup.showAtLocation(mParentView, 0, mBalloonPopupX, mBalloonPopupY);
            }
        }

        mBalloonView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mType != TYPE_BALLOON_SIMPLE) {
                    return false;
                }
                dismiss(true);
                return false;
            }
        });
    }

    private void setBubblePanel() {
        if (mBubblePopup != null) {
            FrameLayout.LayoutParams paramBubblePanel = (FrameLayout.LayoutParams) mBubbleBackground.getLayoutParams();
            if (mMode == MODE_TRANSLUCENT) {
                paramBubblePanel.width = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_width_translucent);
                paramBubblePanel.height = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_height_translucent);
            }

            switch (mArrowDirection) {
                case DIRECTION_TOP_LEFT:
                    mBubblePopup.setPivot((float) mBubblePopup.getWidth(), (float) mBubblePopup.getHeight());
                    paramBubblePanel.gravity = 85;
                    mBubblePopupX = mBubbleX - (mScaleMargin * 2);
                    mBubblePopupY = mBubbleY - (mScaleMargin * 2);

                    if (mMode != MODE_NORMAL) {
                        mBubbleBackground.setRotationX(180.0f);
                    } else {
                        mBubbleBackground.setImageResource(R.drawable.oui_tip_popup_hint_background_03);
                        mBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    }
                    break;
                case DIRECTION_TOP_RIGHT:
                    mBubblePopup.setPivot(0.0f, (float) mBubblePopup.getHeight());
                    paramBubblePanel.gravity = 83;
                    mBubblePopupX = mBubbleX;
                    mBubblePopupY = mBubbleY - (mScaleMargin * 2);

                    if (mMode != MODE_NORMAL) {
                        mBubbleBackground.setRotation(180.0f);
                    } else {
                        mBubbleBackground.setImageResource(R.drawable.oui_tip_popup_hint_background_04);
                        mBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    }
                    break;
                case DIRECTION_BOTTOM_LEFT:
                    mBubblePopup.setPivot((float) mBubblePopup.getWidth(), 0.0f);
                    paramBubblePanel.gravity = 53;
                    mBubblePopupX = mBubbleX - (mScaleMargin * 2);
                    mBubblePopupY = mBubbleY;

                    if (mMode != MODE_NORMAL) {
                        mBubbleBackground.setRotationY(180.0f);
                    } else {
                        mBubbleBackground.setImageResource(R.drawable.oui_tip_popup_hint_background_01);
                        mBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    }
                    break;
                case DIRECTION_BOTTOM_RIGHT:
                    mBubblePopup.setPivot(0.0f, 0.0f);
                    paramBubblePanel.gravity = 51;
                    mBubblePopupX = mBubbleX;
                    mBubblePopupY = mBubbleY;

                    if (mMode != MODE_NORMAL) {
                        mBubbleBackground.setRotationY(180.0f);
                    } else {
                        mBubbleBackground.setImageResource(R.drawable.oui_tip_popup_hint_background_02);
                        mBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    }
                    break;
            }

            mBubbleBackground.setLayoutParams(paramBubblePanel);
            mBubbleIcon.setLayoutParams(paramBubblePanel);

            mBubblePopup.setWidth(mBubbleWidth + (mScaleMargin * 2));
            mBubblePopup.setHeight(mBubbleHeight + (mScaleMargin * 2));
        }
    }

    private void setBalloonPanel() {
        if (mBalloonPopup != null) {
            debugLog("setBalloonPanel()");

            final int leftMargin = mBubbleX - mBalloonX;
            final int rightMargin = (mBalloonX + mBalloonWidth) - mBubbleX;
            final int topMargin = mBubbleY - mBalloonY;
            final int bottomMargin = (mBalloonY + mBalloonHeight) - (mBubbleY + mBubbleHeight);

            DisplayMetrics realMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getRealMetrics(realMetrics);
            final int scaleFactor;

            debugLog("leftMargin[" + leftMargin + "]");
            debugLog("rightMargin[" + rightMargin + "] mBalloonWidth[" + mBalloonWidth + "]");

            final int horizontalContentMargin = mHorizontalTextMargin - mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_button_padding_horizontal);
            final int verticalButtonPadding = mActionView.getVisibility() == View.VISIBLE ? mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_button_padding_vertical) : 0;

            final FrameLayout.LayoutParams paramBalloonBubble = (FrameLayout.LayoutParams) mBalloonBubble.getLayoutParams();
            final FrameLayout.LayoutParams paramBalloonPanel = (FrameLayout.LayoutParams) mBalloonPanel.getLayoutParams();
            final FrameLayout.LayoutParams paramBalloonContent = (FrameLayout.LayoutParams) mBalloonContent.getLayoutParams();
            final FrameLayout.LayoutParams paramBalloonBg1 = (FrameLayout.LayoutParams) mBalloonBg1.getLayoutParams();
            final FrameLayout.LayoutParams paramBalloonBg2 = (FrameLayout.LayoutParams) mBalloonBg2.getLayoutParams();

            if (mMode == MODE_TRANSLUCENT) {
                mBalloonBubbleHint.setImageResource(R.drawable.oui_tip_popup_hint_background_translucent);
                mBalloonBubbleHint.setImageTintList(null);
                mBalloonBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_translucent_rtl : R.drawable.oui_tip_popup_hint_icon_translucent);
                mBalloonBubbleIcon.setImageTintList(null);

                paramBalloonBubble.width = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_width_translucent);
                paramBalloonBubble.height = mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_height_translucent);

                scaleFactor = 0;
            } else {
                scaleFactor = (int) Math.ceil((double) realMetrics.density);
            }

            switch (mArrowDirection) {
                case DIRECTION_TOP_LEFT:
                    mBalloonPopup.setPivot(((float) mArrowPositionX - mBalloonX + mScaleMargin), ((float) mBalloonHeight + mScaleMargin));

                    if (mMode == MODE_NORMAL) {
                        mBalloonBubbleHint.setImageResource(R.drawable.oui_tip_popup_hint_background_03);
                        mBalloonBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    } else {
                        mBalloonBubbleHint.setRotationX(180.0f);
                    }

                    mBalloonBg1.setRotationX(180.0f);
                    mBalloonBg2.setRotationX(180.0f);

                    paramBalloonBg2.gravity = 85;
                    paramBalloonBg1.gravity = 85;
                    paramBalloonBubble.gravity = 85;

                    paramBalloonBg1.setMargins(0, 0, rightMargin - mBubbleWidth, 0);
                    paramBalloonBg2.setMargins(mBubbleWidth + leftMargin - scaleFactor, 0, 0, 0);
                    paramBalloonContent.setMargins(horizontalContentMargin, mVerticalTextMargin, horizontalContentMargin, mArrowHeight + mVerticalTextMargin - verticalButtonPadding);
                    break;
                case DIRECTION_TOP_RIGHT:
                    mBalloonPopup.setPivot(((float) mArrowPositionX - mBalloonX + mScaleMargin), ((float) mBalloonHeight + mScaleMargin));

                    if (mMode == MODE_NORMAL) {
                        mBalloonBubbleHint.setImageResource(R.drawable.oui_tip_popup_hint_background_04);
                        mBalloonBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    } else {
                        mBalloonBubbleHint.setRotation(180.0f);
                    }

                    mBalloonBg1.setRotation(180.0f);
                    mBalloonBg2.setRotation(180.0f);

                    paramBalloonBg2.gravity = 83;
                    paramBalloonBg1.gravity = 83;
                    paramBalloonBubble.gravity = 83;

                    paramBalloonBg1.setMargins(leftMargin, 0, 0, 0);
                    paramBalloonBg2.setMargins(0, 0, rightMargin - scaleFactor, 0);
                    paramBalloonContent.setMargins(horizontalContentMargin, mVerticalTextMargin, horizontalContentMargin, mArrowHeight + mVerticalTextMargin - verticalButtonPadding);
                    break;
                case DIRECTION_BOTTOM_LEFT:
                    mBalloonPopup.setPivot(((float) mArrowPositionX - mBalloonX + mScaleMargin), (float) mScaleMargin);

                    if (mMode == MODE_NORMAL) {
                        mBalloonBubbleHint.setImageResource(R.drawable.oui_tip_popup_hint_background_01);
                        mBalloonBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    }

                    paramBalloonBg2.gravity = 53;
                    paramBalloonBg1.gravity = 53;
                    paramBalloonBubble.gravity = 53;

                    paramBalloonBg1.setMargins(0, 0, rightMargin - mBubbleWidth, 0);
                    paramBalloonBg2.setMargins(mBubbleWidth + leftMargin - scaleFactor, 0, 0, 0);
                    paramBalloonContent.setMargins(horizontalContentMargin, mArrowHeight + mVerticalTextMargin, horizontalContentMargin, mVerticalTextMargin - verticalButtonPadding);
                    break;
                case DIRECTION_BOTTOM_RIGHT:
                    mBalloonPopup.setPivot(((float) mArrowPositionX - mBalloonX + mScaleMargin), (float) mScaleMargin);

                    if (mMode == MODE_NORMAL) {
                        mBalloonBubbleHint.setImageResource(R.drawable.oui_tip_popup_hint_background_02);
                        mBalloonBubbleIcon.setImageResource(isRTL() ? R.drawable.oui_tip_popup_hint_icon_rtl : R.drawable.oui_tip_popup_hint_icon);
                    } else {
                        mBalloonBubbleHint.setRotationY(180.0f);
                    }

                    mBalloonBg1.setRotationY(180.0f);
                    mBalloonBg2.setRotationY(180.0f);

                    paramBalloonBg2.gravity = 51;
                    paramBalloonBg1.gravity = 51;
                    paramBalloonBubble.gravity = 51;

                    paramBalloonBg1.setMargins(leftMargin, 0, 0, 0);
                    paramBalloonBg2.setMargins(0, 0, rightMargin - scaleFactor, 0);
                    paramBalloonContent.setMargins(horizontalContentMargin, mArrowHeight + mVerticalTextMargin, horizontalContentMargin, mVerticalTextMargin - verticalButtonPadding);
                    break;
            }

            paramBalloonBubble.setMargins(leftMargin + mScaleMargin, topMargin + mScaleMargin, rightMargin - mBubbleWidth + mScaleMargin, mScaleMargin + bottomMargin);
            paramBalloonPanel.setMargins(mScaleMargin, mScaleMargin, mScaleMargin, mScaleMargin);

            mBalloonPopupX = mBalloonX - mScaleMargin;
            mBalloonPopupY = mBalloonY - mScaleMargin;

            mBalloonBubble.setLayoutParams(paramBalloonBubble);
            mBalloonPanel.setLayoutParams(paramBalloonPanel);
            mBalloonBg1.setLayoutParams(paramBalloonBg1);
            mBalloonBg2.setLayoutParams(paramBalloonBg2);
            mBalloonContent.setLayoutParams(paramBalloonContent);

            mBalloonPopup.setWidth(mBalloonWidth + (mScaleMargin * 2));
            mBalloonPopup.setHeight(mBalloonHeight + (mScaleMargin * 2));
        }
    }

    private void calculateArrowDirection(int arrowX, int arrowY) {
        if (mParentView != null && mIsDefaultPosition) {
            int[] location = new int[2];
            mParentView.getLocationInWindow(location);
            int parentY = location[1] + (mParentView.getHeight() / 2);

            if (arrowX * 2 <= mDisplayMetrics.widthPixels) {
                if (arrowY <= parentY) {
                    mArrowDirection = DIRECTION_TOP_RIGHT;
                } else {
                    mArrowDirection = DIRECTION_BOTTOM_RIGHT;
                }
            } else if (arrowY <= parentY) {
                mArrowDirection = DIRECTION_TOP_LEFT;
            } else {
                mArrowDirection = DIRECTION_BOTTOM_LEFT;
            }
        } else if (arrowX * 2 <= mDisplayMetrics.widthPixels && arrowY * 2 <= mDisplayMetrics.heightPixels) {
            mArrowDirection = DIRECTION_BOTTOM_RIGHT;
        } else if (arrowX * 2 > mDisplayMetrics.widthPixels && arrowY * 2 <= mDisplayMetrics.heightPixels) {
            mArrowDirection = DIRECTION_BOTTOM_LEFT;
        } else if (arrowX * 2 <= mDisplayMetrics.widthPixels && arrowY * 2 > mDisplayMetrics.heightPixels) {
            mArrowDirection = DIRECTION_TOP_RIGHT;
        } else if (arrowX * 2 > mDisplayMetrics.widthPixels && arrowY * 2 > mDisplayMetrics.heightPixels) {
            mArrowDirection = DIRECTION_TOP_LEFT;
        }

        debugLog("calculateArrowDirection : arrow position (" + arrowX + ", " + arrowY + ") / mArrowDirection = " + mArrowDirection);
    }

    private void calculateArrowPosition() {
        if (mParentView == null) {
            mArrowPositionX = 0;
            mArrowPositionY = 0;
            return;
        }

        final int[] location = new int[2];
        mParentView.getLocationInWindow(location);
        debugLog("calculateArrowPosition anchor location : " + location[0] + ", " + location[1]);

        final int x = location[0] + (mParentView.getWidth() / 2);
        final int y = location[1] + (mParentView.getHeight() / 2);
        if (y * 2 <= mDisplayMetrics.heightPixels) {
            mArrowPositionY = (mParentView.getHeight() / 2) + y;
        } else {
            mArrowPositionY = y - (mParentView.getHeight() / 2);
        }
        mArrowPositionX = x;

        debugLog("calculateArrowPosition mArrowPosition : " + mArrowPositionX + ", " + mArrowPositionY);
    }

    private void calculatePopupSize() {
        final int balloonMaxWidth;
        mDisplayMetrics = mResources.getDisplayMetrics();
        final int screenWidthDp = mResources.getConfiguration().screenWidthDp;
        final int balloonMinWidth = mArrowWidth + (mHorizontalTextMargin * 2);

        if (SeslConfigurationReflector.isDexEnabled(mContext.getResources().getConfiguration())) {
            int windowWidthInDexMode = mParentView.getRootView().getMeasuredWidth();
            final int[] windowLocation = new int[2];
            mParentView.getRootView().getLocationOnScreen(windowLocation);
            if (windowLocation[0] < 0) {
                windowWidthInDexMode += windowLocation[0];
            }

            debugLog("Window width in DexMode " + windowWidthInDexMode);

            if (windowWidthInDexMode <= 480) {
                balloonMaxWidth = (int) (((float) windowWidthInDexMode) * 0.83f);
            } else if (windowWidthInDexMode <= 960) {
                balloonMaxWidth = (int) (((float) windowWidthInDexMode) * 0.6f);
            } else if (windowWidthInDexMode <= 1280) {
                balloonMaxWidth = (int) (((float) windowWidthInDexMode) * 0.45f);
            } else {
                balloonMaxWidth = (int) (((float) windowWidthInDexMode) * 0.25f);
            }
        } else {
            debugLog("screen width DP " + screenWidthDp);

            if (screenWidthDp <= 480) {
                balloonMaxWidth = (int) (((float) mDisplayMetrics.widthPixels) * 0.83f);
            } else if (screenWidthDp <= 960) {
                balloonMaxWidth = (int) (((float) mDisplayMetrics.widthPixels) * 0.6f);
            } else if (screenWidthDp <= 1280) {
                balloonMaxWidth = (int) (((float) mDisplayMetrics.widthPixels) * 0.45f);
            } else {
                balloonMaxWidth = (int) (((float) mDisplayMetrics.widthPixels) * 0.25f);
            }
        }

        if (!mIsMessageViewMeasured) {
            mMessageView.measure(0, 0);
            mInitialmMessageViewWidth = mMessageView.getMeasuredWidth();
            mIsMessageViewMeasured = true;
        }

        int balloonWidth = mInitialmMessageViewWidth + (mHorizontalTextMargin * 2);
        if (balloonWidth < balloonMinWidth) {
            balloonWidth = balloonMinWidth;
        } else if (balloonWidth > balloonMaxWidth) {
            balloonWidth = balloonMaxWidth;
        }
        mBalloonWidth = balloonWidth;

        mMessageView.setWidth(balloonWidth - (mHorizontalTextMargin * 2));
        mMessageView.measure(0, 0);

        mBalloonHeight = mMessageView.getMeasuredHeight() + (mVerticalTextMargin * 2) + mArrowHeight;

        if (mType == TYPE_BALLOON_ACTION) {
            mActionView.measure(0, 0);
            if (mBalloonWidth < mActionView.getMeasuredWidth()) {
                mBalloonWidth = mActionView.getMeasuredWidth() + (mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_button_padding_horizontal) * 2);
            }
            mBalloonHeight += mActionView.getMeasuredHeight() - mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_button_padding_vertical);
        }
    }

    private void calculatePopupPosition() {
        getDisplayFrame(mDisplayFrame);

        if (mBalloonX < 0) {
            if (mArrowDirection == DIRECTION_BOTTOM_RIGHT || mArrowDirection == DIRECTION_TOP_RIGHT) {
                mBalloonX = (mArrowPositionX + mArrowWidth) - (mBalloonWidth / 2);
            } else {
                mBalloonX = (mArrowPositionX - mArrowWidth) - (mBalloonWidth / 2);
            }
        }

        if (mArrowDirection == DIRECTION_BOTTOM_RIGHT || mArrowDirection == DIRECTION_TOP_RIGHT) {
            if (mArrowPositionX < mDisplayFrame.left + mSideMargin + mHorizontalTextMargin) {
                debugLog("Target position is too far to the left!");
                mArrowPositionX = mDisplayFrame.left + mSideMargin + mHorizontalTextMargin;
            } else if (mArrowPositionX > ((mDisplayFrame.right - mSideMargin) - mHorizontalTextMargin) - mArrowWidth) {
                debugLog("Target position is too far to the right!");
                mArrowPositionX = ((mDisplayFrame.right - mSideMargin) - mHorizontalTextMargin) - mArrowWidth;
            }
        } else if (mArrowPositionX < mDisplayFrame.left + mSideMargin + mHorizontalTextMargin + mArrowWidth) {
            debugLog("Target position is too far to the left!");
            mArrowPositionX = mDisplayFrame.left + mSideMargin + mHorizontalTextMargin + mArrowWidth;
        } else if (mArrowPositionX > (mDisplayFrame.right - mSideMargin) - mHorizontalTextMargin) {
            debugLog("Target position is too far to the right!");
            mArrowPositionX = (mDisplayFrame.right - mSideMargin) - mHorizontalTextMargin;
        }

        if (SeslConfigurationReflector.isDexEnabled(mContext.getResources().getConfiguration())) {
            int windowWidthInDexMode = mParentView.getRootView().getMeasuredWidth();
            int[] windowLocation = new int[2];
            mParentView.getRootView().getLocationOnScreen(windowLocation);
            if (windowLocation[0] < 0) {
                windowWidthInDexMode += windowLocation[0];
            }

            if (mBalloonX < mDisplayFrame.left + mSideMargin) {
                mBalloonX = mDisplayFrame.left + mSideMargin;
            } else {
                if (mBalloonX + mBalloonWidth > windowWidthInDexMode - mSideMargin) {
                    mBalloonX = windowWidthInDexMode - mSideMargin - mBalloonWidth;
                    if (windowLocation[0] < 0) {
                        mBalloonX -= windowLocation[0];
                    }
                }
            }
        } else if (mBalloonX < mDisplayFrame.left + mSideMargin) {
            mBalloonX = mDisplayFrame.left + mSideMargin;
        } else if (mBalloonX + mBalloonWidth > mDisplayFrame.right - mSideMargin) {
            mBalloonX = (mDisplayFrame.right - mSideMargin) - mBalloonWidth;
        }

        switch (mArrowDirection) {
            case DIRECTION_TOP_LEFT:
                mBubbleX = mArrowPositionX - mBubbleWidth;
                mBubbleY = mArrowPositionY - mBubbleHeight;
                mBalloonY = mArrowPositionY - mBalloonHeight;
                break;
            case DIRECTION_TOP_RIGHT:
                mBubbleX = mArrowPositionX;
                mBubbleY = mArrowPositionY - mBubbleHeight;
                mBalloonY = mArrowPositionY - mBalloonHeight;
                break;
            case DIRECTION_BOTTOM_LEFT:
                mBubbleX = mArrowPositionX - mBubbleWidth;
                mBubbleY = mArrowPositionY;
                mBalloonY = mArrowPositionY;
                break;
            case DIRECTION_BOTTOM_RIGHT:
                mBubbleX = mArrowPositionX;
                mBubbleY = mArrowPositionY;
                mBalloonY = mArrowPositionY;
                break;
        }

        debugLog("QuestionPopup : " + mBubbleX + ", " + mBubbleY + ", " + mBubbleWidth + ", " + mBubbleHeight);
        debugLog("BalloonPopup : " + mBalloonX + ", " + mBalloonY + ", " + mBalloonWidth + ", " + mBalloonHeight);
    }

    private void dismissBubble(boolean withAnimation) {
        if (mBubblePopup != null) {
            mBubblePopup.setUseDismissAnimation(withAnimation);
            mBubblePopup.dismiss();
        }
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    private void scheduleTimeout() {
        if (mHandler != null) {
            mHandler.removeMessages(0);
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT), TIMEOUT_DURATION_MS);
        }
    }

    private void animateViewIn() {
        final float pivotX;
        float pivotY;
        switch (mArrowDirection) {
            case DIRECTION_TOP_LEFT:
                pivotX = 1.0f;
                pivotY = 1.0f;
                break;
            case DIRECTION_TOP_RIGHT:
                pivotX = 0.0f;
                pivotY = 1.0f;
                break;
            case DIRECTION_BOTTOM_LEFT:
                pivotX = 1.0f;
                pivotY = 0.0f;
                break;
            default:
            case DIRECTION_BOTTOM_RIGHT:
                pivotX = 0.0f;
                pivotY = 0.0f;
                break;
        }

        Animation animScale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RESTART, pivotX, Animation.RESTART, pivotY);
        animScale.setInterpolator(INTERPOLATOR_ELASTIC_50);
        animScale.setDuration(ANIMATION_DURATION_EXPAND_SCALE);
        animScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                scheduleTimeout();
                animateBounce();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mBubbleView.startAnimation(animScale);
    }

    private void animateBounce() {
        final float pivotX;
        final float pivotY;
        switch (mArrowDirection) {
            case DIRECTION_TOP_LEFT:
                pivotX = (float) mBubblePopup.getWidth();
                pivotY = (float) mBubblePopup.getHeight();
                break;
            case DIRECTION_TOP_RIGHT:
                pivotX = 0.0f;
                pivotY = (float) mBubblePopup.getHeight();
                break;
            case DIRECTION_BOTTOM_LEFT:
                pivotX = (float) mBubblePopup.getWidth();
                pivotY = 0.0f;
                break;
            default:
            case DIRECTION_BOTTOM_RIGHT:
                pivotX = 0.0f;
                pivotY = 0.0f;
                break;
        }

        final AnimationSet animationSet = new AnimationSet(false);

        Animation anim1 = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f, Animation.ABSOLUTE, pivotX, Animation.ABSOLUTE, pivotY);
        anim1.setDuration(ANIMATION_DURATION_BOUNCE_SCALE1);
        anim1.setInterpolator(INTERPOLATOR_SINE_IN_OUT_70);

        Animation anim2 = new ScaleAnimation(1.0f, 0.833f, 1.0f, 0.833f, Animation.ABSOLUTE, pivotX, Animation.ABSOLUTE, pivotY);
        anim2.setStartOffset(167);
        anim2.setDuration(ANIMATION_DURATION_BOUNCE_SCALE2);
        anim2.setInterpolator(INTERPOLATOR_SINE_IN_OUT_33);
        anim2.setAnimationListener(new Animation.AnimationListener() {
            int count = 0;

            @Override
            public void onAnimationStart(Animation animation) {
                count++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                debugLog("repeat count " + count);
                mBubbleView.startAnimation(animationSet);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animationSet.addAnimation(anim1);
        animationSet.addAnimation(anim2);
        animationSet.setStartOffset(ANIMATION_OFFSET_BOUNCE_SCALE);

        mBubbleView.startAnimation(animationSet);
    }

    private void animateScaleUp() {
        final float deltaHintY;
        final float pivotHintX;
        final float pivotHintY;
        final float pivotPanelX;
        final float pivotPanelY;
        final float panelScale = ((float) mResources.getDimensionPixelSize(R.dimen.oui_tip_popup_bubble_height)) / ((float) mBalloonHeight);
        switch (mArrowDirection) {
            case DIRECTION_TOP_LEFT:
                pivotHintX = (float) mBalloonBubble.getWidth();
                pivotHintY = (float) mBalloonBubble.getHeight();
                pivotPanelX = ((float) mArrowPositionX - mBalloonX);
                pivotPanelY = (float) mBalloonHeight;
                deltaHintY = 0.0f - (((float) mArrowHeight) / 2.0f);
                break;
            case DIRECTION_TOP_RIGHT:
                pivotHintX = 0.0f;
                pivotHintY = (float) mBalloonBubble.getHeight();
                pivotPanelX = ((float) mArrowPositionX - mBalloonX);
                pivotPanelY = (float) mBalloonHeight;
                deltaHintY = 0.0f - (((float) mArrowHeight) / 2.0f);
                break;
            case DIRECTION_BOTTOM_LEFT:
                pivotHintX = (float) mBalloonBubble.getWidth();
                pivotHintY = 0.0f;
                pivotPanelX = ((float) mArrowPositionX - mBalloonX);
                pivotPanelY = 0.0f;
                deltaHintY = ((float) mArrowHeight) / 2.0f;
                break;
            case DIRECTION_BOTTOM_RIGHT:
                pivotHintX = 0.0f;
                pivotHintY = 0.0f;
                pivotPanelX = ((float) mBubbleX - mBalloonX);
                pivotPanelY = 0.0f;
                deltaHintY = ((float) mArrowHeight) / 2.0f;
                break;
            default:
                pivotHintX = 0.0f;
                pivotHintY = 0.0f;
                pivotPanelX = 0.0f;
                pivotPanelY = 0.0f;
                deltaHintY = 0.0f;
                break;
        }

        AnimationSet animationBubble = new AnimationSet(false);

        TranslateAnimation animationBubbleMove = new TranslateAnimation(Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, deltaHintY);
        animationBubbleMove.setDuration(500);
        animationBubbleMove.setInterpolator(INTERPOLATOR_ELASTIC_CUSTOM);

        Animation animationBubbleScale = new ScaleAnimation(1.0f, 1.7f, 1.0f, 1.7f, Animation.ABSOLUTE, pivotHintX, Animation.ABSOLUTE, pivotHintY);
        animationBubbleScale.setDuration(500);
        animationBubbleScale.setInterpolator(INTERPOLATOR_ELASTIC_CUSTOM);

        Animation animationBubbleAlpha = new AlphaAnimation(1.0f, 0.0f);
        animationBubbleAlpha.setDuration(166);
        animationBubbleAlpha.setInterpolator(INTERPOLATOR_SINE_IN_OUT_33);

        animationBubble.addAnimation(animationBubbleMove);
        animationBubble.addAnimation(animationBubbleScale);
        animationBubble.addAnimation(animationBubbleAlpha);

        animationBubble.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mBalloonPanel.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mBalloonBubble.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mBalloonBubble.startAnimation(animationBubble);


        AnimationSet animationPanel = new AnimationSet(false);

        Animation animationPanelScale = new ScaleAnimation(0.27f, 1.0f, panelScale, 1.0f, 0, pivotPanelX, 0, pivotPanelY);
        animationPanelScale.setInterpolator(INTERPOLATOR_ELASTIC_CUSTOM);
        animationPanelScale.setDuration(ANIMATION_DURATION_SHOW_SCALE);

        Animation animationPanelAlpha = new AlphaAnimation(0.0f, 1.0f);
        animationPanelAlpha.setInterpolator(INTERPOLATOR_SINE_IN_OUT_33);
        animationPanelAlpha.setDuration(ANIMATION_DURATION_EXPAND_ALPHA);

        animationPanel.addAnimation(animationPanelScale);
        animationPanel.addAnimation(animationPanelAlpha);

        mBalloonPanel.startAnimation(animationPanel);


        Animation animationText = new AlphaAnimation(0.0f, 1.0f);
        animationText.setInterpolator(INTERPOLATOR_SINE_IN_OUT_33);
        animationText.setStartOffset(ANIMATION_OFFSET_EXPAND_TEXT);
        animationText.setDuration(ANIMATION_DURATION_EXPAND_TEXT);
        animationText.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mMessageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismissBubble(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mMessageView.startAnimation(animationText);
        mActionView.startAnimation(animationText);
    }

    private boolean isNavigationbarHide() {
        return mContext != null && Settings.Global.getInt(mContext.getContentResolver(), "navigationbar_hide_bar_enabled", 0) == 1;
    }

    private int getNavagationbarHeight() {
        int resourceId = mResources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return mResources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private boolean isTablet() {
        DisplayMetrics realMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(realMetrics);

        final int shortSizeDp = ((realMetrics.widthPixels > realMetrics.heightPixels ? realMetrics.heightPixels : realMetrics.widthPixels) * 160) / realMetrics.densityDpi;
        debugLog("short size dp  = " + shortSizeDp);
        return shortSizeDp >= 600;
    }

    private void getDisplayFrame(Rect screenRect) {
        final int navigationbarHeight = getNavagationbarHeight();
        final boolean navigationbarHide = isNavigationbarHide();
        final int displayRotation = mWindowManager.getDefaultDisplay().getRotation();

        DisplayMetrics realMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(realMetrics);

        debugLog("realMetrics = " + realMetrics);
        debugLog("is tablet? = " + isTablet());

        if (mForceRealDisplay) {
            screenRect.left = 0;
            screenRect.top = 0;
            screenRect.right = realMetrics.widthPixels;
            screenRect.bottom = realMetrics.heightPixels;
            debugLog("Screen Rect = " + screenRect + " mForceRealDisplay = " + mForceRealDisplay);
            return;
        }

        screenRect.left = 0;
        screenRect.top = 0;
        screenRect.right = this.mDisplayMetrics.widthPixels;
        screenRect.bottom = this.mDisplayMetrics.heightPixels;

        Rect bounds = new Rect();
        mParentView.getRootView().getWindowVisibleDisplayFrame(bounds);

        debugLog("Bounds = " + bounds);

        if (!isTablet()) {
            debugLog("phone");

            switch (displayRotation) {
                case Surface.ROTATION_0:
                    if (realMetrics.widthPixels == mDisplayMetrics.widthPixels && realMetrics.heightPixels - mDisplayMetrics.heightPixels == navigationbarHeight && navigationbarHide) {
                        screenRect.bottom += navigationbarHeight;
                    }
                    break;
                case Surface.ROTATION_90:
                    if (realMetrics.heightPixels == mDisplayMetrics.heightPixels && realMetrics.widthPixels - mDisplayMetrics.widthPixels == navigationbarHeight && navigationbarHide) {
                        screenRect.right += navigationbarHeight;
                    }

                    WindowInsetsCompat windowInsets = ViewCompat.getRootWindowInsets(mParentView);
                    if (windowInsets != null) {
                        DisplayCutoutCompat displayCutout = windowInsets.getDisplayCutout();
                        if (displayCutout != null) {
                            screenRect.left += displayCutout.getSafeInsetLeft();
                            screenRect.right += displayCutout.getSafeInsetLeft();
                            debugLog("displayCutout.getSafeInsetLeft() :  " + displayCutout.getSafeInsetLeft());
                        }
                    }
                    break;
                case Surface.ROTATION_180:
                    if (realMetrics.widthPixels != mDisplayMetrics.widthPixels || realMetrics.heightPixels - mDisplayMetrics.heightPixels != navigationbarHeight) {
                        if (realMetrics.widthPixels == mDisplayMetrics.widthPixels && bounds.top == navigationbarHeight) {
                            debugLog("Top Docked");
                            screenRect.top += navigationbarHeight;
                            screenRect.bottom += navigationbarHeight;
                        }
                    } else if (!navigationbarHide) {
                        screenRect.top += navigationbarHeight;
                        screenRect.bottom += navigationbarHeight;
                    } else {
                        screenRect.bottom += navigationbarHeight;
                    }
                    break;
                case Surface.ROTATION_270:
                    if (realMetrics.heightPixels != mDisplayMetrics.heightPixels || realMetrics.widthPixels - mDisplayMetrics.widthPixels != navigationbarHeight) {
                        if (realMetrics.heightPixels == mDisplayMetrics.heightPixels && bounds.left == navigationbarHeight) {
                            debugLog("Left Docked");
                            screenRect.left += navigationbarHeight;
                            screenRect.right += navigationbarHeight;
                        }
                    } else if (!navigationbarHide) {
                        screenRect.left += navigationbarHeight;
                        screenRect.right += navigationbarHeight;
                    } else {
                        screenRect.right += navigationbarHeight;
                    }
                    break;
            }
        } else {
            debugLog("tablet");
            if (realMetrics.widthPixels == mDisplayMetrics.widthPixels && realMetrics.heightPixels - mDisplayMetrics.heightPixels == navigationbarHeight && navigationbarHide) {
                screenRect.bottom += navigationbarHeight;
            }
        }

        debugLog("Screen Rect = " + screenRect);
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private static class TipWindow extends PopupWindow {
        private boolean mIsDismissing = false;
        private boolean mIsUsingDismissAnimation = true;
        private float mPivotX = 0.0f;
        private float mPivotY = 0.0f;

        private TipWindow(View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);
        }

        private void setUseDismissAnimation(boolean useAnimation) {
            mIsUsingDismissAnimation = useAnimation;
        }

        private void setPivot(float pivotX, float pivotY) {
            mPivotX = pivotX;
            mPivotY = pivotY;
        }

        @Override
        public void dismiss() {
            if (!mIsUsingDismissAnimation || mIsDismissing) {
                super.dismiss();
            } else {
                animateViewOut();
            }
        }

        private void animateViewOut() {
            AnimationSet animationSet = new AnimationSet(true);

            Animation animScale = new ScaleAnimation(1.0f, 0.81f, 1.0f, 0.81f, 0, this.mPivotX, 0, this.mPivotY);
            animScale.setInterpolator(INTERPOLATOR_ELASTIC_CUSTOM);
            animScale.setDuration(ANIMATION_DURATION_DISMISS_SCALE);

            Animation animAlpha = new AlphaAnimation(1.0f, 0.0f);
            animAlpha.setInterpolator(INTERPOLATOR_SINE_IN_OUT_33);
            animAlpha.setDuration(ANIMATION_DURATION_DISMISS_ALPHA);

            animationSet.addAnimation(animScale);
            animationSet.addAnimation(animAlpha);

            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mIsDismissing = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    dismiss();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            getContentView().startAnimation(animationSet);
        }
    }

    private boolean isRTL() {
        return mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void debugLog(String str) {
        if (localLOGD) {
            Log.d(TAG, " #### " + str);
        }
    }
}
