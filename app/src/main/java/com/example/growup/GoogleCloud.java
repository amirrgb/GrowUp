package com.example.growup;

import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class GoogleCloud extends AppCompatActivity {
    private final FragmentActivity activity;
    private GoogleSignInClient googleSignInClient;
    public GoogleCloud(FragmentActivity activity){
        this.activity = activity;
    }
    public void signInToGoogleCloud(ActivityResultLauncher<Intent> signInLauncher) {
        boolean forceCodeForRefreshToken = true;
        try {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope("https://www.googleapis.com/auth/drive"),
                            new Scope("https://www.googleapis.com/auth/photoslibrary.readonly"),
                            new Scope("https://www.googleapis.com/auth/drive.file"),
                            new Scope("https://www.googleapis.com/auth/photoslibrary.appendonly")
                    )
                    .requestServerAuthCode(activity.getResources().getString(R.string.web_client_id), forceCodeForRefreshToken)
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(activity, googleSignInOptions);
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                signInLauncher.launch(signInIntent);
            });
        } catch (Exception e){
            LogHandler.saveLog("login failed in signInGoogleCloud : "+e.getLocalizedMessage(),true);
        }
    }
    public signInResult handleSignInThread(Intent data){
        final GoogleCloud.signInResult[] signInResult = new signInResult[1];
        Thread signInResultThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String userEmail = null;
                    String refreshToken = null;
                    String folderId = null;
                    try{
                        Task<GoogleSignInAccount> googleSignInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                        GoogleSignInAccount account = googleSignInTask.getResult(ApiException.class);
                        userEmail = account.getEmail();

                        if (userEmail != null && userEmail.toLowerCase().endsWith("@gmail.com")) {
                            userEmail = userEmail.replace("@gmail.com", "");
                        }

                        String authCode = account.getServerAuthCode();
                        refreshToken = getRefreshTokenByAuthCode(authCode);
                        if (refreshToken != null) {
                            folderId = createGrowUpFolderInDrive(initializeDrive(updateAccessToken(refreshToken)));
                            signInResult[0] = new signInResult(userEmail,refreshToken,folderId);
                        }
                    }catch (Exception e){
                        LogHandler.saveLog("handle back up sign in result failed: " + e.getLocalizedMessage(), true);
                    }
                    if (refreshToken != null) {
                        folderId = createGrowUpFolderInDrive(initializeDrive(updateAccessToken(refreshToken)));
                        signInResult[0] = new signInResult(userEmail,refreshToken,folderId);
                    }

                }catch (Exception e){
                    LogHandler.saveLog("Failed to join and run sign in to backUp thread : " + e.getLocalizedMessage(), true);
                }
            }
        });
        signInResultThread.start();
        try {
            signInResultThread.join();
        }catch (Exception e){
            LogHandler.saveLog("Failed to join sign in to backUp thread : " + e.getLocalizedMessage(), true);
        }
        return signInResult[0];
    }
    private String getRefreshTokenByAuthCode(String authCode){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> backgroundTokensTask = () -> {
            String refreshToken = null;
            try {
                System.out.println("checking 1");
                URL googleAPITokenUrl = new URL("https://oauth2.googleapis.com/token");
                HttpURLConnection httpURLConnection = (HttpURLConnection) googleAPITokenUrl.openConnection();
                System.out.println("checking 2");
                String clientId = activity.getResources().getString(R.string.client_id);
                String clientSecret = activity.getString(R.string.client_secret);
                String requestBody = "code=" + authCode +
                        "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=authorization_code";
                System.out.println("checking 3");
                byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postData.length));
//                httpURLConnection.setRequestProperty("Host", "oauth2.googleapis.com");//this line seems to be extra
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                System.out.println("checking 4");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                System.out.println("checking 5");
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postData);
                System.out.println("checking 6");
                outputStream.flush();

                int responseCode = httpURLConnection.getResponseCode();
                System.out.println("checking 7 and responseCode is : " + responseCode);
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder responseBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream())
                    );
                    System.out.println("checking 8");
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    System.out.println("checking 9");
                    JSONObject responseJSONObject = new JSONObject(response);
                    System.out.println(responseJSONObject.toString());
                    refreshToken = responseJSONObject.getString("refresh_token");
                    System.out.println("checking 10");
                    return refreshToken;
                }else {
                    LogHandler.saveLog("Getting tokens failed with response code of " + responseCode, true);
                }
            } catch (Exception e) {
                LogHandler.saveLog("Getting tokens failed: " + e.getLocalizedMessage(), true);
            }
            return refreshToken;
        };
        Future<String> future = executor.submit(backgroundTokensTask);
        String tokens_fromFuture = null;
        try {
            tokens_fromFuture = future.get();
        }catch (Exception e){
            LogHandler.saveLog("failed to get tokens from the future: " + e.getLocalizedMessage(), true);
        }finally {
            executor.shutdown();
        }
        return tokens_fromFuture;
    }
    public static class signInResult{
        private final String userEmail;
        private String refreshToken;
        private String folderId;

        public signInResult(String userEmail, String refreshToken,String folderId) {
            this.userEmail = userEmail;
            this.refreshToken = refreshToken;
            this.folderId = folderId;
        }
        public String getUserEmail() {return userEmail;}
        public String getRefreshToken() {return refreshToken;}
        public String getFolderId() {return folderId;}
    }
    public String updateAccessToken(String refreshToken){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> backgroundTokensTask = () -> {
            String accessToken = null;
            try {
                URL googleAPITokenUrl = new URL("https://www.googleapis.com/oauth2/v4/token");
                HttpURLConnection httpURLConnection = (HttpURLConnection) googleAPITokenUrl.openConnection();
                String clientId = activity.getResources().getString(R.string.client_id);
                String clientSecret = activity.getString(R.string.client_secret);
                String requestBody = "&client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&refresh_token= " + refreshToken +
                        "&grant_type=refresh_token";
                byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postData);
                outputStream.flush();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    LogHandler.saveLog("Updating access token with response code of " + responseCode,false);
                    StringBuilder responseBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream())
                    );
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    String response = responseBuilder.toString();
                    JSONObject responseJSONObject = new JSONObject(response);
                    accessToken = responseJSONObject.getString("access_token");
                    return accessToken;
                }else {
                    LogHandler.saveLog("Getting access token failed with response code of " + responseCode, true);
                }
            } catch (Exception e) {
                LogHandler.saveLog("Getting access token failed: " + e.getLocalizedMessage(), true);
            }
            return accessToken;
        };
        Future<String> future = executor.submit(backgroundTokensTask);
        String tokens_fromFuture = "";
        try {
            tokens_fromFuture = future.get();
        }catch (Exception e){
            LogHandler.saveLog("failed to get access token from the future: " + e.getLocalizedMessage(), true);
        }finally {
            executor.shutdown();
        }
        return tokens_fromFuture;
    }
    private static String createGrowUpFolderInDrive(Drive service){
        String growUpFolderId = "";
        try{
            com.google.api.services.drive.model.File folder_metadata =
                    new com.google.api.services.drive.model.File();
            String folderName = "GrowUp";
            folder_metadata.setName(folderName);
            folder_metadata.setMimeType("application/vnd.google-apps.folder");
            com.google.api.services.drive.model.File folder = service.files().create(folder_metadata).setFields("id").execute();
            growUpFolderId = folder.getId();
        }catch (Exception e){
            LogHandler.saveLog("Failed to create stash synced asset folder : " + e.getLocalizedMessage(), true);
        }
        return growUpFolderId;
    }
    public static Drive initializeDrive(String accessToken){
        Drive service = null;
        try{
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
            String bearerToken = "Bearer " + accessToken;
            HttpRequestInitializer requestInitializer = request -> {
                request.getHeaders().setAuthorization(bearerToken);
                request.getHeaders().setContentType("application/json");
            };
            service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                    .setApplicationName("GrowUp")
                    .build();

        }catch (Exception e){
            LogHandler.saveLog("Failed to initialize DRIVE : " + e.getLocalizedMessage(), true);
        }
        return service;
    }

    public static boolean isAccessTokenValid(String accessToken) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callableTask = () -> {
            String tokenInfoUrl = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=" + accessToken;
            URL url = new URL(tokenInfoUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                LogHandler.saveLog("access token validity " + response, false);
                return !response.toString().contains("error");
            } finally {
                connection.disconnect();
            }
        };
        Future<Boolean> future = executor.submit(callableTask);
        Boolean isValid = false;
        try{
            isValid = future.get();
        }catch (Exception e){
            LogHandler.saveLog("Failed to check validity from future in isAccessTokenValid: " + e.getLocalizedMessage(), true);
        }
        return isValid;
    }
}
