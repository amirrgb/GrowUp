package com.example.growup;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> folderNames;
    private ArrayList<Integer> folderIcons;
    private ArrayList<Integer> folderIds;
    String addNewFolderName = "Add Folder";
    private Button button;

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
        Setting.setListenerForSettingButton();
        updateHeader();
    }

    public static void reinitializeGridAdapter() {
        MainActivity.activity.setContentView(R.layout.activity_main);
        MainActivity.gridView = MainActivity.activity.findViewById(R.id.gridView);
        MainActivity.adapter = new GridAdapter();
        MainActivity.adapter.updateGridAdapter();
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
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridView;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            gridView = inflater.inflate(R.layout.grid_item_layout, null);
            TextView textViewName = gridView.findViewById(R.id.textViewName);
            ImageView imageViewIcon = gridView.findViewById(R.id.imageViewIcon);
            System.out.println("position : " + position);
            System.out.println("textViewName : " + folderNames.get(position));
            System.out.println("imageViewIcon : " + folderIcons.get(position));
            textViewName.setText(folderNames.get(position));
            imageViewIcon.setImageResource(folderIcons.get(position));

            if (folderNames.get(position).equals(addNewFolderName)) {
                gridView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAddFolderDialog();
                    }
                });
            } else if (folderIcons.get(position) == R.drawable.ic_folder) {
                gridView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.currentId = folderIds.get(position);
                        MainActivity.adapter.updateGridAdapter();
                    }
                });
            } else if (folderIcons.get(position) == R.drawable.note) {
                gridView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.currentId = folderIds.get(position);
                        MainActivity.noteCreator.openNote();
                    }
                });
            }


            gridView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (folderNames.get(position).equals(addNewFolderName)) {
                        return false;
                    }
                    deleteItem(folderIds.get(position));
                    return true;
                }
            });
        } else {
            gridView = convertView;
        }

        return gridView;
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
                folderNames.add(newFolderName);
                folderIcons.add(R.drawable.ic_folder);
                if (!MainActivity.dbHelper.insertIntoAssetsTable(newFolderName
                        , MainActivity.dbHelper.getTypeId("folder"), MainActivity.currentId)){
                    Toast.makeText(mContext, "cant create Folder", Toast.LENGTH_SHORT).show();
                };
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

    public void deleteItem(int id) {
        MainActivity.dbHelper.deleteAsset(id);
        MainActivity.adapter.updateGridAdapter();
    }

    public void readChildItemsOf(int pid) {
        ArrayList<String[]> assets = MainActivity.dbHelper.getAssetIdByPid(pid);
        folderIds = new ArrayList<Integer>();
        folderNames = new ArrayList<String>();
        folderIcons = new ArrayList<Integer>();
        if (assets.size() == 0) {
            //no item found
        } else {
            //sort first folders then notes
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
}