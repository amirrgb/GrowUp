package com.example.growup;

import static com.example.growup.DBHelper.dbReadable;
import static com.example.growup.DBHelper.dbWritable;

import android.database.Cursor;

public class TypeHandler {

    public static void TypeInitializer(){
        DBHelper.deleteRedundantTypes(4);
        TypeHandler.insertIntoTypesTable("folder",R.drawable.ic_folder);
        TypeHandler.insertIntoTypesTable("note",R.drawable.ic_note);
        TypeHandler.insertIntoTypesTable("pin_folder",R.drawable.ic_pin_folder);
        TypeHandler.insertIntoTypesTable("pin_note",R.drawable.ic_pin_note);
    }

    public static void insertIntoTypesTable(String type, int icon_id){
        try{
            String sqlQuery = "SELECT * FROM TYPES WHERE type=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{type});
            if (cursor!= null && cursor.moveToFirst()) {
                try {
                    dbWritable.beginTransaction();
                    String sqlQuery2 = "UPDATE TYPES SET icon_id = ? WHERE type = ?";
                    dbWritable.execSQL(sqlQuery2, new String[]{String.valueOf(icon_id), type});
                    dbWritable.setTransactionSuccessful();
                }catch (Exception e1){
                    LogHandler.saveLog("Failed to insert into TYPES table : " + e1.getLocalizedMessage(),true);
                }finally {
                 dbWritable.endTransaction();
                }
            } else {
                try {
                    dbWritable.beginTransaction();
                    sqlQuery = "INSERT INTO TYPES (type,icon_id) VALUES (?,?)";
                    dbWritable.execSQL(sqlQuery, new String[]{type, String.valueOf(icon_id)});
                    dbWritable.setTransactionSuccessful();
                } catch (Exception e) {
                    LogHandler.saveLog("Failed to insert into TYPES table : " + e.getLocalizedMessage(),true);
                }finally {
                    dbWritable.endTransaction();
                }
            }
        }catch (Exception e){
            LogHandler.saveLog("Failed to insert into TYPES table : " + e.getLocalizedMessage(),true);
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

    public static int getTypeIdByAssetId(int assetId){
        int typeId = -1;
        try{
            String sqlQuery = "SELECT typeId FROM ASSETS WHERE id=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(assetId)});
            if (cursor != null && cursor.moveToFirst()) {
                typeId = cursor.getInt(0);
            }
            cursor.close();
            return typeId;
        }catch (Exception e){
            LogHandler.saveLog("Failed to get typeId by assetId : (typeId is "+typeId +") error : "+ e.getLocalizedMessage(),true);
            return 0;
        }

    }

    public static String getTypeNameByTypeId(int typeId){
        try{
            String sqlQuery = "SELECT type FROM TYPES WHERE id=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(typeId)});
            String typeName = "";
            if (cursor != null && cursor.moveToFirst()) {
                typeName = cursor.getString(0);
            }
            cursor.close();
            return typeName;
        }catch (Exception e){
            LogHandler.saveLog("failed to get type by type id : " + e.getLocalizedMessage(), true);
            return null;
        }
    }

    public static int getTypeIdByType(String type){
        try{
            String sqlQuery = "SELECT id FROM TYPES WHERE type=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{type});
            int typeId = 0;
            if (cursor != null && cursor.moveToFirst()) {
                typeId = cursor.getInt(0);
            }
            cursor.close();
            return typeId;
        }catch (Exception e){
            LogHandler.saveLog("failed to get typeId by type : "+ e.getLocalizedMessage(),true);
            return -1;
        }

    }

    public static int getIconIdByTypeId(int typeId){
        try{
            String sqlQuery = "SELECT icon_id FROM TYPES WHERE id=?";
            Cursor cursor = dbReadable.rawQuery(sqlQuery, new String[]{String.valueOf(typeId)});
            int iconId = 0;
            if (cursor!= null && cursor.moveToFirst()) {
                iconId = cursor.getInt(0);
            }
            cursor.close();
            return iconId;
        }catch (Exception e){
            LogHandler.saveLog("failed to get icon_Id by type id : " + e.getLocalizedMessage(), true);
            return -1;
        }
    }

    public static int getIconIdByAssetId(int assetId){
        int typeId = getTypeIdByAssetId(assetId);
        return getIconIdByTypeId(typeId);
    }

    public static String getTypeNameByAssetId(int assetId){
        int typeId = getTypeIdByAssetId(assetId);
        return getTypeNameByTypeId(typeId);
    }

}
