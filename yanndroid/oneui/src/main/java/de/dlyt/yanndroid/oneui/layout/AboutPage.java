package de.dlyt.yanndroid.oneui.layout;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.dlyt.yanndroid.oneui.R;

public class AboutPage extends LinearLayout {

    public static final int NOT_UPDATEABLE = -1;
    public static final int LOADING = 0;
    public static final int UPDATE_AVAILABLE = 1;
    public static final int NO_UPDATE = 2;
    public static final int NO_CONNECTION = 3;
    public static final int TOOLBAR = 0;
    public static final int CONTENT_VIEW = 1;
    public static final int VERSION_TEXT = 2;
    public static final int OPTIONAL_TEXT = 3;
    public static final int UPDATE_BUTTON = 4;
    public static final int LOADING_BAR = 5;
    private ToolbarLayout toolbarLayout;
    private LinearLayout about_content;
    private TextView version;
    private TextView status_text;
    private TextView about_optional_text;
    private MaterialButton update_button;
    private MaterialButton retry_button;
    private ProgressBar loading_bar;
    private String optional_text;
    private int update_state;


    public AboutPage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AboutPage, 0, 0);
        try {
            optional_text = attr.getString(R.styleable.AboutPage_optional_text);
            update_state = attr.getInt(R.styleable.AboutPage_update_state, 0);
        } finally {
            attr.recycle();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_about_screen, this, true);

        toolbarLayout = findViewById(R.id.toolbar_layout);
        about_content = findViewById(R.id.about_content);
        version = findViewById(R.id.version);
        status_text = findViewById(R.id.status_text);
        about_optional_text = findViewById(R.id.about_optional_text);
        update_button = findViewById(R.id.update_button);
        retry_button = findViewById(R.id.retry_button);
        loading_bar = findViewById(R.id.loading_bar);

        setOptionalText(optional_text);
        setUpdateState(update_state);

        toolbarLayout.setNavigationButtonIcon(getResources().getDrawable(R.drawable.ic_samsung_back, context.getTheme()));
        toolbarLayout.setNavigationButtonTooltip(getResources().getText(R.string.sesl_navigate_up));
        toolbarLayout.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        toolbarLayout.addOverflowButton(true,
                R.drawable.ic_samsung_info,
                R.string.app_info,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setData(Uri.parse("package:" + getActivity().getApplicationContext().getPackageName()));
                            getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException unused) {
                            getActivity().startActivity(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version.setText(context.getString(R.string.sesl_version) + " " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public View getView(@AboutPageView int view) {
        switch (view) {
            case TOOLBAR:
                return toolbarLayout;
            case CONTENT_VIEW:
                return about_content;
            case VERSION_TEXT:
                return version;
            case OPTIONAL_TEXT:
                return about_optional_text;
            case UPDATE_BUTTON:
                return update_button;
            case LOADING_BAR:
                return loading_bar;
            default:
                return null;
        }
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public void setUpdateButtonOnClickListener(OnClickListener listener) {
        update_button.setOnClickListener(listener);
    }

    public void setRetryButtonOnClickListener(OnClickListener listener) {
        retry_button.setOnClickListener(listener);
    }

    public void setUpdateState(@UpdateState int state) {
        switch (state) {
            case LOADING:
                loading_bar.setVisibility(VISIBLE);
                update_button.setVisibility(GONE);
                retry_button.setVisibility(GONE);
                status_text.setVisibility(GONE);
                break;
            case NO_UPDATE:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(GONE);
                retry_button.setVisibility(GONE);
                status_text.setVisibility(VISIBLE);
                status_text.setText(R.string.latest_version_installed);
                break;
            case UPDATE_AVAILABLE:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(VISIBLE);
                retry_button.setVisibility(GONE);
                status_text.setVisibility(VISIBLE);
                status_text.setText(R.string.new_version_is_available);
                break;
            case NOT_UPDATEABLE:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(GONE);
                retry_button.setVisibility(GONE);
                status_text.setVisibility(GONE);
                break;
            case NO_CONNECTION:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(GONE);
                retry_button.setVisibility(VISIBLE);
                status_text.setVisibility(VISIBLE);
                status_text.setText(R.string.network_connect_is_not_stable);

        }
    }

    public void setOptionalText(String text) {
        optional_text = text;
        about_optional_text.setText(text);
        about_optional_text.setVisibility(text == null || text.isEmpty() ? GONE : VISIBLE);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (about_content == null) {
            super.addView(child, index, params);
        } else {
            about_content.addView(child, index, params);
        }
    }

    @IntDef({CONTENT_VIEW, VERSION_TEXT, OPTIONAL_TEXT, UPDATE_BUTTON, LOADING_BAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AboutPageView {
    }

    @IntDef({LOADING, UPDATE_AVAILABLE, NO_UPDATE, NOT_UPDATEABLE, NO_CONNECTION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UpdateState {
    }


}
