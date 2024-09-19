package com.example.growup;

public class Alarm {
    private final String title;
    private final String message;
    private final String date;
    private final String requestCode;

    public Alarm(String title, String message, String date, String requestCode) {
        this.title = title;
        this.message = message;
        this.date = date;
        this.requestCode = requestCode;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getRequestCode() {
        return requestCode;
    }
}
