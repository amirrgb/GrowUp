package com.example.growup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
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
        DBHelper.getInstance(context);
        List<Alarm> alarms = DBHelper.getAllAlarms();
        System.out.println("size of alarms: " + alarms.size());

        for (Alarm alarm : alarms) {
            String title = alarm.getTitle();
            String message = alarm.getMessage();
            String date = alarm.getDate();
            int requestCode = Integer.parseInt(alarm.getRequestCode());

            try {
                Date alarmDate = Tools.dateFormat.parse(date);
                if (alarmDate != null) {
                    AlarmReceiver.setAlarm(context, alarmDate.getTime(), requestCode, title, message);
                }
            } catch (ParseException e) {
                LogHandler.saveLog("Failed to parse date: " + e.getLocalizedMessage(), true);
            }
        }
    }



}
