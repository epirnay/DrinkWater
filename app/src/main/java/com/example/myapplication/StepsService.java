package com.example.myapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StepsService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    public static final String CHANNEL_ID = "StepsServiceChannel";
    public static final int NOTIF_ID = 101;
    private float totalSteps = 0f;
    private float previousTotalSteps = 0f;
    private boolean stepsInit = true;
    private float lastSaved = 0f;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean firstSave = true;
    private static StepsService instance = null;
    private int remindTime = 60;
    MyDatabaseHelper myDatabaseHelper;
    private final Runnable saveSteps = new Runnable() {
        @Override
        public void run() {
            saveSteps();
            int b=myDatabaseHelper.getLastDataFromColumn("settingsDB","remind_step");
            handler.postDelayed(this, 60 * 60 * 1000); // every 1 hour
        }
    };


    private void saveSteps() {
        String dateTimeString = MainActivity.getFormattedDate();
        int stepsToSave = (int) (totalSteps - lastSaved);
        if(firstSave){
            stepsToSave = 0;
            firstSave = false;
        }
        lastSaved = totalSteps;
        MyDatabaseHelper myDatabaseHelper = MyDatabaseHelper.getInstance(this);
        String datee = dateTimeString.substring(0,8);
        String timee = dateTimeString.substring(8,14);
        myDatabaseHelper.addStep(datee, timee, stepsToSave);
    }


    @Override
    public void onCreate(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        instance = this;
        scheduleAlarm();
    }
    public static StepsService getInstance() {
        return instance;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.equals(stepSensor)){
            totalSteps = (int) sensorEvent.values[0];

            int currentSteps = (int) (totalSteps - previousTotalSteps);
            if(stepsInit){
                previousTotalSteps = totalSteps;
                stepsInit = false;
                currentSteps = 0;
            }
            updateNotification(currentSteps);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Steps Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class); // Assuming you want to go to MainActivity when clicking the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter")
                .setContentText("0")
                .setSmallIcon(R.drawable.ic_steps) // Replace this with your icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIF_ID, notification);

        // Register the sensor listener
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }
    private void updateNotification(int currentSteps) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter")
                .setContentText("Steps: " + currentSteps)
                .setSmallIcon(R.drawable.ic_steps) // Make sure you have this icon in your drawable folder
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID, notification);
    }
    public void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //remindTime = myDatabaseHelper.getLastDataFromColumn("settingsDB","remind_step");
        long repeatInterval = 60 * 60 * 1000;  // Every hour
        long triggerTime = System.currentTimeMillis() + repeatInterval;

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
            else {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, repeatInterval, pendingIntent);
            }
        }
    }

    public float getTotalSteps() {
        return totalSteps;
    }

    public void setLastSaved(float lastSaved) {
        this.lastSaved = lastSaved;
    }

    public float getLastSaved() {
        return lastSaved;
    }

    public boolean isFirstSave() {
        return firstSave;
    }

    public void setFirstSave(boolean firstSave) {
        this.firstSave = firstSave;
    }
}
