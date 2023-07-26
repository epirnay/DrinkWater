package com.example.myapplication;

public class WeeklyEntry {
    private String date;
    private int value;



    public WeeklyEntry(String date, int value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }
    public int getValue() {
        return value;
    }
}
