package com.example.growup;

import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayNoteHandler {

    public static boolean onTodayNoteScreen = false;

    public static List<Alarm> getTodayNotes(){
        ArrayList<Alarm> todayNotes = new ArrayList<>();
        List<Alarm> alarms = DBHelper.getAllAlarms();
        List<Alarm> sortedAlarms = sortAlarmsByTime(alarms);
        for (Alarm alarm : sortedAlarms){
            if (isAlarmForToday(alarm.getDate())) {
                todayNotes.add(alarm);
            }
        }
        return todayNotes;
    }

    public static boolean isAlarmForToday(String date) {
        try {
            Date alarmDate;
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            alarmDate = dateFormat.parse(date);

            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTime(alarmDate);

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(new Date());

            // Check if year, month, and day match
            boolean sameYear = alarmCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
            boolean sameMonth = alarmCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH);
            boolean sameDay = alarmCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);

            return sameYear && sameMonth && sameDay;
        } catch (Exception e) {
            LogHandler.saveLog("Failed to parse date: " + e.getLocalizedMessage(), true);
            return false;
        }
    }

    public static String listAlarmsForToday() {
        List<Alarm> alarms = getTodayNotes();
        StringBuilder sb = new StringBuilder();
        for (Alarm alarm : alarms) {
            if (isAlarmForToday(alarm.getDate())) {
                sb.append(alarm.getTitle())
                    .append(" : \n")
                    .append(getHourAndMinuteOfDate(alarm.getDate()))
                    .append("\n\n- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n");
            }
        }
        return sb.toString().trim();
    }

    public static List<Alarm> sortAlarmsByTime(List<Alarm> alarms) {
        Collections.sort(alarms, (a1, a2) -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            try {
                Date date1 = dateFormat.parse(a1.getDate());
                Date date2 = dateFormat.parse(a2.getDate());
                return date1.compareTo(date2);
            } catch (Exception e) {
                LogHandler.saveLog("Failed to parse date: " + e.getLocalizedMessage(), true);
                return 0;
            }
        });
        return alarms;
    }

    public static void openTodayNote() {
        onTodayNoteScreen = true;
        MainActivity.activity.setContentView(R.layout.note_item);
        MainActivity.activity.findViewById(R.id.setting_button).setBackgroundResource(R.drawable.ic_back_button);
        EditText textViewTitle = MainActivity.activity.findViewById(R.id.textViewTitle);
        EditText textViewContent = MainActivity.activity.findViewById(R.id.textViewContent);
        textViewTitle.setText("Today");
        textViewContent.setText(listAlarmsForToday());
        Setting.setListenerForButtons();
    }

    public static String getHourAndMinuteOfDate(String date){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date1 = dateFormat.parse(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            return simpleDateFormat.format(date1);
        }catch (Exception e){
            LogHandler.saveLog("failed to parse date in getHourAndMinuteOfDate method : " + e.getLocalizedMessage(), true);
        }
    return "";
    }
}
