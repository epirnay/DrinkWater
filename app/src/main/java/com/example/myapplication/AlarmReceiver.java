package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyDatabaseHelper mdh = MyDatabaseHelper.getInstance(context.getApplicationContext());
        StepsService ss = StepsService.getInstance();
        String now = MainActivity.getFormattedDate();
        String date = now.substring(0, 8);
        String time = now.substring(8);
        if (ss.isFirstSave()){
            ss.setFirstSave(false);
            ss.setLastSaved(ss.getTotalSteps());
        }
        mdh.addStep(date, time, (int) (ss.getTotalSteps() - ss.getLastSaved()));
        scheduleAlarm(context);
    }
    private void scheduleAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        long repeatInterval = 60 * 60 * 1000;
        long triggerAtMillis = System.currentTimeMillis() + repeatInterval;  // 1 hour from now
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            else {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, repeatInterval, pendingIntent);
            }
        }
    }
}
