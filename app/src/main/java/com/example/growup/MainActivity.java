package com.example.growup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    public static Activity activity;
    public static DBHelper dbHelper;
    public static int currentId = 0;
    public static SharedPreferences preferences;
    public static GridAdapter adapter;
    public static GridView gridView;
    public static NoteHandler noteCreator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        System.out.println("step 1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manageAccessThread.start();
        try {
            manageAccessThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        manageReadAndWritePermissonsThread.start();
        try {
            manageReadAndWritePermissonsThread.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("step 2");
        LogHandler.CreateLogFile();
        LogHandler.saveLog("--------------------------new run----------------------------", false);
        preferences = getPreferences(Context.MODE_PRIVATE);
        System.out.println("step 3");
        dbHelper = new DBHelper(this);
        Upgrade.versionHandler(preferences);
        System.out.println("step 4");
        dbHelper.insertIntoTypesTable("2","folder","ic_folder");
        dbHelper.insertIntoTypesTable("3","note","ic_note");

        noteCreator = new NoteHandler();
        System.out.println("step 5");
        gridView = findViewById(R.id.gridView);
        adapter = new GridAdapter();
        System.out.println("step 6");
        gridView.setAdapter(adapter);
        System.out.println("step 7");

    }

    @Override
    protected void onStart(){
        super.onStart();
        Setting.setListenerForSettingButton();

    }

    Thread manageAccessThread = new Thread() {
        @Override
        public void run() {
            try {
                int buildSdkInt = Build.VERSION.SDK_INT;
                if (buildSdkInt >= 30) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            Intent getPermission = new Intent();
                            getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(getPermission);
                            while (!Environment.isExternalStorageManager()){
                                System.out.println("here " + Environment.isExternalStorageManager());
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        System.out.println("Starting to get access from your android device");
                    }}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Thread manageReadAndWritePermissonsThread = new Thread() {
        @Override
        public void run() {
            if (true){
                int requestCode =1;
                String[] permissions = {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                };
                boolean isWriteAndReadPermissionGranted = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                while(!isWriteAndReadPermissionGranted){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED |
                                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(MainActivity.this, permissions, requestCode);
                    }
                    isWriteAndReadPermissionGranted = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                }
            }
        }
    };

    public static void backButtonProcess(){
        if (dbHelper.getParentId(currentId) == 0){
            Setting.setListenerForSettingButton();
        }
        boolean canSaveNote = true;
        if (DBHelper.getTypeIdOfAsset(currentId).equals("3")) {
            if (!NoteHandler.saveNote()){
                MainActivity.noteCreator.openNote();
                canSaveNote = false;
            }
        }
        if (canSaveNote){
            currentId = dbHelper.getParentId(currentId);
            adapter.reinitializeGridAdapter();
        }
    }


    @Override
    public void onBackPressed() {
       backButtonProcess();
    }

}