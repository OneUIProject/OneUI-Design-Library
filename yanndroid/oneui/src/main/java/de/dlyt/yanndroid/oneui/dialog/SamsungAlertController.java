package de.dlyt.yanndroid.oneui.dialog;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EdgeEffect;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;
import androidx.core.view.ViewCompat;

import java.lang.ref.WeakReference;

import de.dlyt.yanndroid.oneui.NestedScrollView;
import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.utils.ReflectUtils;
import de.dlyt.yanndroid.oneui.widget.SamsungEdgeEffect;

public class SamsungAlertController {
    public final int mButtonIconDimen;
    public final Context mContext;
    public final AppCompatDialog mDialog;
    public final Window mWindow;
    public ListAdapter mAdapter;
    public int mAlertDialogLayout;
    public Button mButtonNegative;
    public Drawable mButtonNegativeIcon;
    public Message mButtonNegativeMessage;
    public CharSequence mButtonNegativeText;
    public Button mButtonNeutral;
    public Drawable mButtonNeutralIcon;
    public Message mButtonNeutralMessage;
    public CharSequence mButtonNeutralText;
    public int mButtonPanelLayoutHint = 0;
    public int mButtonPanelSideLayout;
    public Button mButtonPositive;
    public Drawable mButtonPositiveIcon;
    public Message mButtonPositiveMessage;
    public CharSequence mButtonPositiveText;
    public int mCheckedItem = -1;
    public View mCustomTitleView;
    public Handler mHandler;
    public final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        public void onClick(View var1) {
            Message var3;
            label33:
            {
                SamsungAlertController var2 = SamsungAlertController.this;
                Message var4;
                if (var1 == var2.mButtonPositive) {
                    var4 = var2.mButtonPositiveMessage;
                    if (var4 != null) {
                        var3 = Message.obtain(var4);
                        break label33;
                    }
                }

                var2 = SamsungAlertController.this;
                if (var1 == var2.mButtonNegative) {
                    var4 = var2.mButtonNegativeMessage;
                    if (var4 != null) {
                        var3 = Message.obtain(var4);
                        break label33;
                    }
                }

                var2 = SamsungAlertController.this;
                if (var1 == var2.mButtonNeutral) {
                    var3 = var2.mButtonNeutralMessage;
                    if (var3 != null) {
                        var3 = Message.obtain(var3);
                        break label33;
                    }
                }

                var3 = null;
            }

            if (var3 != null) {
                var3.sendToTarget();
            }

            SamsungAlertController var5 = SamsungAlertController.this;
            var5.mHandler.obtainMessage(1, var5.mDialog).sendToTarget();
        }
    };
    public Drawable mIcon;
    public int mIconId = 0;
    public ImageView mIconView;
    public int mLastOrientation;
    public int mListItemLayout;
    public int mListLayout;
    public ListView mListView;
    public CharSequence mMessage;
    public TextView mMessageView;
    public int mMultiChoiceItemLayout;
    public NestedScrollView mScrollView;
    public boolean mShowTitle;
    public int mSingleChoiceItemLayout;
    public CharSequence mTitle;
    public TextView mTitleView;
    public View mView;
    public int mViewLayoutResId;
    public int mViewSpacingBottom;
    public int mViewSpacingLeft;
    public int mViewSpacingRight;
    public boolean mViewSpacingSpecified = false;
    public int mViewSpacingTop;

    public SamsungAlertController(Context var1, AppCompatDialog var2, Window var3) {
        this.mContext = var1;
        this.mDialog = var2;
        this.mWindow = var3;
        this.mHandler = new ButtonHandler(var2);
        TypedArray var4 = var1.obtainStyledAttributes((AttributeSet) null, R.styleable.SamsungAlertDialog, R.attr.alertDialogStyle, 0);
        this.mAlertDialogLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_android_layout, 0);
        this.mButtonPanelSideLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_buttonPanelSideLayout, 0);
        this.mListLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_listLayout, 0);
        this.mMultiChoiceItemLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_multiChoiceItemLayout, 0);
        this.mSingleChoiceItemLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_singleChoiceItemLayout, 0);
        this.mListItemLayout = var4.getResourceId(R.styleable.SamsungAlertDialog_listItemLayout, 0);
        this.mShowTitle = var4.getBoolean(R.styleable.SamsungAlertDialog_showTitle, true);
        this.mButtonIconDimen = var4.getDimensionPixelSize(R.styleable.SamsungAlertDialog_buttonIconDimen, 0);
        var4.recycle();
        var3.setGravity(Gravity.BOTTOM);
        var2.supportRequestWindowFeature(1);
    }

    public static boolean canTextInput(View var0) {
        if (var0.onCheckIsTextEditor()) {
            return true;
        } else if (!(var0 instanceof ViewGroup)) {
            return false;
        } else {
            ViewGroup var3 = (ViewGroup) var0;
            int var1 = var3.getChildCount();

            int var2;
            do {
                if (var1 <= 0) {
                    return false;
                }

                var2 = var1 - 1;
                var1 = var2;
            } while (!canTextInput(var3.getChildAt(var2)));

            return true;
        }
    }

    public static void manageScrollIndicators(View var0, View var1, View var2) {
        byte var3 = 0;
        byte var4;
        if (var1 != null) {
            if (var0.canScrollVertically(-1)) {
                var4 = 0;
            } else {
                var4 = 4;
            }

            var1.setVisibility(var4);
        }

        if (var2 != null) {
            if (var0.canScrollVertically(1)) {
                var4 = var3;
            } else {
                var4 = 4;
            }

            var2.setVisibility(var4);
        }

    }

    public static boolean shouldCenterSingleButton(Context var0) {
        TypedValue var1 = new TypedValue();
        Resources.Theme var4 = var0.getTheme();
        int var2 = R.attr.alertDialogCenterButtons;
        boolean var3 = true;
        var4.resolveAttribute(var2, var1, true);
        if (var1.data == 0) {
            var3 = false;
        }

        return var3;
    }

    @SuppressLint("WrongConstant")
    private void adjustButtonsPadding() {
        int var1 = this.mContext.getResources().getDimensionPixelSize(R.dimen.sesl_dialog_button_text_size);
        if (this.mButtonPositive.getVisibility() != 8) {
            this.mButtonPositive.setTextSize(0, (float) var1);
            this.checkMaxFontScale(this.mButtonPositive, var1);
        }

        if (this.mButtonNegative.getVisibility() != 8) {
            this.mButtonNegative.setTextSize(0, (float) var1);
            this.checkMaxFontScale(this.mButtonNegative, var1);
        }

        if (this.mButtonNeutral.getVisibility() != 8) {
            this.mButtonNeutral.setTextSize(0, (float) var1);
            this.checkMaxFontScale(this.mButtonNeutral, var1);
        }

    }

    private void adjustParentPanelPadding(View var1) {
        var1.setPadding(0, 0, 0, 0);
    }

    private void adjustTopPanelPadding(View var1) {
        View var2 = var1.findViewById(R.id.title_template);
        Resources var3 = this.mContext.getResources();
        var2.setPadding(var3.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0, var3.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0);
    }

    private void centerButton(Button var1) {
        LinearLayout.LayoutParams var2 = (LinearLayout.LayoutParams) var1.getLayoutParams();
        var2.gravity = 1;
        var2.weight = 0.5F;
        var1.setLayoutParams(var2);
    }

    private void checkMaxFontScale(TextView var1, int var2) {
        float var3 = this.mContext.getResources().getConfiguration().fontScale;
        if (var3 > 1.3F) {
            var1.setTextSize(0, (float) var2 / var3 * 1.3F);
        }

    }

    private ViewGroup resolvePanel(View var1, View var2) {
        if (var1 == null) {
            var1 = var2;
            if (var2 instanceof ViewStub) {
                var1 = ((ViewStub) var2).inflate();
            }

            return (ViewGroup) var1;
        } else {
            if (var2 != null) {
                ViewParent var3 = var2.getParent();
                if (var3 instanceof ViewGroup) {
                    ((ViewGroup) var3).removeView(var2);
                }
            }

            var2 = var1;
            if (var1 instanceof ViewStub) {
                var2 = ((ViewStub) var1).inflate();
            }

            return (ViewGroup) var2;
        }
    }

    private int selectContentView() {
        int var1 = this.mButtonPanelSideLayout;
        if (var1 == 0) {
            return this.mAlertDialogLayout;
        } else {
            return this.mButtonPanelLayoutHint == 1 ? var1 : this.mAlertDialogLayout;
        }
    }

    private void setScrollIndicators(ViewGroup var1, View var2, int var3, int var4) {
        View var5 = this.mWindow.findViewById(R.id.scrollIndicatorUp);
        View var6 = this.mWindow.findViewById(R.id.scrollIndicatorDown);
        if (Build.VERSION.SDK_INT >= 23) {
            ViewCompat.setScrollIndicators(var2, var3, var4);
            if (var5 != null) {
                var1.removeView(var5);
            }

            if (var6 != null) {
                var1.removeView(var6);
            }
        } else {
            ListView var7 = null;
            var2 = var5;
            if (var5 != null) {
                var2 = var5;
                if ((var3 & 1) == 0) {
                    var1.removeView(var5);
                    var2 = null;
                }
            }

            if (var6 != null && (var3 & 2) == 0) {
                var1.removeView(var6);
                var6 = var7;
            }

            if (var2 != null || var6 != null) {
                if (this.mMessage != null) {
                    final View finalVar = var2;
                    final View finalVar1 = var6;
                    this.mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        public void onScrollChange(NestedScrollView var1, int var2x, int var3, int var4, int var5) {
                            SamsungAlertController.manageScrollIndicators(var1, finalVar, finalVar1);
                        }
                    });
                    final View finalVar2 = var2;
                    final View finalVar3 = var6;
                    this.mScrollView.post(new Runnable() {
                        public void run() {
                            SamsungAlertController.manageScrollIndicators(SamsungAlertController.this.mScrollView, finalVar2, finalVar3);
                        }
                    });
                } else {
                    var7 = this.mListView;
                    if (var7 != null) {
                        final View finalVar4 = var2;
                        final View finalVar5 = var6;
                        var7.setOnScrollListener(new AbsListView.OnScrollListener() {
                            public void onScroll(AbsListView var1, int var2x, int var3, int var4) {
                                SamsungAlertController.manageScrollIndicators(var1, finalVar4, finalVar5);
                            }

                            public void onScrollStateChanged(AbsListView var1, int var2x) {
                            }
                        });
                        final View finalVar6 = var2;
                        final View finalVar7 = var6;
                        this.mListView.post(new Runnable() {
                            public void run() {
                                SamsungAlertController.manageScrollIndicators(SamsungAlertController.this.mListView, finalVar6, finalVar7);
                            }
                        });
                    } else {
                        if (var2 != null) {
                            var1.removeView(var2);
                        }

                        if (var6 != null) {
                            var1.removeView(var6);
                        }
                    }
                }
            }
        }

    }

    @SuppressLint("WrongConstant")
    private void setupButtons(ViewGroup var1) {
        ContentResolver var2 = this.mContext.getContentResolver();
        boolean var3 = true;
        boolean var4;
        if (var2 != null && Settings.System.getInt(var2, "show_button_background", 0) == 1) {
            var4 = true;
        } else {
            var4 = false;
        }

        TypedValue var10 = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16842801, var10, true);
        int var5 = -1;
        if (var10.resourceId > 0) {
            var5 = this.mContext.getResources().getColor(var10.resourceId, null);
        }

        this.mButtonPositive = (Button) var1.findViewById(R.id.button1);
        this.mButtonPositive.setOnClickListener(this.mButtonHandler);
        if (Build.VERSION.SDK_INT > 26) {
            if (var10.resourceId > 0) {
                ReflectUtils.genericInvokeMethod(this.mButtonPositive, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4, var5);
            } else {
                ReflectUtils.genericInvokeMethod(this.mButtonPositive, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4);
            }
        } else if (var4) {
            this.mButtonPositive.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        int var6;
        Drawable var7;
        if (TextUtils.isEmpty(this.mButtonPositiveText) && this.mButtonPositiveIcon == null) {
            this.mButtonPositive.setVisibility(8);
            var6 = 0;
        } else {
            this.mButtonPositive.setText(this.mButtonPositiveText);
            var7 = this.mButtonPositiveIcon;
            if (var7 != null) {
                var6 = this.mButtonIconDimen;
                var7.setBounds(0, 0, var6, var6);
                this.mButtonPositive.setCompoundDrawables(this.mButtonPositiveIcon, (Drawable) null, (Drawable) null, (Drawable) null);
            }

            this.mButtonPositive.setVisibility(0);
            var6 = 1;
        }

        this.mButtonNegative = (Button) var1.findViewById(R.id.button2);
        this.mButtonNegative.setOnClickListener(this.mButtonHandler);
        if (Build.VERSION.SDK_INT > 26) {
            if (var10.resourceId > 0) {
                ReflectUtils.genericInvokeMethod(this.mButtonNegative, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4, var5);
            } else {
                ReflectUtils.genericInvokeMethod(this.mButtonNegative, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4);
            }
        } else if (var4) {
            this.mButtonNegative.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        if (TextUtils.isEmpty(this.mButtonNegativeText) && this.mButtonNegativeIcon == null) {
            this.mButtonNegative.setVisibility(8);
        } else {
            this.mButtonNegative.setText(this.mButtonNegativeText);
            var7 = this.mButtonNegativeIcon;
            if (var7 != null) {
                int var8 = this.mButtonIconDimen;
                var7.setBounds(0, 0, var8, var8);
                this.mButtonNegative.setCompoundDrawables(this.mButtonNegativeIcon, (Drawable) null, (Drawable) null, (Drawable) null);
            }

            this.mButtonNegative.setVisibility(0);
            var6 |= 2;
        }

        this.mButtonNeutral = (Button) var1.findViewById(R.id.button3);
        this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
        if (Build.VERSION.SDK_INT > 26) {
            if (var10.resourceId > 0) {
                ReflectUtils.genericInvokeMethod(this.mButtonNeutral, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4, var5);
            } else {
                ReflectUtils.genericInvokeMethod(this.mButtonNeutral, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetButtonShapeEnabled" : "semSetButtonShapeEnabled", var4);
            }
        } else if (var4) {
            this.mButtonNeutral.setBackgroundResource(R.drawable.sesl_dialog_btn_show_button_shapes_background);
        }

        if (TextUtils.isEmpty(this.mButtonNeutralText) && this.mButtonNeutralIcon == null) {
            this.mButtonNeutral.setVisibility(8);
        } else {
            this.mButtonNeutral.setText(this.mButtonNeutralText);
            Drawable var11 = this.mButtonPositiveIcon;
            if (var11 != null) {
                var5 = this.mButtonIconDimen;
                var11.setBounds(0, 0, var5, var5);
                this.mButtonPositive.setCompoundDrawables(this.mButtonPositiveIcon, (Drawable) null, (Drawable) null, (Drawable) null);
            }

            this.mButtonNeutral.setVisibility(0);
            var6 |= 4;
        }

        if (shouldCenterSingleButton(this.mContext)) {
            if (var6 == 1) {
                this.centerButton(this.mButtonPositive);
            } else if (var6 == 2) {
                this.centerButton(this.mButtonNegative);
            } else if (var6 == 4) {
                this.centerButton(this.mButtonNeutral);
            }
        }

        boolean var13;
        if (var6 != 0) {
            var13 = true;
        } else {
            var13 = false;
        }

        if (!var13) {
            var1.setVisibility(8);
        }

        if (this.mButtonNeutral.getVisibility() == 0) {
            var13 = true;
        } else {
            var13 = false;
        }

        boolean var12;
        if (this.mButtonPositive.getVisibility() == 0) {
            var12 = true;
        } else {
            var12 = false;
        }

        if (this.mButtonNegative.getVisibility() != 0) {
            var3 = false;
        }

        View var9 = this.mWindow.findViewById(R.id.sem_divider2);
        if (var9 != null && (var13 && var12 || var13 && var3)) {
            var9.setVisibility(0);
        }

        var9 = this.mWindow.findViewById(R.id.sem_divider1);
        if (var9 != null && var12 && var3) {
            var9.setVisibility(0);
        }

    }

    @SuppressLint({"WrongConstant", "ResourceType"})
    private void setupContent(ViewGroup var1) {
        this.mScrollView = (NestedScrollView) this.mWindow.findViewById(R.id.scrollView);
        this.mScrollView.setFocusable(false);
        this.mScrollView.setNestedScrollingEnabled(false);
        this.mMessageView = (TextView) var1.findViewById(16908299);
        TextView var2 = this.mMessageView;
        if (var2 != null) {
            CharSequence var3 = this.mMessage;
            if (var3 != null) {
                var2.setText(var3);
            } else {
                var2.setVisibility(8);
                this.mScrollView.removeView(this.mMessageView);
                if (this.mListView != null) {
                    var1 = (ViewGroup) this.mScrollView.getParent();
                    int var4 = var1.indexOfChild(this.mScrollView);
                    var1.removeViewAt(var4);
                    var1.addView(this.mListView, var4, new ViewGroup.LayoutParams(-1, -1));
                } else {
                    var1.setVisibility(8);
                }
            }

        }
    }

    @SuppressLint("WrongConstant")
    private void setupCustomContent(ViewGroup var1) {
        View var2 = this.mView;
        boolean var3 = false;
        if (var2 == null) {
            if (this.mViewLayoutResId != 0) {
                var2 = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, var1, false);
            } else {
                var2 = null;
            }
        }

        if (var2 != null) {
            var3 = true;
        }

        if (!var3 || !canTextInput(var2)) {
            this.mWindow.setFlags(131072, 131072);
        }

        if (var3) {
            FrameLayout var4 = (FrameLayout) this.mWindow.findViewById(R.id.custom);
            var4.addView(var2, new ViewGroup.LayoutParams(-1, -1));
            if (this.mViewSpacingSpecified) {
                var4.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
            }

            if (this.mListView != null) {
                if (var1.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) var1.getLayoutParams()).weight = 0.0F;
                } else {
                    ((androidx.appcompat.widget.LinearLayoutCompat.LayoutParams) var1.getLayoutParams()).weight = 0.0F;
                }
            }
        } else {
            var1.setVisibility(8);
        }

    }

    @SuppressLint("WrongConstant")
    private void setupPaddings() {
        View var1 = this.mWindow.findViewById(R.id.parentPanel);
        View var2 = var1.findViewById(R.id.title_template);
        View var3 = var1.findViewById(R.id.scrollView);
        View var4 = var1.findViewById(R.id.buttonPanel);
        Resources var5 = this.mContext.getResources();
        ViewGroup var6 = (ViewGroup) var1.findViewById(R.id.customPanel);
        View var7 = var1.findViewById(R.id.topPanel);
        View var8 = var1.findViewById(R.id.contentPanel);
        boolean var9 = true;
        boolean var10;
        if (var6 != null && var6.getVisibility() != 8) {
            var10 = true;
        } else {
            var10 = false;
        }

        boolean var11;
        if (var7 != null && var7.getVisibility() != 8) {
            var11 = true;
        } else {
            var11 = false;
        }

        boolean var12;
        if (var8 != null && var8.getVisibility() != 8) {
            var12 = true;
        } else {
            var12 = false;
        }

        var7 = this.mCustomTitleView;
        if (var7 == null || var7.getVisibility() == 8) {
            var9 = false;
        }

        if ((!var10 || var11 || var12) && !var9) {
            var1.setPadding(0, var5.getDimensionPixelSize(R.dimen.sesl_dialog_title_padding_top), 0, 0);
        } else {
            var1.setPadding(0, 0, 0, 0);
        }

        if (var2 != null) {
            if (var10 && var11 && !var12) {
                var2.setPadding(var5.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0, var5.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0);
            } else {
                var2.setPadding(var5.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), 0, var5.getDimensionPixelSize(R.dimen.sesl_dialog_padding_horizontal), var5.getDimensionPixelSize(R.dimen.sesl_dialog_title_padding_bottom));
            }
        }

        if (var3 != null) {
            var3.setPadding(var5.getDimensionPixelSize(R.dimen.sesl_dialog_body_text_scroll_padding_start), 0, var5.getDimensionPixelSize(R.dimen.sesl_dialog_body_text_scroll_padding_end), var5.getDimensionPixelSize(R.dimen.sesl_dialog_body_text_padding_bottom));
        }

        if (var4 != null) {
            var4.setPadding(var5.getDimensionPixelSize(R.dimen.sesl_dialog_button_bar_padding_horizontal), 0, var5.getDimensionPixelSize(R.dimen.sesl_dialog_button_bar_padding_horizontal), var5.getDimensionPixelSize(R.dimen.sesl_dialog_button_bar_padding_bottom));
        }

    }

    @SuppressLint({"WrongConstant", "ResourceType"})
    private void setupTitle(ViewGroup var1) {
        if (this.mCustomTitleView != null) {
            ViewGroup.LayoutParams var2 = new ViewGroup.LayoutParams(-1, -2);
            var1.addView(this.mCustomTitleView, 0, var2);
            this.mWindow.findViewById(R.id.title_template).setVisibility(8);
        } else {
            this.mIconView = (ImageView) this.mWindow.findViewById(16908294);
            if (TextUtils.isEmpty(this.mTitle) ^ true && this.mShowTitle) {
                this.mTitleView = (TextView) this.mWindow.findViewById(R.id.alertTitle);
                this.mTitleView.setText(this.mTitle);
                this.checkMaxFontScale(this.mTitleView, this.mContext.getResources().getDimensionPixelSize(R.dimen.sesl_dialog_title_text_size));
                int var3 = this.mIconId;
                if (var3 != 0) {
                    this.mIconView.setImageResource(var3);
                } else {
                    Drawable var4 = this.mIcon;
                    if (var4 != null) {
                        this.mIconView.setImageDrawable(var4);
                    } else {
                        this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
                        this.mIconView.setVisibility(8);
                    }
                }
            } else {
                this.mWindow.findViewById(R.id.title_template).setVisibility(8);
                this.mIconView.setVisibility(8);
                var1.setVisibility(8);
            }
        }

    }

    @SuppressLint("WrongConstant")
    private void setupView() {
        final View var1 = this.mWindow.findViewById(R.id.parentPanel);
        var1.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View var1x, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
                var1x.post(new Runnable() {
                    public void run() {
                        int var1x = SamsungAlertController.this.mContext.getResources().getConfiguration().orientation;
                        SamsungAlertController var2 = SamsungAlertController.this;
                        if (var1x != var2.mLastOrientation) {
                            var2.setupPaddings();
                            var1.requestLayout();
                        }

                        var2 = SamsungAlertController.this;
                        var2.mLastOrientation = var2.mContext.getResources().getConfiguration().orientation;
                    }
                });
            }
        });
        View var2 = var1.findViewById(R.id.topPanel);
        View var3 = var1.findViewById(R.id.contentPanel);
        View var4 = var1.findViewById(R.id.buttonPanel);
        ViewGroup var5 = (ViewGroup) var1.findViewById(R.id.customPanel);
        this.setupCustomContent(var5);
        View var6 = var5.findViewById(R.id.topPanel);
        View var7 = var5.findViewById(R.id.contentPanel);
        View var8 = var5.findViewById(R.id.buttonPanel);
        ViewGroup var19 = this.resolvePanel(var6, var2);
        ViewGroup var20 = this.resolvePanel(var7, var3);
        ViewGroup var18 = this.resolvePanel(var8, var4);
        this.setupContent(var20);
        this.setupButtons(var18);
        this.setupTitle(var19);
        boolean var9;
        if (var5 != null && var5.getVisibility() != 8) {
            var9 = true;
        } else {
            var9 = false;
        }

        byte var10;
        if (var19 != null && var19.getVisibility() != 8) {
            var10 = 1;
        } else {
            var10 = 0;
        }

        boolean var11;
        if (var18 != null && var18.getVisibility() != 8) {
            var11 = true;
        } else {
            var11 = false;
        }

        boolean var12;
        if (var2 != null && var2.getVisibility() != 8) {
            var12 = true;
        } else {
            var12 = false;
        }

        boolean var13;
        if (var3 != null && var3.getVisibility() != 8) {
            var13 = true;
        } else {
            var13 = false;
        }

        var2 = this.mCustomTitleView;
        boolean var14;
        if (var2 != null && var2.getVisibility() != 8) {
            var14 = true;
        } else {
            var14 = false;
        }

        if (var9 && !var12 && !var13 || var14) {
            this.adjustParentPanelPadding(var1);
        }

        if (var9 && var12 && !var13) {
            this.adjustTopPanelPadding(var1);
        }

        this.adjustButtonsPadding();
        if (var10 != 0) {
            NestedScrollView var15 = this.mScrollView;
            if (var15 != null) {
                var15.setClipToPadding(true);
            }
        }

        ListView var16 = this.mListView;
        if (var16 instanceof RecycleListView) {
            ((RecycleListView) var16).setHasDecor(var10 == 1, var11);
        }

        if (!var9) {
            Object var17 = this.mListView;
            if (var17 == null) {
                var17 = this.mScrollView;
            }

            if (var17 != null) {
                byte var22;
                if (var11) {
                    var22 = 2;
                } else {
                    var22 = 0;
                }

                this.setScrollIndicators(var20, (View) var17, var22 | var10, 3);
            }
        }

        var16 = this.mListView;
        if (var16 != null) {
            ListAdapter var21 = this.mAdapter;
            if (var21 != null) {
                var16.setAdapter(var21);

                if (Build.VERSION.SDK_INT >= 28)
                    ReflectUtils.genericInvokeMethod(AdapterView.class, var21, Build.VERSION.SDK_INT >= 29 ? "hidden_semSetBottomColor" : "semSetBottomColor", 0);

                int var23 = this.mCheckedItem;
                if (var23 > -1) {
                    var16.setItemChecked(var23, true);
                    var16.setSelectionFromTop(var23, this.mContext.getResources().getDimensionPixelSize(R.dimen.sesl_select_dialog_padding_top));
                }
            }
        }

    }

    public Button getButton(int var1) {
        if (var1 != -3) {
            if (var1 != -2) {
                return var1 != -1 ? null : this.mButtonPositive;
            } else {
                return this.mButtonNegative;
            }
        } else {
            return this.mButtonNeutral;
        }
    }

    public int getIconAttributeResId(int var1) {
        TypedValue var2 = new TypedValue();
        this.mContext.getTheme().resolveAttribute(var1, var2, true);
        return var2.resourceId;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public void installContent() {
        int var1 = this.selectContentView();
        this.mDialog.setContentView(var1);
        this.setupView();
    }

    public boolean onKeyDown(int var1, KeyEvent var2) {
        NestedScrollView var3 = this.mScrollView;
        boolean var4;
        if (var3 != null && var3.executeKeyEvent(var2)) {
            var4 = true;
        } else {
            var4 = false;
        }

        return var4;
    }

    public boolean onKeyUp(int var1, KeyEvent var2) {
        NestedScrollView var3 = this.mScrollView;
        boolean var4;
        if (var3 != null && var3.executeKeyEvent(var2)) {
            var4 = true;
        } else {
            var4 = false;
        }

        return var4;
    }

    public void setButton(int var1, CharSequence var2, DialogInterface.OnClickListener var3, Message var4, Drawable var5) {
        Message var6 = var4;
        if (var4 == null) {
            var6 = var4;
            if (var3 != null) {
                var6 = this.mHandler.obtainMessage(var1, var3);
            }
        }

        if (var1 != -3) {
            if (var1 != -2) {
                if (var1 != -1) {
                    throw new IllegalArgumentException("Button does not exist");
                }

                this.mButtonPositiveText = var2;
                this.mButtonPositiveMessage = var6;
                this.mButtonPositiveIcon = var5;
            } else {
                this.mButtonNegativeText = var2;
                this.mButtonNegativeMessage = var6;
                this.mButtonNegativeIcon = var5;
            }
        } else {
            this.mButtonNeutralText = var2;
            this.mButtonNeutralMessage = var6;
            this.mButtonNeutralIcon = var5;
        }

    }

    public void setButtonPanelLayoutHint(int var1) {
        this.mButtonPanelLayoutHint = var1;
    }

    public void setCustomTitle(View var1) {
        this.mCustomTitleView = var1;
    }

    @SuppressLint("WrongConstant")
    public void setIcon(int var1) {
        this.mIcon = null;
        this.mIconId = var1;
        ImageView var2 = this.mIconView;
        if (var2 != null) {
            if (var1 != 0) {
                var2.setVisibility(0);
                this.mIconView.setImageResource(this.mIconId);
            } else {
                var2.setVisibility(8);
            }
        }

    }

    @SuppressLint("WrongConstant")
    public void setIcon(Drawable var1) {
        this.mIcon = var1;
        this.mIconId = 0;
        ImageView var2 = this.mIconView;
        if (var2 != null) {
            if (var1 != null) {
                var2.setVisibility(0);
                this.mIconView.setImageDrawable(var1);
            } else {
                var2.setVisibility(8);
            }
        }

    }

    public void setMessage(CharSequence var1) {
        this.mMessage = var1;
        TextView var2 = this.mMessageView;
        if (var2 != null) {
            var2.setText(var1);
        }

    }

    public void setTitle(CharSequence var1) {
        this.mTitle = var1;
        TextView var2 = this.mTitleView;
        if (var2 != null) {
            var2.setText(var1);
        }

    }

    public void setView(int var1) {
        this.mView = null;
        this.mViewLayoutResId = var1;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View var1) {
        this.mView = var1;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = false;
    }

    public void setView(View var1, int var2, int var3, int var4, int var5) {
        this.mView = var1;
        this.mViewLayoutResId = 0;
        this.mViewSpacingSpecified = true;
        this.mViewSpacingLeft = var2;
        this.mViewSpacingTop = var3;
        this.mViewSpacingRight = var4;
        this.mViewSpacingBottom = var5;
    }

    public static class AlertParams {
        public final Context mContext;
        public final LayoutInflater mInflater;
        public ListAdapter mAdapter;
        public boolean mCancelable;
        public int mCheckedItem = -1;
        public boolean[] mCheckedItems;
        public Cursor mCursor;
        public View mCustomTitleView;
        public boolean mForceInverseBackground;
        public Drawable mIcon;
        public int mIconAttrId = 0;
        public int mIconId = 0;
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
        public boolean mRecycleOnMeasure = true;
        public CharSequence mTitle;
        public View mView;
        public int mViewLayoutResId;
        public int mViewSpacingBottom;
        public int mViewSpacingLeft;
        public int mViewSpacingRight;
        public boolean mViewSpacingSpecified = false;
        public int mViewSpacingTop;

        @SuppressLint("WrongConstant")
        public AlertParams(Context var1) {
            this.mContext = var1;
            this.mCancelable = true;
            this.mInflater = (LayoutInflater) var1.getSystemService("layout_inflater");
        }

        private void createListView(final SamsungAlertController var1) {
            final RecycleListView var2 = (RecycleListView) this.mInflater.inflate(var1.mListLayout, (ViewGroup) null);
            Cursor var3;
            Object var6;
            if (this.mIsMultiChoice) {
                var3 = this.mCursor;
                if (var3 == null) {
                    var6 = new ArrayAdapter<CharSequence>(this.mContext, var1.mMultiChoiceItemLayout, 16908308, this.mItems) {
                        public View getView(int var1, View var2x, ViewGroup var3) {
                            View var5 = super.getView(var1, var2x, var3);
                            boolean[] var4 = AlertParams.this.mCheckedItems;
                            if (var4 != null && var4[var1]) {
                                var2.setItemChecked(var1, true);
                            }

                            return var5;
                        }
                    };
                } else {
                    var6 = new CursorAdapter(this.mContext, var3, false) {
                        public final int mIsCheckedIndex;
                        public final int mLabelIndex;

                        {
                            Cursor var7 = this.getCursor();
                            this.mLabelIndex = var7.getColumnIndexOrThrow(AlertParams.this.mLabelColumn);
                            this.mIsCheckedIndex = var7.getColumnIndexOrThrow(AlertParams.this.mIsCheckedColumn);
                        }

                        @SuppressLint("ResourceType")
                        public void bindView(View var1x, Context var2x, Cursor var3) {
                            ((CheckedTextView) var1x.findViewById(16908308)).setText(var3.getString(this.mLabelIndex));
                            RecycleListView var7 = var2;
                            int var4 = var3.getPosition();
                            int var5 = var3.getInt(this.mIsCheckedIndex);
                            boolean var6 = true;
                            if (var5 != 1) {
                                var6 = false;
                            }

                            var7.setItemChecked(var4, var6);
                        }

                        public View newView(Context var1x, Cursor var2x, ViewGroup var3) {
                            return AlertParams.this.mInflater.inflate(var1.mMultiChoiceItemLayout, var3, false);
                        }
                    };
                }
            } else {
                int var4;
                if (this.mIsSingleChoice) {
                    var4 = var1.mSingleChoiceItemLayout;
                } else {
                    var4 = var1.mListItemLayout;
                }

                var3 = this.mCursor;
                if (var3 != null) {
                    var6 = new SimpleCursorAdapter(this.mContext, var4, var3, new String[]{this.mLabelColumn}, new int[]{16908308});
                } else {
                    var6 = this.mAdapter;
                    if (var6 == null) {
                        var6 = new CheckedItemAdapter(this.mContext, var4, 16908308, this.mItems);
                    }
                }
            }

            OnPrepareListViewListener var5 = this.mOnPrepareListViewListener;
            if (var5 != null) {
                var5.onPrepareListView(var2);
            }

            var1.mAdapter = (ListAdapter) var6;
            var1.mCheckedItem = this.mCheckedItem;
            if (this.mOnClickListener != null) {
                var2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> var1x, View var2, int var3, long var4) {
                        AlertParams.this.mOnClickListener.onClick(var1.mDialog, var3);
                        if (!AlertParams.this.mIsSingleChoice) {
                            var1.mDialog.dismiss();
                        }

                    }
                });
            } else if (this.mOnCheckboxClickListener != null) {
                var2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> var1x, View var2x, int var3, long var4) {
                        boolean[] var6 = AlertParams.this.mCheckedItems;
                        if (var6 != null) {
                            var6[var3] = var2.isItemChecked(var3);
                        }

                        AlertParams.this.mOnCheckboxClickListener.onClick(var1.mDialog, var3, var2.isItemChecked(var3));
                    }
                });
            }

            AdapterView.OnItemSelectedListener var7 = this.mOnItemSelectedListener;
            if (var7 != null) {
                var2.setOnItemSelectedListener(var7);
            }

            if (this.mIsSingleChoice) {
                var2.setChoiceMode(1);
            } else if (this.mIsMultiChoice) {
                var2.setChoiceMode(2);
            }

            var1.mListView = var2;
        }

        public void apply(SamsungAlertController var1) {
            View var2 = this.mCustomTitleView;
            int var3;
            CharSequence var4;
            if (var2 != null) {
                var1.setCustomTitle(var2);
            } else {
                var4 = this.mTitle;
                if (var4 != null) {
                    var1.setTitle(var4);
                }

                Drawable var5 = this.mIcon;
                if (var5 != null) {
                    var1.setIcon(var5);
                }

                var3 = this.mIconId;
                if (var3 != 0) {
                    var1.setIcon(var3);
                }

                var3 = this.mIconAttrId;
                if (var3 != 0) {
                    var1.setIcon(var1.getIconAttributeResId(var3));
                }
            }

            var4 = this.mMessage;
            if (var4 != null) {
                var1.setMessage(var4);
            }

            if (this.mPositiveButtonText != null || this.mPositiveButtonIcon != null) {
                var1.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, (Message) null, this.mPositiveButtonIcon);
            }

            if (this.mNegativeButtonText != null || this.mNegativeButtonIcon != null) {
                var1.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, (Message) null, this.mNegativeButtonIcon);
            }

            if (this.mNeutralButtonText != null || this.mNeutralButtonIcon != null) {
                var1.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, (Message) null, this.mNeutralButtonIcon);
            }

            if (this.mItems != null || this.mCursor != null || this.mAdapter != null) {
                this.createListView(var1);
            }

            var2 = this.mView;
            if (var2 != null) {
                if (this.mViewSpacingSpecified) {
                    var1.setView(var2, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
                } else {
                    var1.setView(var2);
                }
            } else {
                var3 = this.mViewLayoutResId;
                if (var3 != 0) {
                    var1.setView(var3);
                }
            }

        }

        public interface OnPrepareListViewListener {
            void onPrepareListView(ListView var1);
        }
    }

    public static class ButtonBarLayout extends LinearLayout {
        public ButtonBarLayout(Context var1) {
            super(var1);
        }

        public ButtonBarLayout(Context var1, AttributeSet var2) {
            super(var1, var2);
        }

        public ButtonBarLayout(Context var1, AttributeSet var2, int var3) {
            super(var1, var2, var3);
        }

        public ButtonBarLayout(Context var1, AttributeSet var2, int var3, int var4) {
            super(var1, var2, var3, var4);
        }

        @SuppressLint("WrongConstant")
        public void onMeasure(int var1, int var2) {
            super.onMeasure(var1, var2);
            int var3 = this.getChildCount();
            int var4 = MeasureSpec.getSize(var1) - this.getPaddingRight() - this.getPaddingLeft();
            int var5 = MeasureSpec.getSize(var2);
            int var6 = this.getPaddingTop();
            int var7 = this.getPaddingBottom();
            byte var8 = 0;
            int var9 = 0;
            int var10 = var9;
            int var11 = var9;

            int var12;
            View var13;
            int var16;
            for (var12 = var9; var9 < var3; var12 = var16) {
                var13 = this.getChildAt(var9);
                int var14 = var10;
                int var15 = var11;
                var16 = var12;
                if (var13.getVisibility() != 8) {
                    var14 = var10;
                    var15 = var11;
                    var16 = var12;
                    if (var13 instanceof Button) {
                        var15 = var11 + var13.getMeasuredWidth();
                        var13.measure(MeasureSpec.makeMeasureSpec(var4, -2147483648), MeasureSpec.makeMeasureSpec(var5 - var6 - var7, -2147483648));
                        var16 = var12 + var13.getMeasuredWidth();
                        var14 = var10 + 1;
                    }
                }

                ++var9;
                var10 = var14;
                var11 = var15;
            }

            boolean var18 = true;
            --var10;
            if (var10 > 0) {
                var10 = (int) ((float) var10 * this.getContext().getResources().getDisplayMetrics().density);
            } else {
                var10 = 0;
            }

            if (var11 >= var12 && var4 > var12 + var10) {
                if (this.getOrientation() != 0) {
                    this.setOrientation(0);
                    boolean var20;
                    if (this.findViewById(R.id.button1).getVisibility() == 0) {
                        var20 = true;
                    } else {
                        var20 = false;
                    }

                    boolean var19;
                    if (this.findViewById(R.id.button2).getVisibility() == 0) {
                        var19 = true;
                    } else {
                        var19 = false;
                    }

                    boolean var21;
                    if (this.findViewById(R.id.button3).getVisibility() == 0) {
                        var21 = var18;
                    } else {
                        var21 = false;
                    }

                    var13 = this.findViewById(R.id.sem_divider1);
                    View var17 = this.findViewById(R.id.sem_divider2);
                    if (var17 != null && (var21 && var20 || var21 && var19)) {
                        var17.setVisibility(0);
                    }

                    if (var13 != null && var20 && var19) {
                        var13.setVisibility(0);
                    }
                }
            } else if (this.getOrientation() != 1) {
                this.setOrientation(1);

                for (var10 = var8; var10 < var3; ++var10) {
                    var13 = this.getChildAt(var10);
                    if (var13.getVisibility() != 8 && !(var13 instanceof Button)) {
                        var13.setVisibility(8);
                    }
                }

                this.setGravity(17);
            }

            super.onMeasure(var1, var2);
        }
    }

    private static final class ButtonHandler extends Handler {
        public static final int MSG_DISMISS_DIALOG = 1;
        public WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface var1) {
            this.mDialog = new WeakReference(var1);
        }

        public void handleMessage(Message var1) {
            int var2 = var1.what;
            if (var2 != -3 && var2 != -2 && var2 != -1) {
                if (var2 == 1) {
                    ((DialogInterface) var1.obj).dismiss();
                }
            } else {
                ((DialogInterface.OnClickListener) var1.obj).onClick((DialogInterface) this.mDialog.get(), var1.what);
            }

        }
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context var1, int var2, int var3, CharSequence[] var4) {
            super(var1, var2, var3, var4);
        }

        public long getItemId(int var1) {
            return (long) var1;
        }

        public boolean hasStableIds() {
            return true;
        }
    }

    public static class RecycleListView extends ListView {
        public final int mPaddingBottomNoButtons;
        public final int mPaddingTopNoTitle;

        public RecycleListView(Context var1) {
            this(var1, (AttributeSet) null);
        }

        public RecycleListView(Context var1, AttributeSet var2) {
            super(var1, var2);
            TypedArray var3 = var1.obtainStyledAttributes(var2, R.styleable.RecycleListView);
            this.mPaddingBottomNoButtons = var3.getDimensionPixelOffset(R.styleable.RecycleListView_paddingBottomNoButtons, -1);
            this.mPaddingTopNoTitle = var3.getDimensionPixelOffset(R.styleable.RecycleListView_paddingTopNoTitle, -1);
        }

        public void setHasDecor(boolean var1, boolean var2) {
            if (!var2 || !var1) {
                int var3 = this.getPaddingLeft();
                int var4;
                if (var1) {
                    var4 = this.getPaddingTop();
                } else {
                    var4 = this.mPaddingTopNoTitle;
                }

                int var5 = this.getPaddingRight();
                int var6;
                if (var2) {
                    var6 = this.getPaddingBottom();
                } else {
                    var6 = this.mPaddingBottomNoButtons;
                }

                this.setPadding(var3, var4, var5, var6);
            }

        }

        public void setOverScrollMode(int var1) {
            if (var1 != 2) {
                if ((EdgeEffect) ReflectUtils.genericGetField(this, Build.VERSION.SDK_INT >= 29 ? "hidden_mEdgeGlowTop" : "mEdgeGlowTop") == null) {
                    Context var2 = this.getContext();
                    SamsungEdgeEffect var3 = new SamsungEdgeEffect(var2);
                    SamsungEdgeEffect var4 = new SamsungEdgeEffect(var2);
                    var3.setSeslHostView(this);
                    var4.setSeslHostView(this);

                    ReflectUtils.genericSetField(this, Build.VERSION.SDK_INT >= 29 ? "hidden_mEdgeGlowTop" : "mEdgeGlowTop", var3);
                    ReflectUtils.genericSetField(this, Build.VERSION.SDK_INT >= 29 ? "hidden_mEdgeGlowBottom" : "mEdgeGlowBottom", var4);
                }
            } else {
                ReflectUtils.genericSetField(this, Build.VERSION.SDK_INT >= 29 ? "hidden_mEdgeGlowTop" : "mEdgeGlowTop", (EdgeEffect) null);
                ReflectUtils.genericSetField(this, Build.VERSION.SDK_INT >= 29 ? "hidden_mEdgeGlowBottom" : "mEdgeGlowBottom", (EdgeEffect) null);
            }

            super.setOverScrollMode(var1);
        }
    }
}
