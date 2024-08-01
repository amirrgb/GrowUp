package com.example.growup;

import android.content.Context;
import android.widget.Toast;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import java.io.File;

public class BackUpDataBase {

    public static void backUpDataBaseToDrive() {
        if (!MainActivity.isLinkedToGoogleDrive){
            return;
        }
        Context context = MainActivity.activity.getApplicationContext();
        String dataBasePath = context.getDatabasePath(MainActivity.activity.getString(R.string.DataBase_Name)).getPath();
        Thread backupDataBaseToDriveThread = new Thread( () -> {
            try {
                GoogleCloud.signInResult account = DBHelper.getAccount();
                String driveBackUpRefreshToken = account.getRefreshToken();
                String driveBackupAccessToken = MainActivity.googleCloud.updateAccessToken(driveBackUpRefreshToken);
                Drive service = GoogleCloud.initializeDrive(driveBackupAccessToken);
                String databaseFolderId = account.getFolderId();
                String uploadedFileId = setAndCreateDatabaseContent(service,databaseFolderId,dataBasePath);
                if (uploadedFileId == null || uploadedFileId.isEmpty()) {
                    LogHandler.saveLog("Failed to upload profileMap from Android to backup because it's null",true);
                    return;
                }
                MainActivity.activity.runOnUiThread(()->{
                    Toast.makeText(MainActivity.activity, "You're Synced, No Worry", Toast.LENGTH_SHORT).show();
                });
            }catch (Exception e) {
                LogHandler.saveLog("Failed to upload database from Android to backup : " + e.getLocalizedMessage(),true);
            }

        });
        backupDataBaseToDriveThread.start();
    }

    private static String setAndCreateDatabaseContent(Drive service,String databaseFolderId, String dataBasePath){
        String uploadFileId = "";
        try{
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(MainActivity.activity.getString(R.string.DataBase_Name) + ".db");
            fileMetadata.setParents(java.util.Collections.singletonList(databaseFolderId));
            File androidFile = new File(dataBasePath);
            if (!androidFile.exists()) {
                LogHandler.saveLog("Failed to upload database from Android to backup because it doesn't exist", true);
            }
            FileContent mediaContent = new FileContent("application/x-sqlite3", androidFile);
            com.google.api.services.drive.model.File uploadFile =
                    service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            uploadFileId = uploadFile.getId();
        }catch (Exception e){
            LogHandler.saveLog("Failed to set profile map content:" + e.getLocalizedMessage(), true);
        }
        return uploadFileId;
    }

    public static boolean isLinkedToGoogleDrive(){
        try {
            GoogleCloud.signInResult account = DBHelper.getAccount();
            if (account == null){
                return false;
            }
            String refreshToken = account.getRefreshToken();
            System.out.println("in isLinkedTo.. email is : " + account.getUserEmail());
            if (refreshToken == null || refreshToken.isEmpty()) {
                return false;
            }
            String accessToken = MainActivity.googleCloud.updateAccessToken(refreshToken);
            if (accessToken == null || accessToken.isEmpty()) {
                return false;
            }
            if (GoogleCloud.isAccessTokenValid(accessToken)) {
                System.out.println("your access token is valid");
                return true;
            }
        }catch (Exception e){
            LogHandler.saveLog("Failed to check linked to Google Drive: " + e.getLocalizedMessage(), true);
        }
        return false;
    }
}
