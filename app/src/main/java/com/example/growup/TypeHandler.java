package com.example.growup;

import static com.example.growup.DBHelper.dbReadable;
import static com.example.growup.DBHelper.dbWritable;

import android.database.Cursor;

public class TypeHandler {

    public static void TypeInitializer(){
//        DBHelper.deleteRedundantTypes(5);
        TypeHandler.insertIntoTypesTable("folder",R.drawable.ic_folder);
        TypeHandler.insertIntoTypesTable("note",R.drawable.ic_note);
        TypeHandler.insertIntoTypesTable("pin_folder",R.drawable.ic_pin_folder);
        TypeHandler.insertIntoTypesTable("pin_note",R.drawable.ic_pin_note);
        TypeHandler.insertIntoTypesTable("today_note", R.drawable.ic_today);
        TypeHandler.insertIntoTypesTable("fill_note", R.drawable.ic_fill_note);
        TypeHandler.insertIntoTypesTable("pin_fill_note", R.drawable.ic_pin_fill_note);
    }

    public static void insertIntoTypesTable(String type, int icon_id) {
        Cursor cursor = null;
        try {
            String sqlQuery = "SELECT * FROM TYPES WHERE type=?";
            cursor = dbReadable.rawQuery(sqlQuery, new String[]{type});

            if (cursor != null && cursor.moveToFirst()) {
                try {
                    dbWritable.beginTransaction();
                    String sqlQuery2 = "UPDATE TYPES SET icon_id = ? WHERE type = ?";
                    dbWritable.execSQL(sqlQuery2, new String[]{String.valueOf(icon_id), type});
                    dbWritable.setTransactionSuccessful();
                } catch (Exception e1) {
                    LogHandler.saveLog("Failed to update TYPES table: " + e1.getLocalizedMessage(), true);
                } finally {
                    dbWritable.endTransaction();
                }
            } else {
                try {
                    dbWritable.beginTransaction();
                    sqlQuery = "INSERT INTO TYPES (type, icon_id) VALUES (?, ?)";
                    dbWritable.execSQL(sqlQuery, new String[]{type, String.valueOf(icon_id)});
                    dbWritable.setTransactionSuccessful();
                } catch (Exception e) {
                    LogHandler.saveLog("Failed to insert into TYPES table: " + e.getLocalizedMessage(), true);
                } finally {
                    dbWritable.endTransaction();
                }
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to select from TYPES table: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    public static void updateAssetType(int assetId, String type){
        try {
            int typeId = getTypeIdByType(type);
            dbWritable.beginTransaction();
            String sqlQuery = "UPDATE ASSETS SET typeId =? WHERE id=?";
            dbWritable.execSQL(sqlQuery, new String[]{String.valueOf(typeId), String.valueOf(assetId)});
            dbWritable.setTransactionSuccessful();
        } catch (Exception e) {
            LogHandler.saveLog("Failed to update asset type : " + e.getLocalizedMessage(),true);
        } finally {
            dbWritable.endTransaction();
        }
    }

    public static int getTypeIdByAssetId(int assetId) {
        int typeId = -1;
        Cursor cursor = null;
        try {
            String sqlQuery = "SELECT typeId FROM ASSETS WHERE id=?";
            cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(assetId)});

            if (cursor != null && cursor.moveToFirst()) {
                typeId = cursor.getInt(0);
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to get typeId by assetId: (typeId is " + typeId + ") error: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return typeId;
    }

    public static String getTypeNameByTypeId(int typeId) {
        Cursor cursor = null;
        String typeName = "";
        try {
            String sqlQuery = "SELECT type FROM TYPES WHERE id=?";
            cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(typeId)});

            if (cursor != null && cursor.moveToFirst()) {
                typeName = cursor.getString(0);
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to get typeName by typeId: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return typeName;
    }


    public static int getTypeIdByType(String type) {
        Cursor cursor = null;
        int typeId = 0;
        try {
            String sqlQuery = "SELECT id FROM TYPES WHERE type=?";
            cursor = dbReadable.rawQuery(sqlQuery, new String[]{type});

            if (cursor != null && cursor.moveToFirst()) {
                typeId = cursor.getInt(0);
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to get typeId by type: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return typeId;
    }


    public static int getIconIdByTypeId(int typeId) {
        Cursor cursor = null;
        int iconId = 0;
        try {
            String sqlQuery = "SELECT icon_id FROM TYPES WHERE id=?";
            cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(typeId)});

            if (cursor != null && cursor.moveToFirst()) {
                iconId = cursor.getInt(0);
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to get icon_id by typeId: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return iconId;
    }


    public static int getIconIdByAssetId(int assetId){
        int typeId = getTypeIdByAssetId(assetId);
        if (getTypeNameByTypeId(typeId).equals("note")){
            if (isFillNote(assetId)){
                return R.drawable.ic_fill_note;
            }
        } else if (getTypeNameByTypeId(typeId).equals("pin_note")) {
            if (isFillNote(assetId)){
                return R.drawable.ic_pin_fill_note;
            }
            
        }
        return getIconIdByTypeId(typeId);
    }

    public static String getTypeNameByAssetId(int assetId){
        int typeId = getTypeIdByAssetId(assetId);
        return getTypeNameByTypeId(typeId);
    }

    public static boolean isFillNote(int assetId){
        String[] note = DBHelper.getNote(assetId);
        return !note[1].isEmpty();
    }

    public static boolean isAssetNote(int assetId){
        return TypeHandler.getTypeNameByAssetId(assetId).contains("note");
    }
}
