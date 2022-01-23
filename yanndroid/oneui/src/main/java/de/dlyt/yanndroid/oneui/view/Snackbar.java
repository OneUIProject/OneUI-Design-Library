package de.dlyt.yanndroid.oneui.view;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.reflect.widget.SeslTextViewReflector;

import de.dlyt.yanndroid.oneui.R;
import de.dlyt.yanndroid.oneui.sesl.snackbar.BaseTransientBottomBar;
import de.dlyt.yanndroid.oneui.sesl.snackbar.SnackbarContentLayout;


public class Snackbar extends BaseTransientBottomBar<Snackbar> {
    private static final int[] SNACKBAR_BUTTON_STYLE_ATTR = {R.attr.snackbarButtonStyle};
    @Nullable
    private final AccessibilityManager accessibilityManager;
    @Nullable
    private BaseCallback<Snackbar> callback;
    private boolean hasAction;

    private Snackbar(@NonNull ViewGroup viewGroup, @NonNull View view, @NonNull de.dlyt.yanndroid.oneui.sesl.snackbar.ContentViewCallback contentViewCallback) {
        super(viewGroup, view, contentViewCallback);
        this.accessibilityManager = (AccessibilityManager) viewGroup.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence charSequence, int i) {
        ViewGroup findSuitableParent = findSuitableParent(view);
        if (findSuitableParent != null) {
            SnackbarContentLayout snackbarContentLayout = (SnackbarContentLayout) LayoutInflater.from(findSuitableParent.getContext()).inflate(R.layout.oui_design_layout_snackbar_include, findSuitableParent, false);
            Snackbar snackbar = new Snackbar(findSuitableParent, snackbarContentLayout, snackbarContentLayout);
            snackbar.setText(charSequence);
            snackbar.setDuration(i);
            return snackbar;
        }
        throw new IllegalArgumentException("No suitable parent found from the given view. Please provide a valid view.");
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int i, int i2) {
        return make(view, view.getResources().getText(i), i2);
    }

    @Nullable
    private static ViewGroup findSuitableParent(View view) {
        ViewGroup viewGroup = null;
        while (!(view instanceof CoordinatorLayout)) {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
                viewGroup = (ViewGroup) view;
            }
            if (view != null) {
                ViewParent parent = view.getParent();
                if (parent instanceof View) {
                    view = (View) parent;
                    continue;
                } else {
                    view = null;
                    continue;
                }
            }
            if (view == null) {
                return viewGroup;
            }
        }
        return (ViewGroup) view;
    }

    @Override // BaseTransientBottomBar
    public void show() {
        super.show();
    }

    @Override // BaseTransientBottomBar
    public void dismiss() {
        super.dismiss();
    }

    @Override // BaseTransientBottomBar
    public boolean isShown() {
        return super.isShown();
    }

    @NonNull
    public Snackbar setText(@NonNull CharSequence charSequence) {
        @SuppressLint("RestrictedApi") TextView messageView = ((SnackbarContentLayout) this.view.getChildAt(0)).getMessageView();
        messageView.setText(charSequence);
        semCheckMaxFontScale(messageView, getContext().getResources().getDimensionPixelSize(R.dimen.design_snackbar_text_size));
        return this;
    }

    @NonNull
    public Snackbar setText(@StringRes int i) {
        return setText(getContext().getText(i));
    }

    @NonNull
    public Snackbar setAction(@StringRes int i, View.OnClickListener onClickListener) {
        return setAction(getContext().getText(i), onClickListener);
    }

    @NonNull
    public Snackbar setAction(@Nullable CharSequence charSequence, @Nullable final View.OnClickListener onClickListener) {
        SnackbarContentLayout snackbarContentLayout = (SnackbarContentLayout) this.view.getChildAt(0);
        snackbarContentLayout.setBackground(this.view.getResources().getDrawable(R.drawable.sem_snackbar_action_frame_mtrl));
        @SuppressLint("RestrictedApi") Button actionView = snackbarContentLayout.getActionView();
        if (TextUtils.isEmpty(charSequence) || onClickListener == null) {
            actionView.setVisibility(View.GONE);
            actionView.setOnClickListener(null);
            this.hasAction = false;
        } else {
            this.hasAction = true;
            actionView.setVisibility(View.VISIBLE);
            actionView.setText(charSequence);
            SeslTextViewReflector.semSetButtonShapeEnabled(actionView, isShowButtonBackgroundEnabled());
            actionView.setOnClickListener(new View.OnClickListener() {
                /* class Snackbar.AnonymousClass1 */

                public void onClick(View view) {
                    onClickListener.onClick(view);
                    Snackbar.this.dispatchDismiss(1);
                }
            });
            semCheckMaxFontScale(actionView, getContext().getResources().getDimensionPixelSize(R.dimen.sesl_design_snackbar_action_text_size));
        }
        return this;
    }

    private boolean isShowButtonBackgroundEnabled() {
        ContentResolver contentResolver = getContext().getContentResolver();
        return contentResolver != null && Settings.System.getInt(contentResolver, "show_button_background", 0) == 1;
    }

    @Override // BaseTransientBottomBar
    public int getDuration() {
        int duration = super.getDuration();
        if (duration == -2) {
            return -2;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            return this.accessibilityManager.getRecommendedTimeoutMillis(duration, (this.hasAction ? AccessibilityManager.FLAG_CONTENT_CONTROLS : 0) | AccessibilityManager.FLAG_CONTENT_ICONS | AccessibilityManager.FLAG_CONTENT_TEXT);
        } else if (!this.hasAction || !this.accessibilityManager.isTouchExplorationEnabled()) {
            return duration;
        } else {
            return -2;
        }
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    public Snackbar setTextColor(ColorStateList colorStateList) {
        ((SnackbarContentLayout) this.view.getChildAt(0)).getMessageView().setTextColor(colorStateList);
        return this;
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    public Snackbar setTextColor(@ColorInt int i) {
        ((SnackbarContentLayout) this.view.getChildAt(0)).getMessageView().setTextColor(i);
        return this;
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    public Snackbar setActionTextColor(ColorStateList colorStateList) {
        ((SnackbarContentLayout) this.view.getChildAt(0)).getActionView().setTextColor(colorStateList);
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(@ColorInt int i) {
        ((SnackbarContentLayout) this.view.getChildAt(0)).getActionView().setTextColor(i);
        return this;
    }

    @NonNull
    public Snackbar setBackgroundTint(@ColorInt int i) {
        Drawable background = this.view.getBackground();
        if (background != null) {
            Drawable mutate = background.mutate();
            if (Build.VERSION.SDK_INT >= 22) {
                DrawableCompat.setTint(mutate, i);
            } else {
                mutate.setColorFilter(i, PorterDuff.Mode.SRC_IN);
            }
        }
        return this;
    }

    @NonNull
    public Snackbar setBackgroundTintList(ColorStateList colorStateList) {
        DrawableCompat.setTintList(this.view.getBackground().mutate(), colorStateList);
        return this;
    }

    @NonNull
    @Deprecated
    public Snackbar setCallback(@Nullable Callback callback2) {
        BaseCallback<Snackbar> baseCallback = this.callback;
        if (baseCallback != null) {
            removeCallback(baseCallback);
        }
        if (callback2 != null) {
            addCallback(callback2);
        }
        this.callback = callback2;
        return this;
    }

    private void semCheckMaxFontScale(TextView textView, int i) {
        float f = getContext().getResources().getConfiguration().fontScale;
        if (f > 1.2f) {
            textView.setTextSize(0, (((float) i) / f) * 1.2f);
        }
    }

    public static class Callback extends BaseCallback<Snackbar> {
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_TIMEOUT = 2;

        public void onDismissed(Snackbar snackbar, int i) {
        }

        public void onShown(Snackbar snackbar) {
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public static final class SnackbarLayout extends SnackbarBaseLayout {
        public SnackbarLayout(Context context) {
            super(context);
            setBackgroundColor(17170445);
        }

        public SnackbarLayout(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            setBackgroundColor(17170445);
        }

        @Override // BaseTransientBottomBar.SnackbarBaseLayout
        public /* bridge */ /* synthetic */ void setOnClickListener(@Nullable OnClickListener onClickListener) {
            super.setOnClickListener(onClickListener);
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            int childCount = getChildCount();
            int measuredWidth = (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
            for (int i3 = 0; i3 < childCount; i3++) {
                View childAt = getChildAt(i3);
                if (childAt.getLayoutParams().width == -1) {
                    childAt.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childAt.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
            }
        }
    }
}
