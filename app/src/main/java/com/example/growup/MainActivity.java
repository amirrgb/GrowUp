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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.sqlcipher.database.SQLiteDebug;


public class MainActivity extends AppCompatActivity {
    public static Activity activity;
    public static int currentId = 0;
    public static final String tempNoteString = "thisIsTempNoteStringX";
    public static SharedPreferences preferences;
    public static GridAdapter adapter;
    public static GoogleCloud googleCloud;
    public static GridView gridView;
    public static NoteHandler noteCreator;
    public static ActivityResultLauncher<Intent> signInToBackUpLauncher;
    public static boolean isLinkedToGoogleDrive;
    public static boolean onSetAlarmScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        new Thread(MainActivity::checkPermissions).start();
//        checkPermissions(); // can copy from stash

        preferences = getPreferences(Context.MODE_PRIVATE);
        DBHelper.getInstance(activity);
        Upgrade.versionHandler(preferences);
        googleCloud = new GoogleCloud(this);
        noteCreator = new NoteHandler();
        isLinkedToGoogleDrive = BackUpDataBase.isLinkedToGoogleDrive();
        GridAdapter.initializeGridAdapter();

        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            showRebootAlertDialog();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        Setting.setListenerForButtons();

        signInToBackUpLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                System.out.println("result code of signIn launcher is : " + result.getResultCode());
                if(result.getResultCode() == RESULT_OK){
                    try{
                        Thread signInToBackUpThread = new Thread(() -> {
                            final GoogleCloud.signInResult signInResult =
                                    googleCloud.handleSignInThread(result.getData());
                            if (signInResult.getUserEmail() == null || signInResult.getFolderId() == null){
                                Tools.toast("Login Failed (Because of VPN) Try Again ): ");
                                return;
                            }
                            isLinkedToGoogleDrive = DBHelper.insertIntoAccountsTable(signInResult.getUserEmail(),signInResult.getRefreshToken(),signInResult.getFolderId());
                        });
                        signInToBackUpThread.start();
                    }catch (Exception e){
                        LogHandler.saveLog("Failed to sign in to backup : "  + e.getLocalizedMessage(),true);
                    }
                }
            });

        AlarmHandler.rescheduleAlarms(activity);
        if (isLinkedToGoogleDrive){new Thread(BackUpDataBase::backUpDataBaseToDrive).start();}
    }



    public static void checkPermissions(){
        Thread manageAccessThread = new Thread(() -> {
            try {
                int buildSdkInt = Build.VERSION.SDK_INT;
                if (buildSdkInt >= 30) {
                    if (!Environment.isExternalStorageManager()) {
                        Intent getPermission = new Intent();
                        getPermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        activity.startActivity(getPermission);
                        while (!Environment.isExternalStorageManager()) {
                            System.out.println("waiting for permission " + Environment.isExternalStorageManager());
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                System.out.println("Error while waiting for external storage manager: " + e.getLocalizedMessage());
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        System.out.println("Starting to get access from your android device");
                    }}
            } catch (Exception e) {
                System.out.println("Error while checking permissions: " + e.getLocalizedMessage());
            }
        });
        Thread manageReadAndWritePermissonsThread = new Thread( () -> {
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
        });
        manageAccessThread.start();
        try {
            manageAccessThread.join();
        } catch (Exception e) {
            System.out.println("Error while waiting for access: " + e.getLocalizedMessage());
        }
        manageReadAndWritePermissonsThread.start();
        try {
            manageReadAndWritePermissonsThread.join();
        }catch (Exception e){
            System.out.println("Error while waiting for read and write permissions: " + e.getLocalizedMessage());
        }

    }

    public static void backButtonProcess(){
        if (onSetAlarmScreen) {
            onSetAlarmScreen = false;
            GridAdapter.initializeGridAdapter();
            return;
        }
        if (TodayNoteHandler.onTodayNoteScreen){
            GridAdapter.initializeGridAdapter();
            TodayNoteHandler.onTodayNoteScreen = false;
            return;
        }
        if (DBHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
        }

        if (TypeHandler.getTypeNameByAssetId(currentId).equals("note") || TypeHandler.getTypeNameByAssetId(currentId).equals("pin_note")) {
            String result = NoteHandler.saveNote();
            switch (result) {
                case "failure":
                    MainActivity.noteCreator.openNote();
                    break;
                case "temp_failure":
                    int tempNoteId = currentId;
                    currentId = DBHelper.getParentId(currentId);
                    NoteHandler.deleteTempNote(tempNoteId);
                    GridAdapter.initializeGridAdapter();
                    break;
                case "success":
                    currentId = DBHelper.getParentId(currentId);
                    GridAdapter.initializeGridAdapter();
                    break;
            }
        }else {
            currentId = DBHelper.getParentId(currentId);
            GridAdapter.initializeGridAdapter();
        }
    }
    @Override
    public void onBackPressed() {
       backButtonProcess();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (DBHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
        }

        if (TypeHandler.getTypeNameByAssetId(currentId).equals("note") || TypeHandler.getTypeNameByAssetId(currentId).equals("pin_note")) {
            String result = NoteHandler.saveNote();
            switch (result) {
                case "failure":
                    MainActivity.noteCreator.openNote();
                    break;
                case "temp_failure":
                    int tempNoteId = currentId;
                    currentId = DBHelper.getParentId(currentId);
                    NoteHandler.deleteTempNote(tempNoteId);
                    GridAdapter.initializeGridAdapter();
                    break;
                case "success":
                    currentId = DBHelper.getParentId(currentId);
                    GridAdapter.initializeGridAdapter();
                    break;
            }
        }else {
            currentId = DBHelper.getParentId(currentId);
            GridAdapter.initializeGridAdapter();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (DBHelper.getParentId(currentId) == 0){
            Setting.setListenerForButtons();
        }

        if (TypeHandler.getTypeNameByAssetId(currentId).equals("note") || TypeHandler.getTypeNameByAssetId(currentId).equals("pin_note")) {
            String result = NoteHandler.saveNote();
            switch (result) {
                case "failure":
                    MainActivity.noteCreator.openNote();
                    break;
                case "temp_failure":
                    int tempNoteId = currentId;
                    currentId = DBHelper.getParentId(currentId);
                    NoteHandler.deleteTempNote(tempNoteId);
                    GridAdapter.initializeGridAdapter();
                    break;
                case "success":
                    currentId = DBHelper.getParentId(currentId);
                    GridAdapter.initializeGridAdapter();
                    break;
            }
        }else {
            currentId = DBHelper.getParentId(currentId);
            GridAdapter.initializeGridAdapter();
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

    private void showRebootAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Information");
        builder.setMessage("Please note that the app needs to be opened at least once after installation or reboot to reschedule your alarms. Thank you!");

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
