package com.example.growup;

import android.content.SharedPreferences;
import android.widget.Toast;

public class Upgrade {

    public static boolean versionHandler(SharedPreferences preferences) {
        boolean isFirstTime = false;
        int savedVersionCode = preferences.getInt("currentVersionCode", -1); // Default to -1 if not found
        int currentVersionCode = BuildConfig.VERSION_CODE;
        if (savedVersionCode == -1) {
            isFirstTime = true;
        } else if (savedVersionCode <= currentVersionCode) {
            switch (savedVersionCode) {
//                case 1:
//                    upgrade_1_to_2();
//                    break;
                default:
                    lastVersion();
            }
        } else if (savedVersionCode > currentVersionCode) {
            Toast.makeText(MainActivity.activity, "Please install last version of App", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("currentVersionCode", currentVersionCode);
        editor.apply();
        return isFirstTime;
    }

    public static void lastVersion() {
        MainActivity.activity.runOnUiThread(() -> {
            Toast.makeText(MainActivity.activity, "Your App is Up-To-Date", Toast.LENGTH_SHORT).show();
        });
    }
//    public static void upgrade_1_to_2() {}
}