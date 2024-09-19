package com.example.growup;

import android.content.SharedPreferences;

public class Upgrade {

    public static void versionHandler(SharedPreferences preferences) {
        int savedVersionCode = preferences.getInt("currentVersionCode", -1); // Default to -1 if not found
        int currentVersionCode = BuildConfig.VERSION_CODE;
        if (savedVersionCode <= currentVersionCode) {
            switch (savedVersionCode) {
                case 1:
                    upgrade_1_to_2();
                    break;
                case 2:
                    upgrade_2_to_3();
                    break;
                default:
                    lastVersion();
            }
        } else {
            Tools.toast("Please install last version of App");
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("currentVersionCode", currentVersionCode);
        editor.apply();
    }

    public static void lastVersion() {
        Tools.toast("Your App is Up-To-Date Now, version "+ BuildConfig.VERSION_NAME);
    }

    public static void upgrade_1_to_2() {

    }

    public static void upgrade_2_to_3() {
        DBHelper.recreateRemindersTable();
        lastVersion();
    }



}