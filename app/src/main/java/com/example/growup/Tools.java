package com.example.growup;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Tools {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static void toast(String message){
        MainActivity.activity.runOnUiThread(() -> Toast.makeText(MainActivity.activity, message, Toast.LENGTH_LONG).show());
    }

}
