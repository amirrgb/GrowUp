package com.example.growup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.widget.Toast;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = MainActivity.activity.getResources().getString(R.string.DataBase_Name);
    public static final int DATABASE_VERSION = 12;
    public static SQLiteDatabase dbReadable;
    public static SQLiteDatabase dbWritable;
    private static final String ENCRYPTION_KEY = MainActivity.activity.getResources().getString(R.string.ENCRYPTION_KEY);

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
        dbReadable = getReadableDatabase(ENCRYPTION_KEY);
        dbWritable = getReadableDatabase(ENCRYPTION_KEY);
        onCreate(getWritableDatabase(ENCRYPTION_KEY));
        TypeHandler.TypeInitializer();
    }

    public DBHelper(Context context,String name) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
        dbReadable = getReadableDatabase(ENCRYPTION_KEY);
        dbWritable = getReadableDatabase(ENCRYPTION_KEY);
        onCreate(getWritableDatabase(ENCRYPTION_KEY));
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create ASSETS table
        String Assets = "CREATE TABLE IF NOT EXISTS ASSETS(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "pid INTEGER DEFAULT 0," +
                "KEYWORD TEXT UNIQUE," +
                "typeId INTEGER," +
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (pid) REFERENCES ASSETS(id) ON DELETE CASCADE," +
                "FOREIGN KEY (typeId) REFERENCES TYPES(id) ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(Assets);

        // Create TYPES table
        String TYPES = "CREATE TABLE IF NOT EXISTS TYPES(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type TEXT," +
                "icon_id INTEGER)";
        sqLiteDatabase.execSQL(TYPES);

        // Create NOTES table
        String Notes = "CREATE TABLE IF NOT EXISTS NOTES(" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT," +
                "content TEXT," +
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (id) REFERENCES ASSETS(id) ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(Notes);

        // Create ACCOUNTS table
        String Accounts = "CREATE TABLE IF NOT EXISTS ACCOUNTS(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Email TEXT UNIQUE," +
                "RefreshToken TEXT," +
                "folderId TEXT)";
        sqLiteDatabase.execSQL(Accounts);

        // Create REMINDERS table
        String Reminders = "CREATE TABLE IF NOT EXISTS REMINDERS(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "assetId INTEGER," +
                "title TEXT," +
                "message TEXT," +
                "date Date," +
                "alarmType TEXT," +
                "milisToNextAlarm TEXT," +
                "priority TEXT," +
                "requestCode INTEGER," +
                "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (assetId) REFERENCES ASSETS(id) ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(Reminders);

        // Create triggers
        createTrigger(sqLiteDatabase, "update_assets_updatedAt",
                "CREATE TRIGGER update_assets_updatedAt AFTER UPDATE ON ASSETS " +
                        "FOR EACH ROW BEGIN " +
                        "UPDATE ASSETS SET updatedAt = CURRENT_TIMESTAMP WHERE id = old.id;" +
                        "END;");

        createTrigger(sqLiteDatabase, "update_notes_updatedAt",
                "CREATE TRIGGER update_notes_updatedAt AFTER UPDATE ON NOTES " +
                        "FOR EACH ROW BEGIN " +
                        "UPDATE NOTES SET updatedAt = CURRENT_TIMESTAMP WHERE id = old.id;" +
                        "END;");

        createTrigger(sqLiteDatabase, "update_reminders_updatedAt",
                "CREATE TRIGGER update_reminders_updatedAt AFTER UPDATE ON REMINDERS " +
                        "FOR EACH ROW BEGIN " +
                        "UPDATE REMINDERS SET updatedAt = CURRENT_TIMESTAMP WHERE id = old.id;" +
                        "END;");
    }

    private void createTrigger(SQLiteDatabase sqLiteDatabase, String triggerName, String triggerSQL) {
        try {
            sqLiteDatabase.execSQL(triggerSQL);
        } catch (SQLiteException e) {
            if (!e.getMessage().contains("trigger " + triggerName + " already exists")) {
                throw e;
            }
        }
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {}

    public static boolean insertIntoAccountsTable(String email,String refreshToken,String folderId){
        try {
            dbWritable.beginTransaction();
            String sqlQuery = "INSERT INTO ACCOUNTS (Email, RefreshToken, folderId) VALUES (?,?,?)";
            dbWritable.execSQL(sqlQuery,new Object[]{email,refreshToken,folderId});
            dbWritable.setTransactionSuccessful();
        }catch (SQLiteConstraintException e){
            MainActivity.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.activity, "Duplicate Account", Toast.LENGTH_SHORT).show();
                }
            });
            return false;
        }catch (Exception e){
            LogHandler.saveLog("Failed to insert into Accounts table : " + e.getLocalizedMessage(),true);
            return false;
        } finally {
            dbWritable.endTransaction();
        }
        return true;
    }

    //should change to protected from sql injection
    public boolean insertIntoAssetsTable(String keyword, int typeId, int pid){
        boolean[] result = {false};
        Thread insertThread = new Thread(() -> {
            try {
                dbWritable.beginTransaction();
                String sqlQuery = "INSERT INTO ASSETS (KEYWORD, typeId,pid) VALUES (?,?,?)";
                dbWritable.execSQL(sqlQuery, new String[]{keyword, String.valueOf(typeId), String.valueOf(pid)});
                dbWritable.setTransactionSuccessful();
                result[0] = true;
            } catch(SQLiteConstraintException ee) {
                MainActivity.activity.runOnUiThread(() -> Toast.makeText(MainActivity.activity, "Duplicate Keyword", Toast.LENGTH_SHORT).show());
            }catch(Exception e) {
                LogHandler.saveLog("Failed to insert into ASSETS table : " + e.getLocalizedMessage(),true);
            } finally {
                dbWritable.endTransaction();
            }
        });
        insertThread.start();
        try {
            insertThread.join();
        }catch (InterruptedException e) {
            LogHandler.saveLog("Failed to join insert thread: " + e.getLocalizedMessage(),true);
        }
        return result[0];
    }

    public static boolean insertIntoRemindersTable(String assetId,String title,String message, String date
            , String time,String priority){
        try {
            dbWritable.beginTransaction();
            String sqlQuery = "INSERT INTO REMINDERS (assetId, title, message, date, time, priority) VALUES (?,?,?,?,?,?)";
            dbWritable.execSQL(sqlQuery, new Object[]{assetId,title, message, date, time, priority});
            dbWritable.setTransactionSuccessful();
            return true;
        }catch (Exception e){
            LogHandler.saveLog("Failed to insert into REMINDERS table : " + e.getLocalizedMessage(),true);
        }
        return false;
    }

    public void insertIntoNotesTable(String id,String title,String content){
        System.out.println("data for insert is : "+ id + " " + title + " "
         + content);
        try {
            dbWritable.beginTransaction();

            String sqlQuery = "INSERT OR REPLACE INTO NOTES (id, title, content) VALUES (?, ?, ?)";
            dbWritable.execSQL(sqlQuery, new Object[]{id, title, content});

            dbWritable.setTransactionSuccessful();
        } catch (Exception e) {
            LogHandler.saveLog("Failed to insert or update note: " + e.getLocalizedMessage(),true);
        } finally {
            dbWritable.endTransaction();
        }
        updateAssetName(id,title);
    }

    public void updateAssetName(String id, String title) {
        try {
            dbWritable.beginTransaction();
            String sqlQuery = "UPDATE ASSETS SET KEYWORD = ? WHERE id = ?";
            dbWritable.execSQL(sqlQuery, new Object[]{title, id});
            dbWritable.setTransactionSuccessful();
        } catch (Exception e) {
            LogHandler.saveLog("Failed to update ASSETS table : " + e.getLocalizedMessage(),true);
        } finally {
            dbWritable.endTransaction();
        }
    }

    public String[] getAsset(String id){
        String sqlQuery = "SELECT * FROM ASSETS WHERE id = ?";
        Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{id});
        String[] result;
        if(cursor != null && cursor.moveToFirst()){
            result = new String[]{cursor.getString(0),cursor.getString(1)
                    ,cursor.getString(2),cursor.getString(3)};
            cursor.close();
        }else{
            result = new String[]{"","","",""};
        }
        return result;
    }

    public int getParentId(int id){
        if (id == 0){
            return 0;
        }
        String sqlQuery = "SELECT pid FROM ASSETS WHERE id=?";
        Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(id)});
        int parentId = 0;
        if(cursor != null && cursor.moveToFirst()){
            parentId = cursor.getInt(0);
        }
        cursor.close();
        return parentId;
    }

    public void deleteAsset(int id){
        dbWritable.beginTransaction();
        String sqlQuery = "DELETE FROM ASSETS WHERE id = ?";
        dbWritable.execSQL(sqlQuery, new Object[]{id});
        dbWritable.setTransactionSuccessful();
        dbWritable.endTransaction();
    }

    public ArrayList<String[]> getAssetIdByPid(int pid){
        ArrayList<String[]> assets = new ArrayList<>();
        try{
            String sqlQuery = "SELECT id,keyword,typeId,updatedAt FROM ASSETS WHERE pid=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(pid)});
            if(cursor != null && cursor.moveToFirst()){
                do{
                    String id = cursor.getString(0);
                    String keyword = cursor.getString(1);
                    String typeId = cursor.getString(2);
                    String updatedAt = cursor.getString(3);
                    String[] asset = {id,keyword,typeId,updatedAt};
                    assets.add(asset);
                }while (cursor.moveToNext());
            }
            cursor.close();
            return assets;
        }catch (Exception e){
            LogHandler.saveLog("Failed to get assets by pid : "+e.getLocalizedMessage(),true);
        }
        return assets;
    }

    public String[] getNote(int currentId) {
        String[] note = new String[2];
        String sqlQuery = "SELECT title,content FROM NOTES WHERE id=?";
        Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(currentId)});
        if (cursor != null && cursor.moveToFirst()) {
            note[0] = cursor.getString(0);
            note[1] = cursor.getString(1);
        }
        cursor.close();
        return note;
    }

    public String getLastId() {
        dbReadable.beginTransaction();
        String sqlQuery = "SELECT last_insert_rowid()";
        Cursor cursor = dbReadable.rawQuery(sqlQuery, null);
        String lastId = "";
        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getString(0);
        }
        cursor.close();
        dbReadable.endTransaction();
        return lastId;
    }

    public static GoogleCloud.signInResult getAccount(){
        try{
            String sqlQuery = "SELECT Email,RefreshToken,folderId FROM ACCOUNTS";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{});
            String email = null;
            String refreshToken = null;
            String folderId = null;

            if (cursor != null && cursor.moveToFirst()){
                do{
                    email =  cursor.getString(0);
                    refreshToken = cursor.getString(1);
                    folderId = cursor.getString(2);
                    System.out.println("in accounts table : " +
                            "\nemail : " + email +
                            "\nrefresh token : " +refreshToken +
                            "\nfolderId : " + folderId
                            );
                    cursor.close();
                    return new GoogleCloud.signInResult(email,refreshToken,folderId);
                }while(cursor.moveToNext());
            }
        }catch (Exception e){
            LogHandler.saveLog("failed to get account from database : " +e.getLocalizedMessage(), true);
        }
        return null;
    }

    public static Date getAssetLastUpdateTime(int assetId){
        String sqlQuery = "SELECT updatedAt FROM ASSETS WHERE id=?";
        Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(assetId)});
        Date lastUpdateTime = null;
        if (cursor!= null && cursor.moveToFirst()){
            lastUpdateTime = new Date(Long.parseLong(cursor.getString(0)));
            cursor.close();
        }
        return lastUpdateTime;
    }


    public void moveAsset(int assetId, int newParentId){
        dbWritable.beginTransaction();
        String sqlQuery = "UPDATE ASSETS SET pid=? WHERE id=?";
        dbWritable.execSQL(sqlQuery, new Object[]{newParentId, assetId});
        dbWritable.setTransactionSuccessful();
        dbWritable.endTransaction();
    }
}
