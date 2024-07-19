package com.example.growup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteHandler {
    private FloatingActionButton addNewNoteButton = MainActivity.activity.findViewById(R.id.fabCreateNote);

    public void createNoteButton() {
        addNewNoteButton = MainActivity.activity.findViewById(R.id.fabCreateNote);
        addNewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.activity);
                builder.setTitle("Create Note");
                final EditText input = new EditText(MainActivity.activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String noteName = input.getText().toString();
                        if (noteName.equals("")) {
                            MainActivity.activity.runOnUiThread(() -> {
                            Toast.makeText(MainActivity.activity, "Note name cannot be empty", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            if (MainActivity.dbHelper.insertIntoAssetsTable(noteName,
                                    MainActivity.dbHelper.getTypeId("note"), MainActivity.currentId)) {
                                String lastId = MainActivity.dbHelper.getLastId();
                                MainActivity.dbHelper.insertIntoNotesTable(lastId, noteName, "");
                                MainActivity.activity.runOnUiThread(() -> {
                                Toast.makeText(MainActivity.activity, "Note created", Toast.LENGTH_SHORT).show();
                                });
                                MainActivity.adapter.readChildItemsOf(MainActivity.currentId);
                                MainActivity.adapter.updateGridAdapter();
                            }else{
                                MainActivity.activity.runOnUiThread(() -> {
                                Toast.makeText(MainActivity.activity, "cant create Note", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
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
        });
    }

    public void openNote() {
        MainActivity.activity.setContentView(R.layout.note_item);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
        String[] temp = MainActivity.dbHelper.getNote(MainActivity.currentId);
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
        if (title.equals("") || title.equals(" ")) {
            MainActivity.activity.runOnUiThread(() -> {
            Toast.makeText(MainActivity.activity, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            });
            return false;
        } else {
            MainActivity.dbHelper.insertIntoNotesTable(String.valueOf(MainActivity.currentId), title, content);
            return true;
        }

//        MainActivity.currentId = MainActivity.dbHelper.getParentId(MainActivity.currentId);
//        MainActivity.adapter.reinitializeGridAdapter();
    }

}
