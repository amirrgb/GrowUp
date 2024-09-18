package com.example.growup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            System.out.println("Boot completed. Re-scheduling alarms... ");
            rescheduleAlarms(context);
        }
    }

    private void rescheduleAlarms(Context context) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        List<Alarm> alarms = dbHelper.getAllAlarms();
        System.out.println("size of alarms: " + alarms.size());
        for (Alarm alarm : alarms) {
            String title = alarm.getTitle();
            String message = alarm.getMessage();
            String date = alarm.getDate();
            int requestCode = Integer.parseInt(alarm.getRequestCode());
            Date alarmDate = new Date(Long.parseLong(date));
            AlarmReceiver.setAlarm(context, alarmDate.getTime(),requestCode, title, message);
        }
    }
}
