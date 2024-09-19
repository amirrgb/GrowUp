package com.example.growup;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteHandler {

    public void createNoteButton() {
        FloatingActionButton addNewNoteButton = MainActivity.activity.findViewById(R.id.fabCreateNote);
        addNewNoteButton.setOnClickListener(v -> {
            // create temp note and open it
            openTempNote();
            if (DBHelper.insertIntoAssetsTable(MainActivity.tempNoteString,
                    TypeHandler.getTypeIdByType("note"), MainActivity.currentId)) {
                String lastId = DBHelper.getLastId();
                MainActivity.currentId = Integer.parseInt(lastId);
                DBHelper.insertIntoNotesTable(lastId, MainActivity.tempNoteString, "");
            }else{
                Tools.toast("cant create Note");
                MainActivity.backButtonProcess();
            }
        });
    }

    public void openNote() {
        MainActivity.activity.setContentView(R.layout.note_item);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
        String[] temp = DBHelper.getNote(MainActivity.currentId);
        String title = temp[0];
        String content = temp[1];
        EditText textViewTitle = MainActivity.activity.findViewById(R.id.textViewTitle);
        EditText textViewContent = MainActivity.activity.findViewById(R.id.textViewContent);
        textViewTitle.setText(title);
        textViewContent.setText(content);
        Setting.setListenerForButtons();
    }

    public static void openTempNote(){
        MainActivity.activity.setContentView(R.layout.note_item);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
        Setting.setListenerForButtons();
    }

    public static String saveNote(){
        EditText textViewTitle = MainActivity.activity.findViewById(R.id.textViewTitle);
        EditText textViewContent = MainActivity.activity.findViewById(R.id.textViewContent);
        String title = textViewTitle.getText().toString();
        String content = textViewContent.getText().toString();
        String previousTitle = DBHelper.getNote(MainActivity.currentId)[0];
        if (title.isEmpty() || title.trim().isEmpty()) {
            Tools.toast("Title cannot be empty");
            if (previousTitle.equals(MainActivity.tempNoteString)) {
                return "temp_failure";
            }
            return "failure";
        } else {
            String previousMessage = DBHelper.getNote(MainActivity.currentId)[1];
            if (!previousTitle.equals(title) ||!previousMessage.equals(content)) {
                DBHelper.insertIntoNotesTable(String.valueOf(MainActivity.currentId), title, content);
            }
            return "success";
        }
    }

    public static void deleteTempNote(int tempNoteID){
        DBHelper.deleteAsset(tempNoteID);
    }

}
