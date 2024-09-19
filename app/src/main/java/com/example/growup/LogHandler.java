package com.example.growup;

import android.app.Application;

import java.util.Date;


public class LogHandler extends Application {

    public static void saveLog(String text, boolean isError) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                // Read existing lines
                String timestamp = Tools.dateFormat.format(new Date());
                String logEntry;
                if (isError) {
                    logEntry = "err " + timestamp + " --------- " + text;
                    System.out.println("Err IS SAVED: " + logEntry);
                } else {
                    logEntry = "log " + timestamp + " --------- " + text;
                    System.out.println("LOG IS SAVED: " + logEntry);
                }
            }catch (Exception e){
                    System.out.println("Error in buffer writer log handler" + e.getLocalizedMessage());
            }
        }
    }
}