package de.dlyt.yanndroid.oneuiexample.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TabsManager {
    private static String KEY = "current_tab";

    private SharedPreferences sp;
    private int sSelectedTab = 0;
    private int sPrevTab = sSelectedTab;

    public TabsManager(Context context, String spName) {
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public int getCurrentTab() {
        return sSelectedTab;
    }

    public int getPreviousTab() {
        return sPrevTab;
    }

    public void initTabPosition() {
        setTabPosition(getTabFromSharedPreference());
    }

    public void setTabPosition(int position) {
        setTabPositionToSharedPreference(position);
        sPrevTab = sSelectedTab;
        sSelectedTab = position;
    }

    private int getTabFromSharedPreference() {
        int position = sp.getInt(KEY, -1);
        if (position == -1) {
            return 0;
        }
        return position;
    }

    private void setTabPositionToSharedPreference(int position) {
        sp.edit().putInt(KEY, position).apply();
    }
}
