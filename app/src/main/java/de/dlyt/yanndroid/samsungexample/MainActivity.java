package de.dlyt.yanndroid.samsungexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.ColorPickerDialog;
import de.dlyt.yanndroid.samsung.SeekBar;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.ThemeColor;
import de.dlyt.yanndroid.samsung.drawer.OptionButton;
import de.dlyt.yanndroid.samsung.layout.DrawerLayout;

public class MainActivity extends AppCompatActivity {

    Integer[] imageIDs = {R.drawable.ic_samsung_arrow_down, R.drawable.ic_samsung_arrow_left, R.drawable.ic_samsung_arrow_right, R.drawable.ic_samsung_arrow_up, R.drawable.ic_samsung_attach, R.drawable.ic_samsung_audio, R.drawable.ic_samsung_back, R.drawable.ic_samsung_book, R.drawable.ic_samsung_bookmark, R.drawable.ic_samsung_brush, R.drawable.ic_samsung_camera, R.drawable.ic_samsung_close, R.drawable.ic_samsung_convert, R.drawable.ic_samsung_copy, R.drawable.ic_samsung_delete, R.drawable.ic_samsung_document, R.drawable.ic_samsung_download, R.drawable.ic_samsung_drawer, R.drawable.ic_samsung_edit, R.drawable.ic_samsung_equalizer, R.drawable.ic_samsung_favorite, R.drawable.ic_samsung_group, R.drawable.ic_samsung_help, R.drawable.ic_samsung_image, R.drawable.ic_samsung_image_2, R.drawable.ic_samsung_import, R.drawable.ic_samsung_info, R.drawable.ic_samsung_keyboard, R.drawable.ic_samsung_lock, R.drawable.ic_samsung_mail, R.drawable.ic_samsung_maximize, R.drawable.ic_samsung_minimize, R.drawable.ic_samsung_minus, R.drawable.ic_samsung_more, R.drawable.ic_samsung_move, R.drawable.ic_samsung_mute, R.drawable.ic_samsung_page, R.drawable.ic_samsung_pause, R.drawable.ic_samsung_pdf, R.drawable.ic_samsung_pen, R.drawable.ic_samsung_pen_calligraphy, R.drawable.ic_samsung_pen_calligraphy_brush, R.drawable.ic_samsung_pen_eraser, R.drawable.ic_samsung_pen_fountain, R.drawable.ic_samsung_pen_marker, R.drawable.ic_samsung_pen_marker_round, R.drawable.ic_samsung_pen_pencil, R.drawable.ic_samsung_play, R.drawable.ic_samsung_plus, R.drawable.ic_samsung_rectify, R.drawable.ic_samsung_redo, R.drawable.ic_samsung_remind, R.drawable.ic_samsung_rename, R.drawable.ic_samsung_reorder, R.drawable.ic_samsung_restore, R.drawable.ic_samsung_save, R.drawable.ic_samsung_scan, R.drawable.ic_samsung_search, R.drawable.ic_samsung_selected, R.drawable.ic_samsung_send, R.drawable.ic_samsung_settings, R.drawable.ic_samsung_share, R.drawable.ic_samsung_shuffle, R.drawable.ic_samsung_smart_view, R.drawable.ic_samsung_stop, R.drawable.ic_samsung_tag, R.drawable.ic_samsung_text, R.drawable.ic_samsung_text_2, R.drawable.ic_samsung_time, R.drawable.ic_samsung_undo, R.drawable.ic_samsung_unlock, R.drawable.ic_samsung_voice, R.drawable.ic_samsung_volume, R.drawable.ic_samsung_warning, R.drawable.ic_samsung_web_search};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ThemeColor(this);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
        setSupportActionBar(drawerLayout.getToolbar());
        drawerLayout.setDrawerIconOnClickListener(v -> startActivity(new Intent().setClass(getApplicationContext(), SettingsActivity.class)));

        demo();

        GridView images = findViewById(R.id.images);
        images.setAdapter(new ImageAdapter(this));

        init();

    }

    private void init() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void demo() {

        /**SeekBar*/
        SeekBar seekBar1 = findViewById(R.id.seekbar1);
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
        switchbar.addOnSwitchChangeListener((switchCompat, z) -> {
            switchbar.setEnabled(false);
            switchbar.setProgressBarVisible(true);

            new Handler().postDelayed(() -> {
                switchbar.setEnabled(true);
                switchbar.setProgressBarVisible(false);
            }, 700);
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

    public void colorPicker(View view) {

        SharedPreferences sharedPreferences = getSharedPreferences("ThemeColor", Context.MODE_PRIVATE);
        String stringColor = sharedPreferences.getString("color", "0381fe");
        float[] currentColor = new float[3];
        Color.colorToHSV(Color.parseColor("#" + stringColor), currentColor);

        ColorPickerDialog mColorPickerDialog;
        mColorPickerDialog = new ColorPickerDialog(this, 2, currentColor);
        mColorPickerDialog.setColorPickerChangeListener(new ColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i, float[] fArr) {
                if (!(fArr[0] == currentColor[0] && fArr[1] == currentColor[1] && fArr[2] == currentColor[2]))
                    ThemeColor.setColor(MainActivity.this, fArr);
            }

            @Override
            public void onViewModeChanged(int i) {

            }
        });
        mColorPickerDialog.show();
    }

    public void nDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher)
                .setMessage("Message")
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .show();
    }

    public void sChoiceDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher)
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setSingleChoiceItems(charSequences, 0, null)
                .show();
    }

    public void mChoiceDialog(View view) {
        Context context = new ContextThemeWrapper(this, R.style.DialogStyle);
        CharSequence[] charSequences = {"Choice1", "Choice2", "Choice3"};
        boolean[] booleans = {true, false, true};
        new AlertDialog.Builder(context)
                .setTitle("Title")
                .setCancelable(false)
                .setIcon(R.drawable.ic_launcher)
                .setNeutralButton("Maybe", null)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", null)
                .setMultiChoiceItems(charSequences, booleans, null)
                .show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                startActivity(new Intent().setClass(getApplicationContext(), AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return imageIDs.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView mImageView;
            if (convertView == null) {
                mImageView = new ImageView(mContext);
                mImageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                mImageView = (ImageView) convertView;
            }
            mImageView.setImageResource(imageIDs[position]);
            return mImageView;
        }


    }

}