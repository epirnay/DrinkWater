package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ALERT", "Alarm received.");
        MyDatabaseHelper mdh = MyDatabaseHelper.getInstance(context.getApplicationContext());
        StepsService ss = StepsService.getInstance();
        String now = MainActivity.getFormattedDate();
        String date = now.substring(0, 8);
        String time = now.substring(8);

        // Reset on the first save
        if (ss.isFirstSave()){
            ss.setFirstSave(false);
            ss.setLastSaved(ss.getTotalSteps());
        }

        // save to database
        mdh.addStep(date, time, (int) (ss.getTotalSteps() - ss.getLastSaved()));
        // set last save and restart alarm
        ss.setLastSaved(ss.getTotalSteps());
        ss.scheduleAlarm();
    }

}
