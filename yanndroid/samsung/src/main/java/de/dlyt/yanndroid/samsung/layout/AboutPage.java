package de.dlyt.yanndroid.samsung.layout;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import de.dlyt.yanndroid.samsung.R;

public class AboutPage extends LinearLayout {

    private LinearLayout about_content;
    private TextView version;
    private TextView status_text;
    private MaterialButton update_button;
    private ProgressBar loading_bar;
    private ToolbarLayout toolbarLayout;

    public static final int LOADING = 0;
    public static final int UPDATE_AVAILABLE = 1;
    public static final int NO_UPDATE = 2;

    @IntDef({LOADING, UPDATE_AVAILABLE, NO_UPDATE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface UpdateState {
    }

    public AboutPage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.samsung_about_screen, this, true);

        about_content = findViewById(R.id.about_content);
        version = findViewById(R.id.version);
        status_text = findViewById(R.id.status_text);
        update_button = findViewById(R.id.update_button);
        loading_bar = findViewById(R.id.loading_bar);
        toolbarLayout = findViewById(R.id.toolbar_layout);

        toolbarLayout.getToolbar().inflateMenu(R.menu.app_info);
        toolbarLayout.getToolbar().setOnMenuItemClickListener(item -> {
            try {
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
                context.startActivity(intent);
            } catch (ActivityNotFoundException unused) {
                context.startActivity(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version.setText(context.getString(R.string.version) + " " + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void initAboutPage(Activity activity) {
        toolbarLayout.setNavigationOnClickListener(v -> activity.onBackPressed());
    }

    public void setUpdateButtonOnClickListener(OnClickListener listener) {
        update_button.setOnClickListener(listener);
    }

    public void setUpdateState(@UpdateState int state) {
        switch (state) {
            case LOADING:
                loading_bar.setVisibility(VISIBLE);
                update_button.setVisibility(GONE);
                status_text.setVisibility(GONE);
                break;
            case NO_UPDATE:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(GONE);
                status_text.setVisibility(VISIBLE);
                status_text.setText(R.string.latest_version_installed);
                break;
            case UPDATE_AVAILABLE:
                loading_bar.setVisibility(GONE);
                update_button.setVisibility(VISIBLE);
                status_text.setVisibility(VISIBLE);
                status_text.setText(R.string.new_version_is_available);
                break;
        }
    }


    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (about_content == null) {
            super.addView(child, index, params);
        } else {
            about_content.addView(child, index, params);
        }
    }


}
