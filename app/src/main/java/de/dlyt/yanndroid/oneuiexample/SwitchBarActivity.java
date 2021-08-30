package de.dlyt.yanndroid.oneuiexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.oneui.view.Switch;
import de.dlyt.yanndroid.oneui.view.SwitchBar;
import de.dlyt.yanndroid.oneui.utils.ThemeColor;
import de.dlyt.yanndroid.oneui.layout.SwitchBarLayout;

public class SwitchBarActivity extends AppCompatActivity implements SwitchBar.OnSwitchChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switchbar);

        SwitchBarLayout switchBarLayout = findViewById(R.id.switchbarlayout_switchbaractivity);
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
