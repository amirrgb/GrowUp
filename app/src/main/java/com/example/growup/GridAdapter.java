package com.example.growup;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;
import java.util.Objects;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private static final int tempAssetId = -2;
    private ArrayList<String> assetsName;
    private ArrayList<Integer> assetsIcon;
    private ArrayList<Integer> assetsId;


    public static void initializeGridAdapter() {
        MainActivity.activity.setContentView(R.layout.activity_main);
        MainActivity.gridView = MainActivity.activity.findViewById(R.id.gridView);
        MainActivity.adapter = new GridAdapter();
        MainActivity.adapter.updateGridAdapter();
    }

    public GridAdapter() {
        readChildItemsOf(MainActivity.currentId);
        mContext = MainActivity.activity;
        MainActivity.noteCreator.createNoteButton();
    }

    public void updateGridAdapter() {
        readChildItemsOf(MainActivity.currentId);
        MainActivity.noteCreator.createNoteButton();
        notifyDataSetChanged();
        MainActivity.gridView.setAdapter(MainActivity.adapter);
        Setting.setListenerForButtons();
        updateHeader();
    }

    public void readChildItemsOf(int pid) {
        ArrayList<String[]> assets = MainActivity.dbHelper.getAssetIdByPid(pid);
        assetsId = new ArrayList<Integer>();
        assetsName = new ArrayList<String>();
        assetsIcon = new ArrayList<Integer>();
        if (!assets.isEmpty()) {
            //sort first folders then notes (todo)
            for (String[] asset : assets) {
                int assetId = Integer.parseInt(asset[0]);
                String keyword = asset[1];
                assetsId.add(assetId);
                assetsName.add(keyword);
                assetsIcon.add(TypeHandler.getIconIdByAssetId(assetId));
            }
        }
        assetsName.add("Add Folder");
        assetsIcon.add(R.drawable.ic_add_folder);
        assetsId.add(tempAssetId);
    }

    public void updateHeader(){
        TextView header = MainActivity.activity.findViewById(R.id.headerTextView);
        if (MainActivity.currentId == 0){
            header.setText("Home");
            MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.app_setting);
            return;
        }
        String headerText = MainActivity.dbHelper.getAsset(String.valueOf(MainActivity.currentId))[2];
        header.setText(headerText);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
    }

    @Override
    public int getCount() {
        return assetsName.size();
    }

    @Override
    public Object getItem(int position) {
        return assetsName.get(position);
    }

    @Override
    public long getItemId(int position) {
        return assetsId.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.grid_item_layout, null);
            TextView textViewName = gridView.findViewById(R.id.textViewName);
            ImageView imageViewIcon = gridView.findViewById(R.id.imageViewIcon);
            textViewName.setText(assetsName.get(position));
            imageViewIcon.setImageResource(assetsIcon.get(position));

            gridView.setOnClickListener(v -> itemsActions(position));

            gridView.setOnLongClickListener(v -> {
                displayPopUpMenu(position,gridView);
                return true;
            });
        } else {
            gridView = convertView;
        }
        return gridView;
    }

    private void itemsActions(int position) {
        if (assetsIcon.get(position) == R.drawable.ic_add_folder) {
            showAddFolderDialog();
            return;
        }

        int assetId = assetsId.get(position);
        switch (TypeHandler.getTypeNameByAssetId(assetId)) {
            case "note":
                MainActivity.currentId = assetsId.get(position);
                MainActivity.noteCreator.openNote();
                break;
            case "folder":
                MainActivity.currentId = assetsId.get(position);
                MainActivity.adapter.updateGridAdapter();
                break;
            default:
                break;
        }
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add New Folder");
        final EditText input = new EditText(mContext);
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFolderName = input.getText().toString();
                boolean isCreated = MainActivity.dbHelper.insertIntoAssetsTable(newFolderName,
                        TypeHandler.getTypeIdByType("folder"), MainActivity.currentId);
                if (!isCreated){
                    MainActivity.activity.runOnUiThread(() -> {
                        Toast.makeText(mContext, "cant create Folder", Toast.LENGTH_SHORT).show();
                    });
                }
                MainActivity.adapter.updateGridAdapter();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void displayPopUpMenu(int position,View gridView) {
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

    public void handleMenuItem(int position, String itemTitle) {
        int assetId = assetsId.get(position);
        switch (itemTitle) {
            case "Rename":
                RenameItem(assetId, position);
                break;
            case "Move":
                System.out.println("you clicked on move");
                //should implement moving logic
                break;
            case "Pin":
            case "UnPin":
                changePinItem(position);
                break;
            case "Set Reminder":
                System.out.println("you clicked on set reminder");
                //should implement setting reminder logic
                break;
            case "Delete":
                deleteItem(assetId);
                break;
            case "Share":
//                shareItem(MainActivity.gridView.getSelectedItemPosition());
                break;
        }
    }

    public void deleteItem(int assetId) {
        MainActivity.dbHelper.deleteAsset(assetId);
        MainActivity.adapter.updateGridAdapter();
    }

    public void RenameItem(int assetId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Rename folder Folder");
        final EditText input = new EditText(mContext);
        input.setText(assetsName.get(position));
        builder.setView(input);

        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFolderName = input.getText().toString();
                MainActivity.dbHelper.updateAssetName(String.valueOf(assetId), newFolderName);
                MainActivity.adapter.updateGridAdapter();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void changePinItem(int position) {
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
}