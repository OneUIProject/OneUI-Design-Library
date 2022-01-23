package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout;
import de.dlyt.yanndroid.oneui.widget.Switch;
import de.dlyt.yanndroid.oneui.widget.SwitchBar;
import de.dlyt.yanndroid.oneuiexample.base.BaseThemeActivity;

public class SwitchBarActivity extends BaseThemeActivity implements SwitchBar.OnSwitchChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mUseAltTheme = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switchbar);

        SwitchBarLayout switchBarLayout = findViewById(R.id.switchbarlayout_switchbaractivity);

        switchBarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu);
        switchBarLayout.setOnToolbarMenuItemClickListener(item -> {
            Toast.makeText(this, "Item clicked", Toast.LENGTH_SHORT).show();
            return true;
        });

        switchBarLayout.getSwitchBar().setChecked(getSwitchBarDefaultStatus());
        switchBarLayout.getSwitchBar().addOnSwitchChangeListener(this);
    }

    private boolean getSwitchBarDefaultStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("de.dlyt.yanndroid.oneuiexample_preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("switch_preference_screen", false);
    }

    @Override
    public void onSwitchChanged(Switch switchCompat, boolean z) {
        SharedPreferences.Editor editor = getSharedPreferences("de.dlyt.yanndroid.oneuiexample_preferences", Context.MODE_PRIVATE).edit();
        editor.putBoolean("switch_preference_screen", z).apply();
    }
}
