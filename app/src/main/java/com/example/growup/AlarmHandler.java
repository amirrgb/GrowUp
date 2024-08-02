package com.example.growup;

import static com.example.growup.GridAdapter.assetsId;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;
import java.util.Date;

import android.app.TimePickerDialog;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmHandler {
    private final Context context;

    public AlarmHandler(Context context) {
        this.context = context;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setAlarm(long timeInMillis, int requestCode, String title, String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    public void cancelAlarm(int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
    }



    public static void showChooseTime(Context context, int assetId) {
        final Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, selectedHour, selectedMinute) -> {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, selectedHour);
            alarmTime.set(Calendar.MINUTE, selectedMinute);
            alarmTime.set(Calendar.SECOND, 0);

            AlarmHandler alarmHandler = new AlarmHandler(context);
            String[] note = MainActivity.dbHelper.getNote(assetId);
            String title = note[0];
            String message = note[1];

            int requestCode = new Date().hashCode();
            alarmHandler.setAlarm(alarmTime.getTimeInMillis(), requestCode, title, message);

            Toast.makeText(context, "Alarm set for " + selectedHour + ":" + selectedMinute, Toast.LENGTH_SHORT).show();
        }, hour, minute, true);

        // Show the TimePickerDialog
        timePickerDialog.show();
    }

}
