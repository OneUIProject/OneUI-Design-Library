package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout;
import de.dlyt.yanndroid.oneui.layout.ToolbarLayout;
import de.dlyt.yanndroid.oneui.utils.ThemeUtil;
import de.dlyt.yanndroid.oneui.view.Switch;
import de.dlyt.yanndroid.oneui.view.SwitchBar;

public class SwitchBarActivity extends AppCompatActivity implements SwitchBar.OnSwitchChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeUtil(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switchbar);

        SwitchBarLayout switchBarLayout = findViewById(R.id.switchbarlayout_switchbaractivity);
        ToolbarLayout toolbarLayout = switchBarLayout.getToolbarLayout();

        toolbarLayout.inflateToolbarMenu(R.menu.switchpreferencescreen_menu);
        toolbarLayout.setOnToolbarMenuItemClickListener(item -> {
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
