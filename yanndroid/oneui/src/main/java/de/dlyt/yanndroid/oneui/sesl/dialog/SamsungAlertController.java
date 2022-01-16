package de.dlyt.yanndroid.oneui.sesl.dialog;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.view.ViewCompat;
import androidx.reflect.widget.SeslAdapterViewReflector;
import androidx.reflect.widget.SeslTextViewReflector;

import java.lang.ref.WeakReference;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.dialog.AlertDialog;
import de.dlyt.yanndroid.oneui.view.NestedScrollView;

public class SamsungAlertController {
    private boolean mIsOneUI4;
    ListAdapter mAdapter;
    private int mAlertDialogLayout;
    private final int mButtonIconDimen;
    Button mButtonNegative;
    private Drawable mButtonNegativeIcon;
    Message mButtonNegativeMessage;
    private CharSequence mButtonNegativeText;
    Button mButtonNeutral;
    private Drawable mButtonNeutralIcon;
    Message mButtonNeutralMessage;
    private CharSequence mButtonNeutralText;
    private int mButtonPanelSideLayout;
    Button mButtonPositive;
    private Drawable mButtonPositiveIcon;
    Message mButtonPositiveMessage;
    private CharSequence mButtonPositiveText;
    private final Context mContext;
    private View mCustomTitleView;
    final AppCompatDialog mDialog;
    Handler mHandler;
    private Drawable mIcon;
    private ImageView mIconView;
    private int mLastOrientation;
    int mListItemLayout;
    int mListLayout;
    ListView mListView;
    private CharSequence mMessage;
    private TextView mMessageView;
    int mMultiChoiceItemLayout;
    NestedScrollView mScrollView;
    private boolean mShowTitle;
    int mSingleChoiceItemLayout;
    private CharSequence mTitle;
    private TextView mTitleView;
    private View mView;
    private int mViewLayoutResId;
    private int mViewSpacingBottom;
    private int mViewSpacingLeft;
    private int mViewSpacingRight;
    private int mViewSpacingTop;
    private final Window mWindow;
    private boolean mViewSpacingSpecified = false;
    private int mIconId = 0;
    int mCheckedItem = -1;
    private int mButtonPanelLayoutHint = AlertDialog.LAYOUT_HINT_NONE;

    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final Message m;
            if (v == mButtonPositive && mButtonPositiveMessage != null) {
                m = Message.obtain(mButtonPositiveMessage);
            } else if (v == mButtonNegative && mButtonNegativeMessage != null) {
                m = Message.obtain(mButtonNegativeMessage);
            } else if (v == mButtonNeutral && mButtonNeutralMessage != null) {
                m = Message.obtain(mButtonNeutralMessage);
            } else {
                m = null;
            }

            if (m != null) {
                m.sendToTarget();
            }

            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialog).sendToTarget();
        }
    };

    private static final class ButtonHandler extends Handler {
        private static final int MSG_DISMISS_DIALOG = 1;
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }

    private static boolean shouldCenterSingleButton(Context context) {
        final TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.alertDialogCenterButtons, outValue, true);
        return outValue.data != 0;
    }

    public SamsungAlertController(Context context, AppCompatDialog di, Window window) {
        mContext = context;
        mDialog = di;
        mWindow = window;
        mHandler = new ButtonHandler(di);

        mIsOneUI4 = context.getTheme().obtainStyledAttributes(new int[]{R.attr.isOneUI4}).getBoolean(0, false);

        final TypedArray a = context.obtainStyledAttributes(null, R.styleable.SamsungAlertDialog, R.attr.alertDialogStyle, 0);

        mAlertDialogLayout = a.getResourceId(R.styleable.SamsungAlertDialog_android_layout, 0);
        mButtonPanelSideLayout = a.getResourceId(R.styleable.SamsungAlertDialog_buttonPanelSideLayout, 0);

        mListLayout = a.getResourceId(R.styleable.SamsungAlertDialog_listLayout, 0);
        mMultiChoiceItemLayout = a.getResourceId(R.styleable.SamsungAlertDialog_multiChoiceItemLayout, 0);
        mSingleChoiceItemLayout = a.getResourceId(R.styleable.SamsungAlertDialog_singleChoiceItemLayout, 0);
        mListItemLayout = a.getResourceId(R.styleable.SamsungAlertDialog_listItemLayout, 0);
        mShowTitle = a.getBoolean(R.styleable.SamsungAlertDialog_showTitle, true);
        mButtonIconDimen = a.getDimensionPixelSize(R.styleable.SamsungAlertDialog_buttonIconDimen, 0);

        a.recycle();

        // for devices without SamsungBasicInteraction flag
        window.setGravity(Gravity.BOTTOM);

        di.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    static boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }

        if (!(v instanceof ViewGroup)) {
            return false;
        }

        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }

        return false;
    }

    public void installContent() {
        final int contentView = selectContentView();
        mDialog.setContentView(contentView);
        setupView();
    }

    private int selectContentView() {
        if (mButtonPanelSideLayout == 0) {
            return mAlertDialogLayout;
        }
        if (mButtonPanelLayoutHint == AlertDialog.LAYOUT_HINT_SIDE) {
            return mButtonPanelSideLayout;
        }
        return mAlertDialogLayout;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    public void setCustomTitle(View customTitleView) {
        mCustomTitleView = customTitleView;
    }

    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    public void setView(int layoutResId) {
        mView = null;
        mViewLayoutResId = layoutResId;
        mViewSpacingSpecified = false;
    }

    public void setView(View view) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = false;
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        mView = view;
        mViewLayoutResId = 0;
        mViewSpacingSpecified = true;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
    }

    public void setButtonPanelLayoutHint(int layoutHint) {
        mButtonPanelLayoutHint = layoutHint;
    }

    public void setButton(int whichButton, CharSequence text, DialogInterface.OnClickListener listener, Message msg, Drawable icon) {
        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }

        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                mButtonPositiveText = text;
                mButtonPositiveMessage = msg;
                mButtonPositiveIcon = icon;
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mButtonNegativeText = text;
                mButtonNegativeMessage = msg;
                mButtonNegativeIcon = icon;
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mButtonNeutralText = text;
                mButtonNeutralMessage = msg;
                mButtonNeutralIcon = icon;
                break;
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }

    public void setIcon(int resId) {
        mIcon = null;
        mIconId = resId;

        if (mIconView != null) {
            if (resId != 0) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageResource(mIconId);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        mIconId = 0;

        if (mIconView != null) {
            if (icon != null) {
                mIconView.setVisibility(View.VISIBLE);
                mIconView.setImageDrawable(icon);
            } else {
                mIconView.setVisibility(View.GONE);
            }
        }
    }

    public int getIconAttributeResId(int attrId) {
        TypedValue out = new TypedValue();
        mContext.getTheme().resolveAttribute(attrId, out, true);
        return out.resourceId;
    }

    public ListView getListView() {
        return mListView;
    }

    public Button getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mScrollView != null && mScrollView.executeKeyEvent(event);
    }

    @Nullable
    private ViewGroup resolvePanel(@Nullable View customPanel, @Nullable View defaultPanel) {
        if (customPanel == null) {
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }

            return (ViewGroup) defaultPanel;
        }

        if (defaultPanel != null) {
            final ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }

        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }

        return (ViewGroup) customPanel;
    }

    private void setupPaddings() {
        final View parentPanel = mWindow.findViewById(R.id.parentPanel);
        final View titleTemplate = parentPanel.findViewById(R.id.title_template);
        final View scrollView = parentPanel.findViewById(R.id.scrollView);
        final View topPanel = parentPanel.findViewById(R.id.topPanel);
        final View buttonBarLayout = parentPanel.findViewById(R.id.buttonBarLayout);
        final View customPanel = parentPanel.findViewById(R.id.customPanel);
        final View contentPanel = parentPanel.findViewById(R.id.contentPanel);

        final boolean hasCustomTitle = mCustomTitleView != null && mCustomTitleView.getVisibility() != View.GONE;
        final boolean hasCustomPanel = customPanel != null && customPanel.getVisibility() != View.GONE;
        final boolean hasTopPanel = topPanel != null && topPanel.getVisibility() != View.GONE;
        final boolean hasContentPanel = contentPanel != null && contentPanel.getVisibility() != View.GONE;

        Resources resources = mContext.getResources();

        if ((!hasCustomPanel || hasTopPanel || hasContentPanel) && !hasCustomTitle) {
            parentPanel.setPadding(0, resources.getDimensionPixelSize(R.dimen.sesl_dialog_title_padding_top), 0, 0);
        } else {
            parentPanel.setPadding(0, 0, 0, 0);
        }

        if (titleTemplate != null) {
            if (!hasCustomPanel || !hasTopPanel || hasContentPanel) {
                titleTemplate.setPadding(resources.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0, resources.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), resources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_title_padding_bottom : R.dimen.sesl_dialog_title_padding_bottom));
            } else {
                titleTemplate.setPadding(resources.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0, resources.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0);
            }
        }

        if (scrollView != null) {
            scrollView.setPadding(resources.getDimensionPixelSize(R.dimen.sesl_dialog_body_text_scroll_padding_start), 0, resources.getDimensionPixelSize(R.dimen.sesl_dialog_body_text_scroll_padding_end), resources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_body_text_padding_bottom : R.dimen.sesl_dialog_body_text_padding_bottom));
        }

        if (buttonBarLayout != null) {
            buttonBarLayout.setPadding(resources.getDimensionPixelSize(R.dimen.sesl_dialog_button_bar_padding_horizontal), 0, resources.getDimensionPixelSize(R.dimen.sesl_dialog_button_bar_padding_horizontal), resources.getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_button_bar_padding_bottom : R.dimen.sesl_dialog_button_bar_padding_bottom));
        }
    }

    private void setupView() {
        final View parentPanel = mWindow.findViewById(R.id.parentPanel);

        parentPanel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mContext.getResources().getConfiguration().orientation != mLastOrientation) {
                            setupPaddings();
                            parentPanel.requestLayout();
                        }
                        mLastOrientation = mContext.getResources().getConfiguration().orientation;
                    }
                });
            }
        });

        final View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        final View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        final View defaultButtonPanel = parentPanel.findViewById(R.id.buttonPanel);

        ViewGroup customPanel = (ViewGroup) parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);

        final View customTopPanel = customPanel.findViewById(R.id.topPanel);
        final View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        final View customButtonPanel = customPanel.findViewById(R.id.buttonPanel);

        final ViewGroup topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        final ViewGroup contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        final ViewGroup buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);

        setupContent(contentPanel);
        setupButtons(buttonPanel);
        setupTitle(topPanel);

        final boolean hasCustomPanel = customPanel != null && customPanel.getVisibility() != View.GONE;
        final boolean hasTopPanel = topPanel != null && topPanel.getVisibility() != View.GONE;
        final boolean hasButtonPanel = buttonPanel != null && buttonPanel.getVisibility() != View.GONE;
        final boolean hasDefaultTopPanel = defaultTopPanel != null && defaultTopPanel.getVisibility() != View.GONE;
        final boolean hasDefaultContentPanel = defaultContentPanel != null && defaultContentPanel.getVisibility() != View.GONE;
        final boolean hasCustomTitle = mCustomTitleView != null && mCustomTitleView.getVisibility() != View.GONE;

        if ((hasCustomPanel && !hasDefaultTopPanel && !hasDefaultContentPanel) || hasCustomTitle) {
            adjustParentPanelPadding(parentPanel);
        }
        if (!(!hasCustomPanel || !hasDefaultTopPanel || hasDefaultContentPanel)) {
            adjustTopPanelPadding(parentPanel);
        }
        adjustButtonsPadding();

        if (!parentPanel.isInTouchMode()) {
            if (!requestFocusForContent(hasCustomPanel ? customPanel : contentPanel)) {
                requestFocusForDefaultButton();
            }
        }

        if (hasTopPanel) {
            if (mScrollView != null) {
                mScrollView.setClipToPadding(true);
            }
        }

        if (mListView instanceof RecycleListView) {
            ((RecycleListView) mListView).setHasDecor(hasTopPanel, hasButtonPanel);
        }

        if (!hasCustomPanel) {
            final View content = mListView != null ? mListView : mScrollView;
            if (content != null) {
                final int indicators = (hasTopPanel ? ViewCompat.SCROLL_INDICATOR_TOP : 0) | (hasButtonPanel ? ViewCompat.SCROLL_INDICATOR_BOTTOM : 0);
                setScrollIndicators(contentPanel, content, indicators, ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM);
            }
        }

        final ListView listView = mListView;
        if (listView != null && mAdapter != null) {
            listView.setAdapter(mAdapter);
            SeslAdapterViewReflector.semSetBottomColor(listView, 0);
            final int checkedItem = mCheckedItem;
            if (checkedItem > -1) {
                listView.setItemChecked(checkedItem, true);
                listView.setSelection(checkedItem);
            }
        }
    }

    private boolean requestFocusForContent(View view) {
        if (view != null && view.requestFocus()) {
            return true;
        }
        if (mListView != null) {
            mListView.setSelection(0);
            return true;
        } else {
            return false;
        }
    }

    private void requestFocusForDefaultButton() {
        if (mButtonPositive.getVisibility() == View.VISIBLE) {
            mButtonPositive.requestFocus();
        } else if (mButtonNegative.getVisibility() == View.VISIBLE) {
            mButtonNegative.requestFocus();
        } else if (mButtonNeutral.getVisibility() == View.VISIBLE) {
            mButtonNeutral.requestFocus();
        }
    }

    private void setScrollIndicators(ViewGroup contentPanel, View content, final int indicators, final int mask) {
        View indicatorUp = mWindow.findViewById(R.id.scrollIndicatorUp);
        View indicatorDown = mWindow.findViewById(R.id.scrollIndicatorDown);

        if (Build.VERSION.SDK_INT >= 23) {
            ViewCompat.setScrollIndicators(content, indicators, mask);
            if (indicatorUp != null) {
                contentPanel.removeView(indicatorUp);
            }
            if (indicatorDown != null) {
                contentPanel.removeView(indicatorDown);
            }
        } else {
            if (indicatorUp != null && (indicators & ViewCompat.SCROLL_INDICATOR_TOP) == 0) {
                contentPanel.removeView(indicatorUp);
                indicatorUp = null;
            }
            if (indicatorDown != null && (indicators & ViewCompat.SCROLL_INDICATOR_BOTTOM) == 0) {
                contentPanel.removeView(indicatorDown);
                indicatorDown = null;
            }

            if (indicatorUp != null || indicatorDown != null) {
                final View top = indicatorUp;
                final View bottom = indicatorDown;

                if (mMessage != null) {
                    mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            manageScrollIndicators(v, top, bottom);
                        }
                    });
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            manageScrollIndicators(mScrollView, top, bottom);
                        }
                    });
                } else if (mListView != null) {
                    mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                        }

                        @Override
                        public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            manageScrollIndicators(v, top, bottom);
                        }
                    });
                    mListView.post(new Runnable() {
                        @Override
                        public void run() {
                            manageScrollIndicators(mListView, top, bottom);
                        }
                    });
                } else {
                    if (top != null) {
                        contentPanel.removeView(top);
                    }
                    if (bottom != null) {
                        contentPanel.removeView(bottom);
                    }
                }
            }
        }
    }

    private void setupCustomContent(ViewGroup customPanel) {
        final View customView;
        if (mView != null) {
            customView = mView;
        } else if (mViewLayoutResId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }

        final boolean hasCustomView = customView != null;
        if (!hasCustomView || !canTextInput(customView)) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        if (hasCustomView) {
            final FrameLayout custom = (FrameLayout) mWindow.findViewById(R.id.custom);
            custom.addView(customView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

            if (mViewSpacingSpecified) {
                custom.setPadding(mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
            }

            if (mListView != null) {
                if (customPanel.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
                } else {
                    ((LinearLayoutCompat.LayoutParams) customPanel.getLayoutParams()).weight = 0.0f;
                }
            }
        } else {
            customPanel.setVisibility(View.GONE);
        }
    }

    private void checkMaxFontScale(TextView view, int size) {
        float fontScale = this.mContext.getResources().getConfiguration().fontScale;
        if (fontScale > 1.3f) {
            view.setTextSize(0, (((float) size) / fontScale) * 1.3f);
        }
    }

    private void adjustButtonsPadding() {
        int btnTextSize = mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_button_text_size : R.dimen.sesl_dialog_button_text_size);

        if (mButtonPositive.getVisibility() != View.GONE) {
            this.mButtonPositive.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) btnTextSize);
            checkMaxFontScale(mButtonPositive, btnTextSize);
        }
        if (mButtonNegative.getVisibility() != View.GONE) {
            mButtonNegative.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) btnTextSize);
            checkMaxFontScale(mButtonNegative, btnTextSize);
        }
        if (mButtonNeutral.getVisibility() != View.GONE) {
            mButtonNeutral.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) btnTextSize);
            checkMaxFontScale(mButtonNeutral, btnTextSize);
        }
    }

    private void adjustParentPanelPadding(View view) {
        view.setPadding(0, 0, 0, 0);
    }

    private void adjustTopPanelPadding(View view) {
        int padding = mContext.getResources().getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal);
        view.findViewById(R.id.title_template).setPadding(padding, 0, padding, 0);
    }

    private void setupTitle(ViewGroup topPanel) {
        if (mCustomTitleView != null) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

            topPanel.addView(mCustomTitleView, 0, lp);

            View titleTemplate = mWindow.findViewById(R.id.title_template);
            titleTemplate.setVisibility(View.GONE);
        } else {
            mIconView = (ImageView) mWindow.findViewById(android.R.id.icon);

            final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
            if (hasTextTitle && mShowTitle) {
                mTitleView = (TextView) mWindow.findViewById(R.id.alertTitle);
                mTitleView.setText(mTitle);
                checkMaxFontScale(mTitleView, mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_title_text_size : R.dimen.sesl_dialog_title_text_size));

                if (mIconId != 0) {
                    mIconView.setImageResource(mIconId);
                } else if (mIcon != null) {
                    mIconView.setImageDrawable(mIcon);
                } else {
                    mTitleView.setPadding(mIconView.getPaddingLeft(), mIconView.getPaddingTop(), mIconView.getPaddingRight(), mIconView.getPaddingBottom());
                    mIconView.setVisibility(View.GONE);
                }
            } else {
                final View titleTemplate = mWindow.findViewById(R.id.title_template);
                titleTemplate.setVisibility(View.GONE);
                mIconView.setVisibility(View.GONE);
                topPanel.setVisibility(View.GONE);
            }
        }
    }

    private void setupContent(ViewGroup contentPanel) {
        mScrollView = (NestedScrollView) mWindow.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);
        mScrollView.setNestedScrollingEnabled(false);

        mMessageView = (TextView) contentPanel.findViewById(android.R.id.message);
        if (mMessageView == null) {
            return;
        }

        if (mMessage != null) {
            mMessageView.setText(mMessage);
            checkMaxFontScale(mMessageView, mContext.getResources().getDimensionPixelSize(mIsOneUI4 ? R.dimen.sesl4_dialog_body_text_size : R.dimen.sesl_dialog_body_text_size));
        } else {
            mMessageView.setVisibility(View.GONE);
            mScrollView.removeView(mMessageView);

            if (mListView != null) {
                final ViewGroup scrollParent = (ViewGroup) mScrollView.getParent();
                final int childIndex = scrollParent.indexOfChild(mScrollView);
                scrollParent.removeViewAt(childIndex);
                scrollParent.addView(mListView, childIndex, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            } else {
                contentPanel.setVisibility(View.GONE);
            }
        }
    }

    static void manageScrollIndicators(View v, View upIndicator, View downIndicator) {
        if (upIndicator != null) {
            upIndicator.setVisibility(v.canScrollVertically(-1) ? View.VISIBLE : View.INVISIBLE);
        }
        if (downIndicator != null) {
            downIndicator.setVisibility(v.canScrollVertically(1) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void setupButtons(ViewGroup buttonPanel) {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;

        boolean showBtnBg = Settings.System.getInt(mContext.getContentResolver(), "show_button_background", 0) == 1;
        boolean isSecThemeApplied = Settings.System.getString(mContext.getContentResolver(), "current_sec_active_themepackage") != null;

        TypedValue colorBackground = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.colorBackground, colorBackground, true);
        int btnBgColor = colorBackground.resourceId > 0 ? mContext.getResources().getColor(colorBackground.resourceId) : -1;

        TypedValue colorPrimaryDark = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorPrimaryDark, colorPrimaryDark, true);

        int themeTextColor;
        if (colorPrimaryDark.resourceId != 0) {
            themeTextColor = mContext.getResources().getColor(colorPrimaryDark.resourceId);
        } else {
            themeTextColor = colorPrimaryDark.data;
        }

        mButtonPositive = (Button) buttonPanel.findViewById(android.R.id.button1);
        mButtonPositive.setOnClickListener(mButtonHandler);
        if (isSecThemeApplied) {
            mButtonPositive.setTextColor(themeTextColor);
        }
        if (Build.VERSION.SDK_INT > 26) {
            if (colorBackground.resourceId > 0) {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonPositive, showBtnBg, btnBgColor);
            } else {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonPositive, showBtnBg);
            }
        } else if (showBtnBg) {
            mButtonPositive.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        if (TextUtils.isEmpty(mButtonPositiveText) && mButtonPositiveIcon == null) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            if (mButtonPositiveIcon != null) {
                mButtonPositiveIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonPositive.setCompoundDrawables(mButtonPositiveIcon, null, null, null);
            }
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = buttonPanel.findViewById(android.R.id.button2);
        mButtonNegative.setOnClickListener(mButtonHandler);
        if (isSecThemeApplied) {
            mButtonNegative.setTextColor(themeTextColor);
        }
        if (Build.VERSION.SDK_INT > 26) {
            if (colorBackground.resourceId > 0) {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonNegative, showBtnBg, btnBgColor);
            } else {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonNegative, showBtnBg);
            }
        } else if (showBtnBg) {
            this.mButtonNegative.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        if (TextUtils.isEmpty(mButtonNegativeText) && mButtonNegativeIcon == null) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            if (mButtonNegativeIcon != null) {
                mButtonNegativeIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonNegative.setCompoundDrawables(mButtonNegativeIcon, null, null, null);
            }
            mButtonNegative.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (Button) buttonPanel.findViewById(android.R.id.button3);
        mButtonNeutral.setOnClickListener(mButtonHandler);
        if (isSecThemeApplied) {
            mButtonNeutral.setTextColor(themeTextColor);
        }
        if (Build.VERSION.SDK_INT > 26) {
            if (colorBackground.resourceId > 0) {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonNeutral, showBtnBg, btnBgColor);
            } else {
                SeslTextViewReflector.semSetButtonShapeEnabled(mButtonNeutral, showBtnBg);
            }
        } else if (showBtnBg) {
            mButtonNeutral.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        if (TextUtils.isEmpty(mButtonNeutralText) && mButtonNeutralIcon == null) {
            mButtonNeutral.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            if (mButtonNeutralIcon != null) {
                mButtonNeutralIcon.setBounds(0, 0, mButtonIconDimen, mButtonIconDimen);
                mButtonNeutral.setCompoundDrawables(mButtonNeutralIcon, null, null, null);
            }
            mButtonNeutral.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        if (shouldCenterSingleButton(mContext)) {
            if (whichButtons == BIT_BUTTON_POSITIVE) {
                centerButton(mButtonPositive);
            } else if (whichButtons == BIT_BUTTON_NEGATIVE) {
                centerButton(mButtonNegative);
            } else if (whichButtons == BIT_BUTTON_NEUTRAL) {
                centerButton(mButtonNeutral);
            }
        }

        final boolean hasButtons = whichButtons != 0;
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }

        boolean hasNeutralBtn = mButtonNeutral.getVisibility() == View.VISIBLE;
        boolean hasPositiveBtn = mButtonPositive.getVisibility() == View.VISIBLE;
        boolean hasNegativeBtn = mButtonNegative.getVisibility() == View.VISIBLE;

        View divider2 = mWindow.findViewById(R.id.sem_divider2);
        if (divider2 != null && ((hasNeutralBtn && hasPositiveBtn) || (hasNeutralBtn && hasNegativeBtn))) {
            divider2.setVisibility(View.VISIBLE);
        }
        View divider1 = mWindow.findViewById(R.id.sem_divider1);
        if (divider1 != null && hasPositiveBtn && hasNegativeBtn) {
            divider1.setVisibility(View.VISIBLE);
        }
    }

    private void centerButton(Button button) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.weight = 0.5f;
        button.setLayoutParams(params);
    }

    public static class RecycleListView extends ListView {
        private final int mPaddingTopNoTitle;
        private final int mPaddingBottomNoButtons;

        public RecycleListView(Context context) {
            this(context, null);
        }

        public RecycleListView(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RecycleListView);
            mPaddingBottomNoButtons = ta.getDimensionPixelOffset(R.styleable.RecycleListView_paddingBottomNoButtons, -1);
            mPaddingTopNoTitle = ta.getDimensionPixelOffset(R.styleable.RecycleListView_paddingTopNoTitle, -1);
        }

        public void setHasDecor(boolean hasTitle, boolean hasButtons) {
            if (!hasButtons || !hasTitle) {
                final int paddingLeft = getPaddingLeft();
                final int paddingTop = hasTitle ? getPaddingTop() : mPaddingTopNoTitle;
                final int paddingRight = getPaddingRight();
                final int paddingBottom = hasButtons ? getPaddingBottom() : mPaddingBottomNoButtons;
                setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            }
        }
    }

    public static class AlertParams {
        public ListAdapter mAdapter;
        public boolean[] mCheckedItems;
        public final Context mContext;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public Drawable mIcon;
        public final LayoutInflater mInflater;
        public String mIsCheckedColumn;
        public boolean mIsMultiChoice;
        public boolean mIsSingleChoice;
        public CharSequence[] mItems;
        public String mLabelColumn;
        public CharSequence mMessage;
        public Drawable mNegativeButtonIcon;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNegativeButtonText;
        public Drawable mNeutralButtonIcon;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnCancelListener mOnCancelListener;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public DialogInterface.OnClickListener mOnClickListener;
        public DialogInterface.OnDismissListener mOnDismissListener;
        public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
        public DialogInterface.OnKeyListener mOnKeyListener;
        public OnPrepareListViewListener mOnPrepareListViewListener;
        public Drawable mPositiveButtonIcon;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mPositiveButtonText;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public int mViewSpacingTop;
        public int mIconId = 0;
        public int mIconAttrId = 0;
        public boolean mViewSpacingSpecified = false;
        public int mCheckedItem = -1;
        public boolean mRecycleOnMeasure = true;
        public boolean mCancelable = true;

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView listView);
        }

        public AlertParams(Context context) {
            mContext = context;
            mCancelable = true;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void apply(SamsungAlertController dialog) {
            if (mCustomTitleView != null) {
                dialog.setCustomTitle(mCustomTitleView);
            } else {
                if (mTitle != null) {
                    dialog.setTitle(mTitle);
                }
                if (mIcon != null) {
                    dialog.setIcon(mIcon);
                }
                if (mIconId != 0) {
                    dialog.setIcon(mIconId);
                }
                if (mIconAttrId != 0) {
                    dialog.setIcon(dialog.getIconAttributeResId(mIconAttrId));
                }
            }
            if (mMessage != null) {
                dialog.setMessage(mMessage);
            }
            if (mPositiveButtonText != null || mPositiveButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText, mPositiveButtonListener, null, mPositiveButtonIcon);
            }
            if (mNegativeButtonText != null || mNegativeButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText, mNegativeButtonListener, null, mNegativeButtonIcon);
            }
            if (mNeutralButtonText != null || mNeutralButtonIcon != null) {
                dialog.setButton(DialogInterface.BUTTON_NEUTRAL, mNeutralButtonText, mNeutralButtonListener, null, mNeutralButtonIcon);
            }
            if ((mItems != null) || (mCursor != null) || (mAdapter != null)) {
                createListView(dialog);
            }
            if (mView != null) {
                if (mViewSpacingSpecified) {
                    dialog.setView(mView, mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
                } else {
                    dialog.setView(mView);
                }
            } else if (mViewLayoutResId != 0) {
                dialog.setView(mViewLayoutResId);
            }
        }

        private void createListView(final SamsungAlertController dialog) {
            final RecycleListView listView = (RecycleListView) mInflater.inflate(dialog.mListLayout, null);
            final ListAdapter adapter;

            if (mIsMultiChoice) {
                if (mCursor == null) {
                    adapter = new ArrayAdapter<CharSequence>(mContext, dialog.mMultiChoiceItemLayout, android.R.id.text1, mItems) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            if (mCheckedItems != null) {
                                boolean isItemChecked = mCheckedItems[position];
                                if (isItemChecked) {
                                    listView.setItemChecked(position, true);
                                }
                            }
                            return view;
                        }
                    };
                } else {
                    adapter = new CursorAdapter(mContext, mCursor, false) {
                        private final int mLabelIndex;
                        private final int mIsCheckedIndex;

                        {
                            final Cursor cursor = getCursor();
                            mLabelIndex = cursor.getColumnIndexOrThrow(mLabelColumn);
                            mIsCheckedIndex = cursor.getColumnIndexOrThrow(mIsCheckedColumn);
                        }

                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            CheckedTextView text = (CheckedTextView) view.findViewById(android.R.id.text1);
                            text.setText(cursor.getString(mLabelIndex));
                            listView.setItemChecked(cursor.getPosition(), cursor.getInt(mIsCheckedIndex) == 1);
                        }

                        @Override
                        public View newView(Context context, Cursor cursor, ViewGroup parent) {
                            return mInflater.inflate(dialog.mMultiChoiceItemLayout, parent, false);
                        }

                    };
                }
            } else {
                final int layout;
                if (mIsSingleChoice) {
                    layout = dialog.mSingleChoiceItemLayout;
                } else {
                    layout = dialog.mListItemLayout;
                }

                if (mCursor != null) {
                    adapter = new SimpleCursorAdapter(mContext, layout, mCursor, new String[] { mLabelColumn }, new int[] { android.R.id.text1 });
                } else if (mAdapter != null) {
                    adapter = mAdapter;
                } else {
                    adapter = new CheckedItemAdapter(mContext, layout, android.R.id.text1, mItems);
                }
            }

            if (mOnPrepareListViewListener != null) {
                mOnPrepareListViewListener.onPrepareListView(listView);
            }

            dialog.mAdapter = adapter;
            dialog.mCheckedItem = mCheckedItem;

            if (mOnClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        mOnClickListener.onClick(dialog.mDialog, position);
                        if (!mIsSingleChoice) {
                            dialog.mDialog.dismiss();
                        }
                    }
                });
            } else if (mOnCheckboxClickListener != null) {
                listView.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        if (mCheckedItems != null) {
                            mCheckedItems[position] = listView.isItemChecked(position);
                        }
                        mOnCheckboxClickListener.onClick(dialog.mDialog, position, listView.isItemChecked(position));
                    }
                });
            }

            if (mOnItemSelectedListener != null) {
                listView.setOnItemSelectedListener(mOnItemSelectedListener);
            }

            if (mIsSingleChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            } else if (mIsMultiChoice) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
            dialog.mListView = listView;
        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
