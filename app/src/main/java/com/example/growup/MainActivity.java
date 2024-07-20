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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    public static Activity activity;
    public static DBHelper dbHelper;
    public static int currentId = 0;
    public static SharedPreferences preferences;
    public static GridAdapter adapter;
    public static GoogleCloud googleCloud;
    public static GridView gridView;
    public static NoteHandler noteCreator;
    public static ActivityResultLauncher<Intent> signInToBackUpLauncher;
    public static boolean isLinkedToGoogleDrive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        checkPermissions(); // can copy from stash
        LogHandler.CreateLogFile();
        isLinkedToGoogleDrive = BackUpDataBase.isLinkedToGoogleDrive();
        preferences = getPreferences(Context.MODE_PRIVATE);
        dbHelper = new DBHelper(this);
        Upgrade.versionHandler(preferences);
        googleCloud = new GoogleCloud(this);
        noteCreator = new NoteHandler();
        GridAdapter.initializeGridAdapter();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Setting.setListenerForButtons();

        signInToBackUpLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    try{
                        Thread signInToBackUpThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final GoogleCloud.signInResult signInResult =
                                        googleCloud.handleSignInThread(result.getData());
                                if (signInResult.getUserEmail() == null || signInResult.getFolderId() == null){
                                    MainActivity.activity.runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Login Failed (Because of VPN) Try Again ): ", Toast.LENGTH_SHORT).show();
                                    });
                                    return;
                                }
                                isLinkedToGoogleDrive = DBHelper.insertIntoAccountsTable(signInResult.getUserEmail(),signInResult.getRefreshToken(),signInResult.getFolderId());
                            }
                        });
                        signInToBackUpThread.start();
                    }catch (Exception e){
                        LogHandler.saveLog("Failed to sign in to backup : "  + e.getLocalizedMessage(),true);
                    }
                }
            });


        LogHandler.saveLog("time of setting Alarm",false);
        AlarmHandler alarmHandler = new AlarmHandler(this);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 15);
        alarmHandler.setAlarm(calendar.getTimeInMillis(), 0, "Reminder", "This is your reminder");

        // Cancel the alarm
        // alarmHandler.cancelAlarm(0);










    }



    public void checkPermissions(){
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
                                activity.startActivity(getPermission);
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
                    boolean isWriteAndReadPermissionGranted = (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    while(!isWriteAndReadPermissionGranted){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                                (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED |
                                        ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                            ActivityCompat.requestPermissions(activity, permissions, requestCode);
                        }
                        isWriteAndReadPermissionGranted = (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                                (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                    }
                }
            }
        };
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

    }

    public static void backButtonProcess(){
        if (dbHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
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
            adapter.initializeGridAdapter();
        }
    }
    @Override
    public void onBackPressed() {
       backButtonProcess();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (dbHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
        }
        if (DBHelper.getTypeIdOfAsset(currentId).equals("3")) {
            if (!NoteHandler.saveNote()){
                MainActivity.noteCreator.openNote();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (dbHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
        }
        if (DBHelper.getTypeIdOfAsset(currentId).equals("3")) {
            if (!NoteHandler.saveNote()){
                MainActivity.noteCreator.openNote();
            }
        }
    }

    // should be complete and tested
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GridAdapter.initializeGridAdapter();
    }


}
