package com.example.growup;

public class Alarm {

    private String assetId;
    private String title;
    private String message;
    private String date;
    private String alarmType;
    private String milisToNextAlarm;
    private String priority;
    private String requestCode;

    public Alarm(String assetId, String title, String message, String date,
                       String alarmType, String milisToNextAlarm, String priority, String requestCode) {
        this.assetId = assetId;
        this.title = title;
        this.message = message;
        this.date = date;
        this.alarmType = alarmType;
        this.milisToNextAlarm = milisToNextAlarm;
        this.priority = priority;
        this.requestCode = requestCode;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getMilisToNextAlarm() {
        return milisToNextAlarm;
    }

    public void setMilisToNextAlarm(String milisToNextAlarm) {
        this.milisToNextAlarm = milisToNextAlarm;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }
}
