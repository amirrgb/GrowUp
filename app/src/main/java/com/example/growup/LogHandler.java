package com.example.growup;

import android.app.Application;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LogHandler extends Application {
    static String LOG_DIR_PATH = Environment.getExternalStoragePublicDirectory
            (Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + "growUp";
    public static String logFileName = MainActivity.activity.getResources().getString(R.string.logFile_Name);

    public static void CreateLogFile() {
        try {
            File logDir = new File(LOG_DIR_PATH);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            File logFile = new File(LOG_DIR_PATH + File.separator + logFileName);
            if (!logFile.exists()){
                try{
                    logFile.createNewFile();
                }catch (SecurityException e){
                    System.out.println("error in creating log file (security)" + e.getLocalizedMessage());
                }catch (Exception e){
                    System.out.println("error in creating log file (exception)" + e.getLocalizedMessage());
                }
            }else{
                System.out.println("Log file exists");
            }
        } catch (Exception e) {
            System.out.println("error in creating log file in existing directory" + e.getLocalizedMessage());
        }finally {
            LogHandler.saveLog("--------------------------new run----------------------------", false);
        }
    }

    public static void saveLog(String text, boolean isError) {
        System.out.println("ERR OR LOG IS : " + text);
        File logDir = new File(LOG_DIR_PATH);
        File logFile = new File(logDir, logFileName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                // Read existing lines
                List<String> existingLines = new ArrayList<>();
                if (logFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            existingLines.add(line);
                        }
                    }catch (Exception e){
                        System.out.println("Error in buffer reader log handler" + e.getLocalizedMessage());
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
                String logEntry;
                if (isError) {
                    logEntry = "err " + timestamp + " --------- " + text;
                    System.out.println("Err IS SAVED: " + logEntry);
                } else {
                    logEntry = "log " + timestamp + " --------- " + text;
                    System.out.println("LOG IS SAVED: " + logEntry);
                }
                existingLines.add(Math.min(2, existingLines.size()), logEntry);

                // Write back to the file
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile)))) {
                    for (String line : existingLines) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                catch (Exception e){
                    System.out.println("Error in buffer writer log handler" + e.getLocalizedMessage());
                }
            } catch (Exception e) {
                System.out.println("Error in reading all lines or saving logs: " + e.getMessage());
            }
        }
    }

    public static void deleteLogFile(){
        File logFile = new File(LOG_DIR_PATH + File.separator + logFileName);
        if(logFile.exists()){
            logFile.delete();
        }
    }
}