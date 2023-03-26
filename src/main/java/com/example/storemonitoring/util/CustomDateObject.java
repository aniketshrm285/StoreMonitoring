package com.example.storemonitoring.util;


public class CustomDateObject {
    private int year;
    private int month;
    private int day;
    private int hr;
    private int minute;
    private int sec;

    public CustomDateObject(int year, int month, int day, int hr, int minute, int sec) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hr = hr;
        this.minute = minute;
        this.sec = sec;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }
}
