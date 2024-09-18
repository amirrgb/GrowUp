package com.example.growup;

import static com.example.growup.GridAdapter.assetsId;
import static com.example.growup.GridAdapter.assetsName;
import static com.example.growup.GridAdapter.assetsIcon;
import static com.example.growup.GridAdapter.mContext;
import static com.example.growup.GridAdapter.movingStatus;
import static com.example.growup.GridAdapter.tempAssetId;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;



public class GridItemsPopupMenu {
    public static void displayPopUpMenu(int position, View gridView) {
        if (movingStatus){
            MainActivity.activity.runOnUiThread(() -> Toast.makeText(mContext, "paste selected asset first", Toast.LENGTH_SHORT).show());
            return;
        }

        String[] menuItems = new String[]{};
        int assetId = assetsId.get(position);
        if (assetId == tempAssetId) {
            return ;
        }
        switch (TypeHandler.getTypeNameByAssetId(assetId)){
            case "folder":
                menuItems = new String[]{"Rename", "Move", "Delete", "Pin", "Share"};
                break;
            case "note":
                menuItems = new String[]{"Move", "Delete", "Pin", "Set Reminder", "Share"};
                break;
            case "pin_folder":
                menuItems = new String[]{"Rename", "Move", "Delete", "UnPin", "Share"};
                break;
            case "pin_note":
                menuItems = new String[]{"Move", "Delete", "UnPin", "Set Reminder", "Share"};
                break;
        }

        PopupMenu popupMenu = new PopupMenu(MainActivity.activity, gridView, Gravity.CENTER);
        for (String menuItem : menuItems) {
            popupMenu.getMenu().add(menuItem);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            String popupMenuItemTitle = item.getTitle().toString();
            handleMenuItem(position,popupMenuItemTitle);
            return true;
        });
        popupMenu.show();
    }

    public static void handleMenuItem(int position, String itemTitle) {
        int assetId = assetsId.get(position);
        switch (itemTitle) {
            case "Rename":
                RenameItem(assetId, position);
                break;
            case "Move":
                moveItem(position);
                break;
            case "Pin":
            case "UnPin":
                changePinItem(position);
                break;
            case "Set Reminder":
                setReminder(position);
                break;
            case "Delete":
                deleteItem(assetId);
                break;
            case "Share":
                MainActivity.activity.runOnUiThread(() -> Toast.makeText(mContext, "I don't think somebody installed this app", Toast.LENGTH_SHORT).show());
                break;
        }
    }

    public static void deleteItem(int assetId) {
        MainActivity.dbHelper.deleteAsset(assetId);
        MainActivity.adapter.updateGridAdapter();
    }

    public static void RenameItem(int assetId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Rename folder Folder");
        final EditText input = new EditText(mContext);
        input.setText(assetsName.get(position));
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newFolderName = input.getText().toString();
            if (newFolderName.isEmpty()){
                MainActivity.activity.runOnUiThread(() -> {
                    MainActivity.activity.runOnUiThread(() -> Toast.makeText(mContext,
                            "Folder name can't be empty", Toast.LENGTH_SHORT).show());
                });
                return;
            } else {
                MainActivity.dbHelper.updateAssetName(String.valueOf(assetId), newFolderName);
                assetsName.add(newFolderName);
                assetsIcon.add(R.drawable.ic_folder);
                assetsId.add(Integer.valueOf(MainActivity.dbHelper.getLastId()));
            }
            MainActivity.adapter.updateGridAdapter();
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void changePinItem(int position) {
        int assetId = assetsId.get(position);
        String assetType = TypeHandler.getTypeNameByAssetId(assetId);
        String newType = "";
        switch (assetType) {
            case "folder":
                newType = "pin_folder";
                break;
            case "note":
                newType = "pin_note";
                break;
            case "pin_folder":
                newType = "folder";
                break;
            case "pin_note":
                newType = "note";
                break;
        }
        TypeHandler.updateAssetType(assetId, newType);
        MainActivity.adapter.updateGridAdapter();
    }

    public static void setReminder(int position) {
        AlarmHandler alarmHandler = new AlarmHandler(MainActivity.activity);
        alarmHandler.openAlarm(position);
//        int assetId = assetsId.get(position);
//        AlarmHandler.showChooseTime(MainActivity.activity, assetId);
    }

    public static void moveItem(int position){
        movingStatus = true;
        GridAdapter.movingAssetId = assetsId.get(position);
        MainActivity.adapter.updateGridAdapter();
    }
}
