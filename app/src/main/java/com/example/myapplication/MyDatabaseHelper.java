package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Spannable;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MyDatabaseHelper extends SQLiteOpenHelper{

    private Context context;
    public static final String DATABASE_NAME = "WaterApp.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME= "water_intake";

    public static final String TABLE_NAME2= "stepCountDB";

    public static final String TABLE_NAME3= "settingsDB";
    public static final String COLUMN_ID = "id";

    public static final String COLUMN_DATE= "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_INTAKE = "ml";

    public static final String COLUMN_STEP= "step_count";

    public static final String COLUMN_DAILYINTAKE= "daily_intake";
    public static final String COLUMN_DAILYSTEP= "daily_step";

    public static final String COLUMN_REMINDSTEP= "remind_step";
    public static final String COLUMN_REMINDMINS= "remind_mins";


    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_INTAKE + " INTEGER) ;";
        db.execSQL(query);

        String query2 = "CREATE TABLE " + TABLE_NAME2 +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_STEP + " INTEGER) ;";
        db.execSQL(query2);

        String query3 = "CREATE TABLE " + TABLE_NAME3 +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAILYINTAKE + " INTEGER, " +
                COLUMN_DAILYSTEP + " INTEGER, " +
                COLUMN_REMINDSTEP + " INTEGER, " +
                COLUMN_REMINDMINS + " INTEGER) ;";
        db.execSQL(query3);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME2);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME3);
        onCreate(db);
    }

    void addIntake(String date, String time, int ml ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_INTAKE, ml);

        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1){
            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    void addStep (String date, int step_count){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_STEP, step_count);
        long result = db.insert(TABLE_NAME2, null, cv);
    }

    //CREATE DATABASE TABLE OF SETTING SCREEEN VALUES
    void SettingValues (int daily_intake, int daily_step, int remind_step, int remind_mins){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DAILYINTAKE, daily_intake);
        cv.put(COLUMN_DAILYSTEP, daily_step);
        cv.put(COLUMN_REMINDSTEP, remind_step);
        cv.put(COLUMN_REMINDMINS, remind_mins);
        long result = db.insert(TABLE_NAME3, null, cv);
    }

    public Map<String, Integer> getDailyWaterConsumption() {

        Map<String, Integer> dailyWaterConsumptionMap = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.

        rawQuery("SELECT date as date, SUM(ml) as total_water FROM water_intake GROUP BY date", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") int totalWaterConsumed = cursor.getInt(cursor.getColumnIndex("total_water"));

                // Add the date and total water consumption to the map.
                dailyWaterConsumptionMap.put(date, totalWaterConsumed);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return dailyWaterConsumptionMap;
    }
    public Map<String, Integer> getStepCount() {

        Map<String, Integer> dailyStepMap = new HashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.

                rawQuery("SELECT date, step_count as total_step FROM stepCountDB GROUP BY date", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") int totalStep = cursor.getInt(cursor.getColumnIndex("total_step"));

                // Add the date and total water consumption to the map.
                dailyStepMap.put(date, totalStep);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return dailyStepMap;
    }
}