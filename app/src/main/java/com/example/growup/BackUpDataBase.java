package com.example.growup;

import android.content.Context;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BackUpDataBase {

    public static boolean backUpDataBaseToDrive() {
        Context context = MainActivity.activity.getApplicationContext();
        String dataBasePath = context.getDatabasePath(MainActivity.activity.getString(R.string.DataBase_Name)).getPath();
        final boolean[] isBackedUp = {false};
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> uploadTask = () -> {
            try {
                GoogleCloud.signInResult account = DBHelper.getAccount();
                String driveBackUpRefreshToken = account.getRefreshToken();
                String driveBackupAccessToken = MainActivity.googleCloud.updateAccessToken(driveBackUpRefreshToken);
                Drive service = GoogleCloud.initializeDrive(driveBackupAccessToken);
                String databaseFolderId = account.getFolderId();
                    String uploadedFileId = setAndCreateDatabaseContent(service,databaseFolderId,dataBasePath);
                    if (uploadedFileId == null | uploadedFileId.isEmpty()) {
                        LogHandler.saveLog("Failed to upload profileMap from Android to backup because it's null",true);
                    }else{
                        isBackedUp[0] = true;
                    }
            }catch (Exception e) {
                LogHandler.saveLog("Failed to upload database from Android to backup : " + e.getLocalizedMessage(),true);
            }
            return isBackedUp[0];
        };
        Future<Boolean> isBackedUpFuture = executor.submit(uploadTask);
        try{
            isBackedUp[0] = isBackedUpFuture.get();
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
        return isBackedUp[0];
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
            if (mediaContent == null) {
                LogHandler.saveLog("Failed to upload database from Android to backup because it's null", true);
            }
            com.google.api.services.drive.model.File uploadFile =
                    service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            uploadFileId = uploadFile.getId();
        }catch (Exception e){
            LogHandler.saveLog("Failed to set profile map content:" + e.getLocalizedMessage(), true);
        }finally {
            return uploadFileId;
        }
    }

//    public static boolean isUploadHashEqual(String fileHash, String driveFileId, String accessToken){
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        try {
//            return executor.submit(() -> {
//                URL url = new URL("https://www.googleapis.com/drive/v3/files/" + driveFileId +
//                        "?fields=sha256Checksum,+originalFilename");
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-type", "application/json");
//                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
//                int responseCode = connection.getResponseCode();
//                System.out.println("response code for hash equal: " + responseCode);
//                if (responseCode == HttpURLConnection.HTTP_OK) {
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    StringBuilder response = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        response.append(line);
//                    }
//                    JSONObject responseJson = new JSONObject(response.toString());
//                    String sha256Checksum = responseJson.getString("sha256Checksum").toLowerCase();
//                    System.out.println("drive sha256Checksum: " + sha256Checksum + "\nfileHash: " + fileHash);
//                    return sha256Checksum.equals(fileHash);
//                } else {
//                    return false;
//                }
//            }).get();
//        } catch (Exception e) {
//            LogHandler.saveLog("Error in get file from google drive api: " + e.getLocalizedMessage(),true);
//            return false;
//        } finally {
//            executor.shutdown();
//        }
//    }

}
