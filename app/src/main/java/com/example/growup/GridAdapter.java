package com.example.growup;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GridAdapter extends BaseAdapter {

    public static Context mContext;
    public static final int tempAssetId = -2;
    public static ArrayList<String> assetsName;
    public static ArrayList<Integer> assetsIcon;
    public static ArrayList<Integer> assetsId;
    public static boolean movingStatus = false;
    public static int movingAssetId = -1;


    public static void initializeGridAdapter() {
        MainActivity.activity.setContentView(R.layout.activity_main);
        MainActivity.gridView = MainActivity.activity.findViewById(R.id.gridView);
        MainActivity.adapter = new GridAdapter();
        MainActivity.adapter.updateGridAdapter();
        if (TypeHandler.isAssetNote(MainActivity.currentId)){
            NoteHandler.openNote();
        }
    }

    public GridAdapter() {
        readChildItemsOf(MainActivity.currentId);
        mContext = MainActivity.activity;
        NoteHandler.createNoteButton();
    }

    public void updateGridAdapter() {
        notifyDataSetChanged();
        readChildItemsOf(MainActivity.currentId);
        NoteHandler.createNoteButton();
        notifyDataSetChanged();
        MainActivity.gridView.setAdapter(MainActivity.adapter);
        Setting.setListenerForButtons();
        updateHeader();
    }

    public void readChildItemsOf(int pid) {
        ArrayList<String[]> assets = DBHelper.getAssetIdByPid(pid);
        ArrayList<ArrayList<String[]>> sortedAssets = categorizeAssets(assets);
        assetsId = new ArrayList<>();
        assetsName = new ArrayList<>();
        assetsIcon = new ArrayList<>();

        if (MainActivity.currentId == 0) {
            assetsName.add("Today");
            assetsIcon.add(R.drawable.ic_today);
            assetsId.add(tempAssetId);
        }

        for (ArrayList<String[]> assetTypes : sortedAssets) {
            for (String[] asset : assetTypes) {
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
        Button pasteButton = MainActivity.activity.findViewById(R.id.pasteHereButton);
        if (GridAdapter.movingStatus){
            pasteButton.setVisibility(View.VISIBLE);
            pasteButton.setOnClickListener(v -> {
                DBHelper.moveAsset(GridAdapter.movingAssetId, MainActivity.currentId);
                GridAdapter.movingStatus = false;
                GridAdapter.movingAssetId = -1;
                MainActivity.adapter.updateGridAdapter();
            });
        }else{
            pasteButton.setVisibility(View.GONE);
        }
        if (MainActivity.currentId == 0){
            header.setText(R.string.Home);
            MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.app_setting);
            return;
        }
        String headerText = DBHelper.getAsset(String.valueOf(MainActivity.currentId))[2];
        header.setText(headerText);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
    }

    @Override
    public int getCount() {
        return assetsId.size();
    }

    @Override
    public Object getItem(int position) {
        return assetsName.get(position);
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
        } else {
            gridView = convertView;
        }

        TextView textViewName = gridView.findViewById(R.id.textViewName);
        ImageView imageViewIcon = gridView.findViewById(R.id.imageViewIcon);

        String assetName = assetsName.get(position);
        textViewName.setText(limitAssetNameText(assetName));
        imageViewIcon.setImageResource(assetsIcon.get(position));

        gridView.setOnClickListener(v -> itemsActions(position));

        gridView.setOnLongClickListener(v -> {
            GridItemsPopupMenu.displayPopUpMenu(position, gridView, mContext);
            return true;
        });

        return gridView;
    }


    private void itemsActions(int position) {
        if (assetsIcon.get(position) == R.drawable.ic_add_folder) {
            showAddFolderDialog();
            return;
        }

        if (assetsIcon.get(position) == R.drawable.ic_today) {
            TodayNoteHandler.openTodayNote();
        }

        int assetId = assetsId.get(position);
        switch (TypeHandler.getTypeNameByAssetId(assetId)) {
            case "pin_note":
            case "note":
                if (movingStatus){
                    Tools.toast("paste selected asset first");
                    return;
                }
                MainActivity.currentId = assetsId.get(position);
                MainActivity.noteCreator.openNote();
                break;
            case "pin_folder" :
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

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newFolderName = input.getText().toString();
            if (newFolderName.isEmpty()){
                Tools.toast("Folder name can't be empty");
                return;
            }
            boolean isCreated = DBHelper.insertIntoAssetsTable(newFolderName,
                    TypeHandler.getTypeIdByType("folder"), MainActivity.currentId);
            if (!isCreated){
                Tools.toast("cant create Folder");
            }
            MainActivity.adapter.updateGridAdapter();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static ArrayList<String[]> sortAssets(ArrayList<String[]> assets, boolean isAscending) {


        Collections.sort(assets, (o1, o2) -> {
            try {
                Date updatedAt1 = Tools.dateFormat.parse(o1[3]);
                Date updatedAt2 = Tools.dateFormat.parse(o2[3]);
                if (updatedAt1 == null || updatedAt2 == null){
                    return 0;
                }
                return isAscending ? updatedAt1.compareTo(updatedAt2) : updatedAt2.compareTo(updatedAt1);
            } catch (ParseException e) {
                LogHandler.saveLog("Error while sorting assets: " + e.getMessage(), true);
                return 0;
            }
        });
        return assets;
    }

    public static ArrayList<ArrayList<String[]>> categorizeAssets(ArrayList<String[]> assets) {
        ArrayList<String[]> pinFolders = new ArrayList<>();
        ArrayList<String[]> pinNotes = new ArrayList<>();
        ArrayList<String[]> folders = new ArrayList<>();
        ArrayList<String[]> notes = new ArrayList<>();

        for (String[] asset : assets) {
            int typeId = Integer.parseInt(asset[2]);

            switch (TypeHandler.getTypeNameByTypeId(typeId)) {
                case "folder":
                    folders.add(asset);
                    break;
                case "note":
                    notes.add(asset);
                    break;
                case "pin_folder":
                    pinFolders.add(asset);
                    break;
                case "pin_note":
                    pinNotes.add(asset);
                    break;
                default:
                    break;
            }
        }

        ArrayList<ArrayList<String[]>> allAssets = new ArrayList<>();
        allAssets.add(sortAssets(pinFolders, false)); // Sort by newest to oldest
        allAssets.add(sortAssets(pinNotes, false)); // Sort by newest to oldest
        allAssets.add(sortAssets(folders, false)); // Sort by newest to oldest
        allAssets.add(sortAssets(notes, false)); // Sort by newest to oldest
        return allAssets;
    }

    public static String limitAssetNameText(String name){
        if (name.length() > 30){
            return name.substring(0, 30) + "...";
        }
        return name;
    }
}