package com.example.growup;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteHandler {

    public void createNoteButton() {
        FloatingActionButton addNewNoteButton = MainActivity.activity.findViewById(R.id.fabCreateNote);
        addNewNoteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
            builder.setTitle("Create Note");
            final EditText input = new EditText(MainActivity.activity);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("Create", (dialog, which) -> {
                String noteName = input.getText().toString();
                if (noteName.isEmpty()) {
                    Tools.toast("Note name cannot be empty");
                } else {
                    if (DBHelper.insertIntoAssetsTable(noteName,
                            TypeHandler.getTypeIdByType("note"), MainActivity.currentId)) {
                        String lastId = DBHelper.getLastId();
                        DBHelper.insertIntoNotesTable(lastId, noteName, "");
                        Tools.toast("Note created");
                        MainActivity.adapter.readChildItemsOf(MainActivity.currentId);
                        MainActivity.adapter.updateGridAdapter();
                    }else{
                        Tools.toast("cant create Note");
                    }
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
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

    public static boolean saveNote(){
        EditText textViewTitle = MainActivity.activity.findViewById(R.id.textViewTitle);
        EditText textViewContent = MainActivity.activity.findViewById(R.id.textViewContent);
        String title = textViewTitle.getText().toString();
        String content = textViewContent.getText().toString();
        if (title.isEmpty() || title.trim().isEmpty()) {
            Tools.toast("Title cannot be empty");
            return false;
        } else {
            String previousTitle = DBHelper.getNote(MainActivity.currentId)[0];
            String previousMessage = DBHelper.getNote(MainActivity.currentId)[1];
            if (!previousTitle.equals(title) ||!previousMessage.equals(content)) {
                DBHelper.insertIntoNotesTable(String.valueOf(MainActivity.currentId), title, content);
            }
            return true;
        }
    }

}
