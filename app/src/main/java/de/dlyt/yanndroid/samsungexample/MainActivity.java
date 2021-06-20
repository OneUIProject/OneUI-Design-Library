package de.dlyt.yanndroid.samsungexample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

import de.dlyt.yanndroid.samsung.SeekBar;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.drawer.OptionButton;
import de.dlyt.yanndroid.samsung.layout.DrawerLayout;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;


public class MainActivity extends AppCompatActivity {

    Integer[] imageIDs = {R.drawable.ic_samsung_add , R.drawable.ic_samsung_arrow_down , R.drawable.ic_samsung_back , R.drawable.ic_samsung_bookmark , R.drawable.ic_samsung_brush , R.drawable.ic_samsung_close , R.drawable.ic_samsung_convert , R.drawable.ic_samsung_copy , R.drawable.ic_samsung_delete , R.drawable.ic_samsung_drawer , R.drawable.ic_samsung_edit , R.drawable.ic_samsung_help , R.drawable.ic_samsung_image , R.drawable.ic_samsung_info , R.drawable.ic_samsung_lock , R.drawable.ic_samsung_maximize , R.drawable.ic_samsung_minimize , R.drawable.ic_samsung_more , R.drawable.ic_samsung_move , R.drawable.ic_samsung_rectify , R.drawable.ic_samsung_rename , R.drawable.ic_samsung_restore , R.drawable.ic_samsung_save , R.drawable.ic_samsung_search , R.drawable.ic_samsung_selected , R.drawable.ic_samsung_settings , R.drawable.ic_samsung_share , R.drawable.ic_samsung_text , R.drawable.ic_samsung_voice , R.drawable.ic_samsung_warning};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_view);
        setSupportActionBar(drawerLayout.getToolbar());

        demo();

        GridView images = findViewById(R.id.images);
        images.setAdapter(new ImageAdapter(this));

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

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ImageAdapter(Context c) {mContext = c;}
        public int getCount() {return imageIDs.length;}
        public Object getItem(int position) {return null;}
        public long getItemId(int position) {return 0;}
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView mImageView;
            if (convertView == null) {
                mImageView = new ImageView(mContext);
                mImageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                mImageView = (ImageView) convertView;
            }
            mImageView.setClickable(false);
            mImageView.setImageResource(imageIDs[position]);
            return mImageView;
        }
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