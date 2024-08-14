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
        List<String[]> alarms = dbHelper.getAllAlarms();
        System.out.println("size of alarms: " + alarms.size());
        for (String[] alarm : alarms) {
            String title = alarm[1];
            String message = alarm[2];
            String date = alarm[3];
            int requestCode = Integer.parseInt(alarm[7]);
            Date alarmDate = new Date(Long.parseLong(date));
            AlarmReceiver.setAlarm(context, alarmDate.getTime(),requestCode, title, message);
        }
    }
}
