package com.example.growup;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private static final int tempId = -2;
    private ArrayList<String> folderNames;
    private ArrayList<Integer> folderIcons;
    private ArrayList<Integer> folderIds;


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
        folderIds = new ArrayList<Integer>();
        folderNames = new ArrayList<String>();
        folderIcons = new ArrayList<Integer>();
        if (!assets.isEmpty()) {
            //sort first folders then notes (todo)
            for (String[] asset : assets) {
                String id = asset[0];
                String keyword = asset[1];
                String typeId = asset[2];
                System.out.println("keyword : " + keyword + " folderIcon : " + R.drawable.ic_folder);
                String folder_typeId = MainActivity.dbHelper.getTypeId("folder");
                String note_typeId = MainActivity.dbHelper.getTypeId("note");
                if (typeId.equals(folder_typeId)) {
                    folderNames.add(keyword);
                    folderIcons.add(R.drawable.ic_folder);
                    folderIds.add(Integer.valueOf(id));
                } else if (typeId.equals(note_typeId)) {
                    folderNames.add(keyword);
                    folderIcons.add(R.drawable.note);
                    folderIds.add(Integer.valueOf(id));
                }
            }
        }
        folderNames.add("Add Folder");
        folderIcons.add(R.drawable.ic_add_folder);
        folderIds.add(tempId);
//        notifyDataSetChanged();
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
        return folderNames.size();
    }

    @Override
    public Object getItem(int position) {
        return folderNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return folderIds.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.grid_item_layout, null);
            TextView textViewName = gridView.findViewById(R.id.textViewName);
            ImageView imageViewIcon = gridView.findViewById(R.id.imageViewIcon);
            textViewName.setText(folderNames.get(position));
            imageViewIcon.setImageResource(folderIcons.get(position));

            gridView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemsActions(position);
                    }
                });

            gridView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    displayPopUpMenu(folderIds.get(position));
                    return true;
                }
            });

        } else {
            gridView = convertView;
        }
        return gridView;
    }

    private void itemsActions(int position) {
        if (folderIcons.get(position) == R.drawable.ic_add_folder) {
            showAddFolderDialog();
        }else if (folderIcons.get(position) == R.drawable.ic_folder) {
            MainActivity.currentId = folderIds.get(position);
            MainActivity.adapter.updateGridAdapter();
        } else if (folderIcons.get(position) == R.drawable.note) {
            MainActivity.currentId = folderIds.get(position);
            MainActivity.noteCreator.openNote();
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
                String typeId = MainActivity.dbHelper.getTypeId("folder");
                boolean isCreated = MainActivity.dbHelper.insertIntoAssetsTable(newFolderName,typeId, MainActivity.currentId);
                if (!isCreated){
                    MainActivity.activity.runOnUiThread(() -> {
                        Toast.makeText(mContext, "cant create Folder", Toast.LENGTH_SHORT).show();
                    });
                }else {
                    folderNames.add(newFolderName);
                    folderIcons.add(R.drawable.ic_folder);
                    folderIds.add(Integer.valueOf(MainActivity.dbHelper.getLastId()));
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

    public void displayPopUpMenu(int itemId) {
        String[] menuItems = new String[0];
        if (itemId == tempId) {
            return ;
        } else if (String.valueOf(itemId).equals(MainActivity.dbHelper.getTypeId("folder"))) {
            menuItems = new String[]{"Rename", "Move", "Delete", "Pin", "Share"};
        }else if (String.valueOf(itemId).equals(MainActivity.dbHelper.getTypeId("note"))) {
            menuItems = new String[]{"Move", "Delete", "Pin", "Set Reminder", "Share"};
        }

        PopupMenu popupMenu = new PopupMenu(MainActivity.activity, MainActivity.gridView);
        for (String menuItem : menuItems) {
            popupMenu.getMenu().add(menuItem);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleMenuItem(Objects.requireNonNull(item.getTitle()).toString());
                return true;
            }
        });
        popupMenu.show();

    }

    public void handleMenuItem(String itemTitle) {
        switch (itemTitle) {
            case "Rename":
                //should implement renaming logic
                break;
            case "Move":
                //should implement moving logic
                break;
            case "Pin":
//                MainActivity.gridAdapter.pinItem(MainActivity.gridView.getSelectedItemPosition());
                break;
            case "Set Reminder":
                //should implement setting reminder logic
                break;
            case "Delete":
                deleteItem(MainActivity.gridView.getSelectedItemPosition());
                break;
            case "Share":
//                shareItem(MainActivity.gridView.getSelectedItemPosition());
                break;
        }
    }

    public void deleteItem(int id) {
        MainActivity.dbHelper.deleteAsset(id);
        MainActivity.adapter.updateGridAdapter();
    }

}