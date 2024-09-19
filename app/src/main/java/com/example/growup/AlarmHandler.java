package com.example.growup;

import static com.example.growup.DBHelper.dbWritable;
import static com.example.growup.GridAdapter.assetsId;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmHandler {

    public static void openAlarm(int position) {
        MainActivity.activity.setContentView(R.layout.alarm_layout);
        MainActivity.onSetAlarmScreen = true;
        Button setAlarmButton = MainActivity.activity.findViewById(R.id.setAlarmButton);

        RadioGroup alarmRadioGroup = MainActivity.activity.findViewById(R.id.alarmRadioGroup);
        RadioButton todayRadioButton = MainActivity.activity.findViewById(R.id.todayRadioButton);
        RadioButton repeatDailyRadioButton = MainActivity.activity.findViewById(R.id.repeatDailyRadioButton);
        RadioButton specificDateRadioButton = MainActivity.activity.findViewById(R.id.specificDateRadioButton);
        RadioButton periodicRepeatRadioButton = MainActivity.activity.findViewById(R.id.periodicRepeatRadioButton);
        RadioButton hourlyRepeatRadioButton = MainActivity.activity.findViewById(R.id.hourlyRepeatRadioButton);

        DatePicker datePicker = MainActivity.activity.findViewById(R.id.datePicker);
        View periodicRepeatLayout = MainActivity.activity.findViewById(R.id.periodicRepeatLayout);
        TextView labelForStartDate = MainActivity.activity.findViewById(R.id.labelForStartDate);//Start Date :

        EditText periodicRepeatText = MainActivity.activity.findViewById(R.id.periodicRepeatText);//day | hour As input
        EditText periodicRepeatText2 = MainActivity.activity.findViewById(R.id.periodicRepeatText2);//minute As input

        TextView labelForRepeatText2 = MainActivity.activity.findViewById(R.id.labelForPeriodicRepeatText2);//Days | Hours &
        TextView labelForRepeatText3 = MainActivity.activity.findViewById(R.id.labelForPeriodicRepeatText3);//Minutes

        todayRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                datePicker.setVisibility(Button.GONE);
                periodicRepeatLayout.setVisibility(View.GONE);
                labelForStartDate.setVisibility(TextView.GONE);
            }
        });

        repeatDailyRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                datePicker.setVisibility(Button.GONE);
                periodicRepeatLayout.setVisibility(View.GONE);
                labelForStartDate.setVisibility(TextView.GONE);
            }
        });

        specificDateRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                datePicker.setVisibility(Button.VISIBLE);
                periodicRepeatLayout.setVisibility(View.GONE);
                labelForStartDate.setVisibility(TextView.GONE);
            }
        });

        periodicRepeatRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                datePicker.setVisibility(Button.VISIBLE);
                periodicRepeatLayout.setVisibility(View.VISIBLE);
                periodicRepeatText.setText("");
                labelForRepeatText2.setText(R.string.days);
                labelForRepeatText2.setVisibility(View.VISIBLE);

                periodicRepeatText2.setVisibility(View.GONE);
                labelForRepeatText3.setVisibility(View.GONE);

                labelForStartDate.setVisibility(TextView.VISIBLE);
            }
        });

        hourlyRepeatRadioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                datePicker.setVisibility(Button.GONE);
                periodicRepeatLayout.setVisibility(View.VISIBLE);
                periodicRepeatText.setText("");
                labelForRepeatText2.setText(R.string.hours);
                labelForRepeatText2.setVisibility(View.VISIBLE);

                periodicRepeatText2.setVisibility(View.VISIBLE);
                periodicRepeatText2.setText("");
                labelForRepeatText3.setVisibility(View.VISIBLE);

                labelForStartDate.setVisibility(TextView.GONE);
            }
        });

        TimePicker timePicker = MainActivity.activity.findViewById(R.id.timePicker);

        // Date Section //
        int assetId = assetsId.get(position);
        String[] note = DBHelper.getNote(assetId);
        String title = note[0];
        String message = note[1];
        int requestCode = (int) new Date().getTime();




        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                int minute = timePicker.getMinute();
                int hourOfDay = timePicker.getHour();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String alarmType;
                Long intervel = null;

                int checkedRadioButtonId = alarmRadioGroup.getCheckedRadioButtonId();
                switch (checkedRadioButtonId) {
                    case R.id.todayRadioButton:
                        alarmType = "today";
                        break;
                    case R.id.repeatDailyRadioButton:
                        intervel = (long) (24 * 60 * 60 * 1000);
                        alarmType = "repeatDaily";
                        break;
                    case R.id.specificDateRadioButton:
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute, 0);
                        alarmType = "specificDate";
                        break;
                    case R.id.periodicRepeatRadioButton:
                        int days;
                        String daysString = periodicRepeatText.getText().toString();
                        if (!daysString.isEmpty()){
                            days = Integer.parseInt(daysString);
                            if (days <= 0) {
                                Tools.toast("Please enter a positive number of days");
                                return;
                            }
                        }else {
                            Tools.toast("Please enter number of days");
                            return;
                        }
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute, 0);
                        alarmType = "periodicRepeat";
                        intervel =  (long) days * 24 * 60 * 60 * 1000;
                        break;
                    case R.id.hourlyRepeatRadioButton:
                        String hoursString = periodicRepeatText.getText().toString();
                        int hours = 0;
                        int minutes = 0;
                        String minutesString = periodicRepeatText2.getText().toString();
                        if (hoursString.isEmpty() && minutesString.isEmpty()){
                            Tools.toast("Please enter number of hours and minutes");
                            return;
                        }
                        if (!hoursString.isEmpty()){
                            hours = Integer.parseInt(hoursString);
                        }
                        if (!minutesString.isEmpty()){
                            minutes = Integer.parseInt(minutesString);
                        }
                        if (hours < 0 || minutes < 0 || (hours == 0 && minutes == 0)) {
                            Tools.toast("Please enter a positive number of hours and minutes");
                            return;
                        }
                        intervel = (long) hours * 60 * 60 * 1000 + (long) minutes * 60 * 1000;
                        alarmType = "hourlyRepeat";
                        break;
                    default:
                        Tools.toast("There is a problem in setting your alarm");
                        return;
                }

                if (calendar.getTime().before(new Date())) {
                    if (checkedRadioButtonId == R.id.todayRadioButton){
                        // add 1 day to calendar
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }else {
                        Tools.toast("The alarm date is not in the future");
                        return;
                    }
                }
                setAlarm(MainActivity.activity,calendar.getTimeInMillis(),requestCode,title,message);
                insertIntoAlarms(assetId,title,message,calendar.getTime(),alarmType,intervel,"high",requestCode);
                Tools.toast("Alarm set successfully");
                MainActivity.onSetAlarmScreen = false;
                GridAdapter.initializeGridAdapter();
            }
        });
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void setAlarm(Context context,long timeInMillis, int requestCode, String title, String message) {
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

    public static void insertIntoAlarms(int assetId, String title, String message, Date date,
                                 String alarmType, Long millisToNextAlarm, String priority, int requestCode) {
        String dateString = Tools.dateFormat.format(date);
        try {
            dbWritable.beginTransaction();
            String sqlQuery = "INSERT INTO REMINDERS (assetId, title, message, date, alarmType, millisToNextAlarm, priority, requestCode)" +
                    " VALUES (?,?,?,?,?,?,?,?);";
            dbWritable.execSQL(sqlQuery, new Object[]{
                    assetId,
                    title,
                    message,
                    dateString,
                    alarmType,
                    millisToNextAlarm,
                    priority,
                    requestCode
            });
            dbWritable.setTransactionSuccessful();
            dbWritable.endTransaction();
            System.out.println("data inserted into alarms : " + assetId + " " +  title +" " + message +" " + dateString +" " + alarmType +" " + millisToNextAlarm +" " + priority +" " + requestCode);
        } catch (Exception e) {
            LogHandler.saveLog("Failed to insert into alarms: " + e.getLocalizedMessage(), true);
        }
    }

    public static void rescheduleAlarms(Context context) {
        List<Alarm> alarms = DBHelper.getAllAlarms();
        System.out.println("size of alarms: " + alarms.size());

        for (Alarm alarm : alarms) {
            String title = alarm.getTitle();
            String message = alarm.getMessage();
            String date = alarm.getDate();
            int requestCode = Integer.parseInt(alarm.getRequestCode());

            try {
                Date alarmDate = Tools.dateFormat.parse(date);

                if (alarmDate != null && alarmDate.getTime() > System.currentTimeMillis()) {
                    if (!hasAlarmSet(context, requestCode)){
                        AlarmReceiver.setAlarm(context, alarmDate.getTime(), requestCode, title, message);
                    }else{
                        System.out.println("Alarm already set for request code: " + requestCode + " and date : " + alarmDate);
                    }
                } else {
                    AlarmReceiver.deleteAlarmFromDatabase(context, requestCode);
                    System.out.println("Alarm date is in the past: " + alarmDate);
                }

            } catch (Exception e) {
                LogHandler.saveLog("Failed to parse date: " + e.getLocalizedMessage(), true);
            }
        }
    }

    public static boolean hasAlarmSet(Context context, int requestCode) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        return pendingIntent!= null;
    }
}
