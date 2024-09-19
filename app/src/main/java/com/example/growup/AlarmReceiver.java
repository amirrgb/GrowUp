package com.example.growup;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final String ACTION_STOP_ALARM = "com.example.growup.ACTION_STOP_ALARM";
    private static final String ACTION_STOP_AND_CANCEL_ALARMS = "com.example.growup.ACTION_STOP_AND_CANCEL_ALARMS";
    private static Ringtone ringtone;




    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("alarm received : " + intent.getStringExtra("title"));
        String action = intent.getAction();

        boolean hasNextAlarm = false;
        int requestCode = intent.getIntExtra("requestCode", 0);
        System.out.println("requestCode : " + requestCode);
        if (requestCode == 0) {
            return;
        }

        if (ACTION_STOP_ALARM.equals(action)) {
            stopAlarmSound();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            return;
        }
        else if (ACTION_STOP_AND_CANCEL_ALARMS.equals(action)) {
            stopAlarmSound();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
            deleteAlarmFromDatabase(context, requestCode);
            cancelAlarm(context,requestCode);
            return;
        }




        String[] alarm = getAlarmsData(context, requestCode);
        System.out.println("alarm data : " + alarm[0] + ", " + alarm[1]);
        if (alarm[0] == null) {
            return;
        }

        String title = alarm[1];
        String message = alarm[2];
        String alarmType = alarm[4];
        long millisToNextAlarm = Long.parseLong(alarm[5]);

        if (alarmType.equals("repeatDaily") || alarmType.equals("periodicRepeat") || alarmType.equals("hourlyRepeat")) {
            hasNextAlarm = true;
            Date nextAlarmDate = new Date(System.currentTimeMillis() + millisToNextAlarm);
            setAlarm(context, nextAlarmDate.getTime(), requestCode, title, message);
            updateAlarms(context, requestCode, title, message, nextAlarmDate);
        }else{
            deleteAlarmFromDatabase(context, requestCode);
        }

        createNotificationChannel(context);
        playAlarmSound(context);

        Intent stopIntent = new Intent(context, AlarmReceiver.class);
        stopIntent.setAction(ACTION_STOP_ALARM);
        stopIntent.putExtra("requestCode", requestCode);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context, requestCode, stopIntent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.note)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_lets_go, "Stop", stopPendingIntent);

        if (hasNextAlarm) {
            Intent cancelNextAlarmsIntent = new Intent(context, AlarmReceiver.class);
            cancelNextAlarmsIntent.setAction(ACTION_STOP_AND_CANCEL_ALARMS);
            cancelNextAlarmsIntent.putExtra("requestCode", requestCode);
            PendingIntent cancelNextAlarmsPendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, cancelNextAlarmsIntent, PendingIntent.FLAG_MUTABLE);
            builder.addAction(R.drawable.ic_lets_go, "Stop & Cancel", cancelNextAlarmsPendingIntent);
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void playAlarmSound(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(context, alarmUri);
            ringtone.play();
        } catch (Exception e) {
            LogHandler.saveLog("Error while playing alarm sound: " + e.getMessage(), true);
        }
    }

    private void stopAlarmSound() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    public void updateAlarms(Context context, int requestCode, String title, String message, Date date) {
        String ENCRYPTION_KEY = context.getResources().getString(R.string.ENCRYPTION_KEY);
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase(ENCRYPTION_KEY);

        try {
            db.beginTransaction();
            String sqlQuery = "UPDATE REMINDERS SET title =?, message =?, date =? WHERE requestCode =?;";
            db.execSQL(sqlQuery, new Object[]{
                    title,
                    message,
                    date.toString(),
                    requestCode
            });
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            LogHandler.saveLog("Failed to update alarms: " + e.getLocalizedMessage(), true);
        }
    }

    public String[] getAlarmsData(Context context, int requestCode) {
        String ENCRYPTION_KEY = context.getResources().getString(R.string.ENCRYPTION_KEY);
        SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase(ENCRYPTION_KEY);
        Cursor cursor = null;

        try {
            String sqlQuery = "SELECT assetId, title, message, date, alarmType, millisToNextAlarm, priority FROM REMINDERS WHERE requestCode=?";
            cursor = db.rawQuery(sqlQuery, new String[]{String.valueOf(requestCode)});

            if (cursor != null && cursor.moveToFirst()) {
                return new String[]{
                        cursor.getInt(0) + "", // assetId
                        cursor.getString(1),   // title
                        cursor.getString(2),   // message
                        cursor.getString(3),   // date
                        cursor.getString(4),   // alarmType
                        cursor.getLong(5) + "",// millisToNextAlarm
                        cursor.getString(6)    // priority
                };
            }
        } catch (Exception e) {
            LogHandler.saveLog("Failed to get alarms data: " + e.getLocalizedMessage(), true);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }


    @SuppressLint("ScheduleExactAlarm")
    public static void setAlarm(Context context, long timeInMillis, int requestCode, String title, String message) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.putExtra("requestCode", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            System.out.println("alarm set for time : " + Tools.dateFormat.format(timeInMillis));
        } catch (Exception e) {
            LogHandler.saveLog("Failed to set alarm: " + e.getLocalizedMessage(), true);
        }
    }

    public static void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
        System.out.println("alarm cancelled for requestCode : " + requestCode);
    }

    public static void deleteAlarmFromDatabase(Context context, int requestCode) {
        String ENCRYPTION_KEY = context.getResources().getString(R.string.ENCRYPTION_KEY);
        SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase(ENCRYPTION_KEY);
        try {
            db.beginTransaction();
            String sqlQuery = "DELETE FROM REMINDERS WHERE requestCode =?;";
            db.execSQL(sqlQuery, new String[]{String.valueOf(requestCode)});
            db.setTransactionSuccessful();
            db.endTransaction();
            System.out.println("alarm deleted for requestCode : " + requestCode);
        } catch (Exception e) {
            LogHandler.saveLog("Failed to delete alarm from database: " + e.getLocalizedMessage(), true);
        }
    }
}
