package de.dlyt.yanndroid.samsung;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.samsung.layout.DrawerLayout;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        //ToolbarLayout tview = findViewById(R.id.tview);
        DrawerLayout dview = findViewById(R.id.dview);

        setSupportActionBar(dview.getToolbar());

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