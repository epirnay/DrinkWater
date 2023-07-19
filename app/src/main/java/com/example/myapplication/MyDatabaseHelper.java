package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Spannable;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

public class MyDatabaseHelper extends SQLiteOpenHelper{

    private Context context;
    public static final String DATABASE_NAME = "WaterApp.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME= "water_intake";
    public static final String COLUMN_ID = "id";

    public static final String COLUMN_DATE= "date";
    public static final String COLUMN_INTAKE = "ml";


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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addIntake(String date, int ml ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_INTAKE, ml);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1){
            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
