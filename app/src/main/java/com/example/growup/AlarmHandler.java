package com.example.growup;

import static com.example.growup.DBHelper.dbReadable;
import static com.example.growup.DBHelper.dbWritable;
import static com.example.growup.GridAdapter.assetsId;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.telephony.CarrierConfigManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import net.sqlcipher.Cursor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmHandler {
    public static Context context;

    public AlarmHandler(Context context) {
        AlarmHandler.context = context;
    }

    public void openAlarm(int position) {
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

        TextView labelForRepeatText = MainActivity.activity.findViewById(R.id.labelForPeriodicRepeatText);//Repeat Every
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
                periodicRepeatText.setText("2");
                labelForRepeatText2.setText(" Days");
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
                periodicRepeatText.setText("1");
                labelForRepeatText2.setText(" Hours & ");
                labelForRepeatText2.setVisibility(View.VISIBLE);

                periodicRepeatText2.setVisibility(View.VISIBLE);
                periodicRepeatText2.setText("00");
                labelForRepeatText3.setVisibility(View.VISIBLE);

                labelForStartDate.setVisibility(TextView.GONE);
            }
        });

        TimePicker timePicker = MainActivity.activity.findViewById(R.id.timePicker);

        // Date Section //
        int assetId = assetsId.get(position);
        String[] note = MainActivity.dbHelper.getNote(assetId);
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
                calendar.set(Calendar.SECOND, 0);

                int checkedRadioButtonId = alarmRadioGroup.getCheckedRadioButtonId();
                switch (checkedRadioButtonId) {
                    case R.id.todayRadioButton:
                        setAlarm(context,calendar.getTimeInMillis(), requestCode, title, message);
                        insertIntoAlarms(assetId, title, message, calendar.getTime(), "today", null, "high", requestCode);
                        break;
                    case R.id.repeatDailyRadioButton:
                        long dailyInterval = 24 * 60 * 60 * 1000;
                        setAlarm(context,calendar.getTimeInMillis(), requestCode, title, message);
                        insertIntoAlarms(assetId, title, message, calendar.getTime(), "repeatDaily", dailyInterval, "high", requestCode);
                        break;
                    case R.id.specificDateRadioButton:
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute, 0);
                        setAlarm(context,calendar.getTimeInMillis(), requestCode, title, message);
                        insertIntoAlarms(assetId, title, message, calendar.getTime(), "specificDate", null, "high", requestCode);
                        break;
                    case R.id.periodicRepeatRadioButton:
                        int days = Integer.parseInt(periodicRepeatText.getText().toString());
                        long periodicInterval = days * 24 * 60 * 60 * 1000;
                        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), hourOfDay, minute, 0);
                        setAlarm(context,calendar.getTimeInMillis(), requestCode, title, message);
                        insertIntoAlarms(assetId, title, message, calendar.getTime(), "periodicRepeat", periodicInterval, "high", requestCode);
                        break;
                    case R.id.hourlyRepeatRadioButton:
                        int hours = Integer.parseInt(periodicRepeatText.getText().toString());
                        int minutes = Integer.parseInt(periodicRepeatText2.getText().toString());
                        long hourlyInterval = hours * 60 * 60 * 1000 + minutes * 60 * 1000;
                        setAlarm(context,calendar.getTimeInMillis(), requestCode, title, message);
                        insertIntoAlarms(assetId, title, message, calendar.getTime(), "hourlyRepeat", hourlyInterval, "high", requestCode);
                        break;
                    default:
                        MainActivity.activity.runOnUiThread(() -> Toast.makeText(context, "There is a problem in setting your alarm", Toast.LENGTH_SHORT).show());
                        break;
                }

                MainActivity.activity.runOnUiThread(() -> Toast.makeText(context, "Alarm set successfully", Toast.LENGTH_SHORT).show());
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            System.out.println("alarm set for time : " + formatter.format(timeInMillis));
        } catch (Exception e) {
            LogHandler.saveLog("Failed to set alarm: " + e.getLocalizedMessage(), true);
        }
    }

    public void cancelAlarm(int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void updateSettedAlarm(int requestCode, String title, String message, Date date){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("requestCode", requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
        LogHandler.saveLog("Updated alarm for request code : " + requestCode, true);
    }


    public void insertIntoAlarms(int assetId, String title, String message, Date date,
                                 String alarmType, Long millisToNextAlarm, String priority, int requestCode) {
        try {
            dbWritable.beginTransaction();
            String sqlQuery = "INSERT INTO REMINDERS (assetId, title, message, date, alarmType, milisToNextAlarm, priority, requestCode)" +
                    " VALUES (?,?,?,?,?,?,?,?);";
            dbWritable.execSQL(sqlQuery, new Object[]{
                    assetId,
                    title,
                    message,
                    date.toString(),
                    alarmType,
                    millisToNextAlarm,
                    priority,
                    requestCode
            });
            dbWritable.setTransactionSuccessful();
            dbWritable.endTransaction();
            System.out.println("data inserted into alarms : " + assetId + " " +  title +" " + message +" " + date +" " + alarmType +" " + millisToNextAlarm +" " + priority +" " + requestCode);
        } catch (Exception e) {
            LogHandler.saveLog("Failed to insert into alarms: " + e.getLocalizedMessage(), true);
        }
    }


}
