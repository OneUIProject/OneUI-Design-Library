package de.dlyt.yanndroid.samsungexample;

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.SeekBar;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.drawer.OptionButton;
import de.dlyt.yanndroid.samsung.layout.DrawerLayout;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
        setSupportActionBar(drawerLayout.getToolbar());

        demo();

    }

    public void demo() {

        /**SeekBar*/
        SeekBar seekBar1 = findViewById(R.id.seekbar1);
        seekBar1.setMode(5);
        seekBar1.setOverlapPointForDualColor(70);

        android.widget.SeekBar seekBar2 = findViewById(R.id.seekbar2);

        seekBar2.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                seekBar1.setSecondaryProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {

            }
        });


        /**SwitchBar*/
        SwitchBar switchbar = findViewById(R.id.switchbar1);
        switchbar.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean z) {
                switchbar.setEnabled(false);
                switchbar.setProgressBarVisible(true);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switchbar.setEnabled(true);
                        switchbar.setProgressBarVisible(false);
                    }
                }, 700);
            }
        });


        /**Spinner*/
        Spinner spinner = findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("Spinner Item 1");
        categories.add("Spinner Item 2");
        categories.add("Spinner Item 3");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);


        /**OptionButton*/
        OptionButton ob_help = findViewById(R.id.ob_help);
        ob_help.setButtonEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}